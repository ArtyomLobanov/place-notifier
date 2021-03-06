package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import ru.spbau.mit.placenotifier.CalendarLoader.EventDescriptor;
import ru.spbau.mit.placenotifier.predicates.AddressBeacon;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.LatLngBeacon;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;

class AlarmConverter {

    private static final String CONNECTION_ERROR = "Connection error: impossible to find place";
    private static final String BAD_ADDRESS = "Bad address: place not found";
    private static final String AMBIGUOUS_ADDRESS = "Bad address: place not unique";
    private static final double DEFAULT_SENSITIVITY = 50;

    private Geocoder geocoder;

    AlarmConverter(Context context) {
        geocoder = new Geocoder(context);
    }

    Alarm convert(@NonNull EventDescriptor descriptor) throws ConversionException {
        List<Address> places;
        try {
            places = geocoder.getFromLocationName(descriptor.getLocation(), 2);
        } catch (IOException e) {
            throw new ConversionException(CONNECTION_ERROR, e);
        }
        if (places == null || places.isEmpty()) {
            throw new ConversionException(BAD_ADDRESS);
        }
        if (places.size() > 1) {
            Log.w("CONVERTING", AMBIGUOUS_ADDRESS + " \"" + descriptor.getLocation() + "\"");
        }
        Beacon beacon = new AddressBeacon(places.get(0), descriptor.getLocation());
        SerializablePredicate<Location> placePredicate
                = new BeaconPredicate(beacon, DEFAULT_SENSITIVITY);
        SerializablePredicate<Long> timePredicate =
                new TimeIntervalPredicate(descriptor.getStart(), descriptor.getEnd());
        return Alarm.builder().setActive(true)
                .setName(descriptor.getTitle())
                .setComment(descriptor.getDescription())
                .setTimePredicate(timePredicate)
                .setPlacePredicate(placePredicate)
                .setIdentifier("calendar_event|" + descriptor.getId())
                .build();
    }

    Alarm convertPartially(@NonNull EventDescriptor descriptor) {
        List<Address> places;
        try {
            places = geocoder.getFromLocationName(descriptor.getLocation(), 2);
        } catch (IOException e) {
            places = Collections.emptyList();
        }
        Beacon beacon;
        if (places == null || places.isEmpty()) {
            beacon = new LatLngBeacon(new LatLng(0, 0));
        } else {
            if (places.size() > 1) {
                Log.w("CONVERTING", AMBIGUOUS_ADDRESS + " \"" + descriptor.getLocation() + "\"");
            }
            beacon = new AddressBeacon(places.get(0), descriptor.getLocation());
        }
        SerializablePredicate<Location> placePredicate
                = new BeaconPredicate(beacon, DEFAULT_SENSITIVITY);
        SerializablePredicate<Long> timePredicate =
                new TimeIntervalPredicate(descriptor.getStart(), descriptor.getEnd());
        return Alarm.builder().setActive(true)
                .setName(descriptor.getTitle())
                .setComment(descriptor.getDescription())
                .setTimePredicate(timePredicate)
                .setPlacePredicate(placePredicate)
                .setIdentifier("calendar_event|" + descriptor.getId())
                .build();
    }

    final class ConversionException extends Exception {
        private ConversionException(String message, Throwable cause) {
            super(message, cause);
        }

        private ConversionException(String message) {
            super(message);
        }
    }
}
