package com.guide.green.green_guide;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.Utilities.AsyncJSONArray;
import com.guide.green.green_guide.Utilities.Drawing;
import com.guide.green.green_guide.Utilities.Review;
import java.util.ArrayList;

/**
 * Manages requesting the reviews for a specific point, displaying a dialog box to show that data
 * is being retrieved, and filling up the bottom sheet with the resulting reviews.
 */
class FetchedReviewsHandler implements Review.GetReviewsResult {
    private MainActivity.BaiduSuggestion suggestion;
    private LoadingDialog ld;
    private Activity act;

    private TextView btmSheetCompanyName;
    private BottomSheetBehavior btmSheet;
    private android.support.v4.widget.NestedScrollView btmSheetView;

    public FetchedReviewsHandler(@Nullable LoadingDialog ld, @NonNull Activity act,
                                  @NonNull MainActivity.BaiduSuggestion suggestion) {
        this.ld = ld;
        this.suggestion = suggestion;
        this.act = act;
        btmSheetView = (android.support.v4.widget.NestedScrollView) act.findViewById(R.id.btmSheet);
        btmSheet = BottomSheetBehavior.from(btmSheetView);
        btmSheetCompanyName = (TextView) act.findViewById(R.id.previewCompanyName);
    }

    @Override
    public void onSuccess(ArrayList<Review> reviews) {
        ImageView btmSheetRatingStars = (ImageView) act.findViewById(R.id.btmSheetRatingStars);

        // Hide dialog
        if (ld != null) {
            ld.dismiss();
        }
        btmSheetView.requestFocus();

        // Calculate histogram values and average rating
        String address = "", city = "", industry = "", product = "";


        btmSheetCompanyName.setText(suggestion.name);

        LinearLayout root = (LinearLayout) act.findViewById(R.id.userReviewList);
        root.removeAllViews();


        int total = 0;
        String[] histogramX = new String[] {"+3", "+2", "+1", "0", "-1", "-2", "-3"};
        int[] histogramY = new int[histogramX.length];
        for (int i = reviews.size() - 1; i >= 0; i--) {
            Review review = reviews.get(i);
            int rating = Integer.parseInt(review.location.get(Review.Location.Key.RATING));
            histogramY[3 - rating] += 1; // '-3' = 6, '-2' = 5, ..., '+3' = 0
            total += rating;

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
            LayoutInflater lf = LayoutInflater.from(act);
            ViewGroup child = (ViewGroup) lf.inflate(R.layout.review_single_comment,
                    null, false);
            TextView ratingValue = (TextView) child.findViewById(R.id.ratingValue);
            ImageView ratingImage = (ImageView) child.findViewById(R.id.ratingImage);
            TextView reviewText = (TextView) child.findViewById(R.id.reviewText);
            TextView reviewTime = (TextView) child.findViewById(R.id.reviewTime);
//            Button rawDataBtn = (Button) child.findViewById(R.id.rawDataBtn);
//            Button helpfulBtn = (Button) child.findViewById(R.id.helpfulBtn);
//            Button inappropriateBtn = (Button) child.findViewById(R.id.inappropriateBtn);

            ratingValue.setText("Rating: " + (rating > 0 ? "+" : "") + rating);

            String resourceName = "rate" + (rating < 0 ? "_" : "") + Math.abs(rating);
            int resoureceId = act.getResources().getIdentifier(resourceName, "drawable",
                    act.getPackageName());
            Bitmap bmp = BitmapFactory.decodeResource(act.getResources(), resoureceId);
            ratingImage.setImageBitmap(bmp);

            reviewText.setText(review.location.get(Review.Location.Key.REVIEW));
            reviewTime.setText("Time: " + review.location.get(Review.Location.Key.TIME));

            root.addView(child);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams (child.getLayoutParams());
            child.setPadding(0, (int) Drawing.convertDpToPx(act,10), 0, 0);
            child.setLayoutParams(lp);
        }

        // Remove empty X values from histogram.
        int histLeft = 0, histRight = histogramY.length - 1;
        float average = (float) total / reviews.size();
        float ratio = (average + (histogramX.length / 2)) / (float) (histogramX.length - 1);

        // Retrieve resource values using the appropriate (non-deprecated) methods
        int filledStarsColor, backgroundColor;
        if (Build.VERSION.SDK_INT >= 23) {
            filledStarsColor = act.getResources().getColor(R.color.bottom_sheet_gold, null);
            backgroundColor = act.getResources().getColor(R.color.bottom_sheet_blue, null);
        } else {
            filledStarsColor = act.getResources().getColor(R.color.bottom_sheet_gold);
            backgroundColor = act.getResources().getColor(R.color.bottom_sheet_blue);
        }

        // Create the rating stars & add the bitmap to a view
        int h = act.getResources().getDimensionPixelSize(
                R.dimen.reviews_bottom_sheet_peek_height_halved);
        int w = btmSheetRatingStars.getWidth();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Drawing.drawStars(0, 0, w, h, histogramX.length, ratio,
                filledStarsColor, Color.GRAY, new Canvas(bmp));
        btmSheetRatingStars.setImageBitmap(bmp);

        // Create a histogram & add the bitmap to a view
        float textSize = Drawing.convertSpToPx(act, 13);
        w = act.getResources().getDisplayMetrics().widthPixels;
        bmp = Drawing.createBarGraph(histogramX, histogramY, histLeft, histRight, w,
                textSize, filledStarsColor, Color.WHITE, backgroundColor, 7, 7, 7);
        ((ImageView) act.findViewById(R.id.btmSheetHistogram)).setImageBitmap(bmp);


        String sTemp;
        sTemp = (average > 0 ? "+" : "") + average;
        ((TextView) act.findViewById(R.id.btmSheetRatingValue)).setText(sTemp);
        sTemp = reviews.size() + " Review" + (reviews.size() > 1 ? "s" : "");
        ((TextView) act.findViewById(R.id.btmSheetRatingsCount)).setText(sTemp);

        ((TextView) act.findViewById(R.id.btmSheetAddress)).setText("Address: " + address);
        ((TextView) act.findViewById(R.id.btmSheetCityName)).setText("City: " + city);
        ((TextView) act.findViewById(R.id.btmSheetIndustry)).setText("Industry: " + industry);
        ((TextView) act.findViewById(R.id.btmSheetProduct)).setText("Product: " + product);

        btmSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onError(Exception e) {
        Log.e("Getting Review", e.toString());
        e.printStackTrace();
        Toast.makeText(act, "Error retrieving reviews.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdate(long current, long total) {
        if (total != -1) {
            ld.setProgress((double) current / total);
        }
    }

    @Override
    public void onCanceled() {
        Toast.makeText(act, "Getting Review Canceled", Toast.LENGTH_SHORT).show();
    }

    public static void fetch(@NonNull Activity act,
                             @NonNull MainActivity.BaiduSuggestion suggestion) {
        final LoadingDialog ld = new LoadingDialog();
        ld.show(act.getFragmentManager(), "Retrieving Reviews");

        FetchedReviewsHandler fetchHandler = new FetchedReviewsHandler(ld, act, suggestion);
        final AsyncJSONArray reviewTask = Review.getReviewsForPlace(suggestion.point.longitude,
                suggestion.point.latitude, fetchHandler);

//        final AsyncJSONArray reviewTask = Review.getReviewsForPlace(104.075155d,
//                37.198839d, fetchHandler);

        ld.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                reviewTask.cancel(true);
            }
        });
    }
}
