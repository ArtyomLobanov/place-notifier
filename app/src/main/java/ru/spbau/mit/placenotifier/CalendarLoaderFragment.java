package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import ru.spbau.mit.placenotifier.CalendarLoader.CalendarDescriptor;

import java.util.List;

public class CalendarLoaderFragment extends Fragment {

    private CalendarEventsAdapter adapter;
    private Spinner calendarChooser;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View result = inflater.inflate(R.layout.fragment_calendar_loader, container, false);
        ListView listView = (ListView) result.findViewById(R.id.calendar_events_list);
        adapter = new CalendarEventsAdapter(getActivity());
        listView.setAdapter(adapter);

        calendarChooser = (Spinner) result.findViewById(R.id.calendar_chooser);
        return result;
    }

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    private void loadCalendarsList() {
        CalendarLoader loader = new CalendarLoader(getActivity());
        if (loader.checkPermissions()) {
            //noinspection MissingPermission
            List<CalendarDescriptor> list = loader.getAvailableCalendars();
        }
    }
}
