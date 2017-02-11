package ru.spbau.mit.placenotifier;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.spbau.mit.placenotifier.customizers.AlarmCustomizeEngine;

public class AlarmEditor extends AbstractEditor<Alarm> {

    public static IntentBuilder<Alarm> builder() {
        return AbstractEditor.builder(AlarmEditor.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        onCreate(savedInstanceState, () -> new AlarmCustomizeEngine(this, 0), Alarm.class,
                R.layout.activity_alarm_editor);
    }

}


