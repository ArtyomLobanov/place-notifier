package ru.spbau.mit.placenotifier;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class HotPointsListFragment extends Fragment {

    private HotPointManager hotPointManager;
    private ViewGroup tableView;
    private View createButton;
    private ResultRepeater resultRepeater;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View result = inflater.inflate(R.layout.fragment_hot_points_list, container, false);
        tableView = (ViewGroup) result.findViewById(R.id.hot_points_list_table);

        createButton = inflater.inflate(R.layout.hot_points_list_add_button, container, false);
        createButton.setOnClickListener(createHotPoint);
        createButton.findViewById(R.id.inner_add_button).setOnClickListener(createHotPoint);
        tableView.addView(createButton);


        resultRepeater = (ResultRepeater) getActivity();
        resultRepeater.addResultListener((x, y, z) -> refresh());

        hotPointManager = new HotPointManager(getActivity());

        refresh();
        return result;
    }

    private void refresh() {
        new Loader().execute();
    }

    @Nullable
    private View createItem(HotPoint hotPoint) {
        if (getActivity() == null) {
            return null;
        }
        View view = View.inflate(getActivity(), R.layout.hot_points_list_item, null);
        view.setOnClickListener(editHotPoint);

        TextView nameView = (TextView) view.findViewById(R.id.item_name);
        nameView.setOnClickListener(editHotPoint);

        Button deleteButton = (Button) view.findViewById(R.id.item_delete_button);
        deleteButton.setOnClickListener(deleteHotPoint);
        deleteButton.setTag(view);
        updateItem(view, hotPoint);
        return view;
    }

    private void updateItem(@NonNull View view, @NonNull HotPoint hotPoint) {
        view.setTag(hotPoint);
        view.getBackground().setColorFilter(hotPoint.getColor(), Mode.MULTIPLY);

        TextView nameView = (TextView) view.findViewById(R.id.item_name);
        nameView.setText(hotPoint.getName());
        nameView.setTag(hotPoint);
    }

    private final View.OnClickListener editHotPoint = (v) -> {
        Activity parent = resultRepeater.getParentActivity();
        Intent intent = HotPointEditor.builder()
                .setPrototype((HotPoint) v.getTag())
                .build(parent);
        parent.startActivityForResult(intent, MainActivity.HOT_POINT_CHANGING_REQUEST_CODE);
    };

    private final View.OnClickListener deleteHotPoint = (v) -> {
        View parentView = (View) v.getTag();
        HotPoint hotPoint = (HotPoint) parentView.getTag();
        tableView.removeView((View) v.getTag());
        hotPointManager.erase(hotPoint);
    };

    private final View.OnClickListener createHotPoint = (v) -> {
        Activity parent = resultRepeater.getParentActivity();
        Intent intent = HotPointEditor.builder().build(parent);
        parent.startActivityForResult(intent, MainActivity.HOT_POINT_CREATING_REQUEST_CODE);
    };

    private final class Loader extends AsyncTask<Void, Void, List<HotPoint>>  {
        @Override
        protected List<HotPoint> doInBackground(Void... voids) {
            return hotPointManager.getHotPoints();
        }

        @Override
        protected void onPostExecute(List<HotPoint> hotPoints) {
            if (getActivity() == null) {
                return;
            }
            tableView.removeAllViews();
            for (HotPoint hotPoint : hotPoints) {
                tableView.addView(createItem(hotPoint));
            }
            tableView.addView(createButton);
        }
    }

}
