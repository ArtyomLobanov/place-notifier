package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("WeakerAccess")
public class NotificationsListAdapter extends ArrayAdapter<Notification> {

    public NotificationsListAdapter(Context context, ArrayList<Notification> items) {
        super(context, R.layout.notifications_list_item, items);
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

    /**
     * Save links to internal view and to described notification
     * Used as tag for item-views
     */
    private final class NotificationsListItemHolder implements OnClickListener {

        final View view;
        final TextView name;
        final TextView description;
        final ToggleButton powerButton;
        final Button removeButton;
        Notification notification;

        private NotificationsListItemHolder(@NonNull Context context) {
            view = View.inflate(context, R.layout.notifications_list_item, null);
            view.setTag(this);
            name = (TextView) view.findViewById(R.id.notifications_name_view);
            description = (TextView) view.findViewById(R.id.notifications_description_view);
            powerButton = (ToggleButton) view.findViewById(R.id.power_button);
            removeButton = (Button) view.findViewById(R.id.remove_button);

            powerButton.setOnClickListener(this);
            removeButton.setOnClickListener(this);
        }

        /**
         * Update internal views to match notification
         */
        void reset(@NonNull Notification notification) {
            this.notification = notification;
            name.setText(notification.name);
            description.setText(notification.comment);
            powerButton.setChecked(notification.isActive);
        }

        @Override
        public void onClick(View v) {
            if (v == removeButton) {
                remove(notification);
                // TODO: 12.11.2016  remove notification from database
            } else if (v == powerButton) {
                notification.isActive = powerButton.isChecked();
                // TODO: 12.11.2016  update information in database
            }
        }
    }
}
