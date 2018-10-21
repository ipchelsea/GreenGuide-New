package com.guide.green.green_guide.Utilities;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.guide.green.green_guide.Fragments.BtmSheetPoiResultPage;
import com.guide.green.green_guide.PoiOverlay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FragPager extends FragmentStatePagerAdapter implements OnGetPoiSearchResultListener, BtmSheetPoiResultPage.Clicked {
    private class PagerPoiOverlay extends PoiOverlay {
        public PagerPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            if (mPoiClickHandler != null) {
                mPoiClickHandler.onPoiSelected(poi);
            }
            return true;
        }
    }
    public interface PoiResultLoaded {
        void onResult(PoiOverlay overlay, int pageNumber);
    }
    public interface PoiSelected {
        void onPoiSelected(PoiInfo poi);
    }

    private int mTotalPages;
    private BaiduMap mBaiduMap;
    private PoiSearch mPoiSearch;
    private PoiCitySearchOption query;
    private Map<Integer, BtmSheetPoiResultPage> mLoadingPages = new LinkedHashMap<>();
    private List<PoiOverlay> mPoiOverlay = new ArrayList<>();
    private LayoutInflater mInflater;
    private PoiResultLoaded mPoiResultLoaded;
    private PoiSelected mPoiClickHandler;

    @Override
    public void onClicked(PoiInfo poi) {
        if (mPoiClickHandler != null) {
            mPoiClickHandler.onPoiSelected(poi);
        }
    }

    public FragPager(AppCompatActivity act, BaiduMapManager mapManager, PoiCitySearchOption query) {
        super(act.getSupportFragmentManager());
        this.query = query;
        mBaiduMap = mapManager.BAIDU_MAP;
        mPoiSearch = mapManager.POI_SEARCH;
        mInflater = act.getLayoutInflater();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
    }

    public PoiOverlay getPoiOverlay(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < mPoiOverlay.size()) {
            return mPoiOverlay.get(pageNumber);
        }
        return null;
    }

    public void setPoiResultLoaded(PoiResultLoaded handler) {
        mPoiResultLoaded = handler;
    }
    public void setPoiClickHandler(PoiSelected handler) {
        mPoiClickHandler = handler;
    }

    private void setTotalPages(int count) {
        if (count != mTotalPages) {
            mTotalPages = count;
            notifyDataSetChanged();
        }
    }

    private boolean hasPoiResultInCache(int pageNumber) {
        return (pageNumber < mPoiOverlay.size() && mPoiOverlay.get(pageNumber) != null);
    }

    private boolean makePoiQuery(int pageNumber) {
        boolean makeQuery = !hasPoiResultInCache(pageNumber);
        if (makeQuery) {
            mPoiSearch.searchInCity(query.pageNum(pageNumber));
        }
        return makeQuery;
    }

    private void addPoiResultsToPage(BtmSheetPoiResultPage frag, int page) {
        if (hasPoiResultInCache(page)) {
            frag.addResultData(mPoiOverlay.get(page).getPoiResult());
            if (mPoiResultLoaded != null) {
                mPoiResultLoaded.onResult(mPoiOverlay.get(page), page);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        BtmSheetPoiResultPage poiResultFrag = BtmSheetPoiResultPage.newInstance(position);

        if (makePoiQuery(position)) {
            // Add to the queue of pages waiting for data to update their layouts with.
            mLoadingPages.put(position, poiResultFrag);
        }

        if (position + 1 < getCount()) {
            makePoiQuery(position + 1);
        }

        poiResultFrag.setViewCreatedHandler(new BtmSheetPoiResultPage.ViewCreated() {
            @Override
            public void onCreateView(BtmSheetPoiResultPage frag, int pos) {
                // Its data is ready, update its layout with it
                addPoiResultsToPage(frag, pos);
            }
        });

        poiResultFrag.setViewClickedHandler(this);
        return poiResultFrag;
    }

    @Override
    public String getPageTitle(int position) {
        return "Page " + position;
    }

    @Override
    public int getCount() {
        return mTotalPages == 0 ? 1 : mTotalPages;
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null) {
            Log.e("Poi Search Result Error", "UNKNOWN ERROR, poiResult == null");
            return;
        } else if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Log.e("Poi Search Result Error", result.error.toString());
            makePoiQuery(result.getCurrentPageNum());
            return;
        }
        setTotalPages(result.getTotalPageNum());
        PagerPoiOverlay mOverlay = new PagerPoiOverlay(mBaiduMap);
        mBaiduMap.setOnMarkerClickListener(mOverlay);
        mOverlay.setData(result);
        int pageNumber = result.getCurrentPageNum();
        synchronized (mPoiOverlay) {
            while (mPoiOverlay.size() <= result.getCurrentPageNum()) {
                mPoiOverlay.add(null);
            }
            mPoiOverlay.set(pageNumber, mOverlay);
        }

        BtmSheetPoiResultPage frag = null;
        synchronized (mLoadingPages) {
            frag = mLoadingPages.remove(new Integer(pageNumber));
        }
        if (frag != null) {
            addPoiResultsToPage(frag, pageNumber);
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        /* Do nothing */
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
        /* Do nothing */
    }
}