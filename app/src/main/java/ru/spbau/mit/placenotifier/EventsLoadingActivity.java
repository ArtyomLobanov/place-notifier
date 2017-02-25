package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import ru.spbau.mit.placenotifier.CalendarLoader.EventDescriptor;
import ru.spbau.mit.placenotifier.SmartListAdapter.ViewHolder;

public class EventsLoadingActivity extends Activity {

    private static final String PROTOTYPES_LIST_KEY = "prototypes_list";

    private List<Alarm> createdAlarms;
    private List<EventDescriptor> prototypes;

    private SmartListAdapter<Integer> listAdapter;
    private ProgressBar progressBar;

    @NonNull
    private static List<EventDescriptor> getPrototypes(@NonNull Intent data) {
        Object result = data.getSerializableExtra(PROTOTYPES_LIST_KEY);
        //noinspection unchecked
        return result == null ? Collections.emptyList() : (List<EventDescriptor>) result;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            loadingSignal.setVisibility(View.GONE);
            if (createdAlarms.size() <= index) {
                loadingSignal.setVisibility(View.VISIBLE);
            } else if (createdAlarms.get(index) == null) {
                errorSignal.setVisibility(View.VISIBLE);
            } else {
                okSignal.setVisibility(View.VISIBLE);
            }
        }

        @NonNull
        @Override
        public View getView() {
            return view;
        }
    }
}
