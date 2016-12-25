package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import ru.spbau.mit.placenotifier.CalendarLoader.EventDescriptor;
import ru.spbau.mit.placenotifier.predicates.AddressBeacon;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;

public class AlarmConverter {

    private static final String CONNECTION_ERROR = "Connection error: impossible to find place";
    private static final String BAD_ADDRESS = "Bad address: place not found";
    private static final String AMBIGUOUS_ADDRESS = "Bad address: place not unique";
    private static final double DEFAULT_SENSITIVITY = 50;

    private Geocoder geocoder;

    public AlarmConverter(Context context) {
        geocoder = new Geocoder(context);
    }

    public Alarm convert(@NonNull EventDescriptor descriptor) throws ConversionError {
        SerializablePredicate<Long> timePredicate =
                new TimeIntervalPredicate(descriptor.getStart(), descriptor.getEnd());
        List<Address> places;
        try {
            places = geocoder.getFromLocationName(descriptor.getLocation(), 2);
        } catch (IOException e) {
            throw new ConversionError(CONNECTION_ERROR, e);
        }
        if (places == null || places.isEmpty()) {
            throw new ConversionError(BAD_ADDRESS);
        }
        if (places.size() > 1) {
            Log.w("CONVERTING", AMBIGUOUS_ADDRESS + " \"" + descriptor.getLocation() + "\"");
        }
        Beacon beacon = new AddressBeacon(places.get(0), descriptor.getLocation());
        SerializablePredicate<Location> placePredicate
                = new BeaconPredicate(beacon, DEFAULT_SENSITIVITY);
        return Alarm.builder().setActive(true)
                .setName(descriptor.getTitle())
                .setComment(descriptor.getDescription())
                .setTimePredicate(timePredicate)
                .setPlacePredicate(placePredicate)
                .setIdentifier("calendar_event|" + descriptor.getId())
                .build();
    }

    class ConversionError extends Exception {
        private ConversionError(String message, Throwable cause) {
            super(message, cause);
        }

        private ConversionError(String message) {
            super(message);
        }
    }
}
