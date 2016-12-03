package ru.spbau.mit.placenotifier.customizers;

import java.util.ArrayList;

@SuppressWarnings("WeakerAccess")
public final class Customizers {

    private Customizers(){}

    public static class OptionsBuilder<T> {
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

    public static <T> OptionsBuilder<T> forOptions(String title) {
        return new OptionsBuilder<>(title);
    }
}
