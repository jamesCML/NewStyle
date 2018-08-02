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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import com.uubox.adapters.MoveConfigAdapter;
import com.uubox.padtool.R;
import com.uubox.views.KeyboardEditWindowManager;
import com.uubox.views.WrapFloat;

public class SimpleUtil {
    public static boolean DEBUG;
    public static int zoomx;
    public static int zoomy;
    public static int versionCode = 10;
    public static boolean isSaveToXml = true;
    public static boolean screenstate;
    public static boolean mAOAInjectEable;
    public static int LIUHAI;
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

        if (!DEBUG) {
            return;
        }
        //SocketLogEx.getInstance().sendLog(msg);
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
        KeyboardEditWindowManager.getInstance().init(context).addView(view, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);
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

    public static void test(final Context context, final List<AOAConfigTool.Config> allConfigs) {
        String gloabkeyconfig = (String) SimpleUtil.getFromShare(context, "ini", "gloabkeyconfig", String.class, "");
        final String[] sp0 = gloabkeyconfig.split("#Z%W#", -1);
        SimpleUtil.log("test当前使用:" + sp0[1] + "\n" + gloabkeyconfig);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_oversize, null);
        final View listPar = view.findViewById(R.id.dialog_oversize_list_par);
        final View gunPar = view.findViewById(R.id.dialog_oversize_gun_par);
        final TextView rightMsg = view.findViewById(R.id.dialog_oversize_rightmsg);
        final List<AOAConfigTool.Config> configsLeftData = new ArrayList<>();
        final List<AOAConfigTool.Config> configsRightData = new ArrayList<>();
        final int[] rightSize = {0};
        for (AOAConfigTool.Config config : allConfigs) {
            if (config.getIsDeleted() && !config.getIsUsed()) {
                configsLeftData.add(config);
            } else {
                if (config.getIsUsed()) {
                    if (configsRightData.size() == 4) {
                        config.setDeleted(true);
                        configsLeftData.add(config);
                        configsRightData.remove(0);
                    }
                    configsRightData.add(0, config);
                    rightSize[0] += config.getmSize();
                } else if (configsRightData.size() < 4) {
                    configsRightData.add(config);
                    rightSize[0] += config.getmSize();
                }

            }
        }
        if (rightSize[0] > 1024) {
            rightMsg.setTextColor(Color.RED);
            rightMsg.setText("配置过大！");
        } else {
            rightMsg.setTextColor(Color.GREEN);
            rightMsg.setText("可以写入配置！");
        }
        ListView listLeft = view.findViewById(R.id.dialog_oversize_left);
        ListView listRight = view.findViewById(R.id.dialog_oversize_right);

