package com.uubox.views;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.uubox.padtool.MainService;
import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.BtnUtil;
import com.uubox.tools.IniAdapter;
import com.uubox.tools.SaveBtnParamsTask;
import com.uubox.tools.SimpleUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 自定义键盘界面，支持添加、删除、自定义按钮，支持按钮x坐标、y坐标和半径记忆功能。
 *
 * @author 李剑波
 * @date 2015/8/14
 * @time 15:40
 */
public class KeyboardView extends FrameLayout
        implements View.OnDragListener, DragImageView.DragListener,
        DragImageView.ScaleListener, View.OnClickListener, DragImageView.ClickListener, View.OnTouchListener, SimpleUtil.INormalBack {


    public static final String TAG = "KeyboardView";
    /**
     * 菜单栏
     */
    FrameLayout mFlMenu;
    /**
     * “返回”按钮
     */
    ImageView mTvBack1;
    /**
     * “菜单”按钮
     */
    ImageView mIvMenu;

    /**
     * “保存”按钮
     */
    TextView mTvSave;
    LinearLayout mLlClose;
    LinearLayout meyesKeymap;
    ImageView mIvKeymap;
    TextView mTvKeymap;
    /**
     * 添加一个“按钮”
     */
    ImageView mBarWhat;

    ImageView mIvMenuBtnSetting;
    /**
     * 添加一个“左摇杆”
     */
    ImageView mIvMenuBtnL;
    /**
     * 添加一个“右摇杆”
     */
    ImageView mIvMenuBtnR;
    /**
     * 用于获取摇杆按钮的默认半径
     */
    ImageView mIvJoystick;
    /**
     * 正在复制的按钮
     */
    View mCopyingBtn;
    /**
     * 用于创建按钮的菜单条
     */
    View mRlMenuBar;

    ImageView mImgExit;
    private FrameLayout mFlMain;
    private Drawable drawable;
    private boolean dialogShow;
    private Vibrator vibrator;
    private Handler mHandler;
    private final int HANDLE_ADDWHAT = 1;

    @SuppressLint("ResourceType")
    public KeyboardView(Context context) {
        super(context);
        Log.i(TAG, "KeyboardView: ");
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLE_ADDWHAT:
                        if (whatImg != null) {
                            mFlMain.removeView(whatImg);
                            whatImg = null;
                        }
                        whatImg = new DragImageView(getContext());
                        whatImg.setTag(-1);

                        BtnParams param = new BtnParams();
                        param.setBelongBtn(Btn.Q);
                        Drawable drawable = getBtnDrawable(param);
                        if (drawable != null) {
                            whatImg.setImageDrawable(drawable);
                        }
                        whatImg.setDragListener(KeyboardView.this);
                        whatImg.setScaleListener(KeyboardView.this);
                        whatImg.setClickListener(KeyboardView.this);


                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(64, 64);
                        params.leftMargin = msg.arg1 - 32;
                        params.topMargin = msg.arg2 - 32;
                        mFlMain.addView(whatImg, params);


                        ScaleAnimation scaleAnimation_show = new ScaleAnimation(6f, 1f, 6f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                        scaleAnimation_show.setDuration(300);
                        whatImg.startAnimation(scaleAnimation_show);
                        break;
                }
            }
        };
        SimpleUtil.addINormalCallback(this);
        //GuiStep.getInstance().init(context, "skip_note");
        int layout = R.layout.view_keyboard;
        if(SimpleUtil.mAPPUSER== SimpleUtil.APPUSER.AGP)
        {
            layout = R.layout.fire_view_keyboard;
        }
        LayoutInflater.from(context).inflate(layout, this, true);
        findViews();
        initViews();
        // 重新载入按钮参数
        BtnParamTool.loadBtnParamsFromPrefs(getContext());
        loadUi();

    }


    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "KeyboardView: ");

        int layout = R.layout.view_keyboard;
        if(SimpleUtil.mAPPUSER== SimpleUtil.APPUSER.AGP)
        {
            layout = R.layout.fire_view_keyboard;
        }
        LayoutInflater.from(context).inflate(layout, this, true);
        findViews();
        initViews();
        // 重新载入按钮参数
        BtnParamTool.loadBtnParamsFromPrefs(getContext());
        loadUi();

    }

   private void animation_in()
   {

       TranslateAnimation translateAnimation = new TranslateAnimation(0,0,-100,0);
       translateAnimation.setDuration(200);
       mFlMenu.startAnimation(translateAnimation);
   }
    private void findViews() {
        Log.i(TAG, "findViews: ");
        mFlMain = this.findViewById(R.id.fl_main);
        mFlMenu = this.findViewById(R.id.fl_menu);
        mIvMenu = this.findViewById(R.id.iv_menu);
        mTvSave = this.findViewById(R.id.tv_save);
        mLlClose = findViewById(R.id.ll_close);
        meyesKeymap = findViewById(R.id.ll_keymap);
        mIvKeymap = findViewById(R.id.iv_keymap);
        mTvKeymap = findViewById(R.id.tv_keymap);
        mRlMenuBar = this.findViewById(R.id.rl_menu_bar);
        mBarWhat = this.findViewById(R.id.iv_menu_btn_btn);
        mIvMenuBtnSetting = this.findViewById(R.id.iv_menu_btn_setting);
        mIvMenuBtnL = this.findViewById(R.id.iv_menu_btn_l);
        mIvMenuBtnR = this.findViewById(R.id.iv_menu_btn_r);
        mIvJoystick = this.findViewById(R.id.iv_joystick);

        mIvMenuBtnL.setVisibility(VISIBLE);
        mIvMenuBtnR.setVisibility(VISIBLE);
        // FIXME: 2017/9/18  测试页面 和 提示语  后续版本添加

        mImgExit = findViewById(R.id.iv_menu_btn_exit);
        if(SimpleUtil.mAPPUSER== SimpleUtil.APPUSER.AGP)
        {

            animation_in();
        }

    }

    private void initViews() {

        /**
         * 背景半透效果
         */
        mFlMain.getBackground().setAlpha(0);
        //mFlMain.setOnDragListener(this);
        mFlMain.setOnClickListener(this);
        mFlMain.setOnTouchListener(this);
        mIvMenu.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
        mLlClose.setOnClickListener(this);
        meyesKeymap.setOnClickListener(this);
        mImgExit.setOnClickListener(this);
        mIvMenuBtnSetting.setOnTouchListener(this);
        mIvMenuBtnL.setOnTouchListener(this);
        mIvMenuBtnR.setOnTouchListener(this);
        //mFlMain.setOnTouchListener(this);
        mBarWhat.setOnTouchListener(this);
        initEyes();
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        int action = event.getAction();
        SimpleUtil.log("onDrag:" + (int) event.getX() + "," + (int) event.getY());
        switch (action) {
            // 放下按钮
            case DragEvent.ACTION_DROP:
                if (mCopyingBtn == null) {
                    break;
                }
                /**
                 * {@link #onTouch(View, MotionEvent)}
                 */
                int btnRadius = mCopyingBtn.getWidth() / 2;
                DragImageView iv = new DragImageView(getContext());
                BtnParamTool.getBtnNormalBtn(Btn.Q).setBelongBtn(Btn.Q);
                iv.setTag(BtnParamTool.getBtnNormalBtn(Btn.Q));
                if (mCopyingBtn == mBarWhat) {
                    BtnParamTool.getBtnNormalBtn(Btn.Q).img = iv;
                } else if (mCopyingBtn == mIvMenuBtnL) {
                    iv.setTag(BtnParamTool.getBtnNormalBtn(Btn.L));
                    BtnParamTool.getBtnNormalBtn(Btn.L).img = iv;
                    btnRadius = mIvJoystick.getWidth() / 2;
                    BtnParamTool.setBtnPositionX(Btn.L, (int) event.getX());
                    BtnParamTool.setBtnPositionY(Btn.L, (int) event.getY());
                    BtnParamTool.setBtnRadius(Btn.L, mIvJoystick.getWidth() / 2);
                } else if (mCopyingBtn == mIvMenuBtnR) {
                    iv.setTag(BtnParamTool.getBtnNormalBtn(Btn.R));
                    BtnParamTool.getBtnNormalBtn(Btn.R).img = iv;
                    btnRadius = mIvJoystick.getWidth() / 2;
                    SimpleUtil.log("ACTION_DROP:" + (int) event.getX() + "," + (int) event.getY());
                    BtnParamTool.setBtnPositionX(Btn.R, (int) event.getX());
                    BtnParamTool.setBtnPositionY(Btn.R, (int) event.getY());
                    BtnParamTool.setBtnRadius(Btn.R, mIvJoystick.getWidth() / 2);
                }
                Object obj = iv.getTag();
                if (obj instanceof Integer) {
                    return false;
                }
                Drawable drawable = getBtnDrawable((BtnParams) iv.getTag());
                if (drawable != null) {
                    iv.setImageDrawable(drawable);
                }
                iv.setDragListener(this);
                iv.setScaleListener(this);
                iv.setClickListener(this);
                LayoutParams layoutParams =
                        new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                layoutParams.leftMargin = (int) event.getX() - btnRadius;
                layoutParams.topMargin = (int) event.getY() - btnRadius;
                mFlMain.addView(iv, layoutParams);
                mCopyingBtn = null;
                break;
        }
        return true;
    }

    public void showSaveDialog() {
        String gloabkeyconfig = (String) SimpleUtil.getFromShare(getContext(), "ini", "gloabkeyconfig", String.class, "");
        BtnParamTool.setComfirGame(gloabkeyconfig.split("#Z%W#", -1)[3]);
        KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(false);
        List<String> items = new ArrayList<>();
        items.add(getContext().getString(R.string.kbv_savetocurconfig));
        items.add(getContext().getString(R.string.kbv_savetohasconfig));
        items.add(getContext().getString(R.string.kbv_newoneconfig));
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(new Runnable() {
            @Override
            public void run() {
                String curIni = (String) SimpleUtil.getFromShare(getContext(), "ini", "gloabkeyconfig", String.class, "");
                String iniName = "";
                if (curIni != null && !curIni.isEmpty()) {
                    iniName = curIni.split("#Z%W#", -1)[1];
                }
                if (SimpleUtil.isOfficialConfig(iniName)) {

                    LinkedHashMap<String, String> items = new LinkedHashMap<>();
                    items.put(getContext().getString(R.string.kbv_configname), BtnParamTool.getComfirGame() + "_" + SimpleUtil.getSha1(System.currentTimeMillis() + "").substring(0, 5));

                    SimpleUtil.addEditToTop(getContext(), getContext().getString(R.string.kbv_saveconfig), items, null, new Runnable() {
                        @Override
                        public void run() {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    KeyboardEditWindowManager.getInstance().close();
                                }
                            }, 100);

                        }
                    }, newinietback);
                    SimpleUtil.addMsgBottomToTop(getContext(), getContext().getString(R.string.kbv_guanfangnewconfig), true);
                    return;
                }
                if (curIni == null || curIni.isEmpty()) {
                    String tempIni = BtnParamTool.getComfirGame() + System.currentTimeMillis() + "#Z%W#" + BtnParamTool.getComfirGame() + "_1#Z%W#" + BtnParamTool.getComfirGame() + "_1";
                    SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                    saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                    saveBtnParamsTask.execute(tempIni);
                    //BtnParamTool.saveBtnParams(getContext(), tempIni);
                } else {
                    SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                    saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                    saveBtnParamsTask.execute(curIni);
                    //BtnParamTool.saveBtnParams(getContext(), curIni);
                }

            }
        });

        runnables.add(new Runnable() {
            @Override
            public void run() {

                final ViewGroup button_ini_content = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.menu_buttonini, null);
                button_ini_content.setBackgroundResource(R.drawable.dialog_message_box_b_deep);

                View title = button_ini_content.findViewById(R.id.inibt_title);

                TextView inibt_cur = button_ini_content.findViewById(R.id.inibt_cur);
                button_ini_content.findViewById(R.id.inibt_close).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        KeyboardEditWindowManager.getInstance().removeTop();
                    }
                });

                String curName = null;
                String gloabkeyconfig = (String) SimpleUtil.getFromShare(getContext(), "ini", "gloabkeyconfig", String.class, "");
                if (gloabkeyconfig.isEmpty()) {
                    inibt_cur.setTextColor(Color.RED);
                    inibt_cur.setText(R.string.kbv_nouseconfig);
                } else {
                    curName = gloabkeyconfig.split("#Z%W#", -1)[1];
                    inibt_cur.setTextColor(Color.YELLOW);
                    BtnParamTool.setComfirGame(gloabkeyconfig.split("#Z%W#", -1)[3]);
                    inibt_cur.setText(getContext().getString(R.string.kb_curused) + SimpleUtil.zhChange(getContext(), BtnParamTool.getComfirGame()) + "/" + SimpleUtil.zhChange(getContext(), curName));

                }

                title.measure(0, 0);
                ListView listView = button_ini_content.findViewById(R.id.inibt_list);
                final List<IniAdapter.IniObj> iniDatas = new ArrayList<>();
                IniAdapter adapter = new IniAdapter(getContext(), iniDatas);
                SharedPreferences sharedPreferences2 = getContext().getSharedPreferences(BtnParamTool.getComfirGameTab(), 0);
                Iterator<? extends Map.Entry<String, ?>> it = sharedPreferences2.getAll().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, ?> obj = it.next();
                    String key = obj.getKey();
                    String value = (String) obj.getValue();
                    // byte[] value_int_s = SimpleUtil.getAES().decrypt(value.getBytes());
                    //value = new String(value_int_s);
                    SimpleUtil.log("mapini:" + key + "    " + value);

                    String[] sp = value.split("#Z%W#", -1);
                    IniAdapter.IniObj iniObj = new IniAdapter.IniObj();
                    iniObj.name = sp[1];
                    iniObj.whole = value;
                    iniObj.state = iniObj.name.equals(curName) ? getContext().getString(R.string.kbv_used) : null;

                    //发现官方配置，应该直接移除
                    if (SimpleUtil.isOfficialConfig(iniObj.name)) {
                        continue;
                    }

                    if (iniObj.state != null) {
                        iniDatas.add(0, iniObj);
                    } else {
                        iniDatas.add(iniObj);
                    }


                }

                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        if (SimpleUtil.isOfficialConfig(iniDatas.get(position).name)) {
                            //SimpleUtil.toast(getContext().getString(R.string.msg_a5));
                            return;
                        }
                        BtnParamTool.saveBtnParams(getContext(), iniDatas.get(position).whole);
                        //SimpleUtil.toast(getContext().getString(R.string.save_success));
                        BtnParamTool.setBtnParamsChanged(false);
                        KeyboardEditWindowManager.getInstance().removeView(button_ini_content);
                        KeyboardEditWindowManager.getInstance().close();
                        //保存之后使用
                        String[] sp = iniDatas.get(position).whole.split("#Z%W#", -1);
                        SimpleUtil.saveToShare(getContext(), "ini", "gloabkeyconfig", "default#Z%W#" + sp[1] + "#Z%W#" + sp[2] + "#Z%W#" + BtnParamTool.getComfirGame());
                        BtnParamTool.loadBtnParamsFromPrefs(getContext());
                        loadUi();
                        SimpleUtil.notifyall_(10003, null);

                    }
                });
                KeyboardEditWindowManager.getInstance().addView(button_ini_content, (2 * SimpleUtil.zoomy) / 3, (2 * SimpleUtil.zoomx) / 3);

            }
        });


        runnables.add(new Runnable() {
            @Override
            public void run() {
                LinkedHashMap<String, String> items = new LinkedHashMap<>();
                items.put(getContext().getString(R.string.kbv_configname), SimpleUtil.zhChange(getContext(), BtnParamTool.getComfirGame()) + "_" + SimpleUtil.getSha1(System.currentTimeMillis() + "").substring(0, 5));

                SimpleUtil.addEditToTop(getContext(), getContext().getString(R.string.kbv_saveconfig), items, null, new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                KeyboardEditWindowManager.getInstance().close();
                            }
                        }, 100);

                    }
                }, newinietback);
            }
        });

        SimpleUtil.addRadioGrouptoTop(getContext(), getContext().getString(R.string.kbv_save), items, runnables, null, new Runnable() {
            @Override
            public void run() {
                /*ConcurrentHashMap<Btn, BtnParams> params = BtnParamTool.getmBtnParams();
                for (BtnParams btnParam: addingBtns) {
                     // btnParam.backupAssignTo();
                      params.replace(btnParam.getBelongBtn(),btnParam);
                }
                addingBtns.clear();*/
                BtnParamTool.loadBtnParamsFromPrefs(getContext());
                loadUi();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        KeyboardEditWindowManager.getInstance().close();
                    }
                }, 100);

            }
        });
    }

    SimpleUtil.INormalBack newinietback = new SimpleUtil.INormalBack() {
        @Override
        public void back(final int id, Object obj) {
            if (id != 2) {
                return;
            }
            List<String> backs = (List<String>) obj;
            final String iniName = backs.get(0);
            if (!BtnParamTool.canSaveIniToXml(getContext(), iniName)) {
                Toast.makeText(getContext(), getContext().getString(R.string.kbv_confignameexist),
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (iniName == null || iniName.isEmpty()) {
                Toast.makeText(getContext(), getContext().getString(R.string.kbv_confignameempty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (iniName.equals("#*#*wisega2015*#*#")) {
                //SimpleUtil.toast(getContext().getString(R.string.ma4));
                return;
            }


            SimpleUtil.log("preare to save comfirgame:" + BtnParamTool.getComfirGame());
            if (BtnParamTool.getComfirGame() == null) {
                BtnParamTool.setComfirGame(iniName);
                SimpleUtil.log("preare to save setcomfirgame:" + iniName);
            }

            KeyboardEditWindowManager.getInstance().removeTop();
            List<String> items = new ArrayList<>();
            items.add(getContext().getString(R.string.kbv_savetogame) + "【" + SimpleUtil.zhChange(getContext(), BtnParamTool.getComfirGame()) + "】");
            items.add(getContext().getString(R.string.kbv_newgame));
            List<Runnable> tasks = new ArrayList<>();
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                    saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                    saveBtnParamsTask.execute(BtnParamTool.getComfirGame() + System.currentTimeMillis() + "#Z%W#" + iniName + "#Z%W#" + iniName);
                    SimpleUtil.removeINormalCallback(newinietback);
                }
            });
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    LinkedHashMap<String, String> items = new LinkedHashMap<>();
                    items.put(getContext().getString(R.string.kbv_gamename), "");
                    SimpleUtil.addEditToTop(getContext(), getContext().getString(R.string.kbv_newgame), items, null, new Runnable() {
                        @Override
                        public void run() {
                            KeyboardEditWindowManager.getInstance().close();
                        }
                    }, new SimpleUtil.INormalBack() {
                        @Override
                        public void back(int id, Object obj) {
                            if (id != 2) {
                                return;
                            }
                            List<String> backs = (List<String>) obj;
                            final String newIniContentName = backs.get(0);

                            if (newIniContentName == null || newIniContentName.isEmpty()) {
                                Toast.makeText(getContext(), getContext().getString(R.string.kbv_gamenameempty),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SharedPreferences allKeysConfigsTable = getContext().getSharedPreferences("KeysConfigs", 0);
                            Iterator<String> allIt = allKeysConfigsTable.getAll().keySet().iterator();
                            while (allIt.hasNext()) {
                                if (allIt.next().equals(newIniContentName)) {
                                    Toast.makeText(getContext(), getContext().getString(R.string.kbv_gamenameexist),
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            BtnParamTool.setComfirGame(newIniContentName);
                            SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                            saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                            saveBtnParamsTask.execute(BtnParamTool.getComfirGame() + System.currentTimeMillis() + "#Z%W#" + iniName + "#Z%W#" + iniName);
                            SimpleUtil.removeINormalCallback(newinietback);

                        }
                    });


                }
            });
            SimpleUtil.addRadioGrouptoTop(getContext(), getContext().getString(R.string.kbv_saveconfig), items, tasks, null, new Runnable() {
                @Override
                public void run() {
                    KeyboardEditWindowManager.getInstance().close();
                }
            });

        }
    };

    @Override
    public void onClick(View v) {
        Log.i(TAG, "onClick: v=" + v);
        if ((v == mTvBack1 && SimpleUtil.mCallBacks.size() != 0)) {
            // 当前为测试界面

            // 当前为编辑界面

            Log.w(TAG, "onClick: mCallBack.onBackBtnClick();");
            SimpleUtil.notifyall_(1, null);


        } else if (v == meyesKeymap) {
            // 更新显示状态到配置中
            // 刷新键位按钮的UI
            refreshKeymapBtn();
        } else if (v == mTvSave | v == mLlClose) {
            if (KeyboardEditWindowManager.getInstance().rootViewChildCount() > 1) {
                return;
            }
            boolean changed = BtnParamTool.hasBtnParamsChanged();
            if (changed) {
                //确认保存对话框
                showSaveDialog();

            } else {
                //SimpleUtil.notifyall_(1, null);
                KeyboardEditWindowManager.getInstance().close();
            }
        } else if (v == mImgExit) {
            SimpleUtil.addMsgtoTop(getContext(), getContext().getString(R.string.kbv_warmwarn), getContext().getString(R.string.kbv_exitapp), new Runnable() {
                @Override
                public void run() {
                    getContext().stopService(new Intent(getContext(), MainService.class));
                    System.exit(0);
                }
            }, new Runnable() {
                @Override
                public void run() {
                    KeyboardEditWindowManager.getInstance().removeTop();
                }
            }, false);
        }
    }


    /**
     * 设置新按钮的按键类型
     *
     * @param btn 按钮类型
     */
    public void setBtn(final Btn btn) {
        if (dialogShow || whatImg == null || btn == null || btn == Btn.Q) {
            return;
        }
        //第二次映射
        if (BtnParamTool.getBtnNormalBtn(btn).getKeyType() == 3) {
            SimpleUtil.addMsgBottomToTop(getContext(), getContext().getString(R.string.kbv_type3addkey), true);
            return;
        }
        if (BtnParamTool.getBtnNormalBtn(btn).img != null) {
            List<String> items = new ArrayList<>();
            items.add(getContext().getString(R.string.kbv_union));
            // items.add("交替");
            items.add(getContext().getString(R.string.kbv_clear) + btn);
            List<Runnable> runnables = new ArrayList<>();

            runnables.add(new Runnable() {
                @Override
                public void run() {
                    addSecFucButton(btn, 1);
                }
            });
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    dialogShow = false;
                    removeBtn(BtnParamTool.getBtnNormalBtn(btn));
                    mFlMain.removeView(whatImg);
                    whatImg = null;
                }
            });
            SimpleUtil.addRadioGrouptoTop(getContext(), getContext().getString(R.string.kbv_choicemod), items, runnables, new Runnable() {
                @Override
                public void run() {
                    dialogShow = false;
                }
            }, new Runnable() {
                @Override
                public void run() {
                    dialogShow = false;
                }
            });

            dialogShow = true;

            return;
        } else {
//            btn.setKeyType(0);
            drawable = getBtnDrawable(BtnParamTool.getBtnNormalBtn(btn));
        }
        if (drawable != null) {
            whatImg.setImageDrawable(drawable);
        }
        whatImg.setTag(BtnParamTool.getBtnNormalBtn(btn));

        BtnParamTool.setBtnPositionX(btn,
                (int) whatImg.getX() + whatImg.getWidth() / 2);
        BtnParamTool.setBtnPositionY(btn,
                (int) whatImg.getY() + whatImg.getHeight() / 2);
        BtnParamTool.setBtnRadius(btn, whatImg.getWidth() / 2);

        BtnParamTool.getBtnNormalBtn(btn).img = whatImg;
        BtnParamTool.getBtnNormalBtn(btn).setBelongBtn(btn);
        whatImg = null;
    }

    private void addSecFucButton(Btn btn, int type) {
        dialogShow = false;
        if (BtnParamTool.getBtnRepeatBtn2(btn) != null) {
            mFlMain.removeView(BtnParamTool.getBtnRepeatBtn2(btn).img);
        }

        BtnParamTool.getBtnNormalBtn(btn).doParent(true);
        //设置btn2
        BtnParams params = new BtnParams();
        // params.setmBackup(params.clone());
        //设置主节点，表示该参数是从属
        params.doParent(false);
        params.setBelongBtn(btn);
        params.setKeyType(type);
        drawable = getBtnDrawable(params);
        if (drawable != null) {
            whatImg.setImageDrawable(drawable);
        }
        whatImg.setBackgroundResource(BtnParamTool.getBtnBelongColor(params));

        whatImg.setTag(params);

        params.setX(
                (int) whatImg.getX() + whatImg.getWidth() / 2);
        params.setY(
                (int) whatImg.getY() + whatImg.getHeight() / 2);
        params.setR(whatImg.getWidth() / 2);

        params.img = whatImg;
        whatImg = null;
        BtnParamTool.setBtnRepeatBtn2(btn, params);

        //addingBtns.add(params.clone());
    }

    /**
     * @return 按钮对应图片资源
     */
    private Drawable getBtnDrawable(final BtnParams params) {
        Btn btn = params.getBelongBtn();
        return BtnUtil.getBtnDrawable(btn, getContext());
    }

    /**
     * 在拖动开始
     *
     * @param v 被拖拽的控件
     */
    @Override
    public void onDragStart(View v) {
        if (BtnParamTool.getBtnNormalBtn(Btn.R) == null) {
            return;
        }
        View vv = BtnParamTool.getBtnNormalBtn(Btn.R).img;
        SimpleUtil.log("vv3:" + vv);
        Log.i(TAG, "onDragStart: 在拖动开始" + v.toString());
        // 显示删除按钮
        mIvMenu.setVisibility(VISIBLE);
        SimpleUtil.anim_shake(mIvMenu);
    }


    /**
     * 拖动完成时
     *
     * @param v 被拖拽的控件
     */
    @Override
    public void onDragFinish(View v) {
        if (v.getTag() == null) {
            return;
        }

        // 松开位置位于删除按钮内，移除按钮
        if (isIntersectRemoveBtn(v)) {

            if (v.getTag() instanceof Integer) {
                mFlMain.removeView(v);
                whatImg = null;
            } else {
                BtnParams params = (BtnParams) v.getTag();
                if (params.getKeyType() == 3) {
                    SimpleUtil.addMsgBottomToTop(getContext(), getContext().getString(R.string.kbv_deltype3), true);
                    return;
                }
                // params.setmBackup(params.clone());

                //addingBtns.add(params.clone());
                removeBtn(params);

                Log.w(TAG, "onDragFinish: getId()=" + v.getId() + "getTag()=" + v.getTag());

                // FIXME: 2017/9/18 删除按钮后再编辑页面显示按钮
                //tag = v.getTag();

//            setVisible(tag);

                // tag = null;
            }
        }
        // 松开位置位于其他区域，保存按钮坐标
        else {
            if (!(v.getTag() instanceof Integer)) {
                int[] position = new int[2];
                v.getLocationInWindow(position);
                final int x = position[0] + v.getWidth() / 2;
                final int y = position[1] + v.getHeight() / 2;
                //final int x = (int) v.getX() + v.getWidth() / 2+SimpleUtil.LIUHAI;
                //final int y = (int) v.getY() + v.getHeight() / 2;
                BtnParams params = (BtnParams) v.getTag();
                //params.setmBackup(params.clone());
                //addingBtns.add(params.clone());
                if (params.getX() != x || params.getY() != y) {
                    params.setX(x);
                    params.setY(y);
                    SimpleUtil.log("save the XY:" + x + "," + y + " LIUHAI:" + SimpleUtil.LIUHAI + "  hash:" + params.hashCode());
                    BtnParamTool.setBtnParamsChanged(true);
                }

            }

        }

        mIvMenu.clearAnimation();
        mIvMenu.setVisibility(GONE);
    }

    @Override
    public void onScaleStart(View v) {
        Log.i(TAG, "onScaleStart: " + v.toString());
        // 显示添加按钮
        mIvMenu.setVisibility(VISIBLE);
        SimpleUtil.anim_shake(mIvMenu);
    }

    @Override
    public void onScaleFinish(View v) {
        Log.i(TAG, "onScaleFinish: " + v.toString());
        //TODO 先拖拽按钮，到达目标位置后，不松开手指，直接缩放，此时记忆的坐标为拖拽前的坐标
        if (v.getTag() == null) {
            return;
        }
        Object obj = v.getTag();
        if (obj instanceof Integer) {
            return;
        }
        BtnParams params = (BtnParams) v.getTag();
        if (params.isParent()) {
            BtnParamTool.setBtnRadius2(params.getBelongBtn(), v.getWidth() / 2);
        } else {
            BtnParamTool.setBtnRadius(params.getBelongBtn(), v.getWidth() / 2);
        }
        BtnParamTool.setBtnParamsChanged(true);
    }

    public void loadUi() {
        Log.i(TAG, "loadUi:   载入之前保存的UI，如果之前有保存按钮的参数，则将创建该按钮并将其坐标和半径设置为保存的值");
        int childCount = allView.size();
        for (int i = 0; i < childCount; i++) {
            mFlMain.removeView(allView.get(i));
        }
        ConcurrentMap<Btn, BtnParams> buttons = BtnParamTool.getmBtnParams();
        Iterator<Btn> it = buttons.keySet().iterator();
        while (it.hasNext()) {
            Btn btn = it.next();
            makeButtonView(btn, buttons.get(btn));
        }
    }

    //奇怪，用帧布局移除老是只移除基数的角标,增加一个列表保存所有的View
    private List<View> allView = new ArrayList<>();

    public List<View> getAllButtonViews() {
        return allView;
    }
    private void makeButtonView(final Btn btn, final BtnParams params) {
        int x = params.getX();//- SimpleUtil.LIUHAI
        int y = params.getY();
        int rd = params.getR();

        DragImageView iv = (DragImageView) params.img;
        if ((x <= 0 && y <= 0 && rd <= 0) || iv != null) {
            return;
        }
        iv = new DragImageView(getContext());
        Drawable drawable = getBtnDrawable(params);
        if (drawable != null) {
            iv.setImageDrawable(drawable);
        }
        params.setBelongBtn(btn);

        iv.setTag(params);
        iv.setDragListener(this);
        //iv.setScaleListener(this);
        iv.setClickListener(this);
        final LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //按钮试图的位置
        int r = params.getR();
        iv.setLayoutParams(layoutParams);
        iv.measure(0, 0);
        int ivHeight = iv.getMeasuredHeight();
        int ivWith = iv.getMeasuredWidth();
        if (r != ivWith && r > 0) {
            layoutParams.width = 2 * r;
            layoutParams.height = 2 * r;
            ivWith = layoutParams.width;
            ivHeight = layoutParams.height;
        }

        layoutParams.leftMargin = x - ivWith / 2;
        layoutParams.topMargin = y - ivHeight / 2;
        iv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);

        iv.setBackgroundResource(BtnParamTool.getBtnBelongColor(params));
        allView.add(iv);
        mFlMain.addView(iv, layoutParams);
        if (btn == Btn.L) {
            iv.setScaleListener(this);
            mIvMenuBtnL.setVisibility(GONE);
            //GuiStep.getInstance().addToGui(iv, "控制方向按键，如果您觉得按下【W】键人物不能疾跑，建议将该按键放大以达到效果");
        } else if (btn == Btn.R) {
            mIvMenuBtnR.setVisibility(GONE);
            //GuiStep.getInstance().addToGui(iv, "鼠标位置，可以通过点击打开【鼠标设置】界面，可以调节鼠标灵敏度和鼠标呼出方式");
        } else {
            if (BtnParamTool.getBtnNormalBtn(btn).getKeyType() == 1) {
                //GuiStep.getInstance().addtToBuff("key_union", iv);
            } else {
                //GuiStep.getInstance().addtToBuff("key_reflect", iv);
            }
        }

        params.img = iv;

        //表示该按钮参数有附属，则需要递归一次
        if (params.iHaveChild()) {
            makeButtonView(btn, params.btn2);
        }
        /*mFlMain.measure(0,0);
        int[] position = new int[2];
        iv.getLocationOnScreen(position);
        SimpleUtil.log(BtnParamTool.getComfirGame()+" mk:"+btn.name + "   " + position[0] + "," + position[1]);*/
    }

    /**
     * @param view 目标控件
     * @return 目标控件是否与删除按钮有重叠部分
     */
    private boolean isIntersectRemoveBtn(View view) {
        Log.i(TAG, "isIntersectRemoveBtn: param 目标控件，return 是否与删除重叠v=" + view.toString());
        if (view == null || mIvMenu == null) {
            return false;
        }

        RectF r1 = getRectF(view);
        RectF r2 = getRectF(mIvMenu);
        //震动提示
        vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            vibrator.vibrate(30);
        } else {
//                vibrator.vibrate();
        }
        return RectF.intersects(r1, r2);
    }

    /**
     * @param view 目标控件
     * @return 控件的RectF
     */
    private RectF getRectF(View view) {
        Log.i(TAG, "getRectF: v=" + view.toString());
        if (view == null) {
            return null;
        }

        int[] xy = new int[2];
        view.getLocationInWindow(xy);
        return new RectF(xy[0], xy[1], xy[0] + view.getWidth(),
                xy[1] + view.getHeight());
    }

    /**
     * 移除目标按钮及按钮相关的配置
     */
    private void removeBtn(final BtnParams btnParams) {
        Log.e(TAG, "removeBtn:     移除目标按钮及按钮相关的配置 btn=" + btnParams.toString());
        if (btnParams == null) {
            return;
        }

        if (btnParams.iAnChild()) {
            mFlMain.removeView(btnParams.img);
            BtnParamTool.resetRepeatBtnParams(btnParams.getBelongBtn());
            btnParams.img = null;
        } else {//常规按钮
            mFlMain.removeView(btnParams.img);
            //还有附属按键，则一起死！！！！！！
            if (btnParams.iHaveChild()) {
                BtnParams subParams = btnParams.getBtn2();
                mFlMain.removeView(subParams.img);
                BtnParamTool.resetRepeatBtnParams(subParams.getBelongBtn());
                subParams.img = null;
            }
            BtnParamTool.resetBtnParams(btnParams.getBelongBtn());
            btnParams.img = null;


        }

    }


    /**
     * 显示按钮配置对话框
     *
     * @param btn 按钮类型
     */
    private void showBtnDialog(final Btn btn) {
        Log.i(TAG, "showBtnDialog: 显示按钮配置对话框 btn=" + btn.toString());
        if (btn == null || KeyboardEditWindowManager.getInstance().getTopView().getId() == R.id.dialog_keyboard_rock_attr_parent) {
            return;
        }

        KeyboardEditWindowManager.getInstance().addView(new MouseAdjestDialog().create(getContext()), 2 * SimpleUtil.zoomy / 3, 2 * SimpleUtil.zoomx / 3);
    }

    @Override
    public void onDragImageViewClick(final View v) {
        Log.i(TAG, "onDragImageViewClick: v=" + v.toString());
        if (v.getTag() == null) {
            return;
        }
        Object obj = v.getTag();
        if (obj instanceof Integer) {
            return;
        }
        final BtnParams btnParams = (BtnParams) v.getTag();
        switch (btnParams.getBelongBtn()) {
            case Q:
                break;
            case A:
                break;
            case B:
                break;
            case X:
                break;
            case Y:
                break;
            case L1:
                break;
            case R1:
                break;
            case L2:
                break;
            case R2:
                break;
            case L:
                break;
            case R:
                showBtnDialog(((BtnParams) v.getTag()).getBelongBtn());
                return;
            case UP:
                break;
            case DOWN:
                break;
            case LEFT:
                break;
            case RIGHT:
                break;
            case MOUSE_RIGHT:
                break;
            default:
                break;
        }
        if (SimpleUtil.isSaveToXml) {
            if (btnParams.iHaveChild() || btnParams.iAnChild()) {
                SimpleUtil.addMsgBottomToTop(getContext(), getContext().getString(R.string.kbv_type3onlyone), true);
                return;
            }
            List<String> items = new ArrayList<>();
            items.add(getContext().getString(R.string.kbv_settokeytype));
            items.add(getContext().getString(R.string.kbv_del) + btnParams.getBelongBtn());
            List<Runnable> runnables = new ArrayList<>();
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    btnParams.setKeyType(3);
                    v.setBackgroundResource(BtnParamTool.getBtnBelongColor(btnParams));
                }
            });
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    removeBtn(btnParams);
                }
            });
            SimpleUtil.addRadioGrouptoTop(getContext(), getContext().getString(R.string.kbv_keyopreation), items, runnables, new Runnable() {
                @Override
                public void run() {
                    BtnParamTool.setBtnParamsChanged(true);
                }
            }, null);
        }

    }

    private DragImageView whatImg;
    private long mWhatAddTime;
    private boolean mEnableSetting = true;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("touch event", "getX=" + event.getX() + ",getY=" + event.getY());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v == mBarWhat) {
                if (BtnParamTool.getBtnNormalBtn(Btn.Q).img != null) {
                    SimpleUtil.addMsgBottomToTop(getContext(), getContext().getString(R.string.kbv_hasakey), true);
                    return true;
                }
                return true;

            } else if (v == mIvMenuBtnL) {

                if (BtnParamTool.getBtnNormalBtn(Btn.L) != null && BtnParamTool.getBtnNormalBtn(Btn.L).img != null) {
                    Toast.makeText(getContext(), getContext().getString(R.string.kbv_hasexist),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

            } else if (v == mIvMenuBtnR) {
                if (BtnParamTool.getBtnNormalBtn(Btn.R) != null && BtnParamTool.getBtnNormalBtn(Btn.R).img != null) {
                    Toast.makeText(getContext(), getContext().getString(R.string.kbv_hasexist),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            } else if (v == mIvMenuBtnSetting) {
                showTab();
                return true;
            } else if (v == mFlMain) {
                mWhatAddTime = System.currentTimeMillis();
                Message message = new Message();
                message.what = HANDLE_ADDWHAT;
                message.arg1 = (int) event.getX();
                message.arg2 = (int) event.getY();
                mHandler.sendMessageDelayed(message, 800);
                return true;
            }
            DragShadowBuilder mysBuilder = new DragShadowBuilder(v);
            v.startDrag(null, mysBuilder, null, 0);
            mCopyingBtn = v;
            //震动提示
            vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                vibrator.vibrate(30);
            } else {
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (System.currentTimeMillis() - mWhatAddTime < 700) {
                mHandler.removeMessages(HANDLE_ADDWHAT);
            }
            mWhatAddTime = 0;
        }
        return false;

    }

    private void showTab() {
        if (!mEnableSetting) {
            return;
        }
        mEnableSetting = false;
        new IniTab(getContext()).show();
    }

    private void initEyes() {
        if (BtnParamTool.isShowKbFloatView(getContext())) {
            mIvKeymap.setImageResource(R.mipmap.keymap_show);
            mTvKeymap.setText(R.string.kbv_show);
            KeyboardFloatView.getInstance(getContext()).show();
        } else {
            mIvKeymap.setImageResource(R.mipmap.keymap_dismiss);
            mTvKeymap.setText(R.string.kbv_hide);
            KeyboardFloatView.getInstance(getContext()).dismiss();
        }
    }

    private void refreshKeymapBtn() {
        if (mTvKeymap.getText().toString().equals(getContext().getString(R.string.kbv_show))) {
            mIvKeymap.setImageResource(R.mipmap.keymap_dismiss);
            mTvKeymap.setText(R.string.kbv_hide);
            BtnParamTool.setIsShowKbFloatView(getContext(), false);
            KeyboardFloatView.getInstance(getContext()).dismiss();
        } else {
            mIvKeymap.setImageResource(R.mipmap.keymap_show);
            mTvKeymap.setText(R.string.kbv_show);
            BtnParamTool.setIsShowKbFloatView(getContext(), true);
            KeyboardFloatView.getInstance(getContext()).show();
        }

    }

    @Override
    public void back(int id, Object obj) {
        if (id == 10004) {
            SimpleUtil.removeINormalCallback(this);
        } else if (id == 10013) {
            setUse((AOAConfigTool.Config) obj);
        } else if (id == 10014) {
            mEnableSetting = true;
        }else if (id == 10016) {//关闭了悬浮窗
            SimpleUtil.log("debugr 悬浮窗关闭");
            SimpleUtil.removeINormalCallback(this);
        }
    }

    private void setUse(AOAConfigTool.Config config) {
        SimpleUtil.saveToShare(getContext(), "ini", "gloabkeyconfig", "default#Z%W#" + config.getmConfigName() + "#Z%W#" + config.getmTabValue() + "#Z%W#" + config.getmBelongGame());
        BtnParamTool.setComfirGame(config.getmBelongGame());
        BtnParamTool.loadBtnParamsFromPrefs(getContext());
        loadUi();
    }

    /**
     * 按钮类型
     */
    public enum Btn {
        /**
         * 尚未设置类型的按钮
         */
        Q("q"),
        A("a"),
        B("b"),
        X("x"),
        Y("y"),
        L1("l1"),
        R1("r1"),
        L2("l2"),
        R2("r2"),
        /**
         * 左摇杆
         */
        L("l"),
        /**
         * 右摇杆
         */
        R("r"),
        UP("up"),
        DOWN("down"),
        LEFT("left"),
        RIGHT("right"),
        /**
         * 左摇杆按键
         */
        THUMBL("thumbl"),
        /**
         * 右摇杆按键
         */
        THUMBR("thumbr"),
        /**
         * 开始键
         */
        START("start"),
        /**
         * 返回键
         */
        BACK("back"),

        /**
         * 多功能键
         */
        ESC("esc"),
        TAB("tab"),
        CAPS("caps"),
        SHIFT_LEFT("shift_left"),
        CTRL_LEFT("ctrl_Left"),
        WIN("win"),
        ALT_LEFT("alt_left"),
        SPACES("spaces"),
        NUM_1("num_1"),
        NUM_2("num_2"),
        NUM_3("num_3"),
        NUM_4("num_4"),
        NUM_5("num_5"),
        NUM_6("num_6"),
        NUM_7("num_7"),
        KEY_Q("key_q"),
        KEY_W("key_w"),
        KEY_E("key_e"),
        KEY_R("key_r"),
        KEY_T("key_t"),
        KEY_A("key_a"),
        KEY_S("key_s"),
        KEY_D("key_d"),
        KEY_F("key_f"),
        KEY_G("key_g"),
        KEY_H("key_h"),
        KEY_Z("key_z"),
        KEY_X("key_x"),
        KEY_C("key_c"),
        KEY_V("key_v"),
        KEY_B("key_b"),
        KEY_N("key_n"),
        KEY_Y("key_y"),

        //鼠标
        MOUSE_LEFT("mouse_left"),
        MOUSE_IN("mouse_in"),
        MOUSE_RIGHT("mouse_right"),

        MOUSE_SIDE_FRONT("key_mouse_side_front"),
        MOUSE_SIDE_BACK("key_mouse_side_back"),

        KEY_M("key_m"),
        //2017-12-20
        KEY_DOUHAO("key_douhao"),
        KEY_JUHAO("key_juhao"),
        KEY_XIEGANG("key_xiegang"),
        SHIFT_RIGHT("shift_right"),
        ALT_RIGHT("alt_right"),
        CTRL_RGHT("ctrl_right"),
        ENTER("enter"),
        F1("f1"),
        F2("f2"),
        F3("f3"),
        F4("f4"),
        F5("f5"),
        F6("f6"),
        F7("f7"),
        F8("f8"),
        F9("f9"),
        F10("f10"),
        F11("f11"),
        F12("f12"),
        NUM_8("num_8"),
        NUM_9("num_9"),
        NUM_0("num_0"),
        SUB("sub"),
        ADD("add"),
        KEY_U("key_u"),
        KEY_I("key_i"),
        KEY_O("key_o"),
        KEY_P("key_p"),
        KEY_J("key_j"),
        KEY_K("key_k"),
        KEY_L("key_l"),
        KEY_LEFT_BEGIN("key_left_begin"),
        KEY_RIGHT_END("key_right_end"),
        KEY_YINHAO("key_yinhao"),
        KEY_MAOHAO("key_maohao"),
        KEY_FANXIEGANG("key_fanxiegang"),
        GUNLUN("gunlun");


        private String name;

        Btn(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public String getPrefR() {
            return "pref_" + this.name + "_r";
        }

        public String getParent() {
            return "pref_" + this.name + "_parent";
        }

        public String getPrefX() {
            return "pref_" + this.name + "_x";
        }

        public String getPrefY() {
            return "pref_" + this.name + "_y";
        }

        public String getPrefEX() {
            return "pref_" + this.name + "_ex";
        }

        public String getPrefEY() {
            return "pref_" + this.name + "_ey";
        }

        public String getPrefXDP() {
            return "pref_" + this.name + "_x_dp";
        }

        public String getPrefYDP() {
            return "pref_" + this.name + "_y_dp";
        }

        public String getPrefMode() {
            return "pref_" + this.name + "_m";
        }

        //步长
        public String getPrefStep() {
            return "pref_" + this.name + "_step";
        }

        public String getPrefFrequency() {
            return "pref_" + this.name + "_frequency";
        }

        //第二次映射
        public String getPrefType() {
            return "pref_" + this.name + "_type";
        }

        public String getPrefBtn2() {
            return "pref_" + this.name + "_btn2";
        }

        public String getPrefSwitch() {
            return "pref_" + this.name + "_switch";
        }
    }


}
