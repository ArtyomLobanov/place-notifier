package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

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
        Beacon b = new Beacon(new LatLng(10, 10));
        TimeIntervalPredicate p = new TimeIntervalPredicate(System.currentTimeMillis(),
                System.currentTimeMillis() + 1000 * 10);
        for (int i = 0; i < 10; i++) {
            adapter.add(new Notification("Auto-generated notification number " + i,
                    "created from address (Moscow)",
                    new BeaconPredicate<>(b, 10), p, true, getActivity()));
        }
    }
}
