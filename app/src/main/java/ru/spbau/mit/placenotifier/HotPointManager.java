package ru.spbau.mit.placenotifier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;


class HotPointManager {
    private static final String DATABASE_NAME = "HOT_POINTS_DATABASE";
    private static final String NAME = "NAME";
    private static final String COLOR = "COLOR";
    private static final String SCALE = "SCALE";
    private static final String LATITUDE = "LATITUDE";
    private static final String LONGITUDE = "LONGITUDE";
    private static final int NAME_INDEX = 0;
    private static final int COLOR_INDEX = 1;
    private static final int SCALE_INDEX = 2;
    private static final int LATITUDE_INDEX = 3;
    private static final int LONGITUDE_INDEX = 4;
    private static final String[] COLUMNS = {NAME, COLOR, SCALE, LATITUDE, LONGITUDE};


    private static final int VERSION = 1;
    private DBHelper dbHelper;

    HotPointManager(Context context) {
        dbHelper = new DBHelper(context);
    }

    List<HotPoint> getHotPoints() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        List<HotPoint> res = new ArrayList<>();
        try (Cursor cur = database.query(DATABASE_NAME, COLUMNS, null, null, null, null, null)) {
            cur.moveToFirst();
            while (!cur.isAfterLast()) {
                String name = cur.getString(NAME_INDEX);
                int color = cur.getInt(COLOR_INDEX);
                float scale = cur.getFloat(SCALE_INDEX);
                double latitude = cur.getDouble(LATITUDE_INDEX);
                double longitude = cur.getDouble(LONGITUDE_INDEX);
                res.add(new HotPoint(name, new LatLng(latitude, longitude), color, scale));
                cur.moveToNext();
            }
        }
        return res;
    }

    private ContentValues prepareForWriting(HotPoint hotPoint) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, hotPoint.getName());
        contentValues.put(COLOR, hotPoint.getColor());
        contentValues.put(SCALE, hotPoint.getScale());
        contentValues.put(LATITUDE, hotPoint.getPosition().latitude);
        contentValues.put(LONGITUDE, hotPoint.getPosition().longitude);
        return contentValues;
    }

    void erase(HotPoint hotPoint) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(DATABASE_NAME, NAME + "=?", new String[]{hotPoint.getName()});
    }

    void insert(HotPoint hotPoint) {
        erase(hotPoint);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            database.insertOrThrow(DATABASE_NAME, null, prepareForWriting(hotPoint));
            database.setTransactionSuccessful();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            database.endTransaction();
        }
    }

    boolean hasKey(String name) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        try (Cursor cur = database.query(DATABASE_NAME, COLUMNS,
                NAME + "=?", new String[]{name}, null, null, null)) {
            return cur.getCount() != 0;
        }
    }

    void update(HotPoint old, HotPoint hotPoint) {
        if (!old.getName().equals(hotPoint.getName())) {
            erase(hotPoint);
        }
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            database.beginTransaction();
            database.update(DATABASE_NAME,
                    prepareForWriting(hotPoint), NAME + "=?", new String[]{old.getName()});
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
                    + NAME + " text primary key not null, "
                    + COLOR + " integer not null, "
                    + SCALE + " real not null, "
                    + LATITUDE + " real not null, "
                    + LONGITUDE + " real not null" + ");");
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
