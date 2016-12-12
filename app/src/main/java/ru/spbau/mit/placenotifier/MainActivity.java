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
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // todo do something with this
    @SuppressLint("StaticFieldLeak")
    private static ServiceReminder reminder;
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
        reminder = new ServiceReminder(this);
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
        int id = item.getItemId();
        Fragment fragment;
        switch (id) {
            case R.id.active_notifications_menu:
                fragment = new NotificationsList();
                break;
            case R.id.synchronization_menu:
                fragment = new SynchronizationFragment();
                break;
            case R.id.info_menu:
                fragment = new InfoFragment();
                break;
            case R.id.settings_menu:
                fragment = new SettingsFragment();
                break;
            case R.id.test_editor:
                testEditor();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.test_place_picker:
                testPlacePicker();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
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

    @Override
    public Activity getParentActivity() {
        return this;
    }

    @Override
    public void addResultListener(@NonNull ResultListener listener) {
        listeners.add(listener);
    }
}
