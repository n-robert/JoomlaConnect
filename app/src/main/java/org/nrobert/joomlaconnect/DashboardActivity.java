package org.nrobert.joomlaconnect;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class DashboardActivity extends MyAppActivity {
    private static final String TAG = "DashboardActivity";
    private TextView welcomeText;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (!mSession.isLoggedIn()) {
            loadLoginForm();
        }

        mProgressView = findViewById(R.id.dashboard_progress);
        mFormView = findViewById(R.id.dashboard_form);
        welcomeText = findViewById(R.id.welcomeText);

        if (mUser.getLogOutSuccess()) {
            text = String.format(getString(R.string.welcome), mUser.getFullName());
            text += (TextUtils.isEmpty(mUser.getMessage())) ? "" : (" " + mUser.getMessage());
        } else {
            text = String.format(getString(R.string.not_logged_out), mUser.getFullName());
        }

        welcomeText.setText(text);


        FirebaseInstanceId.getInstance().getInstanceId()
                          .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                              @Override
                              public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                  if (!task.isSuccessful()) {
                                      Log.w(TAG, "getInstanceId failed", task.getException());
                                      return;
                                  }

                                  String token = task.getResult().getToken();
                                  String oldToken = mSharedPreferences.getString(KEY_TOKEN, null);

                                  if (mSession.isLoggedIn() && !token.equals(oldToken)) {
                                      doTask(DashboardActivity.this, KEY_UPDATE_TOKEN_TASK, token, null, null);
                                      Log.d(TAG, getString(R.string.msg_token_old, oldToken));
                                  }

                                  Log.d(TAG, getString(R.string.msg_token_fmt, token));
                              }
                          });

        Button logoutBtn = findViewById(R.id.btnLogout);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUser.setLogOutSuccess(false);
                doTask(DashboardActivity.this, KEY_LOGOUT_TASK, null, null, null);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }
}