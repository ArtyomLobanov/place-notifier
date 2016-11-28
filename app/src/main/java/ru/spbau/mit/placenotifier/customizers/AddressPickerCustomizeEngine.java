package ru.spbau.mit.placenotifier.customizers;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.Beacon;

public class AddressPickerCustomizeEngine implements CustomizeEngine<Beacon> {

    private static final String INPUT_STATE_KEY = "input_state_key";
    private static final String MONITOR_STATE_KEY = "monitor_state_key";


    private final ActivityProducer activityProducer;
    private final InputListener inputListener;
    private final String titleMessage;
    private EditText input;
    private TextView monitor;

    private Address result;

    public AddressPickerCustomizeEngine(ActivityProducer activityProducer, String titleMessage) {
        this.activityProducer = activityProducer;
        inputListener = new InputListener();
        this.titleMessage = titleMessage;
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_address_picker;
    }

    private void clean() {
        result = null;
        if (input != null) {
            input.removeTextChangedListener(inputListener);
            input = null;
        }
        monitor = null;
    }

    @Override
    public void observe(@Nullable View view) {
        clean();
        if (view != null) {
            TextView titleView =
                    (TextView) view.findViewById(R.id.customize_engine_address_picker_title);
            titleView.setText(titleMessage);
            input = (EditText) view.findViewById(R.id.customize_engine_address_input);
            input.setText("");
            input.addTextChangedListener(inputListener);
            monitor = (TextView) view.findViewById(R.id.customize_engine_address_picker_monitor);
            reportError("Empty address");
        }
    }

    @Override
    public boolean isReady() {
        return result != null;
    }

    @NonNull
    @Override
    public Beacon getValue() {
        return new Beacon(result);
    }

    @Override
    public boolean setValue(@Nullable Beacon value) {
        return false;
    }

    @Override
    public void restoreState(@Nullable Bundle state) {
        if (input == null) {
            throw new WrongStateException(CustomizeEngine.ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE);
        }
        if (state == null) {
            return;
        }
        input.onRestoreInstanceState(state.getParcelable(INPUT_STATE_KEY));
        monitor.onRestoreInstanceState(state.getParcelable(MONITOR_STATE_KEY));
        reportWarning("Searching...");
        updateResult();
    }

    @Nullable
    @Override
    public Bundle saveState() {
        if (monitor == null || input == null) {
            return null;
        }
        Bundle state = new Bundle();
        state.putParcelable(MONITOR_STATE_KEY, monitor.onSaveInstanceState());
        state.putParcelable(INPUT_STATE_KEY, input.onSaveInstanceState());
        return state;
    }

    private void reportWarning(String message) {
        monitor.setText(message);
        monitor.setTextColor(Color.MAGENTA);
    }

    private void reportError(String message) {
        monitor.setText(message);
        monitor.setTextColor(Color.RED);
    }

    private void reportSuccess(String message) {
        monitor.setText(message);
        monitor.setTextColor(Color.GREEN);
    }

    private void updateResult() {
        String address = input.getText().toString();
        if (address.length() == 0) {
            result = null;
            reportError("Empty address");
        } else {
            AsyncTask<Void, Void, List<Address>> task = new AddressSearcher(address);
            task.execute();
        }
    }

    private class InputListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            reportWarning("Searching...");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            updateResult();
        }
    }

    private class AddressSearcher extends AsyncTask<Void, Void, List<Address>> {

        private final String request;

        AddressSearcher(String request) {
            this.request = request;
        }

        @Override
        protected List<Address> doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(activityProducer.getContext());
            List<Address> addresses;
            try {
                addresses = geocoder.getFromLocationName(request, 5);
            } catch (IOException e) {
                return null;
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {
            if (input == null || !input.getText().toString().equals(request)) {
                return; // our information is out of date
            }
            if (addresses == null || addresses.isEmpty()) {
                reportError("Address was not found");
                result = null;
            } else if (addresses.size() > 1) {
                reportWarning("Address is not unique");
                result = addresses.get(0);
            } else {
                reportSuccess("Address is valid");
                result = addresses.get(0);
            }
        }
    }
}
