package ru.spbau.mit.placenotifier;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.spbau.mit.placenotifier.SmartListAdapter.ViewHolder;
import ru.spbau.mit.placenotifier.predicates.SerializablePredicate;

public class HotPointsListFragment extends Fragment {

    private ViewGroup tableView;
    private ResultRepeater resultRepeater;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View result = inflater.inflate(R.layout.fragment_hot_points_list, container, false);
        tableView = (ViewGroup) result.findViewById(R.id.hot_points_list_table);
        resultRepeater = (ResultRepeater) getActivity();
        new Loader().execute();
        return result;
    }

    private View createItem(HotPoint hotPoint) {
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
        view.getBackground().setColorFilter(hotPoint.getColor(), PorterDuff.Mode.MULTIPLY);

        TextView nameView = (TextView) view.findViewById(R.id.item_name);
        nameView.setText(hotPoint.getName());
        nameView.setTag(hotPoint);
    }

    private final View.OnClickListener editHotPoint = (v) -> {
        Intent intent = HotPointEditor.builder()
                .setPrototype((HotPoint) v.getTag())
                .build(getActivity());
        resultRepeater.getParentActivity()
                .startActivityForResult(intent, MainActivity.HOT_POINT_CHANGING_REQUEST_CODE);
    };

    private final View.OnClickListener deleteHotPoint = (v) -> {
        // todo remove from database
        tableView.removeView((View) v.getTag());
    };

    private static final HotPoint[] hp = {new HotPoint("spb", new LatLng(30, 60), Color.RED, 15),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.YELLOW, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.YELLOW, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.YELLOW, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.YELLOW, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.YELLOW, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.YELLOW, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.BLUE, 14),
            new HotPoint("msc", new LatLng(40, 59), Color.YELLOW, 14)};

    private final class Loader extends AsyncTask<Void, Void, List<HotPoint>>  {
        @Override
        protected List<HotPoint> doInBackground(Void... voids) {
            List<HotPoint> list = new ArrayList<>();
            for (HotPoint point: hp) {
                list.add(point);
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HotPoint> hotPoints) {
            for (HotPoint hotPoint : hotPoints) {
                tableView.addView(createItem(hotPoint));
            }
        }
    }

}
