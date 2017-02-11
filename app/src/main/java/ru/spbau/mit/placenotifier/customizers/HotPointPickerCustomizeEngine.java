package ru.spbau.mit.placenotifier.customizers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import ru.spbau.mit.placenotifier.HotPoint;
import ru.spbau.mit.placenotifier.HotPointManager;
import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.Beacon;
import ru.spbau.mit.placenotifier.predicates.LatLngBeacon;

class HotPointPickerCustomizeEngine implements CustomizeEngine<Beacon> {

    private static final String SELECTED_HOT_POINT_KEY = "selected_hot_point";
    private static final String FILTER_KEY = "filter";
    private static final int NO_SELECTION = -1;

    private List<HotPoint> hotPoints;

    private ViewGroup container;
    private EditText filterInput;
    private List<View> views;

    private int selectedIndex;
    private String filter;
    private final TextWatcher filterWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            filter = editable.toString().toUpperCase();
            applyFilter();
        }
    };

    HotPointPickerCustomizeEngine(Context context) {
        views = new ArrayList<>();
        HotPointManager hotPointManager = new HotPointManager(context);
        hotPoints = hotPointManager.getHotPoints();
        selectedIndex = NO_SELECTION;
        filter = "";
    }

    private void setSelected(int index) {
        if (!views.isEmpty()) {
            if (selectedIndex != NO_SELECTION) {
                int color = hotPoints.get(selectedIndex).getColor();
                views.get(selectedIndex).getBackground().setColorFilter(color, Mode.MULTIPLY);
            }
            if (index != NO_SELECTION) {
                views.get(index).getBackground().setColorFilter(Color.GRAY, Mode.MULTIPLY);
            }
        }
        selectedIndex = index;
    }

    private void applyFilter() {
        if (views.isEmpty()) {
            return;
        }
        for (int i = 0; i < views.size(); i++) {
            if (hotPoints.get(i).getName().startsWith(filter)) {
                views.get(i).setVisibility(View.VISIBLE);
            } else {
                views.get(i).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_hot_point_picker;
    }

    private void clean() {
        for (View view : views) {
            view.setOnClickListener(null);
        }
        views.clear();
        if (container != null) {
            container.removeAllViews();
            container = null;
        }
        if (filterInput != null) {
            filterInput.removeTextChangedListener(filterWatcher);
        }
    }

    @Override
    public void observe(@NonNull View view) {
        clean();

        container = (ViewGroup) view.findViewById(R.id.customize_hot_point_picker_container);
        container.removeAllViews();
        for (int i = 0; i < hotPoints.size(); i++) {
            views.add(createAndAddView(i, hotPoints.get(i), container));
        }

        filterInput = (EditText) view.findViewById(R.id.filter_input);
        filterInput.setText(filter);
        filterInput.addTextChangedListener(filterWatcher);

        setSelected(selectedIndex);
        applyFilter();
    }

    private View createAndAddView(int index, @NonNull HotPoint hotPoint, @NonNull ViewGroup group) {
        TextView view = (TextView) View.inflate(group.getContext(), R.layout.hot_point_view, null);
        view.setOnClickListener(v -> setSelected(index));
        view.setText(hotPoint.getName());
        view.getBackground().setColorFilter(hotPoint.getColor(), Mode.MULTIPLY);
        group.addView(view);
        return view;
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
        return new LatLngBeacon(hotPoints.get(selectedIndex).getPosition());
    }

    @Override
    public boolean setValue(@NonNull Beacon value) {
        LatLng position = ((LatLngBeacon) value).getLatLng();
        for (int i = 0; i < hotPoints.size(); i++) {
            if (position.equals(hotPoints.get(i).getPosition())) {
                filter = hotPoints.get(i).getName();
                setSelected(i);
                applyFilter();
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
        filter = state.getString(FILTER_KEY);
        if (!(data instanceof HotPoint) || filter == null) {
            throw new IllegalStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        if (filterInput != null) {
            filterInput.setText(filter);
        }
        HotPoint selectedPoint = (HotPoint) data;
        for (int i = 0; i < hotPoints.size(); i++) {
            if (selectedPoint.equals(hotPoints.get(i))) {
                setSelected(i);
                applyFilter();
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
            state.putSerializable(SELECTED_HOT_POINT_KEY, hotPoints.get(selectedIndex));
        }
        state.putString(FILTER_KEY, filter);
        return state;
    }
}
