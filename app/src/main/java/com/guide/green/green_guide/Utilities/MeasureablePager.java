package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class MeasureablePager extends android.support.v4.view.ViewPager {
    public ArrayList<View> useToMeasure = new ArrayList<>();

    public MeasureablePager(@NonNull Context context) {
        this(context, null);
    }

    public MeasureablePager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private int getChildHeight(View child, int childFillParentWidth) {
        if (child.getVisibility() == GONE) {
            return 0;
        }

        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (lp == null) {
            return 0;
        }

        int childHeightMeasureSpec = MeasureSpec.UNSPECIFIED;
        int widthMode = MeasureSpec.AT_MOST;

        if (lp.width != ViewGroup.LayoutParams.WRAP_CONTENT) {
            widthMode = MeasureSpec.EXACTLY;
            if (lp.width != ViewGroup.LayoutParams.MATCH_PARENT) {
                childFillParentWidth = lp.width;
            }
        }

        if (lp.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.height,
                    MeasureSpec.EXACTLY);
        }

        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childFillParentWidth, widthMode);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        return child.getMeasuredHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getPaddingBottom() + getPaddingTop();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int childWidthSize = width - getPaddingLeft() - getPaddingRight();

        int childStartIndex = 0;
        int size = getChildCount();
        if (size > 0) {
            View child = getChildAt(0);
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp != null && lp.isDecor) {
                height += getChildHeight(child, childWidthSize);
                childStartIndex = 1;
            }
        }

        int maxHeight = 0;
        for (int i = childStartIndex; i < size; i++) {
            maxHeight = Math.max(maxHeight, getChildHeight(getChildAt(i), childWidthSize));
        }
        height += maxHeight;

        setMeasuredDimension(width, height);
    }
}
