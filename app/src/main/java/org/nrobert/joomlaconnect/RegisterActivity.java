package org.nrobert.joomlaconnect;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends MyAppActivity {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_DOMAIN = "domain";
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_FULL_NAME = "mFullName";
    private static final String KEY_USERNAME = "mUsername";
    private static final String KEY_PASSWORD = "mPassword";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_EMPTY = "";
    private static final String DOMAIN_VALIDATE_PATTERN = "^(http(s)*://)*(([\\w]+\\.)+)([a-zA-Z]+)([/])*$";
    private EditText mDomainView, mUsernameView, mPasswordView, mConfirmPasswordView, mFullNameView;
    private View mProgressView, mRegisterFormView;
    private String mProtocol, mDomain, mStoredDomain;
    private String mUsername, mPassword, confirmPassword, mFullName;
    private String mRegisterUrl;
    private SessionHandler mSession;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSession = new SessionHandler(getApplicationContext());
        setContentView(R.layout.activity_register);

        mDomainView = findViewById(R.id.domain);
        mUsernameView = findViewById(R.id.userName);
        mPasswordView = findViewById(R.id.password);
        mConfirmPasswordView = findViewById(R.id.confirmPassword);
        mFullNameView = findViewById(R.id.fullName);

        mSharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        mStoredDomain = mSharedPreferences.getString(KEY_DOMAIN, null);

        if (null != mStoredDomain) {
            mDomain = mStoredDomain;
            mDomainView.setText(mStoredDomain);
            mFullNameView.requestFocus();
        }

        Button login = findViewById(R.id.btnRegisterLogin);
        Button registration = findViewById(R.id.btnRegister);

        //Launch Login screen when Login Button is clicked
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        });

        registration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDomainView.setError(null);
                String tmpDomain = mDomainView.getText().toString();

                if (!isDomainValid(tmpDomain)) {
                    mDomainView.setError(getString(R.string.error_invalid_domain));
                    mDomainView.requestFocus();
                }

                mDomain = tmpDomain.replaceAll(DOMAIN_VALIDATE_PATTERN, "$3$5");
                mRegisterUrl = "http://" + mDomain + "/android/loginandregistration/register.php";
                mUsername = mUsernameView.getText().toString().toLowerCase().trim();
                mPassword = mPasswordView.getText().toString().trim();
                confirmPassword = mConfirmPasswordView.getText().toString().trim();
                mFullName = mFullNameView.getText().toString().trim();
                if (validateInputs()) {
                    register();
                }

            }
        });

        mRegisterFormView = findViewById(R.id.register_form);
        mProgressView = findViewById(R.id.register_progress);
    }

    /**
     * Attempts to register the account specified by the register form.
     * If there are form errors (invalid login, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    private void register() {
        if (mUserTask != null) {
            return;
        }
    }

    /**
     * Validates inputs and shows error if any
     *
     * @return
     */
    private boolean validateInputs() {
        if (KEY_EMPTY.equals(mFullName)) {
            mFullNameView.setError("Full Name cannot be empty");
            mFullNameView.requestFocus();
            return false;

        }
        if (KEY_EMPTY.equals(mUsername)) {
            mUsernameView.setError("Username cannot be empty");
            mUsernameView.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(mPassword)) {
            mPasswordView.setError("Password cannot be empty");
            mPasswordView.requestFocus();
            return false;
        }

        if (KEY_EMPTY.equals(confirmPassword)) {
            mConfirmPasswordView.setError("Confirm Password cannot be empty");
            mConfirmPasswordView.requestFocus();
            return false;
        }
        if (!mPassword.equals(confirmPassword)) {
            mConfirmPasswordView.setError("Password and Confirm Password does not match");
            mConfirmPasswordView.requestFocus();
            return false;
        }

        return true;
    }
}