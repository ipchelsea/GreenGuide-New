package com.guide.green.green_guide.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.FormInput;
import com.guide.green.green_guide.Utilities.Review;
import com.guide.green.green_guide.Utilities.Review.WaterIssue;
import com.guide.green.green_guide.WriteReviewActivity;

import java.util.HashMap;

public class WriteReviewWaterFragment extends WriteReviewActivity.WriteReviewPage {
    private OnPageChangeListener mOnPageChangeListener;
    private ViewGroup mViewRoot;
    private WaterIssue mWaterIssue;

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
        mViewRoot = (ViewGroup) inflater.inflate(R.layout.fragment_write_water, null);
        mViewRoot.findViewById(R.id.write_review_water_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageChange(PageDirection.NEXT);
                }
            }
        });
        mViewRoot.findViewById(R.id.write_review_water_prev).setOnClickListener(new View.OnClickListener() {
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

    public void setWaterIssueObject(WaterIssue waterIssue) {
        mWaterIssue = waterIssue;
        if (mViewRoot != null) {
            bindViews();
        }
    }

    /**
     * Called when the root view and the review category are available.
     */
    private void bindViews() {
        if (mWaterIssue == null || mViewRoot == null) {
            return;
        }

        String otherValue = getContext().getResources().getString(R.string.list_item_other);

        // Water body other "forward deceleration"
        FormInput.TextInput bodyOther = new FormInput.TextInput((TextView) mViewRoot.findViewById
                (R.id.write_review_water_body_other),
                        WaterIssue.Key.WATER_BODY_OTHER, mWaterIssue);

        // Water body type selector
        Spinner waterBodyView = mViewRoot.findViewById(R.id.write_review_water_body);
        FormInput.addAdapterToSpinner(getContext(), waterBodyView,
                R.array.write_review_water_bodies_dropdown_items);
        new FormInput.DropDown(waterBodyView, WaterIssue.Key.WATER_BODY, mWaterIssue, bodyOther,
                otherValue);

        // Water color other "forward deceleration"
        FormInput.TextInput colorOther = new FormInput.TextInput((TextView)
                mViewRoot.findViewById(R.id.write_review_water_color_other),
                WaterIssue.Key.WATER_COLOR_OTHER, mWaterIssue);

        // Water color selector
        Spinner waterColorView = mViewRoot.findViewById(R.id.write_review_water_color);
        FormInput.addAdapterToSpinner(getContext(), waterColorView,
                R.array.write_review_colors_dropdown_items);
        new FormInput.DropDown(waterColorView, WaterIssue.Key.WATER_COLOR, mWaterIssue,
                colorOther, otherValue);

        // Water turbidity selector
        Spinner turbidityView = mViewRoot.findViewById(R.id.write_review_water_turbidity);
        FormInput.addAdapterToSpinner(getContext(), turbidityView,
                R.array.write_review_water_turbidity);
        new FormInput.DropDown(turbidityView, WaterIssue.Key.TURB_SCORE, mWaterIssue);

        // Water oder "forward deceleration"
        FormInput.TextInput odorOther = new FormInput.TextInput((TextView)
                mViewRoot.findViewById(R.id.write_review_water_odor_other),
                WaterIssue.Key.ODOR_OTHER, mWaterIssue);

        // Water color selector
        Spinner odorView = mViewRoot.findViewById(R.id.write_review_water_odor);
        FormInput.addAdapterToSpinner(getContext(), odorView,
                R.array.write_review_odor_dropdown_items);
        new FormInput.DropDown(odorView, WaterIssue.Key.ODOR, mWaterIssue, odorOther, otherValue);

        // Float type other "forward deceleration" * 2
        FormInput.TextInput floatTypeOther = new FormInput.TextInput((TextView)
                mViewRoot.findViewById(R.id.write_review_water_float_type_other),
                WaterIssue.Key.FLOAT_TYPE_OTHER, mWaterIssue);

        // Float type "forward deceleration"
        Spinner floatTypeView = mViewRoot.findViewById(R.id.write_review_water_float_type);
        View[] floatTypeGroup = new View[] {mViewRoot.findViewById(
                R.id.write_review_water_float_type_label)};
        FormInput.addAdapterToSpinner(getContext(), floatTypeView,
                R.array.write_review_water_float_type_dropdown_items);
        FormInput.DropDown floatType = new FormInput.DropDown(floatTypeView,
                WaterIssue.Key.FLOAT_TYPE, mWaterIssue, floatTypeOther, otherValue, floatTypeGroup);

        // Any Float (Y/N)
        RadioButton floatY = mViewRoot.findViewById(R.id.write_review_water_has_float_yes);
        RadioGroup floatYNView = mViewRoot.findViewById(R.id.write_review_water_has_float);
        new FormInput.YNRadioBtn(floatYNView, WaterIssue.Key.CHECK_FLOAT, mWaterIssue, floatType,
                floatY.getText().toString());

        // Parameter measurements
        HashMap<Integer, Review.Key> textViews = new HashMap<>();
        textViews.put(R.id.write_review_water_measured_dissolved_oxygen,
                WaterIssue.Key.DISSOLVED_OXYGEN);
        textViews.put(R.id.write_review_water_measured_ph, WaterIssue.Key.PH);
        textViews.put(R.id.write_review_water_measured_turbidity, WaterIssue.Key.TURB_PARAMS);
        textViews.put(R.id.write_review_water_measured_bio_oxygen_demand,
                WaterIssue.Key.BIO_OXYGEN_DEMAND);
        textViews.put(R.id.write_review_water_measured_chem_oxygen_demand,
                WaterIssue.Key.CHEM_OXYGEN_DEMAND);
        textViews.put(R.id.write_review_water_measured_organic_carbon,
                WaterIssue.Key.ORGANIC_CARBON);
        textViews.put(R.id.write_review_water_measured_solid, WaterIssue.Key.SOLID);
        textViews.put(R.id.write_review_water_measured_ammonium, WaterIssue.Key.AMMONIUM);
        textViews.put(R.id.write_review_water_measured_phosphorus, WaterIssue.Key.PHOSPHORUS);
        textViews.put(R.id.write_review_water_measured_nitrate, WaterIssue.Key.NITRATE);
        textViews.put(R.id.write_review_water_measured_mercury, WaterIssue.Key.MERCURY);
        textViews.put(R.id.write_review_water_measured_lead, WaterIssue.Key.LEAD);
        textViews.put(R.id.write_review_water_measured_cadmium, WaterIssue.Key.CADMIUM);
        textViews.put(R.id.write_review_water_measured_arsenic, WaterIssue.Key.ARSENIC);
        for (int viewId : textViews.keySet()) {
            new FormInput.TextInput((TextView) mViewRoot.findViewById(viewId),
                    textViews.get(viewId), mWaterIssue);
        }
    }
}
