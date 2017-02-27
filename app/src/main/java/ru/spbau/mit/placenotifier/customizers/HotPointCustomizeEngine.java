package ru.spbau.mit.placenotifier.customizers;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

import ru.spbau.mit.placenotifier.HotPoint;
import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.ResultRepeater;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.LatLngBeacon;

public class HotPointCustomizeEngine implements CustomizeEngine<HotPoint> {

    private static final String NAME_EDITOR_STATE_KEY = "name_editor";
    private static final String PLACE_EDITOR_STATE_KEY = "place_editor";
    private static final String COLOR_EDITOR_STATE_KEY = "color_editor";

    private final CustomizeEngine<String> nameEditor;
    private final CustomizeEngine<Beacon> placeEditor;
    private final CustomizeEngine<Integer> colorEditor;

    public HotPointCustomizeEngine(@NonNull ResultRepeater producer, int id) {
        nameEditor = new StringCustomizeEngine("Name of alarm",
                StringCustomizeEngine.NOT_EMPTY);
        placeEditor = Customizers.createCombinedBeaconCustomizeEngine(producer, id, false);
        colorEditor = Customizers.<Integer>forOptions("Choose color")
                .addOption("Red", Color.RED, Color.RED)
                .addOption("Blue", Color.BLUE, Color.BLUE)
                .addOption("Yellow", Color.YELLOW, Color.YELLOW)
                .addOption("Magenta", Color.MAGENTA, Color.MAGENTA)
                .build();
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_hot_point;
    }

    private void findAndInit(View parent, @IdRes int containerID, CustomizeEngine<?> engine) {
        View view = parent.findViewById(containerID);
        engine.observe(view);
    }

    @Override
    public void observe(@NonNull View view) {
        findAndInit(view, R.id.customize_engine_hot_point_name_input, nameEditor);
        findAndInit(view, R.id.customize_engine_hot_point_place_setting, placeEditor);
        findAndInit(view, R.id.customize_engine_hot_point_color_chooser, colorEditor);
    }

    @Override
    public boolean isReady() {
        return nameEditor.isReady() && placeEditor.isReady()
                && colorEditor.isReady();
    }

    @NonNull
    @Override
    public HotPoint getValue() {
        return new HotPoint(nameEditor.getValue().toUpperCase(),
                placeEditor.getValue().toLatLng(), colorEditor.getValue(), 15);
    }

    @Override
    public boolean setValue(@NonNull HotPoint value) {
        return Customizers.safeExecution(this, () ->
                nameEditor.setValue(value.getName())
                        && placeEditor.setValue(new LatLngBeacon(value.getPosition()))
                        && colorEditor.setValue(value.getColor())
        );
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        Bundle nameEditorState = state.getBundle(NAME_EDITOR_STATE_KEY);
        Bundle placeEditorState = state.getBundle(PLACE_EDITOR_STATE_KEY);
        Bundle colorEditorState = state.getBundle(COLOR_EDITOR_STATE_KEY);
        if (nameEditorState == null || placeEditorState == null || colorEditorState == null) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        nameEditor.restoreState(nameEditorState);
        placeEditor.restoreState(placeEditorState);
        colorEditor.restoreState(colorEditorState);
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putBundle(NAME_EDITOR_STATE_KEY, nameEditor.saveState());
        state.putBundle(PLACE_EDITOR_STATE_KEY, placeEditor.saveState());
        state.putBundle(COLOR_EDITOR_STATE_KEY, colorEditor.saveState());
        return state;
    }
}
