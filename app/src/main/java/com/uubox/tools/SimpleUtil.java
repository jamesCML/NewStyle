package com.uubox.tools;

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
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import com.uubox.adapters.OverSizeAdapter;
import com.uubox.padtool.R;
import com.uubox.views.KeyboardEditWindowManager;
import com.uubox.views.WrapFloat;

public class SimpleUtil {
    public static boolean DEBUG;
    public static int zoomx;
    public static int zoomy;
    public static int versionCode = 10;
    public static boolean isSaveToXml = false;
    public static boolean screenstate;
    public static boolean mAOAInjectEable;

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
            String value_int = (Integer) value + "";
            //SimpleUtil.log("saveToShare:"+key+"->"+value_int);
            return edit.putInt(key, (Integer) value).commit();
        } else if (value instanceof Long) {
            String value_int = (Long) value + "";
            return edit.putLong(key, (Long) value).commit();
        } else if (value instanceof String) {
            String value_int = (String) value;
            return edit.putString(key, (String) value).commit();
        } else if (value instanceof Boolean) {
            String value_int = (Boolean) value + "";
            return edit.putBoolean(key, (Boolean) value).commit();
        } else
            return false;
    }

    public static boolean saveToShare(SharedPreferences.Editor edit, String key, Object value) {

        if (value instanceof Integer) {
            String value_int = (Integer) value + "";
            //SimpleUtil.log("saveToShare:"+key+"->"+value_int);
            return edit.putInt(key, (Integer) value).commit();
        } else if (value instanceof Long) {
            String value_int = (Long) value + "";
            return edit.putLong(key, (Long) value).commit();
        } else if (value instanceof String) {
            String value_int = (String) value;
            return edit.putString(key, (String) value).commit();
        } else if (value instanceof Boolean) {
            String value_int = (Boolean) value + "";
            return edit.putBoolean(key, (Boolean) value).commit();
        } else
            return false;
    }

    private static AES aes = new AES();

    public static AES getAES() {
        return aes;
    }

    public static SharedPreferences.Editor editSaveToShare(SharedPreferences.Editor edit, String key, Object value) {
        //AES aes = new AES();
        if (value instanceof Integer) {
            String value_int = (Integer) value + "";
            //SimpleUtil.log("saveToShare:"+key+"->"+value_int);
            return edit.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            String value_int = (Long) value + "";
            return edit.putLong(key, (Long) value);
        } else if (value instanceof String) {
            String value_int = (String) value;
            return edit.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            String value_int = (Boolean) value + "";
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

        if (!DEBUG) {
            return;
        }
        Log.i("CJLOG", msg);
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
        mCallBacks.add(callback);
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
        for (INormalBack callback : mCallBacks) {
            callback.back(id, obj);
        }
    }

    public static void addMsgtoTop(Context context, String title, String msg, final Runnable okTask, final Runnable noTask, boolean isHideNo) {
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

        KeyboardEditWindowManager.getInstance().init(context).addView(view, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);
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
        KeyboardEditWindowManager.getInstance().addView(view, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);
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
        KeyboardEditWindowManager.getInstance().addView(saveView, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);
    }

    public static void addOverSizetoTop(final Context context, final List<AOADataPack.Config> configs, final int size, final Runnable okTask, final Runnable noTask) {
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_oversize, null);
        GridView gridView = view.findViewById(R.id.dialog_oversize_grid);
        final TextView textView = view.findViewById(R.id.dialog_oversize_title);
        textView.setText(size > 1024 ? "写入配置数量过大(大于1024)，请移除一些:" + size : "可以写入(小于等于1024):" + size);
        textView.setTextColor(size <= 1024 ? Color.GREEN : Color.RED);
        ((TextView) view.findViewById(R.id.dialogmsgyes)).setText("写入");
        ((TextView) view.findViewById(R.id.dialogmsgno)).setText("放弃");
        final OverSizeAdapter overSizeAdapter = new OverSizeAdapter(context, configs);
        gridView.setAdapter(overSizeAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    addMsgBottomToTop(context, "默认配置不能移除！", true);
                    return;
                }

                configs.get(position).setDeleted(!configs.get(position).getIsDeleted());
                String title = textView.getText().toString();
                String[] sp = title.split(":");
                int curSize = (Integer.parseInt(sp[1]) + (configs.get(position).getIsDeleted() ? -configs.get(position).getmSize() : configs.get(position).getmSize()));


                textView.setTextColor(curSize <= 1024 ? Color.GREEN : Color.RED);
                if (curSize <= 1024) {
                    textView.setText("可以写入(小于等于1024):" + curSize);
                    textView.setTextColor(Color.GREEN);
                } else {
                    textView.setText("写入配置数量过大(大于1024)，请移除一些:" + curSize);
                    textView.setTextColor(Color.RED);
                }
                overSizeAdapter.notifyDataSetChanged();
            }
        });
        view.findViewById(R.id.dialogmsgyes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = textView.getText().toString();
                String[] sp = title.split(":");
                if (Integer.parseInt(sp[1]) > 1024) {
                    addMsgBottomToTop(context, "写入配置过大！", true);
                    return;
                }

                if (okTask != null) {
                    runOnUIThread(okTask);
                }
                KeyboardEditWindowManager.getInstance().removeView(view);

            }
        });
        view.findViewById(R.id.dialogmsgno).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noTask != null) {
                    runOnUIThread(noTask);
                }
                KeyboardEditWindowManager.getInstance().removeView(view);

            }
        });
        KeyboardEditWindowManager.getInstance().init(context).addView(view, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);
    }

    public static void addMsgBottomToTop(final Context context, final String msg, final boolean ERROR) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final View view = LayoutInflater.from(context).inflate(R.layout.dialog_msgtoptoast, null);
                ((TextView) view.findViewById(R.id.msgtotop_tv)).setText(msg);
                ((TextView) view.findViewById(R.id.msgtotop_tv)).setTextColor(ERROR ? Color.RED : Color.GREEN);
                view.setBackgroundResource(ERROR ? R.mipmap.msgtotop_bg_red : R.mipmap.msgtotop_bg_green);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.BOTTOM | Gravity.CENTER;

              /*  View topView = KeyboardEditWindowManager.getInstance().init(context).getTopView();
                if (topView != null && topView.getId() == R.id.dialog_msgbottom_par) {
                    KeyboardEditWindowManager.getInstance().removeView(topView);
                }*/
                ScaleAnimation scaleAnimation_show = new ScaleAnimation(0f, 1f, 0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                scaleAnimation_show.setDuration(300);
                view.startAnimation(scaleAnimation_show);
                WrapFloat.getInstance(context).addView(view);
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
                    return;
                }
                waitViewTop = LayoutInflater.from(context).inflate(R.layout.waiting, null);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(zoomy / 3, 4 * (zoomx / 5));
                waitViewTop.setLayoutParams(layoutParams);
                waittingMsg = waitViewTop.findViewById(R.id.waitingmsg);
                waittingMsg.setText(message);
                KeyboardEditWindowManager.getInstance().init(context).addView(waitViewTop, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);
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

    public static void resetWaitTop() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                KeyboardEditWindowManager.getInstance().removeView(waitViewTop);
                waitViewTop = null;
                waittingMsg = null;
            }
        });
    }
}
