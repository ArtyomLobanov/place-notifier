package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

/**
 * Activity shows maps and offer user to choose point on it
 */
public class PlacePicker extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, View.OnClickListener {

    private static final String CAMERA_POSITION_KEY = "camera_position";
    private static final String SELECTED_POSITION_KEY = "marker_key";

    private static final String HOT_POINTS_KEY = "hot_points";
    private static final String INITIAL_POSITION_KEY = "initial_position";
    private static final String INITIAL_SCALE_KEY = "initial_scale";
    private static final String RESULT_KEY = "result";

    private GoogleMap map;
    private Marker marker;
    private Button buttonOK;
    private Button buttonCancel;

    private CameraPosition savedCameraPosition;
    private LatLng savedSelectedPoint;

    @NonNull
    public static IntentBuilder builder() {
        return new IntentBuilder();
    }

    @NonNull
    public static LatLng getSelectedPoint(@NonNull Intent data) {
        return data.getParcelableExtra(RESULT_KEY);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        buttonOK = (Button) findViewById(R.id.OK_button);
        buttonOK.setEnabled(false);
        buttonOK.setOnClickListener(this);

        buttonCancel = (Button) findViewById(R.id.Cancel_button);
        buttonCancel.setOnClickListener(this);

        HotPoint[] hotPoints;
        try {
            hotPoints = (HotPoint[]) getIntent().getSerializableExtra(HOT_POINTS_KEY);
        } catch (ClassCastException e) {
            Log.e("PP", "Hot points loading failed");
            hotPoints = new HotPoint[0];
        }
        LinearLayout hotPointsPanel = (LinearLayout) findViewById(R.id.hot_points_panel);
        for (HotPoint hotPoint : hotPoints) {
            hotPointsPanel.addView(createButton(hotPoint));
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (map != null) {
            outState.putParcelable(CAMERA_POSITION_KEY, map.getCameraPosition());
            outState.putParcelable(SELECTED_POSITION_KEY, marker.getPosition());
        } else {
            outState.putParcelable(CAMERA_POSITION_KEY, savedCameraPosition);
            outState.putParcelable(SELECTED_POSITION_KEY, savedSelectedPoint);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        CameraPosition cameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION_KEY);
        LatLng selectedPoint = savedInstanceState.getParcelable(SELECTED_POSITION_KEY);
        if (map == null) {
            savedCameraPosition = cameraPosition;
            savedSelectedPoint = selectedPoint;
        } else {
            map.moveCamera(newCameraPosition(cameraPosition));
            if (selectedPoint != null) {
                setSelectedPosition(selectedPoint);
            }
        }
    }

    @NonNull
    private Button createButton(@NonNull HotPoint point) {
        Button button = (Button) View.inflate(this, R.layout.hot_point_button, null);
        button.setOnClickListener(this);
        button.setText(point.getName());
        button.getBackground().setColorFilter(point.getColor(), PorterDuff.Mode.MULTIPLY);
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
        map.setOnMapClickListener(this);
        if (savedCameraPosition == null) {
            LatLng initialPosition = getIntent().getParcelableExtra(INITIAL_POSITION_KEY);
            float initialScale = getIntent().getFloatExtra(INITIAL_SCALE_KEY, 1);
            map.moveCamera(newLatLngZoom(initialPosition, initialScale));
        } else {
            map.moveCamera(newCameraPosition(savedCameraPosition));
            if (savedSelectedPoint != null) {
                setSelectedPosition(savedSelectedPoint);
            }
            savedCameraPosition = null;
            savedSelectedPoint = null;
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
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
        } else { // one of HotPoint buttons pressed
            HotPoint hotPoint = (HotPoint) v.getTag();
            map.moveCamera(newLatLngZoom(hotPoint.getPosition(), hotPoint.getScale()));
            setSelectedPosition(hotPoint.getPosition());
        }
    }

    /**
     * Special class to create Intent for starting PlacePiker easier
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static final class IntentBuilder {

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
            HotPoint[] buffer = hotPoints.toArray(new HotPoint[hotPoints.size()]);

            Intent result = new Intent(context, PlacePicker.class);
            result.putExtra(HOT_POINTS_KEY, buffer);
            result.putExtra(INITIAL_POSITION_KEY, initialPosition);
            result.putExtra(INITIAL_SCALE_KEY, initialScale);
            return result;
        }
    }
}
