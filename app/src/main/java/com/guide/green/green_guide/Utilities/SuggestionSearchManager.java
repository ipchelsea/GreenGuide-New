package com.guide.green.green_guide.Utilities;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.guide.green.green_guide.Utilities.BaiduMapManager.BaiduSuggestion;

import java.util.ArrayList;

public class SuggestionSearchManager implements View.OnKeyListener, AdapterView.OnItemClickListener,
        View.OnFocusChangeListener, TextWatcher, OnGetSuggestionResultListener {

    private boolean mShowDropDownOnSuggestion = false;
    private ArrayList<BaiduSuggestion> mSuggestions;
    private BottomSheetManager mBtmSheetManager;
    private AutoCompleteTextView mAutoComplete;
    private BaiduMapManager mMapManager;
    private Activity mAct;
    private TextView mCitySelector;

    public SuggestionSearchManager(@NonNull Activity act, @NonNull AutoCompleteTextView input,
                                   @NonNull TextView cityView, @NonNull BaiduMapManager mapManager,
                                   @NonNull BottomSheetManager btmSheetManager) {
        mAct = act;
        mAutoComplete = input;
        mMapManager = mapManager;
        mCitySelector = cityView;
        mBtmSheetManager = btmSheetManager;
        mMapManager.SUGGESTION_SEARCH.setOnGetSuggestionResultListener(this);
        mAutoComplete.setOnKeyListener(this);
        mAutoComplete.addTextChangedListener(this);
        mAutoComplete.setOnItemClickListener(this);
        mAutoComplete.setOnFocusChangeListener(this);
        mAutoComplete.setThreshold(1);

        mAutoComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mAutoComplete.isPopupShowing() && mAutoComplete.getText().length() != 0) {
                    mAutoComplete.showDropDown();
                }
            }
        });
    }

    private void searchForEnteredTextInCity() {
        PoiCitySearchOption searchOption = new PoiCitySearchOption()
                .city(mCitySelector.getText().toString())
                .keyword(mAutoComplete.getText().toString());
        mBtmSheetManager.searchCity(searchOption);
        mAutoComplete.dismissDropDown();
        Drawing.hideKeyboard(mAutoComplete, mAct);
    }

    private ArrayList<BaiduSuggestion> getAutoCompleteArray() {
        ArrayList<BaiduSuggestion> suggest = new ArrayList<>();
        if (!mAutoComplete.getText().equals("")) {
            suggest.add(new BaiduSuggestion("\uD83D\uDD0D" + mAutoComplete.getText()));
        }
        return suggest;
    }

    @Override
    public void onFocusChange(View view, boolean gainedFocus) {
        AutoCompleteTextView tv = (AutoCompleteTextView) view;
        if (!gainedFocus) {
            mBtmSheetManager.removeMarkers();
            mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
            tv.setText("");
            com.guide.green.green_guide.Utilities.Drawing.hideKeyboard(view, mAct);
        }
    }

    @Override
    public void afterTextChanged(Editable a) { /* Do Nothing */ }

    @Override
    public void beforeTextChanged(CharSequence a, int b, int c, int d) {  /* Do Nothing */ }

    /**
     * Request suggestion when text is entered.
     */
    @Override
    public void onTextChanged(CharSequence cs, int start, int before, int count) {
        if (cs.length() > 0) {
            ArrayAdapter<BaiduSuggestion> sugAdapter = new ArrayAdapter<>(mAct,
                    android.R.layout.simple_dropdown_item_1line, getAutoCompleteArray());
            mAutoComplete.setAdapter(sugAdapter);
            sugAdapter.notifyDataSetChanged();

            mMapManager.SUGGESTION_SEARCH.requestSuggestion((new SuggestionSearchOption())
                    .city(mCitySelector.getText().toString()).keyword(cs.toString()));
        }
    }

    /**
     * Set the items in the autocomplete dropdown to those returned by baidu
     * @param res
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        mSuggestions = getAutoCompleteArray();
        if (res != null && res.getAllSuggestions() != null) {
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                mSuggestions.add(new BaiduSuggestion(info));
            }
        }
        ArrayAdapter<BaiduSuggestion> sugAdapter =
                new ArrayAdapter<>(mAct, android.R.layout.simple_dropdown_item_1line, mSuggestions);
        mAutoComplete.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();

        if (mShowDropDownOnSuggestion) {
            mAutoComplete.showDropDown();
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            searchForEnteredTextInCity();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BaiduSuggestion suggestion = mSuggestions.get(position);

        if (position == 0) {
            CharSequence autoCompleteText = mAutoComplete.getText();
            mAutoComplete.setText(autoCompleteText.subSequence(3, autoCompleteText.length()));
            mAutoComplete.setSelection(autoCompleteText.length() - 3);
            searchForEnteredTextInCity();
            mShowDropDownOnSuggestion = false;
            return;
        }

        // Start the asynchronous retrieval of the GreenGuide DB data about this place
        if (suggestion.point != null) {
            mShowDropDownOnSuggestion = false;
            mBtmSheetManager.getReviews(suggestion);
            Drawing.hideKeyboard(mAutoComplete, mAct);
        } else {
            mShowDropDownOnSuggestion = true;
        }

        mAutoComplete.setText(suggestion.name);
        mAutoComplete.setSelection(suggestion.name.length());
    }
}
