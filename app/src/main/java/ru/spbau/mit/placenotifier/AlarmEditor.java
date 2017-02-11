package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.spbau.mit.placenotifier.customizers.AlarmCustomizeEngine;

public class AlarmEditor extends AbstractEditor<Alarm> {

    // Weaker type of prototype may cause an error,
    // because AlarmEditor intended to edit Alarm
    @SuppressWarnings("TypeMayBeWeakened")
    public static Intent prepareIntent(@Nullable Alarm prototype, @NonNull Context context) {
        Intent intent = new Intent(context, AlarmEditor.class);
        if (prototype != null) {
            putPrototype(intent, prototype);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        onCreate(savedInstanceState, () -> new AlarmCustomizeEngine(this, 0), Alarm.class,
                R.layout.activity_alarm_editor);
    }

}


