package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Special interface, instance of which allow to create
 * Activity and get its result from
 */
public interface ResultRepeater {

    Activity getParentActivity();

    void addResultListener(@NonNull ResultListener listener);

    interface ResultListener {
        void onResult(int requestCode, int resultCode, @Nullable Intent data);
    }
}
