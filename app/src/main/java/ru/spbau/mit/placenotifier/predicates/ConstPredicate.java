package ru.spbau.mit.placenotifier.predicates;

public class ConstPredicate<T> implements SerializablePredicate<T> {

    private final boolean result;

    public ConstPredicate(boolean value) {
        result = value;
    }

    @Override
    public boolean apply(T o) {
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConstPredicate<?> that = (ConstPredicate<?>) o;
        return result == that.result;

    }

    @Override
    public int hashCode() {
        return (result ? 1 : 0);
    }
}
