package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import ru.spbau.mit.placenotifier.customizers.ActivityProducer;
import ru.spbau.mit.placenotifier.customizers.AddressPickerCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.AlternativeCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.ConstantCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.PlacePickerCustomizeEngine;
import ru.spbau.mit.placenotifier.predicates.Beacon;

public class NotificationEditor extends AppCompatActivity implements ActivityProducer{

    private ArrayList<ResultListener> listeners;

    AlternativeCustomizeEngine<Integer> timeCustomizer;
    AlternativeCustomizeEngine<?> testCustomizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listeners = new ArrayList<>();

        setContentView(R.layout.activity_notification_editor);

        // just a rough draft
        final AlternativeCustomizeEngine<Integer> timeCustomizer
                = new AlternativeCustomizeEngine<Integer>("Time settings",
                new ConstantCustomizeEngine<>("no matter 1", 1),
                new ConstantCustomizeEngine<>("no matter 2", 2),
                new ConstantCustomizeEngine<>("no matter 3", 3));
        final AlternativeCustomizeEngine<?> testCustomizer
                = new AlternativeCustomizeEngine<Beacon>("Time settings",
                new PlacePickerCustomizeEngine("Choose point on map", this, 1),
                new PlacePickerCustomizeEngine("Choose point on map", this, 2),
                new AddressPickerCustomizeEngine(this, "Write down your address here"));
        testCustomizer.observe(findViewById(R.id.test_bar));
        this.testCustomizer = testCustomizer;
        this.timeCustomizer = timeCustomizer;
        timeCustomizer.observe(findViewById(R.id.time_settings_bar));
        final AlternativeCustomizeEngine<Integer> placeCustomizer
                = new AlternativeCustomizeEngine<Integer>("Place settings",
                new ConstantCustomizeEngine<>("no matter 1", 1),
                new ConstantCustomizeEngine<>("no matter 2", 2),
                new ConstantCustomizeEngine<>("no matter 3", 3));
        placeCustomizer.observe(findViewById(R.id.place_settings_bar));
        Button okButton = (Button) findViewById(R.id.editor_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("time_settings", timeCustomizer.getValue());
                intent.putExtra("place_settings", placeCustomizer.getValue());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            timeCustomizer.restoreState(savedInstanceState.getBundle("time_state"));
            testCustomizer.restoreState(savedInstanceState.getBundle("time2_state"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("time_state", timeCustomizer.saveState());
        outState.putBundle("time2_state", testCustomizer.saveState());
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void startActivity(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void addResultListener(ResultListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (ResultListener listener : listeners) {
            if (listener.getID() == requestCode) {
                listener.onResult(resultCode, data);
            }
        }
    }
}


