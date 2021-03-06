package ru.spbau.mit.placenotifier.customizers;

import android.content.Context;
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

import java.io.Serializable;
import java.util.List;

import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.AddressBeacon;
import ru.spbau.mit.placenotifier.predicates.Beacon;

class AddressPickerCustomizeEngine implements CustomizeEngine<Beacon> {

    private static final String REQUEST_VALUE_KEY = "request_key";
    private static final String MONITOR_STATE_KEY = "monitor_state_key";
    private static final String RESULT_KEY = "result_key";


    private final Context context;
    private final InputListener inputListener;
    private final String titleMessage;
    private EditText input;
    private Monitor monitor;

    private Address result;
    private String request;

    @SuppressWarnings("WeakerAccess")
    AddressPickerCustomizeEngine(Context context, String titleMessage) {
        this.context = context;
        this.titleMessage = titleMessage;
        inputListener = new InputListener();
        monitor = new Monitor();
        request = "";
    }

    AddressPickerCustomizeEngine(Context context) {
        this(context, context.getString(R.string.address_picker_customize_engine_title));
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
        if (request.isEmpty()) {
            result = null;
            monitor.reportError("Empty address");
        } else {
            AsyncTask<Void, Void, List<Address>> task = new AddressSearcher(request);
            task.execute();
        }
    }

    private static class Monitor implements Serializable {

        private static final int SUCCESS = 0;
        private static final int WARNING = 1;
        private static final int ERROR = 2;
        private static final int[] COLORS = {Color.GREEN, Color.MAGENTA, Color.RED};

        private transient TextView display;
        private int status;
        private String text;

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

        AddressSearcher(@NonNull String request) {
            this.request = request;
        }

        @Override
        @Nullable
        protected List<Address> doInBackground(Void... params) {
            List<Address> addresses;
            try {
                Geocoder geocoder = new Geocoder(context);
                addresses = geocoder.getFromLocationName(request, 2);
            } catch (Exception e) {
                return null;
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(@Nullable List<Address> addresses) {
            if (input == null || !AddressPickerCustomizeEngine.this.request.equals(request)) {
                return; // our information is out of date
            }
            if (addresses == null) {
                monitor.reportError("Connection error");
                result = null;
            } else if (addresses.isEmpty()) {
                monitor.reportError("Address was not found");
                result = null;
            } else if (addresses.size() != 1) {
                monitor.reportWarning("Address is not unique");
                result = addresses.get(0);
            } else {
                result = addresses.get(0);
                monitor.reportSuccess("Address is valid");
            }
        }
    }
}
