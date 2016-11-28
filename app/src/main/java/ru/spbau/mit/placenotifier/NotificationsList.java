package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;

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
        Beacon b = new Beacon(new LatLng(59.939095, 30.315868));
        for (int i = 0; i < 15; i++) { // just for tests
            adapter.add(new Notification("notif" + i, "com" + i,
                    new BeaconPredicate<>(b, 10), null));
        }
    }
}
