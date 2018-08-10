package com.uubox.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;


import com.uubox.tools.BtnUtil;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.SimpleUtil;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import static android.content.Context.WINDOW_SERVICE;

/**
 * 实时键位，坐标和尺寸与实际键位保持一致。
 *
 * @author 李剑波
 * @date 18/1/4
 */
public class KeyboardFloatView extends FrameLayout implements SimpleUtil.INormalBack {

    private static final int BTN_COUNT = KeyboardView.Btn.values().length;
    private static KeyboardFloatView mInstance;
    /**
     * 预防重复添加或移除
     */
    private boolean mIsAdded = false;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    /**
     * 保存创建的全部按钮，按钮具有唯一性
     */

    private KeyboardFloatView(@NonNull Context context) {
        super(context);
        SimpleUtil.addINormalCallback(this);
    }

    public static KeyboardFloatView getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new KeyboardFloatView(context.getApplicationContext());
        }
        return mInstance;
    }

    public void show() {
        if (mIsAdded) {
            return;
        }
        // 重新载入按钮参数
        //BtnParamTool.loadBtnParamsFromPrefs(getContext());
        SimpleUtil.log("keymanager show");
        loadUi();
        addToWM();
        mIsAdded = true;
    }

    public void dismiss() {
        if (!mIsAdded) {
            return;
        }
        SimpleUtil.log("keymanager dismiss");
        removeFromWM();
        clearUI();
        mIsAdded = false;
    }

    public boolean isShowing() {
        return mIsAdded;
    }


    private void loadUi() {
        ConcurrentHashMap<KeyboardView.Btn, BtnParams> buttons = BtnParamTool.getmBtnParams();
        Iterator<KeyboardView.Btn> it = buttons.keySet().iterator();
        while (it.hasNext()) {
            KeyboardView.Btn btn = it.next();
            BtnParams params = buttons.get(btn);
            addView(params, btn);

        }

    }

    private void addView(BtnParams params, KeyboardView.Btn btn) {
        int x = params.getX() - SimpleUtil.LIUHAI;
        int y = params.getY();
        ImageView iv = new ImageView(getContext());
        if ((x <= 0 && y <= 0 && BtnParamTool.getBtnRadius(btn) <= 0)
                // 不显示方向键和鼠标
                || btn == KeyboardView.Btn.L || btn == KeyboardView.Btn.R) {
            return;
        }
        iv = new ImageView(getContext());
        Drawable drawable = BtnUtil.getBtnDrawable(btn, getContext());
        if (drawable != null) {
            iv.setImageDrawable(drawable);
        }
        iv.setTag(btn);
        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = x;
        layoutParams.topMargin = y;
        int r = BtnParamTool.getBtnRadius(btn);
        if (r > 0) {
            layoutParams.width = 2 * r;
            layoutParams.height = layoutParams.width;
            layoutParams.leftMargin -= r;
            layoutParams.topMargin -= r;
        } else {
            iv.setLayoutParams(layoutParams);
            iv.measure(0, 0);
            layoutParams.leftMargin -= iv.getMeasuredWidth() / 2;
            layoutParams.topMargin -= iv.getMeasuredHeight() / 2;
        }
        iv.setBackgroundResource(BtnParamTool.getBtnBelongColor(params));
        addView(iv, layoutParams);
        if (params.iHaveChild()) {
            addView(params.getBtn2(), btn);
        }
    }
    private void clearUI() {
        removeAllViews();
    }

    /**
     * 添加悬浮按钮
     */
    private void addToWM() {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getContext().getSystemService(WINDOW_SERVICE);
        }
        if (mLayoutParams == null) {
            mLayoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            }
            //设置图片格式，效果为背景透明
            mLayoutParams.format = PixelFormat.RGBA_8888;
            // 不响应按键事件和触屏事件
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //调整悬浮窗显示的停靠位置为左侧置顶
            mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        }
        try {
            mWindowManager.addView(this, mLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移除悬浮按钮
     */
    private void removeFromWM() {
        try {
            mWindowManager.removeView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void back(int id, Object obj) {
        if (id == 10015) {
            SimpleUtil.log("小健位收到按键更改通知");
        }
    }

    private void update(byte[] data) {

    }
}
