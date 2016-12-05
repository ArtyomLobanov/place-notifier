package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Special interface, instance of which allow to create
 * Activity and get its result from
 */
public interface ActivityProducer {

    Context getContext();

    void startActivity(@NonNull Intent intent, int targetID);

    void addResultListener(@NonNull ResultListener listener);

    interface ResultListener {
        void onResult(int resultCode, @Nullable Intent data);

        int getID();
    }
}
