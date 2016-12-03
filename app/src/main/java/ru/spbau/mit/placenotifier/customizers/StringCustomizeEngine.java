package ru.spbau.mit.placenotifier.customizers;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ru.spbau.mit.placenotifier.R;

public class StringCustomizeEngine implements CustomizeEngine<String> {

    public static final int MULTILINE = 1 << 1;
    public static final int NOT_EMPTY = 1 << 2;
    private static final String RESULT_KEY = "result_key";
    private final int flags;
    private final TextWatcher listener;
    private final String titleMessage;

    private EditText input;
    private String result;

    public StringCustomizeEngine(String titleMessage, int flags) {
        this.flags = flags;
        this.titleMessage = titleMessage;
        listener = new InputListener();
        result = "";
    }


    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_string;
    }

    private void clean() {
        if (input != null) {
            input.removeTextChangedListener(listener);
        }
    }

    @Override
    public void observe(@NonNull View view) {
        clean();
        TextView titleView = (TextView) view.findViewById(R.id.customize_engine_string_title);
        titleView.setText(titleMessage);
        input = (EditText) view.findViewById(R.id.customize_engine_string_input);
        input.addTextChangedListener(listener);
        input.setSingleLine(!checkFlag(MULTILINE));
        updateInput();
    }

    @Override
    public boolean isReady() {
        return !checkFlag(NOT_EMPTY) || !result.isEmpty();
    }

    @NonNull
    @Override
    public String getValue() {
        if (!isReady()) {
            throw new WrongStateException(ON_NOT_READY_STATE_EXCEPTION_MESSAGE);
        }
        return result;
    }

    @Override
    public boolean setValue(@NonNull String value) {
        if (!checkFlag(MULTILINE) && value.contains("\n")) {
            return false;
        }
        result = value;
        updateInput();
        return true;
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        if (!state.containsKey(RESULT_KEY)) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        result = state.getString(RESULT_KEY);
        updateInput();
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putString(RESULT_KEY, result);
        return state;
    }

    private void updateInput() {
        if (input != null) {
            input.setText(result);
        }
    }

    private boolean checkFlag(int flag) {
        return (flags & flag) != 0;
    }

    private class InputListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String value = s.toString();
            if (!checkFlag(MULTILINE)) {
                String validValue = value.replaceAll("\\n", "");
                if (validValue.length() != value.length()) {
                    int cursorPosition = input.getSelectionStart();
                    input.setText(validValue);
                    int newCursorPosition = cursorPosition - (value.length() - validValue.length());
                    input.setSelection(newCursorPosition);
                    input.clearFocus();
                }
                value = validValue;
            }
            result = value;
        }
    }
}
