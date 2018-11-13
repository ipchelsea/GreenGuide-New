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
import android.widget.Toast;
import com.guide.green.green_guide.Fragments.WriteReviewAirFragment;
import com.guide.green.green_guide.Fragments.WriteReviewGeneralFragment;
import com.guide.green.green_guide.Fragments.WriteReviewSolidFragment;
import com.guide.green.green_guide.Fragments.WriteReviewWaterFragment;
import com.guide.green.green_guide.Utilities.AsyncPostRequest;
import com.guide.green.green_guide.Utilities.BaiduSuggestion;
import com.guide.green.green_guide.Utilities.Review;
import com.guide.green.green_guide.Utilities.SimplePOSTRequest;
import java.util.ArrayList;

public class WriteReviewActivity  extends AppCompatActivity {
    public static final int REQUEST_CODE_PICK_IMAGE = 12;
    private final static int FRAGMENT_CONTAINER = R.id.fragment_container;

    private int mCurrentPage;
    private Review mReview = new Review();
    private WriteReviewGeneralFragment mWriteReviewGeneral;
    private ArrayList<WriteReviewPage> mPages = new ArrayList<>();

    public static abstract class WriteReviewPage extends Fragment {
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
        if (mCurrentPage < mPages.size() - 1) {
            mCurrentPage += 1;
            openPage(mCurrentPage);
        } else {
            submit();
        }
    }

    private void submit() {
        mReview.location.set(Review.Location.Key.INDUSTRY, "StanTestFromAndroidCategory");
        mReview.location.set(Review.Location.Key.PRODUCT, "StanTestFromAndroidProduct");
        mReview.location.set(Review.Location.Key.RATING, "0");
        mReview.location.set(Review.Location.Key.REVIEW, "StanTestFromAndroidPerformanceReview");

        Review.ReviewComponent components[] = new Review.ReviewComponent[] {
                mReview.location,
                mReview.airWaste,
                mReview.solidWaste,
                mReview.waterIssue
        };

        ArrayList<SimplePOSTRequest.FormItem> formItems = new ArrayList<>();
        for (Review.ReviewComponent component : components) {
            for (Review.Key k : component.getKeys()) {
                if (k.postName != null) {
                    String value = component.get(k);
                    if (value == null) { value = ""; }
                    formItems.add(new SimplePOSTRequest.TextFormItem(k.postName, value));
                }
            }
        }

        for (Uri imgUir : mWriteReviewGeneral.getImageUris()) {
            formItems.add(new SimplePOSTRequest.UriFileFormItem("image[]", imgUir, this));
        }

        (new AsyncPostRequest("http://www.lovegreenguide.com/savereview_app.php") {
            @Override
            public void onPostExecute(StringBuilder sb) {
                Log.i("*********", sb == null ? "NULL" : sb.toString());
                Toast.makeText(WriteReviewActivity.this, "!!!!Review Submitted!!!!", Toast.LENGTH_LONG).show();
            }
        }).execute(formItems);

        Toast.makeText(this, "Review Submitted", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_write_review);

        mWriteReviewGeneral = new WriteReviewGeneralFragment();
        mWriteReviewGeneral.setOnUploadImagesClicked(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImageFiles();
            }
        });

        mPages.add(mWriteReviewGeneral);
        mPages.add(new WriteReviewWaterFragment());
        mPages.add(new WriteReviewAirFragment());
        mPages.add(new WriteReviewSolidFragment());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            for (Review.Location.Key key : Review.Location.Key.keys) {
                if (bundle.containsKey(key.jsonName)) {
                    mReview.location.set(key, bundle.getString(key.jsonName));
                }
            }
        }

        mWriteReviewGeneral.setLocationObject(mReview.location);

        for (WriteReviewPage page : mPages) {
            page.setOnPageChange(onPageChangeListener);
        }

        openPage(0);
    }

    private void selectImageFiles() {
        Intent mediaIntent = new Intent(Intent.ACTION_GET_CONTENT);
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

    private Fragment openPage(int pageNumber) {
        Fragment fragment = mPages.get(pageNumber);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mCurrentPage < 0) {
            transaction.add(FRAGMENT_CONTAINER, fragment);
        } else {
            transaction.replace(FRAGMENT_CONTAINER, fragment);
        }
        transaction.commit();
        mCurrentPage = pageNumber;
        return fragment;
    }

    public static void open(Activity act) {
        Intent intent = new Intent(act, WriteReviewActivity.class);
        act.startActivity(intent);
    }

    public static void open(Activity act, BaiduSuggestion.Location baiduLocation) {
        Intent intent = new Intent(act, WriteReviewActivity.class);
        intent.putExtra(Review.Location.Key.COMPANY.jsonName, baiduLocation.name);
        intent.putExtra(Review.Location.Key.ADDRESS.jsonName, baiduLocation.address);
        intent.putExtra(Review.Location.Key.CITY.jsonName, baiduLocation.city);
        intent.putExtra(Review.Location.Key.LAT.jsonName, Double.toString(baiduLocation.point.latitude));
        intent.putExtra(Review.Location.Key.LNG.jsonName, Double.toString(baiduLocation.point.longitude));
        act.startActivity(intent);
    }
}
