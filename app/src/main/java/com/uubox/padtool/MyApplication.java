package com.uubox.padtool;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.uubox.tools.SocketLog;
public class MyApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        new SocketLog().start();
        Log.i("CJLOG", "<<<<<<<<<<<<application attachBaseContext>>>>>>>>>>>>>>>");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("CJLOG", "<<<<<<<<<<<<application onCreate>>>>>>>>>>>>>>>");
    }
}
