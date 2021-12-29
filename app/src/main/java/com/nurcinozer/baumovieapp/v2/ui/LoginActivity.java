package com.nurcinozer.baumovieapp.v2.ui;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.HuaweiIdAuthResult;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton;
import com.nurcinozer.baumovieapp.v2.R;
import com.nurcinozer.baumovieapp.v2.ui.movie_search.MovieSearchActivity;


public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_SIGN_IN_LOGIN = 3001; // Normal Login
    HuaweiIdAuthButton signInButton;
    public HuaweiIdAuthParams mHuaweiIdAuthParams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = findViewById(R.id.challenge_silent_signin);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mHuaweiIdAuthParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                        .setIdToken()
                        .setAccessToken()
                        .createParams();

                HuaweiIdAuthService mHuaweiIdAuthService = HuaweiIdAuthManager.getService (LoginActivity.this, mHuaweiIdAuthParams);

                startActivityForResult(mHuaweiIdAuthService.getSignInIntent(), REQUEST_SIGN_IN_LOGIN);
            }
        });

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            HuaweiIdAuthResult result = HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);
            if (result != null) {
                if (result.isSuccess()) {
                    // Obtain the authorization result.
                    //HuaweiIdAuthResult authResult = HuaweiIdAuthAPIManager.HuaweiIdAuthAPIService.parseHuaweiIdFromIntent(data);

                    Intent i = new Intent(getBaseContext(), MovieSearchActivity.class);
                    startActivity(i);
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}