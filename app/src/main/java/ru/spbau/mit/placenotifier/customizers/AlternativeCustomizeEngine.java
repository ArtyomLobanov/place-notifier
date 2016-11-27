package ru.spbau.mit.placenotifier.customizers;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import ru.spbau.mit.placenotifier.R;

/**
 * Use ViewPager to allow user to choose CustomizeEngine which he/she wants
 */
public class AlternativeCustomizeEngine<T> implements CustomizeEngine<T> {

    private static final String CHILDREN_STATES_ARRAY_KEY = "children_states_array_key";
    private static final String ACTIVE_PAGE_NUMBER_KEY = "active_page_number_key";

    private final List<CustomizeEngine<T>> customizers;
    private final String title;

    private ViewPager viewPager = null;
    private Bundle[] savedStates;

    @SafeVarargs
    public AlternativeCustomizeEngine(String title, CustomizeEngine<T>... customizers) {
        this.customizers = Arrays.asList(customizers);
        this.title = title;
        savedStates = new Bundle[customizers.length];
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_bar;
    }

    @Override
    public void observe(@Nullable View view) {
        Arrays.fill(savedStates, null);
        if (view == null) {
            viewPager = null;
            return;
        }
        TextView titleView = (TextView) view.findViewById(R.id.customize_bar_title);
        viewPager = (ViewPager) view.findViewById(R.id.customize_bar_view_pager);

        if (titleView == null) {
            throw new IllegalArgumentException("Wrong view layout: customize_bar_title not found");
        }
        if (viewPager == null) {
            throw new IllegalArgumentException("Wrong view layout: customize_bar_view_pager not found");
        }

        titleView.setText(title);
        viewPager.setAdapter(new BarAdapter());
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
        if (viewPager == null) {
            throw new WrongStateException(CustomizeEngine.ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE);
        }
        int activePage = viewPager.getCurrentItem();
        return customizers.get(activePage).getValue();
    }

    /**
     * Tries to find CustomizeEngine, which can accept that value
     * Sets found CustomizeEngine as current
     */
    @Override
    public boolean setValue(@Nullable T value) {
        if (viewPager == null) {
            return false;
        }
        for (CustomizeEngine<T> customizer : customizers) {
            if (customizer.setValue(value)) {
                // don't want to use plain for-cycle here
                int index = customizers.indexOf(customizer);
                savedStates[index] = null;
                viewPager.setCurrentItem(index);
                return true;
            }
        }
        return false;
    }

    @Override
    public void restoreState(Bundle state) {
        if (viewPager == null) {
            throw new WrongStateException(CustomizeEngine.ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE);
        }
        if (state == null) {
            return;
        }
        Parcelable[] childrenState = state.getParcelableArray(CHILDREN_STATES_ARRAY_KEY);
        Integer activePageNumber = state.getInt(ACTIVE_PAGE_NUMBER_KEY, -1);
        if (childrenState == null || childrenState.length != savedStates.length
                || !(0 <= activePageNumber && activePageNumber < childrenState.length)) {
            throw new WrongStateException(ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE);
        }
        for (int i = 0; i < savedStates.length; i++) {
            savedStates[i] = (Bundle) childrenState[i];
        }
        viewPager.setCurrentItem(activePageNumber);
        viewPager.getAdapter().notifyDataSetChanged();
    }

    @Nullable
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

    private void validateItem(int position) {
        if (savedStates[position] == null) {
            return;
        }
        customizers.get(position).restoreState(savedStates[position]);
        savedStates[position] = null;
    }

    private class BarAdapter extends PagerAdapter {

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            View view = View.inflate(collection.getContext(),
                    customizers.get(position).expectedViewLayout(), null);
            customizers.get(position).observe(view);
            validateItem(position);
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
