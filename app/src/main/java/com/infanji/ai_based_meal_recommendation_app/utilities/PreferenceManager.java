package com.infanji.ai_based_meal_recommendation_app.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(Constants.KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false);
    }

    public void setUsername(String username) {
        editor.putString(Constants.KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(Constants.KEY_USERNAME, "Guest");
    }

    public void setEmail(String email) {
        editor.putString(Constants.KEY_EMAIL, email);
        editor.apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(Constants.KEY_EMAIL, "");
    }

    public void setUid(String uid) {
        editor.putString(Constants.KEY_UID, uid);
        editor.apply();
    }

    public String getUid() {
        return sharedPreferences.getString(Constants.KEY_UID, "");
    }

    public void setNotCompletedLogin(boolean notCompletedLogin) {
        editor.putBoolean(Constants.KEY_NOT_COMPLETED_LOGIN, notCompletedLogin);
        editor.apply();
    }

    public boolean isNotCompletedLogin() {
        return sharedPreferences.getBoolean(Constants.KEY_NOT_COMPLETED_LOGIN, false);
    }

    public void setGender(String gender) {
        editor.putString(Constants.KEY_GENDER, gender);
        editor.apply();
    }

    public String getGender() {
        return sharedPreferences.getString(Constants.KEY_GENDER, "");
    }

    public void setPreferences(String preferences) {
        editor.putString(Constants.KEY_PREFERENCES, preferences);
        editor.apply();
    }

    public String getPreferences() {
        return sharedPreferences.getString(Constants.KEY_PREFERENCES, "");
    }

    public void setAge(String age) {
        editor.putString(Constants.KEY_AGE, age);
        editor.apply();
    }

    public String getAge() {
        return sharedPreferences.getString(Constants.KEY_AGE, "");
    }

    public void clearPreferences() {
        editor.clear();
        editor.apply();
    }
}