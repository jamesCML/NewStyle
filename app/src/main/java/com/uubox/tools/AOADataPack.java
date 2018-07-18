package com.uubox.tools;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.uubox.threads.AccInputThread;
import com.uubox.views.BtnParams;
import com.uubox.views.KeyboardView;

public class AOADataPack implements SimpleUtil.INormalBack {
    private final int OAODEVICE_X = 4095;
    private final int OAODEVICE_Y = 2304;
    private AccInputThread mAccInputThread;
    private Context mContext;

    public AOADataPack(Context context, AccInputThread accInputThread) {
        mAccInputThread = accInputThread;
        mContext = context;
        SimpleUtil.log("构造AOADataPack");
        SimpleUtil.addINormalCallback(this);
        init();
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
            SimpleUtil.log("目录:" + key);


            SharedPreferences allSubConfigs = mContext.getSharedPreferences(key + "_table", 0);
            Iterator<? extends Map.Entry<String, ?>> it = allSubConfigs.getAll().entrySet().iterator();

            while (it.hasNext()) {
                Config config = new Config();
                config.mContent = key;
                Map.Entry<String, ?> obj2 = it.next();
                String subKey = obj2.getKey();
                if (subKey.contains("default")) {
                    continue;
                }
                String subValue = (String) obj2.getValue();

                // byte[] value_int_s = SimpleUtil.getAES().decrypt(subValue.getBytes());
                // subValue = new String(value_int_s);

                //SimpleUtil.log("游戏 "+subKey+":"+subValue);
                String[] sp = subValue.split("#Z%W#", -1);
                SimpleUtil.log("配置 " + sp[1] + "         +++++++++++++++++++++++++ " + index);
                config.mConfigName = sp[1];
                if (sp0[1].equals(sp[1])) {
                    defaultConfig = index;
                    config.mIsUsed = true;
                }

                index++;
                long t1 = System.currentTimeMillis();
                LinkedHashMap<KeyboardView.Btn, BtnParams> xmlConfig = InjectUtil.getButtonParamsFromXML(mContext, sp[2]);
                SimpleUtil.log("getButtonParamsFromXML:" + (System.currentTimeMillis() - t1));
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
                cj_cfg_t.add(Hex.fromShortB((short) wasd.getR()));
                cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(wasd.getY()))));
                cj_cfg_t.add(Hex.fromShortB((short) turnX(wasd.getX())));


                BtnParams mouse = xmlConfig.get(KeyboardView.Btn.R);
                cj_cfg_t.add((byte) 0x01);
                cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(mouse.getY()))));
                cj_cfg_t.add(Hex.fromShortB((short) turnX(mouse.getX())));


                BtnParams alterLeft = xmlConfig.get(KeyboardView.Btn.ALT_LEFT);
                cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(alterLeft.getY()))));
                cj_cfg_t.add(Hex.fromShortB((short) turnX(alterLeft.getX())));


                int gunlun_step = (Integer) SimpleUtil.getFromShare(mContext, "ini", "mMouseProgressW", int.class, 50);
                cj_cfg_t.add((byte) gunlun_step);//wheel radio
                BtnParams gunlun = xmlConfig.get(KeyboardView.Btn.GUNLUN);
                cj_cfg_t.add(Hex.fromShortB((short) (OAODEVICE_Y - turnY(gunlun.getY()))));
                cj_cfg_t.add(Hex.fromShortB((short) turnX(gunlun.getX())));

                ByteArrayList keyPoints = new ByteArrayList();
                //遍历每一个配置的具体按键
                Iterator<KeyboardView.Btn> it2 = xmlConfig.keySet().iterator();
                while (it2.hasNext()) {
                    KeyboardView.Btn key2 = it2.next();
                    BtnParams btnParams = xmlConfig.get(key2);
                    if (key2 == KeyboardView.Btn.MOUSE_LEFT || key2 == KeyboardView.Btn.MOUSE_RIGHT || key2 == KeyboardView.Btn.MOUSE_IN) //得到鼠标按键
                    {
                        keyPoints.add(packKeyData2(mBtMap.get(key2), 0, OAODEVICE_Y - turnY(btnParams.getY()), turnX(btnParams.getX()), KEYMODE.MP_TOUCH));
                    } else if (key2 == KeyboardView.Btn.L || key2 == KeyboardView.Btn.R) {
                        continue;
                    } else if (btnParams.getX() > 0 && btnParams.getY() > 0 && mBtMap.get(key2) != null) {
                        keyPoints.add(packKeyData2(mBtMap.get(key2), 0, OAODEVICE_Y - turnY(btnParams.getY()), turnX(btnParams.getX()), KEYMODE.MP_TOUCH));
                        if (btnParams.getKeyRepeatType() != 0)//有附属按钮
                        {

                            BtnParams btnParams2 = btnParams.getBtn2();
                            if (btnParams.getKeyRepeatType() == 1)
                                keyPoints.add(packKeyData2(mBtMap.get(key2), 0, OAODEVICE_Y - turnY(btnParams2.getY()), turnX(btnParams2.getX()), KEYMODE.MP_TOUCH_LINK));
                        }
                    }

                }

                int oneConfigLen = cj_cfg_t.all2Bytes().length + keyPoints.all2Bytes().length + 2;
                cj_cfg_t.add_(0, (byte) oneConfigLen);
                cj_cfg_t.add_(0, SimpleUtil.sumCheck(cj_cfg_t.add(keyPoints).all2Bytes()));
                config.mData = cj_cfg_t;
                config.mSize = config.mData.all2Bytes().length;
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

    private Req mReq = new Req();

    public void writeManyConfigs(final List<Config> allConfigs) {

        SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleUtil.addWaitToTop(mContext, "");
                SimpleUtil.sleep(20);
                byte index_ = (byte) 0xc0;
                for (int i = 0; i < allConfigs.size(); i++) {
                    resetReq();
                    SimpleUtil.log("正在载入 " + allConfigs.get(i).getmContent() + ":" + allConfigs.get(i).getmConfigName());
                    SimpleUtil.updateWaitTopMsg("正在载入\n  " + allConfigs.get(i).getmContent() + "\n    " + allConfigs.get(i).getmConfigName());
                    if (!diveSend(allConfigs.get(i).mData.all2Bytes(), index_)) {
                        SimpleUtil.log("请求错误！" + Hex.toString(mReq.mReqResult) + "," + false);
                        SimpleUtil.resetWaitTop();
                        SimpleUtil.addMsgBottomToTop(mContext, "配置写入失败！", true);
                        SimpleUtil.saveToShare(mContext, "ini", "configschange", true);
                        return;
                    }

                    index_++;
                    resetReq();
                }
                SimpleUtil.resetWaitTop();
                SimpleUtil.addMsgBottomToTop(mContext, "配置写入成功！", false);
                SimpleUtil.saveToShare(mContext, "ini", "configschange", false);

            }
        });

    }

    public boolean openOrCloseRecKeycode(boolean open) {
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
                SimpleUtil.addMsgBottomToTop(mContext, "已关闭按键配置调整", true);
            } else if (data[3] == 0x01) {
                SimpleUtil.mAOAInjectEable = false;
                SimpleUtil.addMsgBottomToTop(mContext, "已开启按键配置调整", false);
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
        return mAccInputThread.isConnect();
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

    public class Config {
        protected String mContent;
        protected String mConfigName;
        protected ByteArrayList mData;
        protected boolean mIsUsed;
        protected int mSize;
        protected boolean mIsDeleted;

        public String getmContent() {
            return mContent;
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

        public int getmSize() {
            return mSize;
        }

        public boolean getIsDeleted() {
            return mIsDeleted;
        }

        public void setDeleted(boolean flag) {
            mIsDeleted = flag;
        }

        @Override
        public boolean equals(Object obj) {
            return mConfigName.equals(((Config) obj).mConfigName);
        }

    }


    class Req {
        byte mReqType;//当前请求码类型
        byte[] mReqResult;//请求结果数据
    }
}
