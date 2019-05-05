package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.guide.green.green_guide.R;
import java.util.HashSet;
import java.util.Objects;

public class CredentialManager {
    private static String mToken = null;
    private static String mUsername = null;
    private static boolean mIsLoggedIn = false;
    private static boolean mIsPemenantlyLoggedIn = false;
    private static HashSet<OnLoginStateChanged> mListeners = new HashSet<>();
    private static Review mReview;

    /**
     * Callback interface for responding to a user being logged in or out.
     * Prior to the object's 'initialize' method being invoked, it is assumed that no user is
     * logged in. So the starting login state is that no user is logged in.
     */
    public interface OnLoginStateChanged {
        void onLoginStateChanged(boolean isLoggedIn);
    }

    private static void notifyListeners() {
        for (OnLoginStateChanged listener : mListeners) {
            listener.onLoginStateChanged(mIsLoggedIn);
        }
    }

    public static void addLoginStateChangedListener(OnLoginStateChanged listener) {
        mListeners.add(listener);
    }

    public static void removeLoginStateChangedListener(OnLoginStateChanged listener) {
        mListeners.remove(listener);
    }

    public static void initialize(Context context) {
        SharedPreferences sharedPref  = context.getSharedPreferences(
                context.getString(R.string.preference_file_credentials), Context.MODE_PRIVATE);

        String uNameKey = context.getString(R.string.preference_credentials_token);
        String tokenKey = context.getString(R.string.preference_credentials_user_name);
        if (sharedPref.contains(uNameKey) && sharedPref.contains(tokenKey)) {
            mToken = sharedPref.getString(tokenKey, null);
            mUsername = sharedPref.getString(uNameKey, null);
            mIsPemenantlyLoggedIn = true;
            mIsLoggedIn = true;
            notifyListeners();
        }
    }

    public static boolean isLoggedIn() {
        return mIsLoggedIn;
    }

    public static boolean isPermenantlyLoggedIn() {
        return mIsPemenantlyLoggedIn;
    }

    public static String getToken() {
        return mToken;
    }

    public static String getUsername() {
        return mUsername;
    }

    public static void logOut(Context context) {
        if (mIsLoggedIn) {
            String uNameKey = context.getString(R.string.preference_credentials_token);
            String tokenKey = context.getString(R.string.preference_credentials_user_name);

            SharedPreferences sharedPref  = context.getSharedPreferences(
                    context.getString(R.string.preference_file_credentials), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            if (sharedPref.contains(uNameKey)) { editor.remove(uNameKey); }
            if (sharedPref.contains(tokenKey)) { editor.remove(tokenKey); }
            editor.apply();

            mIsPemenantlyLoggedIn = false;
            mIsLoggedIn = false;
            mUsername = null;
            mToken = null;

            notifyListeners();
        }
    }

    /**
     * Sets a token to be the currently used token and saves/removed tokens from permanent storage.
     *
     * @param token the value that will identify the user to the server.
     * @param username a value that the user used to identify themselves.
     * @param context if null, the token in permanent storage is removed, else used to permanently
     *                save the provided token.
     */
    public static void logIn(@NonNull String token, @NonNull String username,
                             @Nullable Context context) {
        Objects.requireNonNull(token, "The token must be supplied");
        logOut(context);
        mToken = token;
        mUsername = username;
        mIsLoggedIn = true;
        if (context != null) {
            String uNameKey = context.getString(R.string.preference_credentials_token);
            String tokenKey = context.getString(R.string.preference_credentials_user_name);
            SharedPreferences sharedPref  = context.getSharedPreferences(
                    context.getString(R.string.preference_file_credentials), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(uNameKey, mUsername);
            editor.putString(tokenKey, mToken);
            editor.apply();
            mIsPemenantlyLoggedIn = true;
        }
        notifyListeners();
    }

    public static Pair<String, String> getCookie() {
        if (mIsLoggedIn) {
            return new Pair<String, String>("PHPSESSID", getToken());
        }
        return null;
    }

    public static void setMyReview(Review review) {
        mReview = review;
    }

    public static Review getMyReview() {
        return mReview;
    }
}