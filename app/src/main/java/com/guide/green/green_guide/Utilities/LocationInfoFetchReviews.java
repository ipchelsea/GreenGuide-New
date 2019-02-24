package com.guide.green.green_guide.Utilities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.map.Overlay;
import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.OnRequestResultsListener;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.RequestProgress;
import com.guide.green.green_guide.LogInOutSignUpActivity;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.AsyncGetReview;
import com.guide.green.green_guide.ViewOneReview;
import com.guide.green.green_guide.ViewOneReviewFragment;
import com.guide.green.green_guide.WriteReviewActivity;

import java.util.ArrayList;
import java.util.List;

import static com.guide.green.green_guide.HTTPRequest.AsyncRequest.getReviewsForPlace;

/**
 * Manages requesting the reviews for a specific point, displaying a dialog box to show that data
 * is being retrieved, and filling the bottom sheet with the resulting reviews.
 *
 * Note:
 *  - Does not clear away what was already on the map from the last review fetch (such as markers)
 *  - Assumes that the bottom sheet is showing the reviews.
 *  - Does not add any icons to the map
 */
public class LocationInfoFetchReviews extends OnRequestResultsListener<ArrayList<Review>> {

    private LoadingDialog mLoadingDialog;
    private AsyncGetReview mReviewTask;
    private boolean mCompleted = false;
    private AppCompatActivity mAct;
    private String locationName;
    private double longitude;
    private double latitude;

