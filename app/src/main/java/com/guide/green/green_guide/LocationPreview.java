package com.guide.green.green_guide;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.guide.green.green_guide.Utilities.Review;

import java.util.ArrayList;

public class LocationPreview extends Fragment {

    private onLocationPreviewListener mCallback;

    private ArrayList<Review> reviews;

    public LocationPreview() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LocationPreview.
     */
    public static LocationPreview newInstance() {
        LocationPreview fragment = new LocationPreview();
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
        Log.d("Check", "Created");
        View view = inflater.inflate(R.layout.fragment_location_preview, container, false);

        LinearLayout lp = view.findViewById(R.id.FragmentLayout);

        lp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onPreviewFragmentClicked();
            }
        });

        return view;
    }

    public void setOnLocationPreviewListener(Activity activity) {
        mCallback = (onLocationPreviewListener) activity;
    }

    public interface onLocationPreviewListener {
        public void onPreviewFragmentClicked();
        public void onPreviewFragmentCreated();
    }
}
