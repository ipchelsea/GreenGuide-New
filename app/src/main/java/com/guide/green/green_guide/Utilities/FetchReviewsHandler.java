package com.guide.green.green_guide.Utilities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.map.Overlay;
import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.R;
import java.util.ArrayList;

import com.guide.green.green_guide.Utilities.BaiduMapManager.BaiduSuggestion;

/**
 * Manages requesting the reviews for a specific point, displaying a dialog box to show that data
 * is being retrieved, and filling the bottom sheet with the resulting reviews.
 *
 * Note:
 *  - Does not clear away what was already there.
 *  - Assumes that the bottom sheet is showing the reviews.
 *  - Does not add any icons to the map
 */
public class FetchReviewsHandler implements Review.Results {
    private BottomSheetManager mBtmSheetManager;
    private BaiduSuggestion mSuggestion;
    private LoadingDialog mLoadingDialog;
    private AsyncJSONArray mReviewTask;
    private boolean mCompleted = false;
    private Overlay mMarker;
    private Activity mAct;

    public FetchReviewsHandler(@NonNull Activity act,
                               @NonNull BaiduSuggestion suggestion,
                               @NonNull BottomSheetManager manager) {
        mAct = act;
        mSuggestion = suggestion;
        mBtmSheetManager = manager;
        mLoadingDialog = new LoadingDialog();

        mLoadingDialog.show(mAct.getFragmentManager(), "Retrieving ReviewsHolder");

        mReviewTask = Review.getReviewsForPlace(mSuggestion.point.longitude,
                mSuggestion.point.latitude, this);

        mLoadingDialog.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                mReviewTask.cancel(true);
                mBtmSheetManager.clearMarkers();
            }
        });
    }

    @Override
    public void onSuccess(ArrayList<Review> reviews) {
        if (mLoadingDialog != null) { mLoadingDialog.dismiss(); }
        mBtmSheetManager.REVIEWS.peekBar.companyName.setText(mSuggestion.name);
        mBtmSheetManager.REVIEWS.body.reviews.removeAllViews();

        // Calculate histogram values and average rating
        String[] histogramX = new String[] {"+3", "+2", "+1", "0", "-1", "-2", "-3"};
        String address = "", city = "", industry = "", product = "";
        int[] histogramY = new int[histogramX.length];
        int totalRating = 0;

        for (int i = reviews.size() - 1; i >= 0; i--) {
            Review review = reviews.get(i);
            int rating = Integer.parseInt(review.location.get(Review.Location.Key.RATING));
            histogramY[3 - rating] += 1; // '-3' = 6, '-2' = 5, ..., '+3' = 0
            totalRating += rating;

            if (address.equals("")) {
                address = review.location.get(Review.Location.Key.ADDRESS);
            }
            if (city.equals("")) {
                city = review.location.get(Review.Location.Key.CITY);
            }
            if (industry.equals("")) {
                industry = review.location.get(Review.Location.Key.INDUSTRY);
            }
            if (product.equals("")) {
                product = review.location.get(Review.Location.Key.PRODUCT);
            }

            // Add review
            LayoutInflater lf = LayoutInflater.from(mAct);
            ViewGroup child = (ViewGroup) lf.inflate(R.layout.review_single_comment,
                    mBtmSheetManager.REVIEWS.body.reviews, false);
            TextView ratingValue = child.findViewById(R.id.ratingValue);
            ImageView ratingImage = child.findViewById(R.id.ratingImage);
            TextView reviewText = child.findViewById(R.id.reviewText);
            TextView reviewTime = child.findViewById(R.id.reviewTime);
//            Button rawDataBtn = (Button) child.findViewById(R.id.rawDataBtn);
//            Button helpfulBtn = (Button) child.findViewById(R.id.helpfulBtn);
//            Button inappropriateBtn = (Button) child.findViewById(R.id.inappropriateBtn);

            ratingValue.setText(String.format("Rating: %s%d", rating > 0 ? "+" : "", rating));

            String resourceName = "rate" + (rating < 0 ? "_" : "") + Math.abs(rating);
            int resoureceId = mAct.getResources().getIdentifier(resourceName, "drawable",
                    mAct.getPackageName());
            Bitmap bmp = BitmapFactory.decodeResource(mAct.getResources(), resoureceId);
            ratingImage.setImageBitmap(bmp);

            reviewText.setText(review.location.get(Review.Location.Key.REVIEW));
            reviewTime.setText(String.format("Time: %s",
                    review.location.get(Review.Location.Key.TIME)));

            mBtmSheetManager.REVIEWS.body.reviews.addView(child);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams (child.getLayoutParams());
            child.setPadding(0, (int) Drawing.convertDpToPx(mAct,10), 0, 0);
            child.setLayoutParams(lp);
        }

        // Remove empty X values from histogram.
        int histLeft = 0, histRight = histogramY.length - 1;
        float average = (float) totalRating / reviews.size();
        float ratio = (average + (histogramX.length / 2)) / (float) (histogramX.length - 1);

        // Retrieve resource values using the appropriate (non-deprecated) methods
        int filledStarsColor, backgroundColor;
        if (Build.VERSION.SDK_INT >= 23) {
            filledStarsColor = mAct.getResources().getColor(R.color.bottom_sheet_gold, null);
            backgroundColor = mAct.getResources().getColor(R.color.bottom_sheet_blue, null);
        } else {
            filledStarsColor = mAct.getResources().getColor(R.color.bottom_sheet_gold);
            backgroundColor = mAct.getResources().getColor(R.color.bottom_sheet_blue);
        }

        // Create the rating stars & add the bitmap to a view
        int h = mAct.getResources().getDimensionPixelSize(
                R.dimen.reviews_bottom_sheet_peek_height_halved);
        int w = mBtmSheetManager.REVIEWS.peekBar.ratingStars.getWidth();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Drawing.drawStars(0, 0, w, h, histogramX.length, ratio,
                filledStarsColor, Color.GRAY, new Canvas(bmp));
        mBtmSheetManager.REVIEWS.peekBar.ratingStars.setImageBitmap(bmp);

        // Create a histogram & add the bitmap to a view
        float textSize = Drawing.convertSpToPx(mAct, 13);
        w = mAct.getResources().getDisplayMetrics().widthPixels;
        bmp = Drawing.createBarGraph(histogramX, histogramY, histLeft, histRight, w, textSize,
                filledStarsColor, Color.WHITE, backgroundColor, 7, 7, 7);
        mBtmSheetManager.REVIEWS.body.histogram.setImageBitmap(bmp);


        String sTemp;
        sTemp = (average > 0 ? "+" : "") + average;
        mBtmSheetManager.REVIEWS.peekBar.ratingValue.setText(sTemp);
        sTemp = reviews.size() + " Review" + (reviews.size() != 1 ? "s" : "");
        mBtmSheetManager.REVIEWS.peekBar.ratingCount.setText(sTemp);

        mBtmSheetManager.REVIEWS.body.address.setText("Address: " + address);
        mBtmSheetManager.REVIEWS.body.city.setText("City: " + city);
        mBtmSheetManager.REVIEWS.body.industry.setText("Industry: " + industry);
        mBtmSheetManager.REVIEWS.body.product.setText("Product: " + product);

        if (reviews.size() == 0) {
            mBtmSheetManager.REVIEWS.peekBar.ratingStars.setVisibility(View.INVISIBLE);
            mBtmSheetManager.REVIEWS.peekBar.ratingValue.setVisibility(View.INVISIBLE);
            mBtmSheetManager.REVIEWS.body.container.setVisibility(View.INVISIBLE);
            mBtmSheetManager.REVIEWS.firstReviewButton.setText(R.string.write_first_review_button_text);
        } else {
            mBtmSheetManager.REVIEWS.peekBar.ratingStars.setVisibility(View.VISIBLE);
            mBtmSheetManager.REVIEWS.peekBar.ratingValue.setVisibility(View.VISIBLE);
            mBtmSheetManager.REVIEWS.body.container.setVisibility(View.VISIBLE);
            mBtmSheetManager.REVIEWS.firstReviewButton.setText(R.string.write_review_button_text);
        }

        mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
        mCompleted = true;
    }
