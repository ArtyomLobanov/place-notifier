package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AlarmsList extends Fragment {

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View result = inflater.inflate(R.layout.fragment_alarms_list, container, false);
        ListView listView = (ListView) result.findViewById(R.id.alarms_list_container);
        ListAdapter adapter = new AlarmsListAdapter((ActivityProducer) getActivity(), 0);
        listView.setAdapter(adapter);
        return result;
    }
}
