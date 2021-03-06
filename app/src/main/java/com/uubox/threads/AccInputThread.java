package com.uubox.threads;

import android.content.Context;

import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.Hex;
import com.uubox.tools.SimpleUtil;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AccInputThread extends Thread {
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;
    private FileDescriptor mFileDescriptor;
    private Context mContext;

    public AccInputThread(Context context, FileDescriptor fileDescriptor) {
        mFileDescriptor = fileDescriptor;
        mContext = context;
    }

    @Override
    public void run() {
        try {
            int len;
            byte[] buff = new byte[1024];
            byte[] temp;
            mFileInputStream = new FileInputStream(mFileDescriptor);
            mFileOutputStream = new FileOutputStream(mFileDescriptor);
            readDevVer();
            while ((len = mFileInputStream.read(buff)) > 0) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            SimpleUtil.log("AccInputThread is close see exception:" + e.getMessage() + "\n" + e.getMessage());
            try {
                mFileInputStream.close();
                mFileOutputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            mFileInputStream = null;
            mFileOutputStream = null;
            SimpleUtil.notifyall_(10006, null);
        }
    }

    private void readDevVer() {
        SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleUtil.sleep(200);

                byte[] result = null;
                for (int i = 0; i < 3; i++) {
                    result = AOAConfigTool.getInstance(mContext).writeWaitResult((byte) 0xb3, new byte[]{(byte) 0xa5, (byte) 0x04, (byte) 0xb3, (byte) 0x5c}, 3000);
                    if (result != null) {
                        break;
                    }
                }
                if (result == null) {
                    SimpleUtil.log("读取版本信息出错");

                } else {
                    SimpleUtil.mDeviceVersion = Hex.toString(result[3]);
                    SimpleUtil.log("获取版本信息:" + SimpleUtil.mDeviceVersion + "  data:" + Hex.toString(result));
                    SimpleUtil.putOneInfoToMap("devver", SimpleUtil.mDeviceVersion + "");
                    SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_devver) + ":" + SimpleUtil.mDeviceVersion, false);
                    boolean ischange = (Boolean) SimpleUtil.getFromShare(mContext, "ini", "configschange", boolean.class);
                    if (ischange) {
                        SimpleUtil.notifyall_(10003, null);
                    }
                }
            }
        });


    }
    public boolean writeAcc(byte[] data) {
        try {
            if (mFileOutputStream == null) {
                SimpleUtil.log("mFileOutputStream is null!!write data fail:\n" + Hex.toString(data) + ",len:" + data.length);
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
