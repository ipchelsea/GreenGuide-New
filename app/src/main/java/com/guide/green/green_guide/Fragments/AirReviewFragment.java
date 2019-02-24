package com.guide.green.green_guide.Fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guide.green.green_guide.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link GeneralReviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AirReviewFragment extends Fragment {

    private OnAirReviewFragmentListener mListener;

    public AirReviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GeneralReviewFragment.
     */
    public static AirReviewFragment newInstance() {
        AirReviewFragment fragment = new AirReviewFragment();
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
        return inflater.inflate(R.layout.fragment_air_review, container, false);
    }

    @Override
    public void onStart() {

        super.onStart();
        mListener.onAirReviewInteraction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAirReviewFragmentListener) {
            mListener = (OnAirReviewFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAirReviewFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     */
    public interface OnAirReviewFragmentListener {
        void onAirReviewInteraction();
    }

    public void setOnAirReviewFragmentListener(Activity activity) {
        mListener = (OnAirReviewFragmentListener) activity;
    }
}
