package org.nrobert.joomlaconnect;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via domain/login/password.
 */
public class MyAppActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    protected static final int REQUEST_READ_CONTACTS = 0;

    protected static final String PREF_NAME = "UserSession";
    protected static final String KEY_STATUS = "status";
    protected static final String KEY_MESSAGE = "message";
    protected static final String KEY_USERNAME = "username";
    protected static final String KEY_DOMAIN = "domain";
    protected static final String KEY_PASSWORD = "password";
    protected static final String KEY_FULL_NAME = "fullName";
    protected static final String KEY_EXPIRES = "expires";
    protected static final String KEY_AUTOLOGIN = "autoLogin";
    protected static final String KEY_LOGIN_TASK = "loginUser";
    protected static final String KEY_LOGOUT_TASK = "logoutUser";
    protected static final String KEY_TOKEN = "token";
    protected static final String KEY_UPDATE_TOKEN_TASK = "updateToken";
    protected static final String KEY_PROTOCOL = "protocol";
    protected static final String KEY_DEFAULT_PROTOCOL = "http";
    protected static final String KEY_PROTOCOL_SUFFIX = "://";
    protected static final String PROTOCOL_VALIDATE_PATTERN = "^(http(s){0,1}):\\/\\/";
    protected static final String
            DOMAIN_VALIDATE_PATTERN =
            "^((http(s){0,1}):{0,1}(\\/\\/){0,1}){0,1}((?:[a-zA-Z0-9](?:[a-zA-Z0-9-_]*[a-zA-Z0-9])\\.)+[a-z0-9]+)[\\/]{0,1}";
    protected User.UserTask mUserTask = null;
    protected EditText mDomainView;
    protected TextView mFormMessage;
    protected AutoCompleteTextView AutoCompleteTextView;
    protected View mProgressView, mFormView;
    protected SharedPreferences mSharedPreferences;
    protected String mStoredDomain, mStoredUsername, mStoredPassword;
    protected URI mUri;
    protected String mProtocol, mDomain;
    protected SessionHandler mSession;
    protected User mUser;
    protected MyCookieStore mCookieStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mProtocol = mSharedPreferences.getString(KEY_PROTOCOL, KEY_DEFAULT_PROTOCOL);
        mDomain = mSharedPreferences.getString(KEY_DOMAIN, null);
        mUri = URI.create(mProtocol + KEY_PROTOCOL_SUFFIX + mDomain);
        mCookieStore = new MyCookieStore(getApplicationContext(), mUri);
        CookieHandler.setDefault(new CookieManager(mCookieStore, CookiePolicy.ACCEPT_ALL));
        mSession = SessionHandler.getInstance(getApplicationContext());
        mUser = mSession.getUser();
    }

    protected void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    protected boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(AutoCompleteTextView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }

        return false;
    }

    protected boolean isProtocolValid(String protocol) {
        return !TextUtils.isEmpty(protocol) && protocol.matches(PROTOCOL_VALIDATE_PATTERN);
    }

    protected boolean isDomainValid(String domain) {
        return !TextUtils.isEmpty(domain) && domain.matches(DOMAIN_VALIDATE_PATTERN);
    }

    protected boolean isLoginValid(String login) {
        return !TextUtils.isEmpty(login) && login.matches("^[\\w]+$");
    }

    protected boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password);
    }

    protected boolean isEmailValid(String email) {
        return email.contains("@");
    }

    protected void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(MyAppActivity.this,
                                   android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        AutoCompleteTextView.setAdapter(adapter);
    }


    protected interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                                // Retrieve data rows for the device user's 'profile' contact.
                                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                                                     ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                                ProfileQuery.PROJECTION,

                                // Select only login.
                                ContactsContract.Contacts.Data.MIMETYPE +
                                " = ?",
                                new String[]{ContactsContract.CommonDataKinds.Email
                                        .CONTENT_ITEM_TYPE},

                                // Show primary login first. Note that there won't be
                                // a primary login if the user hasn't specified one.
                                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

//        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    public void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public String getDomain() {
        return mDomain;
    }

    public URI getUri() {
        return mUri;
    }

    public void setNullUserTask() {
        mUserTask = null;
    }

    public void loadLoginForm() {
        Intent i = new Intent(MyAppActivity.this, LoginActivity.class);
        startActivity(i);
    }

    public void loadDashboard() {
        Intent i = new Intent(MyAppActivity.this, DashboardActivity.class);
        startActivity(i);
        finish();
    }

    public void doTask(MyAppActivity mAppActivity, String task, String data, String login, String password) {
        if (mUserTask != null) {
            return;
        }

        showProgress(true);

        mUserTask = new User().new UserTask(mAppActivity, task, data, login, password);
        mUserTask.execute((Void) null);
    }
}

