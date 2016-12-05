package ru.spbau.mit.placenotifier.customizers;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.spbau.mit.placenotifier.R;

/**
 * Use ViewPager to allow user to choose CustomizeEngine which he/she wants
 */

@SuppressWarnings("WeakerAccess")
public class AlternativeCustomizeEngine<T> implements CustomizeEngine<T> {

    private static final String CHILDREN_STATES_ARRAY_KEY = "children_states_array_key";
    private static final String ACTIVE_PAGE_NUMBER_KEY = "active_page_number_key";

    private final List<CustomizeEngine<T>> customizers;
    private final String title;

    private ViewPager viewPager;
    private int currentPageCache;

    @SafeVarargs
    public AlternativeCustomizeEngine(@NonNull String title, CustomizeEngine<T>... customizers) {
        this.customizers = Arrays.asList(customizers);
        this.title = title;
    }

    public AlternativeCustomizeEngine(@NonNull String title,
                                      @NonNull Collection<CustomizeEngine<T>> customizers) {
        List<CustomizeEngine<T>> copy = new ArrayList<>();
        copy.addAll(customizers);
        this.customizers = Collections.unmodifiableList(copy);
        this.title = title;
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_engine_alternative;
    }

    @Override
    public void observe(@NonNull View view) {
        cacheCurrentPageNumber();
        TextView titleView = (TextView) view.findViewById(R.id.customize_alternative_title);
        viewPager = (ViewPager) view.findViewById(R.id.customize_alternative_view_pager);
        if (titleView == null || viewPager == null) {
            throw new IllegalArgumentException(ON_BAD_VIEW_LAYOUT);
        }
        titleView.setText(title);
        viewPager.setAdapter(new BarAdapter());
        unloadCachedPageNumber();

    }

    @Override
    public boolean isReady() {
        if (viewPager == null) {
            return false;
        }
        int activePage = viewPager.getCurrentItem();
        return customizers.get(activePage).isReady();
    }

    @NonNull
    @Override
    public T getValue() {
        cacheCurrentPageNumber();
        return customizers.get(currentPageCache).getValue();
    }

    /**
     * Tries to find CustomizeEngine, which can accept that value
     * Sets found CustomizeEngine as current
     */
    @Override
    public boolean setValue(@NonNull T value) {
        for (CustomizeEngine<T> customizer : customizers) {
            if (customizer.setValue(value)) {
                // don't want to use plain for-cycle here
                currentPageCache = customizers.indexOf(customizer);
                unloadCachedPageNumber();
                return true;
            }
        }
        return false;
    }

    @Override
    public void restoreState(@NonNull Bundle state) {
        Parcelable[] childrenState = state.getParcelableArray(CHILDREN_STATES_ARRAY_KEY);
        currentPageCache = state.getInt(ACTIVE_PAGE_NUMBER_KEY, -1);
        if (childrenState == null || childrenState.length != customizers.size()
                || !(0 <= currentPageCache && currentPageCache < childrenState.length)) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        for (int i = 0; i < childrenState.length; i++) {
            customizers.get(i).restoreState((Bundle) childrenState[i]);
        }
        unloadCachedPageNumber();
        if (viewPager != null) {
            viewPager.getAdapter().notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public Bundle saveState() {
        Bundle state = new Bundle();
        Bundle[] childrenState = new Bundle[customizers.size()];
        for (int i = 0; i < childrenState.length; i++) {
            childrenState[i] = customizers.get(i).saveState();
        }
        state.putParcelableArray(CHILDREN_STATES_ARRAY_KEY, childrenState);
        state.putInt(ACTIVE_PAGE_NUMBER_KEY, viewPager.getCurrentItem());
        return state;
    }

    private void cacheCurrentPageNumber() {
        if (viewPager != null) {
            currentPageCache = viewPager.getCurrentItem();
        }
    }

    private void unloadCachedPageNumber() {
        if (viewPager != null) {
            viewPager.setCurrentItem(currentPageCache);
        }
    }

    private class BarAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View view = View.inflate(collection.getContext(),
                    customizers.get(position).expectedViewLayout(), null);
            customizers.get(position).observe(view);
            collection.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return customizers.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
