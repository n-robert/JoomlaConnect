package org.nrobert.joomlaconnect;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

public class SessionHandler {
    private static final String PREF_NAME = "UserSession";
    protected static final String KEY_PROTOCOL = "protocol";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_AUTOLOGIN = "autoLogin";
    private static final String KEY_TOKEN = "token";
    private static SessionHandler mInstance;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private Boolean mLoggedIn;
    private Boolean mTaskSuccessful;

    public SessionHandler(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        mLoggedIn = false;
        mTaskSuccessful = false;
    }

    public static synchronized SessionHandler getInstance(Context context) {
        if (null == mInstance) {
            mInstance = new SessionHandler(context);
        }

        return mInstance;
    }

    public void loginUser(String protocol, String domain, String username, String password, String fullName, String message, int expires) {
        setTaskSuccessful(false);
        setLoggedIn(true);

        editor.putString(KEY_PROTOCOL, protocol);
        editor.putString(KEY_DOMAIN, domain);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_FULL_NAME, fullName);
        editor.putString(KEY_MESSAGE, message);
        editor.putBoolean(KEY_AUTOLOGIN, true);

        Date date = new Date();
        long millis = date.getTime() + (expires * 60 * 1000);
        editor.putLong(KEY_EXPIRES, millis);

        setTaskSuccessful(editor.commit() && isLoggedIn());
    }

    public void setTaskSuccessful(boolean success) {
        mTaskSuccessful = success;
    }

    public boolean isTaskSuccessful() {
        return mTaskSuccessful;
    }

    public boolean isLoggedIn() {
        if (mLoggedIn == false) {
            return false;
        }

        Date currentDate = new Date();
        long millis = mSharedPreferences.getLong(KEY_EXPIRES, 0);

        if (millis == 0) {
            return false;
        }

        Date expiryDate = new Date(millis);

        return currentDate.before(expiryDate);
    }

    public void setLoggedIn(boolean loggedIn) {
        mLoggedIn = loggedIn;
    }

    public User getUser() {
        User user = User.getInstance();
        user.setUsername(mSharedPreferences.getString(KEY_USERNAME, null));
        user.setFullName(mSharedPreferences.getString(KEY_FULL_NAME, null));
        user.setSessionExpiryDate(new Date(mSharedPreferences.getLong(KEY_EXPIRES, 0)));
        user.setMessage(mSharedPreferences.getString(KEY_MESSAGE, null));

        return user;
    }

    public void logoutUser() {
        setTaskSuccessful(false);
        setLoggedIn(false);

        User user = User.getInstance();
        user.setLogOutSuccess(true);

        editor.putBoolean(KEY_AUTOLOGIN, false);
        editor.putLong(KEY_EXPIRES, 0);

        setTaskSuccessful(editor.commit() && user.getLogOutSuccess());
    }

    public void updateToken(String token) {
        setTaskSuccessful(false);

        editor.putString(KEY_TOKEN, token);

        setTaskSuccessful(editor.commit());
    }
}