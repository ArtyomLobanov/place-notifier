package ru.spbau.mit.placenotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;


class AlarmManager {
    private static final String DATABASE_NAME = "MY_ALARMS11";
    private static final String TIME = "TIME_PREDICATE";
    private static final String LOCATION = "LOCATION_PREDICATE";
    private static final String NAME = "ALARM_NAME";
    private static final String COMMENT = "COMMENT";
    private static final String ID = "ID";
    private static final String ACTIVE = "IS_ACTIVE";
    private static final String[] COLUMNS = {ID, NAME, TIME, LOCATION, ACTIVE, COMMENT};
    private static final int VERSION = 1;
    private DBHelper dbHelper;

    AlarmManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    private Object getDeserialized(Cursor cur, int row) {
        ByteArrayInputStream stream;
        ObjectInputStream objectInputStream;
        try {
            stream = new ByteArrayInputStream(cur.getBlob(row));
            objectInputStream = new ObjectInputStream(stream);
            return objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] getSerialised(SerializablePredicate<?> predicate) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutput oos = new ObjectOutputStream(stream);
            oos.writeObject(predicate);
            return stream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    List<Alarm> getAlarms() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<Alarm> res = new ArrayList<>();
        Cursor cur = database.query(DATABASE_NAME, COLUMNS, null, null, null, null, null);
        int n = cur.getCount();
        cur.moveToFirst();
        for (int i = 0; i < n; i++) {
            SerializablePredicate<Location> loc = (SerializablePredicate<Location>) getDeserialized(cur, 3);
            SerializablePredicate<Long> time = (SerializablePredicate<Long>) getDeserialized(cur, 2);
            res.add(new Alarm(cur.getString(1), cur.getString(5),
                    loc, time, cur.getInt(4) > 0, cur.getString(0)));
            cur.moveToNext();
        }
        cur.close();
        return res;
    }

    private ContentValues prepareAlarmForWriting(Alarm alarm) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, alarm.getIdentifier());
        contentValues.put(NAME, alarm.getName());
        contentValues.put(LOCATION, getSerialised(alarm.getPlacePredicate()));
        contentValues.put(TIME, getSerialised(alarm.getTimePredicate()));
        contentValues.put(COMMENT, alarm.getComment());
        contentValues.put(ACTIVE, alarm.isActive() ? 1 : 0);
        return contentValues;
    }

    void erase(Alarm alarm) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DATABASE_NAME, ID + "=?", new String[]{alarm.getIdentifier()});
    }

    void insert(Alarm alarm) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            database.insertOrThrow(DATABASE_NAME, null, prepareAlarmForWriting(alarm));
            database.setTransactionSuccessful();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            database.endTransaction();
        }
    }

    void updateAlarm(Alarm alarm) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            database.update(DATABASE_NAME,
                    prepareAlarmForWriting(alarm), ID + "=?", new String[]{alarm.getIdentifier()});
            database.setTransactionSuccessful();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            database.endTransaction();
        }
    }


    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context context) {
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
