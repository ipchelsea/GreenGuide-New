package com.guide.green.green_guide.Utilities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.guide.green.green_guide.Fragments.GeneralInfoFragment;
import com.guide.green.green_guide.Fragments.ReviewsInfoFragment;
import com.guide.green.green_guide.LocationPreview;

public class LocationInfoTabsPagerAdapter extends FragmentPagerAdapter {

    public LocationInfoTabsPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return GeneralInfoFragment.newInstance();
            case 1: return ReviewsInfoFragment.newInstance();
            default: return  GeneralInfoFragment.newInstance();
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return "General";
            case 1: return "Reviews";
            default: return "";
        }
    }
}