        final MoveConfigAdapter adapterleft = new MoveConfigAdapter(context, configsLeftData);
        final MoveConfigAdapter adapterRight = new MoveConfigAdapter(context, configsRightData);
        listLeft.setAdapter(adapterleft);
        listRight.setAdapter(adapterRight);
        listRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        final TextView changeGunTv = view.findViewById(R.id.dialog_oversize_changetv);
        changeGunTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (changeGunTv.getText().toString().contains("压枪")) {
                    listPar.setVisibility(View.GONE);
                    gunPar.setVisibility(View.VISIBLE);
                    changeGunTv.setText("点击我跳转到配置选择列表");
                } else {
                    listPar.setVisibility(View.VISIBLE);
                    gunPar.setVisibility(View.GONE);
                    changeGunTv.setText("点击我跳转到压枪设置");
                }
            }
        });



        final INormalBack iNormalBack = new INormalBack() {
            @Override
            public void back(int id, Object obj) {
                if (id == 10007) {//取消一个配置
                    AOAConfigTool.Config config = (AOAConfigTool.Config) obj;

                    if (config.getIsUsed()) {
                        addMsgBottomToTop(context, "正在使用的配置不能取消！", true);
                        return;
                    }
                    SimpleUtil.saveToShare(context, config.getmTabValue(), "isDelete", true);
                    config.setDeleted(true);
                    configsLeftData.add(config);
                    configsRightData.remove(obj);
                    rightSize[0] -= config.getmSize();
                    if (rightSize[0] > 1024) {
                        rightMsg.setTextColor(Color.RED);
                        rightMsg.setText("配置过大！");
                    } else {
                        rightMsg.setTextColor(Color.GREEN);
                        rightMsg.setText("可以写入配置！");
                    }

                } else if (id == 10008) {//增加一个配置
                    if (configsRightData.size() == 4) {
                        addMsgBottomToTop(context, "当前最多支持写4个配置！", true);
                        return;
                    }
                    AOAConfigTool.Config config = (AOAConfigTool.Config) obj;
                    config.setDeleted(false);
                    SimpleUtil.saveToShare(context, config.getmTabValue(), "isDelete", false);
                    configsRightData.add(config);
                    configsLeftData.remove(obj);
                    rightSize[0] += config.getmSize();
                    if (rightSize[0] > 1024) {
                        rightMsg.setTextColor(Color.RED);
                        rightMsg.setText("配置过大！");
                    } else {
                        rightMsg.setTextColor(Color.GREEN);
                        rightMsg.setText("可以写入配置！");
                    }
                } else if (id == 10009) {//上
                    int position = (Integer) obj;
                    if (position == 0) {
                        return;
                    } else if (position == 1) {
                        // addMsgBottomToTop(context, "当前使用的配置必须放在第一位！", true);
                        // return;
                    }
                    SimpleUtil.log("up position:" + position);
                    configsRightData.add(position - 1, configsRightData.get(position));
                    configsRightData.remove(position + 1);
                } else if (id == 10010) {//下
                    int position = (Integer) obj;
                    if (position == configsRightData.size() - 1) {
                        return;
                    } else if (position == 0) {
                        //addMsgBottomToTop(context, "当前使用的配置必须放在第一位！", true);
                        //return;
                    }
                    SimpleUtil.log("down position:" + position);
                    configsRightData.add(position + 2, configsRightData.get(position));
                    configsRightData.remove(position);
                }
                adapterleft.notifyDataSetChanged();
                adapterRight.notifyDataSetChanged();
            }
        };
        SimpleUtil.addINormalCallback(iNormalBack);

        view.findViewById(R.id.dialog_oversize_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightSize[0] > 1024) {
                    addMsgBottomToTop(context, "配置过大！", true);
                    return;
                }

                int bqNum = (Integer) SimpleUtil.getFromShare(context, sp0[2], "bqNum", int.class, 25);
                int cfqNum = (Integer) SimpleUtil.getFromShare(context, sp0[2], "cfqNum", int.class, 19);
                int akNum = (Integer) SimpleUtil.getFromShare(context, sp0[2], "akNum", int.class, 28);
                SimpleUtil.log("压枪灵敏度：" + bqNum + "," + cfqNum + "," + akNum);
                for (AOAConfigTool.Config config : allConfigs) {
                    if (config.getIsUsed()) {
                        //压枪数据重新构造一下
                        byte[] data = config.getmData().all2Bytes();
                        data[32] = (byte) bqNum;
                        data[33] = (byte) cfqNum;
                        data[34] = (byte) akNum;
                        byte[] data2 = Arrays.copyOfRange(data, 1, data.length);
                        ByteArrayList bytes = new ByteArrayList();
                        bytes.add(sumCheck(data2));
                        bytes.add(data2);
                        config.setmData(bytes);
                        break;
                    }
                }

                KeyboardEditWindowManager.getInstance().close();
                Iterator<AOAConfigTool.Config> it = allConfigs.iterator();
                while (it.hasNext()) {
                    if (it.next().getIsDeleted()) {
                        it.remove();
                    }
                }
                SimpleUtil.notifyall_(10011, allConfigs);
            }
        });
        view.findViewById(R.id.dialog_oversize_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleUtil.removeINormalCallback(iNormalBack);
                KeyboardEditWindowManager.getInstance().close();
            }
        });

        final TextView cfq = view.findViewById(R.id.dialog_oversize_gun_cfq_tv);
        final TextView bq = view.findViewById(R.id.dialog_oversize_gun_bq_tv);
        final TextView ak = view.findViewById(R.id.dialog_oversize_gun_ak_tv);
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progress += 1;
                switch (seekBar.getId()) {

                    case R.id.dialog_oversize_gun_bq:
                        bq.setText("类型:步枪  开启快捷键:F1+1  关闭快捷键:Esc+1 灵敏度:" + progress);
                        SimpleUtil.saveToShare(context, sp0[2], "bqNum", progress);
                        break;
                    case R.id.dialog_oversize_gun_cfq:
                        cfq.setText("类型:冲锋枪  开启快捷键:F2+1  关闭快捷键:Esc+2 灵敏度:" + progress);
                        SimpleUtil.saveToShare(context, sp0[2], "cfqNum", progress);
                        break;
                    case R.id.dialog_oversize_gun_ak:
                        ak.setText("类型:AK47  开启快捷键:F3+1  关闭快捷键:Esc+3 灵敏度:" + progress);
                        SimpleUtil.saveToShare(context, sp0[2], "akNum", progress);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        SeekBar cfqBar = view.findViewById(R.id.dialog_oversize_gun_cfq);
        SeekBar bqBar = view.findViewById(R.id.dialog_oversize_gun_bq);
        SeekBar akBar = view.findViewById(R.id.dialog_oversize_gun_ak);
        akBar.setOnSeekBarChangeListener(seekBarChangeListener);
        bqBar.setOnSeekBarChangeListener(seekBarChangeListener);
        cfqBar.setOnSeekBarChangeListener(seekBarChangeListener);
        int bqNum = (Integer) SimpleUtil.getFromShare(context, sp0[2], "bqNum", int.class, 25);
        int cfqNum = (Integer) SimpleUtil.getFromShare(context, sp0[2], "cfqNum", int.class, 19);
        int akNum = (Integer) SimpleUtil.getFromShare(context, sp0[2], "akNum", int.class, 28);
        SimpleUtil.log("获取存储的压枪值:" + bqNum + "." + cfqNum + "," + akNum);
        bqBar.setProgress(bqNum - 1);
        cfqBar.setProgress(cfqNum - 1);
        akBar.setProgress(akNum - 1);
        bq.setText("类型:步枪  开启快捷键:F1+1  关闭快捷键:Esc+1 灵敏度:" + bqNum);
        cfq.setText("类型:冲锋枪  开启快捷键:F2+1  关闭快捷键:Esc+2 灵敏度:" + cfqNum);
        ak.setText("类型:AK47  开启快捷键:F3+1  关闭快捷键:Esc+3 灵敏度:" + akNum);
        KeyboardEditWindowManager.getInstance().init(context).addView(view, (7 * SimpleUtil.zoomy) / 8, (7 * SimpleUtil.zoomx) / 8);

    }

    public static void addOverSizetoTop(final Context context, final List<AOAConfigTool.Config> configs, final int size, final Runnable okTask, final Runnable noTask) {
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_oversize, null);
       /* GridView gridView = view.findViewById(R.id.dialog_oversize_grid);
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
        });*/
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
