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
import java.io.Serializable;
import java.util.List;

import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.AddressBeacon;
import ru.spbau.mit.placenotifier.predicates.Beacon;

public class AddressPickerCustomizeEngine implements CustomizeEngine<Beacon> {

    private static final String REQUEST_VALUE_KEY = "request_key";
    private static final String MONITOR_STATE_KEY = "monitor_state_key";
    private static final String RESULT_KEY = "result_key";


    private final ActivityProducer activityProducer;
    private final InputListener inputListener;
    private final String titleMessage;
    private EditText input;
    private Monitor monitor;

    private Address result;
    private String request;

    public AddressPickerCustomizeEngine(ActivityProducer activityProducer, String titleMessage) {
        this.activityProducer = activityProducer;
        inputListener = new InputListener();
        this.titleMessage = titleMessage;
        monitor = new Monitor();
        request = "";
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_address_picker;
    }

    private void clean() {
        if (input != null) {
            input.removeTextChangedListener(inputListener);
            input = null;
        }
        monitor.observe(null);
    }

    @Override
    public void observe(@NonNull View view) {
        clean();
        TextView titleView =
                (TextView) view.findViewById(R.id.customize_engine_address_picker_title);
        titleView.setText(titleMessage);

        input = (EditText) view.findViewById(R.id.customize_engine_address_input);
        input.setText(request);
        input.addTextChangedListener(inputListener);

        TextView display =
                (TextView) view.findViewById(R.id.customize_engine_address_picker_monitor);
        monitor.observe(display);
    }

    @Override
    public boolean isReady() {
        return result != null;
    }

    @NonNull
    @Override
    public Beacon getValue() {
        if (result == null) {
            throw new WrongStateException(ON_NOT_READY_STATE_EXCEPTION_MESSAGE);
        }
        return new AddressBeacon(result, request);
    }

    @Override
    public boolean setValue(@NonNull Beacon value) {
        if (value.getClass() != AddressBeacon.class) {
            return false;
        }
        AddressBeacon addressBeacon = (AddressBeacon) value;
        request = addressBeacon.getAddressLine();
        result = addressBeacon.getAddress();
        monitor.reportSuccess("Cached address");
        return true;
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        request = state.getString(REQUEST_VALUE_KEY);
        if (request == null) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        result = state.getParcelable(RESULT_KEY);
        if (input != null) {
            input.setText(request);
        }
        TextView display = monitor.display;
        monitor = (Monitor) state.getSerializable(MONITOR_STATE_KEY);
        if (monitor == null) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        monitor.observe(display);
        if (result == null) {
            updateResult();
        }
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putString(REQUEST_VALUE_KEY, request);
        state.putSerializable(MONITOR_STATE_KEY, monitor);
        state.putParcelable(RESULT_KEY, result);
        return state;
    }

    private void updateResult() {
        if (request.length() == 0) {
            result = null;
            monitor.reportError("Empty address");
        } else {
            AsyncTask<Void, Void, List<Address>> task = new AddressSearcher(request);
            task.execute();
        }
    }

    private static class Monitor implements Serializable {

        private final static int SUCCESS = 0;
        private final static int WARNING = 1;
        private final static int ERROR = 2;
        private final static int[] COLORS = {Color.GREEN, Color.MAGENTA, Color.RED};

        transient TextView display;
        int status;
        String text;

        Monitor() {
            reportError("Empty address");
        }

        void observe(@Nullable TextView textView) {
            display = textView;
            updateDisplay();
        }

        void reportWarning(@NonNull String message) {
            report(WARNING, message);
        }

        void reportError(@NonNull String message) {
            report(ERROR, message);
        }

        void reportSuccess(@NonNull String message) {
            report(SUCCESS, message);
        }

        private void report(int status, @NonNull String message) {
            this.status = status;
            text = message;
            updateDisplay();
        }

        private void updateDisplay() {
            if (display != null) {
                display.setText(text);
                display.setTextColor(COLORS[status]);
            }
        }
    }

    private class InputListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            monitor.reportWarning("Searching...");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.toString().equals(request) && result != null) {
                return;
            }
            request = s.toString();
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
            if (input == null || !AddressPickerCustomizeEngine.this.request.equals(request)) {
                return; // our information is out of date
            }
            if (addresses == null || addresses.isEmpty()) {
                monitor.reportError("Address was not found");
                result = null;
            } else if (addresses.size() > 1) {
                monitor.reportWarning("Address is not unique");
                result = addresses.get(0);
            } else {
                result = addresses.get(0);
                monitor.reportSuccess("Address is valid");
            }
        }
    }
}
