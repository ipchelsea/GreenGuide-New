package com.guide.green.green_guide.Utilities;

import android.app.Activity;
import android.os.Build;
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
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.BaiduMapManager.BaiduSuggestion;

import java.util.ArrayList;

public class SuggestionSearchManager2 extends AutoComplete implements View.OnKeyListener,
        SuggestionSearchAdapter.OnItemClickListener, OnGetSuggestionResultListener {

    private boolean mWasShowingBottomSheet;
    private boolean mShowDropDownOnSuggestion = true;
    private boolean mEnterJustPressed = false;
    private ArrayList<BaiduSuggestion> mSuggestions;
    private BottomSheetManager mBtmSheetManager;
    private BaiduMapManager mMapManager;
    private TextView mCitySelector;
    private Activity mAct;
    private MenuItem mSearchBarContainer;
    private ViewGroup mDropDownContainer;
    private ViewGroup mMapViewContainer;

    private EditText mTextInput;
    private RecyclerView mDropdown;
    private SuggestionSearchAdapter mSuggestionSearchAdapter;
    private SuggestionSearchAdapter.ItemIcons mItemIcons;

    public SuggestionSearchManager2(@NonNull Activity act, @NonNull EditText textInput,
                                    @NonNull RecyclerView dropdown, @NonNull TextView cityView,
                                    @NonNull BaiduMapManager mapManager,
                                    @NonNull BottomSheetManager btmSheetManager,
                                    @NonNull MenuItem searbarContainer,
                                    @NonNull ViewGroup mapViewContainer,
                                    @NonNull ViewGroup dropDownContainer) {
        super(act, textInput, dropdown);
        mItemIcons = new SuggestionSearchAdapter.ItemIcons();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mItemIcons.FIRST_ITEM = act.getDrawable(R.drawable.ic_search_review);
            mItemIcons.AUTO_COMPLETE_TEXT = null;
            mItemIcons.LOCATION = act.getDrawable(R.drawable.icon_markg_red);
        } else {
            mItemIcons.FIRST_ITEM = act.getResources().getDrawable(R.drawable.ic_search_review);
            mItemIcons.AUTO_COMPLETE_TEXT = null;
            mItemIcons.LOCATION = act.getResources().getDrawable(R.drawable.icon_markg_red);
        }
        mMapViewContainer = mapViewContainer;
        mDropDownContainer = dropDownContainer;
        mSearchBarContainer = searbarContainer;
        mSearchBarContainer.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                onClick();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                if (mWasShowingBottomSheet && !isDropDownOverlayShowing()) {
                    mWasShowingBottomSheet = false;
                }

                hideDropDownOverlay();
                Drawing.hideKeyboard(mTextInput, mAct);
                mTextInput.clearFocus();

                if (mWasShowingBottomSheet) {
                    mWasShowingBottomSheet = false;
                    mBtmSheetManager.restore();
                    return false;
                } else {
                    mBtmSheetManager.removeMarkers();
                    mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
                    mTextInput.setText("");
                    return true;
                }
            }
        });

        mTextInput = textInput;
        mDropdown = dropdown;

        mAct = act;
        mMapManager = mapManager;
        mCitySelector = cityView;
        mBtmSheetManager = btmSheetManager;
        mMapManager.SUGGESTION_SEARCH2.setOnGetSuggestionResultListener(this);
        mTextInput.setOnKeyListener(this);
        mTextInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuggestionSearchManager2.this.onClick();
            }
        });

        setAllParentsClip(dropdown, false);
        dropdown.bringToFront();
    }

    private void onClick() {
        if (mEnterJustPressed) {
            mEnterJustPressed = false;
            return;
        }
        if (mBtmSheetManager.getBottomSheetState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBtmSheetManager.saveAndHide();
            mWasShowingBottomSheet = true;
        }
        if (!isDropDownOverlayShowing()) {
            mShowDropDownOnSuggestion = true;
            showDropDownOverlay();
            if (mTextInput.getText().length() != 0) {
                showDropDown();
            }
        }
    }

    public boolean isDropDownOverlayShowing() {
        return mDropDownContainer.getVisibility() == View.VISIBLE;
    }

    public void hideDropDownOverlay() {
        mDropDownContainer.setVisibility(View.GONE);
        mMapViewContainer.setVisibility(View.VISIBLE);
    }

    public void showDropDownOverlay() {
        mDropDownContainer.setVisibility(View.VISIBLE);
        mMapViewContainer.setVisibility(View.GONE);
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
        dismissDropDown();
        Drawing.hideKeyboard(mTextInput, mAct);
        hideDropDownOverlay();
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
    }

    /**
     * Request suggestion when text is entered.
     */
    @Override
    public void onTextChanged(CharSequence cs, int start, int before, int count) {
        if (cs.length() > 0) {
            replaceAllSuggestions(getAutoCompleteArray());
            mMapManager.SUGGESTION_SEARCH2.requestSuggestion(new SuggestionSearchOption()
                    .city(mCitySelector.getText().toString())
                    .keyword(cs.toString()).citylimit(true));
        } else {
            dismissDropDown();
        }
    }

    public void onItemClick(int position) {
        BaiduSuggestion suggestion = mSuggestions.get(position);

        if (position == 0) {
            searchForEnteredTextInCity();
            mShowDropDownOnSuggestion = false;
            return;
        }

        // Start the asynchronous retrieval of the GreenGuide DB data about this place
        if (suggestion.point != null) {
            mShowDropDownOnSuggestion = false;
            mBtmSheetManager.getReviews(suggestion);
            mTextInput.clearFocus();
            Drawing.hideKeyboard(mTextInput, mAct);
            hideDropDownOverlay();
        } else {
            mShowDropDownOnSuggestion = true;
        }

        mTextInput.setText(suggestion.name);
        mTextInput.setSelection(suggestion.name.length());
    }

    public void replaceAllSuggestions(ArrayList<BaiduSuggestion> suggestions) {
        SuggestionSearchAdapter mSuggestionSearchAdapter =
                new SuggestionSearchAdapter(suggestions, mItemIcons);
        mSuggestionSearchAdapter.setOnItemClickListener(this);
        mDropdown.setAdapter(mSuggestionSearchAdapter);
        mSuggestionSearchAdapter.notifyDataSetChanged();
        mSuggestions = suggestions;
    }

    /**
     * Set the items in the autocomplete dropdown to those returned by baidu
     * @param res
     */
    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        ArrayList<BaiduSuggestion> suggestions = getAutoCompleteArray();
        if (res != null && res.getAllSuggestions() != null) {
            for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
                suggestions.add(new BaiduSuggestion(info));
            }
        }

        replaceAllSuggestions(suggestions);

        if (mTextInput.getText().length() != 0 && isDropDownOverlayShowing()
                && mShowDropDownOnSuggestion) {
            showDropDown();
        }
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            searchForEnteredTextInCity();
            mEnterJustPressed = true;
        }
        return false;
    }
}