package com.guide.green.green_guide.Utilities;

import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import com.guide.green.green_guide.PoiOverlay;

public class PageChangeListener implements ViewPager.OnPageChangeListener,
        FragPager.PoiResultLoaded {
    private int mLastPage;
    private int mCurrentPage;
    private FragPager mFragPager;

    public void remove() {
        PoiOverlay overlay = mFragPager.getPoiOverlay(mCurrentPage);
        if (overlay != null) {
            overlay.removeFromMap();
        }
    }

    public PageChangeListener(@NonNull FragPager fragPager) {
        this.mFragPager = fragPager;
        fragPager.setPoiResultLoaded(this);
    }

    @Override
    public void onResult(PoiOverlay overlay, int pageNumber) {
        if (pageNumber == mCurrentPage) {
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /* Do nothing */
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentPage = position;
        if (mLastPage != mCurrentPage) {
            PoiOverlay lastOverlay = mFragPager.getPoiOverlay(mLastPage);
            if (lastOverlay != null) {
                lastOverlay.removeFromMap();
            }
        }
        PoiOverlay overlay = mFragPager.getPoiOverlay(mCurrentPage);
        if (overlay != null) {
            overlay.addToMap();
            overlay.zoomToSpan();
        }
        mLastPage = mCurrentPage;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        /* Do nothing */
    }
}