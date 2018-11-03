package com.guide.green.green_guide.Utilities;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewPager;
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
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.BaiduMapManager.BaiduSuggestion;



/**
 * Will change the MarkerClickListener when ever a city search is run.
 * Will change
 */
public class BottomSheetManager extends BottomSheetBehavior.BottomSheetCallback implements
        BaiduMap.OnMapClickListener {

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
        public Button firstReviewButton;
        public PeekBar peekBar = new PeekBar();
        public Body body = new Body();
    }

    public static class PoiSearchResults {
        public ViewGroup container;
        public ViewPager swipeView;
    }

    private Overlay mSelectedMarker;
    private AppCompatActivity mAct;
    public final Reviews REVIEWS;
    public final PoiSearchResults POI_RESULTS;
    private BottomSheetBehavior mBtmSheet;
    private NestedScrollView mBtmSheetView;
    private BaiduMapManager mMapManager;
    private PageChangeListener mPageChangeListener;
    private FetchReviewsHandler mFetchReviewsHandler;

    public BottomSheetManager(@NonNull AppCompatActivity act, @NonNull NestedScrollView bottomSheet,
                              @NonNull Reviews reviews, @NonNull PoiSearchResults poiResults,
                              @NonNull BaiduMapManager mapManager) {
        mAct = act;
        REVIEWS = reviews;
        POI_RESULTS = poiResults;
        mMapManager = mapManager;
        mBtmSheetView = bottomSheet;
        mBtmSheet = BottomSheetBehavior.from(mBtmSheetView);
        mBtmSheet.setBottomSheetCallback(this);
        mMapManager.BAIDU_MAP.setOnMapClickListener(this);
    }

    public void setMarkerVisibility(boolean isVisible) {
        if (mSelectedMarker != null) {
            mSelectedMarker.setVisible(isVisible);
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.setVisibility(isVisible);
        }
    }

    public void removeMarkers() {
        if (mSelectedMarker != null) {
            mSelectedMarker.remove();
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.remove();
        }
    }

    public int getBottomSheetState() {
        return mBtmSheet.getState();
    }

    public void setBottomSheetState(int state) {
        mBtmSheet.setState(state);
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            REVIEWS.peekBar.companyName.setSingleLine(true);
            REVIEWS.peekBar.companyName.setPadding(0, 0, 0, 0);
        } else {
            int px = (int) Drawing.convertDpToPx(mAct, 10);
            REVIEWS.peekBar.companyName.setSingleLine(false);
            REVIEWS.peekBar.companyName.setPadding(0, px,0, px);
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                setMarkerVisibility(false);
            }
        }
    }

    public void showPoiResults() {
        POI_RESULTS.container.setVisibility(View.VISIBLE);
        REVIEWS.container.setVisibility(View.GONE);
    }

    public void showReviews() {
        POI_RESULTS.container.setVisibility(View.GONE);
        REVIEWS.container.setVisibility(View.VISIBLE);
    }

    public void getReviews(BaiduSuggestion suggestion) {
        removeMarkers();
        showReviews();

        if (mFetchReviewsHandler != null && !mFetchReviewsHandler.isCompleted()) {
            mFetchReviewsHandler.cancel();
        }

        mFetchReviewsHandler = new FetchReviewsHandler(mAct, suggestion, this);

        mMapManager.moveTo(suggestion.point);
        mSelectedMarker = mMapManager.addMarker(new MarkerOptions()
                .position(suggestion.point), R.drawable.icon_star_marker);
    }

    public void searchCity(PoiCitySearchOption searchOption) {
        removeMarkers();     // Must be called before the mPageChangeListener value is changed
        showPoiResults();

        PoiResultsPagerAdapter pagerAdapter =
                new PoiResultsPagerAdapter(mAct, mMapManager, searchOption);

        pagerAdapter.setPoiClickHandler(new PoiResultsPagerAdapter.PoiSelected() {
            @Override
            public void onPoiSelected(PoiInfo poi) {
                getReviews(new BaiduSuggestion(poi));
                mBtmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        mPageChangeListener = new PageChangeListener(pagerAdapter);
        POI_RESULTS.swipeView.addOnPageChangeListener(mPageChangeListener);
        POI_RESULTS.swipeView.setAdapter(pagerAdapter);

        pagerAdapter.notifyDataSetChanged();
        mBtmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    
    @Override
    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        /* Do Nothing */
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mBtmSheet.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            mBtmSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        getReviews(new BaiduSuggestion(mapPoi));
        return false;
    }

    private int mState;
    public void saveAndHide() {
        setMarkerVisibility(false);
        mState = mBtmSheet.getState();
        setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public void restore() {
        setMarkerVisibility(true);
        mBtmSheet.setState(mState);
    }
}