package com.guide.green.green_guide.Utilities;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.guide.green.green_guide.PoiOverlay;
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
    private Reviews mReviews;
    private PoiSearchResults mPoiResults;
    private BottomSheetBehavior mBtmSheet;
    private NestedScrollView mBtmSheetView;
    private BaiduMapManager mMapManager;
    private PageChangeListener mPageChangeListener;

    public BottomSheetManager(@NonNull AppCompatActivity act, @NonNull NestedScrollView bottomSheet,
                              @NonNull Reviews reviews, @NonNull PoiSearchResults poiResults,
                              @NonNull BaiduMapManager mapManager) {
        mAct = act;
        mReviews = reviews;
        mPoiResults = poiResults;
        mMapManager = mapManager;
        mBtmSheetView = bottomSheet;
        mBtmSheet = BottomSheetBehavior.from(mBtmSheetView);
        mBtmSheet.setBottomSheetCallback(this);
        mMapManager.BAIDU_MAP.setOnMapClickListener(this);
    }

    public void clearMarkers() {
        if (mSelectedMarker != null) {
            mSelectedMarker.remove();
        }
        if (mPageChangeListener != null) {
            mPageChangeListener.remove();
        }
    }

    public void setBottomSheetState(int state) {
        mBtmSheet.setState(state);
    }

    @Override
    public void onStateChanged(@NonNull View bottomSheet, int newState) {
        ViewGroup.LayoutParams layoutParams = mReviews.peekBar.companyName.getLayoutParams();
        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
            mReviews.peekBar.companyName.setSingleLine(true);
            mReviews.peekBar.companyName.setPadding(0, 0, 0, 0);
        } else {
            int px = (int) Drawing.convertDpToPx(mAct, 10);
            mReviews.peekBar.companyName.setSingleLine(false);
            mReviews.peekBar.companyName.setPadding(0, px,0, px);
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                clearMarkers();
            }
        }
    }

    public void showPoiResults() {
        mPoiResults.container.setVisibility(View.VISIBLE);
        mReviews.container.setVisibility(View.GONE);
    }

    public void showReviews() {
        mPoiResults.container.setVisibility(View.GONE);
        mReviews.container.setVisibility(View.VISIBLE);
    }

    public void getReviews(BaiduSuggestion suggestion) {
        FetchReviewsHandler.fetch(mAct, suggestion);
        clearMarkers();
        showReviews();
        mMapManager.moveTo(suggestion.point);

        int resoureseId = mAct.getResources().getIdentifier("icon_markg_red",
                "drawable", mAct.getPackageName());

        mSelectedMarker = mMapManager.addMarker(new MarkerOptions()
                .position(suggestion.point), resoureseId);
    }

    public void searchCity(PoiCitySearchOption searchOption) {
        FragPager pagerAdapter = new FragPager(mAct, mMapManager, searchOption);
        pagerAdapter.setPoiClickHandler(new FragPager.PoiSelected() {
            @Override
            public void onPoiSelected(PoiInfo poi) {
                getReviews(new BaiduSuggestion(poi));
                mBtmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        mPageChangeListener = new PageChangeListener(pagerAdapter);
        mPoiResults.swipeView.addOnPageChangeListener(mPageChangeListener);
        mPoiResults.swipeView.setAdapter(pagerAdapter);

        clearMarkers();
        showPoiResults();
        pagerAdapter.notifyDataSetChanged();
        mBtmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public void onGetPoiResult(PoiResult result) {
        Log.i("TotalPageNumbers", "" + result.getTotalPageNum());
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.w("PoiResult", "Code: " + result.error + ", " + result.error.toString());
            return;
        }

        PoiOverlay mOverlay = new PoiOverlay(mMapManager.BAIDU_MAP);
        mMapManager.BAIDU_MAP.setOnMarkerClickListener(mOverlay);
        mOverlay.setData(result);
        mOverlay.addToMap();
        mOverlay.zoomToSpan();
        return;
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
}
