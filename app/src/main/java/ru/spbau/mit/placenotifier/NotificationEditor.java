package ru.spbau.mit.placenotifier;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import ru.spbau.mit.placenotifier.customizers.AlternativeCustomizeEngine;
import ru.spbau.mit.placenotifier.customizers.ConstantCustomizeEngine;

public class NotificationEditor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_editor);

        // just a rough draft
        final AlternativeCustomizeEngine<Integer> timeCustomizer = new AlternativeCustomizeEngine<Integer>("Time settings",
                new ConstantCustomizeEngine<>("no matter 1", 1),
                new ConstantCustomizeEngine<>("no matter 2", 2),
                new ConstantCustomizeEngine<>("no matter 3", 3));
        timeCustomizer.observe(findViewById(R.id.time_settings_bar));
        final AlternativeCustomizeEngine<Integer> placeCustomizer = new AlternativeCustomizeEngine<Integer>("Place settings",
                new ConstantCustomizeEngine<>("no matter 1", 1),
                new ConstantCustomizeEngine<>("no matter 2", 2),
                new ConstantCustomizeEngine<>("no matter 3", 3));
        placeCustomizer.observe(findViewById(R.id.place_settings_bar));
        Button okButton = (Button) findViewById(R.id.editor_ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("time_settings", timeCustomizer.getValue());
                intent.putExtra("place_settings", placeCustomizer.getValue());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


}


