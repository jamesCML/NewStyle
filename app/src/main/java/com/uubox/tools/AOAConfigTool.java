package com.uubox.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.uubox.threads.AccInputThread;
import com.uubox.views.BtnParams;
import com.uubox.views.BtnParamsHolder;
import com.uubox.views.KeyboardView;

public class AOAConfigTool implements SimpleUtil.INormalBack {
    private final int OAODEVICE_X = 4095;
    private final int OAODEVICE_Y = 2304;
    private AccInputThread mAccInputThread;
    private Context mContext;
    private static AOAConfigTool mInstance;
    private static Object lock = new Object();
    private boolean isNeedToCloseKeySet;
    private String[] mAOAInfos;
    private AOAConfigTool(Context context) {

        mContext = context;
        mAOAInfos = new String[3];
        SimpleUtil.log("构造AOADataPack");
        SimpleUtil.addINormalCallback(this);
        init();
    }

    public static AOAConfigTool getInstance(Context context) {
        synchronized (lock) {
            if (mInstance == null) {
                return mInstance = new AOAConfigTool(context);
            }
            return mInstance;
        }
    }

    public void setAOAInfo(String manufacturer, String model, String serial) {
        mAOAInfos[0] = manufacturer;
        mAOAInfos[1] = model;
        mAOAInfos[2] = serial;
    }

    public String[] getAOAInfo() {
        return mAOAInfos;
    }
    public void setNeedToCloseKeySet(boolean needToCloseKeySet) {
        isNeedToCloseKeySet = needToCloseKeySet;
    }

    public byte[] getDeviceConfigD0() {
        if (!isAOAConnect()) {
            SimpleUtil.log("aoa not connrct!pullDeviceConfigs fail!");
            return null;
        }
        ByteArrayList data = new ByteArrayList();
        data.add((byte) 0xa5);
        data.add((byte) 0x05);
        data.add((byte) 0xd0);
        data.add(SimpleUtil.sumCheck(data.all2Bytes()));
        writeWaitResult((byte) 0xd0, data.all2Bytes(), 2000);
        if (mReq.mReqResult != null) {
            SimpleUtil.log("pullDeviceConfigs ok:" + Hex.toString(mReq.mReqResult));
            return Arrays.copyOf(mReq.mReqResult, mReq.mReqResult.length);
        }
        return null;
    }

    public void startConnect(AccInputThread accInputThread) {
        mAccInputThread = accInputThread;
    }

    public List<Config> loadConfigs() {
        SimpleUtil.log("<------------读取所有配置----------->");
        List<Config> allConfigs = new ArrayList<>();
        byte defaultConfig = -1;

        String gloabkeyconfig = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
        String[] sp0 = gloabkeyconfig.split("#Z%W#", -1);
        SimpleUtil.log("当前使用:" + sp0[1]);

        // LinkedHashMap<String, ArrayList<LinkedHashMap<KeyboardView.Btn, BtnParams>>> mapData = new LinkedHashMap<>();
        SharedPreferences allKeysConfigsTable = mContext.getSharedPreferences("KeysConfigs", 0);
        Map<String, ?> maps = allKeysConfigsTable.getAll();
        Iterator<String> allIt = maps.keySet().iterator();
        byte index = 0;
        while (allIt.hasNext()) {
            String key = allIt.next();
            SimpleUtil.log("游戏:" + key);


            SharedPreferences allSubConfigs = mContext.getSharedPreferences(key + "_table", 0);
            Iterator<? extends Map.Entry<String, ?>> it = allSubConfigs.getAll().entrySet().iterator();

            while (it.hasNext()) {
                Config config = new Config();
                config.mBelongGame = key;
                Map.Entry<String, ?> obj2 = it.next();
                String subKey = obj2.getKey();
                config.setmTabKey(subKey);
                if (subKey.contains("default")) {
                    continue;
                }
                String subValue = (String) obj2.getValue();

                // byte[] value_int_s = SimpleUtil.getAES().decrypt(subValue.getBytes());
                // subValue = new String(value_int_s);

                //SimpleUtil.log("游戏 "+subKey+":"+subValue);
                String[] sp = subValue.split("#Z%W#", -1);
                config.mTabValue = sp[2];
                config.setDeleted((Boolean) SimpleUtil.getFromShare(mContext, sp[2], "isDelete", boolean.class, false));
                SimpleUtil.log("配置 " + sp[1] + "         +++++++++++++++++++++++++ " + index);
                config.mConfigName = sp[1];
                if (sp0[1].equals(sp[1])) {
                    defaultConfig = index;
                    config.mIsUsed = true;
                }

                index++;
                int configID_ = (Integer) SimpleUtil.getFromShare(mContext, sp[2], "configID", int.class);
                config.setmConfigid((byte) configID_);
                if (config.mIsUsed) {
                    SimpleUtil.log("默认配置放到第一位");
                    allConfigs.add(0, config);
                } else {
                    allConfigs.add(config);
                }
            }

           /* if(index==2)
            {
                break;
            }*/
        }
        SimpleUtil.log("加载配置文件完成:" + allConfigs.size());
        return allConfigs;

    }

