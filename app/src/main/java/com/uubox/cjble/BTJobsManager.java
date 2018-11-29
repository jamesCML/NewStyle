package com.uubox.cjble;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.uubox.cjble.ota.APPStartCheckOTA;
import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.Hex;
import com.uubox.tools.SimpleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BTJobsManager implements BTService.IStateCallBack, BTService.IBLENotify {
    private static BTJobsManager mInstance;
    private ServiceConnection mBTBindConnection;
    private BTService.LocalBinder mBinder;
    private BluetoothGatt mGatt;
    private String mDevMod;
    private Context mContext;
    private boolean mOTAUpdating;
    public final static boolean OPEN = true;
    private BTJobsManager() {
    }

    public static BTJobsManager getInstance() {
        synchronized (BTJobsManager.class) {
            if (mInstance == null) {
                mInstance = new BTJobsManager();
            }
            return mInstance;
        }
    }

    public boolean isOTAUpdating() {
        return mOTAUpdating;
    }

    public void setOTAUpdate(boolean flag) {
        mOTAUpdating = flag;
    }

    public void bindBTService(Context context) {
        mContext = context;
        final Intent intent = new Intent(mContext, BTService.class);
        intent.setAction("com.hexad.bluezime.connect");
        mBTBindConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

                mBinder = (BTService.LocalBinder) service;
                mBinder.setIStateCallBack(BTJobsManager.this);
                mBinder.addNotify(BTJobsManager.this);
                mContext.startService(intent);
                SimpleUtil.log("UUBOX绑定了servie");

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                SimpleUtil.log("bind the server fail");
            }
        };
        mContext.bindService(intent, mBTBindConnection, Service.BIND_AUTO_CREATE);
    }

    public BluetoothGatt getGatt() {
        return mGatt;
    }

    public boolean isBLEConnected() {
        return mBinder != null && mBinder.getBleGatt() != null;
    }
    public void writeDefault(byte[] data, BleWriteCallback bleWriteCallback) {
        mBinder.writeDefault(data, bleWriteCallback);
    }

    public void write(byte[] data, String serviceUUID, String charactUUID, BleWriteCallback bleWriteCallback) {
        mBinder.write(data, serviceUUID, charactUUID, bleWriteCallback);
    }

    public void write(BluetoothGattCharacteristic characteristics, BleWriteCallback bleWriteCallback) {
        mBinder.write(characteristics, bleWriteCallback);
    }

    public void addBLENotify(BTService.IBLENotify notify) {
        mBinder.addNotify(notify);
    }

    public void removeBLENotify(BTService.IBLENotify notify) {
        mBinder.removeNotify(notify);
    }

    public BluetoothDevice getDevice() {
        return mBinder.getDevice();
    }

    public String getDevMod() {
        return mDevMod;
    }

    @Override
    public void connectstate(int type, BTService.EState state, Object obj) {
        switch (type) {
            case 1:

                //非人为因素需要重连
                if (state == BTService.EState.USERDISCONNECTED || state == BTService.EState.DISCONNECTED) {//连接断开
                    mGatt = null;
                    SimpleUtil.notifyall_(10006, null);

                } else if (state == BTService.EState.CONNECTFAIL) {
                    BleManager.getInstance().cancelScan();
                } else if (state == BTService.EState.DISCONNECTED) {
                    BleManager.getInstance().cancelScan();
                    SimpleUtil.notifyall_(10006, null);
                } else if (state == BTService.EState.CONNECTED)//连接成功开始发验证信息
                {
                    mGatt = mBinder.getBleGatt();
                    getModeInfo();
                    SimpleUtil.notifyall_(10020, null);
                }
                break;
            case 2:
                break;


        }
    }

    private void getModeInfo() {
        boolean result = false;
        List<BluetoothGattService> services = mGatt.getServices();
        for (BluetoothGattService service : services) {
            SimpleUtil.log("serviceName:" + service.getUuid().toString());
            if (service.getUuid().toString().equals("0000180a-0000-1000-8000-00805f9b34fb")) {
                SimpleUtil.log("we get the mode service!!");
                BluetoothGattCharacteristic modechara = service.getCharacteristic(UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb"));

                if (mGatt == null) {
                    return;
                }
                try {
                    result = mGatt.readCharacteristic(modechara);
                } catch (Exception e) {
                    SimpleUtil.log("readCharacteristic error!!!!");
                    SimpleUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mGatt.disconnect();
                            mGatt.close();
                            mBinder.resetConnectState();
                        }
                    });
                    return;
                }
                SimpleUtil.log("read result:" + result);
                if (!result) {
                    SimpleUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            mGatt.disconnect();
                            mGatt.close();
                            mBinder.resetConnectState();
                        }
                    });
                }
                break;
            }

        }
        if (!result) {
            SimpleUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mGatt.disconnect();
                    mGatt.close();
                    mBinder.resetConnectState();
                }
            });
        }
    }

    @Override
    public void notify(BTService.BLEMODTYPE mode, final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        // SimpleUtil.log("蓝牙数据 mode:" + mode + " 服务:" + characteristic.getUuid().toString() + " 数据:" + Hex.toString(characteristic.getValue()));
        if (mode == BTService.BLEMODTYPE.READ) {

            if (characteristic.getUuid().toString().equals("00002a24-0000-1000-8000-00805f9b34fb")) {
                Map<String, List<String>> modeMap = new HashMap<>();
                List<String> modes = new ArrayList<>();
                initModeStr(modeMap, modes);
                mDevMod = new String(characteristic.getValue());

                String deviceName = gatt.getDevice().getName();
                SimpleUtil.log("get name:" + deviceName + ",mode:" + mDevMod);

                //强制连接模式，特殊处理
                if (!modeMap.containsKey(deviceName)) {
//                    Collection<List<String>> values = modeMap.values();

                    for (String v : modes) {
                        if (mDevMod.contains(v)) {
                            SimpleUtil.log("BT is Other!");
                            mBinder.stopWatch();
                            checkOTA();
                            return;
                        }
                    }
                    SimpleUtil.log("BT is bad!");
                    SimpleUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            gatt.disconnect();
                            gatt.close();
                            mBinder.resetConnectState();
                        }

                    });
                    return;

                }

                Iterator<String> it = modeMap.keySet().iterator();
                while (it.hasNext()) {
                    String key = it.next();
                    if (deviceName.contains(key)) {
                        List<String> value = modeMap.get(key);
                        if (value == null) {
                            SimpleUtil.log("notify: modelist is null !!");
                            gatt.disconnect();
                            gatt.close();
                            return;
                        } else if (!value.contains(mDevMod)) {
                            SimpleUtil.log("notify: mode not contains!!");
                            gatt.disconnect();
                            gatt.close();
                        } else {
                            SimpleUtil.log("The device is match!" + deviceName + ":" + mDevMod);
                            checkOTA();
                        }
                    }
                }
            }
        }

    }

    private void checkOTA() {
        APPStartCheckOTA appStartCheckOTA = new APPStartCheckOTA(mContext);
        appStartCheckOTA.setICheckOTABack(new APPStartCheckOTA.ICheckOTABack() {
            @Override
            public void checkresult(int enter) {
                if (enter == 1) {
                    readDever();
                } else if (enter == 3) {
                    SimpleUtil.log("设备版本读取失败!");
                } else if (enter == 4) {
                    SimpleUtil.log("未知错误!");
                }
            }
        });
        appStartCheckOTA.start();
    }

    private void readDever() {
        SimpleUtil.log("蓝牙开始读取版本");
        byte[] result = AOAConfigTool.getInstance(mContext).writeWaitResult((byte) 0xb3, new byte[]{(byte) 0xa5, (byte) 0x04, (byte) 0xb3, (byte) 0x5c}, 3000);
        if (result == null) {
            SimpleUtil.log("读取版本信息出错");
            SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.ait_readdevverfail), true);
        } else {
            SimpleUtil.mDeviceVersion = Hex.toString(new byte[]{result[3]});
            SimpleUtil.log("获取版本信息:" + SimpleUtil.mDeviceVersion + "  data:" + Hex.toString(result));
            SimpleUtil.putOneInfoToMap("devver", SimpleUtil.mDeviceVersion + "");
            boolean ischange = (Boolean) SimpleUtil.getFromShare(mContext, "ini", "configschange", boolean.class);
            if (ischange) {
                SimpleUtil.notifyall_(10003, null);
            }
        }
    }
    private void initModeStr(Map<String, List<String>> modeMap, List<String> modes) {
        //model
        modes.add("CJ007");
        modes.add("K100");
        modes.add("P200");


        List<String> modeStr0 = new ArrayList<>();
        List<String> modeStr1 = new ArrayList<>();
        List<String> modeStr2 = new ArrayList<>();
        List<String> modeStr3 = new ArrayList<>();
        List<String> modeStr4 = new ArrayList<>();
        List<String> modeStr5 = new ArrayList<>();
        List<String> modeStr6 = new ArrayList<>();
        List<String> modeStr7 = new ArrayList<>();
        List<String> modeStr8 = new ArrayList<>();
        List<String> modeStr11 = new ArrayList<>();
        List<String> modeStr12 = new ArrayList<>();
        modeStr0.add("CJ007-GX1");
        modeMap.put("Gamesir-X1", modeStr0);

        modeStr1.add("CJ007-GX2");
        modeMap.put("Gamesir-X2", modeStr1);

        modeStr2.add("CJ007-A");
        modeStr2.add("CJ007-KY");
        modeStr2.add("CJ007-KY2");
        modeMap.put("CJ007-A", modeStr2);

        modeStr3.add("CJ007-GX2");
        modeMap.put("DELUX-S2", modeStr3);

        modeStr4.add("CJ007-GZ1");
        modeMap.put("Gamesir-Z1", modeStr4);

        modeStr5.add("CJ007-GZ2");
        modeMap.put("Gamesir-Z2", modeStr5);

        modeStr6.add("CJ007-A");
        modeMap.put("mingpin KBOX", modeStr6);

        modeStr7.add("CJ007-A");
        modeMap.put("powkiddy KBOX", modeStr7);

        modeStr8.add("CJ007-pd");
        modeMap.put("powkiddy", modeStr8);

        modeStr11.add("K100");
        modeMap.put("Newgame K100", modeStr11);

        modeStr12.add("P200");
        modeMap.put("Newgame P200", modeStr12);
    }
}
