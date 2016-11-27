package ru.spbau.mit.placenotifier.customizers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.spbau.mit.placenotifier.PlacePicker;
import ru.spbau.mit.placenotifier.R;

public class PlacePikerCustomizeEngine implements CustomizeEngine<LatLng>, OnMapReadyCallback,
        ActivityProducer.ResultListener, GoogleMap.OnMapClickListener {

    private static final String SELECTED_LOCATION_KEY = "selected_location_state";
    private static final float DEFAULT_MAP_SCALE = 15;

    private final int id;
    private final ActivityProducer activityProducer;
    private final String title;
    private GoogleMap map;
    private LatLng result;
    private MapView mapView;


    public PlacePikerCustomizeEngine(String title, ActivityProducer activityProducer, int id) {
        this.activityProducer = activityProducer;
        this.id = id;
        this.title = title;
        activityProducer.addResultListener(this);
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_place_piker;
    }

    @Override
    public void observe(@Nullable View view) {
        map = null;
        result = null;
        mapView = null;
        if (view == null) {
            return;
        }
        TextView titleView = (TextView) view.findViewById(R.id.customize_engine_place_piker_title);
        titleView.setText(title);
        mapView = (MapView) view.findViewById(R.id.customize_engine_place_piker_map);
        mapView.onCreate(null);
        mapView.getMapAsync(this);

    }

    @Override
    public boolean isReady() {
        return result != null;
    }

    @NonNull
    @Override
    public LatLng getValue() {
        if (map == null) {
            throw new WrongStateException(ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE);
        }
        if (result == null) {
            throw new WrongStateException(ON_NOT_READY_STATE_EXCEPTION_MESSAGE);
        }
        return result;
    }

    @Override
    public boolean setValue(@Nullable LatLng value) {
        if (mapView == null) {
            return false;
        }
        result = value;
        updateCamera();
        return true;
    }

    @Override
    public void restoreState(@Nullable Bundle state) {
        if (mapView == null) {
            throw new WrongStateException(CustomizeEngine.ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE);
        }
        if (state == null) {
            return;
        }
        result = state.getParcelable(SELECTED_LOCATION_KEY);
        updateCamera();
    }

    @Nullable
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putParcelable(SELECTED_LOCATION_KEY, result);
        return state;
    }

    private void updateCamera() {
        if (map == null || result == null) {
            return;
        }
        map.clear();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(result, DEFAULT_MAP_SCALE));
        map.addMarker(new MarkerOptions().position(result));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(this);
        updateCamera();
    }

    @Override
    public void onResult(int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        result = PlacePicker.getSelectedPoint(data);
        map.clear();
        updateCamera();
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Intent request = PlacePicker.builder().build(activityProducer.getContext());
        activityProducer.startActivity(request, id);
    }
}
