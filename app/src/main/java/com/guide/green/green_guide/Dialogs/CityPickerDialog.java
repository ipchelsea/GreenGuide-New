package com.guide.green.green_guide.Dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.RomanizedLocation;

import java.util.ArrayList;
import java.util.List;

import static com.guide.green.green_guide.Utilities.Drawing.hideKeyboard;

public class CityPickerDialog extends DialogFragment {
    private AutoCompleteTextView mAutoComplete;

    private Button btnSelectedCity;
    public void setSelectedCityButton(Button btnSelectedCity) {
        this.btnSelectedCity = btnSelectedCity;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
//        this.setCancelable(false);
    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//            Window window = dialog.getWindow();
//            WindowManager.LayoutParams wlp = window.getAttributes();
//            wlp.copyFrom(window.getAttributes());
//            wlp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
//            window.setAttributes(wlp);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_city_picker, container);

        mAutoComplete = (AutoCompleteTextView) layout.findViewById(R.id.searchCity);
        mAutoComplete.setThreshold(1);
        RomanizedAdapter adapter = new RomanizedAdapter(getContext(), RomanizedLocation.getCities(),
                new RomanizedAdapter.OnItemClicked() {
                    @Override
                    public void onClicked(int i, RomanizedAdapter adapter, View v) {
                        RomanizedLocation city = (RomanizedLocation) adapter.getItem(i);
                        btnSelectedCity.setText(city.name);
                        hideKeyboard(mAutoComplete, getContext());
                        dismiss();
                    }
                });
        mAutoComplete.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        return layout;
    }

    public static class RomanizedAdapter extends BaseAdapter implements Filterable {
        public interface OnItemClicked {
            void onClicked(int i, RomanizedAdapter adapter, View v);
        }

        private List<RomanizedLocation> citiesOriginal;
        private List<RomanizedLocation> cities;
        private Context context;
        public OnItemClicked mCallback;

        public RomanizedAdapter(@NonNull Context context, @NonNull List<RomanizedLocation> cities,
                                @NonNull OnItemClicked callback) {
            this.context = context;
            this.cities = cities;
            this.citiesOriginal = cities;
            this.mCallback = callback;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults results = new FilterResults();
                    if (charSequence != null && charSequence.length() > 0) {
                        String query = charSequence.toString().toUpperCase();
                        ArrayList<RomanizedLocation> filteredList = new ArrayList<>();
                        for (RomanizedLocation city : citiesOriginal) {
                            if ((city.pinyin != null && city.pinyin.toUpperCase().startsWith(query))
                                    || (city.name != null && city.name.toUpperCase().startsWith(query))
                                    || (city.fullName != null && city.fullName.toUpperCase().startsWith(query))) {
                                filteredList.add(city);
                            }
                        }
                        results.values = filteredList;
                        results.count = filteredList.size();
                    } else {
                        results.values = citiesOriginal;
                        results.count = citiesOriginal.size();
                    }
                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    cities = (ArrayList<RomanizedLocation>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }

        @Override
        public int getCount() {
            return cities.size();
        }

        @Override
        public Object getItem(int i) {
            return cities.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(context, R.layout.dialog_city_autocomplete_item, null);
                view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (b) {
                            mCallback.onClicked(((ViewHolder) view.getTag()).i,
                                    RomanizedAdapter.this, view);
                        }
                    }
                });
                ViewHolder temp = new ViewHolder((TextView) view.findViewById(R.id.cityName),
                        (TextView) view.findViewById(R.id.cityPinyin));
                temp.mCityPinyin.setClickable(false);
                temp.mCityPinyin.setFocusable(false);
                temp.mCityName.setClickable(false);
                temp.mCityName.setFocusable(false);
                view.setTag(temp);
            }

            ViewHolder v = (ViewHolder) view.getTag();
            v.i = i;
            v.mCityName.setText(cities.get(i).name.toString());
            v.mCityPinyin.setText(cities.get(i).pinyin.toString());

            return view;
        }

        public static class ViewHolder {
            int i;
            TextView mCityName;
            TextView mCityPinyin;
            ViewHolder(TextView cityName, TextView cityPinyin) {
                this.mCityName = cityName;
                this.mCityPinyin = cityPinyin;
            }
        }
    }
}