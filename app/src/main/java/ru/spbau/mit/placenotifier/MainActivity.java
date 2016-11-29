package ru.spbau.mit.placenotifier;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // todo do something with this
    @SuppressLint("StaticFieldLeak")
    private static ServiceReminder reminder;

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        reminder = new ServiceReminder(this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

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

    // just for test
    public void testEditor() {
        Intent intent = new Intent(this, NotificationEditor.class);
        startActivityForResult(intent, 13);
    }

    // just for test
    public void testPlacePicker() {

        Intent i = PlacePicker.builder()
                .addHotPoint(new HotPoint("Spb", new LatLng(59.939095, 30.315868)))
                .addHotPoint(new HotPoint("Msc", new LatLng(55.755814, 37.617635)))
                .addHotPoint(new HotPoint("Nizhny Novgorod", new LatLng(56.326887, 44.005986)))
                .addHotPoint(new HotPoint("Khabarovsk", new LatLng(48.472584, 135.057732)))
                .build(this);
        startActivityForResult(i, 14);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // just for test
        if (requestCode == 14 && resultCode == RESULT_OK) {
            LatLng position = PlacePicker.getSelectedPoint(data);
            Toast.makeText(this, "you choose point:" + position.longitude
                    + " : " + position.latitude, Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "canceled", Toast.LENGTH_LONG).show();
        }
    }
}
