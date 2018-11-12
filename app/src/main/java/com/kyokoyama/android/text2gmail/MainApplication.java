package com.kyokoyama.android.text2gmail;

import android.app.Application;

public class MainApplication extends Application {

    private static final String TAG = MainApplication.class.getName();

    @Override public void onCreate() {
        super.onCreate();
        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        Log.d(TAG, "Installing LeakCanary...");
        LeakCanary.install(this);*/
        // Normal app init code...
    }
}
