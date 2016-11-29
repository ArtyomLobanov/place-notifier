package ru.spbau.mit.placenotifier.predicates;

import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public abstract class Beacon implements Serializable {

    public float distanceTo(double latitude, double longitude) {
        float[] res = new float[1];
        Location.distanceBetween(latitude, longitude, getLatitude(), getLongitude(), res);
        return res[0];
    }

    public float distanceTo(@NonNull LatLng location) {
        return distanceTo(location.latitude, location.longitude);
    }

    public float distanceTo(@NonNull Location location) {
        return distanceTo(location.getLatitude(), location.getLongitude());
    }

    public float distanceTo(@NonNull Address address) {
        return distanceTo(address.getLatitude(), address.getLongitude());
    }

    public abstract double getLatitude();

    public abstract double getLongitude();
}
