package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import ru.spbau.mit.placenotifier.SmartListAdapter.Creator;
import ru.spbau.mit.placenotifier.customizers.CustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.HotPointCustomizeEngine;

public class HotPointEditor extends AbstractEditor<HotPoint> {

    // Weaker type of prototype may cause an error,
    // because AlarmEditor intended to edit Alarm
    @SuppressWarnings("TypeMayBeWeakened")
    public static Intent prepareIntent(@Nullable HotPoint prototype, @NonNull Context context) {
        Intent intent = new Intent(context, HotPointEditor.class);
        if (prototype != null) {
            putPrototype(intent, prototype);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        Creator<CustomizeEngine<HotPoint>> creator = () -> new HotPointCustomizeEngine(this, 0);
        onCreate(savedState, creator, HotPoint.class, R.layout.activity_hot_point_editor);
    }

}


