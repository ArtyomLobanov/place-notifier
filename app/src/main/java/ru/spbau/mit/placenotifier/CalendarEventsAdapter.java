package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import ru.spbau.mit.placenotifier.CalendarLoader.CalendarDescriptor;
import ru.spbau.mit.placenotifier.CalendarLoader.EventDescriptor;


class CalendarEventsAdapter extends ArrayAdapter<EventDescriptor> {

    private static final String EVENTS_LIST_KEY = "events_list_key";
    private static final String SELECTED_EVENTS_ID_SET_KEY = "selected_events_id_set_key";
    private static final String CALENDAR_DESCRIPTOR_KEY = "calendar_key";


    private final CalendarLoader calendarLoader;
    private final Context context;
    private final List<SelectionListener> listeners;
    private CalendarDescriptor calendar;
    // Don't use just List and Set to be sure, that used collections are serializable
    private HashSet<String> selectedEventsId;
    private ArrayList<EventDescriptor> eventsList;

    CalendarEventsAdapter(@NonNull Context context) {
        super(context, R.layout.alarms_list_item);
        this.context = context;
        listeners = new ArrayList<>();
        calendarLoader = new CalendarLoader(context);
        selectedEventsId = new HashSet<>();
        eventsList = new ArrayList<>();
    }

    private CheckBox createView() {
        CheckBox view = (CheckBox) View.inflate(context, R.layout.calendar_events_list_item, null);
        view.setOnCheckedChangeListener((compoundButton, b) -> {
            EventDescriptor descriptor = (EventDescriptor) compoundButton.getTag();
            if (descriptor == null) {
                return;
            }
            if (b) {
                selectedEventsId.add(descriptor.getId());
            } else {
                selectedEventsId.remove(descriptor.getId());
            }
            notifySelectionChanged();
        });
        return view;
    }

    void addSelectionListener(SelectionListener listener) {
        listeners.add(listener);
    }

    private void notifySelectionChanged() {
        for (SelectionListener listener : listeners) {
            listener.onSelectionChanged(this, selectedEventsId.size());
        }
    }

    CalendarDescriptor getCalendar() {
        return calendar;
    }

    void setCalendar(@NonNull CalendarDescriptor calendar) {
        if (this.calendar != calendar) {
            this.calendar = calendar;
            new AsyncLoader().execute();
        }
    }

    void selectAll() {
        //noinspection Convert2streamapi
        for (EventDescriptor descriptor : eventsList) {
            selectedEventsId.add(descriptor.getId());
        }
        notifySelectionChanged();
        notifyDataSetChanged();
    }

    void deselectAll() {
        selectedEventsId.clear();
        notifySelectionChanged();
        notifyDataSetChanged();
    }

    boolean isAllSelected() {
        return selectedEventsId.size() == eventsList.size();
    }

    @NonNull
    List<EventDescriptor> getSelectedEvents() {
        List<EventDescriptor> list = new ArrayList<>();
        //noinspection Convert2streamapi
        for (EventDescriptor descriptor : eventsList) {
            if (selectedEventsId.contains(descriptor.getId())) {
                list.add(descriptor);
            }
        }
        return list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        CheckBox checkBox = convertView != null ? (CheckBox) convertView : createView();
        EventDescriptor descriptor = getItem(position);
        Objects.requireNonNull(descriptor, "Item not found at position: " + position);
        checkBox.setTag(descriptor);
        checkBox.setChecked(selectedEventsId.contains(descriptor.getId()));
        checkBox.setText(descriptor.getTitle());
        return checkBox;
    }

    @NonNull
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putSerializable(EVENTS_LIST_KEY, eventsList);
        state.putSerializable(SELECTED_EVENTS_ID_SET_KEY, selectedEventsId);
        state.putSerializable(CALENDAR_DESCRIPTOR_KEY, calendar);
        return state;
    }

    @SuppressWarnings("unchecked")
    public void restoreState(@NonNull Bundle state) {
        try {
            eventsList = (ArrayList<EventDescriptor>) state.getSerializable(EVENTS_LIST_KEY);
            selectedEventsId = (HashSet<String>) state.getSerializable(SELECTED_EVENTS_ID_SET_KEY);
            calendar = (CalendarDescriptor) state.getSerializable(CALENDAR_DESCRIPTOR_KEY);
            clear();
            addAll(eventsList);
            notifySelectionChanged();
        } catch (ClassCastException e) {
            throw new RuntimeException("wrong state format", e);
        }
    }

    interface SelectionListener {
        void onSelectionChanged(CalendarEventsAdapter adapter, int selectionSize);
    }

    private class AsyncLoader extends AsyncTask<Void, Void, List<EventDescriptor>> {

        @Override
        protected List<EventDescriptor> doInBackground(Void... params) {
            if (!calendarLoader.checkPermissions()) {
                return null;
            }
            try {
                //noinspection MissingPermission
                return calendarLoader.getEvents(calendar);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable List<EventDescriptor> events) {
            clear();
            if (events != null) {
                if (events instanceof ArrayList) {
                    eventsList = (ArrayList<EventDescriptor>) events;
                } else {
                    eventsList.clear();
                    eventsList.addAll(events);
                }
                selectedEventsId.clear();
                addAll(events);
                notifyDataSetChanged();
                notifySelectionChanged();
            }
        }
    }
}
