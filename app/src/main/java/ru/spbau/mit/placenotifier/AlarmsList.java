package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.Comparator;

import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class AlarmsList extends Fragment {

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

        EditText filterInput = (EditText) result.findViewById(R.id.filter_input);
        filterInput.addTextChangedListener(new FilterListener());

        ListView listView = (ListView) result.findViewById(R.id.alarms_list_container);
        AlarmComparator currentComparator = (AlarmComparator) spinner.getSelectedItem();
        listAdapter = new AlarmsListAdapter(currentComparator, new AlarmFilter(""),
                (ResultRepeater) getActivity());
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

    private class FilterListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            listAdapter.setFilter(new AlarmFilter(editable.toString()));
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

    private class AlarmFilter implements SerializablePredicate<Alarm> {
        private final String expectedPrefix;

        private AlarmFilter(String expectedPrefix) {
            this.expectedPrefix = expectedPrefix;
        }

        @Override
        public boolean apply(Alarm alarm) {
            return alarm.getName().startsWith(expectedPrefix);
        }
    }
}
