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
import com.guide.green.green_guide.Utilities.Review.AirWaste;
import com.guide.green.green_guide.WriteReviewActivity;

import java.util.HashMap;

public class WriteReviewAirFragment extends WriteReviewActivity.WriteReviewPage {
    private OnPageChangeListener mOnPageChangeListener;
    private ViewGroup mViewRoot;
    private AirWaste mAirWaste;

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
        mViewRoot = (ViewGroup) inflater.inflate(R.layout.fragment_write_air, null);
        mViewRoot.findViewById(R.id.write_review_air_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageChange(PageDirection.NEXT);
                }
            }
        });
        mViewRoot.findViewById(R.id.write_review_air_prev).setOnClickListener(new View.OnClickListener() {
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

    public void setAirWasteObject(AirWaste airWate) {
        mAirWaste = airWate;
        if (mViewRoot != null) {
            bindViews();
        }
    }

    /**
     * Called when the root view and the review category are available.
     */
    private void bindViews() {
        if (mAirWaste == null || mViewRoot == null) {
            return;
        }

        String otherValue = getContext().getResources().getString(R.string.list_item_other);

        // Other odor "forward deceleration"
        FormInput.TextInput otherOdor = new FormInput.TextInput((TextView) mViewRoot.findViewById(
                R.id.write_review_air_odor_other), AirWaste.Key.ODOR_OTHER, mAirWaste);

        // Odor type selector
        Spinner odorView = mViewRoot.findViewById(R.id.write_review_air_odor);
        FormInput.addAdapterToSpinner(getContext(), odorView,
                R.array.write_review_odor_dropdown_items);
        new FormInput.DropDown(odorView, AirWaste.Key.ODOR, mAirWaste, otherOdor, otherValue);

        // Any Smoke other "forward deceleration" * 2
        FormInput.TextInput otherSmokeColor = new FormInput.TextInput((TextView)
                mViewRoot.findViewById(R.id.write_review_air_smoke_color_other),
                AirWaste.Key.SMOKE_COLOR_OTHER, mAirWaste);

        // Smoke Color type "forward deceleration"
        Spinner smokeColorView = mViewRoot.findViewById(R.id.write_review_air_smoke_color);
        View[] colorGroup = new View[] {mViewRoot.findViewById(
                R.id.write_review_air_smoke_color_lable)};
        FormInput.addAdapterToSpinner(getContext(), smokeColorView,
                R.array.write_review_odor_dropdown_items);
        FormInput.DropDown smokeColor = new FormInput.DropDown(smokeColorView,
                AirWaste.Key.SMOKE_COLOR, mAirWaste, otherSmokeColor, otherValue, colorGroup);

        // Any Smoke (Y/N)
        RadioButton smokeY = mViewRoot.findViewById(R.id.write_review_air_has_smoke_yes);
        new FormInput.RadioBtn((RadioGroup) mViewRoot.findViewById(R.id.write_review_air_has_smoke),
                AirWaste.Key.SMOKE_CHECK, mAirWaste, smokeColor, smokeY.getText().toString());

        // Physical Discomfort from air other "forward deceleration" * 2
        FormInput.TextInput physicalDiscomfortOther = new FormInput.TextInput((TextView)
                mViewRoot.findViewById(R.id.write_review_air_discomfort_other),null, mAirWaste);

        // Physical Discomfort type "forward deceleration"
        Spinner physicalDiscomfort = mViewRoot.findViewById(R.id.write_review_air_discomfort);
        View[] discGroup = new View[] {mViewRoot.findViewById(
                R.id.write_review_air_discomfort_lable)};
        FormInput.addAdapterToSpinner(getContext(), physicalDiscomfort,
                R.array.write_review_air_uncomfortable_dropdown_items);
        FormInput.DropDown discomfortType = new FormInput.DropDown(physicalDiscomfort,
                AirWaste.Key.SYMPTOM, mAirWaste, physicalDiscomfortOther, otherValue, discGroup);

        // Any Physical Discomfort from Air (Y/N)
        RadioButton probY = mViewRoot.findViewById(R.id.write_review_air_causes_physical_probs_yes);
        RadioGroup hasProbs = mViewRoot.findViewById(R.id.write_review_air_causes_physical_probs);
        new FormInput.YNRadioBtn(hasProbs, AirWaste.Key.PHYSICAL_PROBS, mAirWaste, discomfortType,
                probY.getText().toString());

        // Parameter measurements
        HashMap<Integer, Review.Key> textViews = new HashMap<>();
        textViews.put(R.id.write_review_air_pm2_5, AirWaste.Key.PM2_5);
        textViews.put(R.id.write_review_air_pm10, AirWaste.Key.PM10);
        textViews.put(R.id.write_review_air_o3, AirWaste.Key.O3);
        textViews.put(R.id.write_review_air_sox, AirWaste.Key.SOX);
        textViews.put(R.id.write_review_air_nox, AirWaste.Key.NOX);
        textViews.put(R.id.write_review_air_co, AirWaste.Key.CO);
        for (int viewId : textViews.keySet()) {
            new FormInput.TextInput((TextView) mViewRoot.findViewById(viewId),
                    textViews.get(viewId), mAirWaste);
        }
    }
}
