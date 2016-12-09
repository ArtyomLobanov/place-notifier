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

@SuppressWarnings("unused")
public class Alarm implements Serializable {
    private static final String DEFAULT_DEVICE_ID = "unidentified_device";

    private final String identifier;
    private final boolean isActive;
    private final String name;
    private final String comment;

    private final SerializablePredicate<Location> placePredicate;
    private final SerializablePredicate<Long> timePredicate;

    Alarm(@NonNull String name, @NonNull String comment,
          @NonNull SerializablePredicate<Location> placePredicate,
          @NonNull SerializablePredicate<Long> timePredicate,
          boolean isActive, @NonNull Context context) {
        this(name, comment, placePredicate, timePredicate, isActive, context, 0);
    }

    Alarm(@NonNull String name, @NonNull String comment,
          @NonNull SerializablePredicate<Location> placePredicate,
          @NonNull SerializablePredicate<Long> timePredicate,
          boolean isActive, @NonNull Context context, long salt) {
        this(name, comment, placePredicate, timePredicate, isActive,
                createIdentifier(context, salt));
    }

    @SuppressWarnings("WeakerAccess")
    Alarm(@NonNull String name, @NonNull String comment,
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
    public static AlarmBuilder builder() {
        return new AlarmBuilder();
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

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public AlarmBuilder change() {
        return new AlarmBuilder(this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null || obj.getClass() != Alarm.class) {
            return false;
        }
        Alarm other = (Alarm) obj;
        return identifier.equals(other.getIdentifier());
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @SuppressWarnings("WeakerAccess")
    public static final class AlarmBuilder {
        private String identifier;
        private boolean isActive;
        private String name;
        private String comment;
        private SerializablePredicate<Location> placePredicate;
        private SerializablePredicate<Long> timePredicate;

        private AlarmBuilder() {
        }

        private AlarmBuilder(@NonNull Alarm prototype) {
            identifier = prototype.identifier;
            isActive = prototype.isActive;
            name = prototype.name;
            comment = prototype.comment;
            placePredicate = prototype.placePredicate;
            timePredicate = prototype.timePredicate;
        }

        public AlarmBuilder createIdentifier(@NonNull Context context) {
            return createIdentifier(context, 0);
        }

        public AlarmBuilder createIdentifier(@NonNull Context context, long salt) {
            identifier = Alarm.createIdentifier(context, salt);
            return this;
        }

        public AlarmBuilder setIdentifier(@NonNull String identifier) {
            this.identifier = identifier;
            return this;
        }

        public AlarmBuilder setTimePredicate(
                @NonNull SerializablePredicate<Long> timePredicate) {
            this.timePredicate = timePredicate;
            return this;
        }

        // have no idea how to separate this line
        public AlarmBuilder setPlacePredicate(
                @NonNull SerializablePredicate<Location> placePredicate) {
            this.placePredicate = placePredicate;
            return this;
        }

        public AlarmBuilder setComment(@NonNull String comment) {
            this.comment = comment;
            return this;
        }

        public AlarmBuilder setName(@NonNull String name) {
            this.name = name;
            return this;
        }

        public AlarmBuilder setActive(boolean active) {
            isActive = active;
            return this;
        }

        @NonNull
        public Alarm build() {
            if (identifier == null || name == null || comment == null || placePredicate == null
                    || timePredicate == null) {
                throw new RuntimeException("Not all fields are filled in");
            }
            return new Alarm(name, comment, placePredicate, timePredicate,
                    isActive, identifier);
        }
    }
}
