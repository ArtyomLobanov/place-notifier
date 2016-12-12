package ru.spbau.mit.placenotifier;

import android.content.Context;

import android.content.ContentValues;
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

    private static final String DATABASE_NAME = "MY_ALARMS3";

    private static final String TIME = "TIME_PREDICATE";
    private static final String LOCATION = "LOCATION_PREDICATE";
    private static final String NAME = "ALARM_NAME";
    private static final String COMMENT = "COMMENT";
    private static final String ID = "ID";
    private static final String ACTIVE = "IS_ACTIVE";

    private static final String[] COLUMNS = {ID, NAME, TIME, LOCATION, ACTIVE, COMMENT};

    private static final int VERSION = 1;

    public AlarmManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    public List<Alarm> getAlarms() {

        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<Alarm> res = new ArrayList<Alarm>();
        try {
            Cursor cur = database.query(DATABASE_NAME, COLUMNS, null, null, null, null, null);
            int n = cur.getCount();
            cur.moveToFirst();
            for (int i = 0; i < n; i++) {
                ByteArrayInputStream stream = new ByteArrayInputStream(cur.getBlob(3));
                ObjectInputStream objectInputStream = new ObjectInputStream(stream);
                SerializablePredicate<Location> loc = (SerializablePredicate<Location>) objectInputStream.readObject();
                stream = new ByteArrayInputStream(cur.getBlob(2));
                objectInputStream = new ObjectInputStream(stream);
                SerializablePredicate<Long> time = (SerializablePredicate<Long>) objectInputStream.readObject();
                res.add(new Alarm(cur.getString(1), cur.getString(5),
                        loc, time, cur.getInt(4) > 0, cur.getString(0)));
                cur.moveToNext();
            }
            cur.close();
        }
        catch (Exception e) {
            /*some processing*/
        }
        finally {
            return res;
        }
    }

    public void erase(Alarm alarm) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DATABASE_NAME, ID + "=?", new String[]{alarm.getIdentifier()});
    }

    public void insert(Alarm alarm) throws IOException {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID, alarm.getIdentifier());

            contentValues.put(NAME, alarm.getName());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getPlacePredicate());
            contentValues.put(LOCATION, stream.toByteArray());

            stream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getTimePredicate());
            contentValues.put(TIME, stream.toByteArray());

            contentValues.put(COMMENT, alarm.getComment());

            contentValues.put(ACTIVE, alarm.isActive() ? 1 : 0);
            database.insertOrThrow(DATABASE_NAME, null, contentValues);
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    public void updateAlarm(Alarm alarm)  {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put(ID, alarm.getIdentifier());

            contentValues.put(NAME, alarm.getName());

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getPlacePredicate());
            contentValues.put(LOCATION, stream.toByteArray());

            stream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getTimePredicate());
            contentValues.put(TIME, stream.toByteArray());

            contentValues.put(COMMENT, alarm.getComment());

            contentValues.put(ACTIVE, alarm.isActive() ? 1 : 0);
            database.update(DATABASE_NAME,
                    contentValues, ID + "=?", new String[]{alarm.getIdentifier()});
            database.setTransactionSuccessful();
        }
        catch (Exception e) {
            /*some processing*/
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
                    + ID + " text primary key not null, "
                    + NAME + " text not null, "
                    + TIME + " blob not null, "
                    + LOCATION + " blob not null, "
                    + ACTIVE + " integer not null, "
                    + COMMENT + " text not null" + ");");
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