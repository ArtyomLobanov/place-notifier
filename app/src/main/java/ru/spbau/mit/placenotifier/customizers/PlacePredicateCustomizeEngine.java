package ru.spbau.mit.placenotifier.customizers;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class PlacePredicateCustomizeEngine
        implements CustomizeEngine<SerializablePredicate<Location>> {

    private static final String PLACE_PICKER_STATE_KEY = "place_picker_state";
    private static final String RADIUS_PICKER_STATE_KEY = "radius_picker_state";
    private static final String MODE_PICKER_STATE_KEY = "mode_picker_state";

    private static final double DEFAULT_RADIUS_LEFT_BOUND = 10;
    private static final double DEFAULT_RADIUS_RIGHT_BOUND = 1000;

    private final CustomizeEngine<Beacon> placePicker;
    private final CustomizeEngine<Double> radiusPicker;
    private final CustomizeEngine<Boolean> modePicker;

    public PlacePredicateCustomizeEngine(CustomizeEngine<Beacon> placePicker,
                                         CustomizeEngine<Double> radiusPicker,
                                         CustomizeEngine<Boolean> modePicker) {
        this.placePicker = placePicker;
        this.radiusPicker = radiusPicker;
        this.modePicker = modePicker;
    }

    public PlacePredicateCustomizeEngine(CustomizeEngine<Beacon> placePicker,
                                         CustomizeEngine<Double> radiusPicker) {
        this(placePicker, radiusPicker, Customizers.<Boolean>forOptions("Where does it work?")
                .addOption("When you are there", false)
                .addOption("When you are out of there", true)
                .build());
    }

    public PlacePredicateCustomizeEngine(CustomizeEngine<Beacon> placePicker) {
        this(placePicker,
                new NumericalValueCustomizeEngine("Define the sensitivity", "m",
                        NumericalValueCustomizeEngine.EXPONENTIAL_TRANSFORMER,
                        DEFAULT_RADIUS_LEFT_BOUND, DEFAULT_RADIUS_RIGHT_BOUND));
    }

    public PlacePredicateCustomizeEngine(ActivityProducer producer, int id) {
        this(new AlternativeCustomizeEngine<>("Choose place somehow",
                new PlacePickerCustomizeEngine("Choose point on map", producer, id),
                new AddressPickerCustomizeEngine(producer, "Find place by address")));
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_place_predicate;
    }

    private void findAndInit(View parent, @IdRes int containerID, CustomizeEngine<?> engine) {
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
        if (!placePicker.setValue(beaconPredicate.getBeacon())) {
            return false;
        }
        // // TODO: 03.12.2016 add method "canSetValue" to customize engine interface
        radiusPicker.setValue(beaconPredicate.getRadius());
        modePicker.setValue(beaconPredicate.isInverted());
        return true;
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