    public LocationInfoFetchReviews(@NonNull AppCompatActivity act, String location,
                                    double longitude, double latitude) {

        mAct = act;
        mLoadingDialog = new LoadingDialog();

        mLoadingDialog.show(mAct.getFragmentManager(), "Retrieving ReviewsHolder");

        mReviewTask = getReviewsForPlace(longitude, latitude,
                this);

        mLoadingDialog.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                mReviewTask.cancel(true);
            }
        });

        this.locationName = location;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    public void onSuccess(ArrayList<Review> reviews) {

        if (mLoadingDialog != null) { mLoadingDialog.dismiss(); }

        TextView addressView = mAct.findViewById(R.id.general_info_address);

        initGeneralFragment(reviews.get(0));

        initReviewsFragment(reviews);
    }

    @Override
    public void onProgress(RequestProgress progress) {
        if (!progress.remainingIsUnknown()) {
            mLoadingDialog.setProgress((double) progress.current / progress.total);
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e("Getting Review", e.toString());
        e.printStackTrace();
        Toast.makeText(mAct, "Error retrieving reviews.", Toast.LENGTH_SHORT).show();
        mCompleted = true;
        mLoadingDialog.dismiss();
    }

    @Override
    public void onCanceled() {
        Toast.makeText(mAct, "Getting Review Canceled", Toast.LENGTH_SHORT).show();
        mCompleted = true;
    }

    public void cancel() {
        mReviewTask.cancel(true);
    }

    public boolean isCompleted() {
        return mCompleted;
    }

    public void initGeneralFragment(Review review) {

        TextView addressTextView = mAct.findViewById(R.id.general_info_address);
        TextView cityTextView = mAct.findViewById(R.id.general_info_city);
        TextView industryTextView = mAct.findViewById(R.id.general_info_industry);
        TextView productTextView = mAct.findViewById(R.id.general_info_product);

        addressTextView.setText(review.location.get(Review.Location.Key.ADDRESS));
        cityTextView.setText(review.location.get(Review.Location.Key.CITY));
        industryTextView.setText(review.location.get(Review.Location.Key.INDUSTRY));
        productTextView.setText(review.location.get(Review.Location.Key.PRODUCT));
    }

    public void initReviewsFragment(final ArrayList<Review> reviews) {

        ImageView histogramImageView = mAct.findViewById(R.id.reviews_histogram);
        TextView averageRatingTextView = mAct.findViewById(R.id.reviews_average_rating);
        TextView countTextView = mAct.findViewById(R.id.reviews_count);
        LinearLayout reviewsContainer = mAct.findViewById(R.id.reviews_list);
        Button writeReviewButton = mAct.findViewById(R.id.reviews_write_button);

        String[] histogramX = new String[] {"+3", "+2", "+1", " 0", "-1", "-2", "-3"};
        int[] histogramY = new int[] {0,0,0,0,0,0,0};

        int totalRating = 0;

        for (int i = reviews.size() - 1; i >= 0; i--) {
            Review review = reviews.get(i);
            int rating = Integer.parseInt(review.location.get(Review.Location.Key.RATING));
            histogramY[3 - rating] += 1; // '-3' = 6, '-2' = 5, ..., '+3' = 0
            totalRating += rating;

            reviewsContainer.addView(createSingleReview(review, rating, i));

            if (i != 0) {
                //reviewsContainer.addView();
            }
        }

        float average = (float) totalRating / reviews.size();

        // Retrieve resource values using the appropriate (non-deprecated) methods
        int backgroundColor, goldColor, lightGreyColor, textColor;
        if (Build.VERSION.SDK_INT >= 23) {
            backgroundColor = mAct.getResources().getColor(R.color.white, null);
            goldColor = mAct.getResources().getColor(R.color.bottom_sheet_gold, null);
            lightGreyColor = mAct.getResources().getColor(R.color.lightGrey, null);
            textColor = mAct.getResources().getColor(R.color.darkGrey, null);
        } else {
            backgroundColor = mAct.getResources().getColor(R.color.white);
            goldColor = mAct.getResources().getColor(R.color.bottom_sheet_gold);
            lightGreyColor = mAct.getResources().getColor(R.color.lightGrey);
            textColor = mAct.getResources().getColor(R.color.darkGrey);
        }

        // Create a histogram & add the bitmap to a view
        float textSize = Drawing.convertSpToPx(mAct, 14);

        int w = (mAct.getResources().getDisplayMetrics().widthPixels -
                (int) Drawing.convertDpToPx(mAct, 48)) * 2 / 3;

        int margin = (int) Drawing.convertDpToPx(mAct, 8);

        Bitmap bmp = Drawing.createBarGraph(histogramX, histogramY, w, textSize,
                goldColor, textColor, backgroundColor, lightGreyColor, margin, margin / 2);
        histogramImageView.setImageBitmap(bmp);

        String sTemp;
        sTemp = (average > 0 ? "+" : "") + Math.round(average * 10.0) / 10.0;
        averageRatingTextView.setText(sTemp);
        sTemp = "(" + reviews.size() + ")";
        countTextView.setText(sTemp);

        if (reviews.size() == 0) {
            writeReviewButton.setText(R.string.write_first_review_button_text);
        } else {
            writeReviewButton.setText(R.string.write_review_button_text);
        }

        writeReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (CredentialManager.isLoggedIn()) {

                    Review review = reviews.get(0);

                    WriteReviewActivity.open(mAct, locationName,
                            review.location.get(Review.Location.Key.ADDRESS),
                            review.location.get(Review.Location.Key.CITY),
                            latitude, longitude, review.location.get(Review.Location.Key.INDUSTRY));
                } else {
                    LogInOutSignUpActivity.startActivity(mAct);
                }
            }
        });
    }

    /**
     * Creates a view which encapsulates on review.
     *
     */
    private ViewGroup createSingleReview(final Review review, int rating, final int reviewIndex) {
        LayoutInflater lf = LayoutInflater.from(mAct);
        ViewGroup child = (ViewGroup) lf.inflate(R.layout.review_single_comment,
                (ViewGroup) mAct.findViewById(R.id.reviews_list), false);

        FrameLayout ratingLayout = child.findViewById(R.id.ratingLayout);
        TextView ratingDate = child.findViewById(R.id.ratingDate);
        TextView ratingValue = child.findViewById(R.id.ratingValue);
        ImageView ratingImage = child.findViewById(R.id.ratingImage);
        TextView reviewText = child.findViewById(R.id.reviewText);
        TextView reviewTime = child.findViewById(R.id.reviewTime);

        ratingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mAct, ViewOneReview.class);
                intent.putExtra("location", locationName);
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                intent.putExtra("reviewIndex", reviewIndex);
                mAct.startActivity(intent);
            }
        });

        String observationDate;

        if (review.location.get(Review.Location.Key.OBSERVATION_DATE) != null) {
            observationDate = review.location.get(Review.Location.Key.OBSERVATION_DATE);
        } else {
            observationDate =
                    review.location.get(Review.Location.Key.TIME).substring(0,10);
        }

        ratingDate.setText(String.format("Review from " + observationDate));

        ratingValue.setText(String.format("Overall Rating: %s%d", rating > 0 ? "+" : "", rating));

        String resourceName = "rate" + (rating < 0 ? "_" : "") + Math.abs(rating);
        int resoureceId = mAct.getResources().getIdentifier(resourceName, "drawable",
                mAct.getPackageName());
        Bitmap bmp = BitmapFactory.decodeResource(mAct.getResources(), resoureceId);
        ratingImage.setImageBitmap(bmp);

        reviewText.setText(review.location.get(Review.Location.Key.REVIEW));
        reviewTime.setText(String.format("Time: %s",
                review.location.get(Review.Location.Key.TIME)));

        new GetReviewImagesHandler(review, (ViewGroup) child.findViewById(R.id.reviewImages_root));

        return child;
    }



    /**
     * Adds more information to the {@code BaiduSuggestion.Location} passed in the constructor.
     * Used for POI's where a detailed search of the location is done after the fact.
     *
     * //@param suggestion the suggestion whose information should be merged with the suggestion
     *                   provided to the constructor.
     */
    /*public void updatePoiResult(BaiduSuggestion.Location suggestion) {
        if (suggestion.uid.equals(mSuggestion.uid)) {
            mSuggestion = BaiduSuggestion.Location.merge(mSuggestion, suggestion);
        }
    }*/
    private class GetReviewImagesHandler extends OnRequestResultsListener<List<String>> implements
            View.OnClickListener {
        private Review mReview;
        private ViewGroup mRoot;
        private RecyclerView mRecycleView;
        private ProgressBar mImgProgress;
        private Button mImgRetry;

        public GetReviewImagesHandler(Review review, ViewGroup rootView) {
            mReview = review;
            mRoot = rootView;
            mRecycleView = rootView.findViewById(R.id.reviewImages);
            mImgProgress = rootView.findViewById(R.id.reviewImages_progress);
            mImgRetry = rootView.findViewById(R.id.reviewImages_retry);
            mImgRetry.setOnClickListener(this);
            mReview.getImages(this);
        }

        @Override
        public void onSuccess(List<String> imageUrls) {
            if (imageUrls.isEmpty()) {
                mRoot.setVisibility(View.GONE);
                return;
            }
            mImgProgress.setVisibility(View.GONE);
            mRecycleView.setVisibility(View.VISIBLE);

            RecyclerView.Adapter<PictureCarouselAdapter.CarouselViewHolder> adapter = new
                    PictureCarouselAdapter(mAct.getApplicationContext(), imageUrls);
            mRecycleView.setAdapter(adapter);
            mRecycleView.setHasFixedSize(true);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mAct,
                    LinearLayoutManager.HORIZONTAL, false);

            mRecycleView.setLayoutManager(mLayoutManager);
        }

        @Override
        public void onError(Exception error) {
            mImgProgress.setVisibility(View.GONE);
            mImgProgress.setVisibility(View.GONE);
            mImgRetry.setVisibility(View.VISIBLE);
            error.printStackTrace();
        }

        @Override
        public void onClick(View view) {
            mImgProgress.setVisibility(View.VISIBLE);
            mImgRetry.setVisibility(View.GONE);
            mReview.getImages(this);
        }
    }
}
