package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat.Builder;
import android.util.Log;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

class ServiceReminder {

    private static final long MILLIE_IN_MINUTE = 60000;
    private Handler handler;
    private AlarmManager manager;

    ServiceReminder(Activity main) {
        manager = new AlarmManager(main);
        Timer reminder = new Timer();
        handler = new Handler();
        TimerTask task = new TimerTask() {
            public void run() {
                handler.post(() -> {
                    try {
                        List<Alarm> result = manager.getAlarms();
                        sendNotification(main, result);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        };
        reminder.schedule(task, 0, MILLIE_IN_MINUTE);
    }

    private boolean checkPermissions(Activity activity, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean requestPermission(Activity main) {
        if (checkPermissions(main, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION)) {
            return true;
        }
        ActivityCompat.requestPermissions(main,
                new String[]{ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        return false;
    }

    private void sendNotification(Activity main, Iterable<Alarm> result) throws SecurityException {
        if (result == null || !requestPermission(main)) {
            return;
        }
        LocationManager locationManager = (LocationManager) main.getSystemService(LOCATION_SERVICE);
        Location location;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        if (location == null) {
            Log.w("SR", "location not found");
            return;
        }

        Long time = System.currentTimeMillis();

        for (Alarm notification : result) {
            if (!notification.getPlacePredicate().apply(location)
                    || !notification.getTimePredicate().apply(time) || !notification.isActive()) {

                continue;
            }
            Builder builder = (Builder) new Builder(main)
                    .setSmallIcon(R.drawable.alarm)
                    .setContentTitle(notification.getName())
                    .setContentText(notification.getComment());
            Notification not = builder.build();
            not.defaults = Notification.DEFAULT_ALL;
            Intent resultIntent = new Intent(main, main.getClass());
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            main,
                            notification.getIdentifier().hashCode(),
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(resultPendingIntent);
            NotificationManager mNotifyMgr =
                    (NotificationManager) main.getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, not);
        }

    }


}
