package ru.spbau.mit.placenotifier.predicates;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class LatLngBeacon extends Beacon {

    private final double latitude;
    private final double longitude;
    private transient LatLng cache;


    public LatLngBeacon(@NonNull LatLng location) {
        latitude = location.latitude;
        longitude =  location.longitude;
        cache = location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @NonNull
    public LatLng getLatLng() {
        if (cache == null) {
            cache = new LatLng(latitude, longitude);
        }
        return cache;
    }
}
