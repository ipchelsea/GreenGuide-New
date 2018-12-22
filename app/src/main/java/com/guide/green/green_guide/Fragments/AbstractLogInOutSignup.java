package com.guide.green.green_guide.Fragments;

import android.support.v4.app.Fragment;

import com.guide.green.green_guide.LogInOutSignUpActivity;

public abstract class AbstractLogInOutSignup extends Fragment {
    private LogInOutSignUpActivity.PageOption mPendingPageToChangeTo = null;
    private LogInOutSignUpActivity mUserManager;

    public void setUserManager(LogInOutSignUpActivity userManager) {
        mUserManager = userManager;
        if (mPendingPageToChangeTo != null) {
            mUserManager.setPage(mPendingPageToChangeTo);
        }
    }

    protected void switchPageTo(LogInOutSignUpActivity.PageOption page) {
        if (mUserManager == null) {
            mPendingPageToChangeTo = page;
        } else {
            mUserManager.setPage(page);
        }
    }
}
