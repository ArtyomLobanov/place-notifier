package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;

import ru.spbau.mit.placenotifier.predicates.AddressBeacon;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;

public class NotificationsList extends Fragment {

    private NotificationsListAdapter adapter;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_notifications_list, container, false);
        ListView listView = (ListView) result.findViewById(R.id.notifications_list_container);
        ArrayList<Notification> lst = new ArrayList<>();
        adapter = new NotificationsListAdapter(getActivity(), lst);
        listView.setAdapter(adapter);

        justForTest();
        return result;
    }

    void justForTest() {
        Geocoder g = new Geocoder(getActivity());
        Address address = null;
        try {
             address = g.getFromLocationName("Moscow", 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Beacon b = new AddressBeacon(address, "capital of russia (test)");
        TimeIntervalPredicate p = new TimeIntervalPredicate(System.currentTimeMillis(),
                System.currentTimeMillis() + 60*1000 * 10);
        for (int i = 0; i < 15; i++) { // just for tests
            adapter.add(new Notification("notif" + i, "com" + i,
                    new BeaconPredicate<>(b, 10), p, true));
        }
    }
}
