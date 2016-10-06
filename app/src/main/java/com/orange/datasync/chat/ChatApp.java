package com.orange.datasync.chat;

import android.app.Application;

import com.orange.webcom.sdk.Config;
import com.orange.webcom.sdk.internal.util.Log;

/**
 * Useless here but serves as an example
 */
public class ChatApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config.onCreate(this);
        Log.i("ChatApp", "Created");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Config.onTerminate();
    }
}
