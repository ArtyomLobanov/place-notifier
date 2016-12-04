package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import ru.spbau.mit.placenotifier.customizers.CustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.NotificationCustomizeEngine;

public class NotificationEditor extends AppCompatActivity implements ActivityProducer {

    private static final String RESULT_KEY = "result_key";
    private static final String PROTOTYPE_KEY = "prototype_key";
    private static final String CUSTOMIZE_ENGINE_STATE_KEY = "engine_state_key";

    private ArrayList<ResultListener> listeners;
    private CustomizeEngine<Notification> customizeEngine;

    public static IntentBuilder builder() {
        return new IntentBuilder();
    }

    @NonNull
    public static Notification getResult(@NonNull Intent date) {
        return (Notification) date.getSerializableExtra(RESULT_KEY);
    }

    @Nullable
    public static Notification getPrototype(@NonNull Intent date) {
        return (Notification) date.getSerializableExtra(PROTOTYPE_KEY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listeners = new ArrayList<>();

        setContentView(R.layout.activity_notification_editor);

        customizeEngine = new NotificationCustomizeEngine(this, 0);
        customizeEngine.observe(findViewById(R.id.container));

        if (savedInstanceState == null) {
            setupInitialState();
        }

        Button button = (Button) findViewById(R.id.editor_ok_button);
        button.setOnClickListener(v -> {
            if (customizeEngine.isReady()) {
                setResult(Activity.RESULT_OK, prepareResultIntent());
                finish();
            } else {
                Toast.makeText(NotificationEditor.this, "Fill all fields, please",
                        Toast.LENGTH_SHORT).show();
            }
        });
        button.setFocusableInTouchMode(true);
        button.requestFocus();

    }

    private void setupInitialState() {
        Notification prototype = (Notification) getIntent().getSerializableExtra(PROTOTYPE_KEY);
        if (prototype != null) {
            customizeEngine.setValue(prototype);
        }
    }

    private Intent prepareResultIntent() {
        if (!customizeEngine.isReady()) {
            return null;
        }
        Intent result = new Intent();
        result.putExtra(RESULT_KEY, customizeEngine.getValue());
        Notification prototype = getPrototype(getIntent());
        if (prototype != null) {
            result.putExtra(PROTOTYPE_KEY, prototype);
        }
        return result;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState == null) {
            return;
        }
        Bundle customizeEngineState = savedInstanceState.getBundle(CUSTOMIZE_ENGINE_STATE_KEY);
        if (customizeEngineState != null) {
            customizeEngine.restoreState(customizeEngineState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(CUSTOMIZE_ENGINE_STATE_KEY, customizeEngine.saveState());
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

    public static class IntentBuilder {
        private Notification prototype;

        private IntentBuilder() {
        }

        public IntentBuilder setPrototype(Notification notification) {
            prototype = notification;
            return this;
        }

        Intent build(Context context) {
            Intent intent = new Intent(context, NotificationEditor.class);
            if (prototype != null) {
                intent.putExtra(PROTOTYPE_KEY, prototype);
            }
            return intent;
        }
    }
}


