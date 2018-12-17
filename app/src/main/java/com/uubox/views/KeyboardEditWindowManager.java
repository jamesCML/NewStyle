package com.uubox.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.SimpleUtil;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by jack on 2018/1/29.
 */

public class KeyboardEditWindowManager {
    private FrameLayout rootView;
    private static WindowManager mWindowManager;
    private static WindowManager.LayoutParams mLayoutParams;
    private Context mContext;

    static class Holder {
        public static KeyboardEditWindowManager instance = new KeyboardEditWindowManager();
    }

    private KeyboardEditWindowManager() {
    }


    public static KeyboardEditWindowManager getInstance() {
        return Holder.instance;
    }

    public KeyboardEditWindowManager init(final Context context) {
        mContext = context;
        if (rootView != null) {
            try {
                mWindowManager.addView(rootView, mLayoutParams);
            } catch (Exception e) {
            }

            return Holder.instance;
        }


        if (mWindowManager == null)
            mWindowManager = (WindowManager) context.getApplicationContext().getSystemService(WINDOW_SERVICE);

        mLayoutParams = new WindowManager.LayoutParams();
//            mLayoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
        //2017-12-23 Android 8.0 悬浮窗适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.
                    TYPE_SYSTEM_ERROR;
        }
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        rootView = new FrameLayout(context);
        //rootView.setBackgroundColor(Color.parseColor("#8800FF00"));
        hideBottomUIMenu(true);//test
        rootView.setFocusableInTouchMode(true);
        BtnParamTool.disableInjection();
        try {
            mLayoutParams.gravity = Gravity.TOP;
            mWindowManager.addView(rootView, mLayoutParams);
            mLayoutParams.gravity = Gravity.CENTER;
        } catch (Exception e) {
            SimpleUtil.log("donnot add the same view to the window!");

        }

        //unbindInjectService();
        return Holder.instance;
    }

    public int rootViewChildCount() {
        return rootView == null ? 0 : rootView.getChildCount();
    }

    /**
     * 移除顶部的 View
     *
     * @return
     */
    public KeyboardEditWindowManager removeTop() {
        if (rootView == null || rootView.getChildCount() == 0) {
            return Holder.instance;
        }
        removeView(rootView.getChildAt(rootView.getChildCount() - 1));
        if (rootView.getChildCount() == 0) {
            close();
        } else if (rootView.getChildCount() == 1) {
            AOAConfigTool.getInstance(mContext).openOrCloseRecKeycode(true);
        }
        return Holder.instance;
    }

    public KeyboardEditWindowManager removeView(View view) {
        //mWindowManager.removeView(view);
        rootView.removeView(view);
        if (rootView.getChildCount() == 0) {
            close();
        } else if (rootView.getChildCount() == 1) {
            AOAConfigTool.getInstance(mContext).openOrCloseRecKeycode(true);
        }
        return Holder.instance;
    }

    public KeyboardEditWindowManager addView(View view) {
        View topView = getTopView();
        if (topView != null && topView.getId() == view.getId()) {
            return Holder.instance;
        }
        rootView.addView(view);
        if (isHaveEdit(view)) {
            //AOAConfigTool.getInstance(mContext).openOrCloseRecKeycode(false);
        }
        return Holder.instance;
    }

    private boolean isHaveEdit(View view) {
        if (!(view instanceof ViewGroup)) {
            return false;
        }
        ViewGroup root = (ViewGroup) view;
        for (int i = 0; i < root.getChildCount(); i++) {
            if (root.getChildAt(i) instanceof ViewGroup) {
                return isHaveEdit(root.getChildAt(i));
            } else {
                if (root.getChildAt(i) instanceof EditText) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addView(View view, FrameLayout.LayoutParams params) {
        View topView = getTopView();
        if (topView != null && topView.getId() == view.getId()) {

        }
        rootView.addView(view, params);
    }

    public void printViews() {
        for (int i = 0; i < rootView.getChildCount(); i++) {
            SimpleUtil.log("rootCHild:" + rootView.getChildAt(i));
        }
    }

    public void hideRootView() {
        if (rootView == null) {
            return;
        }
        int count = rootViewChildCount();
        for (int i = count - 1; i >= 0; i--) {
            rootView.removeViewAt(i);
        }
        //rootView.setVisibility(View.GONE);
        //rootView.setVisibility(View.GONE);
        try {
            mWindowManager.removeView(rootView);
            SimpleUtil.notifyall_(10016,null);
        } catch (Exception e) {
            SimpleUtil.log("hide the window catch the exception!");
        }

    }

    public void displayRootView() {
        /*if (rootView == null) {
            return;
        }
        rootView.setVisibility(View.VISIBLE);*/
    }

    public KeyboardEditWindowManager addView(View view, int width, int height) {
        View topView = getTopView();
        if (topView != null && topView.getId() == view.getId()) {
            return Holder.instance;
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.gravity = Gravity.CENTER;
        rootView.addView(view, params);
        if (isHaveEdit(view)) {
            //AOAConfigTool.getInstance(mContext).openOrCloseRecKeycode(false);
        }
        return Holder.instance;
    }

    public View getTopView() {
        for (int i = 0; i < rootView.getChildCount(); i++) {
            SimpleUtil.log("rootViewChildID:" + rootView.getChildAt(i).getId() + "  " + rootView.getChildAt(i).getClass().getName());
        }
        return rootView == null || rootView.getChildCount() == 0 ? null : rootView.getChildAt(rootView.getChildCount() - 1);
    }

    public boolean rootIsVisible() {
        int i = rootView.getVisibility();
        int cc = rootViewChildCount();
        return rootView == null ? false : rootView.getVisibility() == View.VISIBLE;
    }

    public void show() {
        //begin onCreate
        rootView.setVisibility(View.VISIBLE);
        mWindowManager.updateViewLayout(rootView, mLayoutParams);

    }

    public void close() {
        close(true);
    }

    public void close(boolean rightnow) {
        BtnParamTool.enableInjection();

        try {
            if (rootView != null) {
                hideOrShowBottomUIMenu(true);

                if (rightnow) {
                    hideRootView();

                } else {
                    SimpleUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            hideRootView();
                        }
                    }, 50);
                }
            }
        } catch (Exception e) {
            SimpleUtil.log("window recycle exception!!!!!");
            e.printStackTrace();
        }


        if (BtnParamTool.isShowKbFloatView(mContext)) {
            KeyboardFloatView.getInstance(mContext).show();
        }

        if (!SimpleUtil.mAOAInjectEable) {
            SimpleUtil.log("notifyall_2 10004");

            SimpleUtil.notifyall_(10004, null);
        }
    }

    public void hideOrShowBottomUIMenu(boolean isHide) {
        hideBottomUIMenu(isHide);
    }

    private void hideBottomUIMenu(boolean isHide) {//yf_error
        //隐藏虚拟按键，并且全屏
     /*   SimpleUtil.log("hideBottomUIMenu:" + isHide);
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = SimpleUtil.home.getWindow().getDecorView();
            if (isHide) {
                rootView.setSystemUiVisibility(View.GONE);
            } else {
                rootView.setSystemUiVisibility(View.VISIBLE);
            }
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = SimpleUtil.home.getWindow().getDecorView();

            int uiOptions = 0;
            if (isHide) {
                uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            } else {
                uiOptions = View.SYSTEM_UI_FLAG_VISIBLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;

            }
            rootView.setSystemUiVisibility(uiOptions);
        }*/
    }

}
