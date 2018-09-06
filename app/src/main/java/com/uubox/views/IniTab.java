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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.uubox.adapters.GunQaAdapter;
import com.uubox.adapters.MoveConfigAdapter;
import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.Hex;
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
        checkUpdate(1000);
    }

    private void checkUpdate(int delay) {
        SimpleUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                new PgyUpdateManager.Builder()
                        .setForced(true)
                        .setUserCanRetry(false)
                        .setDeleteHistroyApk(true)
                        .setUpdateManagerListener(new UpdateManagerListener() {
                            @Override
                            public void onNoUpdateAvailable() {
                                //没有更新是回调此方法
                                SimpleUtil.log("there is no new version");
                            }

                            @Override
                            public void onUpdateAvailable(final AppBean appBean) {
                                SimpleUtil.log("蒲公英版本:" + appBean.getVersionCode());
                                SimpleUtil.addMsgtoTop(mContext, "版本更新", "发现新版本[" + appBean.getVersionName() + "]可更新\n当前应用版本[" + CommonUtils.getAppVersionName(mContext) + "]",
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                    SimpleUtil.addMsgBottomToTop(mContext, "应用【存储权限】未打开，升级失败！", true);
                                                    return;
                                                }
                                                SimpleUtil.addMsgBottomToTop(mContext, "开始下载...", false);
                                                PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                                            }
                                        }, null, false);
                                //PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                            }

                            @Override
                            public void checkUpdateFailed(Exception e) {
                                //更新检测失败回调
                                e.printStackTrace();
                                //  SimpleUtil.log("check update failed ");
                            }
                        })
                        //注意 ：
                        //下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                        //此方法是方便用户自己实现下载进度和状态的 UI 提供的回调
                        //想要使用蒲公英的默认下载进度的UI则不设置此方法
                        .setDownloadFileListener(new DownloadFileListener() {
                            @Override
                            public void downloadFailed() {
                                //下载失败
                                // SimpleUtil.closeDialog(mContext);
                                //SimpleUtil.toast(MainActivity.this,"下载异常，升级失败！");
                                SimpleUtil.addMsgBottomToTop(mContext, "下载异常，升级失败！", true);

                            }

                            @Override
                            public void downloadSuccessful(final Uri uri) {
                                SimpleUtil.log("download apk ok");
                                // 使用蒲公英提供的安装方法提示用户 安装apk
                                SimpleUtil.addMsgBottomToTop(mContext, "新版本下载成功！准备安装！", false);
                                SimpleUtil.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        PgyUpdateManager.installApk(uri);
                                    }
                                }, 2000);
                            }

                            @Override
                            public void onProgressUpdate(Integer... integers) {
                                SimpleUtil.log("apkupdate download apk progress" + integers[0]);
                                //SimpleUtil.updateWaiting(MainActivity.this,"升级中 "+integers[0]+"/100");
                            }
                        }).register();
            }
        }, delay);
    }

    private void WriteConfigs() {

        String gloabkeyconfig = (String) SimpleUtil.getFromShare(mContext, "ini", "gloabkeyconfig", String.class, "");
        final String[] sp0 = gloabkeyconfig.split("#Z%W#", -1);
        SimpleUtil.log("test当前使用:" + sp0[1] + "\n" + gloabkeyconfig);
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_oversize, null);
        final View listPar = view.findViewById(R.id.dialog_oversize_list_par);
        final View gunPar = view.findViewById(R.id.dialog_oversize_gun_par);

        final TextView cfq = view.findViewById(R.id.dialog_oversize_gun_cfq_tv);
        final TextView bq = view.findViewById(R.id.dialog_oversize_gun_bq_tv);
        final TextView ak = view.findViewById(R.id.dialog_oversize_gun_ak_tv);

        final SeekBar cfqBar = view.findViewById(R.id.dialog_oversize_gun_cfq);
        final SeekBar bqBar = view.findViewById(R.id.dialog_oversize_gun_bq);
        final SeekBar akBar = view.findViewById(R.id.dialog_oversize_gun_ak);
        final Button resetBtn = view.findViewById(R.id.btn_reset_gun);

        final RadioGroup radioGroup = view.findViewById(R.id.dialog_oversize_gun_rg);

        final List<AOAConfigTool.Config> configsLeftData = new ArrayList<>();
        final List<AOAConfigTool.Config> configsRightData = new ArrayList<>();
        final List<AOAConfigTool.Config> configCopyRight = new ArrayList<>();


        //这个地方进入线程使用
        SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleUtil.addWaitToTop(mContext, "正在加载，请稍后...");
                final boolean isMatch = AOAConfigTool.getInstance(mContext).AnysLeftRihgtConfigs(configsLeftData, configsRightData);
                SimpleUtil.resetWaitTop();
                SimpleUtil.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
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
                                int akNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "akNum", int.class, 19);
                                SimpleUtil.log("刷新获取存储的压枪值:" + bqNum + "." + cfqNum + "," + akNum);
                                bqBar.setProgress(bqNum - 1);
                                cfqBar.setProgress(cfqNum - 1);
                                akBar.setProgress(akNum - 1);
                                bq.setText("F2类型:步枪   灵敏度:" + bqNum);
                                cfq.setText("F1类型:冲锋枪   灵敏度:" + cfqNum);
                                ak.setText("F3类型:AK47   灵敏度:" + akNum);

                                int defaultgun = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "defaultgun", int.class, 0);
                                ((RadioButton) radioGroup.getChildAt(defaultgun)).setChecked(true);


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


                                SharedPreferences shareLib = mContext.getSharedPreferences(configsLeftData.get(position).getmBelongGame() + "_table", 0);
                                boolean res = shareLib.edit().remove(configsLeftData.get(position).getmTabKey()).commit();
                                SimpleUtil.log("delete iniconfig result:" + res);
                                SimpleUtil.addMsgBottomToTop(mContext, "删除" + (res ? "成功" : "失败"), !res);
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
                                        SimpleUtil.addMsgBottomToTop(mContext, "游戏[" + configsLeftData.get(position).getmBelongGame() + "]已清除", false);
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
                                if (changeGunTv.getText().toString().contains("压枪")) {
                                    view.findViewById(R.id.dialog_oversize_bar).setVisibility(View.GONE);
                                    listPar.setVisibility(View.GONE);
                                    gunPar.setVisibility(View.VISIBLE);
                                    changeGunTv.setText("点击我跳转到配置选择列表");
                                } else {
                                    view.findViewById(R.id.dialog_oversize_bar).setVisibility(View.VISIBLE);
                                    listPar.setVisibility(View.VISIBLE);
                                    gunPar.setVisibility(View.GONE);
                                    changeGunTv.setText("点击我跳转到压枪设置");
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
                                    if (configCopyRight.size() != configsRightData.size() || (Boolean) SimpleUtil.getFromShare(mContext, "ini", "configschange", boolean.class)) {
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
                                    //SimpleUtil.removeINormalCallback(this);
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

                        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                progress += 1;
                                if (progress >= 100) {
                                    progress = 99;
                                }
                                switch (seekBar.getId()) {

                                    case R.id.dialog_oversize_gun_cfq:
                                        cfq.setText("F1类型:冲锋枪   灵敏度:" + progress);
                                        SimpleUtil.saveToShare(mContext, sp0[2], "cfqNum", progress);
                                        break;
                                    case R.id.dialog_oversize_gun_bq:
                                        bq.setText("F2类型:步枪   灵敏度:" + progress);
                                        SimpleUtil.saveToShare(mContext, sp0[2], "bqNum", progress);
                                        break;
                                    case R.id.dialog_oversize_gun_ak:
                                        ak.setText("F3类型:AK47   灵敏度:" + progress);
                                        SimpleUtil.saveToShare(mContext, sp0[2], "akNum", progress);
                                        break;
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                //SimpleUtil.addMsgBottomToTop(mContext, "修改成功", false);
                                SimpleUtil.saveToShare(mContext, "ini", "configschange", true);
                            }
                        };
                        akBar.setOnSeekBarChangeListener(seekBarChangeListener);
                        bqBar.setOnSeekBarChangeListener(seekBarChangeListener);
                        cfqBar.setOnSeekBarChangeListener(seekBarChangeListener);
                        resetBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cfqBar.setProgress(12);
                                bqBar.setProgress(15);
                                akBar.setProgress(19);
                                SimpleUtil.saveToShare(mContext, "ini", "configschange", true);

                            }
                        });
                        int cfqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "cfqNum", int.class, 13);
                        int bqNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "bqNum", int.class, 16);
                        int akNum = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "akNum", int.class, 20);
                        SimpleUtil.log("获取存储的压枪值:" + bqNum + "." + cfqNum + "," + akNum);
                        bqBar.setProgress(bqNum - 1);
                        cfqBar.setProgress(cfqNum - 1);
                        akBar.setProgress(akNum - 1);
                        bq.setText("F2类型:步枪   灵敏度:" + bqNum);
                        cfq.setText("F1类型:冲锋枪  灵敏度:" + cfqNum);
                        ak.setText("F3类型:AK47   灵敏度:" + akNum);

                        int defaultgun = (Integer) SimpleUtil.getFromShare(mContext, sp0[2], "defaultgun", int.class, 0);
                        ((RadioButton) radioGroup.getChildAt(defaultgun)).setChecked(true);

                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                RadioButton radioButton = group.findViewById(checkedId);
                                String text = radioButton.getText().toString();
                                SimpleUtil.log("checkedId:" + text);
                                if (text.contains("压枪")) {
                                    SimpleUtil.saveToShare(mContext, sp0[2], "defaultgun", 0);
                                } else if (text.contains("冲锋枪")) {
                                    SimpleUtil.saveToShare(mContext, sp0[2], "defaultgun", 1);
                                } else if (text.contains("步枪")) {
                                    SimpleUtil.saveToShare(mContext, sp0[2], "defaultgun", 2);
                                } else if (text.contains("AK")) {
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
                                SimpleUtil.addMsgtoTop(mContext, "帮助信息", "问:" + qaData.get(position).mQueston + "\n\n答:" + qaData.get(position).mAnswer, null, null, true);
                            }
                        });
                    }
                });
            }
        });


        addItem("我的配置");
        mViewPageList.add(view);

        //KeyboardEditWindowManager.getInstance().init(mContext).addView(view, (7 * SimpleUtil.zoomy) / 8, (7 * SimpleUtil.zoomx) / 8);
    }

    private void addHelper() {
        ViewGroup helperitem = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.dialog_titlelist, null);
        ((View) helperitem.findViewById(R.id.dialogmsgyes).getParent()).setVisibility(View.GONE);
        ((TextView) helperitem.findViewById(R.id.titlelist_title)).setText("帮助文档");
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

        addItem("帮助文档");
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
                    SimpleUtil.addMsgBottomToTop(mContext, "信息不完整，不能为空！", true);
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
        ((TextView) (view.findViewById(R.id.iniabout_appver))).setText("应用版本:" + CommonUtils.getAppVersionName(mContext));
        ((TextView) (view.findViewById(R.id.iniabout_devver))).setText("设备版本:" + (SimpleUtil.mDeviceVersion == 0 ? "读取版本失败" : SimpleUtil.mDeviceVersion));
        ((TextView) (view.findViewById(R.id.iniabout_pix))).setText("分辨率:" + SimpleUtil.zoomx + "*" + SimpleUtil.zoomy);
        SpannableStringBuilder spannableString = new SpannableStringBuilder("设备号:" + (String) SimpleUtil.getFromShare(mContext, "ini", "idkey", String.class, ""));
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(50);
        spannableString.setSpan(sizeSpan, 4, spannableString.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                ClipboardManager myClipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                myClipboard.setPrimaryClip(ClipData.newPlainText("text", (String) SimpleUtil.getFromShare(mContext, "ini", "idkey", String.class, "")));
                SimpleUtil.addMsgBottomToTop(mContext, "设备号已经复制到剪贴板", false);
            }
        };
        spannableString.setSpan(clickableSpan, 4, spannableString.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        ((TextView) (view.findViewById(R.id.iniabout_count))).setText(spannableString);
        ((TextView) (view.findViewById(R.id.iniabout_count))).setMovementMethod(LinkMovementMethod.getInstance());

        if (SimpleUtil.isSaveToXml) {
            view.findViewById(R.id.iniabout_savexml).setVisibility(View.VISIBLE);
            ((TextView) (view.findViewById(R.id.iniabout_savexml))).setText("配置保存:允许");
        }


        addItem("基本信息");
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
