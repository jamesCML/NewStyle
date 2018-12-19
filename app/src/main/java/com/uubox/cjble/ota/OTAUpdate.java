package com.uubox.cjble.ota;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.uubox.cjble.BTJobsManager;
import com.uubox.cjble.BTService;
import com.uubox.tools.ByteArrayList;
import com.uubox.tools.Hex;
import com.uubox.tools.SimpleUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by CG_Dawson on 2017/12/19.
 */

public class OTAUpdate implements Runnable, BTService.IBLENotify {
    private static final String dISService_UUID = "0000180a-0000-1000-8000-00805f9b34fb";
    private static final String dISSystemID_UUID = "00002a23-0000-1000-8000-00805f9b34fb";
    private static final String dISModelNR_UUID = "00002a24-0000-1000-8000-00805f9b34fb";
    private static final String dISSerialNR_UUID = "00002a25-0000-1000-8000-00805f9b34fb";
    private static final String dISFirmwareREV_UUID = "00002a26-0000-1000-8000-00805f9b34fb";
    private static final String dISHardwareREV_UUID = "00002a27-0000-1000-8000-00805f9b34fb";
    private static final String dISSoftwareREV_UUID = "00002a28-0000-1000-8000-00805f9b34fb";
    private static final String dISManifacturerNAME_UUID = "00002a29-0000-1000-8000-00805f9b34fb";
    public final static String ACTION_FW_REV_UPDATED = "com.example.ti.ble.btsig.ACTION_FW_REV_UPDATED";
    public final static String EXTRA_FW_REV_STRING = "com.example.ti.ble.btsig.EXTRA_FW_REV_STRING";
    private static final String oadService_UUID = "f000ffc0-0451-4000-b000-000000000000";
    private static final String oadImageNotify_UUID = "f000ffc1-0451-4000-b000-000000000000";
    private static final String oadBlockRequest_UUID = "f000ffc2-0451-4000-b000-000000000000";

    private BluetoothGattService mOadService = null;
    private BluetoothGattService mConnControlService = null;
    private BluetoothGattService mBTService = null;
    private BluetoothGattCharacteristic mCharIdentify = null;
    private BluetoothGattCharacteristic mCharBlock = null;
    private final String TAG = "OTAUpdate";

    public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothGattCharacteristic systemIDc;
    private BluetoothGattCharacteristic modelNRc;
    private BluetoothGattCharacteristic serialNRc;
    private BluetoothGattCharacteristic firmwareREVc;
    private BluetoothGattCharacteristic hardwareREVc;
    private BluetoothGattCharacteristic softwareREVc;
    private BluetoothGattCharacteristic ManifacturerNAMEc;
    private final byte[] mOadBuffer = new byte[20];
    private ImgHdr mFileImgHdr = new ImgHdr();
    private ImgHdr mTargImgHdr = new ImgHdr();
    private ProgInfo mProgInfo = new ProgInfo();
    private boolean mProgramming = false;
    private boolean ifBlockSend = false;
    private volatile LinkedList<bleRequest> procQueue = new LinkedList<>();
    private final Lock lock = new ReentrantLock();
    private volatile boolean blocking = false;
    private BluetoothGatt gatt;
    private Context context;
    private final byte[] mFileBuffer = new byte[0x40000];
    private String fwVersion = "0";
    public IOTACallBack iotaCallBack;
    private byte[] curWrite;
    private int writeState;

    //private RecText recText ;
    public OTAUpdate(Context context, BluetoothGatt gatt) {
        this.gatt = gatt;
        this.context = context;
        /*if(recText!=null)
        {
            if(recText.isAlive())
            {
                recText.close();
            }
        }
        recText = new RecText("/sdcard/Zhiwan/otalog.txt");*/
        //recText.start();
    }

