package ru.spbau.mit.placenotifier;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by daphne on 14.12.16.
 */

public class GoogleDriveSync extends Activity {
    private static final String FILENAME = "placenotifier";
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = { DriveScopes.DRIVE };

    private static final int CHOOSE_ACCOUNT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_sync);
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        chooseAccount();
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

    protected void update() {}

    private boolean fileAlreadyExist(Activity main) {
        return false;
    }

    private void createFile() {

    }


    public List<Alarm> getAlarmsFromDrive(Activity main) {
        if (!fileAlreadyExist(main)) {
            createFile();
        }
        return null;
    }


}
