package com.guide.green.green_guide.Utilities;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.widget.NestedScrollView;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.guide.green.green_guide.R;


/**
 * This class stores variables relating to the bottom sheet and manages any interactions with it.
 * It is closely coupled with the {@code BaiduMapManager} class because it uses this class to
 * display search for and display POI results.
 */
public class BottomSheetManager extends BottomSheetBehavior.BottomSheetCallback {
    private AppCompatActivity mAct;
    public final Reviews reviews;
    private DBReviewSearchManager mDbSearcher;
    private BottomSheetBehavior mBtmSheet;
    private NestedScrollView mBtmSheetView;
    private BaiduMapManager mMapManager;
    private FetchReviewsHandler mFetchReviewsHandler;
    private int mState;

    public BottomSheetManager(@NonNull AppCompatActivity act, @NonNull NestedScrollView bottomSheet,
                              @NonNull Reviews reviews, @NonNull DBReviewSearchManager dbSearcher,
                              @NonNull BaiduMapManager mapManager) {
        mAct = act;
        this.reviews = reviews;
        mDbSearcher = dbSearcher;
        mMapManager = mapManager;
        mBtmSheetView = bottomSheet;
        mBtmSheet = BottomSheetBehavior.from(mBtmSheetView);
        mBtmSheet.setBottomSheetCallback(this);
        this.mDbSearcher.setBtmSheetManager(this);
    }

    public static class Reviews {
        public static class PeekBar {
            public TextView companyName;
            public TextView ratingValue;
            public ImageView ratingStars;
            public TextView ratingCount;
        }

        public static class Body {
            public ViewGroup container;
            public ViewGroup reviews;
            public TextView address;
            public TextView city;
            public TextView industry;
            public TextView product;
            public ImageView histogram;
        }

        public ViewGroup container;
        public Button writeReviewButton;
        public PeekBar peekBar = new PeekBar();
        public Body body = new Body();
    }

    public void setMarkerVisibility(boolean isVisible) {
        if (mFetchReviewsHandler != null) {
            mFetchReviewsHandler.marker.setVisible(isVisible);
        }
        mDbSearcher.setVisibility(isVisible);
    }

    public void removeMarkers() {
        if (mFetchReviewsHandler != null) {
            mFetchReviewsHandler.marker.remove();
        }
        mDbSearcher.remove();
    }

    public int getBottomSheetState() {
        return mBtmSheet.getState();
    }

    public void setBottomSheetState(int state) {
        mBtmSheet.setHideable(state == BottomSheetBehavior.STATE_HIDDEN);
        mBtmSheet.setState(state);
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            reviews.peekBar.companyName.setSingleLine(true);
            //reviews.peekBar.companyName.setPadding(0, 0, 0, 0);
        } else {
            int px = (int) Drawing.convertDpToPx(mAct, 10);
            reviews.peekBar.companyName.setSingleLine(false);
            //reviews.peekBar.companyName.setPadding(0, px,0, px);
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                setMarkerVisibility(false);
            }
        }
    }

    public void showPoiResults() {
        mDbSearcher.container.setVisibility(View.VISIBLE);
        reviews.container.setVisibility(View.GONE);
    }

    public void showReviews() {
        mDbSearcher.container.setVisibility(View.GONE);
        reviews.container.setVisibility(View.VISIBLE);
    }

    public void getReviewFor(@NonNull BaiduSuggestion.Location suggestion) {
        removeMarkers();
        showReviews();

        if (mFetchReviewsHandler != null && !mFetchReviewsHandler.isCompleted()) {
            mFetchReviewsHandler.cancel();
        }

        mMapManager.moveTo(suggestion.point);

        mFetchReviewsHandler = new FetchReviewsHandler(mAct, suggestion, this,
                mMapManager.addMarker(new MarkerOptions().position(suggestion.point),
                        R.drawable.icon_star_marker));

        if (suggestion.uid != null && suggestion.address == null || suggestion.address.equals("")) {
            mMapManager.poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(suggestion.uid));
        }
    }

    public void searchGreenGuideReviewsFor(@NonNull String query) {
        removeMarkers();
        showPoiResults();
        mDbSearcher.searchFor(mMapManager, query);
        setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
        mBtmSheet.setHideable(false);
    }
    
    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        /* Do Nothing */
    }

    public void saveAndHide() {
        setMarkerVisibility(false);
        mState = mBtmSheet.getState();
        mBtmSheet.setHideable(true);
        setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void restore() {
        setMarkerVisibility(true);
        setBottomSheetState(mState);
    }
}