package ru.spbau.mit.placenotifier.predicates;

import android.location.Location;
import android.support.annotation.NonNull;

public class BeaconPredicate implements SerializablePredicate<Location> {

    private final Beacon beacon;
    private final double radius;
    private final boolean isInverted;

    public BeaconPredicate(@NonNull Beacon beacon, double radius) {
        this(beacon, radius, false);
    }

    public BeaconPredicate(@NonNull Beacon beacon, double radius, boolean isInverted) {
        this.beacon = beacon;
        this.radius = radius;
        this.isInverted = isInverted;
    }

    @Override
    public boolean apply(@NonNull Location place) {
        return isInverted ^ (beacon.distanceTo(place) < radius);
    }

    public Beacon getBeacon() {
        return beacon;
    }

    public double getRadius() {
        return radius;
    }

    public boolean isInverted() {
        return isInverted;
    }
}
