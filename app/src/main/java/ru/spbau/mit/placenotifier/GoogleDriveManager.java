package ru.spbau.mit.placenotifier;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GoogleDriveManager {

    private static final int RETURN_TO_LOAD = 0;
    private static final int RETURN_TO_SAVE = 1;
    private AlarmManager dbmanager;
    private GoogleApiClient client;


    GoogleDriveManager(Context context, GoogleApiClient client) {
        dbmanager = new AlarmManager(context);
        this.client = client;
    }


    private void make() {
        DriveFolder appFolder = Drive.DriveApi.getAppFolder(client);
        appFolder.createFile(client, new MetadataChangeSet.Builder().setTitle("placenotifier").build(), null)
                .await();

    }


    private void checkAndMake() {
        DriveFolder appFolder = Drive.DriveApi.getAppFolder(client);
        MetadataBuffer buffer = appFolder.queryChildren(client,
                new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                        "placenotifier"))
                        .build())
                .await().getMetadataBuffer();
        if (buffer.getCount() == 0)
            make();
    }

    private void deleteAndMake() {
        DriveFolder appFolder = Drive.DriveApi.getAppFolder(client);
        MetadataBuffer buffer = appFolder.queryChildren(client,
                new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                        "placenotifier"))
                        .build())
                .await().getMetadataBuffer();
        if (buffer.getCount() > 0) {
            DriveFile file = buffer.get(0).getDriveId().asDriveFile();
            file.delete(client);
        }
        make();
    }

    public void load() {
        toLoad.execute((Void[]) null);
    }

    public void save()  {
        toSave.execute((Void[]) null);
    }

    final AsyncTask<Void, Void, Void> toSave = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DriveFolder appFolder = Drive.DriveApi.getAppFolder(client);
                MetadataBuffer buffer = appFolder.queryChildren(client,
                        new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                                "placenotifier"))
                                .build())
                        .await().getMetadataBuffer();
                if (buffer.getCount() > 0) {
                    DriveFile file = buffer.get(0).getDriveId().asDriveFile();
                    file.delete(client);
                }

                appFolder.createFile(client, new MetadataChangeSet.Builder().setTitle("placenotifier").build(), null)
                        .await();

                buffer = appFolder.queryChildren(client,
                        new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                                "placenotifier"))
                                .build())
                        .await().getMetadataBuffer();
                DriveFile file = buffer.get(0).getDriveId().asDriveFile();
                DriveContents contents = file.open(client, DriveFile.MODE_WRITE_ONLY, null).await().getDriveContents();
                OutputStream os = contents.getOutputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                List<Alarm> alarms = dbmanager.getAlarms();
                for (Alarm alarm : alarms) {
                    oos.writeObject(alarm);
                }
                os.write(baos.toByteArray());
                os.close();
                contents.commit(client, null);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    };

    final AsyncTask<Void, Void, Void> toLoad = new AsyncTask<Void, Void, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DriveFolder appFolder = Drive.DriveApi.getAppFolder(client);
                MetadataBuffer buffer = appFolder.queryChildren(client,
                        new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                                "placenotifier"))
                                .build())
                        .await().getMetadataBuffer();
                if (buffer.getCount() == 0) {
                    appFolder.createFile(client, new MetadataChangeSet.Builder().setTitle("placenotifier").build(), null)
                            .await();
                }
                buffer = appFolder.queryChildren(client,
                        new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
                                "placenotifier"))
                                .build())
                        .await().getMetadataBuffer();
                DriveFile file = buffer.get(0).getDriveId().asDriveFile();
                DriveContents contents = file.open(client, DriveFile.MODE_READ_ONLY, null).await().getDriveContents();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(contents.getInputStream(), baos);
                byte[] bytes = baos.toByteArray();
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                while (true) {
                    try {
                        Alarm alarm = (Alarm) ois.readObject();
                        dbmanager.insert(alarm);
                    }
                    catch (EOFException e) {
                        break;
                    }
                }
                ois.close();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }
    };

}
