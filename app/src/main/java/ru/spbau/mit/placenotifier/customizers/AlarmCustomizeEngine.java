package ru.spbau.mit.placenotifier.customizers;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

import ru.spbau.mit.placenotifier.ResultRepeater;
import ru.spbau.mit.placenotifier.Alarm;
import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.ConstPredicate;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class AlarmCustomizeEngine implements CustomizeEngine<Alarm> {

    private static final String NAME_EDITOR_STATE_KEY = "name_editor";
    private static final String COMMENT_EDITOR_STATE_KEY = "comment_editor";
    private static final String PLACE_PREDICATE_EDITOR_STATE_KEY = "place_predicate_editor";
    private static final String TIME_PREDICATE_EDITOR_STATE_KEY = "time_predicate_editor";
    private static final String STATUS_EDITOR_STATE_KEY = "status_editor";
    private static final String ALARM_IDENTIFIER_KEY = "alarm_id";

    private final CustomizeEngine<String> nameEditor;
    private final CustomizeEngine<String> commentEditor;
    private final CustomizeEngine<SerializablePredicate<Location>> placePredicateEditor;
    private final CustomizeEngine<SerializablePredicate<Long>> timePredicateEditor;
    private final CustomizeEngine<Boolean> statusEditor;
    private final Context context;

    private String alarmIdentifier;

    public AlarmCustomizeEngine(@NonNull ResultRepeater producer, int id) {
        context = producer.getParentActivity();
        nameEditor = new StringCustomizeEngine("Name of alarm",
                StringCustomizeEngine.NOT_EMPTY);
        commentEditor = new StringCustomizeEngine("Comments",
                StringCustomizeEngine.MULTILINE);
        placePredicateEditor = new PlacePredicateCustomizeEngine(producer, id);
        timePredicateEditor = new AlternativeCustomizeEngine<>("Select working time",
                new TimeIntervalCustomizeEngine(producer, "Choose interval"),
                new ConstantCustomizeEngine<>("Always", new ConstPredicate<>(true)));
        statusEditor = Customizers.<Boolean>forOptions("Current status")
                .addOption("Active", true).addOption("Not active", false).build();
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_alarm;
    }

    private void findAndInit(View parent, @IdRes int containerID, CustomizeEngine<?> engine) {
        View view = parent.findViewById(containerID);
        engine.observe(view);
    }

    @Override
    public void observe(@NonNull View view) {
        findAndInit(view, R.id.customize_engine_alarm_name_input, nameEditor);
        findAndInit(view, R.id.customize_engine_alarm_comment_input, commentEditor);
        findAndInit(view, R.id.customize_engine_alarm_place_setting, placePredicateEditor);
        findAndInit(view, R.id.customize_engine_alarm_time_setting, timePredicateEditor);
        findAndInit(view, R.id.customize_engine_alarm_status, statusEditor);
    }

    @Override
    public boolean isReady() {
        return nameEditor.isReady() && commentEditor.isReady()
                && placePredicateEditor.isReady() && timePredicateEditor.isReady();
    }

    @NonNull
    @Override
    public Alarm getValue() {
        Alarm.AlarmBuilder builder = Alarm.builder()
                .setName(nameEditor.getValue())
                .setComment(commentEditor.getValue())
                .setPlacePredicate(placePredicateEditor.getValue())
                .setTimePredicate(timePredicateEditor.getValue())
                .setActive(statusEditor.getValue());
        if (alarmIdentifier != null) {
            builder.setIdentifier(alarmIdentifier);
        } else {
            builder.createIdentifier(context);
        }
        return builder.build();
    }

    @Override
    public boolean setValue(@NonNull Alarm value) {
        return Customizers.safeExecution(this, () -> {
            alarmIdentifier = value.getIdentifier();
            return nameEditor.setValue(value.getName())
                    && commentEditor.setValue(value.getComment())
                    && placePredicateEditor.setValue(value.getPlacePredicate())
                    && timePredicateEditor.setValue(value.getTimePredicate())
                    && statusEditor.setValue(value.isActive());
        });
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        Bundle nameEditorState = state.getBundle(NAME_EDITOR_STATE_KEY);
        Bundle commentEditorState = state.getBundle(COMMENT_EDITOR_STATE_KEY);
        Bundle placePredicateEditorState = state.getBundle(PLACE_PREDICATE_EDITOR_STATE_KEY);
        Bundle timePredicateEditorState = state.getBundle(TIME_PREDICATE_EDITOR_STATE_KEY);
        Bundle statusEditorState = state.getBundle(STATUS_EDITOR_STATE_KEY);
        if (nameEditorState == null || commentEditorState == null
                || placePredicateEditorState == null || timePredicateEditorState == null
                || statusEditorState == null) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        nameEditor.restoreState(nameEditorState);
        commentEditor.restoreState(commentEditorState);
        placePredicateEditor.restoreState(placePredicateEditorState);
        timePredicateEditor.restoreState(timePredicateEditorState);
        statusEditor.restoreState(statusEditorState);
        alarmIdentifier = state.getString(ALARM_IDENTIFIER_KEY);
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putBundle(NAME_EDITOR_STATE_KEY, nameEditor.saveState());
        state.putBundle(COMMENT_EDITOR_STATE_KEY, commentEditor.saveState());
        state.putBundle(PLACE_PREDICATE_EDITOR_STATE_KEY, placePredicateEditor.saveState());
        state.putBundle(TIME_PREDICATE_EDITOR_STATE_KEY, timePredicateEditor.saveState());
        state.putBundle(STATUS_EDITOR_STATE_KEY, statusEditor.saveState());
        if (alarmIdentifier != null) {
            state.putString(ALARM_IDENTIFIER_KEY, alarmIdentifier);
        }
        return state;
    }
}
