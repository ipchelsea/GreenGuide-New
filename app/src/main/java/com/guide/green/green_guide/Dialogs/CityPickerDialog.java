package com.guide.green.green_guide.Dialogs;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.guide.green.green_guide.Utilities.RomanizedLocation;

import com.guide.green.green_guide.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CityPickerDialog extends DialogFragment {
    private AutoCompleteTextView mAutoComplete;

    public static class GroupIndex implements Comparable<GroupIndex> {
        public final Character name;
        public final int startIndex;
        public GroupIndex(Character name, int startIndex) {
            this.name = name;
            this.startIndex = startIndex;
        }
        @Override
        public int compareTo(@NonNull GroupIndex groupIndex) {
            return name - groupIndex.name;
        }
    }

    public static class RomanizedAdapter extends RecyclerView.Adapter<RomanizedAdapter.MyViewHolder> {
        private LayoutInflater inflater;
        private Context context;
        private List<RomanizedLocation> items;
        private List<GroupIndex> headers;
        private View.OnClickListener clickListener;

        public RomanizedAdapter(@NonNull Context context, @NonNull List<RomanizedLocation> items,
            @NonNull List<GroupIndex> headers, @NonNull View.OnClickListener clickListener) {
            inflater = LayoutInflater.from(context);
            this.context = context;
            this.items = items;
            this.headers = headers;
            this.clickListener = clickListener;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = inflater.inflate(R.layout.dialog_city_picker_list_item, parent, false);
            MyViewHolder holder = new MyViewHolder(v);
            v.setOnClickListener(clickListener);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.name.setText(items.get(position).name);
        }

        @Override
        public int getItemCount() {
            return items.size() + 1;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            public Button name;
            public MyViewHolder(View itemView) {
                super(itemView);
                name = (Button) itemView;
            }
        }
    }

    private Button btnSelectedCity;
    public void setSelectedCityButton(Button btnSelectedCity) {
        this.btnSelectedCity = btnSelectedCity;
    }

    /***
     * List must be sorted.
     *
     * @param list sorted collection of cities
     * @return a pair containing the group id
     */
    private static List<GroupIndex> getGroupStartIndexes(List<RomanizedLocation> list) {
        HashMap<Character, Integer> firstIndexOfGroup = new LinkedHashMap<>();
        for (int i = 0; i < list.size(); i++) {
            char key = Character.toUpperCase(list.get(i).pinyin.charAt(0));
            if (!firstIndexOfGroup.containsKey(key)) {
                firstIndexOfGroup.put(key, i);
            }
        }

        ArrayList<GroupIndex> results = new ArrayList<>(firstIndexOfGroup.size());
        for (Map.Entry<Character, Integer> item : firstIndexOfGroup.entrySet()) {
            results.add(new GroupIndex(item.getKey(), item.getValue()));
        }

        Collections.sort(results);

        return results;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_city_picker, container);

        List<RomanizedLocation> cities = RomanizedLocation.getCities();
        List<GroupIndex> groupStartIndexs = getGroupStartIndexes(cities);

//        mAutoComplete = (AutoCompleteTextView) layout.findViewById(R.id.searchCity);
//        mAutoComplete.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View view, int i, KeyEvent keyEvent) {
//                return false;
//            }
//        });


        RecyclerView rv = (RecyclerView) layout.findViewById(R.id.cityList2);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        RomanizedAdapter adapter = new RomanizedAdapter(getContext(), cities, groupStartIndexs,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Button btn = (Button) view;
                        btnSelectedCity.setText(((Button) view).getText());
                        CityPickerDialog.this.dismiss();
                    }
                });
        rv.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog d = super.onCreateDialog(savedInstanceState);
        d.requestWindowFeature(DialogInterface.BUTTON_POSITIVE | DialogInterface.BUTTON_NEGATIVE);
        return d;
    }

//        public void show(FragmentManager fragManager) {
//            FragmentTransaction transaction = fragManager.beginTransaction();
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            transaction.add(0,this, null)
//                    .addToBackStack(null).commit();
//        }
}