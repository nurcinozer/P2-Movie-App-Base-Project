package com.nurcinozer.baumovieapp.v2.service;

import android.os.Bundle;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;

public class DemoHmsMessageService extends HmsMessageService {
    private static final String TAG = "PushDemoLog";
    @Override
    public void onNewToken(String token, Bundle bundle) {
        super.onNewToken(token);
        Log.i(TAG, "receive new token:" + token);
    }
}
