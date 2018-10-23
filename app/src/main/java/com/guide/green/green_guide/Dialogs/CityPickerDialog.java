package com.guide.green.green_guide.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.AutoComplete;
import com.guide.green.green_guide.Utilities.RomanizedLocation;
import java.util.ArrayList;
import java.util.List;

import static com.guide.green.green_guide.Utilities.Drawing.hideKeyboard;

public class CityPickerDialog extends DialogFragment implements DialogInterface.OnClickListener {
    private EditText mText;
    private RecyclerView mDropDown;

    private Button btnSelectedCity;
    public void setSelectedCityButton(Button btnSelectedCity) {
        this.btnSelectedCity = btnSelectedCity;
    }

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RomanizedAdapter adapter = new RomanizedAdapter(getContext(), RomanizedLocation.getCities(),
                new RomanizedAdapter.OnItemClicked() {
                    @Override
                    public void onClicked(RomanizedLocation city, View v) {
                        btnSelectedCity.setText(city.name);
                        hideKeyboard(mText, getContext());
                        dismiss();
                    }
                });

        View layout = inflater.inflate(R.layout.dialog_city_picker, container);
        mText = layout.findViewById(R.id.searchCity);
        mDropDown = layout.findViewById(R.id.searchDropDown);

        AutoComplete autoComplete = new AutoComplete(getContext(), mText, mDropDown, adapter);

        return layout;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        /* Do nothing */
    }

    public static class RomanizedAdapter extends
            AutoComplete.FilteredAdapter<RomanizedAdapter.ViewHolder> {
        public interface OnItemClicked {
            void onClicked(RomanizedLocation city, View v);
        }

        private RomanizedFilter mFilter;
        private List<RomanizedLocation> mCitiesOriginal;
        private List<RomanizedLocation> mCities;
        private Context context;
        public OnItemClicked mCallback;

        public RomanizedAdapter(@NonNull Context context, @NonNull List<RomanizedLocation> cities,
                                @NonNull OnItemClicked callback) {
            this.context = context;
            this.mCitiesOriginal = cities;
            this.mCities = mCitiesOriginal;
            this.mCallback = callback;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewGroup view = (ViewGroup) View.inflate(context,
                    R.layout.dialog_city_autocomplete_item, null);

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
                    if (hasFocus) {
                        mCallback.onClicked(mCities.get(vh.i), view);
                    }
                }
            });

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.i = position;
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

        public static class ViewHolder extends RecyclerView.ViewHolder {
            public int i;
            public TextView cityName;
            public TextView cityPinyin;
            public ViewHolder(ViewGroup parent, TextView cityName, TextView cityPinyin) {
                super(parent);
                this.cityName = cityName;
                this.cityPinyin = cityPinyin;
            }
        }

    }

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