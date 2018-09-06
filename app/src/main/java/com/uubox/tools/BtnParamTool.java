package com.uubox.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.example.cgodawson.xml.XmlPugiElement;
import com.uubox.padtool.R;
import com.uubox.views.BtnParams;
import com.uubox.views.KeyboardView;

public class BtnParamTool {

    public static final int JOYSTICK_MODE_ROCK_FINAL = 0;

    public static final int DEFAULT_SLIDE_STEP = 15;

    public static final int DEFAULT_SLIDE_FREQUENCY = 30;
    private static final String KEY_IS_SHOW_KB_FLOAT_VIEW = "is_show_kb_float_view";
    private static final String TAG = "BtnParamTool";
    public static boolean mIsInjectionEnabled = false;
    private static TimerTask mLeftJoystickTimerTask;
    /**
     * 右摇杆定时任务，用于实现右摇杆的滑动模式
     */
    private static TimerTask mRightJoystickTimerTask;
    /**
     * 按钮参数集
     */
    private static ConcurrentHashMap<KeyboardView.Btn, BtnParams> mBtnParams =
            new ConcurrentHashMap<KeyboardView.Btn, BtnParams>();
    private static boolean mHasBtnParamsChanged = false;

    public static String getComfirGame() {
        return comfirGame;
    }

    public static String getComfirGameTab() {
        return comfirGame + "_table";
    }

    private static String comfirGame = "";
    private static boolean pressFloatable = true;

    private static boolean isScreenChange;

    private List<XmlPugiElement> getChild(XmlPugiElement element) {
        List<XmlPugiElement> results = new ArrayList<>();
        String parentName = element.getName();
        XmlPugiElement[] subs = element.getAllChild();

        for (XmlPugiElement e : subs) {
            if (e.getParent().getName().equals(parentName)) {
                results.add(e);
            }
        }
        return results;
    }

    public static void updateGuanfangConfig(Context context, byte[] okxmlbuff) {

        //SimpleUtil.log("updateGuanfangConfig,"+comfirGame+"\n"+new String(okxmlbuff));
        XmlPugiElement xmlPugiElement = new XmlPugiElement(okxmlbuff);

        XmlPugiElement pixtag = xmlPugiElement.getFirstChildByName("Z" + SimpleUtil.zoomx + SimpleUtil.zoomy);
        if (pixtag.loadSucess) {
            SimpleUtil.log(comfirGame + " 寻找到符合的配置：" + "Z" + SimpleUtil.zoomx + SimpleUtil.zoomy);
            xmlPugiElement = pixtag;
        } else {//默认使用1080*1920
            SimpleUtil.log(comfirGame + " 寻找不到符合的配置：" + "Z" + SimpleUtil.zoomx + SimpleUtil.zoomy);
            xmlPugiElement = xmlPugiElement.getFirstChildByName("Z10802160");
        }
        XmlPugiElement[] childs = xmlPugiElement.getAllChild();
        int zoomx = Integer.parseInt(childs[0].getAttr("zoomx"));
        int zoomy = Integer.parseInt(childs[0].getAttr("zoomy"));
        mBtnParams.clear();
        for (int i = 1; i < childs.length; i++) {
            XmlPugiElement element = childs[i];
            String nodeName = element.getName();
            KeyboardView.Btn key = KeyboardView.Btn.valueOf(nodeName);
            if (mBtnParams.containsKey(key)) {
                continue;
            }
            BtnParams btnParam = new BtnParams();
            btnParam.setBelongBtn(key);
            setParam(btnParam, element, zoomx, zoomy);
            // SimpleUtil.log("put:"+btnParam.toString());
            mBtnParams.put(key, btnParam);
            //需要加载子按键
            if (btnParam.iHaveChild()) {
                BtnParams subParam = new BtnParams();
                subParam.setBelongBtn(key);
                setParam(subParam, element.getFirstChild(), zoomx, zoomy);
                btnParam.btn2 = subParam;
                mBtnParams.put(key, btnParam);
            }

        }

        saveBtnParams(context, (comfirGame + System.currentTimeMillis()) + "#Z%W#" + comfirGame + "[官方]#Z%W#" + comfirGame + "[官方]");
        xmlPugiElement.release();

    }

