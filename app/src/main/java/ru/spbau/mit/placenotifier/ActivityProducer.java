package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.content.Intent;

public interface ActivityProducer {

    Context getContext();
    void startActivity(Intent intent, int targetID);
    void addResultListener(ResultListener listener);

    interface ResultListener {
        void onResult(int resultCode, Intent data);
        int getID();
    }
}
