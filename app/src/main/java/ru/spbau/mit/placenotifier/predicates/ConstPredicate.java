package ru.spbau.mit.placenotifier.predicates;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ConstPredicate<T> implements SerializablePredicate<T> {

    private final boolean result;

    public ConstPredicate(boolean value) {
        result = value;
    }

    @Override
    public boolean apply(@NonNull T o) {
        return result;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
