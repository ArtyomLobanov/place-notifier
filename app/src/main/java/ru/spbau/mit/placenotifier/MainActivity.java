package ru.spbau.mit.placenotifier;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v13.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ResultRepeater {

    private static final String CURRENT_FRAGMENT_CLASS_KEY = "fragment_class";
    private static final String CURRENT_FRAGMENT_STATE_KEY = "fragment_state";

    static final int ALARM_CREATING_REQUEST_CODE = 566;
    static final int ALARM_CHANGING_REQUEST_CODE = 239;

    private DrawerLayout drawerLayout;
    private List<ResultListener> listeners;
    private Fragment currentFragment;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        listeners = new ArrayList<>();
        new ServiceReminder(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentFragment != null) {
            outState.putSerializable(CURRENT_FRAGMENT_CLASS_KEY, currentFragment.getClass());
            Bundle fragmentState = new Bundle();
            currentFragment.onSaveInstanceState(fragmentState);
            outState.putBundle(CURRENT_FRAGMENT_STATE_KEY, fragmentState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        if (state == null) {
            return;
        }
        Serializable serializable = state.getSerializable(CURRENT_FRAGMENT_CLASS_KEY);
        Class<? extends Fragment> fragmentClass;
        if (!(serializable instanceof Class)) {
            return;
        }
        //noinspection unchecked
        fragmentClass = (Class<? extends Fragment>) serializable;
        Fragment fragment;
        try {
            fragment = fragmentClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Bad fragment was used", e);
        }
        Bundle fragmentState = state.getBundle(CURRENT_FRAGMENT_STATE_KEY);
        if (fragmentState != null) {
            fragment.setArguments(fragmentState);
        }
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        currentFragment = fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create_alarm) {
            Intent intent = AlarmEditor.builder().build(MainActivity.this);
            startActivityForResult(intent, ALARM_CREATING_REQUEST_CODE);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Fragment fragment;
        switch (id) {
            case R.id.active_alarms_menu:
                fragment = new AlarmsList();
                break;
            case R.id.synchronization_menu:
                fragment = new SynchronizationFragment();
                break;
            case R.id.calendar_import_menu:
                fragment = new CalendarLoaderFragment();
                break;
            case R.id.info_menu:
                fragment = new InfoFragment();
                break;
            case R.id.settings_menu:
                fragment = new SettingsFragment();
                break;
            default:
                throw new IllegalArgumentException("Unexpected MenuItem's id: " + item.getItemId());
        }
        FragmentManager m = getFragmentManager();
        m.beginTransaction().replace(R.id.container, fragment).commit();
        currentFragment = fragment;
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ALARM_CREATING_REQUEST_CODE) {
                AlarmManager alarmManager = new AlarmManager(this);
                alarmManager.insert(AlarmEditor.getResult(data));
            } else if (requestCode == ALARM_CHANGING_REQUEST_CODE) {
                AlarmManager alarmManager = new AlarmManager(this);
                alarmManager.updateAlarm(AlarmEditor.getResult(data));
            }
        }
        //noinspection Convert2streamapi   (API level isn't enought)
        for (ResultRepeater.ResultListener listener : listeners) {
            listener.onResult(requestCode, resultCode, data);
        }
    }

    @Override
    public Activity getParentActivity() {
        return this;
    }

    public void addResultListener(@NonNull ResultRepeater.ResultListener listener) {
        listeners.add(listener);
    }
}