    public static void setIsScreenChange(boolean change) {
        isScreenChange = change;
    }

    private static void setParam(BtnParams btnParam, XmlPugiElement element, int zoomx, int zoomy) {

        btnParam.setFrequency(Integer.parseInt(element.getAttr("frequency")));
        btnParam.doParent(Boolean.valueOf(element.getAttr("isParent")));
        btnParam.setKeyRepeatSwitch(Boolean.valueOf(element.getAttr("switch")));
        btnParam.setKeyType(Integer.parseInt(element.getAttr("keyType")));
        btnParam.setMode(Integer.parseInt(element.getAttr("mode")));
        btnParam.setStep(Integer.parseInt(element.getAttr("step")));

        btnParam.setR((int) ((SimpleUtil.zoomy * Integer.parseInt(element.getAttr("r"))) / (zoomy * 1.0f)));
        btnParam.setX((int) ((Integer.parseInt(element.getAttr("x")) * SimpleUtil.zoomy) / (1.0f * zoomy)));
        btnParam.setY((int) ((Integer.parseInt(element.getAttr("y")) * SimpleUtil.zoomx) / (1.0f * zoomx)));

    }

    //由于json强转，部分手机异常，现在采用解析字符串的方式进行
    private static KeyboardView.Btn getByKey(String key) {
        return KeyboardView.Btn.valueOf(key);
    }


    public static ConcurrentHashMap<KeyboardView.Btn, BtnParams> getmBtnParams() {
        return mBtnParams;
    }


    private static int getPrefInt(Context context, final String spFileName, final String key,
                                  final int defaultValue) {
        return (Integer) SimpleUtil.getFromShare(context, spFileName, key, int.class, defaultValue);
    }

    public static void loadBtnParamsFromPrefs(Context context) {
        loadBtnParamsFromPrefs(context, false);
    }

