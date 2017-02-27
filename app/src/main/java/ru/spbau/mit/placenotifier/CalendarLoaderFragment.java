package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ru.spbau.mit.placenotifier.CalendarLoader.CalendarDescriptor;

import static android.app.Activity.RESULT_OK;

public class CalendarLoaderFragment extends Fragment
        implements FragmentCompat.OnRequestPermissionsResultCallback {
    private static final String ADAPTER_STATE_KEY = "adapter_state_key";
    private static final String CALENDARS_LIST_KEY = "calendars_list_key";

    private static final String[] NECESSARY_PERMISSIONS = {Manifest.permission.READ_CALENDAR};
    private static final int PERMISSION_REQUEST = 13;
    private static final int LOADING_REQUEST = 124;

    private CalendarEventsAdapter listAdapter;
    private Spinner calendarChooser;
    // guaranteed serializable
    private List<CalendarDescriptor> availableCalendars;
    private final OnItemSelectedListener calendarChooserListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            listAdapter.setCalendar(availableCalendars.get(i));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };
    private Context context;

    @NonNull
    static <T> List<T> toSerializableList(@NonNull List<T> list) {
        return list instanceof Serializable ? list : new ArrayList<>(list);
    }

    private boolean checkPermission(String permission) {
        int permissionStatus = ActivityCompat.checkSelfPermission(context, permission);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View result = inflater.inflate(R.layout.fragment_calendar_loader, container, false);
        ListView listView = (ListView) result.findViewById(R.id.calendar_events_list);
        context = getActivity();
        listAdapter = new CalendarEventsAdapter(context);
        listView.setAdapter(listAdapter);
        setRetainInstance(true);
        calendarChooser = (Spinner) result.findViewById(R.id.calendar_chooser);
        calendarChooser.setOnItemSelectedListener(calendarChooserListener);

        Bundle savedState = getArguments();
        if (savedState != null) {
            restoreState(savedState);
        }
        if (!checkPermission(Manifest.permission.READ_CALENDAR)) {
            Log.e("PERMISSION", "have no permission");
            FragmentCompat.requestPermissions(this, NECESSARY_PERMISSIONS, PERMISSION_REQUEST);
        } else if (availableCalendars == null || availableCalendars.isEmpty()) {
            loadCalendarsList();
        }

        CheckBox totalSelector = (CheckBox) result.findViewById(R.id.select_all_button);
        totalSelector.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                listAdapter.selectAll();
            } else if (listAdapter.isAllSelected()) {
                listAdapter.deselectAll();
            }
        });

        Button loadEventsButton = (Button) result.findViewById(R.id.import_button);
        loadEventsButton.setEnabled(false);
        listAdapter.addSelectionListener((adapter, selectionSize) -> {
            String mainText = getResources().getString(R.string.import_events);
            loadEventsButton.setText(mainText + " (" + selectionSize + ")");
            totalSelector.setChecked(adapter.isAllSelected());
            loadEventsButton.setEnabled(selectionSize > 0);
        });

        ResultRepeater repeater = (ResultRepeater) getActivity();
        repeater.addResultListener((requestCode, resultCode, data) -> {
            if (requestCode == LOADING_REQUEST && resultCode == RESULT_OK) {
                AsyncEventsSaver saver = new AsyncEventsSaver();
                List<Alarm> alarms = EventsLoadingActivity.getResult(data);
                //noinspection unchecked
                saver.execute(alarms);
            }
        });
        loadEventsButton.setOnClickListener(view -> {
            Intent intent = EventsLoadingActivity.prepareIntent(listAdapter.getSelectedEvents(),
                    repeater.getParentActivity());
            repeater.getParentActivity().startActivityForResult(intent, LOADING_REQUEST);
        });
        return result;
    }

    private void restoreState(@NonNull Bundle state) {
        Bundle adapterState = state.getBundle(ADAPTER_STATE_KEY);
        if (adapterState == null) {
            throw new RuntimeException("Unexpected state format");
        }
        listAdapter.restoreState(adapterState);

        // we know type, because we own hands saved that list
        //noinspection unchecked
        List<CalendarDescriptor> savedCalendarsList =
                (List<CalendarDescriptor>) state.getSerializable(CALENDARS_LIST_KEY);

        if (savedCalendarsList == null) {
            savedCalendarsList = Collections.emptyList();
        }
        availableCalendars = toSerializableList(savedCalendarsList);
        ArrayAdapter<CalendarDescriptor> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, availableCalendars);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        calendarChooser.setAdapter(adapter);
        if (listAdapter.getCalendar() != null) {
            setCurrentCalendar(listAdapter.getCalendar());
        }
    }

    private void setCurrentCalendar(CalendarDescriptor descriptor) {
        String expectedCalendarID = descriptor.getId();
        for (int i = 0; i < availableCalendars.size(); i++) {
            if (expectedCalendarID.equals(availableCalendars.get(i).getId())) {
                calendarChooser.setSelection(i);
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int request, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (request != PERMISSION_REQUEST) {
            return;
        }
        if (checkPermission(Manifest.permission.READ_CALENDAR)) {
            loadCalendarsList();
        }
    }

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    private void loadCalendarsList() {
        new AsyncCalendarsLoader().execute();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(ADAPTER_STATE_KEY, listAdapter.saveState());
        if (availableCalendars != null) {
            outState.putSerializable(CALENDARS_LIST_KEY, (Serializable) availableCalendars);
        }
    }

    private class AsyncCalendarsLoader extends AsyncTask<Void, Void, List<CalendarDescriptor>> {

        @Override
        protected List<CalendarDescriptor> doInBackground(Void... params) {
            if (!checkPermission(Manifest.permission.READ_CALENDAR)) {
                return null;
            }
            try {
                CalendarLoader loader = new CalendarLoader(context);
                return loader.getAvailableCalendars();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable List<CalendarDescriptor> calendars) {
            if (calendars == null) {
                return;
            }
            availableCalendars = toSerializableList(calendars);
            ArrayAdapter<CalendarDescriptor> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_spinner_item, toSerializableList(calendars));
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            calendarChooser.setAdapter(adapter);
        }
    }

    private class AsyncEventsSaver extends AsyncTask<List<Alarm>, Void, Void> {

        @Override
        @SafeVarargs
        protected final Void doInBackground(List<Alarm>... lists) {
            if (lists.length != 1) {
                throw new IllegalArgumentException("Wrong number of arguments:" + lists.length);
            }
            AlarmManager manager = new AlarmManager(context);
            Collection<String> existingID = new HashSet<>();
            //noinspection Convert2streamapi
            for (Alarm alarm : manager.getAlarms()) {
                existingID.add(alarm.getIdentifier());
            }
            for (Alarm alarm : lists[0]) {
                if (existingID.contains(alarm.getIdentifier())) {
                    manager.updateAlarm(alarm);
                } else {
                    manager.insert(alarm);
                }
            }
            return null;
        }
    }
}
