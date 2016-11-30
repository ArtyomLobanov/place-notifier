package ru.spbau.mit.placenotifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.Serializable;
import java.util.Objects;

import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class Notification implements Serializable {
    private static final String DEFAULT_DEVICE_ID = "unidentified_device";

    private final String identifier;
    private final boolean isActive;
    private final String name;
    private final String comment;

    private final SerializablePredicate<Location> placePredicate;
    private final SerializablePredicate<Long> timePredicate;

    public Notification(String name, String comment, SerializablePredicate<Location> placePredicate,
                        SerializablePredicate<Long> timePredicate,
                        boolean isActive, Context context) {
        this(name, comment, placePredicate, timePredicate, isActive, createIdentifier(context));
    }

    public Notification(String name, String comment, SerializablePredicate<Location> placePredicate,
                        SerializablePredicate<Long> timePredicate,
                        boolean isActive, String identifier) {
        this.name = name;
        this.comment = comment;
        this.placePredicate = placePredicate;
        this.timePredicate = timePredicate;
        this.isActive = isActive;
        this.identifier = identifier;
    }

    @SuppressLint("HardwareIds")
    @NonNull
    private static String createIdentifier(Context context) {
        TelephonyManager manager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceID = null; // todo manager.getDeviceId();
        if (deviceID == null) {
            Log.e("ID creating: ", "Device id is not available");
            deviceID = DEFAULT_DEVICE_ID;
        }
        return System.currentTimeMillis() + "|" + deviceID;
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public boolean isActive() {
        return isActive;
    }

    public SerializablePredicate<Long> getTimePredicate() {
        return timePredicate;
    }

    public SerializablePredicate<Location> getPlacePredicate() {
        return placePredicate;
    }

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public NotificationBuilder change() {
        return new NotificationBuilder(this);
    }

    public static class NotificationBuilder {
        private String identifier;
        private boolean isActive;
        private String name;
        private String comment;
        private SerializablePredicate<Location> placePredicate;
        private SerializablePredicate<Long> timePredicate;

        private NotificationBuilder() {
        }

        private NotificationBuilder(@NonNull Notification prototype) {
            identifier = prototype.identifier;
            isActive = prototype.isActive;
            name = prototype.name;
            comment = prototype.comment;
            placePredicate = prototype.placePredicate;
            timePredicate = prototype.timePredicate;
        }

        public NotificationBuilder createIdentifier(@NonNull Context context) {
            identifier = Notification.createIdentifier(context);
            return this;
        }

        public NotificationBuilder setIdentifier(@NonNull String identifier) {
            this.identifier = identifier;
            return this;
        }

        public NotificationBuilder setTimePredicate(@NonNull SerializablePredicate<Long> p) {
            this.timePredicate = p;
            return this;
        }

        // have no idea how to separate this line
        public NotificationBuilder setPlacePredicate(@NonNull SerializablePredicate<Location> p) {
            this.placePredicate = p;
            return this;
        }

        public NotificationBuilder setComment(@NonNull String comment) {
            this.comment = comment;
            return this;
        }

        public NotificationBuilder setName(@NonNull String name) {
            this.name = name;
            return this;
        }

        public NotificationBuilder setActive(boolean active) {
            isActive = active;
            return this;
        }

        public Notification build() {
            if (identifier == null || name == null || comment == null || placePredicate == null ||
                    timePredicate == null) {
                throw new RuntimeException("Not all fields are filled in");
            }
            return new Notification(name, comment, placePredicate, timePredicate,
                    isActive, identifier);
        }
    }
}
