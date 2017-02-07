package ru.spbau.mit.placenotifier;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.android.internal.util.Predicate;

import java.util.Comparator;
import java.util.List;

class SmartListAdapter<T> extends ArrayAdapter<T> {


    private final Predicate<T> DEFAULT_FILTER = x -> true;
    private final Comparator<T> DEFAULT_COMPARATOR = (x, y) -> x.hashCode() - y.hashCode();

    private final DataLoader<List<T>> loader;
    private final Creator<ViewHolder<T>> holdersCreator;
    private Comparator<T> comparator = DEFAULT_COMPARATOR;
    private Predicate<T> filter = DEFAULT_FILTER;

    SmartListAdapter(@NonNull DataLoader<List<T>> loader,
                     @NonNull Creator<ViewHolder<T>> holdersCreator, @NonNull Context context) {
        super(context, R.layout.alarms_list_item);
        this.loader = loader;
        this.holdersCreator = holdersCreator;
        refresh();
    }

    void setComparator(@Nullable Comparator<T> comparator) {
        this.comparator = (comparator == null? DEFAULT_COMPARATOR : comparator);
        resort();
    }

    @NonNull
    Predicate<T> getCurrentFilter() {
        return filter;
    }

    void setFilter(@Nullable Predicate<T> filter) {
        this.filter = (filter == null? DEFAULT_FILTER : filter);
        refresh();
    }

    void resort() {
        sort(comparator);
    }

    void refresh() {
        new AsyncLoader().execute();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        T item = getItem(position);
        ViewHolder<T> holder;
        if (convertView == null) {
            holder = holdersCreator.create();
            holder.getView().setTag(holder);
        } else {
            //noinspection unchecked
            holder = (ViewHolder<T>) convertView.getTag();
        }
        holder.setItem(item);
        return holder.getView();
    }

    interface DataLoader<T> {
        T load();
    }

    interface Creator<T> {
        T create();
    }

    interface ViewHolder<T> {
        void setItem(T item);

        View getView();
    }

    private class AsyncLoader extends AsyncTask<Void, Void, List<T>> {

        @Override
        protected List<T> doInBackground(Void... params) {
            try {
                return loader.load();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(@Nullable List<T> items) {
            clear();
            if (items == null) {
                Log.e("SLA", "Loading failed");
            } else {
                //noinspection Convert2streamapi (No Stream API at ours API level)
                for (T item : items) {
                    if (filter == null || filter.apply(item)) {
                        add(item);
                    }
                }
                if (comparator != null) {
                    sort(comparator);
                }
            }
        }
    }
}
