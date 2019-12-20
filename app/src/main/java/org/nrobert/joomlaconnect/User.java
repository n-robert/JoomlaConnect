package org.nrobert.joomlaconnect;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class User {
    private static final String KEY_STATUS = "status";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EXPIRES = "expires";
    private static final String KEY_TOKEN = "token";
    protected static final String KEY_LOGIN_TASK = "loginUser";
    protected static final String KEY_LOGOUT_TASK = "logoutUser";
    protected static final String KEY_UPDATE_TOKEN_TASK = "updateToken";
    private static User mInstance;
    private String userName, fullName, message;
    private Date sessionExpiryDate;
    private Boolean logOutSuccess = true;

    public static synchronized User getInstance() {
        if (null == mInstance) {
            mInstance = new User();
        }

        return mInstance;
    }

    public void setUsername(String username) {
        this.userName = username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setSessionExpiryDate(Date sessionExpiryDate) {
        this.sessionExpiryDate = sessionExpiryDate;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return userName;
    }

    public void setLogOutSuccess(Boolean logOutSuccess) {
        this.logOutSuccess = logOutSuccess;
    }

    public String getFullName() {
        return fullName;
    }

    public Date getSessionExpiryDate() {
        return sessionExpiryDate;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getLogOutSuccess() {
        return logOutSuccess;
    }

    public class UserTask extends AsyncTask<Void, Void, Boolean> {
        protected static final String KEY_PROTOCOL_SUFFIX = "://";
        protected static final String KEY_QUERY_STRING_PREFIX = "/index.php?option=com_mobileintegration&task=";
        private final MyAppActivity mAppActivity;
        private final MyCookieStore mCookieStore;
        private final SessionHandler mSession;
        private final String mTask, mProtocol, mDomain, mUrl, mLogin, mPassword, mData;

        public UserTask(MyAppActivity mAppActivity, String task, String data, String login, String password) {
            this.mAppActivity = mAppActivity;
            mTask = task;
            mData = data;
            mLogin = login;
            mPassword = password;
            mCookieStore = mAppActivity.mCookieStore;
            mSession = SessionHandler.getInstance(mAppActivity);
            mProtocol = mAppActivity.mProtocol;
            mDomain = mAppActivity.mDomain;
            mUrl = mProtocol + KEY_PROTOCOL_SUFFIX + mDomain + KEY_QUERY_STRING_PREFIX + mTask;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            Map<String, String> queries = new HashMap<String, String>();
            JSONObject request = new JSONObject();

            switch (mTask) {
                case KEY_LOGIN_TASK:
                    queries.put(KEY_USERNAME, mLogin);
                    queries.put(KEY_PASSWORD, mPassword);
                    break;
                case KEY_UPDATE_TOKEN_TASK:
                    queries.put(KEY_TOKEN, mData);
                    break;
                default:
                    break;
            }

            if (!queries.isEmpty()) {
                try {
                    for (String k : queries.keySet()) {
                        request.put(k, queries.get(k));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                    (Request.Method.POST, mUrl, request, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getInt(KEY_STATUS) == 1) {
                                    switch (mTask) {
                                        case KEY_LOGIN_TASK:
                                            mCookieStore.run();
                                            mSession.loginUser(mProtocol,
                                                               mDomain,
                                                               mLogin,
                                                               mPassword,
                                                               response.getString(KEY_FULL_NAME),
                                                               response.getString(KEY_MESSAGE),
                                                               response.getInt(KEY_EXPIRES));
                                            break;
                                        case KEY_LOGOUT_TASK:
                                            mSession.logoutUser();
                                            break;
                                        case KEY_UPDATE_TOKEN_TASK:
                                            mSession.updateToken(mData);
                                            break;
                                        default:
                                            break;
                                    }
                                }

                                if (!TextUtils.isEmpty(response.getString(KEY_MESSAGE))) {
                                    Toast.makeText(mAppActivity, response.getString(KEY_MESSAGE), Toast.LENGTH_LONG)
                                         .show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (!TextUtils.isEmpty(error.getMessage())) {
                                Toast.makeText(mAppActivity, error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });

            MySingleton.getInstance(mAppActivity).addToRequestQueue(jsArrayRequest);
            mAppActivity.setNullUserTask();

            try {
                // Simulate network access.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return mSession.isTaskSuccessful();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                if (mTask.equals(KEY_LOGOUT_TASK)) {
                    mAppActivity.loadLoginForm();
                } else {
                    mAppActivity.loadDashboard();
                }
            } else {
                if (mTask.equals(KEY_LOGIN_TASK)) {
                    mAppActivity.mFormMessage.setText(mAppActivity.getString(R.string.error_incorrect_credentials));
                }
            }

            mAppActivity.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAppActivity.showProgress(false);
        }
    }
}