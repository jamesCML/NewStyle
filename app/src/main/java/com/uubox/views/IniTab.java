package com.uubox.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import com.uubox.adapters.MoveConfigAdapter;
import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.BigKeyConfigAd;
import com.uubox.tools.ByteArrayList;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.Hex;
import com.uubox.tools.IniAdapter;
import com.uubox.tools.InjectUtil;
import com.uubox.tools.SimpleUtil;


public class IniTab {
    private Context mContext;
    private View parent;
    private KeyboardView mKeyboardView;
    private CopyOnWriteArraySet<IButtonMenuCallback> mCallbacks = new CopyOnWriteArraySet<>();
    private ViewPager mViewPage;
    private LinearLayout mBTBar;
    private List<View> mViewPageList;
    private int mIndex;
    private Button mLastPress;
    private IniTab() {

    }

    public static final IniTab getInstance() {

        return IniTab.Holder.INI_MENU;
    }

    private static class Holder {
        private static final IniTab INI_MENU = new IniTab();
    }

    public IniTab init(Context context, KeyboardView keyboardView) {

        mContext = context;
        mKeyboardView = keyboardView;
        mViewPageList = new ArrayList<>();
        mViewPageList.clear();
        mIndex = 0;
        mLastPress = null;
        parent = LayoutInflater.from(mContext).inflate(R.layout.initable, null);
        mViewPage = parent.findViewById(R.id.initab_page);
        mBTBar = parent.findViewById(R.id.initab_btbar);

        //addKeyInit();
        WriteConfigs();
        addHelper();
        mViewPage.setAdapter(pagerAdapter);
        mViewPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (mLastPress != null) {
                    mLastPress.setTextColor(mContext.getResources().getColor(R.color.grey_2));
                }
                ((Button) mBTBar.getChildAt(position)).setTextColor(mContext.getResources().getColor(R.color.holo_green_dark));
                mLastPress = (Button) mBTBar.getChildAt(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        parent.findViewById(R.id.setting_close).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SimpleUtil.notifyall_(10014, null);//通知我要关闭设置窗口了
                    KeyboardEditWindowManager.getInstance().removeTop();
                }
                return true;
            }
        });
        return Holder.INI_MENU;
    }

    public void show() {
        SimpleUtil.log("show the Initab");
        KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(false);
        KeyboardEditWindowManager.getInstance().addView(parent, (7 * SimpleUtil.zoomy) / 8, (7 * SimpleUtil.zoomx) / 8);
    }

    public void addNotify(IniTab.IButtonMenuCallback iButtonMenuCallback) {
        mCallbacks.add(iButtonMenuCallback);
    }

    public void removeNotify(IniTab.IButtonMenuCallback iButtonMenuCallback) {
        mCallbacks.remove(iButtonMenuCallback);
    }

    public void notifyAllEx(int type, Object carryData) {
        for (IniTab.IButtonMenuCallback callback : mCallbacks) {
            callback.back(type, carryData);
        }
    }

    /**
     * 0:回调回复默认配置
     */
    public interface IButtonMenuCallback {
        void back(int type, Object carryData);
    }


    private void WriteConfigs() {

        String gloabkeyconfig = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
        final String[] sp0 = gloabkeyconfig.split("#Z%W#", -1);
        SimpleUtil.log("test当前使用:" + sp0[1] + "\n" + gloabkeyconfig);
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_oversize, null);
        final View listPar = view.findViewById(R.id.dialog_oversize_list_par);
        final View gunPar = view.findViewById(R.id.dialog_oversize_gun_par);
        final List<AOAConfigTool.Config> configsLeftData = new ArrayList<>();
        final List<AOAConfigTool.Config> configsRightData = new ArrayList<>();
        final List<AOAConfigTool.Config> configCopyRight = new ArrayList<>();
        boolean isMatch = AOAConfigTool.getInstance(mContext).AnysLeftRihgtConfigs(configsLeftData, configsRightData);
        for (AOAConfigTool.Config config : configsRightData) {
            configCopyRight.add((AOAConfigTool.Config) config.clone());
        }
        if (!isMatch && AOAConfigTool.getInstance(mContext).isAOAConnect()) {
            SimpleUtil.addMsgBottomToTop(mContext, "当前配置与设备配置不匹配！请重新写入配置！", true);
        }
        for (AOAConfigTool.Config config : configsRightData) {
            if (config.getIsUsed()) {
                SimpleUtil.notifyall_(10013, config);
                break;
            }
        }
        SimpleUtil.log(configsRightData.size() + "");
        final int[] rightSize = {0};

        ListView listLeft = view.findViewById(R.id.dialog_oversize_left);
        ListView listRight = view.findViewById(R.id.dialog_oversize_right);

        final MoveConfigAdapter adapterleft = new MoveConfigAdapter(mContext, configsLeftData);
        final MoveConfigAdapter adapterRight = new MoveConfigAdapter(mContext, configsRightData);
        listLeft.setAdapter(adapterleft);
        listRight.setAdapter(adapterRight);
        listRight.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!AOAConfigTool.getInstance(mContext).isAOAConnect()) {
                    SimpleUtil.addMsgBottomToTop(mContext, "请先连接设备再调整配置！", true);
                    return;
                }
                if (configsRightData.get(position).getIsUsed()) {
                    return;
                }
                for (AOAConfigTool.Config config1 : configsRightData) {
                    if (config1.getIsUsed()) {
                        config1.setmIsUsed(false);
                    }
                }

                String gamesha = configsRightData.get(position).getmTabValue();
                SimpleUtil.log("item select:" + gamesha);
                configsRightData.get(position).setmIsUsed(true);
                adapterRight.notifyDataSetChanged();
            }
        });
        listLeft.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (configsLeftData.get(position).getmConfigName().endsWith("[官方]")) {
                    SimpleUtil.addMsgBottomToTop(mContext, "不能删除官方配置", true);
                    return true;
                }

                String configIDs = (String) SimpleUtil.getFromShare(mContext, "ini", "configsID", String.class, "");

                int configID_ = (Integer) SimpleUtil.getFromShare(mContext, configsLeftData.get(position).getmTabValue(), "configID", int.class);
                byte[] ids = Hex.parse(configIDs);
                for (int i = 0; i < 100; i++) {
                    if (ids[i] == configID_) {
                        ids[i] = 0;
                        SimpleUtil.saveToShare(mContext, "ini", "configsID", Hex.toString(ids));
                        //SimpleUtil.saveToShare(mContext,mSpFileName,"configID",(int)ids[i]);
                        break;
                    }
                }


                SharedPreferences shareLib = mContext.getSharedPreferences(configsLeftData.get(position).getmContent() + "_table", 0);
                boolean res = shareLib.edit().remove(configsLeftData.get(position).getmTabKey()).commit();
                SimpleUtil.log("delete iniconfig result:" + res);
                SimpleUtil.addMsgBottomToTop(mContext, "删除" + (res ? "成功" : "失败"), !res);

                //如果已经全部删除则删除游戏目录

                if (shareLib.getAll().size() == 0) {
                    File file = new File("/data/data/" + CommonUtils.getAppPkgName(mContext) + "/shared_prefs", configsLeftData.get(position).getmContent() + "_table.xml");
                    if (file.exists()) {
                        file.delete();
                        SimpleUtil.delFromShare(mContext, "KeysConfigs", configsLeftData.get(position).getmContent());
                        SimpleUtil.addMsgBottomToTop(mContext, "目录[" + configsLeftData.get(position).getmContent() + "]已清除", false);
                        SimpleUtil.log("have delete the content " + configsLeftData.get(position).getmContent());
                    }
                }
                configsLeftData.remove(position);
                adapterleft.notifyDataSetChanged();
                return true;
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


        final SimpleUtil.INormalBack iNormalBack = new SimpleUtil.INormalBack() {
            @Override
            public void back(int id, final Object obj) {
                if (id < 10007 && id > 10014) {
                    return;
                }
                if (!AOAConfigTool.getInstance(mContext).isAOAConnect()) {
                    if (id == 10014) {
                        SimpleUtil.removeINormalCallback(this);
                    } else {
                        SimpleUtil.addMsgBottomToTop(mContext, "请先连接设备再调整配置！", true);
                    }
                    return;
                }
                if (id == 10007) {//取消一个配置
                    AOAConfigTool.Config config = (AOAConfigTool.Config) obj;

                    if (config.getIsUsed()) {
                        SimpleUtil.addMsgBottomToTop(mContext, "正在使用的配置不能取消！", true);
                        return;
                    }
                    SimpleUtil.saveToShare(mContext, config.getmTabValue(), "isDelete", true);
                    config.setDeleted(true);
                    configsLeftData.add(config);
                    configsRightData.remove(obj);
                    rightSize[0] -= config.getmSize();
                    adapterleft.notifyDataSetChanged();
                    adapterRight.notifyDataSetChanged();

                } else if (id == 10008) {//增加一个配置
                    if (configsRightData.size() == 4) {
                        SimpleUtil.addMsgBottomToTop(mContext, "当前最多支持写4个配置！", true);
                        return;
                    }
                    AOAConfigTool.Config config = (AOAConfigTool.Config) obj;
                    config.setDeleted(false);
                    SimpleUtil.saveToShare(mContext, config.getmTabValue(), "isDelete", false);
                    configsRightData.add(config);
                    configsLeftData.remove(obj);
                    rightSize[0] += config.getmSize();
                    adapterleft.notifyDataSetChanged();
                    adapterRight.notifyDataSetChanged();
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
                    adapterleft.notifyDataSetChanged();
                    adapterRight.notifyDataSetChanged();
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
                    adapterleft.notifyDataSetChanged();
                    adapterRight.notifyDataSetChanged();
                } else if (id == 10012)//配置写入完成通知结果
                {
                    SimpleUtil.removeINormalCallback(this);
                    SimpleUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            byte[] c0Data = (byte[]) obj;
                            SimpleUtil.notifyall_(10013, configsRightData.get(c0Data[3] - 1));
                            KeyboardEditWindowManager.getInstance().close();

                        }
                    });

                } else if (id == 10014) {
                    if (configCopyRight.size() != configsRightData.size()) {
                        SimpleUtil.addMsgBottomToTop(mContext, "检测到配置更新！自动写入！", false);
                        view.findViewById(R.id.dialog_oversize_write).performClick();
                    } else {
                        for (int i = 0; i < configCopyRight.size(); i++) {
                            if (!configCopyRight.get(i).equals(configsRightData.get(i)) || configCopyRight.get(i).getIsUsed() != configsRightData.get(i).getIsUsed()) {
                                SimpleUtil.addMsgBottomToTop(mContext, "检测到配置更新！自动写入！", false);
                                view.findViewById(R.id.dialog_oversize_write).performClick();
                                break;
                            }
                        }
                    }
                    SimpleUtil.removeINormalCallback(this);
                }

            }
        };
        SimpleUtil.addINormalCallback(iNormalBack);

        view.findViewById(R.id.dialog_oversize_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rightSize[0] > 1024) {
                    SimpleUtil.addMsgBottomToTop(mContext, "配置过大！", true);
                    return;
                }

                int bqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "bqNum", int.class, 25);
                int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "cfqNum", int.class, 19);
                int akNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "akNum", int.class, 28);
                SimpleUtil.log("压枪灵敏度：" + bqNum + "," + cfqNum + "," + akNum);
                for (AOAConfigTool.Config config : configsRightData) {
                    if (config.getIsUsed()) {
                        //压枪数据重新构造一下
                        byte[] data = config.getmData().all2Bytes();
                        data[32] = (byte) bqNum;
                        data[33] = (byte) cfqNum;
                        data[34] = (byte) akNum;
                        byte[] data2 = Arrays.copyOfRange(data, 1, data.length);
                        ByteArrayList bytes = new ByteArrayList();
                        bytes.add(SimpleUtil.sumCheck(data2));
                        bytes.add(data2);
                        config.setmData(bytes);
                        break;
                    }
                }

                AOAConfigTool.getInstance(mContext).writeManyConfigs(configsRightData);

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
                        SimpleUtil.saveToShare(mContext, sp0[2], "bqNum", progress);
                        break;
                    case R.id.dialog_oversize_gun_cfq:
                        cfq.setText("类型:冲锋枪  开启快捷键:F2+1  关闭快捷键:Esc+2 灵敏度:" + progress);
                        SimpleUtil.saveToShare(mContext, sp0[2], "cfqNum", progress);
                        break;
                    case R.id.dialog_oversize_gun_ak:
                        ak.setText("类型:AK47  开启快捷键:F3+1  关闭快捷键:Esc+3 灵敏度:" + progress);
                        SimpleUtil.saveToShare(mContext, sp0[2], "akNum", progress);
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
        int bqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "bqNum", int.class, 25);
        int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "cfqNum", int.class, 19);
        int akNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "akNum", int.class, 28);
        SimpleUtil.log("获取存储的压枪值:" + bqNum + "." + cfqNum + "," + akNum);
        bqBar.setProgress(bqNum - 1);
        cfqBar.setProgress(cfqNum - 1);
        akBar.setProgress(akNum - 1);
        bq.setText("类型:步枪  开启快捷键:F1+1  关闭快捷键:Esc+1 灵敏度:" + bqNum);
        cfq.setText("类型:冲锋枪  开启快捷键:F2+1  关闭快捷键:Esc+2 灵敏度:" + cfqNum);
        ak.setText("类型:AK47  开启快捷键:F3+1  关闭快捷键:Esc+3 灵敏度:" + akNum);

        addItem("写入配置");
        mViewPageList.add(view);

        //KeyboardEditWindowManager.getInstance().init(mContext).addView(view, (7 * SimpleUtil.zoomy) / 8, (7 * SimpleUtil.zoomx) / 8);
    }

    private void addHelper() {
        ViewGroup helperitem = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.dialog_titlelist, null);
        ((View) helperitem.findViewById(R.id.dialogmsgyes).getParent()).setVisibility(View.GONE);
        ((TextView) helperitem.findViewById(R.id.titlelist_title)).setText("帮助文档(" + CommonUtils.getAppVersionName(mContext) + ")");
        ListView listView = helperitem.findViewById(R.id.titlelist_list);
        String[] items = mContext.getResources().getStringArray(R.array.menu_ini_help_items);
        final List<String> data = new ArrayList<>();
        for (String item : items) {
            data.add(item);
        }
        final String[] msgs = mContext.getResources().getStringArray(R.array.menu_ini_help_contents);
        ArrayAdapter arrayAdapter = new ArrayAdapter(mContext, R.layout.list_simple_item, data);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewGroup helperitem = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.inject_helper, null);
                ((TextView) helperitem.findViewById(R.id.inject_helper_title)).setText(data.get(position));
                ((EditText) helperitem.findViewById(R.id.inject_helper_edit)).setText(msgs[position]);
                helperitem.findViewById(R.id.frame_close).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        KeyboardEditWindowManager.getInstance().removeTop();
                    }
                });
                KeyboardEditWindowManager.getInstance().addView(helperitem, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);
            }
        });

        addItem("帮助");
        mViewPageList.add(helperitem);


    }

    private void addItem(final String item) {
        final Button button = new Button(mContext);
        button.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
        button.setTextColor(mIndex == 0 ? mContext.getResources().getColor(R.color.holo_green_dark) : mContext.getResources().getColor(R.color.grey_2));
        button.setText(item);
        button.setTag(mIndex);
        button.setTextSize(20);
        button.measure(0, 0);
        mBTBar.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastPress != null) {
                    mLastPress.setTextColor(mContext.getResources().getColor(R.color.grey_2));
                }
                button.setTextColor(mContext.getResources().getColor(R.color.holo_green_dark));
                mLastPress = button;
                mViewPage.setCurrentItem((Integer) button.getTag(), false);
            }
        });
        mIndex++;
        if (mLastPress == null) {
            mLastPress = button;
        }
    }

    private PagerAdapter pagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return mViewPageList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mViewPageList.get(position));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(mViewPageList.get(position));
            return mViewPageList.get(position);
        }
    };

}
