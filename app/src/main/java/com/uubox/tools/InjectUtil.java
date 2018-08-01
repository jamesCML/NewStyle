package com.uubox.tools;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.cgodawson.xml.XmlPugiElement;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.uubox.padtool.R;
import com.uubox.views.BtnParams;
import com.uubox.views.KeyboardView;

public class InjectUtil {

    public static final int JOYSTICK_MODE_ROCK_FINAL = 0;

    public static final int DEFAULT_SLIDE_STEP = 15;

    public static final int DEFAULT_SLIDE_FREQUENCY = 30;
    private static final String KEY_IS_SHOW_KB_FLOAT_VIEW = "is_show_kb_float_view";
    private static final String TAG = "InjectUtil";
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
    public static int mouseXxx = 5;
    public static int mouseYyy = 5;
    public static int mouseWww = 50;

    public static String getComfirGame() {
        return comfirGame;
    }

    public static String getComfirGameTab() {
        return comfirGame + "_table";
    }

    private static String comfirGame = "";
    private static boolean pressFloatable = true;

    private static boolean isScreenChange;

    private static void loadIniFile(Context context, String filepath, String iniFile) {

        XmlPugiElement xmlPugiElement = new XmlPugiElement(SimpleUtil.getAssertSmallFile(context, "keyconfigs/" + filepath));

        XmlPugiElement[] childs = xmlPugiElement.getAllChild();

        int zoomx = Integer.parseInt(childs[0].getAttr("zoomx"));
        int zoomy = Integer.parseInt(childs[0].getAttr("zoomy"));
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

        saveBtnParams(context, (comfirGame + System.currentTimeMillis()) + "#Z%W#" + iniFile);
        xmlPugiElement.release();

    }

    public static void setIsScreenChange(boolean change) {
        isScreenChange = change;
    }

