package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.spbau.mit.placenotifier.predicates.AddressBeacon;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.LatLngBeacon;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;

class AlarmManager {

    // just to simulate behavior
    private final Context context;

    AlarmManager(@NonNull Context context) {
        this.context = context;
    }

    @SuppressWarnings("MagicNumber") // testing
    private static List<Alarm> generateForTest(Context context) {
        List<Alarm> res = new ArrayList<>();
        Geocoder g = new Geocoder(context);
        Address address;
        try {
            address = g.getFromLocationName("Moscow", 1).get(0);
        } catch (IOException e) {
            throw new RuntimeException("Cant find location", e);
        }
        Beacon b = new AddressBeacon(address, "Moscow");
        TimeIntervalPredicate p = new TimeIntervalPredicate(System.currentTimeMillis(),
                System.currentTimeMillis() + 60 * 1000 * 10);
        for (int i = 0; i < 5; i++) {
            res.add(new Alarm("alarm number " + i,
                    "created from address (Moscow)",
                    new BeaconPredicate(b, 10), p, true, context, i));
        }
        b = new LatLngBeacon(new LatLng(59.939095, 30.315868));
        p = new TimeIntervalPredicate(System.currentTimeMillis(),
                System.currentTimeMillis() + 60 * 1000 * 20);
        for (int i = 0; i < 5; i++) {
            res.add(new Alarm("alarm number " + (5 + i),
                    "created from latlng (Spb)",
                    new BeaconPredicate(b, 10), p, true, context, i + 5));
        }
        return res;
    }

    @NonNull
    List<Alarm> getAlarms() {
        return generateForTest(context);
    }

    void erase(@NonNull Alarm alarm) {
        Log.i("Database:", "Alarm (id = " + alarm.getIdentifier() + ") erased");
    }

    @SuppressWarnings("unused")
    void insert(@NonNull Alarm alarm) {
        Log.i("Database:", "Alarm (id = " + alarm.getIdentifier() + ") inserted");
        BeaconPredicate bp = (BeaconPredicate) alarm.getPlacePredicate();
        TimeIntervalPredicate p = (TimeIntervalPredicate) alarm.getTimePredicate();
        Log.i("Database:", "Place = " + ((AddressBeacon)bp.getBeacon()).getLatitude() + ") lat");
        String t = DateUtils.formatDateTime(context,
                p.getFrom(), DateUtils.FORMAT_SHOW_TIME|DateUtils.FORMAT_SHOW_DATE);
        Log.i("Database:", "begin = " + t);
    }

    void updateAlarm(@NonNull Alarm alarm) {
        Log.i("Database:", "Alarm (id = " + alarm.getIdentifier() + ") updated");
    }
}
