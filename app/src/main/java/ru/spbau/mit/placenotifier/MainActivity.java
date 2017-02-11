package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ResultRepeater {

    static final int ALARM_CREATING_REQUEST_CODE = 566;
    static final int ALARM_CHANGING_REQUEST_CODE = 239;
    static final int HOT_POINT_CREATING_REQUEST_CODE = 555;
    static final int HOT_POINT_CHANGING_REQUEST_CODE = 222;

    private DrawerLayout drawerLayout;
    private List<ResultRepeater.ResultListener> listeners;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_create_alarm) {
            Intent intent = AlarmEditor.prepareIntent(null, MainActivity.this);
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
                fragment = new AlarmsListFragment();
                break;
            case R.id.synchronization_menu:
                fragment = new SynchronizationFragment();
                break;
            case R.id.info_menu:
                fragment = new HotPointsListFragment();
                break;
            case R.id.settings_menu:
                fragment = new SettingsFragment();
                break;
            default:
                throw new IllegalArgumentException("Unexpected MenuItem's id: " + item.getItemId());
        }

        FragmentManager m = getFragmentManager();
        m.beginTransaction().replace(R.id.container, fragment).commit();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == ALARM_CREATING_REQUEST_CODE) {
                AlarmManager alarmManager = new AlarmManager(this);
                alarmManager.insert(AbstractEditor.getResult(data, Alarm.class));
            } else if (requestCode == ALARM_CHANGING_REQUEST_CODE) {
                AlarmManager alarmManager = new AlarmManager(this);
                alarmManager.updateAlarm(AbstractEditor.getResult(data, Alarm.class));
            } else if (requestCode == HOT_POINT_CREATING_REQUEST_CODE) {
                HotPointManager hotPointManager = new HotPointManager(this);
                hotPointManager.insert(AbstractEditor.getResult(data, HotPoint.class));
            } else if (requestCode == HOT_POINT_CHANGING_REQUEST_CODE) {
                HotPointManager hotPointManager = new HotPointManager(this);
                hotPointManager.update(AbstractEditor.getPrototype(data, HotPoint.class),
                        AbstractEditor.getResult(data, HotPoint.class));
            }
        }
        //noinspection Convert2streamapi   (API level isn't enought)
        for (ResultRepeater.ResultListener listener : listeners) {
            listener.onResult(requestCode, resultCode, data);
        }
    }

    public Activity getParentActivity() {
        return this;
    }

    public void addResultListener(@NonNull ResultRepeater.ResultListener listener) {
        listeners.add(listener);
    }
}
