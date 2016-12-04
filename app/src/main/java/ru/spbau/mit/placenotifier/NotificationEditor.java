package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

import ru.spbau.mit.placenotifier.customizers.ActivityProducer;
import ru.spbau.mit.placenotifier.customizers.AddressPickerCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.AlternativeCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.ConstantCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.CustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.NotificationCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.NumericalValueCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.PlacePickerCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.PlacePredicateCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.StringCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.TimeIntervalCustomizeEngine;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.BeaconPredicate;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class NotificationEditor extends AppCompatActivity implements ActivityProducer{

    private ArrayList<ResultListener> listeners;
    private CustomizeEngine<Notification> customizeEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listeners = new ArrayList<>();

        setContentView(R.layout.activity_notification_editor);

        customizeEngine = new NotificationCustomizeEngine(this, 0);
        customizeEngine.observe(findViewById(R.id.container));

        if (savedInstanceState == null) {
            loadNotification();
        }
    }

    // only for demo
    private void loadNotification() {
        Notification n = (Notification) getIntent().getSerializableExtra("notification");
        if (n == null) return;
        customizeEngine.setValue(n);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            customizeEngine.restoreState(savedInstanceState.getBundle("not"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("not", customizeEngine.saveState());
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


