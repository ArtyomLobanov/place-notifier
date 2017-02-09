package ru.spbau.mit.placenotifier;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.spbau.mit.placenotifier.SmartListAdapter.Creator;
import ru.spbau.mit.placenotifier.customizers.CustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.HotPointCustomizeEngine;

public class HotPointEditor extends AbstractEditor<HotPoint> {

    @Override
    protected void onCreate(@Nullable Bundle savedState) {
        Creator<CustomizeEngine<HotPoint>> creator = () -> new HotPointCustomizeEngine(this, 0);
        onCreate(savedState, creator, HotPoint.class, R.layout.activity_hot_point_editor);
    }

    public static AbstractEditor.IntentBuilder<Alarm> builder() {
        return AbstractEditor.builder(AlarmEditor.class);
    }


}


