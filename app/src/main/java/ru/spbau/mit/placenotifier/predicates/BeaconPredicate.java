package ru.spbau.mit.placenotifier.predicates;

import android.location.Address;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class BeaconPredicate implements SerializablePredicate<Location> {

    private final Beacon beacon;
    private final double radius;
    private final boolean isInverted;

    public BeaconPredicate(Beacon beacon, double radius) {
        this(beacon, radius, false);
    }

    public BeaconPredicate(Beacon beacon, double radius, boolean isInverted) {
        this.beacon = beacon;
        this.radius = radius;
        this.isInverted = isInverted;
    }

    @Override
    public boolean apply(Location place) {
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
