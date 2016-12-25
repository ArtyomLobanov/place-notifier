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
import android.support.v4.app.ActivityCompat;
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

    private DrawerLayout drawerLayout;
    private List<ResultListener> listeners;

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
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.active_alarms_menu:
                fragment = new AlarmsList();
                break;
            case R.id.synchronization_menu:
                fragment = new SynchronizationFragment();
                break;
            case R.id.info_menu:
                fragment = new InfoFragment();
                break;
            case R.id.settings_menu:
                fragment = new CalendarLoaderFragment();
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
        //noinspection Convert2streamapi   (API level isn't enought)
        for (ResultListener listener : listeners) {
            listener.onResult(requestCode, resultCode, data);
        }
    }

    void forTest() {
        CalendarLoader cl = new CalendarLoader(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, 0);
            return;
        }
        List<CalendarLoader.CalendarDescriptor> dlist = cl.getAvailableCalendars();
        for (CalendarLoader.CalendarDescriptor d : dlist) {
            List<CalendarLoader.EventDescriptor> evnts = cl.getEvents(d);
            AlarmManager m = new AlarmManager(this);
            AlarmConverter c = new AlarmConverter(this);
            for (CalendarLoader.EventDescriptor ds : evnts) {
                try {
                    m.insert(c.convert(ds));
                } catch (Exception conversionError) {
                    // throw new RuntimeException(conversionError);
                }
            }
        }
    }

    @Override
    public Activity getParentActivity() {
        return this;
    }

    @Override
    public void addResultListener(@NonNull ResultListener listener) {
        listeners.add(listener);
    }
}
