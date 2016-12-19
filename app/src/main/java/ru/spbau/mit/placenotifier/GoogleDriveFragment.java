package ru.spbau.mit.placenotifier;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class GoogleDriveFragment extends Fragment {

    Button update;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_google_drive, container, false);

        update = (Button)v.findViewById(R.id.update);
        update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GoogleDriveSync.class);
                startActivity(intent);
            }
        });
        return v;
    }

}
