package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class GoogleDriveSync extends Activity {
    private static final String FILENAME = "placenotifier";
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {DriveScopes.DRIVE};
    ProgressBar progress;
    private static final int CHOOSE_ACCOUNT = 1;
    private static final int DOWNLOAD = 2;
    private static final int UPLOAD = 3;
    AlarmManager manager;
    Button download;
    Button upload;
    AccountManager accountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_sync);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        chooseAccount();
        progress = (ProgressBar) findViewById(R.id.update_GD_progress);
        manager = new AlarmManager(this);
        download = (Button)findViewById(R.id.download);
        download.setOnClickListener(v -> update(DOWNLOAD));
        upload = (Button)findViewById(R.id.upload);
        upload.setOnClickListener(v -> update(UPLOAD));
        accountManager = AccountManager.get(this);
    }

    protected void chooseAccount() {
        startActivityForResult(mCredential.newChooseAccountIntent(), CHOOSE_ACCOUNT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String accountName =
                    data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            mCredential.setSelectedAccountName(accountName);
        }
    }

    private boolean requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS) == PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS) == PERMISSION_GRANTED)
            return true;
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS}, 0);
        return false;
    }

    @TargetApi(Build.VERSION_CODES.N)
    protected void update(int request) {
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    if (!requestPermission())
                        return "none";
                    String mScope = "oauth2:"+ DriveScopes.DRIVE;
                    final HttpTransport mTransport = AndroidHttp.newCompatibleTransport();
                    final JsonFactory mJsonFactory = GsonFactory.getDefaultInstance();
                    Drive driveService = new Drive.Builder(mTransport, mJsonFactory, mCredential).build();
                    GoogleAuthUtil.getToken(getBaseContext(), mCredential.getSelectedAccountName(), mScope);
                    if (!fileAlreadyExist(driveService))
                        createFile(driveService);
                    File content = getFile(driveService);
                    ObjectInputStream stream = downloadContent(driveService, content);
                    List<Alarm> fromDrive = readFromStream(stream);
                    List<Alarm> fromDatabase = manager.getAlarms();
                    List<Alarm> all = new ArrayList<>();
                    if (request == DOWNLOAD) {
                        all.addAll(fromDatabase);
                        all.removeAll(fromDrive);
                        all.addAll(fromDrive);
                        for (Alarm alarm : all) {
                            manager.updateAlarm(alarm);
                        }
                    } else {
                        all.addAll(fromDrive);
                        all.removeAll(fromDatabase);
                        all.addAll(fromDatabase);
                        driveService.files().delete(content.getId()).execute();
                        uploadContent(all, driveService);
                    }
                    return "done";
                }
                catch (UserRecoverableAuthException e) {
                    startActivityForResult(e.getIntent(), 0);
                    return "none";
                }
                catch (UserRecoverableAuthIOException e) {
                    startActivityForResult(e.getIntent(), 0);
                    return "none";
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            protected void onPostExecute(String token) {
                Log.i("", "Updated:" + token);
            }
        };
        task.execute();
    }

    private boolean fileAlreadyExist(Drive drive) {
        List<File> res = new ArrayList<>();
        try {
            FileList files = drive.files().list().setQ("title='place-notifier'").execute();
            res.addAll(files.getItems());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        return !res.isEmpty();
    }

    private void createFile(Drive drive) {
        File fileMetadata = new File();
        fileMetadata.setTitle("place-notifier");
        try {
            drive.files().insert(fileMetadata).setFields("id").execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile(Drive drive) {
        List<File> res = new ArrayList<>();
        try {
            FileList files = drive.files().list().setQ("title='place-notifier'").execute();
            res.addAll(files.getItems());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return res.get(0);
    }

    private ObjectInputStream downloadContent(Drive drive, File file) {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                InputStream input = drive.files().get(file.getId()).executeMediaAsInputStream();
                if (input.available() > 0)
                    return new ObjectInputStream(drive.files().get(file.getId()).executeMediaAsInputStream());
                else
                    return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    private List<Alarm> readFromStream(ObjectInputStream stream) {
        List<Alarm> res = new ArrayList<>();
        try {
            if (stream == null)
                return res;
            while (stream.available() > 0) {
                res.add((Alarm) stream.readObject());
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private void uploadContent(List <Alarm> list, Drive drive) {
        try {
            java.io.File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES);
            java.io.File file = new java.io.File(path, "/" + "place-notifier-tmp");
            FileOutputStream out = new FileOutputStream(file);
            ObjectOutputStream outputStream = new ObjectOutputStream(out);
            for (Alarm alarm : list) {
                outputStream.writeObject(alarm);
            }
            File fileMetadata = new File();
            fileMetadata.setTitle("place-notifier");
            FileContent mediaContent = new FileContent("", file);
            drive.files().insert(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
