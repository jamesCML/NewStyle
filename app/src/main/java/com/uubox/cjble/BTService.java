package com.uubox.cjble;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.data.BleScanState;
import com.clj.fastble.exception.OtherException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.uubox.padtool.R;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.Hex;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 键鼠设备操作（连接、断连等）服务。
 */
public class BTService extends Service {

    private static final String XJ_ET_UUID_SERVICE = "00000000-0000-1000-8000-00805f9b34fb";
    //    private static final String ET_UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String ET_UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String ET_UUID_WRITE = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String ET_UUID_NOTIFY = "0000ffe2-0000-1000-8000-00805f9b34fb";
    /**
     * K100's UUID
     */
    private static final String ET_UUID_SERVICE_K100 = "91680001-1111-6666-8888-0123456789ab";
    private static final String ET_UUID_WRITE_K100 = "91680002-1111-6666-8888-0123456789ab";
    private static final String ET_UUID_NOTIFY_K100 = "91680003-1111-6666-8888-0123456789ab";

    public enum UUID_TYPE {
        SERVICE, XJ_SERVICE, WRITE, NOTIFY
    }

    public static final String getUUID(String name, UUID_TYPE uuidType) {
        if (name.contains("K100")
                || name.contains("P200")) {
            switch (uuidType) {
                case XJ_SERVICE:
                case SERVICE:
                    return ET_UUID_SERVICE_K100;
                case WRITE:
                    return ET_UUID_WRITE_K100;
                case NOTIFY:
                    return ET_UUID_NOTIFY_K100;
                default:
                    return null;
            }
        } else {
            switch (uuidType) {
                case XJ_SERVICE:
                    return XJ_ET_UUID_SERVICE;
                case SERVICE:
                    return ET_UUID_SERVICE;
                case WRITE:
                    return ET_UUID_WRITE;
                case NOTIFY:
                    return ET_UUID_NOTIFY;
                default:
                    return null;
            }
        }
    }