    public static void loadBtnParamsFromPrefs(Context context, boolean iniReset) {
        SimpleUtil.log("=============开始加载默认配置参数=============");
        SimpleUtil.log("当前运行的游戏:" + comfirGame);
        mBtnParams.clear();

       /* if (comfirGame.contains("丛林法则") || comfirGame.contains("光荣使命") ||
                comfirGame.contains("小米枪战") || comfirGame.contains("穿越火线") || comfirGame.contains("终结者")
                || comfirGame.contains("全军出击") || comfirGame.contains("刺激战场") || comfirGame.contains("荒野行动")) {
            SimpleUtil.log("[预加载官方配置]");
            loadIniFile(context, comfirGame + ".xml", comfirGame + "[官方]#Z%W#" + comfirGame + "[官方]");
            mBtnParams.clear();
        }*/
        mHasBtnParamsChanged = false;
        String gloabkeyconfig = (String) SimpleUtil.getFromShare(context, "ini", "gloabkeyconfig", String.class, "");
        SimpleUtil.log("默认使用==>" + gloabkeyconfig);
        String[] sp = gloabkeyconfig.split("#Z%W#", -1);
        if (sp.length < 2) {
            return;
        }
        SimpleUtil.log("加载配置文件到内存==>" + sp[2]);
        SharedPreferences lib = context.getSharedPreferences(sp[2], 0);
        for (KeyboardView.Btn btn : KeyboardView.Btn.values()) {
            BtnParams params = new BtnParams();
            params.setBelongBtn(btn);
            params.setX((Integer) SimpleUtil.getFromShare(lib, btn.getPrefX(), int.class, -1));
            params.setY((Integer) SimpleUtil.getFromShare(lib, btn.getPrefY(), int.class, -1));
            params.setR((Integer) SimpleUtil.getFromShare(lib, btn.getPrefR(), int.class, -1));

            params.setKeyType((Integer) SimpleUtil.getFromShare(lib, btn.getPrefType(), int.class, 0));
            params.setKeyRepeatSwitch((Boolean) SimpleUtil.getFromShare(context, sp[2], btn.getPrefSwitch(), boolean.class));


            if (btn == KeyboardView.Btn.L || btn == KeyboardView.Btn.R) {
                params.setMode(
                        (Integer) SimpleUtil.getFromShare(lib, btn.getPrefMode(), int.class, 0));
                params.setStep(
                        (Integer) SimpleUtil.getFromShare(lib, btn.getPrefStep(), int.class, 15));
                params.setFrequency((Integer) SimpleUtil.getFromShare(lib, btn.getPrefFrequency(), int.class, 30));
            } else {
                params.setMode((Integer) SimpleUtil.getFromShare(lib, btn.getPrefMode(), int.class, -1));
                params.setStep((Integer) SimpleUtil.getFromShare(lib, btn.getPrefStep(), int.class, -1));
                params.setFrequency((Integer) SimpleUtil.getFromShare(lib, btn.getPrefFrequency(), int.class, -1));
            }
            params.doParent((Boolean) SimpleUtil.getFromShare(lib, btn.getParent(), boolean.class, false));

            //有附属按键需要加载
            if (params.iHaveChild()) {
                BtnParams subParams = new BtnParams();
                subParams.setBelongBtn(btn);
                subParams.doParent(false);
                subParams.setKeyType((Integer) SimpleUtil.getFromShare(lib, btn.getPrefType() + "_2", int.class, 0));
                subParams.setX((Integer) SimpleUtil.getFromShare(lib, btn.getPrefX() + "_2", int.class, -1));
                subParams.setY((Integer) SimpleUtil.getFromShare(lib, btn.getPrefY() + "_2", int.class, -1));
                subParams.setR((Integer) SimpleUtil.getFromShare(lib, btn.getPrefR() + "_2", int.class, -1));


                if (btn == KeyboardView.Btn.L || btn == KeyboardView.Btn.R) {
                    subParams.setMode(
                            (Integer) SimpleUtil.getFromShare(lib, btn.getPrefMode() + "_2", int.class, JOYSTICK_MODE_ROCK_FINAL));
                    subParams.setStep(
                            (Integer) SimpleUtil.getFromShare(lib, btn.getPrefStep() + "_2", int.class, DEFAULT_SLIDE_STEP));
                    subParams.setFrequency((Integer) SimpleUtil.getFromShare(lib, btn.getPrefFrequency() + "_2",
                            int.class, DEFAULT_SLIDE_FREQUENCY));
                } else {
                    subParams.setMode((Integer) SimpleUtil.getFromShare(lib, btn.getPrefMode() + "_2", int.class, -1));
                    subParams.setStep((Integer) SimpleUtil.getFromShare(lib, btn.getPrefStep() + "_2", int.class, -1));
                    subParams.setFrequency((Integer) SimpleUtil.getFromShare(lib, btn.getPrefFrequency() + "_2", int.class, -1));
                }

                params.setBtn2(subParams);

            }

            mBtnParams.put(btn, params);
        }
    }

