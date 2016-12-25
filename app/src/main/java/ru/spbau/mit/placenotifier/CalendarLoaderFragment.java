package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v13.app.ActivityCompat;
import android.support.v13.app.FragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

import ru.spbau.mit.placenotifier.CalendarLoader.CalendarDescriptor;

public class CalendarLoaderFragment extends Fragment
        implements FragmentCompat.OnRequestPermissionsResultCallback {

    private static final String[] NECESSARY_PERMISSIONS = {Manifest.permission.READ_CALENDAR};
    private static final int PERMISSION_REQUEST = 13;

    private CalendarEventsAdapter listAdapter;
    private Spinner calendarChooser;
    private List<CalendarDescriptor> availableCalendars;

    private boolean checkPermission(String permission) {
        int permissionStatus = ActivityCompat.checkSelfPermission(getActivity(), permission);
        return permissionStatus == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View result = inflater.inflate(R.layout.fragment_calendar_loader, container, false);
        ListView listView = (ListView) result.findViewById(R.id.calendar_events_list);
        listAdapter = new CalendarEventsAdapter(getActivity());
        listView.setAdapter(listAdapter);

        calendarChooser = (Spinner) result.findViewById(R.id.calendar_chooser);
        calendarChooser.setOnItemSelectedListener(new CalendarChooserListener());
        if (!checkPermission(Manifest.permission.READ_CALENDAR)) {
            FragmentCompat.requestPermissions(this, NECESSARY_PERMISSIONS, PERMISSION_REQUEST);
        }
        loadCalendarsList();
        return result;
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

    private class CalendarChooserListener implements Spinner.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            listAdapter.setCalendar(availableCalendars.get(i));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {}
    }

    private class AsyncCalendarsLoader extends AsyncTask<Void, Void, List<CalendarDescriptor>> {

        @Override
        protected List<CalendarDescriptor> doInBackground(Void... params) {
            if (!checkPermission(Manifest.permission.READ_CALENDAR)) {
                return null;
            }
            try {
                CalendarLoader loader = new CalendarLoader(getActivity());
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
            availableCalendars = calendars;
            ArrayAdapter<CalendarDescriptor> adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item, calendars);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            calendarChooser.setAdapter(adapter);
        }
    }
}
