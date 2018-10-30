package com.guide.green.green_guide.Utilities;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.BaiduMapManager.BaiduSuggestion;

import java.util.ArrayList;

public class SuggestionSearchAdapter3 extends BaseAdapter implements Filterable {
    private ArrayList<BaiduSuggestion> mSuggestions;
    private Context mCtx;
    public SuggestionSearchAdapter3(Context ctx, ArrayList<BaiduSuggestion> suggestions) {
        mSuggestions = suggestions;
        mCtx = ctx;
    }

    @Override
    public int getCount() {
        return mSuggestions.size();
    }

    @Override
    public Object getItem(int i) {
        return mSuggestions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(mCtx).inflate(R.layout.suggestion_search_dropdown_item,
                    viewGroup);

            ViewHolder tempViewHolder = new ViewHolder();
            tempViewHolder.icon = view.findViewById(R.id.dropdown_typeIcon);
            tempViewHolder.topText = view.findViewById(R.id.dropdown_topText);
            tempViewHolder.btmText = view.findViewById(R.id.dropdown_bottomText);
            view.setTag(tempViewHolder);
        }

        ViewHolder vh = (ViewHolder) view.getTag();
        return null;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
//                FilterResults result = new FilterResults();
//                result.values = mSuggestions;
//                result.count = mSuggestions.size();
                return null;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                /* Do Nothing */
            }
        };
    }

    private static class ViewHolder {
        public ImageView icon;
        public TextView topText;
        public TextView btmText;
        public void setFirstSearchItem(String name, String address) {
            topText.setText(name);
            icon.setVisibility(View.GONE);
            topText.setVisibility(View.VISIBLE);
            btmText.setVisibility(View.GONE);
        }
        public void setAutoComplete(String name, String address) {
            topText.setText(name);
            icon.setVisibility(View.VISIBLE);
            topText.setVisibility(View.VISIBLE);
            btmText.setVisibility(View.GONE);
        }
        public void setLocation(String name, String address) {
            topText.setText(name);
            btmText.setText(address);
            icon.setVisibility(View.VISIBLE);
            topText.setVisibility(View.VISIBLE);
            btmText.setVisibility(View.VISIBLE);
        }
    }
}