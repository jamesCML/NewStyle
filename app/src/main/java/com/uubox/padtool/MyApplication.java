package com.uubox.padtool;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.uubox.tools.SimpleUtil;
import com.uubox.tools.SocketLog;

import java.util.Stack;

public class MyApplication extends Application {
    private boolean isReady;
    private Stack<Activity> mActivitys = new Stack<>();

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        new SocketLog().start();
        Log.i("CJLOG", "<<<<<<<<<<<<application attachBaseContext>>>>>>>>>>>>>>>");
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {


                mActivitys.push(activity);
                Log.i("CJLOG", "activity oncreate:" + activity.getLocalClassName() + ",hash:" + activity.hashCode() + ",size:" + mActivitys.size());
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                mActivitys.remove(0);
                Log.i("CJLOG", "activity onActivityDestroyed:" + activity.getLocalClassName() + ",hash:" + activity.hashCode() + ",size:" + mActivitys.size());
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("CJLOG", "<<<<<<<<<<<<application onCreate>>>>>>>>>>>>>>>");
    }
}
