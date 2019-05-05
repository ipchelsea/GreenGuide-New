package com.guide.green.green_guide;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.Fragments.WriteReviewAirFragment;
import com.guide.green.green_guide.Fragments.WriteReviewGeneralFragment;
import com.guide.green.green_guide.Fragments.WriteReviewSolidFragment;
import com.guide.green.green_guide.Fragments.WriteReviewWaterFragment;
import com.guide.green.green_guide.HTTPRequest.AbstractFormItem;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.HTTPRequest.POSTMultipartData;
import com.guide.green.green_guide.Utilities.BaiduSuggestion;
import com.guide.green.green_guide.Utilities.CredentialManager;
import com.guide.green.green_guide.Utilities.ImageResizer;
import com.guide.green.green_guide.Utilities.PictureCarouselAdapter;
import com.guide.green.green_guide.Utilities.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class WriteReviewActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PICK_IMAGE = 12;
    private final static int FRAGMENT_CONTAINER = R.id.fragment_container;

    private int mCurrentPage, mLastPage;
    private Review mReview = new Review();
    private WriteReviewGeneralFragment mWriteReviewGeneral;
    private ArrayList<WriteReviewPage> mPages = new ArrayList<>();
    private ArrayList<ViewGroup> mPageContainers = new ArrayList<>();

    private boolean currentlyEditing = false;

    public static abstract class WriteReviewPage extends Fragment {
        private int mPageNumber, mTotalPages;
        public void setPageNumber(int pageNum, int totalPages) {
            mPageNumber = pageNum;
            mTotalPages = totalPages;
            displayPageNumber();
        }

        public abstract TextView getPageNumberTextView();

        /**
         * Called whenever tha page number is changed and when the root view is first created.
         */
        public void displayPageNumber() {
            TextView tv = getPageNumberTextView();
            if (tv != null) {
                tv.setText("Page " + mPageNumber + " of " + mTotalPages);
            }
        }

        public interface OnPageChangeListener {
            void onPageChange(PageDirection direction);
        }
        public enum PageDirection { NEXT, PREVIOUS }
        public abstract void setOnPageChange(OnPageChangeListener listener);
    }

    private WriteReviewPage.OnPageChangeListener onPageChangeListener = new WriteReviewPage.OnPageChangeListener() {
        @Override
        public void onPageChange(WriteReviewPage.PageDirection direction) {
            if (direction == WriteReviewPage.PageDirection.NEXT) {
                openNextPage();
            } else {
                openPreviousPage();
            }
        }
    };

    private void openPreviousPage() {
        if (mCurrentPage > 0) {
            mCurrentPage -= 1;
            openPage(mCurrentPage);
        }
    }

    private void openNextPage() {
        Review.ReviewCategory components[] = new Review.ReviewCategory[] {
                mReview.location,
                mReview.airWaste,
                mReview.solidWaste,
                mReview.waterIssue
        };

        if (mCurrentPage < mPages.size() - 1) {
            mCurrentPage += 1;
            openPage(mCurrentPage);
        } else {
            submit();
        }
    }

    private void submit() {
        Review.ReviewCategory components[] = new Review.ReviewCategory[] {
                mReview.location,
                mReview.airWaste,
                mReview.solidWaste,
                mReview.waterIssue
        };

        final ArrayList<AbstractFormItem> formItems = new ArrayList<>();
        for (Review.ReviewCategory component : components) {
            for (Review.Key k : component.allKeys()) {
                if (k.postName != null) {
                    String value = component.get(k);
                    if (value == null) { value = ""; }
                    formItems.add(new AbstractFormItem.TextFormItem(k.postName, value));
                    Log.d("CHECK", "FORMITEM: " + k.postName + ", " + value);
                }
            }
        }

        if (currentlyEditing) {
            ImageResizer.ResizeCompletedListener listener = new ImageResizer.ResizeCompletedListener() {
                @Override
                public void onResizeCompleted(AbstractFormItem.FileFormItem[] images) {
                    for (AbstractFormItem formItem : images) {
                        formItems.add(formItem);
                    }

                    ArrayList<Pair<String, String>> httpHeader = null;
                    if (CredentialManager.isLoggedIn()) {
                        httpHeader = new ArrayList<>();
                        formItems.add(new AbstractFormItem.TextFormItem("name", CredentialManager.getUsername()));
                        String reviewId = String.valueOf(Integer.parseInt(CredentialManager.getMyReview().id));
                        formItems.add(new AbstractFormItem.TextFormItem("id", reviewId));
                        //formItems.add(new AbstractFormItem.TextFormItem("id", CredentialManager.getMyReview().id));
                        //Log.d("EDITCHECK Review", reviewId);
                        Log.d("EDITCHECK Username", CredentialManager.getUsername());
                        Log.d("EDITCHECK ReviewID", CredentialManager.getMyReview().id);
                    }

                    /*for (AbstractFormItem item : formItems) {
                        item.getHeader()
                        Log.d("SENDING_NAME", item.getName().toString());
                        Log.d("SENDING_VALUE", item.getValue().toString());
                        Log.d("SENDING_VALUE", item.getValue().toString());
                    }*/

                    AsyncRequest.postMultipartData("http://www.lovegreenguide.com/s_edit_app.php",
                            formItems, new AbstractRequest.OnRequestResultsListener<StringBuilder>() {


                                @Override
                                public void onSuccess(StringBuilder sb) {
                                    Log.d("EDITCHECK", sb.toString());
                                    Toast.makeText(WriteReviewActivity.this, "Review Edit Submitted! " + sb.toString(), Toast.LENGTH_LONG).show();

                                }

                                @Override
                                public void onError(Exception mException) {
                                    Log.d("EPP", "****************");
                                    Log.d("EPP", mException.getMessage());
                                    Log.d("EPP", mException.toString());
                                    mException.printStackTrace();
                                    for (StackTraceElement elem : mException.getStackTrace()) {
                                        Log.d("EPP", elem.toString());
                                    }
                                }
                            });

                }
            };
            int oneMegaByte = 1048576; // 2^20
            ImageResizer.resizeImages(getApplicationContext(), oneMegaByte, "image[]",
                    mWriteReviewGeneral.getImageUris(), listener);

            Toast.makeText(this, "Submitting Edited Review", Toast.LENGTH_LONG).show();
            finish();

        } else {
            ImageResizer.ResizeCompletedListener listener = new ImageResizer.ResizeCompletedListener() {
                @Override
                public void onResizeCompleted(AbstractFormItem.FileFormItem[] images) {
                    for (AbstractFormItem formItem : images) {
                        formItems.add(formItem);
                    }

                    ArrayList<Pair<String, String>> httpHeader = null;
                    if (CredentialManager.isLoggedIn()) {
                        httpHeader = new ArrayList<>();
                        httpHeader.add(CredentialManager.getCookie());
                        formItems.add(new AbstractFormItem.TextFormItem("token",
                                CredentialManager.getToken()));
                        formItems.add(new AbstractFormItem.TextFormItem("s_name", CredentialManager.getUsername()));
                    }

                    AsyncRequest.postMultipartData("http://www.lovegreenguide.com/savereview_app.php",
                            formItems, new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                                @Override
                                public void onSuccess(StringBuilder sb) {
                                    Log.d("*********", "yyy: " + (sb == null ? "NULL" : sb.toString()));
                                    Toast.makeText(WriteReviewActivity.this, "Review Submitted!", Toast.LENGTH_LONG).show();
                                }
                            }, httpHeader);

                }
            };
            int oneMegaByte = 1048576; // 2^20
            ImageResizer.resizeImages(getApplicationContext(), oneMegaByte, "image[]",
                    mWriteReviewGeneral.getImageUris(), listener);

            Toast.makeText(this, "Submitting Review", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        Bundle bundle = getIntent().getExtras();

        if (bundle.keySet().contains("Editing")) {
            mReview = CredentialManager.getMyReview();

            Toast.makeText(this, "Retrieved location: " + mReview.imageCount, Toast.LENGTH_LONG).show();
        }


        mWriteReviewGeneral = new WriteReviewGeneralFragment();
        mWriteReviewGeneral.setOnUploadImagesClicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFiles();
            }
        });

        mPages.add(mWriteReviewGeneral);

        WriteReviewWaterFragment waterFragment = new WriteReviewWaterFragment();
        WriteReviewAirFragment airFragment = new WriteReviewAirFragment();
        WriteReviewSolidFragment solidFragment = new WriteReviewSolidFragment();

        mPages.add(waterFragment);
        mPages.add(airFragment);
        mPages.add(solidFragment);

        int totalPages = mPages.size();
        for (int i = 0; i < totalPages; i++) {
            mPages.get(i).setPageNumber(i + 1, totalPages);
        }

        if (bundle != null) {
            for (Review.Location.Key key : Review.Location.Key.allKeys()) {
                if (bundle.containsKey(key.jsonName)) {
                    mReview.location.set(key, bundle.getString(key.jsonName));
                }
            }
        }

        if (bundle.keySet().contains("Editing")) {
            currentlyEditing = getIntent().getBooleanExtra("Editing", false);

            Toast.makeText(this, "currently editing: " + currentlyEditing, Toast.LENGTH_LONG).show();
        }

        mWriteReviewGeneral.setLocationObject(mReview.location);
        waterFragment.setWaterIssueObject(mReview.waterIssue);
        airFragment.setAirWasteObject(mReview.airWaste);
        solidFragment.setSolidWasteObject(mReview.solidWaste);

        for (WriteReviewPage page : mPages) {
            page.setOnPageChange(onPageChangeListener);
        }

        InitPageContainers();
        openPage(0);

        /*mWriteReviewGeneral.addPreviousImages();



        mReview.getImages(new GetReviewImagesHandler(mReview, findViewById(R.id.write_review_gen_selected_images_container)));

        ArrayList<AbstractFormItem> formItems = new ArrayList<>();
        formItems.add(new AbstractFormItem.TextFormItem("id", mReview.id));
        formItems.add(new AbstractFormItem.TextFormItem("img_token", "02242019work"));

        final POSTMultipartData.AsyncPostData postRequest = AsyncRequest.postMultipartData(
                "http://www.lovegreenguide.com/img_e_app.php", formItems, mImgHandler);*/
    }

    private AbstractRequest.OnRequestResultsListener<StringBuilder> mImgHandler =
            new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                @Override
                public void onSuccess(StringBuilder stringBuilder) {

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

                    Log.d("HI", "IMG_CHECK: " + stringBuilder.toString());

                    try {
                        int startingIndex = sb.toString().indexOf("[", sb.toString().indexOf("[") + 1);
                        jArr = new JSONArray(sb.toString().substring(startingIndex));
                        Log.d("HI", "IMG_CHECK2: " + jArr.toString());
                    } catch (Exception e) {

                    }
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

    private class GetReviewImagesHandler extends AbstractRequest.OnRequestResultsListener<List<String>> implements
            View.OnClickListener {
        private Review mReview;
        private View mRoot;

        public GetReviewImagesHandler(Review review, View rootView) {
            mReview = review;
            mRoot = rootView;
            mReview.getImages(this);
        }

        @Override
        public void onSuccess(List<String> imageUrls) {
            if (imageUrls.isEmpty()) {
                mRoot.setVisibility(View.GONE);
                return;
            }
            Log.d("HI", "IMAGE-CHECK: " + imageUrls.toString());
        }

        @Override
        public void onError(Exception error) {
            error.printStackTrace();
        }

        @Override
        public void onClick(View view) {
            mReview.getImages(this);
        }
    }

    private void selectImageFiles() {
        Intent mediaIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        mediaIntent.setType("image/*");
        mediaIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(mediaIntent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE
                && resultCode == Activity.RESULT_OK) {
            mWriteReviewGeneral.clearUploadImages();
            Uri singleSelectedImage = data.getData();
            if (singleSelectedImage != null) {
                mWriteReviewGeneral.addUploadImage(singleSelectedImage);
                Log.d( ">", "Image URI= " + singleSelectedImage);
            } else {
                ClipData extraData = data.getClipData();
                for (int i = extraData.getItemCount() - 1; i >= 0; i--) {
                    Uri imgUri = extraData.getItemAt(i).getUri();
                    mWriteReviewGeneral.addUploadImage(imgUri);
                    Log.d(i + ">", "Image URI= " + imgUri);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentPage == 0) {
            super.onBackPressed();
        } else {
            openPreviousPage();
        }
    }

    private void InitPageContainers() {
        ViewGroup fragContainer = findViewById(FRAGMENT_CONTAINER);
        for (Fragment fragment : mPages) {
            FrameLayout parent = new FrameLayout(this);
            fragContainer.addView(parent);
            parent.setId(FRAGMENT_CONTAINER + 1 + mPageContainers.size());
            ViewGroup.LayoutParams lParams = parent.getLayoutParams();
            lParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            parent.setLayoutParams(lParams);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(parent.getId(), fragment);
            transaction.commit();
            parent.setVisibility(View.GONE);
            mPageContainers.add(parent);
        }
        mPageContainers.get(0).setVisibility(View.VISIBLE);
    }

    private void openPage(int pageNumber) {
        if (pageNumber >= 0 && pageNumber < mPageContainers.size()) {
            mPageContainers.get(mLastPage).setVisibility(View.GONE);
            mPageContainers.get(pageNumber).setVisibility(View.VISIBLE);
            mLastPage = pageNumber;
        }
    }

    public static void open(Activity act) {
        Intent intent = new Intent(act, WriteReviewActivity.class);
        act.startActivity(intent);
    }

    public static void open(Activity act, BaiduSuggestion.Location baiduLocation, String industry) {
        Intent intent = new Intent(act, WriteReviewActivity.class);
        intent.putExtra(Review.Location.Key.COMPANY.jsonName, baiduLocation.name);
        intent.putExtra(Review.Location.Key.ADDRESS.jsonName, baiduLocation.address);
        intent.putExtra(Review.Location.Key.CITY.jsonName, baiduLocation.city);
        intent.putExtra(Review.Location.Key.LAT.jsonName, Double.toString(baiduLocation.point.latitude));
        intent.putExtra(Review.Location.Key.LNG.jsonName, Double.toString(baiduLocation.point.longitude));
        if (industry != null) {
            intent.putExtra(Review.Location.Key.INDUSTRY.jsonName, industry.substring("Industry: ".length()));
        }
        act.startActivity(intent);
    }

    public static void open(Activity act, String name, String address, String city, double latitude,
                            double longitude, String industry) {

        Intent intent = new Intent(act, WriteReviewActivity.class);
        intent.putExtra(Review.Location.Key.COMPANY.jsonName, name);
        intent.putExtra(Review.Location.Key.ADDRESS.jsonName, address);
        intent.putExtra(Review.Location.Key.CITY.jsonName, city);
        intent.putExtra(Review.Location.Key.LAT.jsonName, Double.toString(latitude));
        intent.putExtra(Review.Location.Key.LNG.jsonName, Double.toString(longitude));
        if (industry != null) {
            intent.putExtra(Review.Location.Key.INDUSTRY.jsonName, industry);
        }
        act.startActivity(intent);
    }

    public static void open(Activity act, Review review) {
        Intent intent = new Intent(act, WriteReviewActivity.class);
        act.startActivity(intent);
    }
}
