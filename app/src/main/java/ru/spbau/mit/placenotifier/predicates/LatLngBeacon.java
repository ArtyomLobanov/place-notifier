package ru.spbau.mit.placenotifier.predicates;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class LatLngBeacon extends Beacon {

    private LatLng latLng;


    public LatLngBeacon(@NonNull LatLng location) {
        latLng = location;
    }

    public double getLatitude() {
        return latLng.latitude;
    }

    public double getLongitude() {
        return latLng.longitude;
    }

    @NonNull
    public LatLng getLatLng() {
        return latLng;
    }

    @SuppressWarnings("unused")
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeDouble(latLng.latitude);
        out.writeDouble(latLng.longitude);
    }

    @SuppressWarnings("unused")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        latLng = new LatLng(in.readDouble(), in.readDouble());
    }
}
