package com.guide.green.green_guide.Fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.guide.green.green_guide.R;

public class SingleImageFragment extends Fragment {
    private ImageView mImgView;
    private Drawable mImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mImgView = (ImageView) inflater.inflate(R.layout.fragment_single_image, container, false);
        setImage(mImage);
        return mImgView;
    }

    public void setImage(Drawable img) {
        mImage = img;
        if (mImgView != null && mImgView != null) {
            mImgView.setImageDrawable(mImage);
        }
    }
}
