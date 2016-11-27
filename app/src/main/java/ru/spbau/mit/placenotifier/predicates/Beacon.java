package ru.spbau.mit.placenotifier.predicates;

import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class Beacon implements Serializable {
    private final double latitude;
    private final double longitude;
    private final String address;

    public Beacon(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude =  location.getLongitude();
        this.address = null;
    }

    public Beacon(@NonNull Address address) {
        latitude = address.getLatitude();
        longitude =  address.getLongitude();
        this.address = address.toString();
    }

    public Beacon(@NonNull Address address, String addressLine) {
        latitude = address.getLatitude();
        longitude =  address.getLongitude();
        this.address = addressLine;
    }

    public Beacon(@NonNull LatLng location) {
        latitude = location.latitude;
        longitude =  location.longitude;
        address = null;
    }

    public float distanceTo(double latitude, double longitude) {
        float[] res = new float[1];
        Location.distanceBetween(latitude, longitude, this.latitude, this.longitude, res);
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

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }
}
