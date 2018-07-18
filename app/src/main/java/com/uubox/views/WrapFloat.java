package com.uubox.views;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.uubox.tools.BtnUtil;
import com.uubox.tools.InjectUtil;
import com.uubox.tools.SimpleUtil;

import static android.content.Context.WINDOW_SERVICE;

/**
 * 实时键位，坐标和尺寸与实际键位保持一致。
 *
 * @author 李剑波
 * @date 18/1/4
 */
public class WrapFloat {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private static WrapFloat mInstance;
    private View mShow;

    public static WrapFloat getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new WrapFloat();
            mInstance.initWindParam(context);
        }
        return mInstance;
    }

    private void initWindParam(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

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
            mLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
            mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

    }

    public void addView(View view) {
        try {
            if (mShow != null) {
                removeView(mShow);
            }
            mWindowManager.addView(view, mLayoutParams);
            mShow = view;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeView(View view) {
        try {
            mWindowManager.removeView(view);
            mShow = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
