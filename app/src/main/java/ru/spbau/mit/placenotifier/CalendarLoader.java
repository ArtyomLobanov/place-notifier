package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CalendarLoader {

    private final ContentResolver contentResolver;
    private final Context context;

    public CalendarLoader(@NonNull Context context) {
        this.context = context;
        contentResolver = context.getContentResolver();
    }

    boolean checkPermissions() {
        int status = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR);
        return status == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    public List<CalendarDescriptor> getAvailableCalendars() {
        List<CalendarDescriptor> calendars = new ArrayList<>();
        if (!checkPermissions()) {
            return calendars; // empty list
        }
        //noinspection MissingPermission (already checked)
        try (Cursor cursor = contentResolver.query(Calendars.CONTENT_URI,
                CalendarDescriptor.PROJECTION, null, null, null)) {
            while (cursor != null && cursor.moveToNext()) {
                calendars.add(CalendarDescriptor.readCalendar(cursor));
            }
        }
        return calendars;
    }

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    public List<EventDescriptor> getEvents(CalendarDescriptor descriptor) {
        List<EventDescriptor> events = new ArrayList<>();
        if (!checkPermissions()) {
            return events; // empty list
        }
        //noinspection MissingPermission (already checked)
        try (Cursor cursor = contentResolver.query(Events.CONTENT_URI,
                EventDescriptor.PROJECTION, Events.CALENDAR_ID + " = ? ", new String[]{descriptor.id}, null)) {
            while (cursor != null && cursor.moveToNext()) {
                events.add(EventDescriptor.readEvent(cursor));
            }
        }
        return events;
    }

    public static class CalendarDescriptor implements Serializable {

        private static final String[] PROJECTION = {Calendars._ID, Calendars.NAME,
                Calendars.ACCOUNT_NAME};
        private static final int ID_INDEX = 0;
        private static final int NAME_INDEX = 1;
        private static final int OWNER_INDEX = 2;

        private final String id;
        private final String name;
        private final String owner;

        private CalendarDescriptor(String id, String name, String owner) {
            this.id = id;
            this.name = name;
            this.owner = owner;
        }

        private static CalendarDescriptor readCalendar(Cursor cursor) {
            String id = cursor.getString(ID_INDEX);
            String name = cursor.getString(NAME_INDEX);
            String owner = cursor.getString(OWNER_INDEX);
            return new CalendarDescriptor(id, name, owner);
        }

        public String getName() {
            return name;
        }

        public String getOwner() {
            return owner;
        }

        @Override
        public String toString() {
            return name + " (" + owner + ")";
        }
    }

    public static class EventDescriptor implements Serializable {

        private static final String[] PROJECTION = {Events.TITLE, Events.DESCRIPTION,
                Events.DTSTART, Events.DTEND, Events.EVENT_LOCATION, Events._ID};
        private static final int TITLE_INDEX = 0;
        private static final int DESCRIPTION_INDEX = 1;
        private static final int START_INDEX = 2;
        private static final int END_INDEX = 3;
        private static final int LOCATION_INDEX = 4;
        private static final int ID_INDEX = 5;

        private final String title;
        private final String description;
        private final long start;
        private final long end;
        private final String location;
        private final String id;

        private EventDescriptor(String title, String description, long start, long end,
                                String location, String id) {
            this.title = title;
            this.description = description;
            this.start = start;
            this.end = end;
            this.location = location;
            this.id = id;
        }

        private static EventDescriptor readEvent(Cursor cursor) {
            String title = cursor.getString(TITLE_INDEX);
            String description = cursor.getString(DESCRIPTION_INDEX);
            long start = cursor.getLong(START_INDEX);
            long end = cursor.getLong(END_INDEX);
            String location = cursor.getString(LOCATION_INDEX);
            String id = cursor.getString(ID_INDEX);
            return new EventDescriptor(title, description, start, end, location, id);
        }

        public String getLocation() {
            return location;
        }

        public long getEnd() {
            return end;
        }

        public long getStart() {
            return start;
        }

        public String getDescription() {
            return description;
        }

        public String getTitle() {
            return title;
        }

        public String getId() {
            return id;
        }
    }
}
