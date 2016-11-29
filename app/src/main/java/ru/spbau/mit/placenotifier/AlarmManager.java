package ru.spbau.mit.placenotifier;

import android.app.*;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
/**
 * Created by daphne on 29.11.16.
 */

public class AlarmManager {
    private DBHelper dbHelper;

    private static final String DATABASE_NAME = "MY_ALARMS";

    private static final String TIME = "TIME_PREDICATE";
    private static final String LOCATION = "LOCATION_PREDICATE";
    private static final String NAME = "ALARM_NAME";
    private static final String COMMENT = "COMMENT";
    private static final String ID = "ID";

    private static final String [] COLUMNS = {ID, NAME, TIME, LOCATION, COMMENT};

    private static final int VERSION = 1;

    public AlarmManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public Cursor getAlarms() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        return database.query(DATABASE_NAME, COLUMNS, null, null, null, null, null);
    }

    public void erase(Notification alarm) {
        //then it have id...
    }

    public void insert(Notification alarm) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID, 0); //there will be id then alarm have this one
            if (alarm.getName() != null) {
                contentValues.put(NAME, alarm.getName());
            }
            if (alarm.getPlacePredicate() != null) {
                //there will be predicate then it is serializable
            }
            if (alarm.getTimePredicate() != null) {
                //there will be predicate then it is serializable
            }
            if (alarm.getComment() != null) {
                contentValues.put(COMMENT, alarm.getComment());
            }
            database.insertOrThrow(DATABASE_NAME, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void updateAlarm(Notification alarm) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID, 0); //there will be id then alarm have this one
            if (alarm.getName() != null) {
                contentValues.put(NAME, alarm.getName());
            }
            if (alarm.getPlacePredicate() != null) {
                //there will be predicate then it is serializable
            }
            if (alarm.getTimePredicate() != null) {
                //there will be predicate then it is serializable
            }
            if (alarm.getComment() != null) {
                contentValues.put(COMMENT, alarm.getComment());
            }
            database.update(DATABASE_NAME,
                    contentValues, ID + "=?", new String[]{""});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }


    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + DATABASE_NAME + "("
                    + ID + " integer primary key not null, "
                    + NAME + " text, "
                    + TIME + " text, "
                    + LOCATION + " text, "
                    + COMMENT + " text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

}