//
//    /***
//     * Returns a resource ID for the appropriately colored marker for the supplied rating.
//     *
//     * @param averageRating A number in the set [-3,3] inclusive of both.
//     * @return A resource Id.
//     */
//    private int getColoredMarkerFromRating(float averageRating) {
//        int drawableId;
//        switch (Math.round(averageRating)) {
//            case -3: drawableId = R.drawable.icon_markg_red; break;
//            case -2: drawableId = R.drawable.icon_markg_orange; break;
//            case -1: drawableId = R.drawable.icon_markg_yellow; break;
//            case 0: drawableId = R.drawable.icon_markg_white; break;
//            case 1: drawableId = R.drawable.icon_markg_aqua; break;
//            case 2: drawableId = R.drawable.icon_markg_lime; break;
//            default: drawableId = R.drawable.icon_markg_green; break;
//        }
//        return drawableId;
//    }

    public void remove() {
        if (mMarker != null) {
            mMarker.remove();
        }
    }

    @Override
    public void onError(Exception e) {
        Log.e("Getting Review", e.toString());
        e.printStackTrace();
        Toast.makeText(mAct, "Error retrieving reviews.", Toast.LENGTH_SHORT).show();
        mCompleted = true;
    }

    @Override
    public void onUpdate(long current, long total) {
        if (total != -1) {
            mLoadingDialog.setProgress((double) current / total);
        }
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
}