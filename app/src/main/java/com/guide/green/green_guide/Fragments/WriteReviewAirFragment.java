package com.guide.green.green_guide.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.guide.green.green_guide.WriteReviewActivity.WriteReviewPage;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.WriteReviewActivity;

public class WriteReviewAirFragment extends WriteReviewActivity.WriteReviewPage {
    private OnPageChangeListener mOnPageChangeListener;
    @Override
    public void setOnPageChange(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.fragment_write_air, null);
        parent.findViewById(R.id.air_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageChange(PageDirection.NEXT);
                }
            }
        });
        parent.findViewById(R.id.air_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageChange(PageDirection.PREVIOUS);
                }
            }
        });
        return parent;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
