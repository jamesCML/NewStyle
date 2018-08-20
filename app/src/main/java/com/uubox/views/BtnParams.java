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
public class BtnParams implements Serializable, Cloneable {
    /**
     * 复次按钮
     */
    public BtnParams btn2;
    /**
     * 交替开关
     */
    private boolean keyRepeatSwitch = true;
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
    private int mKeyType;
    private boolean mIsParent;
    private KeyboardView.Btn belongBtn;

    // private BtnParams mBackup;
    public KeyboardView.Btn getBelongBtn() {
        return belongBtn;
    }


    public void setBelongBtn(KeyboardView.Btn belongBtn) {
        this.belongBtn = belongBtn;
    }

    public boolean isParent() {
        return mIsParent;
    }

    public void doParent(boolean mIsParent) {
        this.mIsParent = mIsParent;
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

    public int getKeyType() {
        return mKeyType;
    }

    public boolean iHaveChild() {
        return isParent();
    }

    public boolean iAnChild() {
        return mKeyType == 1 || mKeyType == 2;
    }
    /**
     * 0：没有附属按键
     * 1：联动按键
     * 2：互斥按键
     */
    public void setKeyType(int mKeyType) {
        this.mKeyType = mKeyType;
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

   /* public BtnParams getmBackup() {
        return mBackup;
    }

    public void setmBackup(BtnParams mBackup) {
        if(this.mBackup!=null)
        {
            SimpleUtil.loge("只能有一个按键参数备份！！！");
            return;
        }
        this.mBackup = mBackup;
    }*/

    /* public void backupAssignTo()
     {
         if(mBackup==null)
         {
             SimpleUtil.loge("按键参数备份是空的!!!!");
             return;
         }
          this.x = mBackup.x;
          this.y = mBackup.y;
          this.r = mBackup.r;
          this.step = mBackup.step;
          this.frequency = mBackup.frequency;
          this.mode = mBackup.mode;
          this.keyRepeatSwitch = mBackup.keyRepeatSwitch;
          this.mKeyType = mBackup.mKeyType;
          this.btn2 = mBackup.btn2;
          this.mIsParent = mBackup.mIsParent;
          mBackup = null;
     }*/
    @Override
    public String toString() {
        return belongBtn + "(x: " + x + ", y: " + y + ", r: " + r + ", mKeyType: " + mKeyType + ", step: "
                + step + ", mIsParent: " + mIsParent + ")";
    }

   /* @Override
    protected BtnParams clone()  {
        BtnParams btnParams = null;
        try {
            btnParams = (BtnParams)super.clone();
            if(btnParams.mIsParent)
            {
                BtnParams sub = btnParams.btn2.clone();
                btnParams.btn2 = sub;
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return btnParams;
    }*/
}
