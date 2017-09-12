package com.example.shinelon.ocrcamera.helper;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Shinelon on 2017/9/12.
 */

public class CheckApplication extends Application {
    @Override public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
