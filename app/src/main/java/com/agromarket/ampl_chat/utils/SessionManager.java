package com.agromarket.ampl_chat.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "AM_Chat_Pref";
    private static final String TOKEN_KEY = "api_token";
    private static final String USER_ID_KEY = "user_id";
    private static final String USER_ROLE_KEY = "user_role";

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveToken(String token) {
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public void saveUserId(int userId) {
        editor.putInt(USER_ID_KEY, userId);
        editor.apply();
    }

    public void saveUserRole(String userRole) {
        editor.putString(USER_ROLE_KEY, userRole);
        editor.apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(USER_ID_KEY, -1);
    }
    public String getUserRole() {
        return sharedPreferences.getString(USER_ROLE_KEY, "customer");
    }

    public void clear() {
        editor.clear().apply();
    }
}