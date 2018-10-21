package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

public class AutoComplete implements TextWatcher, View.OnFocusChangeListener, Filter.FilterListener {
    private EditText mText;
    private RecyclerView mDropdown;
    private FilteredAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Context mContext;

    public static abstract class FilteredAdapter<VH extends RecyclerView.ViewHolder> extends
            RecyclerView.Adapter<VH> implements Filterable {/* Empty */}

    public AutoComplete(Context context, EditText textInput, RecyclerView dropdown,
                        FilteredAdapter adapter) {
        mContext = context;
        mText = textInput;
        mDropdown = dropdown;
        mAdapter = adapter;

        mDropdown.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(context);
        mDropdown.setLayoutManager(mLayoutManager);
        mDropdown.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mText.setOnFocusChangeListener(this);
        mText.addTextChangedListener(this);
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() == 0) {
            hideDropDown();
        } else {
            mAdapter.getFilter().filter(charSequence, this);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus && mText.getText().length() != 0) {
            showDropDown();
        } else {
            hideDropDown();
        }
    }

    @Override
    public void onFilterComplete(int i) {
        if (i == 0) {
            hideDropDown();
        } else {
            showDropDown();
        }
    }

    public void hideDropDown() { mDropdown.setVisibility(View.GONE); }

    public void showDropDown() { mDropdown.setVisibility(View.VISIBLE); }

    @Override
    public void afterTextChanged(Editable e) { /* Do nothing */ }

    @Override
    public void beforeTextChanged(CharSequence c, int i, int i1, int i2) { /* Do nothing */ }
}