    public void setIotaCallBack(IOTACallBack iotaCallBack) {
        this.iotaCallBack = iotaCallBack;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public String getCurImage() {
        return mTargImgHdr.imgType == null ? null : mTargImgHdr.imgType + "";
    }

    public boolean checkImg() {
        if (mTargImgHdr.imgType == null || mFileImgHdr.imgType == null) {
            return false;
        }
        return mTargImgHdr.imgType != mFileImgHdr.imgType;
    }

    public BluetoothGattCharacteristic getFirmwareREVc() {
        return firmwareREVc;
    }

    private int calc_crc16(byte[] data, int len) {
        int i, j;
        byte ds;
        int crc = 0xffff;
        int poly[] = {0, 0xa001};

        for (j = 0; j < len; j++) {
            ds = data[j];
            for (i = 0; i < 8; i++) {
                crc = (crc >> 1) ^ poly[(crc ^ ds) & 1];
                ds = (byte) (ds >> 1);
            }
        }
        return crc;
    }

    public void getInfomation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                iotaCallBack.callback(6, 0, 0, null);
                //初始化
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService service : services) {
                    SimpleUtil.log("serviceName:" + service.getUuid().toString());
                    List<BluetoothGattCharacteristic> characterList = service.getCharacteristics();

                    if (characterList.size() == 0) {
                        SimpleUtil.log("This service has no characteristic");
                        return;
                    }
                    //发现OTA服务！！！
                    if ((service.getUuid().toString().compareTo(oadService_UUID)) == 0) {
                        mOadService = service;
                        SimpleUtil.log("get OTA service！");
                        List<BluetoothGattCharacteristic> oadCharacterList = mOadService.getCharacteristics();

                        mCharIdentify = oadCharacterList.get(0);
                        SimpleUtil.log("get OTA-Ident:" + mCharIdentify.getUuid().toString());

                        boolean res = enableNotify(gatt, mCharIdentify);
                        SimpleUtil.log("enableImgNotify:" + res);

                        long time = 0;
                        SimpleUtil.sleep(50);
                        for (int i = 0; i < 6; i++) {
                            time = System.currentTimeMillis();
                            BTJobsManager.getInstance().write(new byte[]{0}, oadService_UUID, mCharIdentify.getUuid().toString(), new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess() {
                                    SimpleUtil.log("send the get img again1:ok");
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    SimpleUtil.log("send the get img again1:fail\n" + exception.getDescription());
                                }
                            });
                            while (mTargImgHdr.imgType == null && (System.currentTimeMillis() - time) < 3000)
                                ;
                            if (mTargImgHdr.imgType != null) {
                                SimpleUtil.log("we had got the first img!");
                                break;
                            }


                            if (mTargImgHdr.imgType == null && i == 5) {
                                SimpleUtil.log("we want to get the img first fail!we had try 5 times!!!");
                                iotaCallBack.callback(6, 4, 0, fwVersion);
                                return;
                            }
                            SimpleUtil.log("1:get img agan!");
                        }
                        char type = mTargImgHdr.imgType;
                        mTargImgHdr.imgType = null;

                        SimpleUtil.sleep(50);
                        for (int i = 0; i < 6; i++) {
                            time = System.currentTimeMillis();
                            BTJobsManager.getInstance().write(new byte[]{1}, oadService_UUID, mCharIdentify.getUuid().toString(), new BleWriteCallback() {
                                @Override
                                public void onWriteSuccess() {
                                    SimpleUtil.log("send the get img again2:ok");
                                }

                                @Override
                                public void onWriteFailure(BleException exception) {
                                    SimpleUtil.log("send the get img again2:fail\n" + exception.getDescription());
                                }
                            });
                            while (mTargImgHdr.imgType == null && (System.currentTimeMillis() - time) < 3000)
                                ;
                            if (mTargImgHdr.imgType != null) {
                                SimpleUtil.log("we had got the second img!");
                                break;
                            }
                            if (mTargImgHdr.imgType == null && i == 5) {
                                SimpleUtil.log("we want to get the img second fail!we had try 5 times!!!");
                                iotaCallBack.callback(6, 4, 0, fwVersion);
                                return;
                            }

                            SimpleUtil.log("2:get img agan!");
                        }

                        if (type != mTargImgHdr.imgType) {
                            iotaCallBack.callback(6, 4, 0, fwVersion);
                            SimpleUtil.log("we comfirm the second img not equal the first img,so we think it is fail!");
                            return;
                        }


                        iotaCallBack.callback(6, 3, 0, fwVersion);
                        mCharBlock = oadCharacterList.get(1);

                        SimpleUtil.log("get OTA-BLOCK:" + mCharBlock.getUuid().toString());
                        mCharBlock.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);

