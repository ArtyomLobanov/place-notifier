package ru.spbau.mit.placenotifier.customizers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import ru.spbau.mit.placenotifier.R;

public class ConstantCustomizeEngine<T> implements CustomizeEngine<T> {

    private final String messageText;
    private final T result;

    public ConstantCustomizeEngine(String messageText, T result) {
        this.messageText = messageText;
        this.result = result;
    }

    @Override
    public int expectedViewLayout() {
        Thread d = new Thread(() -> {return;});
        return R.layout.customize_engine_constant;
    }

    @Override
    public void observe(@Nullable View view) {
        if (view != null) {
            TextView messageView = (TextView) view.findViewById(R.id.message_view);
            messageView.setText(messageText);
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @NonNull
    @Override
    public T getValue() {
        return result;
    }

    @Override
    public boolean setValue(@Nullable T value) {
        return result.equals(value);
    }

    // this customize engine have no state

    @Override
    public void restoreState(@Nullable Bundle state) {}

    @Nullable
    @Override
    public Bundle saveState() {
        return null;
    }
}
