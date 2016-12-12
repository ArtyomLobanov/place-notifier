package ru.spbau.mit.placenotifier;

<<<<<<< HEAD
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
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;


public class AlarmManager {
    private DBHelper dbHelper;

    private static final String DATABASE_NAME = "MY_ALARMS3";

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
            ByteArrayInputStream stream = new ByteArrayInputStream(cur.getBlob(3));
            ObjectInputStream objectInputStream = new ObjectInputStream(stream);
            SerializablePredicate<Location> loc = (SerializablePredicate<Location>)objectInputStream.readObject();
            stream = new ByteArrayInputStream(cur.getBlob(2));
            objectInputStream = new ObjectInputStream(stream);
            SerializablePredicate<Long> time = (SerializablePredicate<Long>)objectInputStream.readObject();
            res.add(new Notification(cur.getString(1), cur.getString(5),
                    loc, time, cur.getInt(4) > 0, cur.getString(0)));
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
            contentValues.put(ID, alarm.getIdentifier());
            if (alarm.getName() != null) {
                contentValues.put(NAME, alarm.getName());
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getPlacePredicate());
            if (alarm.getPlacePredicate() != null) {
                contentValues.put(LOCATION, stream.toByteArray());
            }
            stream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getTimePredicate());
            if (alarm.getTimePredicate() != null) {
                contentValues.put(TIME, stream.toByteArray());
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
            if (alarm.getPlacePredicate() != null) {
                contentValues.put(LOCATION, stream.toByteArray());
            }
            stream = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(stream);
            oos.writeObject(alarm.getTimePredicate());
            if (alarm.getTimePredicate() != null) {
                contentValues.put(TIME, stream.toByteArray());
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
                    + ID + " text primary key not null, "
                    + NAME + " text, "
                    + TIME + " blob, "
                    + LOCATION + " blob, "
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

=======
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.spbau.mit.placenotifier.predicates.AddressBeacon;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.LatLngBeacon;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;

class AlarmManager {

    // just to simulate behavior
    private final Context context;

    AlarmManager(@NonNull Context context) {
        this.context = context;
    }

    @SuppressWarnings("MagicNumber") // testing
    private static List<Alarm> generateForTest(Context context) {
        List<Alarm> res = new ArrayList<>();
        Geocoder g = new Geocoder(context);
        Address address;
        try {
            address = g.getFromLocationName("Moscow", 1).get(0);
        } catch (IOException e) {
            throw new RuntimeException("Cant find location", e);
        }
        Beacon b = new AddressBeacon(address, "Moscow");
        TimeIntervalPredicate p = new TimeIntervalPredicate(System.currentTimeMillis(),
                System.currentTimeMillis() + 60 * 1000 * 10);
        for (int i = 0; i < 5; i++) {
            res.add(new Alarm("alarm number " + i,
                    "created from address (Moscow)",
                    new BeaconPredicate(b, 10), p, true, context, i));
        }
        b = new LatLngBeacon(new LatLng(59.939095, 30.315868));
        p = new TimeIntervalPredicate(System.currentTimeMillis(),
                System.currentTimeMillis() + 60 * 1000 * 20);
        for (int i = 0; i < 5; i++) {
            res.add(new Alarm("alarm number " + (5 + i),
                    "created from latlng (Spb)",
                    new BeaconPredicate(b, 10), p, true, context, i + 5));
        }
        return res;
    }

    @NonNull
    List<Alarm> getAlarms() {
        return generateForTest(context);
    }

    void erase(@NonNull Alarm alarm) {
        Log.i("Database:", "Alarm (id = " + alarm.getIdentifier() + ") erased");
    }

    @SuppressWarnings("unused")
    void insert(@NonNull Alarm alarm) {
        Log.i("Database:", "Alarm (id = " + alarm.getIdentifier() + ") inserted");
    }

    void updateAlarm(@NonNull Alarm alarm) {
        Log.i("Database:", "Alarm (id = " + alarm.getIdentifier() + ") updated");
    }
>>>>>>> origin/master
}
