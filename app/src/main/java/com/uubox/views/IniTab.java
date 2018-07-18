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
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

import com.uubox.padtool.R;
import com.uubox.tools.AES;
import com.uubox.tools.BigKeyConfigAd;
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

        addKeyInit();
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
        KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(false);
        KeyboardEditWindowManager.getInstance().addView(parent, (2 * SimpleUtil.zoomy) / 3, (7 * SimpleUtil.zoomx) / 8);
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
