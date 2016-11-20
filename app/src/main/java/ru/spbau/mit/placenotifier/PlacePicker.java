package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

/**
 * Activity shows maps and offer user to choose point on it
 */
public class PlacePicker extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, View.OnClickListener {

    public static final String HOT_POINTS_KEY = "hot_points";
    public static final String INITIAL_POSITION_KEY = "initial_position";
    public static final String INITIAL_SCALE_KEY = "initial_scale";
    public static final String RESULT_KEY = "result";

    private GoogleMap map;
    private Marker marker;
    private Button buttonOK;
    private Button buttonCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        buttonOK = (Button) findViewById(R.id.OK_button);
        buttonOK.setEnabled(false);
        buttonOK.setOnClickListener(this);

        buttonCancel = (Button) findViewById(R.id.Cancel_button);
        buttonCancel.setOnClickListener(this);

        Parcelable[] array = getIntent().getParcelableArrayExtra(HOT_POINTS_KEY);

        LinearLayout hotPointsPanel = (LinearLayout) findViewById(R.id.hot_points_panel);
        for (Parcelable hotPoint : array) {
            hotPointsPanel.addView(createButton((HotPoint) hotPoint));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @NonNull
    private Button createButton(@NonNull HotPoint point) {
        Button button = (Button) View.inflate(this, R.layout.hot_point_button, null);
        button.setOnClickListener(this);
        button.setText(point.getName());
        button.getBackground().setColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY);
        button.setTag(point);
        return button;
    }

    private void setSelectedPosition(@NonNull LatLng position) {
        if (marker != null) {
            marker.remove();
        } else {
            buttonOK.setEnabled(true);
        }
        marker = map.addMarker(new MarkerOptions().position(position));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        LatLng initialPosition = getIntent().getParcelableExtra(INITIAL_POSITION_KEY);
        float initialScale = getIntent().getFloatExtra(INITIAL_SCALE_KEY, 1);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPosition, initialScale));
        map.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        setSelectedPosition(latLng);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (v == buttonOK) {
            Intent result = new Intent();
            result.putExtra(RESULT_KEY, marker.getPosition());
            setResult(RESULT_OK, result);
            finish();
        } else if (v == buttonCancel) {
            setResult(RESULT_CANCELED);
            finish();
        }  else { // one of HotPoint buttons pressed
            HotPoint hotPoint = (HotPoint) v.getTag();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(hotPoint.getPosition(), 10));
            setSelectedPosition(hotPoint.getPosition());
        }
    }

    @NonNull
    public static IntentBuilder builder() {
        return new IntentBuilder();
    }

    @NonNull
    public static LatLng getSelectedPoint(@NonNull Intent data) {
        return data.getParcelableExtra(RESULT_KEY);
    }

    /**
     * Special class to create Intent for starting PlacePiker easier
     */
    public static class IntentBuilder {

        private final ArrayList<HotPoint> hotPoints;
        private LatLng initialPosition;
        private float initialScale;

        private IntentBuilder() {
            hotPoints = new ArrayList<>();
            initialScale = 1;
            initialPosition = new LatLng(0, 0);
        }

        @NonNull
        public IntentBuilder addHotPoint(@NonNull HotPoint hotPoint) {
            hotPoints.add(hotPoint);
            return this;
        }

        @NonNull
        public IntentBuilder addAllHotPoint(@NonNull Iterable<HotPoint> collection) {
            for (HotPoint hotPoint : collection) {
                hotPoints.add(hotPoint);
            }
            return this;
        }

        @NonNull
        public IntentBuilder setInitialPosition(@NonNull LatLng position) {
            initialPosition = position;
            return this;
        }

        @NonNull
        public IntentBuilder setInitialScale(float scale) {
            initialScale = scale;
            return this;
        }

        @NonNull
        public Intent build(@NonNull Context context) {
            Intent result = new Intent(context, PlacePicker.class);
            result.putExtra(HOT_POINTS_KEY, hotPoints.toArray(new Parcelable[hotPoints.size()]));
            result.putExtra(INITIAL_POSITION_KEY, initialPosition);
            result.putExtra(INITIAL_SCALE_KEY, initialScale);
            return result;
        }
    }
}
