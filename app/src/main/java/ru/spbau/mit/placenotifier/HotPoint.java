package ru.spbau.mit.placenotifier;

import android.graphics.Color;
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
    private int color;
    private float scale;

    public HotPoint(@NonNull String name, @NonNull LatLng position, int color, float scale) {
        this.name = name;
        this.position = position;
        this.color = color;
        this.scale = scale;
    }

    @NonNull
    String getName() {
        return name;
    }

    @NonNull
    LatLng getPosition() {
        return position;
    }

    public int getColor() {
        return color;
    }

    public float getScale() {
        return scale;
    }

    // serialization magic

    @SuppressWarnings("unused")
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(name);
        out.writeDouble(position.latitude);
        out.writeDouble(position.longitude);
        out.writeInt(color);
        out.writeFloat(scale);
    }

    @SuppressWarnings("unused")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        name = (String) in.readObject();
        position = new LatLng(in.readDouble(), in.readDouble());
        color = in.readInt();
        scale = in.readInt();
    }


}
