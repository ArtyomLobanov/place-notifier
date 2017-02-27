package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CalendarLoader {

    private final ContentResolver contentResolver;
    private final Context context;

    CalendarLoader(@NonNull Context context) {
        this.context = context;
        contentResolver = context.getContentResolver();
    }

    boolean checkPermissions() {
        int status = ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR);
        return status == PackageManager.PERMISSION_GRANTED;
    }

    @NonNull
    private <T> List<T> readFromUri(@NonNull ObjectReader<T> reader, @NonNull Uri uri,
                                    @Nullable String[] projection, @Nullable String selection,
                                    @Nullable String[] args) {
        if (!checkPermissions()) {
            return Collections.emptyList();
        }
        List<T> result = new ArrayList<>();
        //noinspection MissingPermission (already checked)
        try (Cursor cursor = contentResolver.query(uri, projection, selection, args, null)) {
            while (cursor != null && cursor.moveToNext()) {
                result.add(reader.read(cursor));
            }
        }
        return result;
    }

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    @NonNull
    List<CalendarDescriptor> getAvailableCalendars() {
        return readFromUri(CalendarDescriptor::readCalendar, Calendars.CONTENT_URI,
                CalendarDescriptor.PROJECTION, null, null);
    }

    @RequiresPermission(Manifest.permission.READ_CALENDAR)
    @NonNull
    List<EventDescriptor> getEvents(CalendarDescriptor descriptor) {
        final String selection = Events.CALENDAR_ID + " = ? ";
        final String[] selectionArs = {descriptor.id};
        Log.wtf("getEvents", "get");
        return readFromUri(EventDescriptor::readEvent, Events.CONTENT_URI,
                EventDescriptor.PROJECTION, selection, selectionArs);
    }

    @FunctionalInterface
    private interface ObjectReader<T> {
        T read(Cursor cursor);
    }

    @NonNull
    private static <T> T orDefault(@Nullable T value, @NonNull T defaultValue) {
        return value == null? defaultValue : value;
    }

    static final class CalendarDescriptor implements Serializable {

        private static final String[] PROJECTION = {Calendars._ID, Calendars.NAME,
                Calendars.ACCOUNT_NAME};
        private static final int ID_INDEX = 0;
        private static final int NAME_INDEX = 1;
        private static final int OWNER_INDEX = 2;

        private final String id;
        private final String name;
        private final String owner;

        private CalendarDescriptor(@NonNull String id, String name, String owner) {
            this.id = id;
            this.name = orDefault(name, "");
            this.owner = orDefault(owner, "");
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

        public String getId() {
            return id;
        }

        @Override
        public String toString() {
            return name + " (" + owner + ")";
        }
    }

    static final class EventDescriptor implements Serializable {

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
                                String location, @NonNull String id) {
            this.title = orDefault(title, "");
            this.description = orDefault(description, "");
            this.start = start;
            this.end = end;
            this.location = orDefault(location, "");
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

        long getStart() {
            return start;
        }

        String getDescription() {
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
