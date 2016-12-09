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

class NotificationsListAdapter extends ArrayAdapter<Notification>
        implements ActivityProducer.ResultListener {

    private final ActivityProducer activityProducer;
    private final AlarmManager alarmManager;
    private final int id;

    NotificationsListAdapter(@NonNull ActivityProducer activityProducer, int id) {
        super(activityProducer.getContext(), R.layout.notifications_list_item);
        this.activityProducer = activityProducer;
        this.id = id;
        alarmManager = new AlarmManager(activityProducer.getContext());
        activityProducer.addResultListener(this);
        new NotificationLoader().execute();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Notification notification = getItem(position);
        Objects.requireNonNull(notification, "Item not found at position: " + position);
        NotificationsListItemHolder holder;
        if (convertView == null) {
            holder = new NotificationsListItemHolder(getContext());
        } else {
            holder = (NotificationsListItemHolder) convertView.getTag();
        }
        holder.reset(notification);
        return holder.view;
    }

    @Override
    public void onResult(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            remove(NotificationEditor.getPrototype(data));
            add(NotificationEditor.getResult(data));
            alarmManager.updateAlarm(NotificationEditor.getResult(data));
        }
    }

    @Override
    public int getID() {
        return id;
    }

    /**
     * Save links to internal view and to described notification
     * Used as tag for item-views
     */
    private final class NotificationsListItemHolder implements OnClickListener {

        private final View view;
        private final TextView name;
        private final TextView description;
        private final ToggleButton powerButton;
        private final Button removeButton;
        private Notification notification;

        private NotificationsListItemHolder(@NonNull Context context) {
            view = View.inflate(context, R.layout.notifications_list_item, null);
            view.setTag(this);
            name = (TextView) view.findViewById(R.id.notifications_name_view);
            description = (TextView) view.findViewById(R.id.notifications_description_view);
            powerButton = (ToggleButton) view.findViewById(R.id.power_button);
            removeButton = (Button) view.findViewById(R.id.remove_button);

            powerButton.setOnClickListener(this);
            removeButton.setOnClickListener(this);
            name.setOnClickListener(this);
        }

        /**
         * Update internal views to match notification
         */
        void reset(@NonNull Notification notification) {
            this.notification = notification;
            name.setText(notification.getName());
            description.setText(notification.getComment());
            powerButton.setChecked(notification.isActive());
        }

        @Override
        public void onClick(View v) {
            if (v == removeButton) {
                remove(notification);
                alarmManager.erase(notification);
            } else if (v == powerButton) {
                boolean isActive = powerButton.isChecked();
                Notification changedNotification = notification.change()
                        .setActive(isActive)
                        .build();
                remove(notification);
                add(changedNotification);
                alarmManager.updateAlarm(changedNotification);
            } else {
                Intent intent = NotificationEditor.builder()
                        .setPrototype(notification)
                        .build(activityProducer.getContext());
                activityProducer.startActivity(intent, id);
            }
        }
    }

    private class NotificationLoader extends AsyncTask<Void, Void, List<Notification>> {

        @Override
        protected List<Notification> doInBackground(Void... params) {
            try {
                return alarmManager.getAlarms();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable List<Notification> notifications) {
            if (notifications == null) {
                Toast.makeText(activityProducer.getContext(), "Loading of notifications failed",
                        Toast.LENGTH_LONG).show();
            } else {
                addAll(notifications);
            }
        }
    }
}