    public final static int[] KEY = new int[]{//zikway201511_cj0
            Hex.toIntB("zikw".getBytes()), Hex.toIntB("ay20".getBytes()),
            Hex.toIntB("1511".getBytes()), Hex.toIntB("_cj0".getBytes())
    };
    private static final String TAG = "zhiwan-BTService";
    private final Binder binder = new LocalBinder();
    private boolean isBleManagerInitialized;
    //接收数据时的时间
    private IStateCallBack iBlueconect;
    private BluetoothGatt mGatt;
    private int mConnectState;
    private CopyOnWriteArraySet<IBLENotify> notifies = new CopyOnWriteArraySet<>();
    private BluetoothGattCharacteristic writeCharacter;
    private Handler mHandler;
    /**
     * 0:未指定连接方式
     * 1:跳到系统连接方式
     * 2:APP连接方式
     */
    private int connectMode = 1;
    private BluetoothDevice connectedDebice;
    private IKeyMouseData iKeyMouseData;
    //public static RecText recText;
    private BroadcastReceiver blueStateListner = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                Log.i(TAG, "bluetooth state change:" + blueState);
                if (blueState == BluetoothAdapter.STATE_ON) {
                    Log.i(TAG, "检测到蓝牙打开，开启扫描！" + Thread.currentThread().getName());
                    //scanAndConnect();
                } else if (blueState == BluetoothAdapter.STATE_OFF) {
                    // BleManager.getInstance().disconnectAllDevice();
                    // BleManager.getInstance().destroy();
                    Log.i(TAG, "检测到蓝牙已经关闭，正在释放资源！");

                }
            }
        }
    };
    /**
     * Wisega 激活方式
     */
    private final byte[] SET_ADB = new byte[]{(byte) 0xA5, (byte) 0x05, (byte) 0xE1, (byte) 0x00, (byte) 0x8B};

    public BTService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null || intent.getAction() == null)
            return START_STICKY;

        switch (intent.getAction()) {
            case "com.hexad.bluezime.connect":
                if (!isBleManagerInitialized) {
                    initBleManager();
                }
                //默认方式1
                if (connectMode == 1) {
                    scanBound();
                }
                //setScanRule();
                //scanAndConnect();
                break;
            case "com.hexad.bluezime.disconnect":
                BleManager.getInstance().disconnectAllDevice();
                BleManager.getInstance().notify();
                BleManager.getInstance().destroy();
                break;
            default:
                break;
        }
        registerReceiver(blueStateListner, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        return START_STICKY;
    }

    private void initBleManager() {
        BleManager.getInstance().init(getApplication());
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(7)
                .setOperateTimeout(5000);
        BleScanRuleConfig.Builder config = new BleScanRuleConfig.Builder();
        config.setDeviceName(true, "CJ007", "Gamesir-X1", "KT008", "K100").setScanTimeOut(15000);
        BleManager.getInstance().initScanRule(config.build());
        isBleManagerInitialized = true;
    }

    private void disConnectWithMode2() {

        if (mGatt != null) {
            mGatt.close();
            mGatt.disconnect();
           /* try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mGatt.close();*/
            mGatt = null;
            mConnectState = 0;
            iBlueconect.connectstate(1, EState.DISCONNECTED, null);

        }

    }

    @SuppressLint("PrivateApi")
    public synchronized boolean scanBound() {
        Log.i(TAG, "start scanBound:" + mConnectState);
        //recText.rec("scanBound");
        if (mConnectState != 0 || BleManager.getInstance().getBluetoothAdapter() == null || !BleManager.getInstance().getBluetoothAdapter().isEnabled()) {
            return false;
        }
        mConnectState = 1;
        boolean isConnect = false;
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
        try {//得到连接状态的方法
            Method method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
            method.setAccessible(true);
            int state = (int) method.invoke(adapter, (Object[]) null);
            Log.i(TAG, "===================================:" + state);
            //recText.rec("=3
            // ==================================:" + state);
            if (state == BluetoothAdapter.STATE_CONNECTED) {
                Set<BluetoothDevice> devices = adapter.getBondedDevices();
                Log.i(TAG, "device num:" + devices.size());
                BluetoothDevice otherDevice = null;
                for (BluetoothDevice device : devices) {
                    Method isConnectedMethod = null;
                    try {
                        isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
                        method.setAccessible(true);
                    } catch (Exception e) {
                        Log.e(TAG, "scan connect exception!!!!");
                        e.printStackTrace();
                    }
                    boolean isConnected = isConnectedMethod == null || (boolean) isConnectedMethod.invoke(device, (Object[]) null);
                    if (isConnected) {
                        if ((device.getName().contains("CJ007") || device.getName().contains("Gamesir-X1") || device.getName().contains("KT008") || device.getName().contains("K100") || device.getName().contains("mingpin") || device.getName().contains("pow"))) {
                            Log.i(TAG, "get the device:" + device.getName() + "[" + device.getAddress() + "]");
                            //recText.rec("get the device:" + device.getName() + "[" + device.getAddress() + "]");
                            isConnect = true;
                            connect(device);
                            break;
                        }
                        otherDevice = device;
                    }
                }
                if (!isConnect) {
                    if (otherDevice != null) {
                        isConnect = true;
                        connect(otherDevice);
                    } else {
                        for (BluetoothDevice device : devices) {
                            if ((device.getName().contains("CJ007") || device.getName().contains("Gamesir-X1") || device.getName().contains("KT008") || device.getName().contains("K100") || device.getName().contains("mingpin") || device.getName().contains("pow")) || device.getName().contains("P10")) {
                                Log.i(TAG, "get the device:" + device.getName() + "[" + device.getAddress() + "]");
                                //recText.rec("get the device:" + device.getName() + "[" + device.getAddress() + "]");
                                isConnect = true;
                                connect(device);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "===================================异常");
            //recText.rec("execption1:"+e.getMessage());
        }
        if (!isConnect) {
            mConnectState = 0;
            iBlueconect.connectstate(1, EState.DISCONNECTED, null);
        }
        return isConnect;
    }

    public void scanDeviceWithMode2(final IStateCallBack iGetDevice) {
        if (BleManager.getInstance().getScanSate() == BleScanState.STATE_SCANNING) {
            Log.i(TAG, "正在扫描，禁止多次扫描...");
            return;
        }
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                Log.i(TAG, "onScanStarted");
            }

            @Override
            public void onScanning(BleDevice result) {
                Log.i(TAG, "onScanning:" + result.getDevice().getName());
                iGetDevice.connectstate(5, null, result);
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Log.i(TAG, "onScanFinished:" + scanResultList.size());
            }
        });
    }

    private BluetoothDevice bluetoothDeviceTmp;

    public void connect(final BluetoothDevice bluetoothDevice) {
        bluetoothDeviceTmp = bluetoothDevice;
        bluetoothDevice.connectGatt(getApplicationContext(), false, bluetoothGattCallback);
    }

    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //Log.i("zhiwan", "RRRRRRRRRR:[" + characteristic.getUuid().toString() + "]" + Hex.toString(characteristic.getValue()));
            notifyAllEx(BLEMODTYPE.READ, gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            notifyAllEx(BLEMODTYPE.WRITE, gatt, characteristic, status);
            // Log.i(TAG,"SDK-Write:"+characteristic.getUuid().toString()+":"+Hex.toString(characteristic.getValue()));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            Log.i(TAG, "CCCCCCCC:[" + characteristic.getUuid().toString() + "]" + Hex.toString(characteristic.getValue()));
            handleRecData(characteristic);
            notifyAllEx(BLEMODTYPE.CHANGE, gatt, characteristic, -100);
        }

        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            Log.i(TAG, status + "," + newState + "   " + Thread.currentThread().getName());

            //recText.rec(status + "," + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:

                    Log.i(TAG, "蓝牙已经连接");
                    //recText.rec("bluetooth is connected! go to refresh the view");
                    mConnectState = 2;
                    mGatt = gatt;
                    connectedDebice = bluetoothDeviceTmp;
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    iBlueconect.connectstate(1, EState.CONNECTING, null);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    mConnectState = 0;
                    if (mGatt == null) {
                        return;
                    }
                    mGatt.close();
                    mGatt = null;
                    iBlueconect.connectstate(1, EState.DISCONNECTED, null);

                    if (connectMode == 1) {
                        mHandler.sendEmptyMessage(1);
                    }
                    break;
            }


            if (newState == BluetoothProfile.STATE_CONNECTED) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        gatt.discoverServices();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        UUID serviceUUID = null;

                        for (BluetoothGattService gattService : gatt.getServices()) {
                            if ((gattService.getUuid().toString().contains(getUUID(gatt.getDevice().getName(), UUID_TYPE.SERVICE))) || (gattService.getUuid().toString().contains(getUUID(gatt.getDevice().getName(), UUID_TYPE.XJ_SERVICE)))) {
                                serviceUUID = gattService.getUuid();
                            }
                        }
                        enableNotificationOfCharacteristic2(gatt, serviceUUID);
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //iBlueconect.connectstate(3, null, null);
                        try {//部分手机需要延迟更新设备
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        iBlueconect.connectstate(1, EState.CONNECTED, gatt.getDevice());
                    }
                }).start();
            }
        }
    };

    private void enableNotificationOfCharacteristic2(BluetoothGatt gatt, UUID serviceUUID) {
        UUID CharaUUID = UUID.fromString(getUUID(gatt.getDevice().getName(), UUID_TYPE.NOTIFY));
        List<BluetoothGattService> services = gatt.getServices();
        Log.i(TAG, "services-size:" + services.size());
        for (BluetoothGattService service : services) {
            Log.i(TAG, "subservice:" + service.getUuid().toString());
        }
        BluetoothGattService service = gatt.getService(serviceUUID);

        if (service != null) {
            BluetoothGattCharacteristic chara = service.getCharacteristic(CharaUUID);
            gatt.setCharacteristicNotification(chara, true);
            if (chara != null) {

                List<BluetoothGattDescriptor> descriptors = chara.getDescriptors();
                for (BluetoothGattDescriptor dp : descriptors) {
                    if (dp != null) {
                        if ((chara.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        } else if ((chara.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
                            dp.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                        }
                        gatt.writeDescriptor(dp);
                    }
                }
            }
        }
    }

    private void handleRecData(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        Log.i("zhiwan", "sdk-handleRecData: data = " + Hex.toString(data));
        if (data.length < 3) {
            return;
        }
        if (data[0] == (byte) 0xa5 || data[0] == (byte) 0x20)//包头 (K100 0x20)
        {
            int len = data[1] & 0xff;
            if (data.length < len) {
                Log.e(TAG, "丢包！");
                return;
            }
            /*if (sumCheck(Arrays.copyOfRange(data, 0, len - 1)) != data[len - 1]) {
                Log.e(TAG, "校验错误！");
                return;
            }*/

            //得到有效数据


            switch (data[2] & 0xff) {
                case 0x01:
                    if (data.length < 17) {
                        Log.e(TAG, "onCharacteristicChanged: bad length = " + data.length);
                    }

                    break;
//                case 0xe2:
                case 0x02:
                    byte[] content = Arrays.copyOfRange(data, 3, len);
                    iBlueconect.connectstate(2, null, content);
                    break;
                case 0x11:
                    //确认信息返回
                    //Tea tea = new Tea();
                    //tea.decrypt(content,0,KEY,32);
                    break;
                case 0xe1:
                    /*if (data[3] == 0x00)
                        iBlueconect.connectstate(4, null, null);*/
                    break;
            }
        }
    }

    private byte sumCheck(byte[] data) {
        byte result = 0;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            result = (byte) (result + data[i]);
        }
        return result;
    }

    private void notifyAllEx(BLEMODTYPE mode, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        for (IBLENotify ibleNotify : notifies) {
            ibleNotify.notify(mode, gatt, characteristic, status);
        }
    }

    public void write(byte[] data, String serviceUUID_, String charactUUID_, BleWriteCallback bleWriteCallback) {
        UUID serviceUUID = UUID.fromString(serviceUUID_);
        UUID charactUUID = UUID.fromString(charactUUID_);
        BluetoothGattService service = null;
        if (serviceUUID == null || mGatt == null) {
            bleWriteCallback.onWriteFailure(new OtherException("null point-1:" + serviceUUID + "," + mGatt));
            return;
        }
        service = mGatt.getService(serviceUUID);

        if (service == null || charactUUID == null) {
            bleWriteCallback.onWriteFailure(new OtherException("null point-1:" + service + "," + charactUUID));
            return;
        }
        writeCharacter = service.getCharacteristic(charactUUID);
        if (data == null || data.length <= 0) {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("the data to be written is empty"));
            return;
        }

        if (writeCharacter == null
                || (writeCharacter.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) == 0) {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("this characteristic not support write!"));
            return;
        }

        if (writeCharacter.setValue(data)) {
            if (!mGatt.writeCharacteristic(writeCharacter)) {
                if (bleWriteCallback != null)
                    bleWriteCallback.onWriteFailure(new OtherException("gatt writeCharacteristic fail"));
            } else {
                bleWriteCallback.onWriteSuccess();
            }
        } else {
            if (bleWriteCallback != null)
                bleWriteCallback.onWriteFailure(new OtherException("Updates the locally stored value of this characteristic fail"));
        }
    }

    /**
     * 设置服务为前台
     *
     * @param text
     */
    @SuppressLint("ObsoleteSdkInt")
    private void setUpAsForeground(String text) {
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(text)
                    .setTicker(text)
                    .setSmallIcon(R.mipmap.ic_folat_online)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_folat_online))
                    .build();
        }
        startForeground(1, notification);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onCreate() {
        super.onCreate();
        //将蓝牙连接服务设置为前台
        setUpAsForeground(CommonUtils.getAppName(this));
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        scanBound();
                        mHandler.sendEmptyMessageDelayed(1, 3000);
                        break;
                }
            }
        };
    }

    public enum EState {STOPSCAN, TIMEOUTANDRESET, STARTSCAN, CONNECTED, CONNECTING, CONNECTFAIL, DISCONNECTED, USERDISCONNECTED, DISCONNECTING, SCANNING, SCANOVER}

    public enum BLEMODTYPE {READ, WRITE, CHANGE}
    public interface IStateCallBack {
        void connectstate(int type, EState state, Object obj);
    }

    public interface IBLENotify {
        void notify(BLEMODTYPE mode, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status);
    }

    public interface IKeyMouseData {
        void backdata(byte[] data);
    }

    public class LocalBinder extends Binder implements Serializable {

        BTService getService() {
            return (BTService.this);
        }

        public void setIStateCallBack(IStateCallBack iBlueconect_) {
            iBlueconect = iBlueconect_;
        }

        public boolean scanBound() {
            return BTService.this.scanBound();
        }

        public BluetoothDevice getDevice() {
            return BTService.this.mGatt != null ? BTService.this.mGatt.getDevice() : null;
        }

        public void write(byte[] data, String serviceUUID, String charactUUID, BleWriteCallback bleWriteCallback) {
            BTService.this.write(data, serviceUUID, charactUUID, bleWriteCallback);
        }

        public void write(BluetoothGattCharacteristic characteristics, BleWriteCallback bleWriteCallback) {
            byte[] data = characteristics.getValue();
            if (data == null) {
                Log.i(TAG, "write data is null!");
                return;
            }
            BTService.this.write(data, characteristics.getService().getUuid().toString(), characteristics.getUuid().toString(), bleWriteCallback);
        }

        public void writeDefault(byte[] data, BleWriteCallback bleWriteCallback) {
            if (getBleGatt() == null) {
                return;
            }
            write(data, getUUID(getBleGatt().getDevice().getName(), UUID_TYPE.XJ_SERVICE), getUUID(getBleGatt().getDevice().getName(), UUID_TYPE.WRITE), bleWriteCallback);
        }

        public void addNotify(IBLENotify ibleNotify) {
            notifies.add(ibleNotify);
        }

        public void removeNotify(IBLENotify ibleNotify) {
            notifies.remove(ibleNotify);
        }

        public int getConnectMode() {
            return connectMode;
        }

        public void setConnectMode(int mode) {
            connectMode = mode;
        }

        public void scanDeviceWithMode2(IStateCallBack iStateCallBack) {
            BTService.this.scanDeviceWithMode2(iStateCallBack);
        }

        public void connectWithMode2(BleDevice bleDevice) {
            BTService.this.connect(bleDevice.getDevice());
        }

        public BluetoothDevice getConnectedDevice() {
            return connectedDebice;
        }

        public BluetoothGatt getBleGatt() {
            return BTService.this.mGatt;
        }

        public void disconnectWithMode2() {
            BTService.this.disConnectWithMode2();
        }

        public void initConnectedDevice() {
            connectedDebice = null;
        }

        public void setIKeyMouseData(IKeyMouseData iKeyMouseData) {
            BTService.this.iKeyMouseData = iKeyMouseData;
        }

        public void resetConnectState() {
            BTService.this.mConnectState = 0;
        }

        public void stopWatch() {
            mHandler.removeMessages(1);
        }
    }
}
