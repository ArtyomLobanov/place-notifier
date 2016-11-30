package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class NotificationsList extends Fragment {

    private NotificationsListAdapter adapter;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_notifications_list, container, false);
        ListView listView = (ListView) result.findViewById(R.id.notifications_list_container);
        adapter = new NotificationsListAdapter(getActivity());
        listView.setAdapter(adapter);
        return result;
    }
}
