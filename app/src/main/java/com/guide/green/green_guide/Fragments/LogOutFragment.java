package com.guide.green.green_guide.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.guide.green.green_guide.LogInOutSignUpActivity;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.CredentialManager;

public class LogOutFragment extends AbstractLogInOutSignup {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_logout, null);
        root.findViewById(R.id.log_out_log_in_as_other).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switchPageTo(LogInOutSignUpActivity.PageOption.LOGIN);
                    }
                });
        root.findViewById(R.id.log_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logUserOut();
            }
        });
        return root;
    }

    private void logUserOut() {
        CredentialManager.logOut(getContext());
        switchPageTo(LogInOutSignUpActivity.PageOption.LOGIN);
    }
}
