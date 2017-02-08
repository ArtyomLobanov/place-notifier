package ru.spbau.mit.placenotifier.customizers;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import ru.spbau.mit.placenotifier.HotPoint;
import ru.spbau.mit.placenotifier.ResultRepeater;
import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

class PlacePredicateCustomizeEngine
        implements CustomizeEngine<SerializablePredicate<Location>> {

    private static final String PLACE_PICKER_STATE_KEY = "place_picker_state";
    private static final String RADIUS_PICKER_STATE_KEY = "radius_picker_state";
    private static final String MODE_PICKER_STATE_KEY = "mode_picker_state";

    private static final double DEFAULT_RADIUS_LEFT_BOUND = 10;
    private static final double DEFAULT_RADIUS_RIGHT_BOUND = 1000;

    private final CustomizeEngine<Beacon> placePicker;
    private final CustomizeEngine<Double> radiusPicker;
    private final CustomizeEngine<Boolean> modePicker;

    @SuppressWarnings("WeakerAccess")
    PlacePredicateCustomizeEngine(@NonNull CustomizeEngine<Beacon> placePicker,
                                  @NonNull CustomizeEngine<Double> radiusPicker,
                                  @NonNull CustomizeEngine<Boolean> modePicker) {
        this.placePicker = placePicker;
        this.radiusPicker = radiusPicker;
        this.modePicker = modePicker;
    }

    @SuppressWarnings("WeakerAccess")
    PlacePredicateCustomizeEngine(@NonNull CustomizeEngine<Beacon> placePicker,
                                  @NonNull CustomizeEngine<Double> radiusPicker) {
        this(placePicker, radiusPicker, Customizers.<Boolean>forOptions("Where does it work?")
                .addOption("When you are there", false)
                .addOption("When you are out of there", true)
                .build());
    }

    @SuppressWarnings("WeakerAccess")
    PlacePredicateCustomizeEngine(@NonNull CustomizeEngine<Beacon> placePicker) {
        this(placePicker,
                new NumericalValueCustomizeEngine("Define the sensitivity", "m",
                        NumericalValueCustomizeEngine.EXPONENTIAL_TRANSFORMER,
                        DEFAULT_RADIUS_LEFT_BOUND, DEFAULT_RADIUS_RIGHT_BOUND));
    }

    private static final HotPoint[] hp = {new HotPoint("spb", new LatLng(30, 60), Color.RED, 15),
        new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.GREEN, 14)};

    PlacePredicateCustomizeEngine(@NonNull ResultRepeater producer, int id) {
        this(new AlternativeCustomizeEngine<>("Choose place somehow",
                new PlacePickerCustomizeEngine("Choose point on map", producer, id),
                new AddressPickerCustomizeEngine(producer, "Find place by address"),
                new HotPointPickerCustomizeEngine(hp, "choose")));
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_place_predicate;
    }

    private void findAndInit(@NonNull View parent, @IdRes int containerID,
                             @NonNull CustomizeEngine<?> engine) {
        ViewGroup container = (ViewGroup) parent.findViewById(containerID);
        container.removeAllViews();
        View view = View.inflate(container.getContext(), engine.expectedViewLayout(), null);
        container.addView(view);
        engine.observe(view);
    }

    @Override
    public void observe(@NonNull View view) {
        findAndInit(view, R.id.customize_engine_place_predicate_place_picker, placePicker);
        findAndInit(view, R.id.customize_engine_place_predicate_radius_picker, radiusPicker);
        findAndInit(view, R.id.customize_engine_place_predicate_mode_picker, modePicker);
    }

    @Override
    public boolean isReady() {
        return placePicker.isReady() && radiusPicker.isReady() && modePicker.isReady();
    }

    @NonNull
    @Override
    public SerializablePredicate<Location> getValue() {
        return new BeaconPredicate(placePicker.getValue(), radiusPicker.getValue(),
                modePicker.getValue());
    }

    @Override
    public boolean setValue(@NonNull SerializablePredicate<Location> value) {
        if (value.getClass() != BeaconPredicate.class) {
            return false;
        }
        BeaconPredicate beaconPredicate = (BeaconPredicate) value;

        return Customizers.safeExecution(this, () ->
                placePicker.setValue(beaconPredicate.getBeacon())
                        && radiusPicker.setValue(beaconPredicate.getRadius())
                        && modePicker.setValue(beaconPredicate.isInverted()));
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        Bundle placePickerState = state.getBundle(PLACE_PICKER_STATE_KEY);
        Bundle radiusPickerState = state.getBundle(RADIUS_PICKER_STATE_KEY);
        Bundle modePickerState = state.getBundle(MODE_PICKER_STATE_KEY);
        if (placePickerState == null || radiusPickerState == null || modePickerState == null) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        placePicker.restoreState(placePickerState);
        radiusPicker.restoreState(radiusPickerState);
        modePicker.restoreState(modePickerState);
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putBundle(PLACE_PICKER_STATE_KEY, placePicker.saveState());
        state.putBundle(RADIUS_PICKER_STATE_KEY, radiusPicker.saveState());
        state.putBundle(MODE_PICKER_STATE_KEY, modePicker.saveState());
        return state;
    }
}
