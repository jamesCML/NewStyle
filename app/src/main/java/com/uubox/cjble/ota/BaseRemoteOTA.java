package com.uubox.cjble.ota;


import com.uubox.tools.ByteArrayList;

public abstract class BaseRemoteOTA implements Runnable {
    protected ByteArrayList mBuff;
    public static final int FWVERSION_CALLBACK = 10;
    public static final int OKBUFF_CALLBACK = 11;
    public static final int CRCEER_CALLBACK = 12;
    public static final int DOWNLOADEER_CALLBACK = 13;
    protected String mCurImg;
    protected String mFWVersion;
    protected String mServerVer = "";

    public BaseRemoteOTA(String mFWVersion, String curImg) {
        mBuff = new ByteArrayList();
        mCurImg = curImg;
        this.mFWVersion = mFWVersion;
    }

    @Override
    public void run() {
        task();
    }

    public ByteArrayList getmBuff() {
        return mBuff;
    }


    public abstract void task();

    public abstract void downloadError(String msg);

    public abstract boolean writeRemote(byte[] data);

    public abstract void close();


    public abstract void wantImg();

    public String getSerVer() {
        return mServerVer;
    }

}
