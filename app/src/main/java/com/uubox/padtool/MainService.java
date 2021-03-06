package com.uubox.padtool;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.uubox.cjble.BTJobsManager;
import com.uubox.threads.AccInputThread;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.Hex;
import com.uubox.tools.SimpleUtil;
import com.uubox.views.KeyboardEditWindowManager;
import com.uubox.views.KeyboardFloatView;
import com.uubox.views.KeyboardView;
import com.uubox.views.MouseUtils;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Arrays;

public class MainService extends Service implements SimpleUtil.INormalBack {
    private UsbManager mUSBManager;
    private UsbDeviceConnection mUsbDeviceConnection;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mUsbEndpointIN;
    private UsbEndpoint mUsbEndpointOUT;
    private UsbDevice mUsbDevice;

    public final static String USBPERMISSION = "com.uubox.newstyle.MainActivity.USBPERMISSION";
    public final static String ACCUSBPERMISSION = "com.uubox.newstyle.MainActivity.ACCUSBPERMISSION";
    private ParcelFileDescriptor mParcelFileDescriptor;
    private AOAConfigTool mAOAConfigTool;
    private final int HANDLE_SCAN_AOA = 2;
    private boolean mHasTry;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SimpleUtil.log("服务Oncreate");
        SimpleUtil.zoomx = (Integer) SimpleUtil.getFromShare(getBaseContext(), "ini", "zoomx", int.class);
        SimpleUtil.zoomy = (Integer) SimpleUtil.getFromShare(getBaseContext(), "ini", "zoomy", int.class);
        initWindowOnlyOnce();
        SimpleUtil.addINormalCallback(this);
        mAOAConfigTool = AOAConfigTool.getInstance(this);
        mUSBManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (BTJobsManager.OPEN) {
            BTJobsManager.getInstance().bindBTService(this);
        }

