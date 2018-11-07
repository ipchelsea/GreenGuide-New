package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

/**
 * Autocomplete base-class which manages hiding the dropdown when no text is present.
 * Leaves implementation of what is shown in the dropdown to the supplied adapter and handling
 * user input to the un-implement {@code onTextChanged} method.
 */
public abstract class AutoComplete implements TextWatcher, View.OnFocusChangeListener {
    private EditText mText;
    private RecyclerView mDropdown;
    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * Constructor which sets all of the values but sets the dropdown to null.
     *
     * @param context Context used by the {@code LinearLayoutManager}.
     * @param textInput The view which the user will input text into.
     * @param dropdown The list items which will be updates as more text is entered into the
     *                  {@code textInput}.
     */
    public AutoComplete(@NonNull Context context, @NonNull EditText textInput,
                        @NonNull RecyclerView dropdown) {
        this(context, textInput, dropdown, null);
    }

    /**
     * Constructor which sets all values supplied by the user.
     *
     * @param context Context used by the {@code LinearLayoutManager}.
     * @param textInput The view which the user will input text into.
     * @param dropdown The list items which will be updates as more text is entered into the
     *                  {@code textInput}.
     * @param adapter The adapter which creates the views.
     */
    public AutoComplete(@NonNull Context context, @NonNull EditText textInput,
                        @NonNull RecyclerView dropdown, @Nullable RecyclerView.Adapter adapter) {
        mText = textInput;
        mDropdown = dropdown;

        mDropdown.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mDropdown.setLayoutManager(mLayoutManager);

        if (adapter != null) {
            mDropdown.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        mText.setOnFocusChangeListener(this);
        mText.addTextChangedListener(this);
    }

    public int getInputTextLength() {
        return mText.getText().length();
    }

    /** Hides the dropdown when there the auto-complete is navigated away from. */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus && getInputTextLength() != 0) {
            showDropDown();
        } else {
            dismissDropDown();
        }
    }

    /** Hides the dropdown */
    public void dismissDropDown() { mDropdown.setVisibility(View.GONE); }

    /** Shows the dropdown */
    public void showDropDown() { mDropdown.setVisibility(View.VISIBLE); }

    /** @return true is the dropdown is visible, else false */
    public boolean isPopupShowing() { return mDropdown.getVisibility() == View.VISIBLE; }

    @Override
    public void afterTextChanged(Editable e) { /* Do nothing */ }

    @Override
    public void beforeTextChanged(CharSequence c, int i, int i1, int i2) { /* Do nothing */ }
}
