package ru.spbau.mit.placenotifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;

import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Notification implements Serializable {
    private static final String DEFAULT_DEVICE_ID = "unidentified_device";

    private final String identifier;
    private final boolean isActive;
    private final String name;
    private final String comment;

    private final SerializablePredicate<Location> placePredicate;
    private final SerializablePredicate<Long> timePredicate;

    public Notification(@NonNull String name, @NonNull String comment,
                        @NonNull SerializablePredicate<Location> placePredicate,
                        @NonNull SerializablePredicate<Long> timePredicate,
                        boolean isActive, @NonNull Context context) {
        this(name, comment, placePredicate, timePredicate, isActive, context, 0);
    }

    public Notification(@NonNull String name, @NonNull String comment,
                        @NonNull SerializablePredicate<Location> placePredicate,
                        @NonNull SerializablePredicate<Long> timePredicate,
                        boolean isActive, @NonNull Context context, long salt) {
        this(name, comment, placePredicate, timePredicate, isActive,
                createIdentifier(context, salt));
    }

    public Notification(@NonNull String name, @NonNull String comment,
                        @NonNull SerializablePredicate<Location> placePredicate,
                        @NonNull SerializablePredicate<Long> timePredicate,
                        boolean isActive, @NonNull String identifier) {
        this.name = name;
        this.comment = comment;
        this.placePredicate = placePredicate;
        this.timePredicate = timePredicate;
        this.isActive = isActive;
        this.identifier = identifier;
    }

    @NonNull
    @SuppressLint("HardwareIds")
    private static String createIdentifier(@NonNull Context context, long salt) {
        String deviceID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (deviceID == null) {
            Log.e("ID creating: ", "Device id is not available");
            deviceID = DEFAULT_DEVICE_ID;
        }
        return salt + "|" + System.currentTimeMillis() + "|" + deviceID;
    }

    @NonNull
    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public boolean isActive() {
        return isActive;
    }

    @NonNull
    public SerializablePredicate<Long> getTimePredicate() {
        return timePredicate;
    }

    @NonNull
    public SerializablePredicate<Location> getPlacePredicate() {
        return placePredicate;
    }

    @NonNull
    public String getComment() {
        return comment;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getIdentifier() {
        return identifier;
    }

    @NonNull
    public NotificationBuilder change() {
        return new NotificationBuilder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != Notification.class) {
            return false;
        }
        Notification other = (Notification) obj;
        return identifier.equals(other.getIdentifier());
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    public static final class NotificationBuilder {
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
            return createIdentifier(context, 0);
        }

        public NotificationBuilder createIdentifier(@NonNull Context context, long salt) {
            identifier = Notification.createIdentifier(context, salt);
            return this;
        }

        public NotificationBuilder setIdentifier(@NonNull String identifier) {
            this.identifier = identifier;
            return this;
        }

        public NotificationBuilder setTimePredicate(
                @NonNull SerializablePredicate<Long> timePredicate) {
            this.timePredicate = timePredicate;
            return this;
        }

        // have no idea how to separate this line
        public NotificationBuilder setPlacePredicate(
                @NonNull SerializablePredicate<Location> placePredicate) {
            this.placePredicate = placePredicate;
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

        @NonNull
        public Notification build() {
            if (identifier == null || name == null || comment == null || placePredicate == null
                    || timePredicate == null) {
                throw new RuntimeException("Not all fields are filled in");
            }
            return new Notification(name, comment, placePredicate, timePredicate,
                    isActive, identifier);
        }
    }
}
