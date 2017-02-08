package ru.spbau.mit.placenotifier.customizers;

import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import ru.spbau.mit.placenotifier.HotPoint;
import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.LatLngBeacon;

public class HotPointPickerCustomizeEngine implements CustomizeEngine<Beacon> {

    private static final String SELECTED_HOT_POINT_KEY = "selected_hot_point";
    private static final int NO_SELECTION = -1;

    private final HotPoint[] hotPoints;
    private final String title;

    private ViewGroup container;
    private Button[] buttons;
    private int selectedIndex;

    public HotPointPickerCustomizeEngine(HotPoint[] hotPoints, String title) {
        this.hotPoints = hotPoints;
        this.title = title;
    }

    private void setSelected(int index) {
        if (buttons != null) {
            if (selectedIndex != NO_SELECTION) {
                buttons[selectedIndex].setTypeface(null, Typeface.NORMAL);
                buttons[selectedIndex].setEnabled(true);
            }
            buttons[index].setEnabled(false);
            buttons[index].setTypeface(null, Typeface.BOLD);
        }
        selectedIndex = index;
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_hot_point_picker;
    }

    private void clean() {
        if (buttons != null) {
            for (Button button : buttons) {
                button.setOnClickListener(null);
            }
            buttons = null;
        }
        if (container != null) {
            container.removeAllViews();
            container = null;
        }
    }

    @Override
    public void observe(@NonNull View view) {
        clean();

        TextView titleView = (TextView) view.findViewById(R.id.customize_hot_point_picker_title);
        titleView.setText(title);

        container = (ViewGroup) view.findViewById(R.id.customize_hot_point_picker_container);
        buttons = new Button[hotPoints.length];
        for (int i = 0; i < hotPoints.length; i++) {
            buttons[i] = createAndAddButton(i, container);
        }
        setSelected(selectedIndex);
    }

    private Button createAndAddButton(int index, @NonNull ViewGroup parent) {
        HotPoint hotPoint = hotPoints[index];
        Button button = (Button) View.inflate(parent.getContext(), R.layout.hot_point_button, null);
        button.setOnClickListener(v -> setSelected(index));
        button.setText(hotPoint.getName());
        button.getBackground().setColorFilter(hotPoint.getColor(), PorterDuff.Mode.MULTIPLY);
        parent.addView(button);
        return button;
    }

    @Override
    public boolean isReady() {
        return selectedIndex != NO_SELECTION;
    }

    @NonNull
    @Override
    public Beacon getValue() {
        if (selectedIndex == NO_SELECTION) {
            throw new IllegalStateException(ON_NOT_READY_STATE_EXCEPTION_MESSAGE);
        }
        return new LatLngBeacon(hotPoints[selectedIndex].getPosition());
    }

    @Override
    public boolean setValue(@NonNull Beacon value) {
        LatLng position = ((LatLngBeacon) value).getLatLng();
        for (int i = 0; i < hotPoints.length; i++) {
            if (position.equals(hotPoints[i].getPosition())) {
                setSelected(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        Object data = state.getSerializable(SELECTED_HOT_POINT_KEY);
        if (data == null) {
            setSelected(NO_SELECTION);
            return;
        }
        if (!(data instanceof HotPoint)) {
            throw new IllegalStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        HotPoint selectedPoint = (HotPoint) data;
        for (int i = 0; i < hotPoints.length; i++) {
            if (selectedPoint.equals(hotPoints[i])) {
                setSelected(i);
                return;
            }
        }
        throw new IllegalStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        if (selectedIndex != NO_SELECTION) {
            state.putSerializable(SELECTED_HOT_POINT_KEY, hotPoints[selectedIndex]);
        }
        return state;
    }
}
