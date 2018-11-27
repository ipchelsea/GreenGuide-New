package com.guide.green.green_guide.Fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.guide.green.green_guide.R;

/**
 * A fragment which represents a single image in a review images carousel.
 */
public class CarouselPicture extends Fragment {
    private OnItemClickedListener mViewClickedListener;
    private OnRetryListener mRetry;
    private Bitmap mPendingBitmap;
    private ViewGroup mRoot;
    private ImageView mImg;
    private int mPageNum;

    /**
     * Callback interface invoked when the retry button is pressed. Used to allow all of the
     * image requests to take place in the PagerAdapter.
     */
    public interface OnRetryListener {
        /**
         * Is called when the users clicked the retry button. The retry button is available when
         * an error is encountered and {@code onErrorEncountered} is called with the appropriate
         * arguments.
         *
         * @param pageNumber The page that the the clicked button is in.
         */
        void onRetry(int pageNumber);
    }

    /**
     * Callback interface called when the displayed image is clicked.
     */
    public interface OnItemClickedListener {
        /**
         * A callback called when one of the items of this page is clicked.
         * @param pageNumber The page that the the clicked button is in.
         */
        void onClicked(int pageNumber);
    }

    // Setters
    public void setOnRetrySearchListener(OnRetryListener listener) {
        mRetry = listener;
    }
    public void setOnItemClickedListener(OnItemClickedListener listener) {
        mViewClickedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle bundle) {
        super.onCreate(bundle);
        mPageNum = getArguments().getInt("pageNumber", -1);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_carousel_picture,
                container, false);
        mImg = mRoot.findViewById(R.id.carousel_img);
        mImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewClickedListener != null) {
                    mViewClickedListener.onClicked(mPageNum);
                }
            }
        });
        mRoot.findViewById(R.id.carousel_img_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRetry != null) {
                    mRetry.onRetry(mPageNum);
                }
            }
        });
        if (mPendingBitmap != null) {
            addResultData(mPendingBitmap);
            mPendingBitmap = null;
        }
        return mRoot;
    }

    /**
     * Hides all of the child views of the {@code mRoot} and only displays the view with an id
     * matching the supplied value.
     *
     * @param id the id of the view to make/keep visible.
     */
    private void showOnly(@IdRes int id) {
        for (int i = mRoot.getChildCount() - 1; i >= 0; i--) {
            View child = mRoot.getChildAt(i);
            if (id == child.getId()) {
                child.setVisibility(View.VISIBLE);
            } else {
                child.setVisibility(View.GONE);
            }
        }
    }

    /***
     * Must be called after {@code onCreateView} has already been called because it uses the
     * an item in the inflated layout.
     *
     * @bmp the image to display for this page.
     */
    public void addResultData(@NonNull Bitmap bmp) {
        if (mRoot == null) {
            mPendingBitmap = bmp;
        } else {
            mImg.setImageBitmap(bmp);
            showOnly(R.id.carousel_img);
        }
    }

    /**
     * Hides the loading bar and asks the user if they want to retry loading the data.
     */
    public void onErrorEncountered() {
        showOnly(R.id.carousel_img_retry);
    }

    /**
     * Creates a result page fragment associated with the specified page number.
     * This page number is used as a return value in a callback to identify this fragment.
     *
     * @param pageNumber A number equal to or greater than 0.
     * @return New fragment representing the specified {@code pageNumber}.
     */
    public static CarouselPicture newInstance(int pageNumber) {
        CarouselPicture frag = new CarouselPicture();
        Bundle b = new Bundle();
        b.putInt("pageNumber", pageNumber);
        frag.setArguments(b);
        return frag;
    }
}

