package ru.spbau.mit.placenotifier.customizers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.GregorianCalendar;

import ru.spbau.mit.placenotifier.ActivityProducer;
import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;
import ru.spbau.mit.placenotifier.predicates.TimeIntervalPredicate;

@SuppressWarnings("WeakerAccess")
public class TimeIntervalCustomizeEngine implements CustomizeEngine<SerializablePredicate<Long>> {

    private static final String FROM_TIME_VALUE_KEY = "from_time_value_key";
    private static final String TO_TIME_VALUE_KEY = "to_time_value_key";

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
        fromTime = null;
        fromDate = null;
        toTime = null;
        toDate = null;
    }

    @Override
    public void observe(@NonNull View view) {
        clean();
        TextView title = (TextView) view.findViewById(R.id.customize_engine_time_interval_title);
        title.setText(titleMessage);

        fromTime = (TextView) view.findViewById(R.id.customize_engine_time_interval_from_time);
        fromDate = (TextView) view.findViewById(R.id.customize_engine_time_interval_from_date);
        toTime = (TextView) view.findViewById(R.id.customize_engine_time_interval_to_time);
        toDate = (TextView) view.findViewById(R.id.customize_engine_time_interval_to_date);
        if (from == null || to == null) {
            setupInitialValues();
        }
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
        if (fromTime == null || toTime == null) {
            return;
        }
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
    public boolean setValue(@NonNull SerializablePredicate<Long> value) {
        if (value.getClass() != TimeIntervalPredicate.class) {
            return false;
        }
        TimeIntervalPredicate timeIntervalPredicate = (TimeIntervalPredicate) value;
        if (from == null || to == null) {
            setupInitialValues();
        }
        from.setTimeInMillis(timeIntervalPredicate.getFrom());
        to.setTimeInMillis(timeIntervalPredicate.getTo());
        updateViews();
        return true;
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        from = (Calendar) state.getSerializable(FROM_TIME_VALUE_KEY);
        to = (Calendar) state.getSerializable(TO_TIME_VALUE_KEY);
        updateViews();
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        state.putSerializable(FROM_TIME_VALUE_KEY, from);
        state.putSerializable(TO_TIME_VALUE_KEY, to);
        return state;
    }
}
