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
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.guide.green.green_guide.R;


/**
 * This class stores variables relating to the bottom sheet and manages any interactions with it.
 * It is closely coupled with the {@code BaiduMapManager} class because it uses this class to
 * display search for and display POI results.
 */
public class BottomSheetManager extends BottomSheetBehavior.BottomSheetCallback implements
        BaiduMap.OnMapClickListener, OnGetPoiSearchResultListener {
    private AppCompatActivity mAct;
    public final Reviews reviews;
    public final PoiSearchResults poiResults;
    private BottomSheetBehavior mBtmSheet;
    private NestedScrollView mBtmSheetView;
    private BaiduMapManager mMapManager;
    private PageChangeListener mPageChangeListener;
    private FetchReviewsHandler mFetchReviewsHandler;

    public BottomSheetManager(@NonNull AppCompatActivity act, @NonNull NestedScrollView bottomSheet,
                              @NonNull Reviews reviews, @NonNull PoiSearchResults poiResults,
                              @NonNull BaiduMapManager mapManager) {
        mAct = act;
        this.reviews = reviews;
        this.poiResults = poiResults;
        mMapManager = mapManager;
        mBtmSheetView = bottomSheet;
        mBtmSheet = BottomSheetBehavior.from(mBtmSheetView);
        mBtmSheet.setBottomSheetCallback(this);
        mMapManager.baiduMap.setOnMapClickListener(this);
        mMapManager.poiSearch.setOnGetPoiSearchResultListener(this);
    }

    public static class PoiSearchResults {
        public ViewGroup container;
        public ViewPager swipeView;
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
        if (mPageChangeListener != null) {
            mPageChangeListener.setVisibility(isVisible);
        }
    }

    public void removeMarkers() {
        if (mFetchReviewsHandler != null) {
            mFetchReviewsHandler.marker.remove();
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
            reviews.peekBar.companyName.setSingleLine(true);
            reviews.peekBar.companyName.setPadding(0, 0, 0, 0);
        } else {
            int px = (int) Drawing.convertDpToPx(mAct, 10);
            reviews.peekBar.companyName.setSingleLine(false);
            reviews.peekBar.companyName.setPadding(0, px,0, px);
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                setMarkerVisibility(false);
            }
        }
    }

    public void showPoiResults() {
        poiResults.container.setVisibility(View.VISIBLE);
        reviews.container.setVisibility(View.GONE);
    }

    public void showReviews() {
        poiResults.container.setVisibility(View.GONE);
        reviews.container.setVisibility(View.VISIBLE);
    }

    public void getReview(@NonNull BaiduSuggestion.Location suggestion) {
        removeMarkers();
        showReviews();

        if (mFetchReviewsHandler != null && !mFetchReviewsHandler.isCompleted()) {
            mFetchReviewsHandler.cancel();
        }


        mMapManager.moveTo(suggestion.point);

        mFetchReviewsHandler = new FetchReviewsHandler(mAct, suggestion, this,
                mMapManager.addMarker(new MarkerOptions().position(suggestion.point),
                        R.drawable.icon_star_marker));

        if (suggestion.uid != null) {
            mMapManager.poiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(suggestion.uid));
        }
    }

    public void poiSearchCity(@NonNull PoiCitySearchOption searchOption) {
        removeMarkers();     // Must be called before the mPageChangeListener value is changed
        showPoiResults();

        PoiResultsPagerAdapter pagerAdapter =
                new PoiResultsPagerAdapter(mAct, mMapManager, searchOption);

        pagerAdapter.setPoiClickHandler(new PoiResultsPagerAdapter.PoiSelected() {
            @Override
            public void onPoiSelected(PoiInfo poi) {
                getReview(new BaiduSuggestion.Location(poi));
                mBtmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        mPageChangeListener = new PageChangeListener(pagerAdapter);
        poiResults.swipeView.addOnPageChangeListener(mPageChangeListener);
        poiResults.swipeView.setAdapter(pagerAdapter);

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
            removeMarkers();
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        /* Do nothing */
        return false;
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        if (mFetchReviewsHandler != null) {
            mFetchReviewsHandler.updatePoiResult(new BaiduSuggestion.Location(poiDetailResult));
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {}

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