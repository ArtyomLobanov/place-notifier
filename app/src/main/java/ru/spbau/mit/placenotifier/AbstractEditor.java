package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import ru.spbau.mit.placenotifier.SmartListAdapter.Creator;
import ru.spbau.mit.placenotifier.customizers.CustomizeEngine;

public class AbstractEditor<T extends Serializable> extends AppCompatActivity
        implements ResultRepeater {

    private static final String RESULT_KEY = "result_key";
    private static final String PROTOTYPE_KEY = "prototype_key";
    private static final String CUSTOMIZE_ENGINE_STATE_KEY = "engine_state_key";

    private final Collection<ResultListener> listeners = new ArrayList<>();
    private CustomizeEngine<T> customizeEngine;
    private Class<T> objectType;

    @NonNull
    public static IntentBuilder<?> builder() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    protected static <T extends Serializable> IntentBuilder<T>
    builder(@NonNull Class<? extends AbstractEditor<T>> editorClass) {
        return new IntentBuilder<>(editorClass);
    }

    @NonNull
    public static <T> T getResult(@NonNull Intent data, @NonNull Class<T> expectedType) {
        Object o = data.getSerializableExtra(RESULT_KEY);
        if (!expectedType.isInstance(o)) {
            throw new BadDataFormatException("Result not found");
        }
        return expectedType.cast(o);
    }

    @Nullable
    public static <T> T getPrototype(@NonNull Intent data, @NonNull Class<T> expectedType) {
        Object o = data.getSerializableExtra(PROTOTYPE_KEY);
        if (o == null) {
            return null;
        }
        if (!expectedType.isInstance(o)) {
            throw new BadDataFormatException("Result not found");
        }
        return expectedType.cast(o);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        throw new UnsupportedOperationException();
    }

    protected void onCreate(@Nullable Bundle savedInstanceState,
                            @NonNull Creator<CustomizeEngine<T>> customizeEngineCreator,
                            @NonNull Class<T> objectType,
                            @LayoutRes int layoutID) {
        this.objectType = objectType;
        listeners.clear();
        super.onCreate(savedInstanceState);

        setContentView(layoutID);

        customizeEngine = customizeEngineCreator.create();
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
                Toast.makeText(AbstractEditor.this, "Fill all fields, please",
                        Toast.LENGTH_SHORT).show();
            }
        });
        button.setFocusableInTouchMode(true);
        button.requestFocus();
        int primaryColor = ContextCompat.getColor(this, R.color.colorPrimary);
        button.getBackground().setColorFilter(primaryColor, PorterDuff.Mode.MULTIPLY);
    }

    private void setupInitialState() {
        Object o = getIntent().getSerializableExtra(PROTOTYPE_KEY);
        if (o != null) {
            if (!objectType.isInstance(o)) {
                throw new BadDataFormatException("Bad initial intent format");
            }
            customizeEngine.setValue(objectType.cast(o));
        }
    }

    @Nullable
    private Intent prepareResultIntent() {
        if (!customizeEngine.isReady()) {
            return null;
        }
        Intent result = new Intent();
        result.putExtra(RESULT_KEY, customizeEngine.getValue());
        T prototype = getPrototype(getIntent(), objectType);
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

    static final class IntentBuilder<T extends Serializable> {
        private T prototype;
        private Class<? extends AbstractEditor> editorClass;

        private IntentBuilder(@NonNull Class<? extends AbstractEditor> editorClass) {
            this.editorClass = editorClass;
        }

        IntentBuilder setPrototype(@NonNull T value) {
            prototype = value;
            return this;
        }

        Intent build(@NonNull Context context) {
            Intent intent = new Intent(context, editorClass);
            if (prototype != null) {
                intent.putExtra(PROTOTYPE_KEY, prototype);
            }
            return intent;
        }
    }

    public static class BadDataFormatException extends RuntimeException {
        BadDataFormatException(String message) {
            super(message);
        }
    }
}