    private static void setParam(BtnParams btnParam, XmlPugiElement element, int zoomx, int zoomy) {

        btnParam.setFrequency(Integer.parseInt(element.getAttr("frequency")));
        btnParam.setBelongButton(Boolean.valueOf(element.getAttr("isBelongButton")));
        btnParam.setKeyRepeatSwitch(Boolean.valueOf(element.getAttr("keyRepeatSwitch")));
        btnParam.setKeyRepeatType(Integer.parseInt(element.getAttr("keyRepeatType")));
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
        SimpleUtil.log("=============start to loadBtnParamsFromPrefs=============");
        //鼠标灵敏度系数
        mouseXxx = (Integer) SimpleUtil.getFromShare(context, "ini", "mMouseProgressX", int.class, 5);
        mouseYyy = (Integer) SimpleUtil.getFromShare(context, "ini", "mMouseProgressY", int.class, 5);
        mouseWww = (Integer) SimpleUtil.getFromShare(context, "ini", "mMouseProgressW", int.class, 50);

        Log.e(TAG, "loadBtnParamsFromPrefs playgame:" + comfirGame);
        mBtnParams.clear();
        // 读取之前保存的文件名（防止游戏过程手柄助手进程因内存问题被系统回收导致mSpFileName复位

        String assertFile = "";
        boolean isFirstOpen = true;
        if (SimpleUtil.versionCode == 0) {
            CommonUtils.getAppVersionName(context);
        }
        //由于当前的配置保存只有官方和用户，不能强制更新保存，应该由用户自己点击按钮更新
        int lastVersion = 0;
        if (comfirGame.contains("荒野行动")) {
            assertFile = "荒野行动.xml";
            comfirGame = "荒野行动";
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "hyxd_ini_", int.class, 0)) < SimpleUtil.versionCode;
        } else if (comfirGame.contains("光荣使命")) {
            ;
            assertFile = "光荣使命.xml";
            ;
            comfirGame = "光荣使命";
            ;
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "grsm_ini_", int.class, 0)) < SimpleUtil.versionCode;
        } else if (comfirGame.contains("终结者")) {
            ;
            assertFile = "终结者.xml";
            ;
            comfirGame = "终结者";
            ;
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "zjz_ini_", int.class, 0)) < SimpleUtil.versionCode;
        } else if (comfirGame.contains("小米枪战")) {
            ;
            assertFile = "小米枪战.xml";
            ;
            comfirGame = "小米枪战";
            ;
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "xmqz_ini_", int.class, 0)) < SimpleUtil.versionCode;
        } else if (comfirGame.contains("穿越火线")) {
            ;
            assertFile = "穿越火线.xml";
            ;
            comfirGame = "穿越火线";
            ;
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "cyhx_ini_", int.class, 0)) < SimpleUtil.versionCode;
        } else if (comfirGame.contains("丛林法则")) {
            ;
            assertFile = "丛林法则.xml";
            ;
            comfirGame = "丛林法则";
            ;
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "clfz_ini_", int.class, 0)) < SimpleUtil.versionCode;
        } else if (comfirGame.contains("全军出击")) {
            ;
            assertFile = "绝地求生之全军出击.xml";
            ;
            comfirGame = "绝地求生之全军出击";
            ;
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "qjcj_ini_", int.class, 0)) < SimpleUtil.versionCode;
        } else if (comfirGame.contains("刺激战场")) {
            assertFile = "绝地求生之刺激战场.xml";
            comfirGame = "绝地求生之刺激战场";
            isFirstOpen = (lastVersion = (Integer) SimpleUtil.getFromShare(context, "ini", "cjzc_ini_", int.class, 0)) < SimpleUtil.versionCode;
        }
        //配置文件默认
        String mSpFileName = null;
        //第一次或者是点击更新按钮，铁定加载
        //同一个手机想要加载配置想要加上：  ||(!assertFile.isEmpty() &&getPrefInt(context, mSpFileName, KeyboardView.Btn.L.getPrefR(), -1)==-1)  这个条件
        if ((lastVersion == 0 || iniReset) && ((iniReset && !assertFile.isEmpty()) || (!assertFile.isEmpty() && isFirstOpen)))//还没有配置则配置
        {
            loadIniFile(context, assertFile, comfirGame + "[官方]#Z%W#" + comfirGame + "[官方]");
            mHasBtnParamsChanged = false;
            if (comfirGame.contains("荒野行动")) {
                SimpleUtil.saveToShare(context, "ini", "hyxd_ini_", SimpleUtil.versionCode);
            } else if (comfirGame.contains("光荣使命")) {
                SimpleUtil.saveToShare(context, "ini", "grsm_ini_", SimpleUtil.versionCode);
            } else if (comfirGame.contains("终结者")) {
                SimpleUtil.saveToShare(context, "ini", "zjz_ini_", SimpleUtil.versionCode);
            } else if (comfirGame.contains("小米枪战")) {
                SimpleUtil.saveToShare(context, "ini", "xmqz_ini_", SimpleUtil.versionCode);
            } else if (comfirGame.contains("穿越火线")) {
                SimpleUtil.saveToShare(context, "ini", "cyhx_ini_", SimpleUtil.versionCode);
            } else if (comfirGame.contains("丛林法则")) {
                SimpleUtil.saveToShare(context, "ini", "clfz_ini_", SimpleUtil.versionCode);
            } else if (comfirGame.contains("刺激战场")) {
                SimpleUtil.saveToShare(context, "ini", "cjzc_ini_", SimpleUtil.versionCode);
            } else if (comfirGame.contains("全军出击")) {
                SimpleUtil.saveToShare(context, "ini", "qjcj_ini_", SimpleUtil.versionCode);
            }
            return;
        } else //默认配置加载选定的配置
        {

            String gloabkeyconfig = (String) SimpleUtil.getFromShare(context, "ini", "gloabkeyconfig", String.class, "");
            if (gloabkeyconfig != null && !gloabkeyconfig.isEmpty()) {
                String[] sp = gloabkeyconfig.split("#Z%W#", -1);
                mSpFileName = sp[2];
            }

        }

        SimpleUtil.log("loadBtnParamsFromPrefs: now start to init the button params===>" + mSpFileName);
        SharedPreferences lib = context.getSharedPreferences(mSpFileName, 0);
        for (KeyboardView.Btn btn : KeyboardView.Btn.values()) {
            BtnParams params = new BtnParams();
            params.setBelongBtn(btn);
            params.setX((Integer) SimpleUtil.getFromShare(lib, btn.getPrefX(), int.class, -1));
            params.setY((Integer) SimpleUtil.getFromShare(lib, btn.getPrefY(), int.class, -1));
            params.setR((Integer) SimpleUtil.getFromShare(lib, btn.getPrefR(), int.class, -1));

            params.setKeyRepeatType((Integer) SimpleUtil.getFromShare(lib, btn.getPrefType(), int.class, 0));
            params.setKeyRepeatSwitch((Boolean) SimpleUtil.getFromShare(context, mSpFileName, btn.getPrefSwitch(), boolean.class));


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


            //有附属按键需要加载
            if (params.iHaveChild()) {
                BtnParams subParams = new BtnParams();
                subParams.setBelongBtn(btn);
                subParams.setBelongButton(true);
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
        mHasBtnParamsChanged = false;
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
            params.setR((Integer) SimpleUtil.getFromShare(sp, btn.getPrefR(), int.class, -1));

            params.setKeyRepeatType((Integer) SimpleUtil.getFromShare(sp, btn.getPrefType(), int.class, 0));
            params.setKeyRepeatSwitch((Boolean) SimpleUtil.getFromShare(sp, btn.getPrefSwitch(), boolean.class, true));

            // SimpleUtil.log("getButtonParamsFromXML " + btn + ",x:" + params.getX() + " db_xml:" + db_xml);
            if (btn == KeyboardView.Btn.L || btn == KeyboardView.Btn.R) {
                params.setMode(
                        (Integer) SimpleUtil.getFromShare(sp, btn.getPrefMode(), int.class, 0));
                params.setStep(
                        (Integer) SimpleUtil.getFromShare(sp, btn.getPrefStep(), int.class, 15));
                params.setFrequency((Integer) SimpleUtil.getFromShare(sp, btn.getPrefFrequency(), int.class, 30));
            } else {
                params.setMode((Integer) SimpleUtil.getFromShare(sp, btn.getPrefMode(), int.class, -1));
                params.setStep((Integer) SimpleUtil.getFromShare(sp, btn.getPrefStep(), int.class, -1));
                params.setFrequency((Integer) SimpleUtil.getFromShare(sp, btn.getPrefFrequency(), int.class, -1));
            }


            //有附属按键需要加载
            if (params.iHaveChild()) {
                BtnParams subParams = new BtnParams();
                subParams.setBelongBtn(btn);
                subParams.setBelongButton(true);
                subParams.setX((Integer) SimpleUtil.getFromShare(sp, btn.getPrefX() + "_2", int.class, -1));
                subParams.setY((Integer) SimpleUtil.getFromShare(sp, btn.getPrefY() + "_2", int.class, -1));
                subParams.setR((Integer) SimpleUtil.getFromShare(sp, btn.getPrefR() + "_2", int.class, -1));


                if (btn == KeyboardView.Btn.L || btn == KeyboardView.Btn.R) {
                    subParams.setMode(
                            (Integer) SimpleUtil.getFromShare(sp, btn.getPrefMode() + "_2", int.class, 0));
                    subParams.setStep(
                            (Integer) SimpleUtil.getFromShare(sp, btn.getPrefStep() + "_2", int.class, 15));
                    subParams.setFrequency((Integer) SimpleUtil.getFromShare(sp, btn.getPrefFrequency() + "_2", int.class, 30));
                } else {
                    subParams.setMode((Integer) SimpleUtil.getFromShare(sp, btn.getPrefMode() + "_2", int.class, -1));
                    subParams.setStep((Integer) SimpleUtil.getFromShare(sp, btn.getPrefStep() + "_2", int.class, -1));
                    subParams.setFrequency((Integer) SimpleUtil.getFromShare(sp, btn.getPrefFrequency() + "_2", int.class, -1));
                }

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
        mBtnParams.get(btn).setKeyRepeatType(0);
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

        String mSpFileName;

        //保存到新的配置
        if (sp[1].equals(sp[2])) {
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
        } else {//保存到已有配置
            mSpFileName = sp[2];
        }
        SharedPreferences lib = context.getSharedPreferences(mSpFileName, 0);
        SharedPreferences.Editor editor = lib.edit();
        for (KeyboardView.Btn btn : KeyboardView.Btn.values()) {
            if (btn == KeyboardView.Btn.Q) {
                continue;
            }
            SimpleUtil.editSaveToShare(editor, btn.getPrefY(), getBtnPositionY(btn));
            SimpleUtil.editSaveToShare(editor, btn.getPrefR(), getBtnRadius(btn));
            SimpleUtil.editSaveToShare(editor, btn.getPrefX(), getBtnPositionX(btn));
            SimpleUtil.editSaveToShare(editor, btn.getPrefMode(), getJoystickMode(null, btn));
            SimpleUtil.editSaveToShare(editor, btn.getPrefStep(), getJoystickStep(null, btn));
            SimpleUtil.editSaveToShare(editor, btn.getPrefFrequency(), getJoystickFrequency(null, btn));
            SimpleUtil.editSaveToShare(editor, btn.getPrefType(), getBtnRepeatType(btn));
            SimpleUtil.editSaveToShare(editor, btn.getPrefSwitch(), getBtnRepeatSwitch(btn));

            //该按键有附属按键，需要进行保存
            if (isBtnHasChild(btn)) {
                BtnParams param = getBtnRepeatBtn2(btn);
                SimpleUtil.editSaveToShare(editor, btn.getPrefY() + "_2", param.getY());
                SimpleUtil.editSaveToShare(editor, btn.getPrefR() + "_2", param.getR());
                SimpleUtil.editSaveToShare(editor, btn.getPrefX() + "_2", param.getX());
                SimpleUtil.editSaveToShare(editor, btn.getPrefMode() + "_2", param.getMode());
                SimpleUtil.editSaveToShare(editor, btn.getPrefStep() + "_2", param.getStep());
                SimpleUtil.editSaveToShare(editor, btn.getPrefFrequency() + "_2", param.getFrequency());
            }
        }
        if (sp[1].endsWith("[官方]")) {
            if (!sp[1].contains("刺激战场") && !sp[1].contains("全军出击") && !sp[1].contains("荒野行动") && !sp[1].contains("光荣使命")) {
                SimpleUtil.editSaveToShare(editor, "isDelete", true);
            }
        }
        editor.commit();
        mHasBtnParamsChanged = false;
        //需要增加配置，打开这个函数
        if (SimpleUtil.isSaveToXml) {
            saveBtnParamsObjs();
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

    public static boolean getPressFloatable() {
        return pressFloatable;
    }

    private static void saveBtnParamsObjs() {
        XmlPugiElement ini_xml = XmlPugiElement.createXml("/sdcard/Zhiwan/ini_button_" + comfirGame + ".xml", "Root", true);
        XmlPugiElement zoom = ini_xml.addNode("ZOOM");
        zoom.addAttr("zoomx", SimpleUtil.zoomx + "");
        zoom.addAttr("zoomy", SimpleUtil.zoomy + "");
        Iterator<KeyboardView.Btn> it = ((Map<KeyboardView.Btn, BtnParams>) mBtnParams).keySet().iterator();
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

    }

    private static void saveXmlNode(XmlPugiElement mainNode, KeyboardView.Btn key, BtnParams param) {

        try {
            LinkedHashMap<String, String> attrs = new LinkedHashMap<>();
            attrs.put("frequency", param.getFrequency() + "");
            attrs.put("isBelongButton", param.isBelongButton() + "");
            attrs.put("keyRepeatSwitch", true + "");
            attrs.put("keyRepeatType", param.getKeyRepeatType() + "");
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

    public static int getBtnRepeatType(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return 0;
        }

        return mBtnParams.get(btn).getKeyRepeatType();
    }

    public static boolean isBtnHasChild(final KeyboardView.Btn btn) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return false;
        }

        return mBtnParams.get(btn).getKeyRepeatType() == 1 || mBtnParams.get(btn).getKeyRepeatType() == 2;
    }

    public static int getBtnBelongColor(final BtnParams params) {

        switch (params.getKeyRepeatType()) {
            case 1:
                return R.mipmap.circle_liandong;
            case 2:
                return R.mipmap.circle_huchi;
            case 3:
                return R.mipmap.circle_key;
        }
        return 0;
    }
    public static void setBtnRepeatType(final KeyboardView.Btn btn, final int type) {
        if (btn == null || mBtnParams == null || !mBtnParams.containsKey(btn)) {
            return;
        }
        BtnParams params = mBtnParams.get(btn);
        params.setKeyRepeatType(type);
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
