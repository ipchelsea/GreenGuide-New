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

public abstract class AutoComplete implements TextWatcher,
        View.OnFocusChangeListener {

    private EditText mText;
    private RecyclerView mDropdown;
    private RecyclerView.LayoutManager mLayoutManager;

    public AutoComplete(Context context, EditText textInput, RecyclerView dropdown) {
        this(context, textInput, dropdown, null);
    }

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

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus && getInputTextLength() != 0) {
            showDropDown();
        } else {
            dismissDropDown();
        }
    }

    public void dismissDropDown() { mDropdown.setVisibility(View.GONE); }

    public void showDropDown() { mDropdown.setVisibility(View.VISIBLE); }

    public boolean isPopupShowing() {
        return mDropdown.getVisibility() == View.VISIBLE;
    }

    @Override
    public void afterTextChanged(Editable e) { /* Do nothing */ }

    @Override
    public void beforeTextChanged(CharSequence c, int i, int i1, int i2) { /* Do nothing */ }
}
