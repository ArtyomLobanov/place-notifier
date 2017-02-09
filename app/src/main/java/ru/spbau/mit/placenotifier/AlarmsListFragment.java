package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Comparator;

import ru.spbau.mit.placenotifier.SmartListAdapter.ViewHolder;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class AlarmsListFragment extends Fragment {

    private SmartListAdapter<Alarm> listAdapter;
    private ResultRepeater resultRepeater;
    private AlarmManager alarmManager;

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

        alarmManager = new AlarmManager(getActivity());

        ListView listView = (ListView) result.findViewById(R.id.alarms_list_container);
        Comparator<Alarm> currentComparator = (AlarmComparator) spinner.getSelectedItem();
        listAdapter = new SmartListAdapter<>(alarmManager::getAlarms, AlarmHolder::new, getActivity());
        listAdapter.setComparator(currentComparator);
        listView.setAdapter(listAdapter);

        resultRepeater = (ResultRepeater) getActivity();
        resultRepeater.addResultListener((x, y, z) -> listAdapter.refresh());
        return result;
    }

    private final class AlarmHolder implements ViewHolder<Alarm>, View.OnClickListener {

        private final View view;
        private final TextView name;
        private final TextView description;
        private final ToggleButton powerButton;
        private final Button removeButton;
        private Alarm alarm;

        private AlarmHolder() {
            view = View.inflate(getActivity(), R.layout.alarms_list_item, null);
            name = (TextView) view.findViewById(R.id.alarms_name_view);
            description = (TextView) view.findViewById(R.id.alarms_description_view);
            powerButton = (ToggleButton) view.findViewById(R.id.power_button);
            removeButton = (Button) view.findViewById(R.id.remove_button);

            powerButton.setOnClickListener(this);
            removeButton.setOnClickListener(this);
            name.setOnClickListener(this);
            description.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == removeButton) {
                listAdapter.remove(alarm);
                alarmManager.erase(alarm);
            } else if (v == powerButton) {
                boolean isActive = powerButton.isChecked();
                Alarm changedAlarm = alarm.change()
                        .setActive(isActive)
                        .build();
                listAdapter.remove(alarm);
                if (listAdapter.getCurrentFilter().apply(changedAlarm)) {
                    listAdapter.add(changedAlarm);
                    listAdapter.resort();
                }
                alarmManager.updateAlarm(changedAlarm);
            } else {
                Intent intent = AlarmEditor.builder()
                        .setPrototype(alarm)
                        .build(getActivity());
                resultRepeater.getParentActivity()
                        .startActivityForResult(intent, MainActivity.ALARM_CHANGING_REQUEST_CODE);
            }
        }

        @Override
        public void setItem(@NonNull Alarm alarm) {
            this.alarm = alarm;
            name.setText(alarm.getName());
            description.setText(alarm.getComment());
            powerButton.setChecked(alarm.isActive());
        }

        @NonNull
        @Override
        public View getView() {
            return view;
        }
    }

    private enum AlarmComparator implements Comparator<Alarm> {
        SORT_BY_NAME("Sort by name") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                int cmp = a1.getName().compareTo(a2.getName());
                return cmp != 0 ? cmp : a1.getIdentifier().compareTo(a2.getIdentifier());
            }
        },

        NEWEST_FIRST("Newest first") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                return a1.getIdentifier().compareTo(a2.getIdentifier());
            }
        },

        OLDEST_FIRST("Oldest first") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                return a2.getIdentifier().compareTo(a1.getIdentifier());
            }
        },

        ACTIVE_FIRST("Active first") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                int cmp = Boolean.compare(a2.isActive(), a1.isActive());
                return cmp != 0 ? cmp : a2.getIdentifier().compareTo(a1.getIdentifier());
            }
        },

        ACTIVE_LAST("Active last") {
            @Override
            public int compare(Alarm a1, Alarm a2) {
                int cmp = -Boolean.compare(a2.isActive(), a1.isActive());
                return cmp != 0 ? cmp : a2.getIdentifier().compareTo(a1.getIdentifier());
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
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            listAdapter.setFilter(new AlarmFilter(editable.toString()));
        }
    }

    private final class AlarmFilter implements SerializablePredicate<Alarm> {
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
