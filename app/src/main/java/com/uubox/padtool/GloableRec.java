package com.uubox.padtool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;

import com.uubox.tools.SimpleUtil;

import static com.uubox.padtool.MainService.ACCUSBPERMISSION;
import static com.uubox.padtool.MainService.USBPERMISSION;

public class GloableRec extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        SimpleUtil.log("action:" + action + " hash:" + hashCode());
        if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
            SimpleUtil.log("Device attached!");
        } else if (action.equals("android.hardware.usb.action.USB_DEVICE_DETACHED")) {
            SimpleUtil.log("USB detached!");
        } else if (action.equals(USBPERMISSION)) {
               /* UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean isHasPermission = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED,false);
                if(isHasPermission) {
                    SimpleUtil.log(usbDevice.getVendorId() + "is permission ok!");

                    int vendorID = usbDevice.getVendorId();
                    int prodecuID = usbDevice.getProductId();

                    SimpleUtil.log("vendorID:"+vendorID+"   prodecuID:"+prodecuID+"  name:"+usbDevice.getDeviceName());
                    if(vendorID==8746)
                    {
                        if(prodecuID==1)
                        {
                            openUSBDevice(usbDevice);
                        }
                    }

                }
                else{
                    SimpleUtil.log(usbDevice.getVendorId() + "is permission fail");
                }*/
        } else if (action.equals(UsbManager.ACTION_USB_ACCESSORY_DETACHED)) {
            SimpleUtil.log("ACTION_USB_ACCESSORY_DETACHED");
            UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (accessory != null) {
                SimpleUtil.notifyall_(10006, null);
            }
        } else if (action.equals(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)) {
            SimpleUtil.log("ACTION_USB_ACCESSORY_ATTACHED");
        } else if (action.equals(ACCUSBPERMISSION)) {
            SimpleUtil.log("usb accessonry permission rec!");

            UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                SimpleUtil.log("usb accessonry permission ok!");

                SimpleUtil.notifyall_(10001, accessory);
            } else {
                SimpleUtil.log("usb accessonry permission fail!");
                SimpleUtil.notifyall_(10001, null);
            }
        } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            SimpleUtil.log("屏幕关闭");
        } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
            SimpleUtil.log("屏幕开启");
        } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
            SimpleUtil.log("屏幕解锁");
        }
           /* intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            // 屏幕亮屏广播
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            // 屏幕解锁广播
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);*/

    }
}
