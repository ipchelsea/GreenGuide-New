package com.guide.green.green_guide;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.guide.green.green_guide.Fragments.WriteReviewAirFragment;
import com.guide.green.green_guide.Fragments.WriteReviewGeneralFragment;
import com.guide.green.green_guide.Fragments.WriteReviewSolidFragment;
import com.guide.green.green_guide.Fragments.WriteReviewWaterFragment;
import com.guide.green.green_guide.HTTPRequest.AbstractFormItem;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.Utilities.BaiduSuggestion;
import com.guide.green.green_guide.Utilities.Review;
import java.util.ArrayList;

public class WriteReviewActivity  extends AppCompatActivity {
    public static final int REQUEST_CODE_PICK_IMAGE = 12;
    private final static int FRAGMENT_CONTAINER = R.id.fragment_container;

    private int mCurrentPage, mLastPage;
    private Review mReview = new Review();
    private WriteReviewGeneralFragment mWriteReviewGeneral;
    private ArrayList<WriteReviewPage> mPages = new ArrayList<>();
    private ArrayList<ViewGroup> mPageContainers = new ArrayList<>();

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
        for (Review.ReviewCategory component : components) {
            for (Review.Key k : component.allKeys()) {
                String value = component.get(k);
                if (value != null) {
                    Log.i(">>>>", "(" + k + ", " + value + ")");
                }
            }
        }


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

        ArrayList<AbstractFormItem> formItems = new ArrayList<>();
        for (Review.ReviewCategory component : components) {
            for (Review.Key k : component.allKeys()) {
                if (k.postName != null) {
                    String value = component.get(k);
                    if (value == null) { value = ""; }
                    formItems.add(new AbstractFormItem.TextFormItem(k.postName, value));
                }
            }
        }

        for (Uri imgUir : mWriteReviewGeneral.getImageUris()) {
            formItems.add(new AbstractFormItem.UriFileFormItem("image[]", imgUir, this));
        }

        AsyncRequest.postMultipartData("http://www.lovegreenguide.com/savereview_app.php",
                formItems, new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                    @Override
                    public void onSuccess(StringBuilder sb) {
                        Log.i("*********", sb == null ? "NULL" : sb.toString());
                        Toast.makeText(WriteReviewActivity.this, "Review Submitted", Toast.LENGTH_LONG).show();
                    }
                });

        Toast.makeText(this, "Submitting Review", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            for (Review.Location.Key key : Review.Location.Key.allKeys()) {
                if (bundle.containsKey(key.jsonName)) {
                    mReview.location.set(key, bundle.getString(key.jsonName));
                }
            }
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
}
