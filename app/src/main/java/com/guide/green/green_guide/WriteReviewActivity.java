package com.guide.green.green_guide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.guide.green.green_guide.Fragments.WriteReviewAirFragment;
import com.guide.green.green_guide.Fragments.WriteReviewGeneralFragment;
import com.guide.green.green_guide.Fragments.WriteReviewSolidFragment;
import com.guide.green.green_guide.Fragments.WriteReviewWaterFragment;

import java.util.ArrayList;

public class WriteReviewActivity  extends AppCompatActivity {
    private ArrayList<WriteReviewPage> mPages = new ArrayList<>();
    private int mCurrentPage;
    private final static int fragment_container = R.id.fragment_container;

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

    public void openPreviousPage() {
        if (mCurrentPage > 0) {
            mCurrentPage -= 1;
            openPage(mCurrentPage);
        }
    }

    public void openNextPage() {
        if (mCurrentPage < mPages.size() - 1) {
            mCurrentPage += 1;
            openPage(mCurrentPage);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_write_review);

        mPages.add(new WriteReviewGeneralFragment());
        mPages.add(new WriteReviewWaterFragment());
        mPages.add(new WriteReviewAirFragment());
        mPages.add(new WriteReviewSolidFragment());

        for (WriteReviewPage page : mPages) {
            page.setOnPageChange(onPageChangeListener);
        }

        openPage(0);
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
            transaction.add(fragment_container, fragment);
        } else {
            transaction.replace(fragment_container, fragment);
        }
        transaction.commit();
        mCurrentPage = pageNumber;
        return fragment;
    }

    public static void open(Activity act) {
        Intent intent = new Intent(act, WriteReviewActivity.class);
        act.startActivity(intent);
    }
}
