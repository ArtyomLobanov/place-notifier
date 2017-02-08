package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.IOUtils;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class GoogleDriveSync extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    ProgressBar progress;
    AlarmManager manager;
    Button download;
    Button upload;
    private GoogleApiClient client;

    @Override
    public void onConnected(Bundle bundle) {
        download = (Button)findViewById(R.id.download);
        download.setOnClickListener(v -> load());
        upload = (Button)findViewById(R.id.upload);
        upload.setOnClickListener(v -> save());
        progress = (ProgressBar)findViewById(R.id.update_GD_progress);
        progress.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        result.getErrorMessage();
        result.getErrorCode();
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_sync);
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this)
                .addApi(com.google.android.gms.drive.Drive.API)
                .addScope(com.google.android.gms.drive.Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);
        client = builder.build();
        progress = (ProgressBar) findViewById(R.id.update_GD_progress);
        manager = new AlarmManager(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }

    public void load() {
        LoadTask toLoad = new LoadTask();
        toLoad.execute((Void[]) null);
    }

    public void save()  {
        SaveTask toSave = new SaveTask();
        toSave.execute((Void[]) null);
    }

    private class SaveTask extends AsyncTask<Void, Void, Void>  {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DriveFolder appFolder = com.google.android.gms.drive.Drive.DriveApi.getAppFolder(client);
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
                List<Alarm> alarms = manager.getAlarms();
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

    private class LoadTask extends AsyncTask<Void, Void, Void>  {

        @Override
        protected void onPreExecute() {
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.setVisibility(View.INVISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                DriveFolder appFolder = com.google.android.gms.drive.Drive.DriveApi.getAppFolder(client);
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
                        manager.erase(alarm);
                        manager.insert(alarm);
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
