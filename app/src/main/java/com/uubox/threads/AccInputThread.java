package com.uubox.threads;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.uubox.tools.Hex;
import com.uubox.tools.SimpleUtil;

public class AccInputThread extends Thread {
    private FileInputStream mFileInputStream;
    private boolean flag = true;
    private FileOutputStream mFileOutputStream;

    public AccInputThread(FileInputStream fileInputStream, FileOutputStream fileOutputStream) {
        mFileInputStream = fileInputStream;
        mFileOutputStream = fileOutputStream;
    }

    @Override
    public void run() {
        int len;
        byte[] buff = new byte[1024];
        byte[] temp;
        while (flag) {
            try {
                len = mFileInputStream.read(buff);
                temp = Arrays.copyOfRange(buff, 0, len);
                if (len <= 0) {
                    try {
                        mFileInputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    mFileInputStream = null;
                    SimpleUtil.notifyall_(10006, null);
                    SimpleUtil.log("AccInputThread is close len error!");
                    return;
                }
                SimpleUtil.notifyall_(10002, temp);
            } catch (Exception e) {
                e.printStackTrace();
                SimpleUtil.log("AccInputThread is close see exception!");
                flag = false;
                try {
                    mFileInputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                mFileInputStream = null;
                SimpleUtil.notifyall_(10006, null);
            }
        }
    }

    public boolean writeAcc(byte[] data) {
        try {
            if (mFileOutputStream == null) {
                SimpleUtil.log("write data fail:\n" + Hex.toString(data) + ",len:" + data.length);
                return false;
            }
            mFileOutputStream.write(data);
            mFileOutputStream.flush();
            SimpleUtil.log("write data ok:\n" + Hex.toString(data) + ",len:" + data.length);
        } catch (Exception e) {
            e.printStackTrace();
            SimpleUtil.log("write data fail:\n" + Hex.toString(data) + ",len:" + data.length);
            try {
                mFileOutputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            mFileOutputStream = null;
            SimpleUtil.notifyall_(10006, null);
            return false;
        }
        return true;
    }

    public boolean isConnect() {
        return mFileOutputStream != null && mFileInputStream != null;
    }
}
