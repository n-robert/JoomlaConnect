package org.nrobert.joomlaconnect;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;

public class LoginActivity extends MyAppActivity {
    private static final String TAG = "LoginActivity";
    private Switch mProtocolView;
    private EditText mPasswordView, mLoginView;
    private Boolean mAutoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mProgressView = findViewById(R.id.login_progress);
        mFormView = findViewById(R.id.login_form);
        mProtocolView = findViewById(R.id.protocol);

        if (mProtocolView != null) {
            mProtocolView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mProtocol = isChecked ? KEY_DEFAULT_PROTOCOL + "s" : KEY_DEFAULT_PROTOCOL;
                }
            });
        }

        mDomainView = findViewById(R.id.domain);
        mLoginView = findViewById(R.id.login);
        mPasswordView = findViewById(R.id.password);
        Button mLoginButton = findViewById(R.id.login_button);
        mFormMessage = findViewById(R.id.login_message);
//        populateAutoComplete();

        mStoredDomain = mSharedPreferences.getString(KEY_DOMAIN, null);
        mStoredUsername = mSharedPreferences.getString(KEY_USERNAME, null);
        mStoredPassword = mSharedPreferences.getString(KEY_PASSWORD, null);
        mAutoLogin = mSharedPreferences.getBoolean(KEY_AUTOLOGIN, false);

        if (null != mStoredDomain) {
            mDomain = mStoredDomain;
            mDomainView.setText(mStoredDomain);
            mLoginView.requestFocus();
        }

        if (null != mStoredUsername) {
            mLoginView.setText(mStoredUsername);
        }

        if (null != mStoredPassword) {
            mPasswordView.setText(mStoredPassword);
        }

        if (null != mStoredDomain && null != mStoredUsername && null != mStoredPassword && mAutoLogin) {
            login();
        }

        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }

                return false;
            }
        });

        mLoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    private void login() {
        mDomainView.setError(null);
        mLoginView.setError(null);
        mPasswordView.setError(null);

        String tmpDomain = mDomainView.getText().toString();
        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
        }

        if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_invalid_login));
            mLoginView.requestFocus();
        }

        if (!isDomainValid(tmpDomain)) {
            mDomainView.setError(getString(R.string.error_invalid_domain));
            mDomainView.requestFocus();
        }

        mDomain = tmpDomain.replaceAll(DOMAIN_VALIDATE_PATTERN, "$5");

        mUri = URI.create(mProtocol + KEY_PROTOCOL_SUFFIX + mDomain);
        mCookieStore = new MyCookieStore(getApplicationContext(), mUri);
        CookieHandler.setDefault(new CookieManager(mCookieStore, CookiePolicy.ACCEPT_ALL));

        doTask(LoginActivity.this, KEY_LOGIN_TASK, null, login, password);
    }
}

