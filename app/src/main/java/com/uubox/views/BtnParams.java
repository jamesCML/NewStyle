package com.uubox.views;

import android.widget.ImageView;

import java.io.Serializable;

/**
 * 按钮参数类，用于存储按钮的x坐标、y坐标和半径等参数。
 *
 * @author 李剑波
 * @date 2015/8/26
 * @time 16:18
 */
public class BtnParams implements Serializable {
    /**
     * 复次按钮
     */
    public BtnParams btn2;
    /**
     * 交替开关
     */
    public boolean keyRepeatSwitch = true;
    public ImageView img;
    /**
     * x坐标
     */
    private int x;
    /**
     * y坐标
     */
    private int y;
    /**
     * 半径
     */
    private int r;
    /**
     * 模式
     */
    private int mode;
    /**
     * 步长
     */
    private int step;
    /**
     * 频率
     */
    private int frequency;
    /**
     * 复次的按钮类型
     */
    private int keyRepeatType;
    private boolean isBelongButton;

    private KeyboardView.Btn belongBtn;

    public KeyboardView.Btn getBelongBtn() {
        return belongBtn;
    }

    public void setBelongBtn(KeyboardView.Btn belongBtn) {
        this.belongBtn = belongBtn;
    }

    public boolean isBelongButton() {
        return isBelongButton;
    }

    public void setBelongButton(boolean belongButton) {
        isBelongButton = belongButton;
    }

    public BtnParams getBtn2() {
        return btn2;
    }

    public void setBtn2(BtnParams btn2) {
        this.btn2 = btn2;
    }

    public boolean isKeyRepeatSwitch() {
        return keyRepeatSwitch;
    }

    public void setKeyRepeatSwitch(boolean keyRepeatSwitch) {
        this.keyRepeatSwitch = keyRepeatSwitch;
    }

    public int getKeyRepeatType() {
        return keyRepeatType;
    }

    /**
     * 0：没有附属按键
     * 1：联动按键
     * 2：互斥按键
     */
    public void setKeyRepeatType(int keyRepeatType) {
        this.keyRepeatType = keyRepeatType;
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return "BtnParams(x: " + x + ", y: " + y + ", r: " + r + ", mode: " + mode + ", step: "
                + step + ", frequency: " + frequency + ")";
    }
}
