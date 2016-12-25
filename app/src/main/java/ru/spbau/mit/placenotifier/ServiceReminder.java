package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.os.Handler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

class ServiceReminder {

    private static final long MILLISEC_IN_MINUTE = 60000;
    private Timer reminder;
    private TimerTask task;
    private Handler handler;
    private AlarmManager manager;

    ServiceReminder(Activity main) {
        manager = new AlarmManager(main);
        reminder = new Timer();
        handler = new Handler();
        task = new TimerTask() {
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
        reminder.schedule(task, 0, MILLISEC_IN_MINUTE);
    }

    private boolean requestPermission(Activity main) {
        if (ContextCompat.checkSelfPermission(main,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(main,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED)
            return true;
        ActivityCompat.requestPermissions(main,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        return false;
    }

    private void sendNotification(Activity main, List<Alarm> result) throws SecurityException {
        if (result == null)
            return;
        if (!requestPermission(main))
            return;
        LocationManager locationManager = (LocationManager) main.getSystemService(LOCATION_SERVICE);
        Location location = null;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        else
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Long time = System.currentTimeMillis();

        if (location == null)
            return;

        for (Alarm notif : result) {
            if (!notif.getPlacePredicate().apply(location) ||
                    !notif.getTimePredicate().apply(time) || !notif.isActive())
                continue;
            NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(main)
                    .setSmallIcon(R.drawable.alarm)
                    .setContentTitle(notif.getName())
                    .setContentText(notif.getComment());
            Notification not = builder.build();
            not.defaults = Notification.DEFAULT_ALL;
            Intent resultIntent = new Intent(main, main.getClass());
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            main,
                            notif.getIdentifier().hashCode(),
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
