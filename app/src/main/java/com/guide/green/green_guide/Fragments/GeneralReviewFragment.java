package com.guide.green.green_guide.Fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
public class GeneralReviewFragment extends Fragment {

    private OnGeneralReviewFragmentListener mListener;

    public GeneralReviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GeneralReviewFragment.
     */
    public static GeneralReviewFragment newInstance() {
        GeneralReviewFragment fragment = new GeneralReviewFragment();
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
        Log.d("General", "OnCreate");
        return inflater.inflate(R.layout.fragment_general_review, container, false);
    }

    @Override
    public void onStart() {

        super.onStart();
        mListener.onGeneralReviewInteraction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGeneralReviewFragmentListener) {
            mListener = (OnGeneralReviewFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGeneralReviewFragmentListener");
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
    public interface OnGeneralReviewFragmentListener {
        void onGeneralReviewInteraction();
    }

    public void setOnGeneralReviewFragmentListener(Activity activity) {
        mListener = (OnGeneralReviewFragmentListener) activity;
    }
}
