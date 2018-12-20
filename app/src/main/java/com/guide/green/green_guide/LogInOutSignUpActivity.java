package com.guide.green.green_guide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.guide.green.green_guide.Fragments.AbstractLogInOutSignup;
import com.guide.green.green_guide.Fragments.LogInFragment;
import com.guide.green.green_guide.Fragments.LogOutFragment;
import com.guide.green.green_guide.Fragments.SignUpFragment;
import com.guide.green.green_guide.Utilities.CredentialManager;


public class LogInOutSignUpActivity extends AppCompatActivity {
    public enum PageOption { LOGIN, LOGOUT, SIGN_UP }
    private AbstractLogInOutSignup[] fragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        fragments = new AbstractLogInOutSignup[3];
        fragments[PageOption.LOGIN.ordinal()] = new LogInFragment();
        fragments[PageOption.LOGOUT.ordinal()] = new LogOutFragment();
        fragments[PageOption.SIGN_UP.ordinal()] = new SignUpFragment();
        for (AbstractLogInOutSignup frag : fragments) {
            frag.setUserManager(this);
        }

        if (CredentialManager.isLoggedIn()) {
            setPage(PageOption.LOGOUT);
        } else {
            setPage(PageOption.LOGIN);
        }
    }

    public void setPage(PageOption page) {
        Fragment fragment = fragments[page.ordinal()];
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }
    }

    public static void startActivity(Activity act) {
        Intent intent = new Intent(act, LogInOutSignUpActivity.class);
        act.startActivity(intent);
    }
}
