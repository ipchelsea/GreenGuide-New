package com.guide.green.green_guide.Utilities;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.guide.green.green_guide.R;
import java.util.ArrayList;

public class SuggestionSearchManager extends AutoComplete implements View.OnKeyListener,
        SuggestionSearchAdapter.OnItemClickListener, OnGetSuggestionResultListener {

    private boolean mWasShowingBottomSheet;
    private boolean mShowDropDownOnSuggestion = true;
    private ArrayList<BaiduSuggestion> mSuggestions;
    private BottomSheetManager mBtmSheetManager;
    private BaiduMapManager mMapManager;
    private TextView mCitySelector;
    private AppCompatActivity mAct;
    private ViewGroup mDropDownContainer;
    private ViewGroup mMapViewContainer;
    private EditText mTextInput;
    private RecyclerView mDropdown;
    private SuggestionSearchAdapter.ItemIcons mItemIcons;
    private DrawerController mDrawerCtrler;

    /** Provides a way to toggle between the back button and the hamburger menu button */
    public interface DrawerController {
        void showBackButton(boolean enabled);
    }

    public SuggestionSearchManager(@NonNull AppCompatActivity act, @NonNull EditText textInput,
                                   @NonNull RecyclerView dropdown, @NonNull TextView cityView,
                                   @NonNull BaiduMapManager mapManager,
                                   @NonNull BottomSheetManager btmSheetManager,
                                   @NonNull ViewGroup mapViewContainer,
                                   @NonNull ViewGroup dropDownContainer,
                                   @NonNull DrawerController drawerCtrler) {
        super(act, textInput, dropdown);
        mItemIcons = new SuggestionSearchAdapter.ItemIcons();
        mItemIcons.FIRST_ITEM = Drawing.getDrawable(act, R.drawable.ic_stan_db);
        mItemIcons.AUTO_COMPLETE_TEXT = null;
        mItemIcons.LOCATION = Drawing.getDrawable(act, R.drawable.icon_markg_red);
        mMapViewContainer = mapViewContainer;
        mDropDownContainer = dropDownContainer;
        mDrawerCtrler = drawerCtrler;

        textInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    mDrawerCtrler.showBackButton(true);
                    onSuggestionClick();
                }
            }
        });

        mTextInput = textInput;
        mDropdown = dropdown;

        mAct = act;
        mMapManager = mapManager;
        mCitySelector = cityView;
        mBtmSheetManager = btmSheetManager;
        mMapManager.SUGGESTION_SEARCH.setOnGetSuggestionResultListener(this);
        mTextInput.setOnKeyListener(this);
        mTextInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SuggestionSearchManager.this.onSuggestionClick();
            }
        });

        setAllParentsClip(dropdown, false);
        dropdown.bringToFront();
    }

    private void onTextInputLostFocus() {
        hideDropDownOverlay();
        Misc.hideKeyboard(mTextInput, mAct);
        mTextInput.clearFocus();
    }

    /**
     * Called externally when the drawer back button is pressed.
     */
    public void onBackButtonClick() {
        if (mWasShowingBottomSheet && !isDropDownOverlayShowing()) {
            mWasShowingBottomSheet = false;
        }

        onTextInputLostFocus();

        if (mWasShowingBottomSheet) {
            mWasShowingBottomSheet = false;
            mBtmSheetManager.restore();
        } else {
            mBtmSheetManager.removeMarkers();
            mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
            mTextInput.setText("");
            mDrawerCtrler.showBackButton(false);
        }

        mTextInput.clearFocus();
    }

    private void onSuggestionClick() {
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

    public boolean hasText() {
        return mTextInput.getText().length() != 0;
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

    private void poiSearchForEnteredTextInCity() {
        mBtmSheetManager.searchGreenGuideReviewsFor(mTextInput.getText().toString());
        onTextInputLostFocus();
    }

    private ArrayList<BaiduSuggestion> getAutoCompleteArray() {
        ArrayList<BaiduSuggestion> suggest = new ArrayList<>();
        if (getInputTextLength() != 0) {
            suggest.add(new BaiduSuggestion.TextSuggestion(mTextInput.getText().toString()));
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
            mMapManager.SUGGESTION_SEARCH.requestSuggestion(new SuggestionSearchOption()
                    .city(mCitySelector.getText().toString())
                    .keyword(cs.toString()).citylimit(false));
        } else {
            dismissDropDown();
        }
    }

    public void onItemClick(int position) {
        BaiduSuggestion suggestion = mSuggestions.get(position);

        if (position == 0) {
            poiSearchForEnteredTextInCity();
            mShowDropDownOnSuggestion = false;
            return;
        }

        // Start the asynchronous retrieval of the GreenGuide DB data about this place
        if (suggestion.getType() == BaiduSuggestion.Type.LOCATION) {
            BaiduSuggestion.Location suggestedLocation = (BaiduSuggestion.Location) suggestion;

            mShowDropDownOnSuggestion = false;
            mBtmSheetManager.getReviewFor(suggestedLocation);
            mTextInput.clearFocus();
            Misc.hideKeyboard(mTextInput, mAct);
            hideDropDownOverlay();

            mTextInput.setText(suggestedLocation.name);
            mTextInput.setSelection(suggestedLocation.name.length());
        } else {
            BaiduSuggestion.TextSuggestion suggestedText =
                    (BaiduSuggestion.TextSuggestion) suggestion;

            mShowDropDownOnSuggestion = true;
            mTextInput.setText(suggestedText.suggestion);
            mTextInput.setSelection(suggestedText.suggestion.length());
        }
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
                if (info.pt != null) {
                    suggestions.add(new BaiduSuggestion.Location(info));
                }
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
            poiSearchForEnteredTextInCity();
            return true;
        }
        return false;
    }
}