                        //开启升级

                    } else if ((service.getUuid().toString().compareTo("f000ffc0-0451-4000-b000-000000000000")) == 0) {
                        //System.out.println("get control service success");
                        mConnControlService = service;
                    } else if ((service.getUuid().toString().compareTo(dISService_UUID)) == 0) {
                        //System.out.println("get control service success");
                        mBTService = service;

                        List<BluetoothGattCharacteristic> characteristics = mBTService.getCharacteristics();


                        for (final BluetoothGattCharacteristic c : characteristics) {
                            //gatt.setCharacteristicNotification(c,true);
                            SimpleUtil.log("subService" + c.getUuid().toString() + "    " + c.getProperties());

                            if (c.getUuid().toString().equals(dISSystemID_UUID)) {
                                systemIDc = c;
                            }
                            if (c.getUuid().toString().equals(dISModelNR_UUID)) {
                                modelNRc = c;
                            }
                            if (c.getUuid().toString().equals(dISSerialNR_UUID)) {
                                serialNRc = c;
                            }
                            if (c.getUuid().toString().equals(dISFirmwareREV_UUID)) {
                                firmwareREVc = c;
                                SimpleUtil.log("open the fw notify:" + enableNotify(gatt, firmwareREVc));
                                // SimpleUtil.sleep(1000);
                                int count = 0;
                                while (!gatt.readCharacteristic(firmwareREVc) && count++ < 10) {
                                    SimpleUtil.log("read the fw version fail!");
                                    SimpleUtil.sleep(100);
                                }
                            }
                            if (c.getUuid().toString().equals(dISHardwareREV_UUID)) {
                                hardwareREVc = c;
                            }
                            if (c.getUuid().toString().equals(dISSoftwareREV_UUID)) {
                                softwareREVc = c;
                            }
                            if (c.getUuid().toString().equals(dISManifacturerNAME_UUID)) {
                                ManifacturerNAMEc = c;
                            }
                        }
                    }
                }
                iotaCallBack.callback(6, 1, 0, null);
            }

        }).start();
    }

    private void sendIntvalFuctions() {
        if (!updateInteval) {
            updateInteval = true;

            int retryCount2 = 0;
            int reSendCount3 = 0;
            byte inteval_b = (byte) 0x0c;
            while (waitingInteval) {

                boolean isSendIntevalOK = setUpdateInteval2(inteval_b);

                if (!isSendIntevalOK) {

                    reSendCount3++;
                    if (reSendCount3 == 10) {
                        mProgramming = false;
                        iotaCallBack.callback(4, 0, 0, null);
                        SimpleUtil.log("send inteval fail!!!!!!!");
                        return;
                    }
                    continue;
                }
                reSendCount3 = 0;
                SimpleUtil.sleep(2000);
                setUpdateInteval(inteval_b);
                SimpleUtil.sleep(2000);
                retryCount2++;
                if (!waitingInteval) {
                    break;
                }
                if (retryCount2 == 10) {
                    mProgramming = false;
                    iotaCallBack.callback(4, 0, 0, null);
                    SimpleUtil.log("setinteval fail!!!!!!!");
                    return;
                }
                //inteval_b += (byte) 0x02;
                SimpleUtil.log("++inteval:" + inteval_b);
            }

        }
    }

    @Override
    public void run() {
        update();

    }

    private boolean updateInteval;
    private boolean waitingInteval = true;

    public boolean loadFile(String filepath, boolean isAsset) {
        //初始化文件

        try {
            // Read the file raw into a buffer
            InputStream stream;
            if (isAsset) {
                stream = context.getAssets().open(filepath);
            } else {
                File f = new File(filepath);
                stream = new FileInputStream(f);
            }
            stream.read(mFileBuffer, 0, mFileBuffer.length);
            stream.close();
        } catch (IOException e) {
            // Handle exceptions here
            SimpleUtil.log("File open failed: " + filepath + "\n");
            return false;
        }

        mFileImgHdr.ver = Conversion.buildUint16(mFileBuffer[5], mFileBuffer[4]);
        mFileImgHdr.len = Conversion.buildUint16(mFileBuffer[7], mFileBuffer[6]);
        //long templen = mFileImgHdr.len;
        //templen |= 0x80000000L;
        mFileImgHdr.imgType = ((mFileImgHdr.ver & 1) == 1) ? 'B' : 'A';
        System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);

        SimpleUtil.log("load file ver:" + mFileImgHdr.ver);
        SimpleUtil.log("load file len:" + mFileImgHdr.len);
        //System.out.println("load file templen:"+templen);
        SimpleUtil.log("load file imgType:" + mFileImgHdr.imgType);
        iotaCallBack.callback(1, (int) mFileImgHdr.ver, (int) mFileImgHdr.len, mFileImgHdr.imgType);
        iotaCallBack.callback(8, isAsset ? 0 : 1, 0, filepath);

        return true;
    }

    public void loadFromeNet(byte[] data, String server_ver) {

        System.arraycopy(data, 0, mFileBuffer, 0, data.length);
        mFileImgHdr.ver = Conversion.buildUint16(mFileBuffer[5], mFileBuffer[4]);
        mFileImgHdr.len = Conversion.buildUint16(mFileBuffer[7], mFileBuffer[6]);
        //long templen = mFileImgHdr.len;
        //templen |= 0x80000000L;
        mFileImgHdr.imgType = ((mFileImgHdr.ver & 1) == 1) ? 'B' : 'A';
        System.arraycopy(mFileBuffer, 8, mFileImgHdr.uid, 0, 4);

        SimpleUtil.log("load file ver:" + mFileImgHdr.ver);
        SimpleUtil.log("load file len:" + mFileImgHdr.len);
        //System.out.println("load file templen:"+templen);
        SimpleUtil.log("load file imgType:" + mFileImgHdr.imgType);
        iotaCallBack.callback(1, (int) mFileImgHdr.ver, (int) mFileImgHdr.len, mFileImgHdr.imgType);
        iotaCallBack.callback(10, 0, 0, "已加载网络固件，版本:" + server_ver);

    }

    private void update() {
        if (mFileImgHdr.imgType == mTargImgHdr.imgType) {
            iotaCallBack.callback(4, 1, 0, null);
            return;
        }
        int delay = (Integer) SimpleUtil.getFromShare(context, "ini", "otadelay", int.class, 20);
        SimpleUtil.log("otadelay:" + delay);
        //准备发送数据
        //recText.recSys(SimpleUtil.getCurTime());

        sendIntvalFuctions();
        if (waitingInteval) {
            //mProgramming = false;
            SimpleUtil.log("inteval set fail!!!!!!");
            //iotaCallBack.callback(9, 0, 0, null);
            //return;
        }

        byte[] buf = new byte[8 + 2 + 2];
        buf[0] = Conversion.loUint16(mFileImgHdr.ver);
        buf[1] = Conversion.hiUint16(mFileImgHdr.ver);
        buf[2] = Conversion.loUint16(mFileImgHdr.len);
        buf[3] = Conversion.hiUint16(mFileImgHdr.len);
        System.arraycopy(mFileImgHdr.uid, 0, buf, 4, 4);

        //add crc
        int fileCRC = calc_crc16(Arrays.copyOfRange(mFileBuffer, 16, (int) (mFileImgHdr.len * 4)), (int) (mFileImgHdr.len * 4 - 16));
        //iotaCallBack.callback(20, (int) (mFileImgHdr.len * 4 - 16), 0, null);
        buf[8] = Conversion.loUint16(fileCRC);
        buf[9] = Conversion.hiUint16(fileCRC);

        // Send image notification
        mCharIdentify.setValue(buf);
        boolean notifyOK = gatt.writeCharacteristic(mCharIdentify);
        SimpleUtil.log("send the filehead ok?" + notifyOK);
        mProgInfo.reset();
        //进度条初始化回调
        iotaCallBack.callback(2, (int) mProgInfo.nBlocks, 0, null);
        //开始发送数据
        mProgramming = true;
        while (mProgramming) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 6 & mProgramming; i++) {
                if (!mProgramming)
                    return;
                ifBlockSend = true;
                if (mProgInfo.iBlocks < mProgInfo.nBlocks) {
                    mProgramming = true;
                    // Prepare block
                    mOadBuffer[0] = Conversion.loUint16(mProgInfo.iBlocks);
                    mOadBuffer[1] = Conversion.hiUint16(mProgInfo.iBlocks);
                    System.arraycopy(mFileBuffer, (int) mProgInfo.iBytes, mOadBuffer, 2, 16);

                    int crc = calc_crc16(mOadBuffer, 18);
                    mOadBuffer[18] = Conversion.loUint16(crc);
                    mOadBuffer[19] = Conversion.hiUint16(crc);

                    mCharBlock.setValue(mOadBuffer);

                    //SimpleUtil.log("send block data:"+ Hex.toString(mOadBuffer));
                    blocking = true;
                    /*try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    boolean success = true;
                    int retryCount = 0;
                    curWrite = mOadBuffer;

                    writeState = 1;
                    //recText.recSys(Hex.toString(curWrite)+"   write");
                    gatt.writeCharacteristic(mCharBlock);
                    //SimpleUtil.log("OTAWrite:"+Hex.toString(mCharBlock.getValue()));
                    //recText.rec(Hex.toString(mCharBlock.getValue()));
                    long time = System.currentTimeMillis();
                    while (writeState != 0) {

                        if (writeState == 2 || (System.currentTimeMillis() - time) > 10000)//发送失败，要求重试
                        {
                            if (retryCount++ > 10) {
                                writeState = 3;
                                //SimpleUtil.closeDialog();
                                break;
                            }

                            if (BTJobsManager.getInstance().getGatt() == null) {
                                mProgramming = false;
                                SimpleUtil.log("GATT writeCharacteristic failed:" + retryCount);
                                iotaCallBack.callback(5, 0, 0, null);
                                return;
                            }

                            SimpleUtil.log("OTAWrite-re:" + Hex.toString(mCharBlock.getValue()));
                            //recText.recSys(Hex.toString(curWrite)+"   retry");
                            iotaCallBack.callback(7, 0, 0, null);
                            //SimpleUtil.showWaiting(context,"发送失败，正在重试 "+retryCount+"/10");
                            //recText.rec("error1-write:"+Hex.toString(mCharBlock.getValue())+"        retry   ->state:"+writeState);
                            gatt.writeCharacteristic(mCharBlock);
                            time = System.currentTimeMillis();
                            writeState = 1;
                        }
                    }

                    /*while(!(success=gatt.writeCharacteristic(mCharBlock)))
                    {
                        if(retryCount++>50000000)
                        {
                            break;
                        }
                    }*/

                    if (writeState == 0) {
                        //recText.rec(Hex.toString(mCharBlock.getValue()));
                        if (delay > 0) {
                            SimpleUtil.sleep(delay);
                        }
                        // Update stats
                        mProgInfo.iBlocks++;
                        mProgInfo.iBytes += 16;
                        // mProgressBar.setProgress((short)((mProgInfo.iBlocks * 100) / mProgInfo.nBlocks));
                        iotaCallBack.callback(3, (int) mProgInfo.iBlocks, 0, Hex.toString(mOadBuffer));

                    } else {
                        mProgramming = false;
                        SimpleUtil.log("GATT writeCharacteristic failed:" + retryCount);
                        iotaCallBack.callback(5, 0, 0, null);
                    }
                    if (!success) {
                        //if (success!=0) {
                        //mLog.append(msg);
                    }
                } else {
                    mProgramming = false;
                }
                ifBlockSend = false;

            }
            if ((mProgInfo.iBlocks % 100) < 6) {
                // Display statistics each 100th block

            }
        }
    }


    private boolean setUpdateInteval2(byte inteval_b) {
        mCharIdentify.setValue(new byte[]{0x01, 0x51, 0x02, inteval_b, inteval_b});

        final boolean[] waitSetting = {true};
        BTJobsManager.getInstance().write(mCharIdentify, new BleWriteCallback() {
            @Override
            public void onWriteSuccess() {
                SimpleUtil.log("send the update inteval is ok:" + Hex.toString(mCharIdentify.getValue()));
                waitSetting[0] = false;
            }

            @Override
            public void onWriteFailure(BleException exception) {
                SimpleUtil.log("send the update inteval is fail!!!!!!!!:" + Hex.toString(mCharIdentify.getValue()));

            }
        });
        long time0 = System.currentTimeMillis();
        while (waitSetting[0] && (System.currentTimeMillis() - time0) < 5000) {
            // SimpleUtil.log("wait for set the update inteval...");
            SimpleUtil.sleep(10);
        }
        if (waitSetting[0]) {
            SimpleUtil.log("wait for set the update inteval time out or fail.update fail!!!!!!!!");
            return false;
        }

        return true;
    }

    private void setUpdateInteval(final byte inteval_b) {
        final ByteArrayList data = new ByteArrayList();
        data.add((byte) 0xa5);
        data.add((byte) 0x06);
        data.add((byte) 0x12);
        data.add((byte) 0x00);
        data.add((byte) 0x01);
        data.add(SimpleUtil.sumCheck(data.all2Bytes()));


        BTJobsManager.getInstance().addBLENotify(new BTService.IBLENotify() {
            @Override
            public void notify(BTService.BLEMODTYPE mode, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (mode == BTService.BLEMODTYPE.CHANGE) {
                    byte[] data = characteristic.getValue();
                    if (data[2] == (byte) 0x12 && data.length >= 6) {

                        SimpleUtil.log("读取蓝牙interval[" + (data[3] & 0xff) + "]");
                        if (data[3] >= 0x0c) {
                            waitingInteval = false;
                        }
                        BTJobsManager.getInstance().removeBLENotify(this);
                    }
                }
            }
        });

        BTJobsManager.getInstance().write(data.all2Bytes(), BTService.getUUID(gatt.getDevice().getName(), BTService.UUID_TYPE.XJ_SERVICE), BTService.getUUID(gatt.getDevice().getName(), BTService.UUID_TYPE.WRITE), new BleWriteCallback() {
            @Override
            public void onWriteSuccess() {
                SimpleUtil.log("蓝牙interval发送成功");
            }

            @Override
            public void onWriteFailure(BleException exception) {
                SimpleUtil.log("蓝牙interval发送失败...");
            }
        });
    }

    private boolean enableNotify(BluetoothGatt gatt, BluetoothGattCharacteristic c) {
        //SimpleUtil.sleep(2000);
        if (!gatt.setCharacteristicNotification(c, true)) {
            return false;
        }
        List<BluetoothGattDescriptor> descriptors = c.getDescriptors();
        for (BluetoothGattDescriptor dp : descriptors) {
            if (dp != null) {
                if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                    if (!dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                        return false;
                    }
                } else if ((c.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                    if (!dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE)) {
                        return false;
                    }
                }
                SimpleUtil.sleep(200);
                if (!gatt.writeDescriptor(dp)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isGetImageOK() {
        return mTargImgHdr != null && mTargImgHdr.imgType != null;
    }

    @Override
    public void notify(BTService.BLEMODTYPE mode, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (oadImageNotify_UUID.equals(characteristic.getUuid().toString())) {
            //chara back
            if (mode == BTService.BLEMODTYPE.CHANGE) {

                byte[] imageData = characteristic.getValue();
                if (imageData.length > 4) {
                    SimpleUtil.log("get image data:" + Hex.toString(imageData));
                    mTargImgHdr.ver = Conversion.buildUint16(imageData[1], imageData[0]);
                    mTargImgHdr.imgType = ((mTargImgHdr.ver & 1) == 1) ? 'B' : 'A';
                    mTargImgHdr.len = Conversion.buildUint16(imageData[3], imageData[2]);
                    long imgVer = (mTargImgHdr.ver) >> 1;
                    long imgSize = mTargImgHdr.len * 4;
                    //curImage = String.format("Type: %c Ver.: %d Size: %d", mTargImgHdr.imgType, imgVer, imgSize);
                    //curImage = mTargImgHdr.imgType +"";
                }

            }

        } else if (dISFirmwareREV_UUID.equals(characteristic.getUuid().toString())) {

            if (mode == BTService.BLEMODTYPE.READ) {
                byte[] fwData = characteristic.getValue();
                SimpleUtil.log("get fw brand:" + Hex.toString(fwData));
                try {
                    fwVersion = new String(fwData, "UTF-8");
                    //fwVersion = "1111";
                    SimpleUtil.log("handle:6-2");
                    iotaCallBack.callback(6, 2, 0, fwVersion);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else if (oadBlockRequest_UUID.equals(characteristic.getUuid().toString())) {
            //chara back
            if (mode == BTService.BLEMODTYPE.WRITE) {
                byte[] data = characteristic.getValue();

                if (equalBytes(data, curWrite)) {
                    if (status == 0) {
                        //recText.recSys(Hex.toString(data)+"   back  "+Hex.toString(curWrite));
                        writeState = 0;

                        //recText.rec(Hex.toString(mCharBlock.getValue()));
                    }
                } else {
                    //recText.recSys(Hex.toString(data)+"   back error  "+Hex.toString(curWrite));
                    writeState = 2;

                    //recText.rec("error2-write:"+Hex.toString(mCharBlock.getValue())+"        bad data");
                }
                //iotaCallBack.callback(5,0,0,Hex.toString(characteristic.getValue())+"  state:"+status);
            }

        }

    }

    private boolean equalBytes(byte[] data1, byte[] data2) {
        if (data1 == null || data2 == null || data1.length != data2.length) {
            return false;
        }
        int len = data1.length;
        for (int i = 0; i < len; i++) {
            if (data1[i] != data2[i]) {
                return false;
            }
        }
        return true;
    }

    private class ImgHdr {
        long ver;
        long len;
        Character imgType;
        byte[] uid = new byte[4];
    }

    public class bleRequest {
        public int id;
        public BluetoothGattCharacteristic characteristic;
        public bleRequestOperation operation;
        public volatile bleRequestStatus status;
        public int timeout;
        public int curTimeout;
        public boolean notifyenable;
    }

    public enum bleRequestOperation {
        wrBlocking,
        wr,
        rdBlocking,
        rd,
        nsBlocking,
    }

    public enum bleRequestStatus {
        not_queued,
        queued,
        processing,
        timeout,
        done,
        no_such_request,
        failed,
    }

    private class ProgInfo {
        long iBytes = 0; // Number of bytes programmed
        long iBlocks = 0; // Number of blocks programmed
        long nBlocks = 0; // Total number of blocks
        int iTimeElapsed = 0; // Time elapsed in milliseconds

        void reset() {
            iBytes = 0;
            iBlocks = 0;
            iTimeElapsed = 0;
            nBlocks = (short) (mFileImgHdr.len / (16 / 4));
            //System.out.println("nBlocks:"+nBlocks);
        }
    }


    public boolean addRequestToQueue(bleRequest req) {
        lock.lock();
        if (procQueue.peekLast() != null) {
            req.id = procQueue.peek().id++;
        } else {
            req.id = 0;
            procQueue.add(req);
        }
        lock.unlock();
        return true;
    }

    private volatile bleRequest curBleRequest = null;

    public bleRequestStatus pollForStatusofRequest(bleRequest req) {
        lock.lock();
        if (req == curBleRequest) {
            bleRequestStatus stat = curBleRequest.status;
            if (stat == bleRequestStatus.done) {
                curBleRequest = null;
            }
            if (stat == bleRequestStatus.timeout) {
                curBleRequest = null;
            }
            lock.unlock();
            return stat;
        } else {
            lock.unlock();
            return bleRequestStatus.no_such_request;
        }
    }

    public interface IOTACallBack {
        void callback(int mode, int arg1, int arg2, Object obj);
    }

}
