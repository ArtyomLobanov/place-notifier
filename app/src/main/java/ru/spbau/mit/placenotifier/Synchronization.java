package ru.spbau.mit.placenotifier;

import android.view.View;
import android.widget.Button;

@SuppressWarnings("unused")
public class Synchronization {
    private Button sync;

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
