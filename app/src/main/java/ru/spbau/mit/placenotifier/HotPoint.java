package ru.spbau.mit.placenotifier;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Should be used to contains user's favourite locations
 */
public class HotPoint implements Serializable {


    private String name;
    private LatLng position;

    public HotPoint(@NonNull String name, @NonNull LatLng position) {
        this.name = name;
        this.position = position;
    }

    @NonNull
    String getName() {
        return name;
    }

    @NonNull
    LatLng getPosition() {
        return position;
    }

    // serialization magic

    @SuppressWarnings("unused")
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(name);
        out.writeDouble(position.latitude);
        out.writeDouble(position.longitude);
    }

    @SuppressWarnings("unused")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        position = new LatLng(in.readDouble(), in.readDouble());
    }
}
