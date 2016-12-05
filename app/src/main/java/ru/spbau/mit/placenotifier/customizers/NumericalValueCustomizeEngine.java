package ru.spbau.mit.placenotifier.customizers;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import ru.spbau.mit.placenotifier.R;

@SuppressWarnings("WeakerAccess")
public class NumericalValueCustomizeEngine implements CustomizeEngine<Double> {

    @SuppressWarnings("unused")
    public static final NumericTransformer LINEAR_TRANSFORMER =
            (leftBound, rightBound, point, pointsCount) ->
                    leftBound + (rightBound - leftBound) / pointsCount * point;

    public static final NumericTransformer EXPONENTIAL_TRANSFORMER =
            (leftBound, rightBound, point, pointsCount) ->
                    leftBound * Math.pow(rightBound / leftBound, (double) point / pointsCount);

    private static final String CURRENT_POINT_KEY = "current_point_key";
    private static final String CACHED_VALUE_KEY = "current_value_key";
    private static final int DEFAULT_DISCRETIZATION_RATE = 10000;

    private final String titleMessage;
    private final String measureUnit;
    private final NumericTransformer transformer;
    private final double leftBound;
    private final double rightBound;
    private final int discretizationRate;
    private final SeekBarListener listener;

    private TextView monitor;
    private SeekBar seekBar;

    private int currentPoint;
    private Double cachedValue;

    public NumericalValueCustomizeEngine(@NonNull String titleMessage, @NonNull String measureUnit,
                                         @NonNull NumericTransformer transformer,
                                         double leftBound, double rightBound,
                                         int discretizationRate) {
        this.titleMessage = titleMessage;
        this.measureUnit = measureUnit;
        this.transformer = transformer;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
        this.discretizationRate = discretizationRate;
        listener = new SeekBarListener();
    }

    public NumericalValueCustomizeEngine(@NonNull String titleMessage, @NonNull String measureUnit,
                                         @NonNull NumericTransformer transformer,
                                         double leftBound, double rightBound) {
        this(titleMessage, measureUnit, transformer, leftBound, rightBound,
                DEFAULT_DISCRETIZATION_RATE);
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_numerical_value;
    }

    private void clean() {
        if (seekBar != null) {
            seekBar.setOnSeekBarChangeListener(null);
            seekBar = null;
        }
        monitor = null;
    }

    @Override
    public void observe(@NonNull View view) {
        clean();
        TextView titleView =
                (TextView) view.findViewById(R.id.customize_engine_numerical_value_title);
        titleView.setText(titleMessage);
        monitor = (TextView) view.findViewById(R.id.customize_engine_numerical_value_monitor);
        seekBar = (SeekBar) view.findViewById(R.id.customize_engine_numerical_value_seekBar);
        seekBar.setOnSeekBarChangeListener(listener);
        seekBar.setMax(discretizationRate);
        updateMonitor();
        updateSeekBar();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @NonNull
    @Override
    public Double getValue() {
        return transformer.transformValue(leftBound, rightBound, currentPoint, discretizationRate);
    }

    private int findNearestPoint(double value) {
        if (value <= getValueAt(0)) {
            return 0;
        }
        if (value >= getValueAt(discretizationRate)) {
            return discretizationRate;
        }
        int leftPointer = 0; // getValueAt(leftPointer) <= value
        int rightPointer = discretizationRate; // getValueAt(rightPointer) > value
        int middlePointer;

        while (rightPointer - leftPointer > 1) {
            middlePointer = (rightPointer + leftPointer) / 2;
            if (getValueAt(middlePointer) > value) {
                rightPointer = middlePointer;
            } else {
                leftPointer = middlePointer;
            }
        }

        double leftDifference = value - getValueAt(leftPointer);
        double rightDifference = getValueAt(rightPointer) - value;
        return rightDifference < leftDifference ? rightPointer : leftPointer;
    }

    @Override
    public boolean setValue(@NonNull Double value) {
        if (!(leftBound <= value && value <= rightBound)) {
            return false;
        }
        // approximately the desired position
        currentPoint = findNearestPoint(value);
        // cache it to return absolutely equal value
        // if user has not changed it
        cachedValue = value;
        updateSeekBar();
        updateMonitor();
        return true;
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        currentPoint = state.getInt(CURRENT_POINT_KEY);
        cachedValue = null;
        if (state.containsKey(CACHED_VALUE_KEY)) {
            cachedValue = state.getDouble(CACHED_VALUE_KEY);
        }
        updateSeekBar();
        updateMonitor();
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putInt(CURRENT_POINT_KEY, currentPoint);
        if (cachedValue != null) {
            state.putDouble(CACHED_VALUE_KEY, cachedValue);
        }
        return state;
    }

    private double getValueAt(int point) {
        return transformer.transformValue(leftBound, rightBound, point, discretizationRate);
    }

    private void updateMonitor() {
        if (monitor != null) {
            double value = cachedValue != null ? cachedValue : getValueAt(currentPoint);
            monitor.setText(String.format(Locale.getDefault(), "%.1f %s", value, measureUnit));
        }
    }

    private void updateSeekBar() {
        if (seekBar != null) {
            seekBar.setProgress(currentPoint);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public interface NumericTransformer {
        double transformValue(double leftBound, double rightBound, int point, int pointsCount);
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                currentPoint = progress;
                cachedValue = null;
                updateMonitor();
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
