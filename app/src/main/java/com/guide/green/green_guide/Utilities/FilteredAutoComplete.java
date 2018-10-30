package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;

public class FilteredAutoComplete extends AutoComplete implements Filter.FilterListener {
    private FilteredAdapter mFilteredAdapter;

    public static abstract class FilteredAdapter<VH extends RecyclerView.ViewHolder> extends
            RecyclerView.Adapter<VH> implements Filterable {/* Empty */}

    public FilteredAutoComplete(Context context, EditText textInput, RecyclerView dropdown,
                                FilteredAdapter adapter) {
        super(context, textInput, dropdown, adapter);
        mFilteredAdapter = adapter;
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() == 0) {
            dismissDropDown();
        } else {
            mFilteredAdapter.getFilter().filter(charSequence, this);
        }
    }

    @Override
    public void onFilterComplete(int i) {
        if (i == 0) {
            dismissDropDown();
        } else {
            showDropDown();
        }
    }
}
