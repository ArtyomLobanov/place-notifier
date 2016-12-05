package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

@SuppressWarnings("WeakerAccess")
public class ServiceReminder {

    private static final long VERY_LONG_TIME = 30000;

    private ReminderThread thread;
    private Context mContext;

    ServiceReminder(@NonNull Context c, @NonNull Activity m) {
        mContext = c;
        thread = new ReminderThread(m);
    }

    public void startChecking(@NonNull Activity main) {
        /*
        ServisReminder will send a request to database, get alarms to notify with
        its identifiers and send notifications to user. But for demo it will just
        sending notification with identifier 1.
         */
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(VERY_LONG_TIME);
                sendNotification(main, 1);
            } catch (InterruptedException e) {
                if (Thread.interrupted()) {
                    break;
                }
            }
        }
    }

    public void sendNotification(@NonNull Activity main, @SuppressWarnings("UnusedParameters") int id) {
        NotificationCompat.Builder builder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(main)
                .setSmallIcon(R.drawable.alarm)
                .setContentTitle("Alarm")
                .setContentText("do something somewhere")
                .setTicker("alarm!!!");
        Notification not = builder.build();
        not.defaults = Notification.DEFAULT_ALL;
        Intent resultIntent = new Intent(main, main.getClass());
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        main,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(resultPendingIntent);
        NotificationManager mNotifyMgr =
                (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(1, not);
    }

    @NonNull
    public ReminderThread getThread() {
        return thread;
    }

    public class ReminderThread extends Thread {
        private Activity main;

        ReminderThread(Activity m) {
            main = m;
        }

        @Override
        public void run() {
            startChecking(main);
        }
    }

}
