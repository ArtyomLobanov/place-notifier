package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class AlarmManager {

    // just to simulate behavior

    public AlarmManager(Context context) {
    }

    public Cursor getAlarms() {
        return null;
    }

    public void erase(Notification alarm) {
        Log.i("Database:", "Alarm " + alarm.getName() +" erased");
    }

    public void insert(Notification alarm) {
        Log.i("Database:", "Alarm " + alarm.getName() +" inserted");
    }

    public void updateAlarm(Notification alarm) {
        Log.i("Database:", "Alarm " + alarm.getName() +" updated");
    }
}