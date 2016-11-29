package ru.spbau.mit.placenotifier.customizers;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

@SuppressWarnings("WeakerAccess")
public interface CustomizeEngine<T> {

    String ON_NULL_OBSERVED_VIEW_EXCEPTION_MESSAGE =
            "Wrong view state for that operation: "
                    + "there is no observed view";

    String ON_WRONG_SAVED_STATE_FORMAT_EXCEPTION_MESSAGE =
            "Wrong view state for that operation: "
                    + "saved state not suitable for this customize engine";

    String ON_NOT_READY_STATE_EXCEPTION_MESSAGE =
            "Wrong view state for that operation: "
                    + "not all view's field are filled in";

    /**
     * Define type of view, which can be observed by this CustomizeEngine.
     *
     * @return id of XML layout that should be used on view creating
     *         which will be observed by this CustomizeEngine
     */
    @LayoutRes
    int expectedViewLayout();

    /**
     * Set view that be used as source of information
     * which will be used to create instance of T.
     *
     * @param view The view which will be observed
     */
    void observe(@NonNull View view);

    /**
     * @return if observed view is in right state to create instance of T.
     *         false if there is no observed view
     */
    boolean isReady();

    /**
     * Define type of view, which can be observed by this CustomizeEngine.
     *
     * @return instance of T which is defined now in observed view
     * @exception WrongStateException if observed view is absent or isn't in
     *         right state to create object. Use {@link #isReady}
     */
    @NonNull
    T getValue();

    /**
     * Try to change observed view so that {@link #getValue} will generate
     * object equal to value. Do nothing if there is no observed view
     *
     * @return true in case of success
     */
    boolean setValue(@NonNull T value);

    /**
     * Restore state after screen rotation
     */
    void restoreState(@NonNull Bundle state);

    /**
     * Save state before screen rotation
     */
    @NonNull
    Bundle saveState();

    class WrongStateException extends RuntimeException {
        WrongStateException(String message) {
            super(message);
        }
    }
}
