package ru.spbau.mit.placenotifier.customizers;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import ru.spbau.mit.placenotifier.R;

/**
 * Use ViewPager to allow user to choose CustomizeEngine which he/she wants
 */
public class AlternativeCustomizeEngine<T> implements CustomizeEngine<T> {

    private final ArrayList<CustomizeEngine<T>> customizers;
    private final String title;

    private ViewPager viewPager = null;

    @SafeVarargs
    public AlternativeCustomizeEngine(String title, CustomizeEngine<T>... customizers) {
        this.customizers = new ArrayList<>(Arrays.asList(customizers));
        this.title = title;
    }

    @Override
    public int expectedViewLayout() {
        return R.layout.customize_bar;
    }

    @Override
    public void observe(@Nullable View view) {
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
                viewPager.setCurrentItem(index);
                return true;
            }
        }
        return false;
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
