package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.lang.reflect.Array;
import java.util.Comparator;

public class AlarmsList extends Fragment {

    private AlarmComparator currentComparator;
    private AlarmsListAdapter listAdapter;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View result = inflater.inflate(R.layout.fragment_alarms_list, container, false);

        Spinner spinner = (Spinner) result.findViewById(R.id.sorting_spinner);
        ArrayAdapter spinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, AlarmComparator.values());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new SpinnerListener());
        currentComparator = (AlarmComparator) spinner.getSelectedItem();

        ListView listView = (ListView) result.findViewById(R.id.alarms_list_container);
        listAdapter = new AlarmsListAdapter(currentComparator, (ResultRepeater) getActivity(), 0);
        listView.setAdapter(listAdapter);
        return result;
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            listAdapter.setComparator((AlarmComparator) adapterView.getItemAtPosition(i));
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    private enum AlarmComparator implements Comparator<Alarm> {
        SORT_BY_NAME("Sort by name") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                int cmp = a1.getName().compareTo(a2.getName());
                return cmp == 0? a1.getIdentifier().compareTo(a2.getIdentifier()) : cmp;
            }
        },

        NEWEST_FIRST("Newest first") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                return a1.getIdentifier().compareTo(a2.getIdentifier()) ;
            }
        },

        OLDEST_FIRST("Oldest first") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                return a2.getIdentifier().compareTo(a1.getIdentifier()) ;
            }
        };

        private final String name;

        AlarmComparator(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
