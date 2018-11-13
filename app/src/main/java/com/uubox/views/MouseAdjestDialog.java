package com.uubox.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uubox.padtool.R;
import com.uubox.tools.SimpleUtil;

/**
 * 摇杆模式
 * Created by Yj on 2015/8/19.
 */
public class MouseAdjestDialog implements View.OnClickListener {
    private View mView;
    private Context mContext;
    private int mInitmousesen;
    private int mInitmousesrcollsen;
    private TextView mSBsen;
    private TextView mGLsen;
    public View create(Context context) {
        KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(false);
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_keyboard_rock_attribute_edit, null);
        initView();
        return mView;
    }

    private void initView() {
        mInitmousesen = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesen", int.class, 10);
        mInitmousesrcollsen = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesrcollsen", int.class, 20);
        mSBsen = mView.findViewById(R.id.mouse_adjust_sb_sen);
        mGLsen = mView.findViewById(R.id.mouse_adjust_gl_sen);
        mSBsen.setText(mInitmousesen + "");
        mGLsen.setText(mInitmousesrcollsen + "");
        mView.findViewById(R.id.sb_add).setOnClickListener(this);
        mView.findViewById(R.id.gl_add).setOnClickListener(this);
        mView.findViewById(R.id.sb_sub).setOnClickListener(this);
        mView.findViewById(R.id.gl_sub).setOnClickListener(this);
        mView.findViewById(R.id.btn_reset_mousesbr).setOnClickListener(this);
        mView.findViewById(R.id.rbn_mouse_save).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.rbn_mouse_save:
                KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(true);
                KeyboardEditWindowManager.getInstance().removeTop();

                int aftermousesen = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesen", int.class, 10);
                int aftermousesrcollsen = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesrcollsen", int.class, 20);
                if (aftermousesen != mInitmousesen || aftermousesrcollsen != mInitmousesrcollsen) {
                    SimpleUtil.notifyall_(10003, null);
                }
                break;
            case R.id.btn_reset_mousesbr:
                mSBsen.setText(10 + "");
                mGLsen.setText(20 + "");
                SimpleUtil.saveToShare(mContext, "ini", "mousesen", 10);
                SimpleUtil.saveToShare(mContext, "ini", "mousesrcollsen", 20);
                break;
            case R.id.sb_add:
                String text = mSBsen.getText().toString();
                int cur = Integer.parseInt(text) + 1;
                if (cur >= 100) {
                    cur = 99;
                }
                mSBsen.setText(cur + "");
                SimpleUtil.saveToShare(mContext, "ini", "mousesen", cur);
                break;
            case R.id.sb_sub:
                text = mSBsen.getText().toString();
                cur = Integer.parseInt(text) - 1;
                if (cur <= 0) {
                    cur = 0;
                }
                mSBsen.setText(cur + "");
                SimpleUtil.saveToShare(mContext, "ini", "mousesen", cur);
                break;
            case R.id.gl_add:
                text = mGLsen.getText().toString();
                cur = Integer.parseInt(text) + 1;
                if (cur >= 100) {
                    cur = 99;
                }
                mGLsen.setText(cur + "");
                SimpleUtil.saveToShare(mContext, "ini", "mousesrcollsen", cur);
                break;
            case R.id.gl_sub:
                text = mGLsen.getText().toString();
                cur = Integer.parseInt(text) - 1;
                if (cur <= 0) {
                    cur = 0;
                }
                mGLsen.setText(cur + "");
                SimpleUtil.saveToShare(mContext, "ini", "mousesrcollsen", cur);
                break;
        }


    }


}
