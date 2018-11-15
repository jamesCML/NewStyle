package com.uubox.views;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.uubox.adapters.GunQaAdapter;
import com.uubox.adapters.MoveConfigAdapter;
import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.AliyuOSS;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.Hex;
import com.uubox.tools.LogToFileUtils;
import com.uubox.tools.SimpleUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class IniTab {
    private Context mContext;
    private View parent;
    private ViewPager mViewPage;
    private LinearLayout mBTBar;
    private List<View> mViewPageList;
    private int mIndex;
    private Button mLastPress;

    public IniTab(Context context) {
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mViewPageList = new ArrayList<>();
        mViewPageList.clear();
        mIndex = 0;
        mLastPress = null;
        parent = LayoutInflater.from(mContext).inflate(R.layout.initable, null);
        mViewPage = parent.findViewById(R.id.initab_page);
        mBTBar = parent.findViewById(R.id.initab_btbar);

        //addKeyInit();
        WriteConfigs();
        addAbout();
        //addAOAParamChange();
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
    }

    public void show() {
        SimpleUtil.log("show the Initab");
        KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(false);
        KeyboardEditWindowManager.getInstance().addView(parent, (7 * SimpleUtil.zoomy) / 8, (7 * SimpleUtil.zoomx) / 8);
    }

    private void WriteConfigs() {

        String gloabkeyconfig = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
        final String[] sp0 = gloabkeyconfig.split("#Z%W#", -1);
        if (sp0.length < 2) {
            SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_configloadfail), true);
            return;
        }
        SimpleUtil.log("test当前使用:" + sp0[1] + "\n" + gloabkeyconfig);

        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_oversize, null);
        final View listPar = view.findViewById(R.id.dialog_oversize_list_par);
        final View gunPar = view.findViewById(R.id.dialog_oversize_gun_par);

        final TextView cfq = view.findViewById(R.id.dialog_oversize_gun_cfq_sen);
        final TextView bq = view.findViewById(R.id.dialog_oversize_gun_bq_sen);
        final TextView ak = view.findViewById(R.id.dialog_oversize_gun_ak_sen);

        View.OnClickListener click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {
                    case R.id.cfq_add:
                        String text = cfq.getText().toString();
                        int cur = Integer.parseInt(text) + 1;
                        if (cur >= 100) {
                            cur = 99;
                        }
                        cfq.setText(cur + "");
                        SimpleUtil.saveToShare(mContext, sp0[2], "cfqNum", cur);
                        break;
                    case R.id.bq_add:
                        text = bq.getText().toString();
                        cur = Integer.parseInt(text) + 1;
                        if (cur >= 100) {
                            cur = 99;
                        }
                        bq.setText(cur + "");
                        SimpleUtil.saveToShare(mContext, sp0[2], "bqNum", cur);
                        break;
                    case R.id.ak_add:
                        text = ak.getText().toString();
                        cur = Integer.parseInt(text) + 1;
                        if (cur >= 100) {
                            cur = 99;
                        }
                        ak.setText(cur + "");
                        SimpleUtil.saveToShare(mContext, sp0[2], "akNum", cur);
                        break;
                    case R.id.cfq_sub:
                        text = cfq.getText().toString();
                        cur = Integer.parseInt(text) - 1;
                        if (cur <= 0) {
                            cur = 0;
                        }
                        cfq.setText(cur + "");
                        SimpleUtil.saveToShare(mContext, sp0[2], "cfqNum", cur);
                        break;
                    case R.id.bq_sub:
                        text = bq.getText().toString();
                        cur = Integer.parseInt(text) - 1;
                        if (cur <= 0) {
                            cur = 0;
                        }
                        bq.setText(cur + "");
                        SimpleUtil.saveToShare(mContext, sp0[2], "bqNum", cur);
                        break;
                    case R.id.ak_sub:
                        text = ak.getText().toString();
                        cur = Integer.parseInt(text) - 1;
                        if (cur <= 0) {
                            cur = 0;
                        }
                        ak.setText(cur + "");
                        SimpleUtil.saveToShare(mContext, sp0[2], "akNum", cur);
                        break;
                    case R.id.btn_reset_gun:
                        bq.setText(16 + "");
                        cfq.setText(13 + "");
                        ak.setText(22 + "");
                        SimpleUtil.saveToShare(mContext, sp0[2], "cfqNum", 13);
                        SimpleUtil.saveToShare(mContext, sp0[2], "bqNum", 16);
                        SimpleUtil.saveToShare(mContext, sp0[2], "akNum", 22);
                        break;
                }
                SimpleUtil.saveToShare(mContext, "ini", "configschange", true);
            }
        };
        view.findViewById(R.id.cfq_add).setOnClickListener(click);
        view.findViewById(R.id.cfq_sub).setOnClickListener(click);
        view.findViewById(R.id.bq_add).setOnClickListener(click);
        view.findViewById(R.id.bq_sub).setOnClickListener(click);
        view.findViewById(R.id.ak_add).setOnClickListener(click);
        view.findViewById(R.id.ak_sub).setOnClickListener(click);
        view.findViewById(R.id.btn_reset_gun).setOnClickListener(click);
        final RadioGroup radioGroup = view.findViewById(R.id.dialog_oversize_gun_rg);

        final List<AOAConfigTool.Config> configsLeftData = new ArrayList<>();
        final List<AOAConfigTool.Config> configsRightData = new ArrayList<>();
        final List<AOAConfigTool.Config> configCopyRight = new ArrayList<>();


        //这个地方进入线程使用
        SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleUtil.addWaitToTop(mContext, mContext.getString(R.string.initab_loading_wait));
                final String isMatch = AOAConfigTool.getInstance(mContext).AnysLeftRihgtConfigs(configsLeftData, configsRightData);
                if (isMatch.equals("local")) {
                    SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.aoac_readinfofail), true);
                } else if (!isMatch.equals("match")) {
                    SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_configsnotmatch), true);
                    SimpleUtil.saveToShare(mContext, "ini", "configschange", true);
                }
                SimpleUtil.resetWaitTop(mContext);
                SimpleUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        for (AOAConfigTool.Config config : configsRightData) {
                            configCopyRight.add((AOAConfigTool.Config) config.clone());
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
                                    SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_condev), true);
                                    return;
                                }
                                if (configsRightData.get(position).getIsUsed()) {
                                    return;
                                }
                                String gamesha = configsRightData.get(position).getmTabValue();
                                SimpleUtil.log("item select:" + gamesha);
                                for (AOAConfigTool.Config config1 : configsRightData) {
                                    if (config1.getIsUsed()) {
                                        config1.setmIsUsed(false);
                                    }
                                }

                                //这里刷新一下UI
                                sp0[2] = gamesha;
                                int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "cfqNum", int.class, 13);
                                int bqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "bqNum", int.class, 16);
                                int akNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "akNum", int.class, 22);
                                SimpleUtil.log("刷新获取存储的压枪值:" + bqNum + "." + cfqNum + "," + akNum);
                                bq.setText(bqNum + "");
                                cfq.setText(cfqNum + "");
                                ak.setText(akNum + "");

                                int defaultgun = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "defaultgun", int.class, 0);
                                ((RadioButton) radioGroup.getChildAt(defaultgun)).setChecked(true);


                                configsRightData.get(position).setmIsUsed(true);
                                adapterRight.notifyDataSetChanged();
                            }
                        });
                        listLeft.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                                if (SimpleUtil.isOfficialConfig(configsLeftData.get(position).getmConfigName())) {
                                    SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_del_guanfang), true);
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


                                SharedPreferences shareLib = mContext.getSharedPreferences(configsLeftData.get(position).getmBelongGame() + "_table", 0);
                                boolean res = shareLib.edit().remove(configsLeftData.get(position).getmTabKey()).commit();
                                SimpleUtil.log("delete iniconfig result:" + res);
                                SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_del) + (res ? mContext.getString(R.string.initab_sucessful) : mContext.getString(R.string.initab_fail)), !res);
                                String newConfig = (String) SimpleUtil.getFromShare(mContext, "ini", "NewConfigNotWrite", String.class, "");
                                if (!newConfig.isEmpty()) {
                                    if (configsLeftData.get(position).getmConfigName().equals(newConfig)) {
                                        SimpleUtil.log("we remove the newconfig in the lib!");
                                        SimpleUtil.saveToShare(mContext, "ini", "NewConfigNotWrite", "");
                                    }
                                }

                                //如果已经全部删除则删除游戏目录

                                if (shareLib.getAll().size() == 0) {
                                    File file = new File("/data/data/" + CommonUtils.getAppPkgName(mContext) + "/shared_prefs", configsLeftData.get(position).getmBelongGame() + "_table.xml");
                                    if (file.exists()) {
                                        file.delete();
                                        SimpleUtil.delFromShare(mContext, "KeysConfigs", configsLeftData.get(position).getmBelongGame());
                                        SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_game) + "[" + configsLeftData.get(position).getmBelongGame() + "]" + mContext.getString(R.string.initab_haveclear), false);
                                        SimpleUtil.log("have delete the content " + configsLeftData.get(position).getmBelongGame());
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
                                if (changeGunTv.getText().toString().contains(mContext.getString(R.string.initab_pressgun))) {
                                    view.findViewById(R.id.dialog_oversize_bar).setVisibility(View.GONE);
                                    listPar.setVisibility(View.GONE);
                                    gunPar.setVisibility(View.VISIBLE);
                                    changeGunTv.setText(R.string.initab_gotoconfiglist);
                                } else {
                                    view.findViewById(R.id.dialog_oversize_bar).setVisibility(View.VISIBLE);
                                    listPar.setVisibility(View.VISIBLE);
                                    gunPar.setVisibility(View.GONE);
                                    changeGunTv.setText(R.string.initab_gotogunpresslist);
                                }
                            }
                        });


                        final SimpleUtil.INormalBack iNormalBack = new SimpleUtil.INormalBack() {
                            @Override
                            public void back(int id, final Object obj) {
                                if (id < 10007 || id > 10014) {
                                    return;
                                }
                                if (!AOAConfigTool.getInstance(mContext).isAOAConnect()) {
                                    if (id == 10014) {
                                        SimpleUtil.removeINormalCallback(this);
                                    } else {
                                        SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_condev), true);
                                    }
                                    return;
                                }
                                if (id == 10007) {//取消一个配置
                                    AOAConfigTool.Config config = (AOAConfigTool.Config) obj;

                                    if (config.getIsUsed()) {
                                        SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_curusedel), true);
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
                                        SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_more4configs), true);
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
                                    if (configCopyRight.size() != configsRightData.size() || (Boolean) SimpleUtil.getFromShare(mContext, "ini", "configschange", boolean.class)) {
                                        SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_checknewconfigs), false);
                                        view.findViewById(R.id.dialog_oversize_write).performClick();
                                    } else {
                                        for (int i = 0; i < configCopyRight.size(); i++) {
                                            if (!configCopyRight.get(i).equals(configsRightData.get(i)) || configCopyRight.get(i).getIsUsed() != configsRightData.get(i).getIsUsed()) {
                                                SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_checknewconfigs), false);
                                                view.findViewById(R.id.dialog_oversize_write).performClick();
                                                break;
                                            }
                                        }
                                    }
                                    //SimpleUtil.removeINormalCallback(this);
                                }

                            }
                        };
                        SimpleUtil.addINormalCallback(iNormalBack);

                        view.findViewById(R.id.dialog_oversize_write).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (rightSize[0] > 1024) {
                                    SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_configbiger), true);
                                    return;
                                }
                                SimpleUtil.log("dialog_oversize_write==>");
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

                        int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "cfqNum", int.class, 13);
                        int bqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "bqNum", int.class, 16);
                        int akNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "akNum", int.class, 22);
                        SimpleUtil.log("获取存储的压枪值:" + bqNum + "." + cfqNum + "," + akNum);
                        bq.setText(bqNum + "");
                        cfq.setText(cfqNum + "");
                        ak.setText(akNum + "");

                        int defaultgun = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "defaultgun", int.class, 0);
                        ((RadioButton) radioGroup.getChildAt(defaultgun)).setChecked(true);

                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                RadioButton radioButton = group.findViewById(checkedId);
                                String text = radioButton.getText().toString();
                                SimpleUtil.log("checkedId:" + text);
                                if (text.contains(mContext.getString(R.string.initab_pressgun))) {
                                    SimpleUtil.saveToShare(mContext, sp0[2], "defaultgun", 0);
                                } else if (text.contains(mContext.getString(R.string.initab_cfq))) {
                                    SimpleUtil.saveToShare(mContext, sp0[2], "defaultgun", 1);
                                } else if (text.contains(mContext.getString(R.string.initab_bq))) {
                                    SimpleUtil.saveToShare(mContext, sp0[2], "defaultgun", 2);
                                } else if (text.contains(mContext.getString(R.string.initab_ak))) {
                                    SimpleUtil.saveToShare(mContext, sp0[2], "defaultgun", 3);
                                }
                                SimpleUtil.saveToShare(mContext, "ini", "configschange", true);
                            }
                        });

                        ListView qaList = view.findViewById(R.id.dialog_oversize_gun_qa);
                        final List<GunQaAdapter.QAItem> qaData = new ArrayList<>();
                        GunQaAdapter qaAdapter = new GunQaAdapter(mContext, qaData);
                        String[] questionArr = mContext.getResources().getStringArray(R.array.gunqaitems);
                        for (String que : questionArr) {
                            String[] queSp = que.split("`");
                            GunQaAdapter.QAItem qaItem = new GunQaAdapter.QAItem();
                            qaItem.mQueston = queSp[0];
                            qaItem.mAnswer = queSp[1];
                            qaData.add(qaItem);
                        }
                        qaList.setAdapter(qaAdapter);

                        int height = 0;
                        int count = qaAdapter.getCount();
                        for (int i = 0; i < count; i++) {
                            View temp = qaAdapter.getView(i, null, qaList);
                            temp.measure(0, 0);
                            height += temp.getMeasuredHeight();
                        }
                        ViewGroup.LayoutParams params = qaList.getLayoutParams();
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        params.height = height;
                        qaList.setLayoutParams(params);

                        qaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                SimpleUtil.addMsgtoTop(mContext, mContext.getString(R.string.initab_helpinfo), mContext.getString(R.string.initab_ask) + qaData.get(position).mQueston + "\n\n" + mContext.getString(R.string.initab_answer) + qaData.get(position).mAnswer, null, null, true);
                            }
                        });
                    }
                });
            }
        });


        addItem(mContext.getString(R.string.initab_myconfigs));
        mViewPageList.add(view);


    }

    private void addHelper() {
        ViewGroup helperitem = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.dialog_titlelist, null);
        ((View) helperitem.findViewById(R.id.dialogmsgyes).getParent()).setVisibility(View.GONE);
        ((TextView) helperitem.findViewById(R.id.titlelist_title)).setText(R.string.initab_helpdoc);
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

        addItem(mContext.getString(R.string.initab_helpdoc));
        mViewPageList.add(helperitem);


    }

    private void addAOAParamChange() {
        ViewGroup aoaparamview = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.aoaparam, null);
        final EditText changshang = aoaparamview.findViewById(R.id.aoaparam_changshang);
        final EditText moshi = aoaparamview.findViewById(R.id.aoaparam_moshi);
        final EditText xuliehao = aoaparamview.findViewById(R.id.aoaparam_xuliehao);
        changshang.setText(AOAConfigTool.getInstance(mContext).getAOAInfo()[0]);
        moshi.setText(AOAConfigTool.getInstance(mContext).getAOAInfo()[1]);
        xuliehao.setText(AOAConfigTool.getInstance(mContext).getAOAInfo()[2]);
        aoaparamview.findViewById(R.id.aoaparam_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (changshang.getText().toString().isEmpty() || moshi.getText().toString().isEmpty() || xuliehao.getText().toString().isEmpty()) {
                    SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_infoempty), true);
                    return;
                }

                AOAConfigTool.getInstance(mContext).setAOAInfo(changshang.getText().toString(), moshi.getText().toString(), xuliehao.getText().toString());
                SimpleUtil.saveToShare(mContext, "ini", "configschange", true);
                SimpleUtil.saveToShare(mContext, "ini", "aoaparamschange", true);
            }
        });
        addItem("设备修改");
        mViewPageList.add(aoaparamview);


    }
    private void addAbout() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.iniabout, null);
        ((TextView) (view.findViewById(R.id.iniabout_appver))).setText(mContext.getString(R.string.initab_appver) + CommonUtils.getAppVersionName(mContext));
        ((TextView) (view.findViewById(R.id.iniabout_devver))).setText(mContext.getString(R.string.initab_devber) + (SimpleUtil.mDeviceVersion == null ? mContext.getString(R.string.initab_redverfail) : SimpleUtil.mDeviceVersion));
        ((TextView) (view.findViewById(R.id.iniabout_pix))).setText(mContext.getString(R.string.initab_pixs) + SimpleUtil.zoomx + "*" + SimpleUtil.zoomy);
        SpannableStringBuilder spannableString = new SpannableStringBuilder(mContext.getString(R.string.initab_devnum) + (String) SimpleUtil.getFromShare(mContext, "ini", "idkey", String.class, ""));
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(50);
        spannableString.setSpan(sizeSpan, spannableString.toString().indexOf(':'), spannableString.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                ClipboardManager myClipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                myClipboard.setPrimaryClip(ClipData.newPlainText("text", (String) SimpleUtil.getFromShare(mContext, "ini", "idkey", String.class, "")));
                SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.initab_devnumtoclip), false);
            }
        };
        spannableString.setSpan(clickableSpan, spannableString.toString().indexOf(':'), spannableString.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) (view.findViewById(R.id.iniabout_count))).setText(spannableString);
        ((TextView) (view.findViewById(R.id.iniabout_count))).setMovementMethod(LinkMovementMethod.getInstance());

        if (SimpleUtil.isSaveToXml) {
            view.findViewById(R.id.iniabout_savexml).setVisibility(View.VISIBLE);
            ((TextView) (view.findViewById(R.id.iniabout_savexml))).setText(R.string.initab_confgsaveallow);
        }
        if (SimpleUtil.isEnableOSSLog) {
            view.findViewById(R.id.iniabout_correctlog).setVisibility(View.VISIBLE);
        }

        addItem(mContext.getString(R.string.initab_baseinfo));
        mViewPageList.add(view);
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
