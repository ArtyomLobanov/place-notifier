package ru.spbau.mit.placenotifier.customizers;

import android.os.Bundle;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public final class Customizers {

    private Customizers() {
    }

    /**
     * create and initialized OptionsBuilder
     *
     * @param title Will be shown on top of observed view
     * @return initialized builder
     */
    public static <T> OptionsBuilder<T> forOptions(String title) {
        return new OptionsBuilder<>(title);
    }

    /**
    * Allow to create CustomizeEngine which let user
    * choose something from a range of options
    */
    public static final class OptionsBuilder<T> {
        private final String title;
        private final ArrayList<CustomizeEngine<T>> options;

        private OptionsBuilder(String title) {
            this.title = title;
            options = new ArrayList<>();
        }

        public OptionsBuilder<T> addOption(String massage, T value) {
            options.add(new ConstantCustomizeEngine<>(massage, value));
            return this;
        }

        public CustomizeEngine<T> build() {
            return new AlternativeCustomizeEngine<>(title, options);
        }
    }

    /**
     * Help you to do "atomic" changes of some customize engine.
     * This means, that at the end of execution your customize engine
     * will be correctly changed or will be restored to the original state.
     *
     * @param engine Engine, that will be changed
     * @param task Some instructions which change engine
     * @return true if engine was changed correctly
     */
    public static boolean safeExecution(CustomizeEngine<?> engine, UnsafeTask task) {
        Bundle state = engine.saveState();
        boolean success = task.executeTask();
        if (!success) {
            engine.restoreState(state);
        }
        return success;
    }

    public interface UnsafeTask {

        /**
         * Some instructions, which can fail.
         *
         * @return true in case os success
         */
        boolean executeTask();
    }
}