    public static LinkedHashMap<KeyboardView.Btn, BtnParams> getButtonParamsFromXML(Context context, String db_xml) {
        final SharedPreferences sp =
                context.getSharedPreferences(db_xml, Context.MODE_MULTI_PROCESS);
        LinkedHashMap<KeyboardView.Btn, BtnParams> result = new LinkedHashMap<>();
        for (KeyboardView.Btn btn : KeyboardView.Btn.values()) {
            BtnParams params = new BtnParams();
            params.setBelongBtn(btn);
            params.setX((Integer) SimpleUtil.getFromShare(sp, btn.getPrefX(), int.class, -1));
            params.setY((Integer) SimpleUtil.getFromShare(sp, btn.getPrefY(), int.class, -1));
            params.setEx((Integer) SimpleUtil.getFromShare(sp, btn.getPrefEX(), int.class, -1));
            params.setEy((Integer) SimpleUtil.getFromShare(sp, btn.getPrefEY(), int.class, -1));
            params.setR((Integer) SimpleUtil.getFromShare(sp, btn.getPrefR(), int.class, -1));
            params.doParent((Boolean) SimpleUtil.getFromShare(sp, btn.getParent(), boolean.class, false));
            params.setKeyType((Integer) SimpleUtil.getFromShare(sp, btn.getPrefType(), int.class, 0));
            params.setKeyRepeatSwitch((Boolean) SimpleUtil.getFromShare(sp, btn.getPrefSwitch(), boolean.class, true));

            //SimpleUtil.log("getButtonParamsFromXML " + db_xml+":"+params.toString());
            params.setMode((Integer) SimpleUtil.getFromShare(sp, btn.getPrefMode(), int.class, 0));
            params.setStep((Integer) SimpleUtil.getFromShare(sp, btn.getPrefStep(), int.class, 15));
                params.setFrequency((Integer) SimpleUtil.getFromShare(sp, btn.getPrefFrequency(), int.class, 30));

            //有附属按键需要加载
            if (params.iHaveChild()) {
                BtnParams subParams = new BtnParams();
                subParams.setBelongBtn(btn);
                subParams.doParent(false);
                subParams.setX((Integer) SimpleUtil.getFromShare(sp, btn.getPrefX() + "_2", int.class, -1));
                subParams.setY((Integer) SimpleUtil.getFromShare(sp, btn.getPrefY() + "_2", int.class, -1));
                subParams.setEx((Integer) SimpleUtil.getFromShare(sp, btn.getPrefEX() + "_2", int.class, -1));
                subParams.setEy((Integer) SimpleUtil.getFromShare(sp, btn.getPrefEY() + "_2", int.class, -1));
                subParams.setR((Integer) SimpleUtil.getFromShare(sp, btn.getPrefR() + "_2", int.class, -1));
                subParams.setKeyType((Integer) SimpleUtil.getFromShare(sp, btn.getPrefType() + "_2", int.class, 0));

                subParams.setMode((Integer) SimpleUtil.getFromShare(sp, btn.getPrefMode() + "_2", int.class, 0));
                subParams.setStep((Integer) SimpleUtil.getFromShare(sp, btn.getPrefStep() + "_2", int.class, 15));
                subParams.setFrequency((Integer) SimpleUtil.getFromShare(sp, btn.getPrefFrequency() + "_2", int.class, 30));
                params.setBtn2(subParams);

            }

            result.put(btn, params);
        }
        return result;
    }

    public static void resetBtnParams(final KeyboardView.Btn btn) {
        if (btn == null) {
            return;
        }

        setBtnPositionX(btn, -1);
        setBtnPositionY(btn, -1);
        setBtnRadius(btn, -1);
        mBtnParams.get(btn).setKeyType(0);
        mBtnParams.get(btn).doParent(false);
        if (btn == KeyboardView.Btn.L || btn == KeyboardView.Btn.R) {
            setJoystickMode(null, btn, JOYSTICK_MODE_ROCK_FINAL);
            setJoystickStep(null, btn, DEFAULT_SLIDE_STEP);
            setJoystickFrequency(null, btn, DEFAULT_SLIDE_FREQUENCY);
        } else {
            setJoystickMode(null, btn, -1);
            setJoystickStep(null, btn, -1);
            setJoystickFrequency(null, btn, -1);
        }
        mHasBtnParamsChanged = true;
    }


    public static void resetRepeatBtnParams(final KeyboardView.Btn btn) {
        if (btn == null) {
            return;
        }
        BtnParams params = mBtnParams.get(btn).getBtn2();
        params.setX(-1);
        params.setY(-1);
        params.setR(-1);
        params.setKeyType(0);

        if (btn == KeyboardView.Btn.L || btn == KeyboardView.Btn.R) {
            params.setMode(JOYSTICK_MODE_ROCK_FINAL);
            params.setStep(DEFAULT_SLIDE_STEP);
            params.setFrequency(DEFAULT_SLIDE_FREQUENCY);
        } else {
            params.setMode(JOYSTICK_MODE_ROCK_FINAL);
            params.setStep(DEFAULT_SLIDE_STEP);
            params.setFrequency(DEFAULT_SLIDE_FREQUENCY);
        }
        mHasBtnParamsChanged = true;
    }

