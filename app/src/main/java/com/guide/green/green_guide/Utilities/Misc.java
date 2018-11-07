package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


/**
 * Contains miscellaneous useful functions which don't have large enough number to make their own
 * class.
 */
public class Misc {
    /**
     * Hides the virtual keyboard without taking away focus from the view that is using its input.
     *
     * @param view  the view which caused the keyboard to appear
     * @param ctx   the context of the view
     */
    public static void hideKeyboard(@NonNull View view, @NonNull Context ctx) {
        InputMethodManager imm = (InputMethodManager)
                ctx.getSystemService(ctx.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
