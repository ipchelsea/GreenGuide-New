package com.guide.green.green_guide.Utilities;


import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.guide.green.green_guide.R;
import java.util.ArrayList;

public class SuggestionSearchAdapter
        extends RecyclerView.Adapter<SuggestionSearchAdapter.SuggestionViewHolder> {
    private ArrayList<BaiduSuggestion> mSuggestions;
    private OnItemClickListener itemClickListener;
    private ItemIcons mItemIcons;
    public static class ItemIcons {
        public Drawable FIRST_ITEM;
        public Drawable AUTO_COMPLETE_TEXT;
        public Drawable LOCATION;
    }

    public SuggestionSearchAdapter(ArrayList<BaiduSuggestion> suggestions, ItemIcons itemIcons) {
        mSuggestions = suggestions;
        mItemIcons = itemIcons;
    }

    public interface OnItemClickListener {
        void onItemClick(int index);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mSuggestions.size();
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.suggestion_search_dropdown_item, parent, false);

        ViewGroup.LayoutParams lp = view.getLayoutParams();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(lp);

        SuggestionViewHolder result = new SuggestionViewHolder(view);
        result.icon = view.findViewById(R.id.dropdown_typeIcon);
        result.topText = view.findViewById(R.id.dropdown_topText);
        result.btmText = view.findViewById(R.id.dropdown_bottomText);
        return result;
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, final int position) {
        final BaiduSuggestion suggestion = mSuggestions.get(position);
        holder.position = position;
        if (position == 0) {
            holder.setFirstSearchItem(((BaiduSuggestion.TextSuggestion) suggestion).suggestion);
        } else if (suggestion.getType() == BaiduSuggestion.Type.TEXT_SUGGESTION) {
            holder.setAutoComplete(((BaiduSuggestion.TextSuggestion) suggestion).suggestion);
        } else {
            BaiduSuggestion.Location locationSuggestion = (BaiduSuggestion.Location) suggestion;
            holder.setLocation(locationSuggestion.name, locationSuggestion.uid);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public class SuggestionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView icon;
        public TextView topText;
        public TextView btmText;
        public ViewGroup parent;
        public int position;
        public void setFirstSearchItem(String name) {
            icon.setImageDrawable(mItemIcons.FIRST_ITEM);
            topText.setText(name);
            icon.setVisibility(View.VISIBLE);
            topText.setVisibility(View.VISIBLE);
            btmText.setVisibility(View.GONE);
        }
        public void setAutoComplete(String name) {
            icon.setImageDrawable(mItemIcons.AUTO_COMPLETE_TEXT);
            topText.setText(name);
            icon.setVisibility(View.VISIBLE);
            topText.setVisibility(View.VISIBLE);
            btmText.setVisibility(View.GONE);
        }
        public void setLocation(String name, String uid) {
            icon.setImageDrawable(mItemIcons.LOCATION);
            topText.setText(name);
            btmText.setText(uid);
            icon.setVisibility(View.VISIBLE);
            topText.setVisibility(View.VISIBLE);
            btmText.setVisibility(View.VISIBLE);
        }
        private void recursiveSetListener(View view) {
            if (view instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) view;
                int childrenCount = parent.getChildCount();
                for (int i = 0; i < childrenCount; i++) {
                    recursiveSetListener(parent.getChildAt(i));
                }
            }
            view.setFocusable(false);
            view.setClickable(true);
            view.setOnClickListener(this);
        }
        public SuggestionViewHolder(ViewGroup parent) {
            super(parent);
            this.parent = parent;
            recursiveSetListener(parent);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(position);
            }
        }
    }
}