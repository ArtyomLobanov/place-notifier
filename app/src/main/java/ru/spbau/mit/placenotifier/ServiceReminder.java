package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.os.Handler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;

class ServiceReminder {

    private Timer reminder;
    private TimerTask task;
    private Handler handler;
    private AlarmManager manager;
    private static final long MILLISEC_IN_MINUTE = 60000;

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

    private void sendNotification(Activity main, List<Alarm> result) {
        if (result == null)
            return;
        /*need some check if there is a time and there is a place*/
        for (Alarm notif : result) {
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
