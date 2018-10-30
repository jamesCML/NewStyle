package com.uubox.tools;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.uubox.padtool.R;
import com.uubox.views.KeyboardEditWindowManager;
import com.uubox.views.WrapFloat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SimpleUtil {
    public static boolean DEBUG;
    public static int zoomx;
    public static int zoomy;
    public static boolean isSaveToXml = false;
    public static boolean isEnableOSSLog = false;
    public static boolean isNetLog = false;
    public static boolean screenstate;
    public static boolean mAOAInjectEable;
    public static int LIUHAI;
    public static int mDeviceVersion;
    private static ConcurrentHashMap<String, String> mInfoMap = new ConcurrentHashMap<>();
    public static APPUSER mAPPUSER = APPUSER.WISEGA;
    public static byte[] getAssertSmallFile(Context context, String path) {
        try {
            InputStream stream = context.getAssets().open(path);
            byte[] buff = new byte[stream.available()];
            stream.read(buff, 0, stream.available());
            return buff;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getSmallFile(Context context, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return new byte[]{};
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buff = new byte[fileInputStream.available()];
            fileInputStream.read(buff, 0, fileInputStream.available());
            return buff;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean saveSmallFileToLocal(byte[] buff, String path) {
        File file = new File(path);
        if (file.isDirectory()) {
            SimpleUtil.log("不能保存到目录:" + path);
            return false;
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            fileOutputStream.write(buff);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    public static void putOneInfoToMap(String key, String value) {
      /* if(mInfoMap.containsKey(key))
       {
           mInfoMap.put(getCurTime()+"_"+key+"_"+getSha1(System.currentTimeMillis()+""),value);
       }*/
        mInfoMap.put(key, value);
    }

    public static void clearInfoMap() {
        mInfoMap.clear();
    }

    public static String getInfoMapToString() {
        Iterator<String> it = mInfoMap.keySet().iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            String key = it.next();
            sb.append("【" + key + ":" + mInfoMap.get(key) + "】");
        }
        clearInfoMap();
        return sb.toString();
    }
    /**
     * 保存数据到本地
     *
     * @param context 上下文
     * @param lib
     * @param key
     * @param value
     * @return
     */
    public static boolean saveToShare(Context context, String lib, String key, Object value) {

        SharedPreferences preferences = context.getSharedPreferences(lib, 0);
        SharedPreferences.Editor edit = preferences.edit();
        if (value instanceof Integer) {
            return edit.putInt(key, (Integer) value).commit();
        } else if (value instanceof Long) {
            return edit.putLong(key, (Long) value).commit();
        } else if (value instanceof String) {
            return edit.putString(key, (String) value).commit();
        } else if (value instanceof Boolean) {
            return edit.putBoolean(key, (Boolean) value).commit();
        } else
            return false;
    }

    public static boolean saveToShare(SharedPreferences.Editor edit, String key, Object value) {

        if (value instanceof Integer) {
            return edit.putInt(key, (Integer) value).commit();
        } else if (value instanceof Long) {
            return edit.putLong(key, (Long) value).commit();
        } else if (value instanceof String) {
            return edit.putString(key, (String) value).commit();
        } else if (value instanceof Boolean) {
            return edit.putBoolean(key, (Boolean) value).commit();
        } else
            return false;
    }

    private static AES aes = new AES();

    public static AES getAES() {
        return aes;
    }

    public static SharedPreferences.Editor editSaveToShare(SharedPreferences.Editor edit, String key, Object value) {
        if (value instanceof Integer) {
            return edit.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            return edit.putLong(key, (Long) value);
        } else if (value instanceof String) {
            return edit.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            return edit.putBoolean(key, (Boolean) value);
        } else
            return null;
    }


    public static Object getFromShare(Context context, String lib, String key, Class<?> type) {
        SharedPreferences preferences = context.getSharedPreferences(lib, 0);
        if (type == int.class) {
            return preferences.getInt(key, 0);
        } else if (type == long.class) {
            return preferences.getLong(key, 0);
        } else if (type == String.class) {
            return preferences.getString(key, null);
        } else if (type == boolean.class) {
            return preferences.getBoolean(key, false);
        } else return null;
    }

    public static Object getFromShare(Context context, String lib, String key, Class<?> type, Object defaultValue) {
        SharedPreferences preferences = context.getSharedPreferences(lib, 0);
        if (type == int.class) {
            return preferences.getInt(key, (Integer) defaultValue);
        } else if (type == long.class) {
            return preferences.getLong(key, (Long) defaultValue);
        } else if (type == String.class) {
            return preferences.getString(key, (String) defaultValue);
        } else if (type == boolean.class) {
            return preferences.getBoolean(key, (Boolean) defaultValue);
        } else return null;
    }

    public static Object getFromShare(SharedPreferences preferences, String key, Class<?> type, Object defaultValue) {
        if (type == int.class) {
            return preferences.getInt(key, (Integer) defaultValue);
        } else if (type == long.class) {
            return preferences.getLong(key, (Long) defaultValue);
        } else if (type == String.class) {
            return preferences.getString(key, (String) defaultValue);
        } else if (type == boolean.class) {
            return preferences.getBoolean(key, (Boolean) defaultValue);
        } else return null;
    }

    public static boolean delFromShare(Context context, String lib, String key) {
        SharedPreferences preferences = context.getSharedPreferences(lib, 0);
        SharedPreferences.Editor edit = preferences.edit();
        return edit.remove(key).commit();
    }

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void log(String msg) {
        if (isNetLog || SimpleUtil.DEBUG)
            SocketLogEx.getInstance().sendLog(getCurTime() + "  " + msg);
        if (isEnableOSSLog) {
            LogToFileUtils.write(msg);
        }
        if (!DEBUG) {
            return;
        }
        Log.i("CJLOG", msg);
    }

    public static void loge(String msg) {

        if (!DEBUG) {
            return;
        }
        Log.i("CJLOG", msg + "                     XXXXXXXXXXXXXXXXXXXX");
    }
    public static void sleep(long millisTime) {
        try {
            Thread.sleep(millisTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getSha1(String str) {
        if (str == null || str.length() == 0) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");
            mdTemp.update(str.getBytes("UTF-8"));

            byte[] md = mdTemp.digest();
            int j = md.length;
            char buf[] = new char[j * 2];
            int k = 0;
            for (int i = 0; i < j; i++) {
                byte byte0 = md[i];
                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];
                buf[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(buf);
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
    }

    public interface INormalBack {
        void back(int id, Object obj);
    }

    /**
     * 回调接口
     */
    public static CopyOnWriteArraySet<INormalBack> mCallBacks = new CopyOnWriteArraySet<>();

    /**
     * 设置回调接口
     *
     * @param callback 回调接口
     */
    public static void addINormalCallback(INormalBack callback) {
        if (!mCallBacks.contains(callback)) {
            mCallBacks.add(callback);
        }
    }

    public static void removeINormalCallback(INormalBack callback) {
        mCallBacks.remove(callback);
    }

    public static void notifyall_(int id, Object obj) {
        /**
         * Object obj
         * id 1 -- 回调KeyboardView的backkey,object 为 null
         * id 2 -- 回调多文本框数值List<String>
         * id 3 -- 返回listview 的item点击条项 object int类型 position
         * id 4 --KeyboardEditWindowManager的回调 object == null
         * id 5 --回调映射 object==boolean 开启:true,断开/未开启:false
         *
         * id--10001保存配置之后告诉
         * id--10002AOA接口数据分发
         * id--10004悬浮窗口关闭通知
         * */
        //log("debugr listener size:"+mCallBacks.size());
        for (INormalBack callback : mCallBacks) {
            //log("debugr notify:"+callback);
            callback.back(id, obj);
        }
    }

    public static void popWindow(final Context context, final String title, final String msg, final Runnable okTask, final Runnable noTask, final String ok, final String no, final boolean isHideno, int boundColor) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                final View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
                ((TextView) view.findViewById(R.id.dialogmsgtitle)).setText(title);
                ((TextView) view.findViewById(R.id.dialogmsgmsg)).setText(msg);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                if (ok != null) {
                    ((Button) view.findViewById(R.id.dialogmsgyes)).setText(ok);
                }
                if (no != null) {
                    ((Button) view.findViewById(R.id.dialogmsgno)).setText(no);
                }
                if (isHideno) {
                    view.findViewById(R.id.dialogmsgno).setVisibility(View.GONE);
                }

                view.setBackgroundResource(R.mipmap.bg_black);
                view.findViewById(R.id.dialogmsgyes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        if (okTask == null) {
                            return;
                        }
                        ((Activity) context).runOnUiThread(okTask);
                    }
                });
                view.findViewById(R.id.dialogmsgno).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        if (noTask == null) {
                            return;
                        }
                        ((Activity) context).runOnUiThread(noTask);
                    }
                });
                alertDialog.getWindow().setWindowAnimations(R.style.popwindow);
                alertDialog.show();
                Window window = alertDialog.getWindow();
                window.setBackgroundDrawableResource(android.R.color.transparent);

                window.setLayout(9 * zoomy / 20, zoomx / 2);
                window.setContentView(view);
            }
        });
    }

    public static void addMsgtoTop(final Context context, final String title, final String msg, final Runnable okTask, final Runnable noTask, final boolean isHideNo) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final View view = LayoutInflater.from(context).inflate(R.layout.dialog_message, null);
                ((TextView) view.findViewById(R.id.dialogmsgtitle)).setText(title);
                ((TextView) view.findViewById(R.id.dialogmsgmsg)).setText(msg);


                view.findViewById(R.id.dialogmsgyes).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (okTask != null) {
                            new Handler(Looper.getMainLooper()).post(okTask);
                        }
                        KeyboardEditWindowManager.getInstance().removeView(view);


                    }
                });

                if (isHideNo) {
                    view.findViewById(R.id.dialogmsgno).setVisibility(View.GONE);
                }

                view.findViewById(R.id.dialogmsgno).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (noTask != null) {
                            new Handler(Looper.getMainLooper()).post(noTask);
                        }
                        KeyboardEditWindowManager.getInstance().removeView(view);


                    }
                });

                KeyboardEditWindowManager.getInstance().init(context).addView(view, (9 * SimpleUtil.zoomy) / 20, (2 * SimpleUtil.zoomx) / 3);
            }
        });
    }

    public static void addMsgtoTopNoRes(final Context context, final String title, final String msg) {
        addMsgtoTop(context, title, msg, null, null, true);
    }
    public static String getCurTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return simpleDateFormat.format(new Date());
    }
    public static void toastTop(Context context, String msg) {
        addMsgBottomToTop(context, msg, true);
    }

    public static byte sumCheck(byte[] data) {
        byte result = 0;
        int len = data.length;
        for (int i = 0; i < len; i++) {
            result = (byte) (result + data[i]);
        }
        return result;
    }

    public static void addEditToTop(Context context, String title, LinkedHashMap<String, String> items, final Runnable okTask, final Runnable noTask, final INormalBack iNormalBack) {
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit, null);
        ((TextView) view.findViewById(R.id.dialogmsgtitle)).setText(title);

        LinearLayout parent = view.findViewById(R.id.dialog_edit_parent);

        Iterator<String> it = items.keySet().iterator();
        String key = null, value = null, hint = null;
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 5f);
        param1.gravity = Gravity.CENTER;
        LinearLayout.LayoutParams param2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 7f);

        View.OnKeyListener keyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    KeyboardEditWindowManager.getInstance().removeView(view);
                }
                return false;
            }
        };

        final List<EditText> editTexts = new ArrayList<>();
        while (it.hasNext()) {
            key = it.next();
            value = items.get(key);
            LinearLayout subLayout = new LinearLayout(context);
            subLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView left = new TextView(context);
            left.setSingleLine();
            left.setGravity(Gravity.CENTER);
            left.setTextColor(Color.WHITE);
            left.setTextSize(20);
            left.setEnabled(false);
            left.setText(key);


            EditText right = new EditText(context);
            right.setTextSize(16);
            right.setTextColor(Color.WHITE);
            right.setFocusable(true);
            right.setSingleLine();
            right.setFocusableInTouchMode(true);
            right.setOnKeyListener(keyListener);


            right.setSingleLine();
            if (value != null) {
                right.setText(value);
            } else if (hint != null) {
                right.setHint(hint);
            }
            editTexts.add(right);
            subLayout.addView(left, param1);
            subLayout.addView(right, param2);
            parent.addView(subLayout);

            hint = null;

        }
        view.findViewById(R.id.dialogmsgyes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (okTask != null) {
                    runOnUIThread(okTask);
                }

                List<String> backTexts = new ArrayList<>();
                for (EditText editText : editTexts) {
                    backTexts.add(editText.getText().toString());
                }
                iNormalBack.back(2, backTexts);

                //KeyboardEditWindowManager.getInstance().recycle();

            }
        });
        view.findViewById(R.id.dialogmsgno).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noTask != null) {
                    runOnUIThread(noTask);
                }
                KeyboardEditWindowManager.getInstance().close();
                //KeyboardEditWindowManager.getInstance().recycle();
            }
        });
        KeyboardEditWindowManager.getInstance().init(context).addView(view, (9 * SimpleUtil.zoomy) / 20, (2 * SimpleUtil.zoomx) / 3);
    }

    public static void runOnThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void runOnUIThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static void runOnUIThread(Runnable runnable, int time) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, time);
    }

    public static void addRadioGrouptoTop(Context context, String title, List<String> items, final List<Runnable> runnables, final Runnable okTask, final Runnable noTask) {
        final View saveView = LayoutInflater.from(context).inflate(R.layout.dialog_radiogroup, null);
        ((TextView) saveView.findViewById(R.id.dialogmsgtitle)).setText(title);
        final RadioGroup radioGroup = saveView.findViewById(R.id.dialog_radiogroup_group);
        for (int i = 0; i < items.size(); i++) {

            RadioButton radioButton = (RadioButton) LayoutInflater.from(context).inflate(R.layout.radiogroup_radio, null);
            LinearLayout.LayoutParams param_ = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            param_.topMargin = 15;
            if (i != 0) {
                radioButton.setLayoutParams(param_);
            }
            radioButton.setText(items.get(i));
            radioButton.setId(radioGroup.getId() + i + 0x1111);
            radioButton.setTextColor(context.getResources().getColor(R.color.mk_white));
            //low api exception
            /*Drawable drawable = context.getResources().getDrawable(R.drawable.correctorerror_empty);
            radioButton.setCompoundDrawablePadding(20);
            radioButton.setButtonDrawable(null);
            radioButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);*/
            radioGroup.addView(radioButton);
            if (i == 0) {
                radioButton.setChecked(true);
            }
        }

        View child = radioGroup.getChildAt(0);
        child.measure(0, 0);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        //params.topMargin = child.getMeasuredHeight()+5;
        radioGroup.setLayoutParams(params);


        saveView.findViewById(R.id.dialogmsgyes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (okTask != null) {
                    runOnUIThread(okTask);
                }
                KeyboardEditWindowManager.getInstance().removeView(saveView);
                if ((radioGroup.getCheckedRadioButtonId() - radioGroup.getId() - 0x1111) < 0) {
                    return;
                }
                SimpleUtil.runOnUIThread(runnables.get(radioGroup.getCheckedRadioButtonId() - radioGroup.getId() - 0x1111));
            }
        });
        saveView.findViewById(R.id.dialogmsgno).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noTask != null) {
                    runOnUIThread(noTask);
                }
                KeyboardEditWindowManager.getInstance().removeView(saveView);
                // KeyboardEditWindowManager.getInstance().removeView(parent);
                //KeyboardEditWindowManager.getInstance().recycle();
            }
        });
        KeyboardEditWindowManager.getInstance().addView(saveView, (9 * SimpleUtil.zoomy) / 20, (2 * SimpleUtil.zoomx) / 3);
    }



    public static void addMsgBottomToTop(final Context context, final String msg, final boolean ERROR) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final View view = LayoutInflater.from(context).inflate(R.layout.dialog_msgtoptoast, null);
                ((TextView) view.findViewById(R.id.msgtotop_tv)).setText(msg);
                ((TextView) view.findViewById(R.id.msgtotop_tv)).setTextColor(ERROR ? Color.RED : Color.GREEN);
                view.setBackgroundResource(ERROR ? R.mipmap.msgtotop_bg_red : R.mipmap.msgtotop_bg_green);


                ScaleAnimation scaleAnimation_show = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation_show.setDuration(300);
                view.startAnimation(scaleAnimation_show);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 20;
                params.gravity = Gravity.CENTER;
                WrapFloat.getInstance(context).addView(view, params);
                SimpleUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        ScaleAnimation scaleAnimation_close = new ScaleAnimation(1f, 0f, 1f, 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation_close.setDuration(300);
                        view.startAnimation(scaleAnimation_close);
                        WrapFloat.getInstance(context).removeView(view);
                    }
                }, 2000);
            }
        });

    }

    private static View waitViewTop;
    private static TextView waittingMsg;

    public static void addWaitToTop(final Context context, final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (waitViewTop != null) {
                    log("已经存在等待窗口，则关闭重新建立");
                    resetWaitTop(context);
                }
                waitViewTop = LayoutInflater.from(context).inflate(R.layout.waiting, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(zoomy / 3, 4 * (zoomx / 5));
                waitViewTop.setLayoutParams(layoutParams);
                waittingMsg = waitViewTop.findViewById(R.id.waitingmsg);
                waittingMsg.setText(message);
                KeyboardEditWindowManager.getInstance().init(context).addView(waitViewTop, (9 * SimpleUtil.zoomy) / 20, (2 * SimpleUtil.zoomx) / 3);
            }
        });
    }

    public static void updateWaitTopMsg(final String msg) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (waitViewTop == null) {
                    return;
                }
                waittingMsg.setText(msg);
            }
        });
    }

    public static void resetWaitTop(final Context context) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                KeyboardEditWindowManager.getInstance().init(context).removeView(waitViewTop);
                waitViewTop = null;
                waittingMsg = null;
            }
        });
    }

    private static AlertDialog waiting;

    public static void showWaiting(final Context context, final String message) {
        if (waiting != null || !(context instanceof Activity)) {
            return;
        }

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final View view = LayoutInflater.from(context).inflate(R.layout.waiting, null);
                view.setBackgroundResource(R.mipmap.bg_black);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(zoomy / 3, 4 * (zoomx / 5));
                view.setLayoutParams(layoutParams);
                waittingMsg = view.findViewById(R.id.waitingmsg);
                waittingMsg.setText(message);
                waiting = new AlertDialog.Builder(context, R.style.dialog2).create();
                waiting.setCanceledOnTouchOutside(false);
                waiting.setCancelable(false);

                waiting.show();
                Window window = waiting.getWindow();
                window.setBackgroundDrawableResource(android.R.color.transparent);
                window.setLayout(9 * zoomy / 20, zoomx / 2);
                window.setContentView(view);
            }
        });


    }

    public static void updateWaiting(Activity activity, final String msg) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (waiting == null || !waiting.isShowing() || waittingMsg == null) {
                    return;
                }
                waittingMsg.setText(msg);
            }
        });

    }

    public static void closeDialog(Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (waiting != null && waiting.isShowing()) {
                    waiting.dismiss();
                    waittingMsg = null;
                    waiting = null;
                }
            }
        });

    }

    public static boolean isOfficialConfig(String gameconfig) {
        return gameconfig.endsWith("[官方]") || gameconfig.endsWith("[Official]");
    }

    public static boolean isZh(Context context) {
        Locale locale = context.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

    public static String zhChange(Context context, String str) {
        if (!SimpleUtil.isZh(context)) {
            return str.replace("[官方]", "[Official]")
                    .replace("小米枪战", "Millet gunfight")
                    .replace("荒野行动", "Wilderness action")
                    .replace("穿越火线", "Cross Fire")
                    .replace("终结者", "Terminator")
                    .replace("丛林法则", "Law of the jungle")
                    .replace("光荣使命", "Passion Leads Army")
                    .replace("绝地求生之全军出击", "PUBG Tecent")
                    .replace("绝地求生之刺激战场", "PUBG");
        }
        return str;
    }

    public static void anim_shake(final View view) {

        Animation rotateAnim = new RotateAnimation(-5, 5, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        rotateAnim.setDuration(100);
        rotateAnim.setRepeatMode(Animation.REVERSE);
        rotateAnim.setRepeatCount(10000);
        view.startAnimation(rotateAnim);
    }

    public static byte[] getBytesWithSumCheck(byte[] data) {
        byte sumCheck = sumCheck(data);
        ByteArrayList bytes = new ByteArrayList(data);
        bytes.add(sumCheck);
        return bytes.all2Bytes();
    }
    public static String entozhChange(Context context, String str) {
        if (!SimpleUtil.isZh(context)) {
            return
                    str.replace("Millet gunfight", "小米枪战")
                            .replace("Wilderness action", "荒野行动")
                            .replace("Cross Fire", "穿越火线")
                            .replace("Terminator", "终结者")
                            .replace("Law of the jungle", "丛林法则")
                            .replace("Passion Leads Army", "光荣使命")
                            .replace("PUBG Tecent", "绝地求生之全军出击")
                            .replace("PUBG", "绝地求生之刺激战场");
        }
        return str;
    }
    public enum APPUSER {
        WISEGA, FPS, AGP
    }

}