    private void loadXmlToConfigData(@NonNull Config config) {
        if (config == null) {
            SimpleUtil.log("loadXmlToConfigData fail!config is null!");
            return;
        }
        LinkedHashMap<KeyboardView.Btn, BtnParams> xmlConfig = BtnParamTool.getButtonParamsFromXML(mContext, config.mTabValue);
        SimpleUtil.log("加载xml数据到configbuff:" + config.getmBelongGame() + "/" + config.getmConfigName());
        ByteArrayList cj_cfg_t = new ByteArrayList();
        cj_cfg_t.add((byte) 0x35);//show mouse board key,暂时鼠标中建，可能键码不对
        cj_cfg_t.add((byte) 0xf2);//show mouse mouse key

        cj_cfg_t.add((byte) 0xe2);//alter
        cj_cfg_t.add((byte) 0x10);//map
        cj_cfg_t.add((byte) 0x2b);//tab
        cj_cfg_t.add((byte) 0xe1);//left shift
        cj_cfg_t.add((byte) 0x1b);//get off key
        cj_cfg_t.add((byte) 0xe0);//fn key
        cj_cfg_t.add((byte) 0x1b);//switch
        cj_cfg_t.add((byte) 0xe1);//keyboard key left shift 11

        BtnParams wasd = xmlConfig.get(KeyboardView.Btn.L);
        cj_cfg_t.add(Hex.fromShortB((short) (wasd.getR() * 2)));
        cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(wasd.getEy()))));
        cj_cfg_t.add(Hex.fromShortB((short) turnX(wasd.getEx())));


        BtnParams mouse = xmlConfig.get(KeyboardView.Btn.R);
        int mouse_step = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesen", int.class, 10);
        cj_cfg_t.add((byte) mouse_step);
        cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(mouse.getEy()))));
        cj_cfg_t.add(Hex.fromShortB((short) turnX(mouse.getEx())));


        BtnParams alterLeft = xmlConfig.get(KeyboardView.Btn.ALT_LEFT);
        cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(alterLeft.getEy()))));
        cj_cfg_t.add(Hex.fromShortB((short) turnX(alterLeft.getEx())));


        int gunlun_step = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mousesrcollsen", int.class, 20);
        cj_cfg_t.add((byte) gunlun_step);//wheel radio
        BtnParams gunlun = xmlConfig.get(KeyboardView.Btn.KEY_H);
        cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(gunlun.getEy()))));
        cj_cfg_t.add(Hex.fromShortB((short) turnX(gunlun.getEx())));

        //开始索引：33
        byte[] tempContainer = new byte[18];


        //添加游戏ID
        int configID_ = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "configID", int.class);
        //压枪灵敏度

        int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "cfqNum", int.class, 13);
        int bqNum = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "bqNum", int.class, 16);
        int akNum = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "akNum", int.class, 19);
        tempContainer[0] = (byte) bqNum;
        tempContainer[1] = (byte) cfqNum;
        tempContainer[2] = (byte) akNum;
        tempContainer[3] = (byte) configID_;

        SimpleUtil.log("configID:" + config.mTabValue + "   " + tempContainer[3]);

        int defaultgun = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "defaultgun", int.class, 0);
        tempContainer[4] = (byte) defaultgun;
        cj_cfg_t.add(tempContainer);

        ByteArrayList keyPoints = new ByteArrayList();
        //遍历每一个配置的具体按键

        Iterator<KeyboardView.Btn> it2 = xmlConfig.keySet().iterator();
        while (it2.hasNext()) {
            KeyboardView.Btn key2 = it2.next();
            BtnParams btnParams = xmlConfig.get(key2);

            if (key2 == KeyboardView.Btn.L || key2 == KeyboardView.Btn.R) {
                continue;
            } else if (btnParams.getX() > 0 && btnParams.getY() > 0 && mBtMap.get(key2) != null) {
                SimpleUtil.log(config.mConfigName + ":" + btnParams.toString());
                        /*if(btnParams.img!=null) {
                            int[] position = new int[2];
                            btnParams.img.getLocationOnScreen(position);
                            SimpleUtil.log("实际位置:" + Arrays.toString(position));
                        }*/
                keyPoints.add(packKeyData2(mBtMap.get(key2), 0, OAODEVICE_Y - turnY(btnParams.getEy()), turnX(btnParams.getEx()), btnParams.getKeyType() == 3 ? KEYMODE.MP_KEY : KEYMODE.MP_TOUCH));

                if (btnParams.iHaveChild()) {
                    BtnParams btnParams2 = btnParams.getBtn2();
                    //SimpleUtil.log("我有子按键按键:" + btnParams2.toString());
                    if (btnParams2.getKeyType() == 1) {
                        //SimpleUtil.log("添加联动按键:" + btnParams2.toString());
                        keyPoints.add(packKeyData2(mBtMap.get(key2), 0, OAODEVICE_Y - turnY(btnParams2.getEy()), turnX(btnParams2.getEx()), btnParams.getKeyType() == 3 ? KEYMODE.MP_KEY : KEYMODE.MP_TOUCH));
                    }

                }
            }

        }

        int oneConfigLen = cj_cfg_t.all2Bytes().length + keyPoints.all2Bytes().length + 2;
        cj_cfg_t.add_(0, (byte) oneConfigLen);
        cj_cfg_t.add_(0, SimpleUtil.sumCheck(cj_cfg_t.add(keyPoints).all2Bytes()));
        config.mData = cj_cfg_t;
        config.mSize = config.mData.all2Bytes().length;
    }

    private Req mReq = new Req();

    public byte[] writeWaitResult(byte type, byte[] data, long timeout) {
        long time = System.currentTimeMillis();
        resetReq();
        mReq.mReqType = type;
        mAccInputThread.writeAcc(data);
        while ((System.currentTimeMillis() - time) < timeout && mReq.mReqResult == null) ;
        return mReq.mReqResult;
    }

    public void setReq(byte reqType, byte[] result) {
        resetReq();
        mReq.mReqType = reqType;
        mReq.mReqResult = result;
    }
    public void writeManyConfigs(final List<Config> allConfigs) {
        if (!isAOAConnect()) {
            SimpleUtil.log("aoa not connrct!writeManyConfigs fail!");
            SimpleUtil.addMsgBottomToTop(mContext, "写入配置失败！", true);
            return;
        }
        SimpleUtil.log("准备开始写配置，其中分辨率:" + SimpleUtil.zoomx + "," + SimpleUtil.zoomy);
        SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleUtil.addWaitToTop(mContext, "");
                SimpleUtil.sleep(20);

                new BtnParamsHolder(mContext).preareLoadConfigs(allConfigs, new BtnParamsHolder.IMeasureResult() {
                    @Override
                    public void measurefinish() {

                        for (AOAConfigTool.Config config : allConfigs) {
                            loadXmlToConfigData(config);
                            if (config.getIsUsed()) {
                                //压枪数据重新构造一下
                                int bqNum = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "bqNum", int.class, 16);
                                int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "cfqNum", int.class, 13);
                                int akNum = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "akNum", int.class, 19);
                                int defaultgun = (Integer) SimpleUtil.getFromShare(mContext, config.mTabValue, "defaultgun", int.class, 0);

                                SimpleUtil.log("重新调整一下压枪灵敏度、压枪：" + bqNum + "," + cfqNum + "," + akNum + "," + defaultgun);
                                byte[] data = config.getmData().all2Bytes();
                                data[32] = (byte) cfqNum;
                                data[33] = (byte) bqNum;
                                data[34] = (byte) akNum;
                                //[35]正在使用的gameid
                                data[36] = (byte) defaultgun;//压枪设置，如开启、使用哪一把枪等
                                byte[] data2 = Arrays.copyOfRange(data, 1, data.length);
                                ByteArrayList bytes = new ByteArrayList();
                                bytes.add(SimpleUtil.sumCheck(data2));
                                bytes.add(data2);
                                config.setmData(bytes);
                                //break;
                            }
                        }


                        short totlen = 0;
                        int defaultIndex = 0;
                        byte[] gameList = new byte[4];
                        //构建C0
                        SimpleUtil.log("构造C0:" + allConfigs.size());
                        for (int i = 0; i < allConfigs.size(); i++) {
                            totlen += allConfigs.get(i).getmSize();
                            if (allConfigs.get(i).getIsUsed()) {
                                defaultIndex = i + 1;
                            }
                            gameList[i] = allConfigs.get(i).getmConfigid();
                        }

                        ByteArrayList c0Data = new ByteArrayList();
                        c0Data.add((byte) 0xa5);
                        c0Data.add((byte) 0x14);
                        c0Data.add((byte) 0xc0);
                        c0Data.add((byte) defaultIndex);
                        c0Data.add(gameList);
                        c0Data.add(Hex.fromShortB(totlen));
                        byte[] leave = new byte[9];
                        leave[0] = (byte) Build.VERSION.SDK_INT;//发送安卓版本信息
                        c0Data.add(leave);

                        c0Data.add(SimpleUtil.sumCheck(c0Data.all2Bytes()));

                        resetReq();
                        mReq.mReqType = (byte) 0xc0;
                        mAccInputThread.writeAcc(c0Data.all2Bytes());
                        long time = System.currentTimeMillis();
                        while ((System.currentTimeMillis() - time) < 3000 && mReq.mReqResult == null)
                            ;
                        if (mReq.mReqResult != null) {
                            if (mReq.mReqResult[3] != 0) {
                                SimpleUtil.log("发送C0失败！！！");
                                return;
                            }
                        }
                        SimpleUtil.log("发送C0成功！！！");

                        byte index_ = (byte) 0xc1;
                        for (int i = 0; i < allConfigs.size(); i++) {
                            resetReq();
                            SimpleUtil.log("正在载入 " + allConfigs.get(i).getmBelongGame() + ":" + allConfigs.get(i).getmConfigName());
                            SimpleUtil.updateWaitTopMsg("正在载入\n  " + allConfigs.get(i).getmBelongGame() + "\n    " + allConfigs.get(i).getmConfigName());
                            if (!diveSend(allConfigs.get(i).mData.all2Bytes(), index_)) {
                                SimpleUtil.log("请求错误！" + Hex.toString(mReq.mReqResult) + "," + false);
                                SimpleUtil.resetWaitTop();
                                SimpleUtil.addMsgBottomToTop(mContext, "配置写入失败！", true);
                                return;
                            }

                            index_++;
                            resetReq();
                        }
                        SimpleUtil.log("保底配置顺序，查询一下！！！");
                        //保底配置顺序，查询一下
                        byte[] d0data = getDeviceConfigD0();
                        SimpleUtil.resetWaitTop();
                        if (d0data != null) {
                            SimpleUtil.addMsgBottomToTop(mContext, "配置写入成功！", false);
                            SimpleUtil.saveToShare(mContext, "ini", "configschange", false);
                            SimpleUtil.saveToShare(mContext, "ini", "configsorderbytes", Hex.toString(d0data));
                            SimpleUtil.saveToShare(mContext, "ini", "NewConfigNotWrite", "");
                            if ((Boolean) SimpleUtil.getFromShare(mContext, "ini", "aoaparamschange", boolean.class)) {
                                sendAOAChangeInfo("AAABBCC" + (uuu++), "LALALA", "0000000012345678");
                            }
                            SimpleUtil.notifyall_(10015, d0data);
                        } else {
                            SimpleUtil.addMsgBottomToTop(mContext, "配置写入失败！", true);
                        }
                        SimpleUtil.notifyall_(10012, d0data);
                        if (isNeedToCloseKeySet) {
                            openOrCloseRecKeycode(false);
                        }
                    }
                });

            }
        });

    }

    static int uuu = 1;
    private void sendAOAChangeInfo(String accessory_manufacturer, String accessory_model, String accessory_serial) {
        if (accessory_manufacturer == null || accessory_model == null || accessory_serial == null || accessory_manufacturer.length() > 8 || accessory_model.length() > 8 || accessory_serial.length() > 16
                || accessory_manufacturer.isEmpty() || accessory_model.isEmpty() || accessory_serial.isEmpty()) {
            SimpleUtil.addMsgBottomToTop(mContext, "AOA参数变更失败，字符串长度过长！", true);
            return;
        }

        byte[] accessory_manufacturer_bytes = new byte[9];
        byte[] accessory_model_bytes = new byte[9];
        byte[] accessory_serial_bytes = new byte[17];
        System.arraycopy(accessory_manufacturer.getBytes(), 0, accessory_manufacturer_bytes, 0, accessory_manufacturer.length());
        System.arraycopy(accessory_model.getBytes(), 0, accessory_model_bytes, 0, accessory_model.length());
        System.arraycopy(accessory_serial.getBytes(), 0, accessory_serial_bytes, 0, accessory_serial.length());

        ByteArrayList buff = new ByteArrayList();
        buff.add((byte) 0xa5);
        buff.add((byte) 0x44);
        buff.add((byte) 0xe1);
        buff.add(accessory_manufacturer_bytes);
        buff.add(accessory_model_bytes);
        buff.add(accessory_serial_bytes);
        buff.add(new byte[29]);
        buff.add(SimpleUtil.sumCheck(buff.all2Bytes()));
        SimpleUtil.log("AOA参数变更:" + Hex.toString(buff.all2Bytes()));
        writeWaitResult((byte) 0xe1, buff.all2Bytes(), 2000);
        SimpleUtil.log("更新AOA参数:" + Hex.toString(mReq.mReqResult));
        if (mReq.mReqResult != null && mReq.mReqResult.length > 3 && mReq.mReqResult[3] == 0) {
            SimpleUtil.addMsgBottomToTop(mContext, "AOA参数变更" + (mReq.mReqResult[3] == 0 ? "成功" : "失败"), mReq.mReqResult[3] != 0);
            if (mReq.mReqResult[3] == 0) {
                SimpleUtil.saveToShare(mContext, "ini", "aoaparamschange", false);
            }
        }
    }
    public void writeDefaultConfigs() {
        List<Config> configsRightData = new ArrayList<>();
        List<Config> configsLeftData = new ArrayList<>();
        AnysLeftRihgtConfigs(configsLeftData, configsRightData);

        //特殊处理新建的配置
        String newConfig = (String) SimpleUtil.getFromShare(mContext, "ini", "NewConfigNotWrite", String.class, "");

        if (!newConfig.isEmpty()) {
            SimpleUtil.log("we find the newConfig in the lib:" + newConfig);
            boolean isFind = false;
            for (Config config : configsLeftData) {
                if (config.getmConfigName().equals(newConfig)) {
                    for (int i = 0; i < configsRightData.size(); i++) {
                        if (configsRightData.get(i).getIsUsed()) {
                            config.setmIsUsed(true);
                            configsRightData.set(i, config);
                            isFind = true;
                            break;
                        }
                    }
                }

                if (isFind) {
                    break;
                }
            }
        }
        writeManyConfigs(configsRightData);
    }

    public boolean AnysLeftRihgtConfigs(List<Config> configsLeftData, List<Config> configsRightData) {
        final List<AOAConfigTool.Config> mConfigs = loadConfigs();
        byte[] d0 = getDeviceConfigD0();
        //byte[] d0 = Hex.parse("A5 14 D0 03 03 02 09 01 02 92 00 00 00 00 00 00 00 00 00 2F");
        String configsorderbytes = (String) SimpleUtil.getFromShare(mContext, "ini", "configsorderbytes", String.class, null);
        byte[] d1 = Hex.parse(configsorderbytes);
        SimpleUtil.log("get d0:\n" + Hex.toString(d0) + "\nget d1:\n" + configsorderbytes);
        boolean isOrderEqual = false;
        if (d0 != null && d1 != null) {
            isOrderEqual = Arrays.equals(Arrays.copyOfRange(d0, 4, 8), Arrays.copyOfRange(d1, 4, 8));
        }
        if (isOrderEqual) {
            if (d1[3] != d0[3]) {
                SimpleUtil.log("检测到配置切换:" + d1[3] + "-->" + d0[3]);
                d1[3] = d0[3];
                SimpleUtil.saveToShare(mContext, "ini", "configsorderbytes", Hex.toString(d1));
            }
        }

        //final byte[] d0 = Hex.parse("A5 14 D0 01 04 07 05 09 02 E2 00 00 00 00 00 00 00 00 00 87");
        if (d0 == null) {//为了排序，只能暂时获取来自存储的排序
            SimpleUtil.addMsgBottomToTop(mContext, "读取设备配置信息失败！", true);
            SimpleUtil.log("get lib d0:" + configsorderbytes);
            d0 = Hex.parse(configsorderbytes);
        }

        AOAConfigTool.Config[] rightOrder = new AOAConfigTool.Config[4];
        SimpleUtil.log("准备分配配置到左右，其中d0:" + Hex.toString(d0));
        for (AOAConfigTool.Config config : mConfigs) {
            if (d0 != null) {
                boolean isFind = false;
                config.setmIsUsed(false);
                for (int order = 0; order < 4; order++) {
                    if (config.getmConfigid() == Arrays.copyOfRange(d0, 4, 8)[order]) {
                        config.setDeleted(false);
                        rightOrder[order] = config;
                        isFind = true;
                        break;
                    }
                }
                if (!isFind) {
                    config.setDeleted(true);
                    if (configsLeftData != null) {
                        if (config.getmConfigName().endsWith("[官方]"))
                            configsLeftData.add(0, config);
                        else
                            configsLeftData.add(config);
                    }
                }
                continue;
            } else {
                if (!config.getIsDeleted() && configsRightData.size() < 4) {
                    config.setDeleted(false);
                    configsRightData.add(config);
                    SimpleUtil.log("增加了一个到右边:" + config.getmConfigName());
                    //rightSize[0] += config.getmSize();
                } else {
                    config.setmIsUsed(false);
                    config.setDeleted(true);
                    if (configsLeftData != null) {
                        SimpleUtil.log("增加了一个到左边:" + config.getmConfigName());
                        if (config.getmConfigName().endsWith("[官方]"))
                            configsLeftData.add(0, config);
                        else
                            configsLeftData.add(config);
                    }
                }
            }

        }

        if (d0 != null && d0[3] - 1 >= 0 && d0[3] - 1 < 4 && rightOrder[d0[3] - 1] != null) {//匹配不上
            rightOrder[d0[3] - 1].setmIsUsed(true);
            for (AOAConfigTool.Config one : rightOrder) {
                if (one != null)
                    configsRightData.add(one);
            }
        } else {//只能先放一点了
            SimpleUtil.log("匹配不上，则放一点");
            for (AOAConfigTool.Config one : rightOrder) {
                if (one != null) {
                    SimpleUtil.log("符合4个的非空的先放在右边:" + one.getmConfigName());
                    configsRightData.add(one);
                }
            }


            Iterator<Config> it = configsLeftData.iterator();
            while (it.hasNext()) {
                Config leftOne = it.next();
                if (configsRightData.size() != 4) {
                    if (leftOne.mConfigName.contains("刺激战场") || leftOne.mConfigName.contains("全军出击") || leftOne.mConfigName.contains("荒野行动") || leftOne.mConfigName.contains("光荣使命")) {
                        leftOne.setDeleted(false);
                        configsRightData.add(leftOne);
                        SimpleUtil.log("把左边的一个增加到右边:" + leftOne.getmConfigName());
                        it.remove();
                    }
                }
            }

            configsRightData.get(0).setmIsUsed(true);

        }
        //SimpleUtil.log("device order use is null:"+(rightOrder[d0[3] - 1]==null));
        return d0 != null && d1 != null && d0[3] - 1 >= 0 && isOrderEqual && rightOrder[d0[3] - 1] != null;
    }

    public boolean openOrCloseRecKeycode(boolean open) {
        if (!isAOAConnect()) {
            SimpleUtil.log("aoa not connrct!openOrCloseRecKeycode fail!");
            return false;
        }
        SimpleUtil.log((open ? "打开" : "关闭") + "接收按键");
        mReq.mReqType = (byte) 0xb2;
        mReq.mReqResult = null;
        return open ? mAccInputThread.writeAcc(new byte[]{(byte) 0xa5, (byte) 0x05, (byte) 0xb2, (byte) 0x01, (byte) 0x5d}) :
                mAccInputThread.writeAcc(new byte[]{(byte) 0xa5, (byte) 0x05, (byte) 0xb2, (byte) 0x00, (byte) 0x5c});
    }

    public void removeDataRec() {
        SimpleUtil.removeINormalCallback(this);
    }

    private boolean writeOneConfig(byte index, Config allConfig) {

        ByteArrayList onebyone = new ByteArrayList();
        onebyone.add(allConfig.mData);
        return diveSend(onebyone.all2Bytes(), index);
    }

    public boolean readConfig(byte readIndex) {
        ByteArrayList bytes = new ByteArrayList();
        bytes.add((byte) 0xa5);
        bytes.add((byte) 0x05);
        bytes.add(readIndex);
        bytes.add((byte) 0x00);
        bytes.add(SimpleUtil.sumCheck(bytes.all2Bytes()));
        return mAccInputThread.writeAcc(bytes.all2Bytes());
    }

    private int turnX(int x) {
        return (OAODEVICE_X * x) / SimpleUtil.zoomy;
    }

    private int turnY(int y) {
        return (OAODEVICE_Y * y) / SimpleUtil.zoomx;
    }

    private static byte[] packKeyData2(int keycode_, int slot_, int x_, int y_, KEYMODE mode_) {
        ByteArrayList result = new ByteArrayList();
        result.add((byte) keycode_);

        int a = x_ << 4 | slot_;
        int b = y_ | (mode_.ordinal() << 12);
        byte[] a_ = Hex.fromIntB(a);
        byte[] b_ = Hex.fromIntB(b);
        result.add(a_[2]);
        result.add(a_[3]);
        result.add(b_[2]);
        result.add(b_[3]);
        return result.all2Bytes();
    }

    private static byte[] packKeyData(int keycode_, int slot_, int x_, int y_, int mode_) {
        ByteArrayList result = new ByteArrayList();
        result.add((byte) keycode_);
        int container = 0x00000000;
        container = container | x_ << 20;
        container = container | slot_ << 16;
        container = container | mode_ << 12;
        container = container | y_;

        result.add(Hex.fromIntB(container));
        return result.all2Bytes();
    }

    private boolean diveSend(byte[] totalBuffer, byte type) {

        SimpleUtil.log("开始分包写入配置:\n" + Hex.toString(totalBuffer) + ",len:" + totalBuffer.length);
        int count = totalBuffer.length / 59;
        int offset = 0;
        while (offset < totalBuffer.length) {
            resetReq();
            ByteArrayList configSend = new ByteArrayList();
            configSend.add((byte) 0xa5);
            configSend.add((byte) 0x00);
            configSend.add(type);
            configSend.add((byte) offset);

            if (count == 0) {
                configSend.add(totalBuffer);
            } else if (offset + 58 < totalBuffer.length) {
                configSend.add(Arrays.copyOfRange(totalBuffer, offset, offset += 58));
            } else {
                configSend.add(Arrays.copyOfRange(totalBuffer, offset, totalBuffer.length));
                offset = totalBuffer.length;
            }

            byte checksum = SimpleUtil.sumCheck(configSend.all2Bytes());
            configSend.add(checksum);
            configSend.set(1, new byte[]{(byte) configSend.all2Bytes().length});


            long time = System.currentTimeMillis();
            mReq.mReqType = type;
            boolean result = mAccInputThread.writeAcc(configSend.all2Bytes());
            while ((System.currentTimeMillis() - time) < 5000 && mReq.mReqResult == null) ;
            if (mReq.mReqResult == null || !result || mReq.mReqResult[3] != 0x00) {
                return false;
            }

        }
        return true;

    }

    @Override
    public void back(int id, Object obj) {
        if (id == 10002)//AOA数据接口
        {
            byte[] data = (byte[]) obj;
            SimpleUtil.log("AOADataPack rec:" + Hex.toString(data));
            if (data.length < 4) {
                SimpleUtil.log("bad data:" + Hex.toString(data));
                return;
            }

            if (data[0] == (byte) 0xa5) {
                if (mReq.mReqType != 0)//有请求待返回
                {
                    SimpleUtil.log("有请求码");
                    if (mReq.mReqType == data[2])//符合请求码，则填入OK数据
                    {
                        SimpleUtil.log("符合请求码，返回数据");
                        mReq.mReqResult = data;
                    }
                } else {
                    SimpleUtil.log("没有请求码:" + Hex.toString(data));
                    if (data[2] == (byte) 0xd0)//检测到配置切换
                    {
                        String configsorderbytes = (String) SimpleUtil.getFromShare(mContext, "ini", "configsorderbytes", String.class, null);
                        byte[] libOrder = Hex.parse(configsorderbytes);
                        if (libOrder == null) {
                            return;
                        }
                        SimpleUtil.log("liborder:" + configsorderbytes);
                        if (libOrder[3] != data[3]) {
                            SimpleUtil.notifyall_(10015, data);
                        }

                    }
                }
            }

            handleSpecil(data);

        }
    }

    private void handleSpecil(byte[] data) {
        if (mReq.mReqType == (byte) 0xb2)//读键盘数据请求
        {
            if (data[3] == 0x00) {
                SimpleUtil.mAOAInjectEable = true;
                //SimpleUtil.addMsgBottomToTop(mContext, "已关闭按键配置调整", true);
            } else if (data[3] == 0x01) {
                SimpleUtil.mAOAInjectEable = false;
                //SimpleUtil.addMsgBottomToTop(mContext, "已开启按键配置调整", false);
            }
            resetReq();
        } else if (!SimpleUtil.mAOAInjectEable) {
            if (data[1] == 0x08 || data[1] == 0x07) {
                KeyboardView.Btn btn = null;
               /*byte mouseCode = (byte) (0xf0|data[3]);
               byte keyboardCode = data[10];
               byte fnCode = (byte) (0xe0|data[9]);*/

                if (data[3] != 0)//先看鼠标数据
                {
                    if (data[3] == 0x01) {
                        btn = KeyboardView.Btn.MOUSE_LEFT;
                    } else if (data[3] == 0x02) {
                        btn = KeyboardView.Btn.MOUSE_RIGHT;
                    }
                    if (data[3] == 0x04) {
                        btn = KeyboardView.Btn.MOUSE_IN;
                    }
                } else if (data[9] != 0)//功能按键
                {
                    if (data[9] == 0x01) {
                        btn = KeyboardView.Btn.CTRL_LEFT;
                    } else if (data[9] == 0x10) {
                        btn = KeyboardView.Btn.CTRL_RGHT;
                    }
                    if (data[9] == 0x04) {
                        btn = KeyboardView.Btn.ALT_LEFT;
                    } else if (data[9] == 0x40) {
                        btn = KeyboardView.Btn.ALT_RIGHT;
                    }
                    if (data[9] == 0x02) {
                        btn = KeyboardView.Btn.SHIFT_LEFT;
                    } else if (data[9] == 0x20) {
                        btn = KeyboardView.Btn.SHIFT_RIGHT;
                    }

                } else if (data[10] != 0) {
                    btn = getBtnByByte(data[10]);
                }

                //SimpleUtil.log(Hex.toString(new byte[]{mouseCode,keyboardCode,fnCode}));

                SimpleUtil.notifyall_(10005, btn);

            }

        }

    }


    public boolean isAOAConnect() {
        return mAccInputThread != null && mAccInputThread.isConnect();
    }

    public void resetReq() {
        mReq.mReqType = 0x00;
        mReq.mReqResult = null;
    }

    private HashMap<KeyboardView.Btn, Byte> mBtMap = new HashMap<>();

    private void init() {
        mBtMap.put(KeyboardView.Btn.KEY_A, (byte) 0x04);
        mBtMap.put(KeyboardView.Btn.KEY_B, (byte) 0x05);
        mBtMap.put(KeyboardView.Btn.KEY_C, (byte) 0x06);
        mBtMap.put(KeyboardView.Btn.KEY_D, (byte) 0x07);
        mBtMap.put(KeyboardView.Btn.KEY_E, (byte) 0x08);
        mBtMap.put(KeyboardView.Btn.KEY_F, (byte) 0x09);
        mBtMap.put(KeyboardView.Btn.KEY_G, (byte) 0x0a);
        mBtMap.put(KeyboardView.Btn.KEY_H, (byte) 0x0b);
        mBtMap.put(KeyboardView.Btn.KEY_I, (byte) 0x0c);
        mBtMap.put(KeyboardView.Btn.KEY_J, (byte) 0x0d);
        mBtMap.put(KeyboardView.Btn.KEY_K, (byte) 0x0e);
        mBtMap.put(KeyboardView.Btn.KEY_L, (byte) 0x0f);
        mBtMap.put(KeyboardView.Btn.KEY_M, (byte) 0x10);
        mBtMap.put(KeyboardView.Btn.KEY_N, (byte) 0x11);
        mBtMap.put(KeyboardView.Btn.KEY_O, (byte) 0x12);
        mBtMap.put(KeyboardView.Btn.KEY_P, (byte) 0x13);
        mBtMap.put(KeyboardView.Btn.KEY_Q, (byte) 0x14);
        mBtMap.put(KeyboardView.Btn.KEY_R, (byte) 0x15);
        mBtMap.put(KeyboardView.Btn.KEY_S, (byte) 0x16);
        mBtMap.put(KeyboardView.Btn.KEY_T, (byte) 0x17);
        mBtMap.put(KeyboardView.Btn.KEY_U, (byte) 0x18);
        mBtMap.put(KeyboardView.Btn.KEY_V, (byte) 0x19);
        mBtMap.put(KeyboardView.Btn.KEY_W, (byte) 0x1a);
        mBtMap.put(KeyboardView.Btn.KEY_X, (byte) 0x1b);
        mBtMap.put(KeyboardView.Btn.KEY_Y, (byte) 0x1c);
        mBtMap.put(KeyboardView.Btn.KEY_Z, (byte) 0x1d);

        mBtMap.put(KeyboardView.Btn.NUM_0, (byte) 0x27);
        mBtMap.put(KeyboardView.Btn.NUM_1, (byte) 0x1e);
        mBtMap.put(KeyboardView.Btn.NUM_2, (byte) 0x1f);
        mBtMap.put(KeyboardView.Btn.NUM_3, (byte) 0x20);
        mBtMap.put(KeyboardView.Btn.NUM_4, (byte) 0x21);
        mBtMap.put(KeyboardView.Btn.NUM_5, (byte) 0x22);
        mBtMap.put(KeyboardView.Btn.NUM_6, (byte) 0x23);
        mBtMap.put(KeyboardView.Btn.NUM_7, (byte) 0x24);
        mBtMap.put(KeyboardView.Btn.NUM_8, (byte) 0x25);
        mBtMap.put(KeyboardView.Btn.NUM_9, (byte) 0x26);
        mBtMap.put(KeyboardView.Btn.F1, (byte) 0x3a);
        mBtMap.put(KeyboardView.Btn.F2, (byte) 0x3b);
        mBtMap.put(KeyboardView.Btn.F3, (byte) 0x3c);
        mBtMap.put(KeyboardView.Btn.F4, (byte) 0x3d);
        mBtMap.put(KeyboardView.Btn.F5, (byte) 0x3e);
        mBtMap.put(KeyboardView.Btn.F6, (byte) 0x3f);
        mBtMap.put(KeyboardView.Btn.F7, (byte) 0x40);
        mBtMap.put(KeyboardView.Btn.F8, (byte) 0x41);
        mBtMap.put(KeyboardView.Btn.F9, (byte) 0x42);
        mBtMap.put(KeyboardView.Btn.F10, (byte) 0x43);
        mBtMap.put(KeyboardView.Btn.F11, (byte) 0x44);
        mBtMap.put(KeyboardView.Btn.F12, (byte) 0x45);

        mBtMap.put(KeyboardView.Btn.UP, (byte) 0x52);
        mBtMap.put(KeyboardView.Btn.DOWN, (byte) 0x51);
        mBtMap.put(KeyboardView.Btn.LEFT, (byte) 0x50);
        mBtMap.put(KeyboardView.Btn.RIGHT, (byte) 0x4f);

        mBtMap.put(KeyboardView.Btn.KEY_YINHAO, (byte) 0x34);
        mBtMap.put(KeyboardView.Btn.KEY_MAOHAO, (byte) 0x33);
        mBtMap.put(KeyboardView.Btn.KEY_FANXIEGANG, (byte) 0x31);
        mBtMap.put(KeyboardView.Btn.KEY_LEFT_BEGIN, (byte) 0x2f);
        mBtMap.put(KeyboardView.Btn.KEY_RIGHT_END, (byte) 0x30);
        mBtMap.put(KeyboardView.Btn.SUB, (byte) 0x2d);
        mBtMap.put(KeyboardView.Btn.ADD, (byte) 0x2e);
        mBtMap.put(KeyboardView.Btn.ENTER, (byte) 0x28);
        mBtMap.put(KeyboardView.Btn.ESC, (byte) 0x29);
        mBtMap.put(KeyboardView.Btn.TAB, (byte) 0x2b);
        mBtMap.put(KeyboardView.Btn.SPACES, (byte) 0x2c);

        mBtMap.put(KeyboardView.Btn.CTRL_LEFT, (byte) 0xe0);
        mBtMap.put(KeyboardView.Btn.CTRL_RGHT, (byte) 0xe4);
        mBtMap.put(KeyboardView.Btn.SHIFT_LEFT, (byte) 0xe1);
        mBtMap.put(KeyboardView.Btn.SHIFT_RIGHT, (byte) 0xe5);
        mBtMap.put(KeyboardView.Btn.ALT_LEFT, (byte) 0xe2);
        mBtMap.put(KeyboardView.Btn.ALT_RIGHT, (byte) 0xe6);

        mBtMap.put(KeyboardView.Btn.MOUSE_LEFT, (byte) 0xf0);
        mBtMap.put(KeyboardView.Btn.MOUSE_RIGHT, (byte) 0xf1);
        mBtMap.put(KeyboardView.Btn.MOUSE_IN, (byte) 0xf4);

    }

    public KeyboardView.Btn getBtnByByte(byte b) {
        Iterator<KeyboardView.Btn> it = mBtMap.keySet().iterator();
        while (it.hasNext()) {
            KeyboardView.Btn btn = it.next();
            if (mBtMap.get(btn) == b) {
                return btn;
            }
        }
        return null;
    }

    enum KEYMODE                //5
    {
        MP_KEY,          //  按键功能
        MP_TOUCH,               //  触摸屏功能
        MP_TOUCH_SWAP,          //  交替触摸
        MP_TOUCH_LINK,          //  联动触摸
        MP_TOUCH_MULT,          //  多功能
    }

    public class Config implements Cloneable {
        private String mBelongGame;
        private String mConfigName;
        private ByteArrayList mData;
        private boolean mIsUsed;
        private int mSize;
        private boolean mIsDeleted;
        private String mTabValue;
        private byte mConfigid;
        private String mTabKey;

        public String getmBelongGame() {
            return mBelongGame;
        }

        public String getmConfigName() {
            return mConfigName;
        }

        public ByteArrayList getmData() {
            return mData;
        }

        public boolean getIsUsed() {
            return mIsUsed;
        }

        public void setmIsUsed(boolean flag) {
            mIsUsed = flag;
        }

        public int getmSize() {
            return mSize;
        }

        public boolean getIsDeleted() {
            return mIsDeleted;
        }

        public void setmData(ByteArrayList mData) {
            this.mData = mData;
        }

        public String getmTabValue() {
            return mTabValue;
        }

        public void setDeleted(boolean flag) {
            mIsDeleted = flag;
        }

        public byte getmConfigid() {
            return mConfigid;
        }

        public void setmConfigid(byte mConfigid) {
            this.mConfigid = mConfigid;
        }

        public String getmTabKey() {
            return mTabKey;
        }

        public void setmTabKey(String mTabKey) {
            this.mTabKey = mTabKey;
        }

        @Override
        public boolean equals(Object obj) {
            Config right = ((Config) obj);
            return mTabValue.equals(right.mTabValue);
        }

        @Override
        public Object clone() {
            Config config = null;
            try {
                config = (Config) super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return config;
        }
    }


    class Req {
        byte mReqType;//当前请求码类型
        byte[] mReqResult;//请求结果数据
    }
}