    public static boolean canSaveIniToXml(Context context, String iniName) {
        SharedPreferences sharedPreferences2 = context.getSharedPreferences(getComfirGameTab(), 0);
        Iterator<? extends Map.Entry<String, ?>> it = sharedPreferences2.getAll().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ?> obj = it.next();
            String value = (String) obj.getValue();

            //byte[] value_int_s = SimpleUtil.getAES().decrypt(value.getBytes());
            // value = new String(value_int_s);

            String[] value_sp = value.split("#Z%W#", -1);
            if (value_sp[1].equals(iniName)) {
                SimpleUtil.log("we donnot save the same param===>" + value_sp[1]);
                return false;
            }

        }
        return true;
    }

    public static synchronized boolean saveBtnParams(Context context, String wholeName) {
        pressFloatable = false;
        SimpleUtil.log("saveBtnParams===>" + wholeName);
        String[] sp = wholeName.split("#Z%W#", -1);

        //判断是否重复

        /*if (!canSaveIniToXml(sp[1])) {
            return false;
        }*/

        String mSpFileName = null;
        boolean isFind = false;
        //保存到新的配置
        if (sp[1].equals(sp[2])) {
            SimpleUtil.log("检测到有新的配置需要保存！");
            //查一下是否已经存在，主要防止官方配置重新加载
            SharedPreferences sharedPreferences = context.getSharedPreferences(comfirGame + "_table", 0);
            Map<String, ?> map = sharedPreferences.getAll();
            Iterator<String> tableit = map.keySet().iterator();

            while (tableit.hasNext()) {
                String key = tableit.next();
                String value = (String) map.get(key);
                SimpleUtil.log("key:" + key + ",value:" + value);
                String[] valuesp = value.split("#Z%W#");
                if (valuesp[1].equals(sp[1])) {
                    SimpleUtil.log("已经存在了一个配置了，则直接保存进去即可！");
                    mSpFileName = valuesp[2];
                    isFind = true;
                    break;
                }
            }
            if (!isFind) {
                mSpFileName = SimpleUtil.getSha1(sp[2] + sp[0]);
                SimpleUtil.log("it is a new config===>" + mSpFileName);

                String configIDs = (String) SimpleUtil.getFromShare(context, "ini", "configsID", String.class, "");
                if (configIDs.isEmpty()) {
                    byte[] ids = new byte[100];
                    ids[0] = 0x01;
                    SimpleUtil.saveToShare(context, "ini", "configsID", Hex.toString(ids));
                    SimpleUtil.saveToShare(context, mSpFileName, "configID", 1);
                } else {
                    byte[] ids = Hex.parse(configIDs);
                    for (int i = 0; i < 100; i++) {
                        if (ids[i] == 0) {
                            ids[i] = (byte) (ids[i - 1] + 1);
                            SimpleUtil.saveToShare(context, "ini", "configsID", Hex.toString(ids));
                            SimpleUtil.saveToShare(context, mSpFileName, "configID", (int) ids[i]);
                            break;
                        }
                    }
                }
                SimpleUtil.log("保存新的配置ID到xml:" + mSpFileName);
            }

        } else {//保存到已有配置
            mSpFileName = sp[2];
        }
        SharedPreferences lib = context.getSharedPreferences(mSpFileName, 0);
        SharedPreferences.Editor editor = lib.edit();
        saveT0LocalXMLLib(editor);
        if (sp[1].endsWith("[官方]") && !isFind) {
            if (!sp[1].contains("刺激战场") && !sp[1].contains("全军出击") && !sp[1].contains("荒野行动") && !sp[1].contains("光荣使命")) {
                SimpleUtil.editSaveToShare(editor, "isDelete", true);
            }
        }
        editor.commit();
        mHasBtnParamsChanged = false;
        //需要增加配置，打开这个函数
        if (SimpleUtil.isSaveToXml) {
            saveBtnParamsObjs(context);
        }
        if (isFind) {
            pressFloatable = true;
            return true;
        }
        //第一次，则需要建立映射表
        wholeName = sp[0] + "#Z%W#" + sp[1] + "#Z%W#" + (sp[1].equals(sp[2]) ? SimpleUtil.getSha1(sp[2] + sp[0]) : sp[2]);
        if (mSpFileName != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(comfirGame + "_table", 0);

            //加到总映射表

            SimpleUtil.saveToShare(context, "KeysConfigs", comfirGame, System.currentTimeMillis());
            if (sharedPreferences.getAll().size() == 0 || sp[1].equals(sp[2])) {
                //sharedPreferences.edit().putString("default", wholeName).commit();
                SimpleUtil.saveToShare(context, "ini", "gloabkeyconfig", wholeName + "#Z%W#" + comfirGame);
                SimpleUtil.saveToShare(sharedPreferences.edit(), sp[0], wholeName);
                pressFloatable = true;
                return true;
            }

            sharedPreferences.edit().putString(sp[0], wholeName).commit();
        }
        pressFloatable = true;
        return true;

    }

    private static void saveT0LocalXMLLib(SharedPreferences.Editor editor) {

        Iterator<KeyboardView.Btn> it = mBtnParams.keySet().iterator();
        while (it.hasNext()) {
            KeyboardView.Btn btn = it.next();
            if (btn == KeyboardView.Btn.Q) {
                continue;
            }
            BtnParams params = mBtnParams.get(btn);
            //SimpleUtil.log("saveT0LocalXMLLib:"+params.toString());
            SimpleUtil.editSaveToShare(editor, btn.getPrefY(), params.getY());
            SimpleUtil.editSaveToShare(editor, btn.getPrefR(), params.getR());
            SimpleUtil.editSaveToShare(editor, btn.getPrefX(), params.getX());
            SimpleUtil.editSaveToShare(editor, btn.getPrefMode(), params.getMode());
            SimpleUtil.editSaveToShare(editor, btn.getPrefStep(), params.getStep());
            SimpleUtil.editSaveToShare(editor, btn.getPrefFrequency(), params.getFrequency());
            SimpleUtil.editSaveToShare(editor, btn.getPrefType(), params.getKeyType());
            SimpleUtil.editSaveToShare(editor, btn.getPrefSwitch(), params.isKeyRepeatSwitch());
            SimpleUtil.editSaveToShare(editor, btn.getParent(), params.isParent());
            if (params.isParent()) {
                BtnParams childParam = mBtnParams.get(btn).btn2;
                SimpleUtil.editSaveToShare(editor, btn.getPrefY() + "_2", childParam.getY());
                SimpleUtil.editSaveToShare(editor, btn.getPrefR() + "_2", childParam.getR());
                SimpleUtil.editSaveToShare(editor, btn.getPrefX() + "_2", childParam.getX());
                SimpleUtil.editSaveToShare(editor, btn.getPrefMode() + "_2", childParam.getMode());
                SimpleUtil.editSaveToShare(editor, btn.getPrefStep() + "_2", childParam.getStep());
                SimpleUtil.editSaveToShare(editor, btn.getPrefFrequency() + "_2", childParam.getFrequency());
                SimpleUtil.editSaveToShare(editor, btn.getPrefType() + "_2", childParam.getKeyType());
                SimpleUtil.editSaveToShare(editor, btn.getPrefSwitch() + "_2", childParam.isKeyRepeatSwitch());
                SimpleUtil.editSaveToShare(editor, btn.getParent() + "_2", childParam.isParent());
            }

        }

    }

    public static boolean getPressFloatable() {
        return pressFloatable;
    }

    private static void saveBtnParamsObjs(Context context) {
        XmlPugiElement ini_xml = XmlPugiElement.createXml("/sdcard/Zhiwan/ini_button_" + comfirGame + ".xml", "Root", true);
        XmlPugiElement zoom = ini_xml.addNode("ZOOM");
        zoom.addAttr("zoomx", SimpleUtil.zoomx + "");
        zoom.addAttr("zoomy", SimpleUtil.zoomy + "");
        Iterator<KeyboardView.Btn> it = mBtnParams.keySet().iterator();
        while (it.hasNext()) {
            KeyboardView.Btn key = it.next();
            BtnParams param = mBtnParams.get(key);
            if (param == null) {
                continue;
            }

            saveXmlNode(ini_xml, key, param);

        }
        ini_xml.save();
        ini_xml.release();

        //暂时不加密
        //boolean result = SimpleUtil.saveSmallFileToLocal(SimpleUtil.getAES().encrypt(SimpleUtil.getSmallFile(context, "/sdcard/Zhiwan/ini_button_" + comfirGame + ".xml")), "/sdcard/Zhiwan/ini_button_" + comfirGame + ".xml");
        SimpleUtil.addMsgBottomToTop(context, "保存配置到本地" + (true ? "成功" : "失败") + " 路径:" + "/sdcard/Zhiwan/ini_button_" + comfirGame + ".xml", false);
    }

    private static void saveXmlNode(XmlPugiElement mainNode, KeyboardView.Btn key, BtnParams param) {

        try {
            LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
            attrs.put("frequency", param.getFrequency() + "");
            attrs.put("isParent", param.isParent() + "");
            attrs.put("switch", true + "");
            attrs.put("keyType", param.getKeyType() + "");
            attrs.put("mode", param.getMode() + "");
            attrs.put("r", param.getR() + "");
            attrs.put("step", param.getStep() + "");
            attrs.put("x", param.getX() + "");
            attrs.put("y", param.getY() + "");

            XmlPugiElement addNode = mainNode.addNode(key.name(), "", attrs);

            //有附属按键，则需要增加子节点
            if (param.iHaveChild()) {
                saveXmlNode(addNode, key, param.btn2);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //default#Z%W#绝地求生之全军出击[官方]#Z%W#d8a4370a36d19286077bc485d61601d3ede501bd#Z%W#绝地求生之全军出击
    public static boolean setDefaultUseConfig(Context context, String configname, String configsha, String belonggame) {
        setComfirGame(belonggame);
        return SimpleUtil.saveToShare(context, "ini", "gloabkeyconfig", "default#Z%W#" + configname + "#Z%W#" + configsha + "#Z%W#" + belonggame);

    }
    public static int getBtnRepeatType(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return 0;
        }

        return mBtnParams.get(btn).getKeyType();
    }

    public static boolean isBtnHasChild(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return false;
        }

        return mBtnParams.get(btn).isParent();
    }

    public static int getBtnBelongColor(final BtnParams params) {

        switch (params.getKeyType()) {
            case 3:
                return R.mipmap.circle_key;
            case 1:
                return R.mipmap.circle_liandong;
            case 2:
                return R.mipmap.circle_huchi;
        }
        return 0;
    }

    public static void setBtnRepeatType(final KeyboardView.Btn btn, final int type) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return;
        }
        BtnParams params = mBtnParams.get(btn);
        params.setKeyType(type);
        mBtnParams.replace(btn, params);
        mHasBtnParamsChanged = true;
    }

    public static boolean getBtnRepeatSwitch(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return false;
        }

        return mBtnParams.get(btn).isKeyRepeatSwitch();
    }

    public static BtnParams getBtnRepeatBtn2(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return null;
        }

        return mBtnParams.get(btn).getBtn2();
    }

    public static BtnParams getBtnNormalBtn(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return null;
        }

        return mBtnParams.get(btn);
    }

    public static void setBtnRepeatBtn2(final KeyboardView.Btn btn, final BtnParams btn2) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return;
        }
        BtnParams params = mBtnParams.get(btn);
        params.setBtn2(btn2);
        //mBtnParams.replace(btn, params);
        mHasBtnParamsChanged = true;
    }

    public static int getBtnPositionX(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return -1;
        }

        return mBtnParams.get(btn).getX();
    }

    public static void setBtnPositionX(final KeyboardView.Btn btn, final int x) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return;
        }
        BtnParams params = mBtnParams.get(btn);
        params.setX(x);
        mBtnParams.replace(btn, params);
        mHasBtnParamsChanged = true;
    }

    public static int getBtnPositionY(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return -1;
        }

        return mBtnParams.get(btn).getY();
    }

    public static void setBtnPositionY(final KeyboardView.Btn btn, final int y) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return;
        }

        BtnParams params = mBtnParams.get(btn);
        params.setY(y);
        mBtnParams.replace(btn, params);
        mHasBtnParamsChanged = true;
        Log.e("mk", "setBtnPositionY: " + params.toString());
    }

    public static int getBtnRadius(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return -1;
        }

        return mBtnParams.get(btn).getR();
    }

    public static boolean getBtnParent(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return false;
        }

        return mBtnParams.get(btn).isParent();
    }

    public static void setBtnRadius(final KeyboardView.Btn btn, final int r) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return;
        }

        BtnParams params = mBtnParams.get(btn);
        params.setR(r);
        mBtnParams.replace(btn, params);
        mHasBtnParamsChanged = true;
    }

    public static void setBtnRadius2(final KeyboardView.Btn btn, final int r) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return;
        }

        BtnParams params = getBtnRepeatBtn2(btn);
        params.setR(r);
        //mBtnParams.replace(btn, params);
        mHasBtnParamsChanged = true;
    }

    public static void setJoystickMode(final Context context, KeyboardView.Btn joystick,
                                       final int mode) {
        if (joystick == null || mBtnParams == null || !mBtnParams.containsKey(joystick)) {
            return;
        }

        BtnParams params = mBtnParams.get(joystick);
        params.setMode(mode);
        mBtnParams.replace(joystick, params);
        mHasBtnParamsChanged = true;
        //        setPrefInt(context, mSpFileName, joystick.getPrefMode(), mode);
    }

    public static int getJoystickMode(final Context context, final KeyboardView.Btn joystick) {
        if (joystick == null || mBtnParams == null || !mBtnParams.containsKey(joystick)) {
            return -1;
        }

        return mBtnParams.get(joystick).getMode();
    }

    public static void setJoystickStep(final Context context, final KeyboardView.Btn joystick,
                                       final int step) {
        if (joystick == null || mBtnParams == null || !mBtnParams.containsKey(joystick)) {
            return;
        }

        BtnParams params = mBtnParams.get(joystick);
        params.setStep(step);
        mBtnParams.replace(joystick, params);
        mHasBtnParamsChanged = true;
        //        setPrefInt(context, mSpFileName, joystick.getPrefStep(), step);
    }

    public static int getJoystickStep(final Context context, final KeyboardView.Btn joystick) {
        if (joystick == null || mBtnParams == null || !mBtnParams.containsKey(joystick)) {
            return -1;
        }

        return mBtnParams.get(joystick).getStep();
    }

    public static void setJoystickFrequency(final Context context, final KeyboardView.Btn joystick,
                                            final int frequency) {
        if (joystick == null || mBtnParams == null || !mBtnParams.containsKey(joystick)) {
            return;
        }

        BtnParams params = mBtnParams.get(joystick);
        params.setFrequency(frequency);
        mBtnParams.replace(joystick, params);
        mHasBtnParamsChanged = true;
    }

    public static int getJoystickFrequency(final Context context, final KeyboardView.Btn joystick) {
        if (joystick == null || mBtnParams == null || !mBtnParams.containsKey(joystick)) {
            return -1;
        }

        return mBtnParams.get(joystick).getFrequency();
    }

    public static boolean hasBtnParamsChanged() {
        return mHasBtnParamsChanged;
    }

    public static void setBtnParamsChanged(boolean flag) {
        mHasBtnParamsChanged = flag;
    }

    public static void enableInjection() {
        mIsInjectionEnabled = true;
    }

    public static void disableInjection() {
        mIsInjectionEnabled = false;
        cancelAllTimerTasks();
    }

    public static void cancelAllTimerTasks() {
        if (mLeftJoystickTimerTask != null) {
            mLeftJoystickTimerTask.cancel();
            mLeftJoystickTimerTask = null;
        }
        if (mRightJoystickTimerTask != null) {
            mRightJoystickTimerTask.cancel();
            mRightJoystickTimerTask = null;
        }
    }

    public static void setIsShowKbFloatView(Context context, boolean isShow) {
        SimpleUtil.saveToShare(context, "ini", KEY_IS_SHOW_KB_FLOAT_VIEW, isShow);
    }

    public static boolean isShowKbFloatView(Context context) {
        return (Boolean) SimpleUtil.getFromShare(context, "ini", KEY_IS_SHOW_KB_FLOAT_VIEW, boolean.class, true);
    }

    public static void setComfirGame(String game) {
        comfirGame = game;
        SimpleUtil.log("set use gameItem:" + game);
    }

}
