package com.uubox.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.uubox.padtool.R;
import com.uubox.tools.SimpleUtil;

/**
 * 摇杆模式
 * Created by Yj on 2015/8/19.
 */
public class BtnDialogActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {


    private static final String TAG = "BtnDialogActivity";
    /**
     * 鼠标灵敏度 X,Y 轴
     */
    private SeekBar mSeekBarX, mSeekBarY, mSeekBarW;
    /**
     * 鼠标灵敏度-文本 X,Y 轴
     */
    private TextView mTextMouseSensitivityX, mTextMouseSensitivityY, mTextMouseSensitivityW;


    /**
     * 记录调节时的鼠标灵敏度 X
     */
    private int mouseXxxTemp = 0;
    /**
     * 记录调节时的鼠标灵敏度 Y
     */
    private int mouseYyyTemp = 0;
    /**
     * 记录调节时的滚轮灵敏度 W
     */
    private int mouseWwwTemp = 0;

    /**
     * 选择使用鼠标中键调出鼠标指针(is:鼠标中键，no:鼠标右键)
     */
    private RadioButton mIsMouseIn;
    /**
     * 选择使用鼠标右键跳出鼠标指针(Is: 右键 ，No：中键)
     */
    private RadioButton mIsMouseRight;

    private View mView;
    private Context mContext;

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
        int tmpX = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mMouseProgressX", int.class, 5);
        int tmpY = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mMouseProgressY", int.class, 5);
        int tmpW = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mMouseProgressW", int.class, 50);

        Log.e(TAG, "initView: tmpX=" + tmpX + ", tmpY=" + tmpY + ", tmpW=" + tmpW);
        mSeekBarX.setProgress(tmpX);
        mSeekBarY.setProgress(tmpY);
        mSeekBarW.setProgress(tmpW);
        mTextMouseSensitivityX.setText(mContext.getString(R.string.horizontal) + tmpX);
        mTextMouseSensitivityY.setText(mContext.getString(R.string.vertical) + tmpY);
        mTextMouseSensitivityW.setText(mContext.getString(R.string.wheel) + tmpW);
        boolean isMouseMidel = false;
        isMouseMidel = (boolean) SimpleUtil.getFromShare(mContext, "ini", "isMouseMidle", boolean.class);
        mIsMouseIn.setChecked(isMouseMidel);
        mIsMouseRight.setChecked(!isMouseMidel);

    }

    private void initRockView() {
        mSeekBarX = mView.findViewById(R.id.sbar_mouse_sensitivity_x);
        mTextMouseSensitivityX = mView.findViewById(R.id.tv_mouse_sensitivity_x);
        mSeekBarY = mView.findViewById(R.id.sbar_mouse_sensitivity_y);
        mTextMouseSensitivityY = mView.findViewById(R.id.tv_mouse_sensitivity_y);
        mSeekBarW = mView.findViewById(R.id.sbar_mouse_Wheel);
        mTextMouseSensitivityW = mView.findViewById(R.id.tv_mouse_wheel);

        mIsMouseIn = mView.findViewById(R.id.rbn_mouse_in);
        mIsMouseRight = mView.findViewById(R.id.rbn_mouse_right);
    }

    private void setRockListener() {
        mSeekBarX.setOnSeekBarChangeListener(this);
        mSeekBarY.setOnSeekBarChangeListener(this);
        mSeekBarW.setOnSeekBarChangeListener(this);
        mIsMouseIn.setOnCheckedChangeListener(this);
        mIsMouseRight.setOnCheckedChangeListener(this);
        mView.findViewById(R.id.rbn_mouse_save).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.rbn_mouse_save:
                mouseXxxTemp = mouseXxxTemp;
                mouseYyyTemp = mouseYyyTemp;
                mouseWwwTemp = mouseWwwTemp;
                SimpleUtil.saveToShare(mContext, "ini", "mMouseProgressX", mouseXxxTemp);
                SimpleUtil.saveToShare(mContext, "ini", "mMouseProgressY", mouseYyyTemp);
                SimpleUtil.saveToShare(mContext, "ini", "mMouseProgressW", mouseWwwTemp);
                KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(true);
                KeyboardEditWindowManager.getInstance().removeTop();
                break;
        }

    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sbar_mouse_sensitivity_x:
                mouseXxxTemp = progress;
                mTextMouseSensitivityX.setText(mContext.getString(R.string.horizontal) + Integer.toString(mouseXxxTemp));
                break;
            case R.id.sbar_mouse_sensitivity_y:
                mouseYyyTemp = progress;
                mTextMouseSensitivityY.setText(mContext.getString(R.string.vertical) + Integer.toString(mouseYyyTemp));
                break;
            case R.id.sbar_mouse_Wheel:
                mouseWwwTemp = progress;
                mTextMouseSensitivityW.setText(mContext.getString(R.string.wheel) + Integer.toString(mouseWwwTemp));
                break;

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e("------------", "开始滑动！");

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.rbn_mouse_in:
                boolean b = SimpleUtil.saveToShare(mContext, "ini", "isMouseMidle", isChecked);
                break;
            default:
                break;
        }
    }


}