        // AOAConfigTool.getInstance(this).bleDiveSend(Hex.parse("01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f 10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e"),(byte)0xdd);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SimpleUtil.log("服务Start");
        HomeWatcherReceiver homeReceiver = new HomeWatcherReceiver();
        registerReceiver(homeReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        init();

        if (BtnParamTool.isShowKbFloatView(getBaseContext()) && mWindowManager.getDefaultDisplay().getRotation() * Surface.ROTATION_90 == 1) {
            SimpleUtil.log("开启小健位");
            SimpleUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    KeyboardFloatView.getInstance(getBaseContext()).show();
                }
            }, 1500);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void init() {

        //先判断是否支持USB
        boolean isSurportUSB = isSurportUSB();
        SimpleUtil.log("support usb:" + isSurportUSB);
        if (!isSurportUSB) {
            return;
        }
        if (mParcelFileDescriptor != null) {
            mHandler.removeMessages(HANDLE_SCAN_AOA);
            SimpleUtil.log("already exits!");
            return;
        }
        //列出设备
     /*   HashMap<String, UsbDevice> deviceList = mUSBManager.getDeviceList();
        Iterator<UsbDevice> it = deviceList.values().iterator();
        SimpleUtil.log("devicesList:" + deviceList.size() + "");
        if (!it.hasNext()) {
            //SimpleUtil.log("0 device!");
            // return;
        } else {
            UsbDevice device = it.next();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    SimpleUtil.log(device.getManufacturerName() + "$" + device.getVersion());
                }
            }
        }*/


        //control方式

       /* UsbDevice device = it.next();
        if(!mUSBManager.hasPermission(device)) {
            requstPermission(device);
        }
        else {
            openUSBDevice(device);
        }*/

        //acc方式

        UsbAccessory[] usbAccessories = mUSBManager.getAccessoryList();

        if (usbAccessories == null) {
            SimpleUtil.log("return usbAccessories list is null!!!!!!");
            mfloatingIv.setImageResource(getAPPUserFloatIcon((Integer) mfloatingIv.getTag() == 0));
            //mHandler.sendEmptyMessageDelayed(HANDLE_SCAN_AOA, 3000);
            return;
        }
        //防止开启异常后继续扫描
        //mHandler.sendEmptyMessageDelayed(HANDLE_SCAN_AOA, 5000);
        String s = "accessory：\n" +
                "model:" + usbAccessories[0].getModel() + "\n" +
                "Manufacturer:" + usbAccessories[0].getManufacturer() + "\n" +
                "Version:" + usbAccessories[0].getVersion() + "\n" +
                "Description:" + usbAccessories[0].getDescription() + "\n" +
                "Serial:" + usbAccessories[0].getSerial() + "\n" +
                "Uri:" + usbAccessories[0].getUri();
        SimpleUtil.log("*******************************************************");
        SimpleUtil.log(s);
        SimpleUtil.log("*******************************************************");
        if (mUSBManager.hasPermission(usbAccessories[0])) {
            openUsbAccessory(usbAccessories[0]);
        } else {
            requestUsbAccessoryPermission(usbAccessories[0]);
        }

    }

    private void restartApplication() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void openUsbAccessory(UsbAccessory usbAccessory) {
        SimpleUtil.log("openUsbAccessory:" + this.hashCode());

        mParcelFileDescriptor = mUSBManager.openAccessory(usbAccessory);

        if (mParcelFileDescriptor == null) {
            SimpleUtil.addMsgBottomToTop(this, getString(R.string.ms_condevfail), true);
            //restartApplication();
            return;
        }
        FileDescriptor fileDescriptor = mParcelFileDescriptor.getFileDescriptor();
        final AccInputThread mAccInputThread = new AccInputThread(this, fileDescriptor);
        mAccInputThread.start();
        mAOAConfigTool.startConnect(mAccInputThread);
        SimpleUtil.log("openUsbAccessory sucessful!!!!!");
        mAOAConfigTool.setAOAInfo(usbAccessory.getManufacturer(), usbAccessory.getModel(), usbAccessory.getSerial());
        mHandler.removeMessages(HANDLE_SCAN_AOA);

        SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleUtil.sleep(1500);
                SimpleUtil.notifyall_(10004, null);
                SimpleUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        mfloatingIv.setImageResource(getAPPUserFloatIcon((Integer) mfloatingIv.getTag() == 0));
                    }
                }, 200);
            }
        });

    }

    private void closeAcc() {
        try {
            SimpleUtil.log("usb accessonry detached!");
            if (mParcelFileDescriptor != null) {
                mParcelFileDescriptor.close();
                mParcelFileDescriptor = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void requestUsbAccessoryPermission(UsbAccessory usbAccessory) {
        SimpleUtil.log("开始申请AOA权限");
        mHandler.removeMessages(HANDLE_SCAN_AOA);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACCUSBPERMISSION), 0);
        mUSBManager.requestPermission(usbAccessory, pendingIntent);
    }

    private boolean isSurportUSB() {
        return getPackageManager().hasSystemFeature("android.hardware.usb.accessory");
    }

    private void requstPermission(UsbDevice usbDevice) {
        Intent intent = new Intent(USBPERMISSION);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        boolean isHasPermission = mUSBManager.hasPermission(usbDevice);
        SimpleUtil.log("openDevice is device has permission:" + isHasPermission);
        if (isHasPermission) {
            openUSBDevice(usbDevice);
            return;
        }
        mUSBManager.requestPermission(usbDevice, pendingIntent);
    }

    private void openUSBDevice(UsbDevice usbDevice) {
        if (usbDevice == null) {
            return;
        }
        SimpleUtil.log("openUSBDevice:" + usbDevice.getDeviceName());
        UsbInterface usbInterface = getUsbInterface(usbDevice);
        if (usbDevice != null && usbInterface != null) {
            UsbDeviceConnection usbDeviceConnection = mUSBManager.openDevice(usbDevice);
            if (usbDeviceConnection != null) {
                mUsbDevice = usbDevice;
                mUsbInterface = usbInterface;
                mUsbDeviceConnection = usbDeviceConnection;
                //enumerateEndPoint(usbInterface);
            }
        }

    }

    private void enumerateEndPoint(UsbInterface sInterface) {
        if (sInterface == null) {
            return;
        }
        for (int i = 0x0; i < sInterface.getEndpointCount(); i++) {
            UsbEndpoint endPoint = sInterface.getEndpoint(i);
            SimpleUtil.log("endPoint addr:" + endPoint.getAddress() + " packsize:" + endPoint.getMaxPacketSize());
            if (endPoint.getAddress() == 0x82) {
                if (endPoint.getMaxPacketSize() == 0x20) {
                    // mBulkInPoint = endPoint;
                    SimpleUtil.log("get mBulkInPoint");
                }
            } else if (endPoint.getAddress() == 0x2) {
                //mBulkOutPoint = endPoint;
                SimpleUtil.log("get mBulkOutPoint");
            }
            // mBulkPacketSize = endPoint.getMaxPacketSize();
        }
    }

    private UsbInterface getUsbInterface(UsbDevice usbdevice) {
        UsbDeviceConnection usbDeviceConnection = mUsbDeviceConnection;
        if (usbDeviceConnection != null) {
            if (mUsbInterface != null) {
                mUsbDeviceConnection.releaseInterface(mUsbInterface);
                mUsbDeviceConnection.close();
                mUsbInterface = null;
                mUsbDeviceConnection = null;
            }
        }

        if (usbdevice == null) {
            return null;
        }
        int interfaceCount = usbdevice.getInterfaceCount();
        if (interfaceCount <= 0) {
            return null;
        }
        SimpleUtil.log("device interface count:" + interfaceCount);
        for (int i = 0; i < interfaceCount; i++) {
            UsbInterface usbInterface = usbdevice.getInterface(i);
            if (usbInterface.getInterfaceClass() == 3)//HID
            {
                if (usbInterface.getInterfaceSubclass() == 0) {
                    return usbInterface;
                }
            }
        }
        return null;
    }

    private void initCommunication(UsbDevice device) {
        SimpleUtil.log("initCommunication in\n");
        if (8746 == device.getVendorId() && 1 == device.getProductId()) {
            SimpleUtil.log("initCommunication in right device\n");
            int interfaceCount = device.getInterfaceCount();
            for (int interfaceIndex = 0; interfaceIndex < interfaceCount; interfaceIndex++) {
                UsbInterface usbInterface = device.getInterface(interfaceIndex);
                SimpleUtil.log("interfaceClass:" + usbInterface.getInterfaceClass());
                if ((UsbConstants.USB_CLASS_CDC_DATA != usbInterface.getInterfaceClass())
                        && (UsbConstants.USB_CLASS_COMM != usbInterface.getInterfaceClass())) {
                    continue;
                }

                for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                    UsbEndpoint ep = usbInterface.getEndpoint(i);
                    if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                        if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                            mUsbEndpointOUT = ep;
                            SimpleUtil.log("getout");
                        } else {
                            mUsbEndpointIN = ep;
                            SimpleUtil.log("getin");
                        }
                    }
                }

                if ((null == mUsbEndpointIN) || (null == mUsbEndpointOUT)) {
                    SimpleUtil.log("endpoint is null\n");
                    mUsbEndpointIN = null;
                    mUsbEndpointOUT = null;
                    mUsbInterface = null;
                } else {
                    SimpleUtil.log("\nendpoint out: " + mUsbEndpointOUT + ",endpoint in: " +
                            mUsbEndpointIN.getAddress() + "\n");
                    mUsbInterface = usbInterface;
                    mUsbDeviceConnection = mUSBManager.openDevice(device);
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SimpleUtil.log("MainService is onDestroy!");
        KeyboardEditWindowManager.getInstance().init(getBaseContext()).close();
        System.exit(0);
        //unregisterReceiver(USBStateBroadRec);
    }


    //from CJ
    /**
     * 消息-移动到左侧
     */
    private static final int MSG_MOVE_TO_LEFT_SIDE = 0;
    private static final int MSG_HIDE_ENTER_GAME = 1;
    /**
     * 时间-未操作多久后移动到左侧
     */
    private static final int TIME_MOVE_TO_LEFT_SIDE_DELAY = 3000;
    /**
     * 时间-左移动画持续时间
     */
    private static final int TIME_ANIMATION_DURATION = 500;
    /**
     * 定义浮动窗口布局
     */
    LinearLayout mlayout;
    /**
     * 悬浮窗控件
     */
    ImageView mfloatingIv;
    //ImageView mfloatingentergame;
    /**
     * 悬浮窗的布局
     */
    WindowManager.LayoutParams wmParams;
    LayoutInflater inflater;
    /**
     * 创建浮动窗口设置布局参数的对象
     */
    WindowManager mWindowManager;

    //触摸监听器
    GestureDetector mGestureDetector;
    //开始触控的坐标，移动时的坐标（相对于屏幕左上角的坐标）
    private int mTouchStartX, mTouchStartY, mTouchCurrentX, mTouchCurrentY;
    //开始时的坐标和结束时的坐标（相对于自身控件的坐标）
    private int mStartX, mStartY, mStopX, mStopY;
    private boolean isMove;//判断悬浮窗是否移动
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MOVE_TO_LEFT_SIDE:
                    moveToLeftSide();
                    break;
                case MSG_HIDE_ENTER_GAME:
                    //mfloatingentergame.setVisibility(View.GONE);
                    break;
                case HANDLE_SCAN_AOA:
                    init();
                    break;
            }
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SimpleUtil.log("newConfig:" + newConfig.toString());
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SimpleUtil.log("当前屏幕状态：横屏");
            SimpleUtil.screenstate = true;
            showFloatViews();
            //判断是否在升级

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            SimpleUtil.log("当前屏幕状态：竖屏");
            SimpleUtil.screenstate = false;
            hideFloatViews();
        }
        if (BTJobsManager.OPEN) {
            SimpleUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    SimpleUtil.log("当前升级状态:" + BTJobsManager.getInstance().isOTAUpdating());
                    if (BTJobsManager.getInstance().isOTAUpdating()) {

                        SimpleUtil.addWaitToTop(MainService.this, "");
                    }
                }
            }, 500);
        }
    }

    private void hideFloatViews() {
        mWindowManager.removeViewImmediate(mlayout);
        KeyboardEditWindowManager.getInstance().hideRootView();
        KeyboardFloatView.getInstance(getApplicationContext()).dismiss();

    }

    private void showFloatViews() {
        try {
            mWindowManager.addView(mlayout, wmParams);
            KeyboardEditWindowManager.getInstance().displayRootView();
            if (BtnParamTool.isShowKbFloatView(getBaseContext())) {
                KeyboardFloatView.getInstance(getApplicationContext()).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化windowManager
     */
    private void initWindowOnlyOnce() {

        if (mWindowManager != null) {
            SimpleUtil.log("mWindowManager is not null!return!");
            return;
        }
        setUpAsForeground(getString(R.string.ms_goodbess));
        mUSBManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        wmParams = getParams(wmParams);//设置好悬浮窗的参数
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 50;
        wmParams.y = 50;
        inflater = LayoutInflater.from(getApplicationContext());
        mlayout = (LinearLayout) inflater.inflate(R.layout.item_0, null);
        mWindowManager.addView(mlayout, wmParams);
        initFloating();
    }

    /**
     * 对windowManager进行设置
     *
     * @param wmParams
     * @return
     */
    public WindowManager.LayoutParams getParams(WindowManager.LayoutParams wmParams) {
        wmParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        return wmParams;
    }

    /**
     * 找到悬浮窗的图标，并且设置事件
     * 设置悬浮窗的点击、滑动事件
     */
    private void initFloating() {
        mfloatingIv = mlayout.findViewById(R.id.floating_imageView);
        //mfloatingentergame = mlayout.findViewById(R.id.floating_entergame);
        mfloatingIv.setImageResource(getAPPUserFloatIcon(false));
        mfloatingIv.getBackground().setAlpha(150);
        mfloatingIv.setTag(1);
        mGestureDetector = new GestureDetector(this, new MyOnGestureListener());
        //设置监听器
        mfloatingIv.setOnTouchListener(new FloatingListener());
        // 启动自动移动到左侧延时
        resetMoveToLeftSideDelay();

    }

    /**
     * 移动到左侧，移动完成后切换成小图标
     */
    private void moveToLeftSide() {
        ValueAnimator animator = ValueAnimator.ofInt(wmParams.x, 0);
        animator.setDuration(TIME_ANIMATION_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                wmParams.x = (int) animation.getAnimatedValue();
                try {
                    mWindowManager.updateViewLayout(mlayout, wmParams);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    // 悬浮窗口已经移除，取消后续的动画
                    // Log.e(Constants.TAG, "cancel animation");
                    animation.cancel();
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mfloatingIv.setTag(0);
                mfloatingIv.setImageResource(getAPPUserFloatIcon(true));
            }
        });
        animator.start();
    }


    private int getAPPUserFloatIcon(boolean atSide) {
        if (atSide) {
            return (mAOAConfigTool == null || !mAOAConfigTool.isConnect()) ? R.mipmap.ic_folat_offline_edge : R.mipmap.ic_folat_online_edge;
        } else {
            return (mAOAConfigTool == null || !mAOAConfigTool.isConnect()) ? R.mipmap.ic_folat_offline : R.mipmap.ic_folat_online;
        }

    }

    /**
     * 重置移动到左侧延时
     */
    private void resetMoveToLeftSideDelay() {
        mHandler.removeMessages(MSG_MOVE_TO_LEFT_SIDE);
        mHandler.sendEmptyMessageDelayed(MSG_MOVE_TO_LEFT_SIDE, TIME_MOVE_TO_LEFT_SIDE_DELAY);
    }

    @Override
    public void back(int id, final Object obj) {
        SimpleUtil.log("MainService getnotify id:" + id);
        if (id == 1) {
            boolean changed = BtnParamTool.hasBtnParamsChanged();
            if (changed) {
                KeyboardEditWindowManager.getInstance().close();
            } else {
                KeyboardEditWindowManager.getInstance().close();
                savechanged = false;
            }
            //SimpleUtil.removeINormalCallback(this);
        } else if (id == 10001) {
            if (obj == null) {
                return;
            }
            openUsbAccessory((UsbAccessory) obj);
        } else if (id == 10002) {
            //SimpleUtil.log("MainService rec:"+Hex.toString((byte[])obj));
        } else if (id == 10003)//配置更新了
        {
            if (mAOAConfigTool.isConnect()) {
                SimpleUtil.addMsgBottomToTop(getBaseContext(), getString(R.string.initab_checknewconfigs), false);
            }
            SimpleUtil.saveToShare(getBaseContext(), "ini", "configschange", true);

            //preareLoadConfigs();

            mAOAConfigTool.writeDefaultConfigs();
        } else if (id == 10004) {
            boolean ischange = (Boolean) SimpleUtil.getFromShare(getBaseContext(), "ini", "configschange", boolean.class);
            SimpleUtil.log("ischange:" + ischange);
            if (!ischange) {
                mAOAConfigTool.openOrCloseRecKeycode(false);
            } else {//需要写完配置之后自己关闭
                mAOAConfigTool.setNeedToCloseKeySet(true);
            }

        } else if (id == 10005)//按键btn返回
        {
            SimpleUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    if (mKeyboardView != null) {
                        mKeyboardView.setBtn(obj == null ? null : (KeyboardView.Btn) obj);
                    }
                }
            });
        } else if (id == 10006)//AOA断开了
        {
            SimpleUtil.log("xxxxxxxxxxxxxxxxxxxxxxxxx---AOA disconnect---xxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
            SimpleUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {

                    if (mParcelFileDescriptor != null) {
                        try {
                            //mParcelFileDescriptor.detachFd();
                            mParcelFileDescriptor.close();
                            mParcelFileDescriptor = null;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mfloatingIv.setImageResource(getAPPUserFloatIcon((Integer) mfloatingIv.getTag() == 0));
                    //mHandler.sendEmptyMessageDelayed(HANDLE_SCAN_AOA, 100);
                    SimpleUtil.addMsgBottomToTop(getBaseContext(), getString(R.string.aoa_disconnect), true);
                    //System.exit(0);
                    if (!mHasTry) {
                        mHasTry = true;
                        //SimpleUtil.log("尝试一次断开重连！");
                    }

                }
            });
        }
        else if (id == 10016)//悬浮窗关闭了
        {
            mfloatingIv.setVisibility(View.VISIBLE);
        } else if (id == 10020) {
            SimpleUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    mfloatingIv.setImageResource(getAPPUserFloatIcon((Integer) mfloatingIv.getTag() == 0));
                }
            });
        } else if (id == 10017)//模拟鼠标UI展现
        {
            byte[] data = (byte[]) obj;

            if (data.length != 9) {
                SimpleUtil.log("模拟鼠标数据长度错误:" + data.length);
                return;
            }
            if (data[3] == 0) {
                SimpleUtil.log("虚拟鼠标隐藏！");
                MouseUtils.removeMouse(MainService.this);
                return;
            }
            MouseUtils.addMouse(MainService.this);
            short x = Hex.toShortL(Arrays.copyOfRange(data, 4, 6));
            short y = Hex.toShortL(Arrays.copyOfRange(data, 6, 8));
            SimpleUtil.log("模拟鼠标数据:" + x + "," + y);
            x = (short) (SimpleUtil.zoomy * x / 4095);
            y = (short) (SimpleUtil.zoomx * y / 4095);
            SimpleUtil.log("模拟鼠标数据(转换):" + x + "," + y);
            MouseUtils.move(SimpleUtil.zoomx, SimpleUtil.zoomy, x, y);
        }

    }

    /**
     * @tips :自己写的悬浮窗监听器
     */
    private class FloatingListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View arg0, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //mfloatingIv.setTag(1);mfloatingIv.setImageDrawable(getDrawable((mAOAConfigTool == null || !mAOAConfigTool.isConnect()) ? R.mipmap.app_icon0805001_gray : R.mipmap.app_icon0805001));
                    mfloatingIv.setImageResource(getAPPUserFloatIcon(false));
                    isMove = false;
                    mTouchStartX = (int) event.getRawX();
                    mTouchStartY = (int) event.getRawY();
                    mStartX = (int) event.getX();
                    mStartY = (int) event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    mTouchCurrentX = (int) event.getRawX();
                    mTouchCurrentY = (int) event.getRawY();
                    wmParams.x += mTouchCurrentX - mTouchStartX;
                    wmParams.y += mTouchCurrentY - mTouchStartY;
                    mWindowManager.updateViewLayout(mlayout, wmParams);
                    mTouchStartX = mTouchCurrentX;
                    mTouchStartY = mTouchCurrentY;
                    break;
                case MotionEvent.ACTION_UP:
                    mStopX = (int) event.getX();
                    mStopY = (int) event.getY();
                    if (Math.abs(mStartX - mStopX) >= 20 || Math.abs(mStartY - mStopY) >= 20) {
                        isMove = true;
                    }
                    break;
            }
            // 重置移动到左侧延时
            resetMoveToLeftSideDelay();
            return mGestureDetector.onTouchEvent(event);  //此处必须返回false，否则OnClickListener获取不到监听
        }
    }

    /**
     * @tips :自己定义的手势监听类
     */
    class MyOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //点击悬浮窗未拖动，则打开按键编辑页面(增加：加载过程中不允许点击)

            if (mWindowManager.getDefaultDisplay().getRotation() * Surface.ROTATION_90 == 0) {
                SimpleUtil.addMsgBottomToTop(getBaseContext(), getString(R.string.ms_entergamefirst), true);
                return true;
            }
            if (!isMove && BtnParamTool.getPressFloatable()) {
                mfloatingIv.setVisibility(View.GONE);
                KeyboardEditWindowManager.getInstance().init(getApplicationContext());
                initViews();
                //GuiStep.getInstance().show(false, true);
                // 键位菜单界面不显示小图标
                KeyboardFloatView.getInstance(getBaseContext()).dismiss();

                if (mAOAConfigTool != null)//打开接收按键数据
                {
                    mAOAConfigTool.openOrCloseRecKeycode(true);
                }

            }

            return super.onSingleTapConfirmed(e);
        }
    }

    private void checkConfigChange() {

        if (mAOAConfigTool == null || !mAOAConfigTool.isConnect()) {
            return;
        }
        SimpleUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                boolean ischange = (Boolean) SimpleUtil.getFromShare(getBaseContext(), "ini", "configschange", boolean.class);
                if (ischange) {
                    SimpleUtil.notifyall_(10003, null);
                }
            }
        }, 500);

    }

    private void initViews() {

        //增加菜单View
        mKeyboardView = new KeyboardView(getBaseContext());
        KeyboardEditWindowManager.getInstance().addView(mKeyboardView);
        //preareLoadConfigs();
    }


    //begin
    private static final String TAG = "KeyboardEditActivity";
    protected boolean savechanged;
    private KeyboardView mKeyboardView;
    private boolean mIsBound = false;

    public class HomeWatcherReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                SimpleUtil.log("reason: " + reason);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    SimpleUtil.log("homekey");
                    //stopService(new Intent(FloatingService.this, FloatingService.class));
                  /*  mWindowManager.removeViewImmediate(mlayout);
                    KeyboardEditWindowManager.getInstance().close();
                    KeyboardFloatView.getInstance(getBaseContext()).dismiss();*/
                    //MouseUtils.removeMouse(SimpleUtil.home);

                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                    SimpleUtil.log("long press home key or activity switch");

                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                    SimpleUtil.log("lock");
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                    SimpleUtil.log("assist");
                }

            }
        }
    }


    private void setUpAsForeground(String text) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.P) {
            return;
        }
        try {

            Notification notification = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                notification = new Notification.Builder(this)
                        .setContentTitle(CommonUtils.getAppName(this))
                        .setContentText(text)
                        .setTicker(text)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_folat_online))
                        .build();
            }
            startForeground(1, notification);
            SimpleUtil.log("前台服务设置成功");
        } catch (Exception e) {
            e.printStackTrace();
            SimpleUtil.log("前台服务设置失败");
        }
    }


}
