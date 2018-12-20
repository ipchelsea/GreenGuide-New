package com.guide.green.green_guide.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.guide.green.green_guide.Dialogs.LoadingDialog;
import com.guide.green.green_guide.HTTPRequest.AbstractFormItem;
import com.guide.green.green_guide.HTTPRequest.AbstractRequest;
import com.guide.green.green_guide.HTTPRequest.AsyncRequest;
import com.guide.green.green_guide.HTTPRequest.POSTMultipartData;
import com.guide.green.green_guide.LogInOutSignUpActivity;
import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.CredentialManager;

import java.util.ArrayList;

public class LogInFragment extends AbstractLogInOutSignup {
    private EditText mLogInUsername;
    private EditText mLogInPassword;
    private CheckBox mLogInRemember;
    private LoadingDialog ld;
    private String mTentativeToken;
    private String mTentativeUsername;
    private boolean mTentativeRememberUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ViewGroup mRoot = (ViewGroup) inflater.inflate(R.layout.fragment_login, null);
        mLogInUsername = mRoot.findViewById(R.id.log_in_username);
        mLogInPassword = mRoot.findViewById(R.id.log_in_password);
        mLogInRemember = mRoot.findViewById(R.id.log_in_remember_me);
        mRoot.findViewById(R.id.log_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logUserIn();
            }
        });
        mRoot.findViewById(R.id.log_in_goto_sign_up).findViewById(R.id.log_in_goto_sign_up)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switchPageTo(LogInOutSignUpActivity.PageOption.SIGN_UP);
                    }
                });
        return mRoot;
    }

    // Instance methods

    private void logUserIn() {
        mTentativeToken = "06232017Job$"; // Make the value randomly generated when the REST code is changed.
        mTentativeUsername = mLogInUsername.getText().toString();
        mTentativeRememberUser = mLogInRemember.isChecked();
        ArrayList<AbstractFormItem> formItems = new ArrayList<>();
        formItems.add(new AbstractFormItem.TextFormItem("email", mTentativeUsername));
        formItems.add(new AbstractFormItem.TextFormItem("password", mLogInPassword.getText().toString()));
        formItems.add(new AbstractFormItem.TextFormItem("token_signup", mTentativeToken));

        ld = new LoadingDialog();
        ld.show(getActivity().getFragmentManager(), "Login In...");
        final POSTMultipartData.AsyncPostData postRequest = AsyncRequest.postMultipartData(
                "http://www.lovegreenguide.com/login_app.php", formItems, mLogInHandler);
        ld.setCallback(new LoadingDialog.Canceled() {
            @Override
            public void onCancel() {
                postRequest.cancel(true);
            }
        });
    }

    private AbstractRequest.OnRequestResultsListener<StringBuilder> mLogInHandler =
            new AbstractRequest.OnRequestResultsListener<StringBuilder>() {
                @Override
                public void onSuccess(StringBuilder stringBuilder) {
                    if (stringBuilder.indexOf("Login successful") != -1) {
                        if (mTentativeRememberUser) {
                            CredentialManager.logIn(mTentativeToken, mTentativeUsername, getContext());
                        } else {
                            CredentialManager.logIn(mTentativeToken, mTentativeUsername, null);
                        }
                        switchPageTo(LogInOutSignUpActivity.PageOption.LOGOUT);
                        getActivity().finish();
                    }

                    Toast.makeText(getContext(),stringBuilder.toString(), Toast.LENGTH_LONG).show();
                    ld.dismiss();
                }

                @Override
                public void onCanceled() {
                    ld.dismiss();
                }

                @Override
                public void onError(Exception error) {
                    error.printStackTrace();
                    ld.dismiss();
                    Toast.makeText(getContext(),"Error Encountered", Toast.LENGTH_LONG).show();
                }
            };
}
