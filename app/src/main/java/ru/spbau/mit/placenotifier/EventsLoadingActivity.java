package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.spbau.mit.placenotifier.CalendarLoader.EventDescriptor;
import ru.spbau.mit.placenotifier.SmartListAdapter.ViewHolder;

import static android.os.AsyncTask.Status.FINISHED;
import static ru.spbau.mit.placenotifier.CalendarLoaderFragment.toSerializableList;

public class EventsLoadingActivity extends AppCompatActivity {

    private static final String PROTOTYPES_LIST_KEY = "prototypes_list";
    private static final String RESULT_KEY = "results_list";

    private List<Alarm> createdAlarms;
    private List<EventDescriptor> prototypes;

    private SmartListAdapter<Integer> listAdapter;
    private ProgressBar progressBar;
    private AlarmConverter converter;
    private AsyncConverter runningTask;

    static Intent prepareIntent(@NonNull List<EventDescriptor> prototypes,
                                @NonNull Context context) {
        Intent result = new Intent(context, EventsLoadingActivity.class);
        result.putExtra(PROTOTYPES_LIST_KEY, (Serializable) toSerializableList(prototypes));
        return result;
    }

    @NonNull
    private static List<EventDescriptor> getPrototypes(@NonNull Intent data) {
        Object result = data.getSerializableExtra(PROTOTYPES_LIST_KEY);
        //noinspection unchecked
        return result == null ? Collections.emptyList() : (List<EventDescriptor>) result;
    }

    @NonNull
    private static List<Alarm> getResult(@NonNull Bundle data) {
        Object result = data.getSerializable(RESULT_KEY);
        //noinspection unchecked
        return result == null ? new ArrayList<>() : (List<Alarm>) result;
    }

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.activity_events_loading);
        prototypes = getPrototypes(getIntent());
        int eventsCount = prototypes.size();
        progressBar = (ProgressBar) findViewById(R.id.loading_progress);
        progressBar.setMax(eventsCount);

        listAdapter = new SmartListAdapter<>(() -> new Range(0, eventsCount),
                R.layout.events_loading_item, EventHolder::new, this);
        listAdapter.setComparator(Integer::compare);

        ListView listView = (ListView) findViewById(R.id.loaded_events_list);
        listView.setAdapter(listAdapter);

        createdAlarms = savedState == null? new ArrayList<>() : getResult(savedState);
        progressBar.setProgress(createdAlarms.size());
        if (createdAlarms.size() == prototypes.size()) {
            progressBar.setVisibility(View.GONE);
        }

        converter = new AlarmConverter(this);

        runningTask = (AsyncConverter) getLastCustomNonConfigurationInstance();
        if (runningTask == null) {
            runNewTask();
        } else if (runningTask.getStatus() == FINISHED) {
            onTaskFinished(runningTask);
        } else {
            runningTask.observe(this);
        }
        updateGUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RESULT_KEY, (Serializable) createdAlarms);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return runningTask;
    }

    private void updateGUI() {
        progressBar.setProgress(createdAlarms.size());
        if (createdAlarms.size() == prototypes.size()) {
            progressBar.setVisibility(View.GONE);
        }
        listAdapter.refresh();
    }

    private void runNewTask() {
        if (createdAlarms.size() == prototypes.size()) {
            runningTask = null;
            return;
        }
        runningTask = new AsyncConverter(prototypes.get(createdAlarms.size()), converter);
        runningTask.observe(this);
        runningTask.execute();
    }

    private void onTaskFinished(@NonNull AsyncConverter task) {
        if (createdAlarms.size() == prototypes.size()) {
            return;
        }
        String expectedID = prototypes.get(createdAlarms.size()).getId();
        String realID = task.prototype.getId();
        if (expectedID.equals(realID)) {
            Alarm result;
            try {
                result = task.get();
            } catch (Exception e) {
                // unreachable situation because this method calls
                // when result has been already calculated
                result = null;
            }
            createdAlarms.add(result);
            updateGUI();
            runNewTask();
        }
    }

    private static final class Range extends AbstractList<Integer> {

        private final int leftBound;
        private final int rightBound;

        Range(int leftBound, int rightBound) {
            this.leftBound = leftBound;
            this.rightBound = rightBound;
        }

        @Override
        public Integer get(int i) {
            int result = leftBound + i;
            if (result >= rightBound) {
                throw new IndexOutOfBoundsException("Requested index: " + i + ", size: " + size());
            }
            return leftBound + i;
        }

        @Override
        public int size() {
            return rightBound - leftBound;
        }
    }

    private final class EventHolder implements ViewHolder<Integer>, View.OnClickListener {

        private final View view;
        private final TextView name;
        private final TextView status;
        private final View okSignal;
        private final View errorSignal;
        private final View loadingSignal;
        private int index;

        private EventHolder() {
            view = View.inflate(EventsLoadingActivity.this, R.layout.events_loading_item, null);
            name = (TextView) view.findViewById(R.id.alarms_name_view);
            status = (TextView) view.findViewById(R.id.status_message);
            okSignal = view.findViewById(R.id.ok_signal);
            errorSignal = view.findViewById(R.id.error_signal);
            loadingSignal = view.findViewById(R.id.loading_signal);
            view.setOnClickListener(this);
            name.setOnClickListener(this);
            status.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (prototypes.size() == createdAlarms.size()) {
                Context context = EventsLoadingActivity.this;
                Intent intent = AlarmEditor.prepareIntent(createdAlarms.get(index), context);
                startActivityForResult(intent, MainActivity.ALARM_CHANGING_REQUEST_CODE);
            }
        }

        @Override
        public void setItem(@NonNull Integer item) {
            index = item;
            name.setText(prototypes.get(item).getTitle());
            updateStatus();
        }

        private void updateStatus() {
            okSignal.setVisibility(View.GONE);
            errorSignal.setVisibility(View.GONE);
            if (createdAlarms.size() <= index) {
                loadingSignal.setVisibility(View.VISIBLE);
            } else if (createdAlarms.get(index) == null) {
                errorSignal.setVisibility(View.VISIBLE);
                loadingSignal.setVisibility(View.GONE);
            } else {
                okSignal.setVisibility(View.VISIBLE);
                loadingSignal.setVisibility(View.GONE);
            }
        }

        @NonNull
        @Override
        public View getView() {
            return view;
        }
    }

    private static final class AsyncConverter extends AsyncTask<Void, Void, Alarm> {

        private final EventDescriptor prototype;
        private final AlarmConverter converter;
        private EventsLoadingActivity observer;

        AsyncConverter(@NonNull EventDescriptor prototype, @NonNull AlarmConverter converter) {
            this.prototype = prototype;
            this.converter = converter;
        }

        @Override
        @Nullable
        protected Alarm doInBackground(Void... args) {
            try {
                return converter.convert(prototype);
            } catch (AlarmConverter.ConversionException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable Alarm alarm) {
            super.onPostExecute(alarm);
            observer.onTaskFinished(this);
        }

        private void observe(EventsLoadingActivity observer) {
            this.observer = observer;
        }
    }
}
