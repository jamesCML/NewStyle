package com.uubox.padtool;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import android.widget.TextView;

import com.uubox.tools.SimpleUtil;


public class InitActivity extends Activity {
    private TextView mTest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTest = new TextView(this);
        mTest.setText("Loading...");
        mTest.setTextSize(20);
        setContentView(mTest);

        SimpleUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                int[] position = new int[2];
                getWindow().getDecorView().getLocationOnScreen(position);
                SimpleUtil.log("检测到停靠屏幕位置为:" + position[0]);
                if (position[0] != 0)//有刘海屏
                {
                    SimpleUtil.log("检测到刘海屏");
                    SimpleUtil.LIUHAI = getStatusBarHeight();
                }
                SimpleUtil.saveToShare(getBaseContext(), "ini", "LH", SimpleUtil.LIUHAI);
                int jLH = (Integer) SimpleUtil.getFromShare(InitActivity.this, "ini", "LH", int.class, -1);

                SimpleUtil.log("jLiuhai:" + jLH);
                finish();
            }
        }, 500);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
