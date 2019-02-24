package com.guide.green.green_guide.Fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guide.green.green_guide.LocationInfoActivity;
import com.guide.green.green_guide.LocationPreview;
import com.guide.green.green_guide.R;

/**
 * A simple {@link Fragment} subclass.
 * to handle interaction events.
 * Use the {@link GeneralInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GeneralInfoFragment extends Fragment {

    private OnGeneralFragmentListener mListener;

    public GeneralInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment GeneralInfoFragment.
     */
    public static GeneralInfoFragment newInstance() {
        GeneralInfoFragment fragment = new GeneralInfoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_general_info, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGeneralFragmentListener) {
            mListener = (OnGeneralFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnGeneralFragmentListener");
        }
    }

    public void setOnGeneralFragmentListener(Activity activity) {
        mListener = (OnGeneralFragmentListener) activity;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnGeneralFragmentListener {
        void onGeneralFragmentInteraction();
    }
}
