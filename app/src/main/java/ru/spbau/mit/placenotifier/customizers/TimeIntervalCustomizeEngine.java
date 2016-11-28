package ru.spbau.mit.placenotifier.customizers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;

public class TimeIntervalCustomizeEngine implements CustomizeEngine<SerializablePredicate<Long>> {

    private static final String FROM_TIME_VALUE_KEY = "from_time_value_key";
    private static final String TO_TIME_VALUE_KEY = "ro_time_value_key";

    private final ActivityProducer activityProducer;
    private final String titleMessage;

    private TextView fromTime;
    private TextView fromDate;
    private TextView toTime;
    private TextView toDate;

    private Calendar from;
    private Calendar to;

    public TimeIntervalCustomizeEngine(ActivityProducer activityProducer, String titleMessage) {
        this.activityProducer = activityProducer;
        this.titleMessage = titleMessage;
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_time_interval;
    }

    private void clean() {
        if (fromTime != null) {
            fromTime.setOnClickListener(null);
            fromDate.setOnClickListener(null);
            toTime.setOnClickListener(null);
            toDate.setOnClickListener(null);
        }
        from = null;
        to = null;
        fromTime = null;
        fromDate = null;
        toTime = null;
        toDate = null;
    }

    @Override
    public void observe(@Nullable View view) {
        clean();
        if (view == null) {
            return;
        }
        ((TextView) view.findViewById(R.id.customize_engine_time_interval_title))
                .setText(titleMessage); ;

        fromTime = (TextView) view.findViewById(R.id.customize_engine_time_interval_from_time);
        fromDate = (TextView) view.findViewById(R.id.customize_engine_time_interval_from_date);
        toTime = (TextView) view.findViewById(R.id.customize_engine_time_interval_to_time);
        toDate = (TextView) view.findViewById(R.id.customize_engine_time_interval_to_date);
        setupInitialValues();
        updateViews();
        setupListeners(fromTime, fromDate, from);
        setupListeners(toTime, toDate, to);
    }

    private void setupInitialValues() {
        long currentTime = System.currentTimeMillis();
        from = new GregorianCalendar();
        from.setTimeInMillis(currentTime);
        to = new GregorianCalendar();
        to.setTimeInMillis(currentTime);
        to.add(Calendar.DAY_OF_YEAR, 1);
    }

    private void updateViews() {
        fromTime.setText(DateUtils.formatDateTime(activityProducer.getContext(),
                from.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        fromDate.setText(DateUtils.formatDateTime(activityProducer.getContext(),
                from.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));

        toTime.setText(DateUtils.formatDateTime(activityProducer.getContext(),
                to.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        toDate.setText(DateUtils.formatDateTime(activityProducer.getContext(),
                to.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    private void setupListeners(TextView time, TextView date, Calendar calendar) {
        TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
            calendar.set(Calendar.HOUR, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            updateViews();
        };
        time.setOnClickListener((v) -> new TimePickerDialog(activityProducer.getContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true)
                .show());

        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateViews();
        };
        date.setOnClickListener((v) -> new DatePickerDialog(activityProducer.getContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show());
    }

    @Override
    public boolean isReady() {
        return from != null && to != null && from.before(to);
    }

    @NonNull
    @Override
    public SerializablePredicate<Long> getValue() {
        return new TimeIntervalPredicate(from.getTimeInMillis(), to.getTimeInMillis());
    }

    @Override
    public boolean setValue(@Nullable SerializablePredicate<Long> value) {
        return false;
    }

    @Override
    public void restoreState(@Nullable Bundle state) {
        if (fromTime == null) {
            throw new WrongStateException(ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE);
        }
        if (state == null) {
            return;
        }
        from.setTimeInMillis(state.getLong(FROM_TIME_VALUE_KEY));
        to.setTimeInMillis(state.getLong(TO_TIME_VALUE_KEY));
        updateViews();
    }

    @Nullable
    @Override
    public Bundle saveState() {
        if (fromTime == null || toTime == null) {
            return null;
        }
        Bundle state = new Bundle();
        state.putLong(FROM_TIME_VALUE_KEY, from.getTimeInMillis());
        state.putLong(TO_TIME_VALUE_KEY, to.getTimeInMillis());
        return state;
    }
}
