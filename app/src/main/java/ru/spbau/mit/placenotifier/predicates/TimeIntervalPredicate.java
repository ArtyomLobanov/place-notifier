package ru.spbau.mit.placenotifier.predicates;

public class TimeIntervalPredicate implements SerializablePredicate<Long> {

    private final long from;
    private final long to;

    public TimeIntervalPredicate(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean apply(Long aLong) {
        return from <= aLong && aLong <= to;
    }
}
