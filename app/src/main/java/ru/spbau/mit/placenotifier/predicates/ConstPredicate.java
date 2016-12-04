package ru.spbau.mit.placenotifier.predicates;

public class ConstPredicate<T> implements SerializablePredicate<T> {

    private final boolean result;

    public ConstPredicate(boolean value) {
        this.result = value;
    }

    @Override
    public boolean apply(T o) {
        return result;
    }

    public boolean isResult() {
        return result;
    }
}
