package ru.spbau.mit.placenotifier.predicates;


import com.android.internal.util.Predicate;

import java.io.Serializable;

public interface SerializablePredicate<T> extends Serializable, Predicate<T> {
}
