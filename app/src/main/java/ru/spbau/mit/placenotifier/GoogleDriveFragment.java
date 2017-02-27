package ru.spbau.mit.placenotifier;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

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
import com.google.api.client.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class GoogleDriveFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static final int PROBLEM_RESOLING_REQUEST = 45;

    private ProgressBar progress;
    private Button download;
    private Button upload;

    private AlarmManager manager;
    private GoogleApiClient client;
    final private Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE,
            "placenotifier"))
            .build();

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_google_drive, container, false);

        download = (Button) v.findViewById(R.id.download);
        download.setOnClickListener(t -> load());
        download.setEnabled(false);

        upload = (Button) v.findViewById(R.id.upload);
        upload.setOnClickListener(t -> save());
        upload.setEnabled(false);

        progress = (ProgressBar)v.findViewById(R.id.update_GD_progress);
        return v;
    }

    @Override
    public void onConnected(Bundle bundle) {
        progress.setVisibility(View.INVISIBLE);
        upload.setEnabled(true);
        download.setEnabled(true);

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
                ResultRepeater repeater = (ResultRepeater) getActivity();
                repeater.addResultListener((requestCode, resultCode, data) -> {
                    if (requestCode == PROBLEM_RESOLING_REQUEST && resultCode == Activity.RESULT_OK) {
                        client.connect();
                    }
                });
                result.startResolutionForResult(this.getActivity(), PROBLEM_RESOLING_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(this.getActivity())
                .addApi(com.google.android.gms.drive.Drive.API)
                .addScope(com.google.android.gms.drive.Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this);
        client = builder.build();
        manager = new AlarmManager(getActivity());

    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }

    @Override
    public void onStop() {
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

    private class SaveTask extends AsyncTask<Void, Void, Void> {

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
            MetadataBuffer buffer = null;
            try {
                DriveFolder appFolder = com.google.android.gms.drive.Drive.DriveApi.getAppFolder(client);
                buffer = appFolder.queryChildren(client, query)
                        .await().getMetadataBuffer();
                if (buffer.getCount() > 0) {
                    DriveFile file = buffer.get(0).getDriveId().asDriveFile();
                    file.delete(client);
                }

                appFolder.createFile(client, new MetadataChangeSet.Builder().setTitle("placenotifier").build(), null)
                        .await();

                buffer = appFolder.queryChildren(client, query)
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
            } catch (Exception e) {
                Log.e("GDF", "Uploading failed");
            } finally {
                if (buffer != null) {
                    buffer.release();
                }
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
            MetadataBuffer buffer = null;
            try {
                DriveFolder appFolder = com.google.android.gms.drive.Drive.DriveApi.getAppFolder(client);
                buffer = appFolder.queryChildren(client, query)
                        .await().getMetadataBuffer();
                if (buffer.getCount() == 0) {
                    appFolder.createFile(client, new MetadataChangeSet.Builder().setTitle("placenotifier").build(), null)
                            .await();
                }
                buffer = appFolder.queryChildren(client, query)
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
                    } catch (EOFException e) {
                        break;
                    }
                }
                ois.close();
            } catch (Exception e) {
                Log.e("GDF", "Loading failed");
            } finally {
                if (buffer != null) {
                    buffer.release();
                }
            }
            return null;
        }
    };

}
