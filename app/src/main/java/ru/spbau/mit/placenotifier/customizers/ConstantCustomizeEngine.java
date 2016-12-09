package ru.spbau.mit.placenotifier.customizers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import ru.spbau.mit.placenotifier.R;

class ConstantCustomizeEngine<T> implements CustomizeEngine<T> {

    private final String messageText;
    private final T result;

    ConstantCustomizeEngine(@NonNull String messageText, @NonNull T result) {
        this.messageText = messageText;
        this.result = result;
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_constant;
    }

    @Override
    public void observe(@NonNull View view) {
        TextView messageView = (TextView) view.findViewById(R.id.message_view);
        messageView.setText(messageText);
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
    public boolean setValue(@NonNull T value) {
        return value.equals(result);
    }

    // this customize engine have no state

    @Override
    public void restoreState(@NonNull Bundle state) {
    }

    @NonNull
    @Override
    public Bundle saveState() {
        return Bundle.EMPTY;
    }
}
