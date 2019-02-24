package com.guide.green.green_guide.Utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.LocationPreview;
import com.guide.green.green_guide.R;

import java.util.ArrayList;

import static com.guide.green.green_guide.HTTPRequest.AsyncRequest.getReviewsForPlace;

// This class will retrieve the reviews for a given location
public class FetchReviews extends AbstractRequest.OnRequestResultsListener<ArrayList<Review>> {

    private AppCompatActivity mAct;
    private BaiduSuggestion.Location reviewsLocation;
    private LoadingDialog mLoadingDialog;
    private AsyncGetReview mReviewTask;
    private boolean fetchCompleted = false;

    public FetchReviews(@NonNull AppCompatActivity act,
                        @NonNull BaiduSuggestion.Location location) {

        mAct = act;
        reviewsLocation = location;
        mLoadingDialog = new LoadingDialog();

        mLoadingDialog.show(mAct.getFragmentManager(), "Retrieving ReviewsHolder");

        mReviewTask = getReviewsForPlace(reviewsLocation.point.longitude,
                reviewsLocation.point.latitude, this);

        mLoadingDialog.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                mReviewTask.cancel(true);
            }
        });
    }

    /*
     * This method will execute once the reviews are retrieved from the WebService
     * This will also replace the content in the PreviewFragment with data
     * relevant to the location in question
     */
    @Override
    public void onSuccess(ArrayList<Review> reviews) {

        // Dismiss the loading dialog
        if (mLoadingDialog != null) { mLoadingDialog.dismiss(); }

        mAct.findViewById(R.id.preview_fragment_container).setVisibility(View.VISIBLE);

        /*// Start the process of showing our location preview
        FragmentTransaction ft = mAct.getSupportFragmentManager().beginTransaction();

        LocationPreview newPreviewFragment = LocationPreview.newInstance(reviews);

        // Replace the contents of the container with the new LocationPreview
        ft.replace(R.id.preview_fragment_container, newPreviewFragment);

        ft.commit();*/

        mAct.findViewById(R.id.FragmentLayout).setClipToOutline(true);

        TextView companyName = mAct.findViewById(R.id.preview_company_name);
        companyName.setText(reviewsLocation.name);



        // Fills the LocationPreview with data from the reviews if there is any,
        // otherwise defaults to "No Reviews"
        if (reviews.size() > 0) {
            displayAndCalculateReviewData(reviews);
            Log.d("Check", "Calculating: " + reviews.size());
        } else {
            TextView ratingValue = mAct.findViewById(R.id.preview_rating_value);
            ImageView ratingImage = mAct.findViewById(R.id.preview_rating_image);
            TextView ratingCount = mAct.findViewById(R.id.preview_ratings_count);

            ratingValue.setText("No Reviews");
            ratingValue.setTypeface(ratingValue.getTypeface(), Typeface.BOLD_ITALIC);

            ratingCount.setText("");
        }
        Log.d("Check", "Calculating: ");

    }

    private void displayAndCalculateReviewData(ArrayList<Review> reviews) {

        // Calculating the average rating based on all the ratings in our ArrayList
        int totalRating = 0;

        for (Review review : reviews) {
            totalRating += Integer.parseInt(review.location.get(Review.Location.Key.RATING));
        }

        float average = (float) totalRating / reviews.size();

        TextView ratingValue = mAct.findViewById(R.id.preview_rating_value);
        ImageView ratingImage = mAct.findViewById(R.id.preview_rating_image);
        TextView ratingCount = mAct.findViewById(R.id.preview_ratings_count);

        String sTemp;
        sTemp = (average > 0 ? "+" : "") + average;
        ratingValue.setText(sTemp);
        sTemp = "(" + reviews.size() + ")";
        ratingCount.setText(sTemp);

        int rating = Math.abs((int) average);
        String resourceName = "rate" + (rating < 0 ? "_" : "") + Math.abs(rating);
        int resourceId = mAct.getResources().getIdentifier(resourceName, "drawable",
                mAct.getPackageName());
        Bitmap ratingBmp = BitmapFactory.decodeResource(mAct.getResources(), resourceId);
        ViewGroup.LayoutParams lp = ratingImage.getLayoutParams();
        lp.height = ratingImage.getHeight();
        lp.width = lp.height * 8;
        ratingImage.setLayoutParams(lp);
        ratingImage.setImageBitmap(ratingBmp);
    }
}
