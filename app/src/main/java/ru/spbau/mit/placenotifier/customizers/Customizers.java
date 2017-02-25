package ru.spbau.mit.placenotifier.customizers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import ru.spbau.mit.placenotifier.R;
import ru.spbau.mit.placenotifier.ResultRepeater;
import ru.spbau.mit.placenotifier.predicates.Beacon;

final class Customizers {

    private Customizers() {
    }

    /**
     * create and initialized OptionsBuilder
     *
     * @param title Will be shown on top of observed view
     * @return initialized builder
     */
    @NonNull
    static <T> OptionsBuilder<T> forOptions(@NonNull String title) {
        return new OptionsBuilder<>(title);
    }

    /**
     * Help you to do "atomic" changes of some customize engine.
     * This means, that at the end of execution your customize engine
     * will be correctly changed or will be restored to the original state.
     *
     * @param engine Engine, that will be changed
     * @param task   Some instructions which change engine
     * @return true if engine was changed correctly
     */
    static boolean safeExecution(@NonNull CustomizeEngine<?> engine,
                                 @NonNull UnsafeTask task) {
        Bundle state = engine.saveState();
        boolean success = task.executeTask();
        if (!success) {
            engine.restoreState(state);
        }
        return success;
    }

    @NonNull
    static CustomizeEngine<Beacon> createCombinedBeaconCustomizeEngine(
            @NonNull ResultRepeater producer, int id) {
        Context context = producer.getParentActivity();
        String title = context.getString(R.string.combined_beacon_piker_customize_engine);
        return new AlternativeCustomizeEngine<>(title,
                new AddressPickerCustomizeEngine(context),
                new HotPointPickerCustomizeEngine(context),
                new PlacePickerCustomizeEngine(producer, id));
    }

    interface UnsafeTask {

        /**
         * Some instructions, which can fail.
         *
         * @return true in case os success
         */
        boolean executeTask();
    }

    /**
     * Allow to create CustomizeEngine which let user
     * choose something from a range of options
     */
    // It's not possible to make this class private, because
    // it's methods should me called from other places in that package
    @SuppressWarnings("WeakerAccess")
    static final class OptionsBuilder<T> {
        private final String title;
        private final ArrayList<CustomizeEngine<T>> options;

        private OptionsBuilder(@NonNull String title) {
            this.title = title;
            options = new ArrayList<>();
        }

        OptionsBuilder<T> addOption(@NonNull String massage, @NonNull T value) {
            options.add(new ConstantCustomizeEngine<>(massage, value));
            return this;
        }

        OptionsBuilder<T> addOption(@NonNull String massage, int color, @NonNull T value) {
            options.add(new ConstantCustomizeEngine<>(massage, color, value));
            return this;
        }
 
        @NonNull
        public CustomizeEngine<T> build() {
            return new AlternativeCustomizeEngine<>(title, options);
        }
    }
}
