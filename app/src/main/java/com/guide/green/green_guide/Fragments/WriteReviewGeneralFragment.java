package com.guide.green.green_guide.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.Review;
import com.guide.green.green_guide.WriteReviewActivity;

import java.util.ArrayList;

public class WriteReviewGeneralFragment extends WriteReviewActivity.WriteReviewPage {
    private OnPageChangeListener mOnPageChangeListener;
    private View.OnClickListener mOnUploadImagesClicked;
    private ViewGroup mImagesContainer;
    private ArrayList<Uri> mImageUris = new ArrayList<>();
    private Review.Location mLocationObj;
    private ViewGroup mParentRoot;

    public Uri[] getImageUris() {
        Uri[] uris = new Uri[mImageUris.size()];
        return mImageUris.toArray(uris);
    }

    @Override
    public void setOnPageChange(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    public void setOnUploadImagesClicked(View.OnClickListener listener) {
        mOnUploadImagesClicked = listener;
    }

    public void setLocationObject(Review.Location locationObject) {
        mLocationObj = locationObject;
        if (mParentRoot != null) {
            setPrepopulateValues();
        }
    }

    private void setPrepopulateValues() {
        if (mLocationObj == null) {
            return;
        }

        if (mLocationObj.get(Review.Location.Key.COMPANY) != null) {
            EditText coName = mParentRoot.findViewById(R.id.write_review_co_name);
            coName.setText(mLocationObj.get(Review.Location.Key.COMPANY));
            coName.setEnabled(false);
        }

        if (mLocationObj.get(Review.Location.Key.ADDRESS) != null) {
            EditText coAddress = mParentRoot.findViewById(R.id.write_review_co_address);
            coAddress.setText(mLocationObj.get(Review.Location.Key.ADDRESS));
            coAddress.setEnabled(false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mParentRoot = (ViewGroup) inflater.inflate(R.layout.fragment_write_general, null);
        mParentRoot.findViewById(R.id.gen_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageChange(PageDirection.NEXT);
                }
            }
        });
        mParentRoot.findViewById(R.id.img_up_but).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnUploadImagesClicked != null) {
                    mOnUploadImagesClicked.onClick(view);
                }
            }
        });
        mImagesContainer = mParentRoot.findViewById(R.id.selected_Images_Container);
        setPrepopulateValues();
        return mParentRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void clearUploadImages() {
        if (mImagesContainer != null) {
            mImagesContainer.removeAllViews();
            mImageUris.clear();
        }
    }

    public void addUploadImage(final Uri imgUri) {
        if (mImagesContainer == null) {
            return;
        }

        final ViewGroup parent = (ViewGroup) LayoutInflater.from(getContext()).inflate(
                R.layout.write_review_selected_image_item, null);
        ImageView imgView = parent.findViewById(R.id.selected_image);
        ImageButton removeImgBtn = parent.findViewById(R.id.remove_selected_image);

        imgView.setImageURI(imgUri);
        removeImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mImageUris.remove(imgUri);
                mImagesContainer.removeView(parent);
            }
        });

        mImageUris.add(imgUri);
        mImagesContainer.addView(parent);

        ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        parent.setLayoutParams(layoutParams);
    }
}
