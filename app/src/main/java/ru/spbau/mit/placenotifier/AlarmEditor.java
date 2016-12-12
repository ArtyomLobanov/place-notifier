package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import ru.spbau.mit.placenotifier.customizers.AlarmCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.CustomizeEngine;

public class AlarmEditor extends AppCompatActivity implements ResultRepeater {

    private static final String RESULT_KEY = "result_key";
    private static final String PROTOTYPE_KEY = "prototype_key";
    private static final String CUSTOMIZE_ENGINE_STATE_KEY = "engine_state_key";

    private ArrayList<ResultListener> listeners;
    private CustomizeEngine<Alarm> customizeEngine;

    @NonNull
    public static IntentBuilder builder() {
        return new IntentBuilder();
    }

    @NonNull
    public static Alarm getResult(@NonNull Intent date) {
        return (Alarm) date.getSerializableExtra(RESULT_KEY);
    }

    @Nullable
    public static Alarm getPrototype(@NonNull Intent date) {
        return (Alarm) date.getSerializableExtra(PROTOTYPE_KEY);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listeners = new ArrayList<>();

        setContentView(R.layout.activity_alarm_editor);

        customizeEngine = new AlarmCustomizeEngine(this, 0);
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
                Toast.makeText(AlarmEditor.this, "Fill all fields, please",
                        Toast.LENGTH_SHORT).show();
            }
        });
        button.setFocusableInTouchMode(true);
        button.requestFocus();

    }

    private void setupInitialState() {
        Alarm prototype = (Alarm) getIntent().getSerializableExtra(PROTOTYPE_KEY);
        if (prototype != null) {
            customizeEngine.setValue(prototype);
        }
    }

    @Nullable
    private Intent prepareResultIntent() {
        if (!customizeEngine.isReady()) {
            return null;
        }
        Intent result = new Intent();
        result.putExtra(RESULT_KEY, customizeEngine.getValue());
        Alarm prototype = getPrototype(getIntent());
        if (prototype != null) {
            result.putExtra(PROTOTYPE_KEY, prototype);
        }
        return result;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Bundle savedInstanceState) {
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(CUSTOMIZE_ENGINE_STATE_KEY, customizeEngine.saveState());
    }

    @Override
    public Activity getParentActivity() {
        return this;
    }

    @Override
    public void addResultListener(@NonNull ResultListener listener) {
        listeners.add(listener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //noinspection Convert2streamapi  (not supported at current API level)
        for (ResultListener listener : listeners) {
            listener.onResult(requestCode, resultCode, data);
        }
    }

    static final class IntentBuilder {
        private Alarm prototype;

        private IntentBuilder() {
        }

        IntentBuilder setPrototype(@NonNull Alarm alarm) {
            prototype = alarm;
            return this;
        }

        Intent build(@NonNull Context context) {
            Intent intent = new Intent(context, AlarmEditor.class);
            if (prototype != null) {
                intent.putExtra(PROTOTYPE_KEY, prototype);
            }
            return intent;
        }
    }
}


