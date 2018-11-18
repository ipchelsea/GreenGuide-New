package com.guide.green.green_guide.Fragments;

//// Is This deprecated?
//NEWS = new Key("news");
//
//// NO PHP Code For:
//        write_review_gen_observation_date
//        write_review_gen_observation_time
//        write_review_gen_weather_cond

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.FormInput;
import com.guide.green.green_guide.Utilities.Review;
import com.guide.green.green_guide.WriteReviewActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class WriteReviewGeneralFragment extends WriteReviewActivity.WriteReviewPage {
    private OnPageChangeListener mOnPageChangeListener;
    private View.OnClickListener mOnUploadImagesClicked;
    private ViewGroup mImagesContainer;
    private ArrayList<Uri> mImageUris = new ArrayList<>();
    private Review.Location mLocation;
    private ViewGroup mViewRoot;

    public Uri[] getImageUris() {
        Uri[] uris = new Uri[mImageUris.size()];
        return mImageUris.toArray(uris);
    }

    @Override
    public TextView getPageNumberTextView() {
        if (mViewRoot != null) {
            return mViewRoot.findViewById(R.id.write_review_page_number);
        }
        return null;
    }

    @Override
    public void setOnPageChange(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewRoot = (ViewGroup) inflater.inflate(R.layout.fragment_write_general, null);
        mViewRoot.findViewById(R.id.write_review_gen_next).setOnClickListener(
                new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnPageChangeListener != null) {
                        mOnPageChangeListener.onPageChange(PageDirection.NEXT);
                    }
                }
            });
        mViewRoot.findViewById(R.id.write_review_gen_image_upload_btn).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnUploadImagesClicked != null) {
                        mOnUploadImagesClicked.onClick(view);
                    }
                }
            });
        mImagesContainer = mViewRoot.findViewById(R.id.write_review_gen_selected_images_container);
        bindViews();
        displayPageNumber();
        return mViewRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void setLocationObject(Review.Location locationObject) {
        mLocation = locationObject;
        if (mViewRoot != null) {
            bindViews();
        }
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

    public void setOnUploadImagesClicked(View.OnClickListener listener) {
        mOnUploadImagesClicked = listener;
    }

    /**
     * Called when the root view and the review category are available.
     */
    private void bindViews() {
        if (mLocation == null || mViewRoot == null) { return; }

        if (mLocation.get(Review.Location.Key.LNG) != null) {
            EditText coName = mViewRoot.findViewById(R.id.write_review_gen_lng);
            coName.setEnabled(false);
        }

        if (mLocation.get(Review.Location.Key.LAT) != null) {
            EditText coName = mViewRoot.findViewById(R.id.write_review_gen_lat);
            coName.setEnabled(false);
        }

        if (mLocation.get(Review.Location.Key.COMPANY) != null) {
            EditText coName = mViewRoot.findViewById(R.id.write_review_gen_co_name);
            coName.setEnabled(false);
        }

        if (mLocation.get(Review.Location.Key.ADDRESS) != null) {
            EditText coAddress = mViewRoot.findViewById(R.id.write_review_gen_co_address);
            coAddress.setEnabled(false);
        }

        if (mLocation.get(Review.Location.Key.CITY) != null) {
            EditText coName = mViewRoot.findViewById(R.id.write_review_gen_co_city);
            coName.setEnabled(false);
        }

        HashMap<Integer, Review.Key> checkBoxes = new HashMap<>();
        HashMap<Integer, Review.Key> textViews = new HashMap<>();

        textViews.put(R.id.write_review_gen_lat, Review.Location.Key.LAT);
        textViews.put(R.id.write_review_gen_lng, Review.Location.Key.LNG);
        textViews.put(R.id.write_review_gen_co_name, Review.Location.Key.COMPANY);
        textViews.put(R.id.write_review_gen_co_address, Review.Location.Key.ADDRESS);
        textViews.put(R.id.write_review_gen_co_city, Review.Location.Key.CITY);
        textViews.put(R.id.write_review_gen_co_industry_category, Review.Location.Key.INDUSTRY);
        textViews.put(R.id.write_review_gen_co_products, Review.Location.Key.PRODUCT);
        textViews.put(R.id.write_review_gen_observation_date, Review.Location.Key.OBSERVATION_DATE);
        textViews.put(R.id.write_review_gen_observation_time, Review.Location.Key.OBSERVATION_TIME);

        // Weather Condition
        Spinner weather = mViewRoot.findViewById(R.id.write_review_gen_weather_cond);
        ArrayAdapter<CharSequence> weatherAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.write_review_gen_weather_conditions_dropdown_items,
                R.layout.write_review_spinner_item);
        weatherAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weather.setAdapter(weatherAdapter);
        weatherAdapter.notifyDataSetChanged();
        new FormInput.DropDown(weather, Review.Location.Key.WEATHER, mLocation);

        // Rating
        RadioGroup rating = mViewRoot.findViewById(R.id.write_review_gen_rating);
        new FormInput.RadioBtn(rating, Review.Location.Key.RATING, mLocation);

        // Review (user experience)
        textViews.put(R.id.write_review_gen_written_experience, Review.Location.Key.REVIEW);

        // Checkboxes
        checkBoxes.put(R.id.write_review_gen_focus_water, Review.Location.Key.WATER);
        checkBoxes.put(R.id.write_review_gen_focus_air, Review.Location.Key.AIR);
        checkBoxes.put(R.id.write_review_gen_focus_waste, Review.Location.Key.WASTE);
        checkBoxes.put(R.id.write_review_gen_focus_land, Review.Location.Key.LAND);
        checkBoxes.put(R.id.write_review_gen_focus_ecosystem, Review.Location.Key.LIVING);

        // Write Review Other Item "forward deceleration"
        TextView reviewFocusOtherView = mViewRoot.findViewById(R.id.write_review_gen_focus_other_item);
        FormInput.TextInput reviewFocusOther = new FormInput.TextInput(reviewFocusOtherView,
                Review.Location.Key.OTHER_ITEM, mLocation);

        // Write review others Checkbox
        CheckBox reviewFocusView = mViewRoot.findViewById(R.id.write_review_gen_focus_other);
        new FormInput.CheckBoxInput(reviewFocusView,
                Review.Location.Key.OTHER, mLocation, reviewFocusOther);

        for (int viewId : textViews.keySet()) {
            new FormInput.TextInput((TextView) mViewRoot.findViewById(viewId),
                    textViews.get(viewId), mLocation);
        }

        for (int viewId : checkBoxes.keySet()) {
            new FormInput.CheckBoxInput((CheckBox) mViewRoot.findViewById(viewId),
                    checkBoxes.get(viewId), mLocation);
        }
    }
}