package com.orange.datasync.chat;

import android.app.Application;

import com.orange.webcom.sdk.internal.util.Log;

/**
 * Useless here but serves as an example
 */
public class ChatApp extends com.orange.webcom.sdk.internal.App {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ChatApp", "Created");
    }
}
