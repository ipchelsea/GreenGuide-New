package com.guide.green.green_guide.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.HTTPRequest.AbstractFormItem;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.HTTPRequest.POSTMultipartData;
import com.guide.green.green_guide.LogInOutSignUpActivity;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.CredentialManager;
import com.guide.green.green_guide.Utilities.Drawing;
import com.guide.green.green_guide.Utilities.LocationInfoFetchReviews;
import com.guide.green.green_guide.Utilities.PictureCarouselAdapter;
import com.guide.green.green_guide.Utilities.Review;
import com.guide.green.green_guide.ViewOneReview;
import com.guide.green.green_guide.WriteReviewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyReviewsFragment extends Fragment {
    private TextView mainTextView;
    private LinearLayout mReviewsContainer;
    private LoadingDialog ld;
    private FragmentActivity mAct;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_my_reviews, null);
        mainTextView = mRoot.findViewById(R.id.my_reviews);
        mReviewsContainer = mRoot.findViewById(R.id.my_reviews_list);

        if (CredentialManager.isLoggedIn()) {
            mRoot.findViewById(R.id.log_in_message).setVisibility(View.GONE);

            loadReviews();
        }

        mAct = getActivity();

        return mRoot;
    }

    // Instance methods

    private void loadReviews() {
        ArrayList<AbstractFormItem> formItems = new ArrayList<>();
        formItems.add(new AbstractFormItem.TextFormItem("s_name", CredentialManager.getUsername()));
        formItems.add(new AbstractFormItem.TextFormItem("profile_token", "01142019work"));

        ld = new LoadingDialog();
        ld.show(getActivity().getFragmentManager(), "Loading Reviews...");
        final POSTMultipartData.AsyncPostData postRequest = AsyncRequest.postMultipartData(
                "http://www.lovegreenguide.com/profile_app.php", formItems, mLogInHandler);
        ld.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                postRequest.cancel(true);
            }
        });
    }

    private AbstractRequest.OnRequestResultsListener<StringBuilder> mLogInHandler =
            new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                @Override
                public void onSuccess(StringBuilder stringBuilder) {
                    Toast.makeText(getContext(), "it worked", Toast.LENGTH_LONG).show();
                    ld.dismiss();

                    StringBuilder sb = stringBuilder;

                    JSONArray jArr = null;
                    try {
                        int startingIndex = sb.toString().indexOf("[", sb.toString().indexOf("[") + 1);
                        jArr = new JSONArray(sb.toString().substring(startingIndex));
                    } catch (Exception e) {

                    }

                    ArrayList<Review> results = new ArrayList<>();
                    for (int i = jArr.length() - 1; i >= 0; i--) {
                        Review review = new Review();

                        try {

                            JSONObject jObj = jArr.getJSONObject(i);

                            if (!jObj.isNull("review")) {
                                JSONObject subJObj = jObj.getJSONObject("review");
                                review.id = subJObj.getString("id");
                            }

                            getJsonValuesForObject(jObj, "review", review.location);
                            getJsonValuesForObject(jObj, "water", review.waterIssue);
                            getJsonValuesForObject(jObj, "solid", review.solidWaste);
                            getJsonValuesForObject(jObj, "air", review.airWaste);
                            results.add(review);
                        } catch (JSONException e) {

                        }
                    }

                    generateReviews(results);
                }

                private String decodeHTML(String htmlString) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString();
                    } else {
                        return Html.fromHtml(htmlString).toString();
                    }
                }

                /**
                 * For the specified category, it goes through all of its keys. For all the once with a
                 * jsonName, it retrieves stores the value of the {@code JSONObject[jsonName]} in that
                 * key.
                 *
                 * @param jObj the json object which which contains another object corresponding to the
                 *             category. E.g., jObj = { 'water': {...}, 'air': {...} }
                 * @param objName the name of the category in the object. E.g., 'water'
                 * @param category the category to fill the data of
                 * @throws JSONException
                 */
                private void getJsonValuesForObject(JSONObject jObj, String objName,
                                                    Review.ReviewCategory category) throws JSONException {
                    if (!jObj.isNull(objName)) {
                        JSONObject subJObj = jObj.getJSONObject(objName);
                        for (Review.Key key : category.allKeys()) {
                            if (key.jsonName != null) {
                                category.set(key, decodeHTML(subJObj.getString(key.jsonName)));
                            }
                        }
                    }
                }
            };

    private void generateReviews(ArrayList<Review> reviews) {
        initReviewsFragment(reviews);
    }

    public void initReviewsFragment(final ArrayList<Review> reviews) {

        for (int i = reviews.size() - 1; i >= 0; i--) {
            Log.d("CHECK", "REVIEWS: " + i);
            Review review = reviews.get(i);
            int rating = Integer.parseInt(review.location.get(Review.Location.Key.RATING));

            mReviewsContainer.addView(createSingleReview(review, rating, i));
        }
    }

    /**
     * Creates a view which encapsulates on review.
     *
     */
    private ViewGroup createSingleReview(final Review review, int rating, final int reviewIndex) {

        LayoutInflater lf = LayoutInflater.from(mAct);
        ViewGroup child = (ViewGroup) lf.inflate(R.layout.review_single_comment,
                (ViewGroup) mReviewsContainer, false);

        FrameLayout ratingLayout = child.findViewById(R.id.ratingLayout);
        TextView reviewPending = child.findViewById(R.id.reviewPendingTextView);
        TextView ratingDate = child.findViewById(R.id.ratingDate);
        TextView ratingValue = child.findViewById(R.id.ratingValue);
        ImageView ratingImage = child.findViewById(R.id.ratingImage);
        TextView reviewText = child.findViewById(R.id.reviewText);
        TextView reviewTime = child.findViewById(R.id.reviewTime);
        ImageButton helpfulBtn = child.findViewById(R.id.helpfulBtn);

        Button editButton = child.findViewById(R.id.edit_review_btn);
        Button deleteButton = child.findViewById(R.id.delete_review_btn);
        Button editImageButton = child.findViewById(R.id.edit_image_btn);

        /*ratingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mAct, ViewOneReview.class);
                intent.putExtra("location", locationName);
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                intent.putExtra("reviewIndex", reviewIndex);
                mAct.startActivity(intent);
            }
        });*/

        String observationDate;

        if (review.location.get(Review.Location.Key.OBSERVATION_DATE) != null) {
            observationDate = review.location.get(Review.Location.Key.OBSERVATION_DATE);
        } else {
            observationDate =
                    review.location.get(Review.Location.Key.TIME).substring(0,10);
        }

        reviewPending.setVisibility(View.VISIBLE);

        Log.d("CHECK", "yyyy: " + review.location.get(Review.Location.Key.STATUS));

        if (review.location.get(Review.Location.Key.STATUS).equals("1")) {
            reviewPending.setText("Public");
            reviewPending.setTextColor(getResources().getColor(R.color.bottom_sheet_blue));
        } else {
            reviewPending.setText("In Process");
            reviewPending.setTextColor(getResources().getColor(R.color.bottom_sheet_gold));
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

        new MyReviewsFragment.GetReviewImagesHandler(review, (ViewGroup) child.findViewById(R.id.reviewImages_root));

        helpfulBtn.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);
        deleteButton.setVisibility(View.VISIBLE);
        editImageButton.setVisibility(View.VISIBLE);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //WriteReviewActivity.open(getActivity());
                Intent intent = new Intent(getActivity(), WriteReviewActivity.class);
                intent.putExtra("EditReview", review);
                getActivity().startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<AbstractFormItem> formItems = new ArrayList<>();
                String reviewId = String.valueOf(Integer.parseInt(review.id) + 140);
                formItems.add(new AbstractFormItem.TextFormItem("id", reviewId));
                formItems.add(new AbstractFormItem.TextFormItem("s_name", CredentialManager.getUsername()));
                formItems.add(new AbstractFormItem.TextFormItem("del_token", "02042019work"));

                Toast.makeText(getContext(), "Deleting Review... " + reviewId, Toast.LENGTH_LONG).show();

                ld = new LoadingDialog();
                ld.show(getActivity().getFragmentManager(), "Deleting Review...");
                final POSTMultipartData.AsyncPostData postRequest = AsyncRequest.postMultipartData(
                        "http://www.lovegreenguide.com/del_app.php", formItems, new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                            @Override
                            public void onSuccess(StringBuilder stringBuilder) {
                                ld.dismiss();
                                Toast.makeText(getContext(), "Review: " + stringBuilder.toString(), Toast.LENGTH_LONG).show();
                            }});
                ld.setCallback(new LoadingDialog.Canceled() {
                    @Override
                    public void onCancel() {
                        postRequest.cancel(true);
                    }
                });
            }
        });

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
    private class GetReviewImagesHandler extends AbstractRequest.OnRequestResultsListener<List<String>> implements
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

            if (mReview.imageCount == 0) {
                mRoot.setVisibility(View.GONE);
            }
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
