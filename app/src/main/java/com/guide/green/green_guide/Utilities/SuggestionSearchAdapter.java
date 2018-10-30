package com.guide.green.green_guide.Utilities;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.BaiduMapManager.BaiduSuggestion;
import java.util.ArrayList;

public class SuggestionSearchAdapter extends RecyclerView.Adapter<SuggestionSearchAdapter.SuggestionViewHolder> {
    private ArrayList<BaiduSuggestion> mSuggestions = new ArrayList<>();

    public void replaceAllSuggestions(ArrayList<BaiduSuggestion> suggestions) {
        if (suggestions != null) {
            synchronized (mSuggestions) {
                mSuggestions = suggestions;
            }
            notifyDataSetChanged();
        }
    }

    public void addSuggestions(ArrayList<BaiduSuggestion> suggestions) {
        if (suggestions != null && !suggestions.isEmpty()) {
            synchronized (mSuggestions) {
                mSuggestions.set(0, suggestions.get(0));
                mSuggestions.addAll(1, suggestions);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return mSuggestions.size();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.suggestion_search_dropdown_item, null);
        SuggestionViewHolder result = new SuggestionViewHolder(view);
        result.icon = view.findViewById(R.id.dropdown_typeIcon);
        result.topText = view.findViewById(R.id.dropdown_topText);
        result.btmText = view.findViewById(R.id.dropdown_bottomText);
        return result;
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        BaiduSuggestion suggestion = mSuggestions.get(position);
        if (position == 0) {
            holder.setFirstSearchItem(suggestion.name);
        } else if (suggestion.point == null) {
            holder.setAutoComplete(suggestion.name);
        } else {
            holder.setLocation(suggestion.name, suggestion.address);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        public ImageView icon;
        public TextView topText;
        public TextView btmText;
        public void setFirstSearchItem(String name) {
            topText.setText(name);
            icon.setVisibility(View.INVISIBLE);
            topText.setVisibility(View.VISIBLE);
            btmText.setVisibility(View.GONE);
        }
        public void setAutoComplete(String name) {
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
        public SuggestionViewHolder(View parent) {
            super(parent);
        }
    }
}