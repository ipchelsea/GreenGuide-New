package com.guide.green.green_guide.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.FilteredAutoComplete;
import com.guide.green.green_guide.Utilities.RomanizedLocation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages a dialog box which allows the user to pick a city from an auto-complete list.
 * Uses {@code RomanizedLocation.getCities()} to acquire its list of cities and
 * {@code FilteredAutoComplete} for filtering the cities base on the user input.
 */
public class CityPickerDialog extends DialogFragment implements DialogInterface.OnClickListener {
    // The callback
    private OnCitySelectedListener mOnCitySelectedListener;

    /**
     * Callback interface for responding to one off the cities in the dropdown being selected.
     */
    public interface OnCitySelectedListener {

        /**
         * Called when a city is selected from the drop down.
         *
         * @param city The city that was selected.
         */
        void onCitySelected(RomanizedLocation city);
    }

    public void setOnCitySelectedListener(@NonNull OnCitySelectedListener listener) {
        mOnCitySelectedListener = listener;
    }

    /** Sets the gravity so that the dialog is in the top middle of the screen. */
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.copyFrom(window.getAttributes());
            wlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            window.setAttributes(wlp);
        }
    }

    /**
     * Called by the adapter managed the drop down when on of its items is selected
     */
    private final OnCitySelectedListener onClickCallback = new OnCitySelectedListener() {
        /**
         * Called when a city is selected from the drop down.
         *
         * @param city The city that was selected.
         */
        @Override
        public void onCitySelected(RomanizedLocation city) {
            if (mOnCitySelectedListener != null) {
                mOnCitySelectedListener.onCitySelected(city);
            }
            dismiss();
        }
    };

    /** Instantiates the adapter which manages the dropdown items */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstState) {
        View layout = inflater.inflate(R.layout.dialog_city_picker, container);
        EditText inputText = layout.findViewById(R.id.searchCity);
        RecyclerView dropDown = layout.findViewById(R.id.searchDropDown);

        new FilteredAutoComplete(getContext(), inputText, dropDown,
                new RomanizedAdapter(Objects.requireNonNull(getContext()),
                        RomanizedLocation.getCities(), onClickCallback));

        return layout;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        /* Do nothing */
    }

    /**
     * Manages the items in a dropdown. Creates the items, sets their values, and alerts a
     * listener to the clicking of one of the items.
     */
    public static class RomanizedAdapter extends
            FilteredAutoComplete.FilteredAdapter<RomanizedAdapter.ViewHolder> {
        private RomanizedFilter mFilter;
        private List<RomanizedLocation> mCitiesOriginal;    // The full list of cities
        private List<RomanizedLocation> mCities;            // The filtered cities
        private Context context;
        public OnCitySelectedListener mCitySelectedListener;

        public RomanizedAdapter(@NonNull Context context, @NonNull List<RomanizedLocation> cities,
                                @Nullable OnCitySelectedListener listener) {
            this.context = context;
            this.mCitiesOriginal = cities;
            this.mCities = mCitiesOriginal;
            this.mCitySelectedListener = listener;
        }

        /** Creates a new layout and adds it to a viewholder instance. */
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewGroup view = (ViewGroup) View.inflate(context,
                    R.layout.dialog_city_autocomplete_item, null);
            view.setFocusable(true);

            final RomanizedAdapter.ViewHolder vh = new RomanizedAdapter.ViewHolder(
                    (ViewGroup) view.getRootView(),
                    (TextView) view.findViewById(R.id.cityName),
                    (TextView) view.findViewById(R.id.cityPinyin));
            vh.cityPinyin.setClickable(false);
            vh.cityPinyin.setFocusable(false);
            vh.cityName.setClickable(false);
            vh.cityName.setFocusable(false);

            view.getRootView().setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus && mCitySelectedListener != null) {
                        mCitySelectedListener.onCitySelected(mCities.get(vh.position));
                    }
                }
            });

            return vh;
        }

        /** Sets the data for the item at the specified {@code position} */
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.position = position;
            holder.cityName.setText(mCities.get(position).name);
            holder.cityPinyin.setText(mCities.get(position).pinyin);
        }

        @Override
        public int getItemCount() {
            return mCities.size();
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new RomanizedFilter(mCities) {
                    @Override
                    protected void publishResults(CharSequence charSequence,
                                                  FilterResults filterResults) {
                        mCities = (ArrayList<RomanizedLocation>) filterResults.values;
                        notifyDataSetChanged();
                    }
                };
            }
            return mFilter;
        }

        /**
         * Stores views so that calls to {@code findView()} only need to be done once.
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            public int position;
            public TextView cityName;
            public TextView cityPinyin;
            public ViewHolder(ViewGroup parent, TextView cityName, TextView cityPinyin) {
                super(parent);
                this.cityName = cityName;
                this.cityPinyin = cityPinyin;
            }
        }

    }

    /**
     * Asynchronously looks finds cities that match inputted auto-complete text.
     */
    public static abstract class RomanizedFilter extends Filter {
        private List<RomanizedLocation> mCities;

        public RomanizedFilter(List<RomanizedLocation> cities) {
            mCities = cities;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if (charSequence != null && charSequence.length() > 0) {
                String query = charSequence.toString().toUpperCase();
                ArrayList<RomanizedLocation> filteredList = new ArrayList<>();
                for (RomanizedLocation city : mCities) {
                    if ((city.pinyin != null && city.pinyin.toUpperCase().startsWith(query))
                            || (city.name != null && city.name.toUpperCase().startsWith(query))
                            || (city.fullName != null && city.fullName.toUpperCase().startsWith(query))) {
                        filteredList.add(city);
                    }
                }
                results.values = filteredList;
                results.count = filteredList.size();
            } else {
                results.values = mCities;
                results.count = mCities.size();
            }
            return results;
        }
    }
}