package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.os.Handler;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;

@SuppressWarnings("ALL")
public class ServiceReminder {

    private Timer reminder;
    private TimerTask task;
    private Handler handler;
    private AlarmManager manager;
    private static final long MILLISEC_IN_MINUTE = 6000;

    ServiceReminder(Activity main) {
        manager = new AlarmManager(main);
        reminder = new Timer();
        handler = new Handler();
        task = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            List<ru.spbau.mit.placenotifier.Notification> result = manager.getAlarms();
                            sendNotification(main, result);
                        }
                        catch (Exception e) {
                            //some processing
                        }
                    }
                });
            }
        };
        reminder.schedule(task, 0, MILLISEC_IN_MINUTE);
    }

    public void sendNotification(Activity main, List <ru.spbau.mit.placenotifier.Notification> result) {
        /*need some check if there is a time and there is a place*/
        for (ru.spbau.mit.placenotifier.Notification notif : result) {
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
