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

import ru.spbau.mit.placenotifier.ResultRepeater;
import ru.spbau.mit.placenotifier.PlacePicker;
import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.LatLngBeacon;

class PlacePickerCustomizeEngine implements CustomizeEngine<Beacon>, OnMapReadyCallback,
        ResultRepeater.ResultListener, GoogleMap.OnMapClickListener {

    private static final String SELECTED_LOCATION_KEY = "selected_location_state";
    private static final float DEFAULT_MAP_SCALE = 15;

    private final int id;
    private final ResultRepeater resultRepeater;
    private final String title;
    private GoogleMap map;
    private LatLng result;

    PlacePickerCustomizeEngine(@NonNull String title,
                               @NonNull ResultRepeater resultRepeater, int id) {
        this.resultRepeater = resultRepeater;
        this.id = id;
        this.title = title;
        resultRepeater.addResultListener(this);
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_place_piker;
    }

    private void clean() {
        if (map != null) {
            map.setOnMapClickListener(null);
            map = null;
        }
    }

    @Override
    public void observe(@NonNull View view) {
        clean();
        TextView titleView = (TextView) view.findViewById(R.id.customize_engine_place_piker_title);
        titleView.setText(title);
        MapView mapView = (MapView) view.findViewById(R.id.customize_engine_place_piker_map);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    @Override
    public boolean isReady() {
        return result != null;
    }

    @NonNull
    @Override
    public Beacon getValue() {
        if (map == null) {
            throw new WrongStateException(ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE);
        }
        if (result == null) {
            throw new WrongStateException(ON_NOT_READY_STATE_EXCEPTION_MESSAGE);
        }
        return new LatLngBeacon(result);
    }

    @Override
    public boolean setValue(@NonNull Beacon value) {
        if (value.getClass() != LatLngBeacon.class) {
            return false;
        }
        LatLngBeacon latLngBeacon = (LatLngBeacon) value;
        result = latLngBeacon.getLatLng();
        updateCamera();
        return true;
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        result = state.getParcelable(SELECTED_LOCATION_KEY);
        updateCamera();
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putParcelable(SELECTED_LOCATION_KEY, result);
        return state;
    }

    private void updateCamera() {
        if (map == null) {
            return;
        }
        map.clear();
        if (result != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(result, DEFAULT_MAP_SCALE));
            map.addMarker(new MarkerOptions().position(result));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMapClickListener(this);
        updateCamera();
    }

    @Override
    public void onResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == id && resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        result = PlacePicker.getSelectedPoint(data);
        map.clear();
        updateCamera();
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        Intent request = PlacePicker.builder().build(resultRepeater.getParentActivity());
        resultRepeater.getParentActivity().startActivityForResult(request, id);
    }
}
