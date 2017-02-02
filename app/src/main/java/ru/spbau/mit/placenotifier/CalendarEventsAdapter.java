package ru.spbau.mit.placenotifier;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
    // Did not use just List and Set to be sure, that used collections are serializable
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
        EventDescriptor eventDescriptor = getItem(position);
        EventListItemHolder holder;
        if (convertView == null) {
            holder = new EventListItemHolder(eventDescriptor);
        } else {
            holder = (EventListItemHolder) convertView.getTag();
            holder.reset(eventDescriptor);
        }
        return holder.view;
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

    private final class EventListItemHolder implements OnCheckedChangeListener, OnClickListener {

        private final CheckBox checkBox;
        private final Button button;
        private final View view;
        private EventDescriptor eventDescriptor;

        private EventListItemHolder(EventDescriptor eventDescriptor) {
            view = View.inflate(context, R.layout.calendar_events_list_item, null);
            view.setTag(this);

            checkBox = (CheckBox) view.findViewById(R.id.calendar_events_item_check_box);
            checkBox.setOnCheckedChangeListener(this);

            button = (Button) view.findViewById(R.id.calendar_events_item_open);
            button.setOnClickListener(this);

            reset(eventDescriptor);
        }

        private void reset(EventDescriptor eventDescriptor) {
            this.eventDescriptor = eventDescriptor;
            boolean isSelected = selectedEventsId.contains(eventDescriptor.getId());

            checkBox.setText(eventDescriptor.getTitle());
            checkBox.setChecked(isSelected);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (eventDescriptor == null) {
                return;
            }
            if (b) {
                selectedEventsId.add(eventDescriptor.getId());
            } else {
                selectedEventsId.remove(eventDescriptor.getId());
            }
            notifySelectionChanged();
        }

        @Override
        public void onClick(View view) {
            long eventID;
            try {
                eventID = Long.parseLong(eventDescriptor.getId());
            } catch (NumberFormatException e) {
                Log.e("CEA", "Bad event id format");
                return;
            }

            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventID);
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
            context.startActivity(intent);
        }
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
