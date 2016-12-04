package ru.spbau.mit.placenotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;


public class AlarmManager {
    private DBHelper dbHelper;

    private static final String DATABASE_NAME = "MY_ALARMS";

    private static final String TIME = "TIME_PREDICATE";
    private static final String LOCATION = "LOCATION_PREDICATE";
    private static final String NAME = "ALARM_NAME";
    private static final String COMMENT = "COMMENT";
    private static final String ID = "ID";
    private static final String ACTIVE = "IS_ACTIVE";

    private static final String [] COLUMNS = {ID, NAME, TIME, LOCATION, ACTIVE, COMMENT};

    private static final int VERSION = 1;

    public AlarmManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<Notification> getAlarms() throws IOException, ClassNotFoundException{
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List <Notification> res = new ArrayList<Notification>();
        Cursor cur = database.query(DATABASE_NAME, COLUMNS, null, null, null, null, null);
        int n = cur.getCount();
        cur.moveToFirst();
        for (int i = 0; i < n; i++) {
            ByteArrayInputStream stream = new ByteArrayInputStream(cur.getString(3).getBytes());
            ObjectInputStream objectInputStream = new ObjectInputStream(stream);
            SerializablePredicate<Location> loc = (SerializablePredicate<Location>)objectInputStream.readObject();
            stream = new ByteArrayInputStream(cur.getString(2).getBytes());
            objectInputStream = new ObjectInputStream(stream);
            SerializablePredicate<Long> time = (SerializablePredicate<Long>)objectInputStream.readObject();
            res.add(new Notification(cur.getString(1), cur.getString(5),
                    loc, time, cur.getInt(4) > 0, cur.getString(0)));
            //still need serialization
            cur.moveToNext();
        }
        cur.close();
        return res;
    }

    public void erase(Notification alarm) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DATABASE_NAME, ID + "=?", new String[]{alarm.getIdentifier()});
    }

    public void insert(Notification alarm) throws IOException {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID, 0); //there will be id then alarm have this one
            if (alarm.getName() != null) {
                contentValues.put(NAME, alarm.getName());
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getPlacePredicate());
            String location = new String(stream.toByteArray(),"UTF-8");
            if (alarm.getPlacePredicate() != null) {
                contentValues.put(LOCATION, location);
            }
            stream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getTimePredicate());
            String time = new String(stream.toByteArray(),"UTF-8");
            if (alarm.getTimePredicate() != null) {
                contentValues.put(TIME, time);
            }
            if (alarm.getComment() != null) {
                contentValues.put(COMMENT, alarm.getComment());
            }
            contentValues.put(ACTIVE, alarm.isActive() ? 1 : 0);
            database.insertOrThrow(DATABASE_NAME, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void updateAlarm(Notification alarm) throws IOException {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID, alarm.getIdentifier());
            if (alarm.getName() != null) {
                contentValues.put(NAME, alarm.getName());
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getPlacePredicate());
            String location = new String(stream.toByteArray(),"UTF-8");
            if (alarm.getPlacePredicate() != null) {
                contentValues.put(LOCATION, location);
            }
            stream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getTimePredicate());
            String time = new String(stream.toByteArray(),"UTF-8");
            if (alarm.getTimePredicate() != null) {
                contentValues.put(TIME, time);
            }
            if (alarm.getComment() != null) {
                contentValues.put(COMMENT, alarm.getComment());
            }
            contentValues.put(ACTIVE, alarm.isActive() ? 1 : 0);
            database.update(DATABASE_NAME,
                    contentValues, ID + "=?", new String[]{alarm.getIdentifier()});
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
                    + ACTIVE + " integer not null, "
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
