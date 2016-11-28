package ru.spbau.mit.placenotifier.predicates;

import android.location.Address;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class BeaconPredicate<T> implements SerializablePredicate<T> {

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
    public boolean apply(T place) {
        if (place.getClass() == LatLng.class)
            return isInverted ^ (beacon.distanceTo((LatLng) place) < radius);
        if (place.getClass() == Location.class)
            return isInverted ^ (beacon.distanceTo((Location) place) < radius);
        if (place.getClass() == Address.class)
            return isInverted ^ (beacon.distanceTo((Address) place) < radius);
        throw new UnsupportedOperationException("Beacon predicate can't work with that object: "
                + place);
    }

    public Beacon getBeacon() {
        return beacon;
    }
}
