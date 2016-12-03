package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Objects;

import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;

@SuppressWarnings("WeakerAccess")
public class NotificationsListAdapter extends ArrayAdapter<Notification> {

    private final Context context;

    public NotificationsListAdapter(Context context, ArrayList<Notification> items) {
        super(context, R.layout.notifications_list_item, items);
        this.context = context;
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
            } else if (v == powerButton) {
                boolean isActive = powerButton.isChecked();
                remove(notification);
                notification = notification.change().setActive(isActive).build();
                add(notification);
            } else {
                Intent intent = new Intent(context, NotificationEditor.class);
                intent.putExtra("notification", notification);
                context.startActivity(intent);
            }
        }
    }
}
