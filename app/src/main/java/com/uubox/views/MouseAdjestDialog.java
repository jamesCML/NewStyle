package com.uubox.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uubox.padtool.R;
import com.uubox.tools.SimpleUtil;

/**
 * 摇杆模式
 * Created by Yj on 2015/8/19.
 */
public class MouseAdjestDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {


    private static final String TAG = "MouseAdjestDialog";
    /**
     * <p>鼠标灵敏度 X,Y 轴</p>
     * <p>mSeekBarY,mSeekBarX 改用 mSeekBarMouse 代替</p>
     */
    private SeekBar mSeekBarMouse, mSeekBarW;
    /**
     * 鼠标灵敏度-文本 X,Y 轴
     */
    private TextView mTextMouseSensitivityX, mTextMouseSensitivityW;


    private View mView;
    private Context mContext;

    private int mInitmousesen;
    private int mInitmousesrcollsen;

    public View create(Context context) {
        KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(false);
        mContext = context;
        mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_keyboard_rock_attribute_edit, null);
        initView();
        return mView;
    }

    private void initView() {
        initRockView();
        setRockListener();
        mInitmousesen = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesen", int.class, 10);
        mInitmousesrcollsen = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesrcollsen", int.class, 20);

        mSeekBarMouse.setProgress(mInitmousesen);
        mSeekBarW.setProgress(mInitmousesrcollsen);
        mTextMouseSensitivityX.setText("鼠标灵敏度:" + mInitmousesen);
        mTextMouseSensitivityW.setText("滚轮灵敏度:" + mInitmousesrcollsen);


    }

    private void initRockView() {
        mSeekBarMouse = mView.findViewById(R.id.sbar_mouse_sensitivity_x);
        mTextMouseSensitivityX = mView.findViewById(R.id.tv_mouse_sensitivity_x);
        mSeekBarW = mView.findViewById(R.id.sbar_mouse_Wheel);
        mTextMouseSensitivityW = mView.findViewById(R.id.tv_mouse_wheel);

    }

    private void setRockListener() {
        mSeekBarMouse.setOnSeekBarChangeListener(this);
        mSeekBarW.setOnSeekBarChangeListener(this);
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
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sbar_mouse_sensitivity_x:
                mTextMouseSensitivityX.setText("鼠标灵敏度:" + progress);
                break;
            case R.id.sbar_mouse_Wheel:
                mTextMouseSensitivityW.setText("滚轮灵敏度:" + progress);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e("------------", "开始滑动！");

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SimpleUtil.saveToShare(mContext, "ini", "mousesen", mSeekBarMouse.getProgress());
        SimpleUtil.saveToShare(mContext, "ini", "mousesrcollsen", mSeekBarW.getProgress());
        //SimpleUtil.addMsgBottomToTop(mContext, "修改成功！", false);
    }


}
