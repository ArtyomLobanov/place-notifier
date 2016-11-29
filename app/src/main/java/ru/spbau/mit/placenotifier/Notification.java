package ru.spbau.mit.placenotifier;

import android.location.Location;

import java.io.Serializable;

import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class Notification implements Serializable{
    // TODO: 12.11.2016 think about structure
    private final boolean isActive;
    private final String name;
    private final String comment;

    private final SerializablePredicate<Location> placePredicate;
    private final SerializablePredicate<Long> timePredicate;

    public Notification(String name, String comment, SerializablePredicate<Location> placePredicate,
                        SerializablePredicate<Long> timePredicate, boolean isActive) {
        this.name = name;
        this.comment = comment;
        this.placePredicate = placePredicate;
        this.timePredicate = timePredicate;
        this.isActive = isActive;
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
}
