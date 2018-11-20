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

import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.WriteReviewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Manages requesting the reviews for a specific point, displaying a dialog box to show that data
 * is being retrieved, and filling the bottom sheet with the resulting reviews.
 *
 * Note:
 *  - Does not clear away what was already on the map from the last review fetch (such as markers)
 *  - Assumes that the bottom sheet is showing the reviews.
 *  - Does not add any icons to the map
 */
public class FetchReviewsHandler implements Review.ReviewResults {
    private BaiduSuggestion.Location mSuggestion;
    private BottomSheetManager mBtmSheetManager;
    private LoadingDialog mLoadingDialog;
    private AsyncJSONArray mReviewTask;
    private boolean mCompleted = false;
    private Activity mAct;

    public FetchReviewsHandler(@NonNull Activity act,
                               @NonNull BaiduSuggestion.Location suggestion,
                               @NonNull BottomSheetManager manager) {
        mAct = act;
        mSuggestion = suggestion;
        mBtmSheetManager = manager;
        mLoadingDialog = new LoadingDialog();

        mLoadingDialog.show(mAct.getFragmentManager(), "Retrieving ReviewsHolder");

        mReviewTask = Review.getReviewsForPlace(suggestion.point.longitude,
                suggestion.point.latitude, this);

        mLoadingDialog.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                mReviewTask.cancel(true);
                mBtmSheetManager.removeMarkers();
            }
        });
    }

    public void updatePoiResult(BaiduSuggestion.Location suggestion) {
        if (suggestion.uid.equals(mSuggestion.uid)) {
            mSuggestion = BaiduSuggestion.Location.merge(mSuggestion, suggestion);
        }
    }

    private ViewGroup createSingleReview(final Review review, int rating) {
        LayoutInflater lf = LayoutInflater.from(mAct);
        final ViewGroup child = (ViewGroup) lf.inflate(R.layout.review_single_comment,
                mBtmSheetManager.reviews.body.reviews, false);
        TextView ratingValue = child.findViewById(R.id.ratingValue);
        ImageView ratingImage = child.findViewById(R.id.ratingImage);
        TextView reviewText = child.findViewById(R.id.reviewText);
        TextView reviewTime = child.findViewById(R.id.reviewTime);
        final LinearLayout reviewImages = child.findViewById(R.id.reviewImages);
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

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(child.getLayoutParams());
        child.setPadding(0, (int) Drawing.convertDpToPx(mAct,10), 0, 0);
        child.setLayoutParams(lp);

        new AsyncJSONObject(new AsyncJSONObject.OnAsyncJSONObjectResultListener() {
            @Override
            public void onFinish(ArrayList<JSONObject> jObject, ArrayList<Exception> exceptions) {
                JSONArray imgUrls = null;
                try {
                    imgUrls = jObject.get(0).getJSONArray("all_image");
                    for (int i = 0; i < imgUrls.length(); i++) {
                        String path = imgUrls.getString(i);
                        new AsyncGetImage() {
                            @Override
                            public void onPostExecute(Bitmap bitmap) {
                                ImageView imgView = new ImageView(mAct);
                                imgView.setImageBitmap(bitmap);
                                reviewImages.addView(imgView);
                            }
                        }.execute("http://www.lovegreenguide.com/" + path);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            }

            @Override
            public void onCanceled(ArrayList<JSONObject> jObject, ArrayList<Exception> exceptions) {
                /* Do nothing */
            }
        }).execute("http://www.lovegreenguide.com/view_app.php?id=" + review.id);

        return child;
    }

    @Override
    public void onSuccess(ArrayList<Review> reviews) {
        mBtmSheetManager.reviews.peekBar.companyName.setText(mSuggestion.name);
        if (mLoadingDialog != null) { mLoadingDialog.dismiss(); }
        mBtmSheetManager.reviews.body.reviews.removeAllViews();

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

            mBtmSheetManager.reviews.body.reviews.addView(createSingleReview(review, rating));
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
        int w = mBtmSheetManager.reviews.peekBar.ratingStars.getWidth();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Drawing.drawStars(0, 0, w, h, histogramX.length, ratio,
                filledStarsColor, Color.GRAY, new Canvas(bmp));
        mBtmSheetManager.reviews.peekBar.ratingStars.setImageBitmap(bmp);

        // Create a histogram & add the bitmap to a view
        float textSize = Drawing.convertSpToPx(mAct, 13);
        w = mAct.getResources().getDisplayMetrics().widthPixels;
        bmp = Drawing.createBarGraph(histogramX, histogramY, histLeft, histRight, w, textSize,
                filledStarsColor, Color.WHITE, backgroundColor, 7, 7, 7);
        mBtmSheetManager.reviews.body.histogram.setImageBitmap(bmp);

        String sTemp;
        sTemp = (average > 0 ? "+" : "") + average;
        mBtmSheetManager.reviews.peekBar.ratingValue.setText(sTemp);
        sTemp = reviews.size() + " Review" + (reviews.size() != 1 ? "s" : "");
        mBtmSheetManager.reviews.peekBar.ratingCount.setText(sTemp);

        mBtmSheetManager.reviews.body.address.setText("Address: " + address);
        mBtmSheetManager.reviews.body.city.setText("City: " + city);
        mBtmSheetManager.reviews.body.industry.setText("Industry: " + industry);
        mBtmSheetManager.reviews.body.product.setText("Product: " + product);

        int visibilityState;
        if (reviews.size() == 0) {
            visibilityState = View.INVISIBLE;
            mBtmSheetManager.reviews.writeReviewButton.setText(R.string.write_first_review_button_text);
        } else {
            visibilityState = View.VISIBLE;
            mBtmSheetManager.reviews.writeReviewButton.setText(R.string.write_review_button_text);
        }
        mBtmSheetManager.reviews.peekBar.ratingStars.setVisibility(visibilityState);
        mBtmSheetManager.reviews.peekBar.ratingValue.setVisibility(visibilityState);
        mBtmSheetManager.reviews.peekBar.ratingCount.setVisibility(visibilityState);
        mBtmSheetManager.reviews.body.container.setVisibility(visibilityState);

        mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_COLLAPSED);
        mCompleted = true;

        mBtmSheetManager.reviews.writeReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WriteReviewActivity.open(mAct, mSuggestion,
                        mBtmSheetManager.reviews.body.industry.getText().toString());
            }
        });
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
