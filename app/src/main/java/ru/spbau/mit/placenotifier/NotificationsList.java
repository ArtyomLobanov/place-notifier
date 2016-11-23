package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

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
        for (int i = 0; i < 15; i++) { // just for tests
            adapter.add(new Notification("notif" + i, "com" + i));
        }
    }
}
