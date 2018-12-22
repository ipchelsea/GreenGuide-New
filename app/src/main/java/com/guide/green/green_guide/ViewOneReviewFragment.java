package com.guide.green.green_guide;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guide.green.green_guide.Utilities.Review;


/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 * Use the {@link ViewOneReviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewOneReviewFragment extends Fragment {

    Review thisReview;

    public ViewOneReviewFragment() {
        // Required empty public constructor
    }

    public static ViewOneReviewFragment newInstance(Review review) {
        ViewOneReviewFragment fragment = new ViewOneReviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_one_review, container, false);
    }
}
