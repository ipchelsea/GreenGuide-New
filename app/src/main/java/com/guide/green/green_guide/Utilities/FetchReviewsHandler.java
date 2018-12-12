package com.guide.green.green_guide.Utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mapapi.map.Overlay;
import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.OnRequestResultsListener;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest.RequestProgress;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.Review.AsyncGetReview;
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
public class FetchReviewsHandler extends OnRequestResultsListener<ArrayList<Review>> {
    private BaiduSuggestion.Location mSuggestion; // Used to retrieve the name of the company
    private BottomSheetManager mBtmSheetManager;
    private LoadingDialog mLoadingDialog;
    private AsyncGetReview mReviewTask;
    private boolean mCompleted = false;
    private AppCompatActivity mAct;
    public final Overlay marker;

    public FetchReviewsHandler(@NonNull AppCompatActivity act,
                               @NonNull BaiduSuggestion.Location suggestion,
                               @NonNull BottomSheetManager manager,
                               @NonNull Overlay marker) {
        this.marker = marker;
        mAct = act;
        mSuggestion = suggestion;
        mBtmSheetManager = manager;
        mLoadingDialog = new LoadingDialog();

        mLoadingDialog.show(mAct.getFragmentManager(), "Retrieving ReviewsHolder");

        mReviewTask = getReviewsForPlace(suggestion.point.longitude, suggestion.point.latitude,
                this);

        mLoadingDialog.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                mReviewTask.cancel(true);
                mBtmSheetManager.removeMarkers();
            }
        });
    }

    /**
     * Adds more information to the {@code BaiduSuggestion.Location} passed in the constructor.
     * Used for POI's where a detailed search of the location is done after the fact.
     *
     * @param suggestion the suggestion whose information should be merged with the suggestion
     *                   provided to the constructor.
     */
    public void updatePoiResult(BaiduSuggestion.Location suggestion) {
        if (suggestion.uid.equals(mSuggestion.uid)) {
            mSuggestion = BaiduSuggestion.Location.merge(mSuggestion, suggestion);
        }
    }

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

    /**
     * Creates a view which encapsulates on review.
     *
     * @param review the review object to display
     * @param rating the ratting of the review
     * @return the view displaying the review
     */
    private ViewGroup createSingleReview(final Review review, int rating) {
        LayoutInflater lf = LayoutInflater.from(mAct);
        ViewGroup child = (ViewGroup) lf.inflate(R.layout.review_single_comment,
                mBtmSheetManager.reviews.body.reviews, false);
        TextView ratingValue = child.findViewById(R.id.ratingValue);
        ImageView ratingImage = child.findViewById(R.id.ratingImage);
        TextView reviewText = child.findViewById(R.id.reviewText);
        TextView reviewTime = child.findViewById(R.id.reviewTime);

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

        new GetReviewImagesHandler(review, (ViewGroup) child.findViewById(R.id.reviewImages_root));

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
        mBtmSheetManager.setBottomSheetState(BottomSheetBehavior.STATE_HIDDEN);
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
