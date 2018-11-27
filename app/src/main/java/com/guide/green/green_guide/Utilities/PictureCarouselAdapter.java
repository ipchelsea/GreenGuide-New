package com.guide.green.green_guide.Utilities;


import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import com.guide.green.green_guide.Fragments.CarouselPicture;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;

import java.util.List;

public class PictureCarouselAdapter extends FragmentStatePagerAdapter
        implements OnPageChangeListener {
    private List<String> mImageUrls;
    private Bitmap mBmps[];

    public PictureCarouselAdapter(FragmentManager fm, @NonNull List<String> imgUrls) {
        super(fm);
        mImageUrls = imgUrls;
        mBmps = new Bitmap[mImageUrls.size()];
    }

    /**
     * Return the Fragment associated with a specified position.
     *
     * @param position
     */
    @Override
    public Fragment getItem(int position) {
        Log.i("getItem", "New: " + position);
        CarouselPicture cPic = CarouselPicture.newInstance(position);
        getImage(position, cPic);
        return cPic;
    }

    /**
     * Return the number of views available.
     */
    @Override
    public int getCount() {
        return mImageUrls.size();
    }

    /**
     * This method will be invoked when a new page becomes selected. Animation is not
     * necessarily complete.
     *
     * @param position Position index of the new selected page.
     */
    @Override
    public void onPageSelected(int position) {
//        getImage(position);
    }

    private void getImage(final int position, final CarouselPicture cPic) {
        if (mBmps[position] != null) {
            Log.i("--getImage", "Reuse: " + position);
            cPic.addResultData(mBmps[position]);
        } else {
            Log.i("--getImage", "GetNew: " + position);
            AsyncRequest.getImage("http://www.lovegreenguide.com/" + mImageUrls.get(position),
                    new AbstractRequest.OnRequestResultsListener<Bitmap>() {
                        @Override
                        public void onSuccess(Bitmap bitmap) {
                            mBmps[position] = bitmap;
                            cPic.addResultData(bitmap);
                            Log.i("--onSuccess_CarouselPic", "Bmp>" + position);
                        }
                        @Override
                        public void onError(Exception error) {
                            cPic.onErrorEncountered();
                            Log.i("--onError_CarouselPic", error.toString());
                            error.printStackTrace();
                        }
                    });
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        /* Do Nothing */
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /* Do Nothing */
    }
}
