package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;
import java.util.Objects;

class AlarmsListAdapter extends ArrayAdapter<Alarm>
        implements ResultRepeater.ResultListener {

    private final ResultRepeater resultRepeater;
    private final AlarmManager alarmManager;
    private final int id;

    AlarmsListAdapter(@NonNull ResultRepeater resultRepeater, int id) {
        super(resultRepeater.getParentActivity(), R.layout.alarms_list_item);
        this.resultRepeater = resultRepeater;
        this.id = id;
        alarmManager = new AlarmManager(resultRepeater.getParentActivity());
        resultRepeater.addResultListener(this);
        new AlarmsLoader().execute();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Alarm alarm = getItem(position);
        Objects.requireNonNull(alarm, "Item not found at position: " + position);
        AlarmsListItemHolder holder;
        if (convertView == null) {
            holder = new AlarmsListItemHolder(getContext());
        } else {
            holder = (AlarmsListItemHolder) convertView.getTag();
        }
        holder.reset(alarm);
        return holder.view;
    }

    @Override
    public void onResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == id && resultCode == Activity.RESULT_OK && data != null) {
            remove(AlarmEditor.getPrototype(data));
            add(AlarmEditor.getResult(data));
            alarmManager.updateAlarm(AlarmEditor.getResult(data));
        }
    }

    /**
     * Save links to internal view and to described alarm
     * Used as tag for item-views
     */
    private final class AlarmsListItemHolder implements OnClickListener {

        private final View view;
        private final TextView name;
        private final TextView description;
        private final ToggleButton powerButton;
        private final Button removeButton;
        private Alarm alarm;

        private AlarmsListItemHolder(@NonNull Context context) {
            view = View.inflate(context, R.layout.alarms_list_item, null);
            view.setTag(this);
            name = (TextView) view.findViewById(R.id.alarms_name_view);
            description = (TextView) view.findViewById(R.id.alarms_description_view);
            powerButton = (ToggleButton) view.findViewById(R.id.power_button);
            removeButton = (Button) view.findViewById(R.id.remove_button);

            powerButton.setOnClickListener(this);
            removeButton.setOnClickListener(this);
            name.setOnClickListener(this);
        }

        /**
         * Update internal views to match alarm
         */
        void reset(@NonNull Alarm alarm) {
            this.alarm = alarm;
            name.setText(alarm.getName());
            description.setText(alarm.getComment());
            powerButton.setChecked(alarm.isActive());
        }

        @Override
        public void onClick(View v) {
            if (v == removeButton) {
                remove(alarm);
                alarmManager.erase(alarm);
            } else if (v == powerButton) {
                boolean isActive = powerButton.isChecked();
                Alarm changedAlarm = alarm.change()
                        .setActive(isActive)
                        .build();
                remove(alarm);
                add(changedAlarm);
                alarmManager.updateAlarm(changedAlarm);
            } else {
                Intent intent = AlarmEditor.builder()
                        .setPrototype(alarm)
                        .build(resultRepeater.getParentActivity());
                resultRepeater.getParentActivity().startActivityForResult(intent, id);
            }
        }
    }

    private class AlarmsLoader extends AsyncTask<Void, Void, List<Alarm>> {

        @Override
        protected List<Alarm> doInBackground(Void... params) {
            try {
                return alarmManager.getAlarms();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable List<Alarm> alarms) {
            if (alarms == null) {
                Toast.makeText(resultRepeater.getParentActivity(), "Loading of alarms failed",
                        Toast.LENGTH_LONG).show();
            } else {
                addAll(alarms);
            }
        }
    }
}
