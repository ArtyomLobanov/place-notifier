package ru.spbau.mit.placenotifier.predicates;

import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

@SuppressWarnings("unused")
public abstract class Beacon implements Serializable {

    private float distanceTo(double latitude, double longitude) {
        float[] res = new float[1];
        Location.distanceBetween(latitude, longitude, getLatitude(), getLongitude(), res);
        return res[0];
    }

    float distanceTo(@NonNull LatLng location) {
        return distanceTo(location.latitude, location.longitude);
    }

    float distanceTo(@NonNull Location location) {
        return distanceTo(location.getLatitude(), location.getLongitude());
    }

    float distanceTo(@NonNull Address address) {
        return distanceTo(address.getLatitude(), address.getLongitude());
    }

    abstract double getLatitude();

    abstract double getLongitude();
}
