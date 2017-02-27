package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoFragment extends Fragment {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View v = inflater.inflate(R.layout.fragment_info, container, false);
        TextView text = (TextView) v.findViewById(R.id.info_text);
        Linkify.addLinks(text, Linkify.WEB_URLS);
        return v;
    }
}
