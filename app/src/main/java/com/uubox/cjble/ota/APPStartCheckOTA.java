package com.uubox.cjble.ota;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;

import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;
import com.uubox.cjble.BTJobsManager;
import com.uubox.cjble.BTService;
import com.uubox.padtool.R;
import com.uubox.tools.ByteArrayList;
import com.uubox.tools.Hex;
import com.uubox.tools.SimpleUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class APPStartCheckOTA extends Thread implements SimpleUtil.INormalBack, BTService.IBLENotify {
    private Context mContext;
    private boolean mFlag = true;
    private int mMaxProc;
    private ICheckOTABack mICheckOTABack;
    private BaseRemoteOTA baseRemoteOTA;
    private String fwVersion;
    private OTAUpdate update;

    public APPStartCheckOTA(Context context) {
        mContext = context;
        BTJobsManager.getInstance().addBLENotify(this);
        SimpleUtil.addINormalCallback(this);
    }

    public void setICheckOTABack(ICheckOTABack iCheckOTABack) {
        mICheckOTABack = iCheckOTABack;
    }

    public void release() {
        SimpleUtil.resetWaitTop(mContext);
        BTJobsManager.getInstance().removeBLENotify(this);
        SimpleUtil.removeINormalCallback(this);
        //SimpleUtil.BleSendOnlyWriteThread_resume();
        mFlag = false;
        BTJobsManager.getInstance().setOTAUpdate(false);
    }

    @Override
    public void run() {
        SimpleUtil.addWaitToTop(mContext, mContext.getResources().getString(R.string.initab_loading_wait));
        // SimpleUtil.BleSendOnlyWriteThread_pause();
        update = new OTAUpdate(mContext, BTJobsManager.getInstance().getGatt());
        update.setIotaCallBack(new OTAUpdate.IOTACallBack() {
            @Override
            public void callback(int mode, int arg1, int arg2, Object obj) {
                if (mode == 6) {
                    if (arg1 == 2) {
                        fwVersion = (String) obj;
                        if (fwVersion.isEmpty()) {
                            mICheckOTABack.checkresult(3);
                            release();
                        }
                        SimpleUtil.log("得到设备峰位版本:" + fwVersion + "，下面获取服务器版本，请稍后...");
                        SimpleUtil.updateWaiting(mContext.getString(R.string.ble_getdevverok) + fwVersion);
                        if (update.getCurImage() != null && baseRemoteOTA == null) {
                            baseRemoteOTA = new WisegaHttpRemoteOTA(fwVersion, update.getCurImage());
                            Thread getserverfwThread = new Thread(baseRemoteOTA);
                            getserverfwThread.start();
                            //SimpleUtil.notifyall_(BaseRemoteOTA.FWVERSION_CALLBACK, "3120");//本地
                        }

                    } else if (arg1 == 4) {//峰位版本读取失败
                        mICheckOTABack.checkresult(3);
                        release();
                    } else if (arg1 == 3) {
                        SimpleUtil.updateWaiting(mContext.getString(R.string.getdevimgok) + update.getCurImage());
                        if (fwVersion != null && baseRemoteOTA == null) {
                            baseRemoteOTA = new WisegaHttpRemoteOTA(fwVersion, update.getCurImage());
                            Thread getserverfwThread = new Thread(baseRemoteOTA);
                            getserverfwThread.start();
                            //SimpleUtil.notifyall_(BaseRemoteOTA.FWVERSION_CALLBACK, "3120");//本地
                        }
                    }
                } else if (mode == 4) {
                    if (arg1 == 0)
                    {
                        //SimpleUtil.updateWaiting((Activity) mContext, "温馨提示", "升级失败！");
                    } else if (arg1 == 1) {
                        SimpleUtil.addMsgtoTopNoRes(mContext, mContext.getString(R.string.kbv_warmwarn), mContext.getString(R.string.ble_sameimg));
                        release();
                    }

                } else if (mode == 5) {
                    SimpleUtil.addMsgtoTopNoRes(mContext, mContext.getString(R.string.kbv_warmwarn), mContext.getString(R.string.ble_otafail));
                    release();
                } else if (mode == 2)//进度条初始化
                {
                    mMaxProc = arg1;
                } else if (mode == 3)//进度
                {
                    int proc = arg1;
                    float percent = (proc * 1.0f) / mMaxProc;
                    DecimalFormat df = new DecimalFormat("0.00%");
                    SimpleUtil.updateWaiting(mContext.getString(R.string.ble_updating) + "[" + df.format(percent) + "]\n" + mContext.getString(R.string.ble_otaoffdev));
                    if (proc == mMaxProc) {
                        //SimpleUtil.updateWaiting((Activity) mContext,mContext.getString(R.string.update_success));
                        SimpleUtil.addMsgtoTopNoRes(mContext, mContext.getString(R.string.kbv_warmwarn), mContext.getString(R.string.ble_otaok));
                        release();
                    }
                }
            }
        });
        BTJobsManager.getInstance().addBLENotify(update);
        update.getInfomation();
        while (mFlag) ;
        SimpleUtil.log("OTA检查结束！");
    }

    private int waitMCUCMDSending;

    private void sendMCUCMD() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<byte[]> datas = new ArrayList<>();
                datas.add(new byte[]{(byte) 0xa5, (byte) 0x04, (byte) 0x80});
                datas.add(new byte[]{(byte) 0x20, (byte) 0x04, (byte) 0x80});
                int retry = 0;
                for (int i = 0; i < datas.size(); ) {
                    byte[] data = datas.get(i);
                    waitMCUCMDSending = 0;
                    long time = System.currentTimeMillis();
                    SimpleUtil.log("send the mcu update:" + Hex.toString(data));
                    BTJobsManager.getInstance().writeDefault(SimpleUtil.getBytesWithSumCheck(data), new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess() {
                            SimpleUtil.log("send the mcu update is ok!");
                            waitMCUCMDSending = 1;
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            SimpleUtil.log("send the mcu update is fai!\n" + exception.getDescription());
                            waitMCUCMDSending = 2;
                        }
                    });
                    while (waitMCUCMDSending == 0 && (System.currentTimeMillis() - time) < 3000) ;
                    SimpleUtil.sleep(250);
                    if (waitMCUCMDSending == 2 && retry++ < 3) {
                        continue;
                    }
                    i++;
                    retry = 0;

                }

            }
        }).start();


    }


    @Override
    public void back(int id, Object obj) {
        switch (id) {
            case BaseRemoteOTA.FWVERSION_CALLBACK:
                String server_fw = (String) obj;
                SimpleUtil.log("得到服务器峰位版本:" + server_fw);
                if (!server_fw.isEmpty() && Integer.parseInt(fwVersion) < Integer.parseInt(server_fw))//设备版本小于服务器版本！//waitdo
                {
                    if (Integer.parseInt(fwVersion.substring(0, 2)) < Integer.parseInt(server_fw.substring(0, 2)))//峰位版本太小，需要下载升级
                    {
                        SimpleUtil.updateWaiting(mContext.getString(R.string.ble_otanewver) + "[" + update.getCurImage() + "]");
                        baseRemoteOTA.wantImg();
                        //本地
                        //SimpleUtil.notifyall_(BaseRemoteOTA.OKBUFF_CALLBACK, new ByteArrayList(SimpleUtil.getAssertSmallFile("P10_"+(update.getCurImage().equals("A")?"B":"A")+"_add_bd_crc_oad_V3120_20181017.bin")));
                        return;
                    } else//需要升级MCU
                    {
                        SimpleUtil.updateWaiting(mContext.getString(R.string.ble_otamcuupdate));
                        SimpleUtil.sleep(1000);
                        sendMCUCMD();
                        return;
                    }
                }
                release();
                mICheckOTABack.checkresult(1);

                break;
            case BaseRemoteOTA.OKBUFF_CALLBACK:
                SimpleUtil.log("固件buff返回");
                ByteArrayList byteArrayList = (ByteArrayList) obj;
                update.loadFromeNet(byteArrayList.all2Bytes(), baseRemoteOTA.getSerVer());
                Thread thread = new Thread(update);
                thread.start();
                break;
            case BaseRemoteOTA.CRCEER_CALLBACK:
                SimpleUtil.log("CRC校验错误");
                break;
            case BaseRemoteOTA.DOWNLOADEER_CALLBACK:
                mICheckOTABack.checkresult(4);
                break;
        }
    }

    @Override
    public void notify(BTService.BLEMODTYPE mode, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        if (mode == BTService.BLEMODTYPE.CHANGE) {
            byte[] data = characteristic.getValue();
            if (data == null || data.length < 5) {
                return;
            }
            if ((data[0] == (byte) 0xa5 || data[0] == (byte) 0x20) && data[2] == (byte) 0x80) {
                if (data[3] == 0)//升级中
                {
                    SimpleUtil.updateWaitTopMsg(mContext.getString(R.string.ble_otaoffdont));
                } else if (data[3] == 1)//升级成功
                {
                    SimpleUtil.addMsgtoTopNoRes(mContext, mContext.getString(R.string.kbv_warmwarn), mContext.getString(R.string.ota_mcuok));
                    //SimpleUtil.popWindowNoRes(mContext, SimpleUtil.getString(R.string.switchtomcu), SimpleUtil.getString(R.string.switchmcuupdateok));
                    release();

                } else if (data[3] == 2)//升级失败
                {
                    SimpleUtil.addMsgtoTopNoRes(mContext, mContext.getString(R.string.kbv_warmwarn), mContext.getString(R.string.ble_mcufail));
                    // SimpleUtil.popWindowNoRes(mContext, SimpleUtil.getString(R.string.switchtomcu), SimpleUtil.getString(R.string.switchmcuupdatefail));
                    release();

                }
            }
        }
    }

    public interface ICheckOTABack {
        //1:可以启动 0:映射过低
        void checkresult(int enter);
    }
}
