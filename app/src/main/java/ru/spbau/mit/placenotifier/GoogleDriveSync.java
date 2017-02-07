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
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    GoogleDriveManager gmanager;
    private GoogleApiClient client;
    CountDownLatch latch;

    @Override
    public void onConnected(Bundle bundle) {
        latch.countDown();
        gmanager = new GoogleDriveManager(this, client);
        download = (Button)findViewById(R.id.download);
        download.setOnClickListener(v -> gmanager.load());
        upload = (Button)findViewById(R.id.upload);
        upload.setOnClickListener(v -> gmanager.save());
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        latch.countDown();
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
        latch = new CountDownLatch(1);
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

}
