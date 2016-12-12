package ru.spbau.mit.placenotifier.predicates;

import android.support.annotation.NonNull;

public class TimeIntervalPredicate implements SerializablePredicate<Long> {

    private final long from;
    private final long to;

    public TimeIntervalPredicate(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean apply(@NonNull Long aLong) {
        return from <= aLong && aLong <= to;
    }

    public long getTo() {
        return to;
    }

    public long getFrom() {
        return from;
    }
}
