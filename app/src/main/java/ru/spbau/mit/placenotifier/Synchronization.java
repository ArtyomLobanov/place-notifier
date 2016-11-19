package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

/**
 * Created by daphne on 19.11.16.
 */

public class Synchronization {
    public Button sync;
    Synchronization() {
        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Here will be synchronizaton with progress bar.
                 */
            }
        });
    }

}
