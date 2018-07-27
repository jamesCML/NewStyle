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
import com.uubox.tools.AES;
import com.uubox.tools.AOADataPack;
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
    private List<AOADataPack.Config> mConfigs;
    private IniTab() {

    }

    public static final IniTab getInstance() {

        return IniTab.Holder.INI_MENU;
    }

    private static class Holder {
        private static final IniTab INI_MENU = new IniTab();
    }

    public IniTab init(Context context, KeyboardView keyboardView, List<AOADataPack.Config> configs) {

        mContext = context;
        mConfigs = configs;
        mKeyboardView = keyboardView;
        mViewPageList = new ArrayList<>();
        mViewPageList.clear();
        mIndex = 0;
        mLastPress = null;
        parent = LayoutInflater.from(mContext).inflate(R.layout.initable, null);
        mViewPage = parent.findViewById(R.id.initab_page);
        mBTBar = parent.findViewById(R.id.initab_btbar);

        addKeyInit();
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

    private void addKeyInit() {

        //加载allKeyConfigstable
        //按键配置
        final ViewGroup button_ini_content = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.menu_buttonini, null);
        final TextView title = button_ini_content.findViewById(R.id.inibt_title);
        final ListView listView = button_ini_content.findViewById(R.id.inibt_list);
        final TextView inibt_cur = button_ini_content.findViewById(R.id.inibt_cur);
        title.setText("按键配置");
        button_ini_content.findViewById(R.id.inibt_close).setVisibility(View.GONE);

        //检查是否有使用的全局配置
        String gloabkeyconfig = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
        SimpleUtil.log("gloabkeyconfig:" + gloabkeyconfig);
        String curIniName = "";
        if (gloabkeyconfig.isEmpty()) {
            inibt_cur.setTextColor(Color.RED);
            inibt_cur.setText("当前没有使用任何配置");
        } else {
            curIniName = gloabkeyconfig.split("#Z%W#", -1)[1];
            inibt_cur.setTextColor(Color.YELLOW);
            InjectUtil.setComfirGame(gloabkeyconfig.split("#Z%W#", -1)[3]);
            inibt_cur.setText("当前使用:" + InjectUtil.getComfirGame() + "/" + curIniName);

        }


        SharedPreferences allKeysConfigsTable = mContext.getSharedPreferences("KeysConfigs", 0);
        Iterator<String> allIt = allKeysConfigsTable.getAll().keySet().iterator();

        final List<BigKeyConfigAd.BigKeyConfigItemObj> allKeysConfigList = new ArrayList<>();
        final BigKeyConfigAd allKeysConfigAd = new BigKeyConfigAd(mContext, allKeysConfigList);
        while (allIt.hasNext()) {

            BigKeyConfigAd.BigKeyConfigItemObj obj = new BigKeyConfigAd.BigKeyConfigItemObj();
            obj.mTv = allIt.next();
            List<IniAdapter.IniObj> subAllConfigs = new ArrayList<>();

            SharedPreferences allSubConfigs = mContext.getSharedPreferences(obj.mTv + "_table", 0);
            Iterator<? extends Map.Entry<String, ?>> it = allSubConfigs.getAll().entrySet().iterator();
            IniAdapter.IniObj guanfangObj = null;
            while (it.hasNext()) {
                Map.Entry<String, ?> obj2 = it.next();
                String key = obj2.getKey();
                String value = (String) obj2.getValue();

                //byte[] value_int_s = SimpleUtil.getAES().decrypt(value.getBytes());
                //value = new String(value_int_s);
                SimpleUtil.log("normal-mapini:" + key + "    " + value + "\n\n\n");
                if (!key.equals("default")) {
                    SimpleUtil.log("add-mapini:" + key + "    " + value + "\n\n\n");
                    String[] sp = value.split("#Z%W#", -1);
                    IniAdapter.IniObj iniObj = new IniAdapter.IniObj();
                    iniObj.name = sp[1];
                    iniObj.whole = value;
                    iniObj.state = InjectUtil.getComfirGame().equals(obj.mTv) && curIniName.equals(iniObj.name) ? "[使用中]" : null;

                    if (iniObj.name.endsWith("[官方]")) {
                        guanfangObj = iniObj;
                        continue;
                    }
                    if (iniObj.state != null) {
                        subAllConfigs.add(0, iniObj);
                    } else {
                        subAllConfigs.add(iniObj);
                    }
                }

            }
            if (guanfangObj != null) {
                subAllConfigs.add(0, guanfangObj);
            }
            obj.mSubData = subAllConfigs;
            allKeysConfigList.add(obj);

            SimpleUtil.log("sub:" + obj.mTv + "  " + obj.mSubData.size());

        }
        listView.setAdapter(allKeysConfigAd);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                SimpleUtil.log("subsize:" + allKeysConfigList.get(position).mSubData.size());
                allKeysConfigList.get(position).mImgState = !allKeysConfigList.get(position).mImgState;
                allKeysConfigAd.notifyDataSetChanged();


            }
        });

        final String finalCurIniName = curIniName;
        allKeysConfigAd.setmIBigKeyConfigClick(new BigKeyConfigAd.IBigKeyConfigClick() {
            @Override
            public void onItemClick(final int mainPosition, final int subPosition, final IniAdapter adapter) {
                SimpleUtil.log("rec callback:" + mainPosition + "," + subPosition);
                ///start

               /* if (KeyboardEditWindowManager.getInstance().rootViewChildCount() >= 3) {
                    return;
                }*/
                //加载 重命名 删除
                List<String> items = new ArrayList<>();
                items.add("使用");
                items.add("重命名");
                items.add("删除");
                //items.add("测试");
                //--------------
                List<Runnable> runnables = new ArrayList<>();
                runnables.add(new Runnable() {
                    @Override
                    public void run() {
                        String[] sp = allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1);
                        // SimpleUtil.saveToShare(mContext, InjectUtil.getComfirGameTab(), "default", "default#Z%W#" + sp[1] + "#Z%W#" + sp[2]+ "#Z%W#" +allKeysConfigList.get(mainPosition).mTv);
                        SimpleUtil.saveToShare(mContext, "ini", "gloabkeyconfig", "default#Z%W#" + sp[1] + "#Z%W#" + sp[2] + "#Z%W#" + allKeysConfigList.get(mainPosition).mTv);
                        InjectUtil.setComfirGame(allKeysConfigList.get(mainPosition).mTv);
                        InjectUtil.loadBtnParamsFromPrefs(mContext);
                        mKeyboardView.loadUi();
                        for (IniAdapter.IniObj obj : allKeysConfigList.get(mainPosition).mSubData) {
                            if (obj.state != null) {
                                obj.state = null;
                                break;
                            }
                        }
                        allKeysConfigList.get(mainPosition).mSubData.get(subPosition).state = "[使用中]";
                        inibt_cur.setText("当前使用:" + allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name);
                        adapter.notifyDataSetChanged();
                        KeyboardEditWindowManager.getInstance().close();
                        SimpleUtil.notifyall_(10003, null);
                    }
                });
                runnables.add(new Runnable() {
                    @Override
                    public void run() {
                        if (allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name.endsWith("[官方]")) {
                            SimpleUtil.toastTop(mContext, "不能重命名到官方配置");
                            return;
                        }
                        LinkedHashMap<String, String> items = new LinkedHashMap<>();
                        items.put("新的名称", null);
                        SimpleUtil.addEditToTop(mContext, "保存配置", items, null, null, new SimpleUtil.INormalBack() {
                            @Override
                            public void back(int id, Object obj) {
                                if (id != 2) {
                                    return;
                                }
                                List<String> backTexts = (List<String>) obj;
                                String newIniName = backTexts.get(0);
                                if (!InjectUtil.canSaveIniToXml(mContext, newIniName)) {
                                    SimpleUtil.toastTop(mContext, "【" + newIniName + "】已经存在！");
                                    return;
                                }
                                if (newIniName == null || newIniName.isEmpty()) {
                                    SimpleUtil.toastTop(mContext, "新名称不能为空！");
                                    return;
                                }
                                if (allKeysConfigList.get(mainPosition).mSubData.contains(newIniName)) {
                                    SimpleUtil.toastTop(mContext, newIniName + "已经存在！");
                                    return;
                                }
                                if (newIniName.endsWith("[官方]")) {
                                    SimpleUtil.toastTop(mContext, newIniName + "包含敏感词汇！");
                                    return;
                                }
                                //查看是否正在使用
                                String gloabkeyconfig = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
                                String select = allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole;
                                String[] sp1 = gloabkeyconfig.split("#Z%W#", -1);

                                if (allKeysConfigList.get(mainPosition).mTv.equals(sp1[3]) && finalCurIniName.equals(allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name)) {
                                    SimpleUtil.log("You want ro rename the ini is used current!");
                                    inibt_cur.setText("当前使用: " + newIniName);
                                    SimpleUtil.saveToShare(mContext, "ini", "gloabkeyconfig", "default#Z%W#" + newIniName + "#Z%W#" + (allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1))[2] + "#Z%W#" + allKeysConfigList.get(mainPosition).mTv);
                                }
                                String[] sp = allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1);
                                SimpleUtil.saveToShare(mContext, allKeysConfigList.get(mainPosition).mTv + "_table", sp[0], sp[0] + "#Z%W#" + newIniName + "#Z%W#" + (allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1))[2]);


                                allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name = newIniName;
                                allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole = sp[0] + "#Z%W#" + newIniName + "#Z%W#" + (allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1))[2];
                                adapter.notifyDataSetChanged();
                                KeyboardEditWindowManager.getInstance().close();

                                SimpleUtil.removeINormalCallback(this);
                            }
                        });
                    }
                });
                runnables.add(new Runnable() {
                    @Override
                    public void run() {
                        if (allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name.endsWith("[官方]")) {
                            SimpleUtil.toastTop(mContext, "[官方配置]不能删除！");
                            return;
                        }
                        String curIni = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
                        String select = allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole;
                        String[] sp1 = curIni.split("#Z%W#", -1);
                        if (allKeysConfigList.get(mainPosition).mTv.equals(sp1[3]) && finalCurIniName.equals(allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name)) {
                            SimpleUtil.toastTop(mContext, "[" + allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name + "]正在使用中，不能删除！");
                            return;
                        }

                        //先处理ID
                        String configIDs = (String) SimpleUtil.getFromShare(mContext, "ini", "configsID", String.class, "");

                        int configID_ = (Integer) SimpleUtil.getFromShare(mContext, (allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1))[2], "configID", int.class);

                        byte[] ids = Hex.parse(configIDs);
                        for (int i = 0; i < 100; i++) {
                            if (ids[i] == configID_) {
                                ids[i] = 0;
                                SimpleUtil.saveToShare(mContext, "ini", "configsID", Hex.toString(ids));
                                //SimpleUtil.saveToShare(mContext,mSpFileName,"configID",(int)ids[i]);
                                break;
                            }
                        }


                        String spName = allKeysConfigList.get(mainPosition).mTv;
                        SharedPreferences shareLib = mContext.getSharedPreferences(spName + "_table", 0);
                        String key = (allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1))[0];
                        boolean res = shareLib.edit().remove(key).commit();
                        SimpleUtil.log("delete iniconfig result:" + res);
                        Toast.makeText(mContext, "删除 " + (res ? "成功" : "失败"), Toast.LENGTH_SHORT).show();
                        allKeysConfigList.get(mainPosition).mSubData.remove(subPosition);

                        //如果已经全部删除则删除游戏目录
                        if (allKeysConfigList.get(mainPosition).mSubData.size() == 0) {
                            allKeysConfigList.remove(mainPosition);
                            File file = new File("/data/data/" + CommonUtils.getAppPkgName(mContext) + "/shared_prefs", spName + "_table.xml");
                            if (file.exists()) {
                                file.delete();
                                SimpleUtil.delFromShare(mContext, "KeysConfigs", spName);
                                Toast.makeText(mContext, "目录已清除", Toast.LENGTH_LONG).show();
                                SimpleUtil.log("have delete the content " + spName);
                            }
                        }

                        allKeysConfigAd.notifyDataSetChanged();
                        //KeyboardEditWindowManager.getInstance().close();
                        SimpleUtil.saveToShare(mContext, "ini", "configschange", true);
                        SimpleUtil.notifyall_(10003, null);
                    }
                });

                runnables.add(new Runnable() {
                    @Override
                    public void run() {

                        //KeyboardEditWindowManager.getInstance().close();

                        final String backupUse = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
                        InjectUtil.setComfirGame(allKeysConfigList.get(mainPosition).mTv);
                        String[] sp = allKeysConfigList.get(mainPosition).mSubData.get(subPosition).whole.split("#Z%W#", -1);
                        SimpleUtil.saveToShare(mContext, "ini", "gloabkeyconfig", "default#Z%W#" + sp[1] + "#Z%W#" + sp[2] + "#Z%W#" + InjectUtil.getComfirGame());
                        InjectUtil.loadBtnParamsFromPrefs(mContext);
                        mKeyboardView.loadUi();
                        for (IniAdapter.IniObj obj : allKeysConfigList.get(mainPosition).mSubData) {
                            if (obj.state != null) {
                                obj.state = null;
                                break;
                            }
                        }
                        allKeysConfigList.get(mainPosition).mSubData.get(subPosition).state = "[测试中]";
                        inibt_cur.setText("当前使用: " + allKeysConfigList.get(mainPosition).mSubData.get(subPosition).name);
                        allKeysConfigAd.notifyDataSetChanged();


                        FrameLayout frameLayout = new FrameLayout(mContext);

                        LinearLayout linearLayout = new LinearLayout(mContext);

                        int id = (int) System.currentTimeMillis();
                        Button use = new Button(mContext);
                        use.setText("使用");
                        use.setTextSize(16);
                        use.setTextColor(Color.BLUE);
                        use.setId(id);
                        Button exit = new Button(mContext);
                        exit.setText("离开");
                        exit.setTextSize(16);
                        exit.setTextColor(Color.BLUE);
                        exit.setId(id + 0x1111);

                        TextView tv = new TextView(mContext);
                        tv.setTextSize(12);
                        tv.setTextColor(Color.GREEN);
                        tv.setId(id + 0x2222);
                        tv.setText("正在测试: " + sp[1]);

                        use.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                KeyboardEditWindowManager.getInstance().close();
                            }
                        });

                        exit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                allKeysConfigList.get(mainPosition).mSubData.get(subPosition).state = null;
                                inibt_cur.setText("当前使用: " + backupUse.split("#Z%W#", -1)[1]);

                                SimpleUtil.saveToShare(mContext, "ini", "gloabkeyconfig", backupUse);
                                InjectUtil.loadBtnParamsFromPrefs(mContext);
                                mKeyboardView.loadUi();

                                KeyboardEditWindowManager.getInstance().removeTop();
                                InjectUtil.disableInjection();
                                allKeysConfigAd.notifyDataSetChanged();
                            }
                        });
                        SimpleUtil.addINormalCallback(new SimpleUtil.INormalBack() {
                            @Override
                            public void back(int id, Object obj) {
                                if (id != 4) {
                                    return;
                                }

                                SimpleUtil.saveToShare(mContext, "ini", "gloabkeyconfig", backupUse);
                                InjectUtil.loadBtnParamsFromPrefs(mContext);
                                mKeyboardView.loadUi();

                                KeyboardEditWindowManager.getInstance().removeTop();
                                InjectUtil.disableInjection();

                                SimpleUtil.removeINormalCallback(this);

                            }
                        });

                        linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        param.gravity = Gravity.CENTER;
                        linearLayout.addView(use, param);
                        linearLayout.addView(exit, param);
                        linearLayout.addView(tv, param);
                        FrameLayout.LayoutParams buttonBarParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        buttonBarParam.gravity = Gravity.TOP;

                        InjectTestView injectTestView = new InjectTestView(mContext);
                        FrameLayout.LayoutParams testViewParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        frameLayout.addView(injectTestView, testViewParam);
                        frameLayout.addView(linearLayout, buttonBarParam);

                        KeyboardEditWindowManager.getInstance().addView(frameLayout, SimpleUtil.zoomy, SimpleUtil.zoomx).show();
                        InjectUtil.enableInjection();

                    }
                });

                SimpleUtil.addRadioGrouptoTop(mContext, "保存", items, runnables, null, null);

                ///end

            }
        });


        addItem("按键配置");
        mViewPageList.add(button_ini_content);
    }

    private void WriteConfigs() {
        String gloabkeyconfig = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
        final String[] sp0 = gloabkeyconfig.split("#Z%W#", -1);
        SimpleUtil.log("test当前使用:" + sp0[1] + "\n" + gloabkeyconfig);
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_oversize, null);
        final View listPar = view.findViewById(R.id.dialog_oversize_list_par);
        final View gunPar = view.findViewById(R.id.dialog_oversize_gun_par);
        final TextView rightMsg = view.findViewById(R.id.dialog_oversize_rightmsg);
        final List<AOADataPack.Config> configsLeftData = new ArrayList<>();
        final List<AOADataPack.Config> configsRightData = new ArrayList<>();
        final int[] rightSize = {0};
        for (AOADataPack.Config config : mConfigs) {
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

        final MoveConfigAdapter adapterleft = new MoveConfigAdapter(mContext, configsLeftData);
        final MoveConfigAdapter adapterRight = new MoveConfigAdapter(mContext, configsRightData);
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


        final SimpleUtil.INormalBack iNormalBack = new SimpleUtil.INormalBack() {
            @Override
            public void back(int id, Object obj) {
                if (id == 10007) {//取消一个配置
                    AOADataPack.Config config = (AOADataPack.Config) obj;

                    if (config.getIsUsed()) {
                        SimpleUtil.addMsgBottomToTop(mContext, "正在使用的配置不能取消！", true);
                        return;
                    }
                    SimpleUtil.saveToShare(mContext, config.getConfigSha(), "isDelete", true);
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
                        SimpleUtil.addMsgBottomToTop(mContext, "当前最多支持写4个配置！", true);
                        return;
                    }
                    AOADataPack.Config config = (AOADataPack.Config) obj;
                    config.setDeleted(false);
                    SimpleUtil.saveToShare(mContext, config.getConfigSha(), "isDelete", false);
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
                    SimpleUtil.addMsgBottomToTop(mContext, "配置过大！", true);
                    return;
                }

                int bqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "bqNum", int.class, 25);
                int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "cfqNum", int.class, 19);
                int akNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "akNum", int.class, 28);
                SimpleUtil.log("压枪灵敏度：" + bqNum + "," + cfqNum + "," + akNum);
                for (AOADataPack.Config config : mConfigs) {
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

                KeyboardEditWindowManager.getInstance().close();
                Iterator<AOADataPack.Config> it = mConfigs.iterator();
                while (it.hasNext()) {
                    if (it.next().getIsDeleted()) {
                        it.remove();
                    }
                }
                SimpleUtil.notifyall_(10011, mConfigs);
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
