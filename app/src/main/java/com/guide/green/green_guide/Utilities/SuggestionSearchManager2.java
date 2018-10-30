package com.guide.green.green_guide.Utilities;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.guide.green.green_guide.Utilities.BaiduMapManager.BaiduSuggestion;

import java.util.ArrayList;

public class SuggestionSearchManager2 extends AutoComplete implements View.OnKeyListener,
        OnGetSuggestionResultListener {

    private String lastSearch = "";
    private boolean mShowDropDownOnSuggestion = false;
    private ArrayList<BaiduSuggestion> mSuggestions;
    private BottomSheetManager mBtmSheetManager;
    private BaiduMapManager mMapManager;
    private TextView mCitySelector;
    private Activity mAct;
    private MenuItem mSearchBarContainer;
    private ViewGroup mDropDownContainer;
    private ViewGroup mMapViewContainer;

    private SuggestionSearchOption mSearchOption;
    private int mTotalPages = Integer.MIN_VALUE;
    private int nextPage = 0;

    private EditText mTextInput;
    private RecyclerView mDropdown;
    private SuggestionSearchAdapter mSuggestionSearchAdapter = new SuggestionSearchAdapter();

    public SuggestionSearchManager2(@NonNull Activity act, @NonNull EditText textInput,
                                    @NonNull RecyclerView dropdown, @NonNull TextView cityView,
                                    @NonNull BaiduMapManager mapManager,
                                    @NonNull BottomSheetManager btmSheetManager,
                                    @NonNull MenuItem searbarContainer,
                                    @NonNull ViewGroup mapViewContainer,
                                    @NonNull ViewGroup dropDownContainer) {
        super(act, textInput, dropdown);
        mMapViewContainer = mapViewContainer;
        mDropDownContainer = dropDownContainer;
        mSearchBarContainer = searbarContainer;
        mSearchBarContainer.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                showDropDownOverlay();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                hideDropDownOverlay();
                mBtmSheetManager.clearMarkers();
                mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
                mTextInput.setText("");
                Drawing.hideKeyboard(mTextInput, mAct);
                return true;
            }
        });

        dropdown.setAdapter(mSuggestionSearchAdapter);
        mSuggestionSearchAdapter.notifyDataSetChanged();

        mTextInput = textInput;
        mDropdown = dropdown;

        mAct = act;
        mMapManager = mapManager;
        mCitySelector = cityView;
        mBtmSheetManager = btmSheetManager;
        mMapManager.SUGGESTION_SEARCH2.setOnGetSuggestionResultListener(this);
        mDropdown.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
                int index = mDropdown.indexOfChild(view);
                if (index > mDropdown.getChildCount() / 2) {
                    loadNextPage();
                }
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {}
        });
        mTextInput.setOnKeyListener(this);
        mTextInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isPopupShowing() && mTextInput.getText().length() != 0) {
                    showDropDown();
                }
            }
        });

        setAllParentsClip(dropdown, false);
        dropdown.bringToFront();
    }

    private void loadNextPage() {
        if (nextPage < mTotalPages) {
            mSearchOption.mCityLimit = true;
            mMapManager.SUGGESTION_SEARCH2.requestSuggestion(mSearchOption);
            mShowDropDownOnSuggestion = true;
        }
    }

    public void hideDropDownOverlay() {
        mDropDownContainer.setVisibility(View.GONE);
        mMapViewContainer.setVisibility(View.VISIBLE);
    }

    public void showDropDownOverlay() {
        mDropDownContainer.setVisibility(View.VISIBLE);
        mMapViewContainer.setVisibility(View.GONE);
    }

    @Override
    public void showDropDown() {
        super.showDropDown();
        showDropDownOverlay();
    }

    public static void setAllParentsClip(View v, boolean enabled) {
        while (v.getParent() != null && v.getParent() instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v.getParent();
            viewGroup.setClipChildren(enabled);
            viewGroup.setClipToPadding(enabled);
            v = viewGroup;
        }
    }

    private void searchForEnteredTextInCity() {
        PoiCitySearchOption searchOption = new PoiCitySearchOption()
                .city(mCitySelector.getText().toString())
                .keyword(mTextInput.getText().toString());
        mBtmSheetManager.searchCity(searchOption);
        hideDropDownOverlay();
        dismissDropDown();
        Drawing.hideKeyboard(mTextInput, mAct);
    }

    private ArrayList<BaiduSuggestion> getAutoCompleteArray() {
        ArrayList<BaiduSuggestion> suggest = new ArrayList<>();
        if (getInputTextLength() != 0) {
            suggest.add(new BaiduSuggestion(mTextInput.getText().toString()));
        }
        return suggest;
    }

    @Override
    public void onFocusChange(View view, boolean gainedFocus) {
        super.onFocusChange(view, gainedFocus);
        showDropDownOverlay();
    }

    /**
     * Request suggestion when text is entered.
     */
    @Override
    public void onTextChanged(CharSequence cs, int start, int before, int count) {
        if (cs.length() > 0) {
            mSuggestionSearchAdapter.replaceAllSuggestions(getAutoCompleteArray());
            mSearchOption = new SuggestionSearchOption().city(mCitySelector.getText().toString())
                    .keyword(cs.toString());

            loadNextPage();
        }
        lastSearch = mCitySelector.toString();
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

        if (lastSearch.equals(mTextInput.getText())) {
            mSuggestionSearchAdapter.addSuggestions(mSuggestions);
        } else {
            mSuggestionSearchAdapter.replaceAllSuggestions(mSuggestions);
        }

        if (mShowDropDownOnSuggestion) {
            showDropDown();
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

    public void onItemClick(int position) {
        BaiduSuggestion suggestion = mSuggestions.get(position);

        if (position == 0) {
            CharSequence autoCompleteText = mTextInput.getText();
            mTextInput.setText(autoCompleteText.subSequence(3, autoCompleteText.length()));
            mTextInput.setSelection(autoCompleteText.length() - 3);
            searchForEnteredTextInCity();
            mShowDropDownOnSuggestion = false;
            return;
        }

        // Start the asynchronous retrieval of the GreenGuide DB data about this place
        if (suggestion.point != null) {
            mShowDropDownOnSuggestion = false;
            mBtmSheetManager.getReviews(suggestion);
            Drawing.hideKeyboard(mTextInput, mAct);
        } else {
            mShowDropDownOnSuggestion = true;
        }

        mTextInput.setText(suggestion.name);
        mTextInput.setSelection(suggestion.name.length());
    }
}
