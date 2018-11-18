package com.guide.green.green_guide.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.FormInput;
import com.guide.green.green_guide.Utilities.Review;
import com.guide.green.green_guide.WriteReviewActivity;

public class WriteReviewSolidFragment extends WriteReviewActivity.WriteReviewPage {
    private OnPageChangeListener mOnPageChangeListener;
    private ViewGroup mViewRoot;
    private Review.SolidWaste mSolidWate;

    @Override
    public void setOnPageChange(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    @Override
    public TextView getPageNumberTextView() {
        if (mViewRoot != null) {
            return mViewRoot.findViewById(R.id.write_review_page_number);
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewRoot = (ViewGroup) inflater.inflate(R.layout.fragment_write_solid, null);
        mViewRoot.findViewById(R.id.write_review_solid_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageChange(PageDirection.NEXT);
                }
            }
        });
        mViewRoot.findViewById(R.id.write_review_solid_prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageChange(PageDirection.PREVIOUS);
                }
            }
        });
        bindViews();
        displayPageNumber();
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setSolidWasteObject(Review.SolidWaste locationObject) {
        mSolidWate = locationObject;
        if (mViewRoot != null) {
            bindViews();
        }
    }

    /**
     * Called when the root view and the review category are available.
     */
    private void bindViews() {
        if (mSolidWate == null || mViewRoot == null) {
            return;
        }
        String otherValue = getContext().getResources().getString(R.string.list_item_other);

        // Waste type other "forward deceleration"
        FormInput.TextInput wasteTypeOther = new FormInput.TextInput((TextView)
                mViewRoot.findViewById(R.id.write_review_solid_waste_type_other),
                Review.SolidWaste.Key.WASTE_TYPE_OTHER, mSolidWate);

        // Waste type selector
        Spinner wateTypeView = mViewRoot.findViewById(R.id.write_review_solid_waste_type);
        FormInput.addAdapterToSpinner(getContext(), wateTypeView,
                R.array.write_review_solid_waste_type_dropdown_items);
        new FormInput.DropDown(wateTypeView, Review.SolidWaste.Key.WASTE_TYPE, mSolidWate,
                wasteTypeOther, otherValue);

        // Waste amount
        Spinner wateAmountView = mViewRoot.findViewById(R.id.write_review_solid_waste_amount);
        FormInput.addAdapterToSpinner(getContext(), wateAmountView,
                R.array.write_review_solid_waste_amount_dropdown_items);
        new FormInput.DropDown(wateAmountView, Review.SolidWaste.Key.AMOUNT, mSolidWate);

        // Waste odor other "forward deceleration"
        FormInput.TextInput odorTypeOther = new FormInput.TextInput((TextView)
                mViewRoot.findViewById(R.id.write_review_solid_odor_other),
                Review.SolidWaste.Key.ODOR_OTHER, mSolidWate);

        // Waste odor selector
        Spinner odorTypeView = mViewRoot.findViewById(R.id.write_review_solid_odor);
        FormInput.addAdapterToSpinner(getContext(), odorTypeView,
                R.array.write_review_odor_dropdown_items);
        new FormInput.DropDown(odorTypeView, Review.SolidWaste.Key.ODOR, mSolidWate,
                odorTypeOther, otherValue);

        // Waste miscellaneous & measurements
        new FormInput.TextInput((TextView) mViewRoot.findViewById(R.id.write_review_solid_misc),
                Review.SolidWaste.Key.MEASUREMENTS, mSolidWate);
    }
}
