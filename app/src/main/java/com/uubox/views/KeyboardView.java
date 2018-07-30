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
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.uubox.padtool.MainService;
import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.BtnUtil;
import com.uubox.tools.IniAdapter;
import com.uubox.tools.InjectUtil;
import com.uubox.tools.SaveBtnParamsTask;
import com.uubox.tools.SimpleUtil;

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
     * 配置（SharedPreferences）文件名
     */
    private static final String SP_FILE_NAME = "keyboard";
    /**
     * 按钮总数量
     */
    private static final int BTN_COUNT = Btn.values().length + 1;

    /**
     * 屏幕高度
     */
    private static int mScreenHeight_ = -1;
    static private ArrayList keys;
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
    LinearLayout mLlKeymap;
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

    /**
     * 保存创建的全部按钮，按钮具有唯一性
     */
    /**
     * 容器
     */
    private FrameLayout mFlMain;
    /**
     * 测试界面提示语
     */
    private TextView mTvTip;
    /**
     * 模式变化监听
     */
    private OnModeChangeListener mOnModeChangeListener;
    /**
     * 记录菜单按钮的点击次数
     */
    private int mIvMenuClickCount = 0;
    /**
     * 用于计算菜单按钮的5次连按是否在5秒内完成
     */
    private long mCurrentTimeMillis = 0;
    private Object tag;
    private float temp;
    private Drawable drawable;
    private boolean dialogShow;
    private Vibrator vibrator;
    private BtnDialogActivity btnDialogActivity;

    @SuppressLint("ResourceType")
    public KeyboardView(Context context) {
        super(context);
        Log.i(TAG, "KeyboardView: ");
        SimpleUtil.addINormalCallback(this);
        GuiStep.getInstance().init(context, "skip_note");
        LayoutInflater.from(context).inflate(R.layout.view_keyboard, this, true);
        findViews();
        initViews();
        initScreenParams();
        // 重新载入按钮参数
        InjectUtil.loadBtnParamsFromPrefs(getContext());
        loadUi();

    }


    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "KeyboardView: ");

        LayoutInflater.from(context).inflate(R.layout.view_keyboard, this, true);
        findViews();
        initViews();
        initScreenParams();
        // 重新载入按钮参数
        InjectUtil.loadBtnParamsFromPrefs(getContext());
        loadUi();

    }


    private void findViews() {
        Log.i(TAG, "findViews: ");
        mFlMain = this.findViewById(R.id.fl_main);
        mFlMenu = this.findViewById(R.id.fl_menu);
        mIvMenu = this.findViewById(R.id.iv_menu);
        mTvSave = this.findViewById(R.id.tv_save);
        mLlClose = findViewById(R.id.ll_close);
        mLlKeymap = findViewById(R.id.ll_keymap);
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
        mTvTip = this.findViewById(R.id.tv_tip);

        mImgExit = findViewById(R.id.iv_menu_btn_exit);
        /**
         * 新建数组存放所有按键
         */
        keys = new ArrayList<>(14);
        keys.add(Btn.A);
        keys.add(Btn.B);
        keys.add(Btn.L);
        keys.add(Btn.R);
        keys.add(Btn.X);
        keys.add(Btn.Y);
        keys.add(Btn.UP);
        keys.add(Btn.DOWN);
        keys.add(Btn.RIGHT);
        keys.add(Btn.LEFT);
        keys.add(Btn.L2);
        keys.add(Btn.L1);
        keys.add(Btn.R2);
        keys.add(Btn.R1);

    }

    private void initViews() {

        /**
         * 背景半透效果
         */
        mFlMain.getBackground().setAlpha(0);
        mFlMain.setOnDragListener(this);
        mFlMain.setOnClickListener(this);

        mIvMenu.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
        mLlClose.setOnClickListener(this);
        mLlKeymap.setOnClickListener(this);
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
                InjectUtil.getBtnNormalBtn(Btn.Q).setBelongBtn(Btn.Q);
                iv.setTag(InjectUtil.getBtnNormalBtn(Btn.Q));
                if (mCopyingBtn == mBarWhat) {
                    InjectUtil.getBtnNormalBtn(Btn.Q).img = iv;
                } else if (mCopyingBtn == mIvMenuBtnL) {
                    iv.setTag(InjectUtil.getBtnNormalBtn(Btn.L));
                    InjectUtil.getBtnNormalBtn(Btn.L).img = iv;
                    btnRadius = mIvJoystick.getWidth() / 2;
                    InjectUtil.setBtnPositionX(Btn.L, (int) event.getX());
                    InjectUtil.setBtnPositionY(Btn.L, (int) event.getY());
                    InjectUtil.setBtnRadius(Btn.L, mIvJoystick.getWidth() / 2);
                } else if (mCopyingBtn == mIvMenuBtnR) {
                    iv.setTag(InjectUtil.getBtnNormalBtn(Btn.R));
                    InjectUtil.getBtnNormalBtn(Btn.R).img = iv;
                    btnRadius = mIvJoystick.getWidth() / 2;
                    SimpleUtil.log("ACTION_DROP:" + (int) event.getX() + "," + (int) event.getY());
                    InjectUtil.setBtnPositionX(Btn.R, (int) event.getX());
                    InjectUtil.setBtnPositionY(Btn.R, (int) event.getY());
                    InjectUtil.setBtnRadius(Btn.R, mIvJoystick.getWidth() / 2);
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
        InjectUtil.setComfirGame(gloabkeyconfig.split("#Z%W#", -1)[3]);
        KeyboardEditWindowManager.getInstance().hideOrShowBottomUIMenu(false);
        List<String> items = new ArrayList<>();
        items.add("保存到当前配置");
        items.add("保存到已有的配置");
        items.add("新建一个配置");
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(new Runnable() {
            @Override
            public void run() {
                String curIni = (String) SimpleUtil.getFromShare(getContext(), "ini", "gloabkeyconfig", String.class, "");
                String iniName = "";
                if (curIni != null && !curIni.isEmpty()) {
                    iniName = curIni.split("#Z%W#", -1)[1];
                }
                if (iniName.endsWith("[官方]")) {
                    SimpleUtil.toastTop(getContext(), "不能保存到官方配置");
                    return;
                }
                if (curIni == null || curIni.isEmpty()) {
                    String tempIni = InjectUtil.getComfirGame() + System.currentTimeMillis() + "#Z%W#" + InjectUtil.getComfirGame() + "_1#Z%W#" + InjectUtil.getComfirGame() + "_1";
                    SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                    saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                    saveBtnParamsTask.execute(tempIni);
                    //InjectUtil.saveBtnParams(getContext(), tempIni);
                } else {
                    SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                    saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                    saveBtnParamsTask.execute(curIni);
                    //InjectUtil.saveBtnParams(getContext(), curIni);
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
                    inibt_cur.setText("当前没有使用任何配置");
                } else {
                    curName = gloabkeyconfig.split("#Z%W#", -1)[1];
                    inibt_cur.setTextColor(Color.YELLOW);
                    InjectUtil.setComfirGame(gloabkeyconfig.split("#Z%W#", -1)[3]);
                    inibt_cur.setText("当前使用:" + InjectUtil.getComfirGame() + "/" + curName);

                }

                title.measure(0, 0);
                ListView listView = button_ini_content.findViewById(R.id.inibt_list);
                final List<IniAdapter.IniObj> iniDatas = new ArrayList<>();
                IniAdapter adapter = new IniAdapter(getContext(), iniDatas);
                SharedPreferences sharedPreferences2 = getContext().getSharedPreferences(InjectUtil.getComfirGameTab(), 0);
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
                    iniObj.state = iniObj.name.equals(curName) ? "[使用中]" : null;

                    //发现官方配置，应该直接移除
                    if (iniObj.name.endsWith("[官方]")) {
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

                        if (iniDatas.get(position).name.endsWith("[官方]")) {
                            //SimpleUtil.toast(getContext().getString(R.string.msg_a5));
                            return;
                        }
                        InjectUtil.saveBtnParams(getContext(), iniDatas.get(position).whole);
                        //SimpleUtil.toast(getContext().getString(R.string.save_success));
                        InjectUtil.setBtnParamsChanged(false);
                        KeyboardEditWindowManager.getInstance().removeView(button_ini_content);
                        KeyboardEditWindowManager.getInstance().close();
                        //保存之后使用
                        String[] sp = iniDatas.get(position).whole.split("#Z%W#", -1);
                        SimpleUtil.saveToShare(getContext(), "ini", "gloabkeyconfig", "default#Z%W#" + sp[1] + "#Z%W#" + sp[2] + "#Z%W#" + InjectUtil.getComfirGame());
                        InjectUtil.loadBtnParamsFromPrefs(getContext());
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
                items.put("配置名称", InjectUtil.getComfirGame() + "_" + SimpleUtil.getSha1(System.currentTimeMillis() + "").substring(0, 5));

                SimpleUtil.addEditToTop(getContext(), "保存配置", items, null, new Runnable() {
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

        SimpleUtil.addRadioGrouptoTop(getContext(), "保存", items, runnables, null, new Runnable() {
            @Override
            public void run() {
                for (HashMap<String, Object> obj : addingBtns) {
                    Btn btn = (Btn) obj.get("btn");
                    boolean isSecond = (Boolean) obj.get("isSecond");

                    if (!isSecond) {
                        InjectUtil.resetBtnParams(btn);
                    } else {
                        InjectUtil.resetRepeatBtnParams(btn);
                    }
                }
                addingBtns.clear();

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
            if (!InjectUtil.canSaveIniToXml(getContext(), iniName)) {
                Toast.makeText(getContext(), "配置名称已经存在",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (iniName == null || iniName.isEmpty()) {
                Toast.makeText(getContext(), "配置名称不能为空",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (iniName.equals("#*#*wisega2015*#*#")) {
                //SimpleUtil.toast(getContext().getString(R.string.ma4));
                return;
            }


            SimpleUtil.log("preare to save comfirgame:" + InjectUtil.getComfirGame());
            if (InjectUtil.getComfirGame() == null) {

                // SimpleUtil.addMsgtoTop(getContext(),"温馨提示");


                InjectUtil.setComfirGame(iniName);
                SimpleUtil.log("preare to save setcomfirgame:" + iniName);
            }

            KeyboardEditWindowManager.getInstance().removeTop();
            List<String> items = new ArrayList<>();
            items.add("保存到目录【" + InjectUtil.getComfirGame() + "】");
            items.add("新建配置目录");
            List<Runnable> tasks = new ArrayList<>();
            tasks.add(new Runnable() {
                @Override
                public void run() {
                    SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                    saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                    saveBtnParamsTask.execute(InjectUtil.getComfirGame() + System.currentTimeMillis() + "#Z%W#" + iniName + "#Z%W#" + iniName);
                    SimpleUtil.removeINormalCallback(newinietback);
                }
            });
            tasks.add(new Runnable() {
                @Override
                public void run() {

                    LinkedHashMap<String, String> items = new LinkedHashMap<>();
                    items.put("目录名", "");
                    SimpleUtil.addEditToTop(getContext(), "新建配置目录", items, null, new Runnable() {
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
                                Toast.makeText(getContext(), "目录名称不能为空！",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SharedPreferences allKeysConfigsTable = getContext().getSharedPreferences("KeysConfigs", 0);
                            Iterator<String> allIt = allKeysConfigsTable.getAll().keySet().iterator();
                            while (allIt.hasNext()) {
                                if (allIt.next().equals(newIniContentName)) {
                                    Toast.makeText(getContext(), "目录名称已存在！",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                            InjectUtil.setComfirGame(newIniContentName);
                            SaveBtnParamsTask saveBtnParamsTask = new SaveBtnParamsTask(getContext());
                            saveBtnParamsTask.setmKeyboardView(KeyboardView.this);
                            saveBtnParamsTask.execute(InjectUtil.getComfirGame() + System.currentTimeMillis() + "#Z%W#" + iniName + "#Z%W#" + iniName);
                            SimpleUtil.removeINormalCallback(newinietback);

                        }
                    });


                }
            });
            SimpleUtil.addRadioGrouptoTop(getContext(), "保存配置", items, tasks, null, new Runnable() {
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


        } else if (v == mLlKeymap) {
            // 更新显示状态到配置中
            // 刷新键位按钮的UI
            refreshKeymapBtn();
        } else if (v == mTvSave | v == mLlClose) {
            if (KeyboardEditWindowManager.getInstance().rootViewChildCount() > 1) {
                return;
            }
            boolean changed = InjectUtil.hasBtnParamsChanged();
            if (changed) {
                //确认保存对话框
                showSaveDialog();

            } else {
                //SimpleUtil.notifyall_(1, null);
                KeyboardEditWindowManager.getInstance().close();
            }
        } else if (v == mImgExit) {
            SimpleUtil.addMsgtoTop(getContext(), "温馨提示", "确定退出程序吗？", new Runnable() {
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


    private ArrayList<HashMap<String, Object>> addingBtns = new ArrayList<>();

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
        if (InjectUtil.getBtnNormalBtn(btn).img != null) {
            List<String> items = new ArrayList<>();
            items.add("联动");
            items.add("交替");
            items.add("清除 " + btn);
            List<Runnable> runnables = new ArrayList<>();
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    dialogShow = false;
                    if (InjectUtil.getBtnRepeatBtn2(btn) != null) {
                        mFlMain.removeView(InjectUtil.getBtnRepeatBtn2(btn).img);
                    }

                    InjectUtil.setBtnRepeatType(btn, 1);

                    //设置btn2
                    BtnParams params = new BtnParams();
                    //设置主节点，表示该参数是从属
                    params.setBelongButton(true);
                    params.setBelongBtn(btn);

                    drawable = getBtnDrawable(params);
                    if (drawable != null) {
                        whatImg.setImageDrawable(drawable);
                    }


                    whatImg.setTag(params);

                    params.setX(
                            (int) whatImg.getX() + whatImg.getWidth() / 2);
                    params.setY(
                            (int) whatImg.getY() + whatImg.getHeight() / 2);
                    params.setR(whatImg.getWidth() / 2);

                    params.img = whatImg;
                    params.img.setBackgroundColor(Color.parseColor("#8833cc33"));
                    whatImg = null;
                    InjectUtil.setBtnRepeatBtn2(btn, params);

                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("btn", btn);
                    //是否属于第二模式
                    obj.put("isSecond", true);
                    addingBtns.add(obj);
                }
            });
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    dialogShow = false;
                    if (InjectUtil.getBtnRepeatBtn2(btn) != null) {
                        mFlMain.removeView(InjectUtil.getBtnRepeatBtn2(btn).img);
                    }

                    InjectUtil.setBtnRepeatType(btn, 2);

                    //设置btn2
                    BtnParams params = new BtnParams();
                    //设置主节点，表示该参数是从属
                    params.setBelongButton(true);
                    params.setBelongBtn(btn);

                    drawable = getBtnDrawable(params);
                    if (drawable != null) {
                        whatImg.setImageDrawable(drawable);
                    }


                    whatImg.setTag(params);

                    params.setX(
                            (int) whatImg.getX() + whatImg.getWidth() / 2);
                    params.setY(
                            (int) whatImg.getY() + whatImg.getHeight() / 2);
                    params.setR(whatImg.getWidth() / 2);

                    params.img = whatImg;
                    params.img.setBackgroundColor(Color.parseColor("#88FF4081"));
                    whatImg = null;
                    InjectUtil.setBtnRepeatBtn2(btn, params);

                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("btn", btn);
                    //是否属于第二模式
                    obj.put("isSecond", true);
                    addingBtns.add(obj);
                }

            });
            runnables.add(new Runnable() {
                @Override
                public void run() {
                    dialogShow = false;
                    removeBtn(InjectUtil.getBtnNormalBtn(btn));
                    mFlMain.removeView(whatImg);
                    whatImg = null;
                }
            });
            SimpleUtil.addRadioGrouptoTop(getContext(), "选择模式", items, runnables, new Runnable() {
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
//            btn.setKeyRepeatType(0);
            drawable = getBtnDrawable(InjectUtil.getBtnNormalBtn(btn));
        }

        if (drawable != null) {
            whatImg.setImageDrawable(drawable);
        }
        HashMap<String, Object> obj = new HashMap<>();
        obj.put("btn", btn);
        //是否属于第二模式
        obj.put("isSecond", false);
        addingBtns.add(obj);

        whatImg.setTag(InjectUtil.getBtnNormalBtn(btn));

        InjectUtil.setBtnPositionX(btn,
                (int) whatImg.getX() + whatImg.getWidth() / 2);
        InjectUtil.setBtnPositionY(btn,
                (int) whatImg.getY() + whatImg.getHeight() / 2);
        InjectUtil.setBtnRadius(btn, whatImg.getWidth() / 2);

        InjectUtil.getBtnNormalBtn(btn).img = whatImg;
        InjectUtil.getBtnNormalBtn(btn).setBelongBtn(btn);
        whatImg = null;
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
        View vv = InjectUtil.getBtnNormalBtn(Btn.R).img;
        SimpleUtil.log("vv3:" + vv);
        Log.i(TAG, "onDragStart: 在拖动开始" + v.toString());
        // 显示删除按钮
        mIvMenu.setImageDrawable(getResources().getDrawable(R.mipmap.del));
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
                removeBtn((BtnParams) v.getTag());

                Log.w(TAG, "onDragFinish: getId()=" + v.getId() + "getTag()=" + v.getTag());

                // FIXME: 2017/9/18 删除按钮后再编辑页面显示按钮
                tag = v.getTag();

//            setVisible(tag);

                tag = null;
            }
        }
        // 松开位置位于其他区域，保存按钮坐标
        else {
            if (!(v.getTag() instanceof Integer)) {
                final int x = (int) v.getX() + v.getWidth() / 2;
                final int y = (int) v.getY() + v.getHeight() / 2;
                BtnParams params = (BtnParams) v.getTag();
                if (params.getX() != x || params.getY() != y) {
                    params.setX(x);
                    params.setY(y);
                    InjectUtil.setBtnParamsChanged(true);
                }

            }

        }
        // 显示添加按钮
        mIvMenu.setImageDrawable(getResources().getDrawable(R.mipmap.icon_edit));
    }

    @Override
    public void onScaleStart(View v) {
        Log.i(TAG, "onScaleStart: " + v.toString());
        // 显示添加按钮
        mIvMenu.setImageDrawable(getResources().getDrawable(R.mipmap.icon_edit));
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
        if (params.isBelongButton()) {
            InjectUtil.setBtnRadius2(params.getBelongBtn(), v.getWidth() / 2);
        } else {
            InjectUtil.setBtnRadius(params.getBelongBtn(), v.getWidth() / 2);
        }
        InjectUtil.setBtnParamsChanged(true);
    }

    /**
     * 初始化屏幕的宽度和高度
     */
    private void initScreenParams() {

        /*
      屏幕宽度
     */
        mScreenHeight_ = SimpleUtil.zoomx;
    }

    private void clearAllView() {
        ConcurrentMap<Btn, BtnParams> buttons = InjectUtil.getmBtnParams();
        Iterator<Btn> it = buttons.keySet().iterator();
        while (it.hasNext()) {
            Btn btn = it.next();
            BtnParams btnParams = buttons.get(btn);
            if (btnParams.img != null) {
                mFlMain.removeView(btnParams.img);
            }
            if (btnParams.btn2 != null && btnParams.btn2.img != null) {
                mFlMain.removeView(btnParams.btn2.img);
            }
        }
    }

    /**
     * 载入之前保存的UI，如果之前有保存按钮的参数，则将创建该按钮并将其坐标和半径设置为保存的值。
     */
    public void loadUi() {
        Log.i(TAG, "loadUi:   载入之前保存的UI，如果之前有保存按钮的参数，则将创建该按钮并将其坐标和半径设置为保存的值");
        int childCount = allView.size();
        for (int i = 0; i < childCount; i++) {
            mFlMain.removeView(allView.get(i));
        }
        ConcurrentMap<Btn, BtnParams> buttons = InjectUtil.getmBtnParams();
        Iterator<Btn> it = buttons.keySet().iterator();
        while (it.hasNext()) {
            Btn btn = it.next();
            makeButtonView(btn, buttons.get(btn), false);
        }
        GuiStep.getInstance().pushBuffToGui("key_union", "联动按钮，绿色标记，按钮同时触发");
        GuiStep.getInstance().pushBuffToGui("key_reflect", "互斥按钮，红色标记，按钮交替触发");
        GuiStep.getInstance().pushBuffToGui("key_normal", "普通按钮，蓝色标记，单点触发");

        GuiStep.getInstance().addToGui(mLlClose, "保存按钮，当您调整好配置之后可以点击该按钮以保存配置");
        GuiStep.getInstance().addToGui(mIvKeymap, "开关小健位，如果您已经熟悉了按键的位置，可以在这里点击关闭实时小健位");
        GuiStep.getInstance().addToGui(mIvMenuBtnSetting, "设置功能，可以对游戏配置进行管理以及查看一些帮助文档");
        GuiStep.getInstance().addToGui(mImgExit, "退出，点击退出按钮可以彻底退出程序");
    }

    //奇怪，用帧布局移除老是只移除基数的角标,增加一个列表保存所有的View
    private List<View> allView = new ArrayList<>();

    private void makeButtonView(final Btn btn, final BtnParams params, boolean isBelong) {
        int x = params.getX();
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
        iv.setScaleListener(this);
        iv.setClickListener(this);
        LayoutParams layoutParams =
                new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //按钮试图的位置
        int r = params.getR();
        iv.setLayoutParams(layoutParams);
        iv.measure(0, 0);
        int hhh = iv.getMeasuredHeight();
        int www = iv.getMeasuredWidth();
        if (r != www && r > 0) {
            layoutParams.width = 2 * r;
            layoutParams.height = 2 * r;
            www = layoutParams.width;
            hhh = layoutParams.height;
        }

        layoutParams.leftMargin = x - www / 2;
        layoutParams.topMargin = y - hhh / 2;
        iv.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        allView.add(iv);
        mFlMain.addView(iv, layoutParams);

        if (btn == Btn.L) {
            mIvMenuBtnL.setVisibility(GONE);
            GuiStep.getInstance().addToGui(iv, "控制方向按键，如果您觉得按下【W】键人物不能疾跑，建议将该按键放大以达到效果");
        } else if (btn == Btn.R) {
            mIvMenuBtnR.setVisibility(GONE);
            GuiStep.getInstance().addToGui(iv, "鼠标位置，可以通过点击打开【鼠标设置】界面，可以调节鼠标灵敏度和鼠标呼出方式");
        } else if (!isBelong) {
            GuiStep.getInstance().addtToBuff("key_normal", iv);
        } else {
            if (InjectUtil.getBtnNormalBtn(btn).getKeyRepeatType() == 1) {
                GuiStep.getInstance().addtToBuff("key_union", iv);
            } else {
                GuiStep.getInstance().addtToBuff("key_reflect", iv);
            }
        }

        params.img = iv;
        if (isBelong) {
            params.setBelongButton(true);
            params.img.setBackgroundColor(InjectUtil.getBtnNormalBtn(btn).getKeyRepeatType() == 2 ? Color.parseColor("#77FF4081") : Color.parseColor("#7733cc33"));
        }


        //表示该按钮参数有附属，则需要递归一次
        if (params.getKeyRepeatType() != 0) {
            makeButtonView(btn, InjectUtil.getBtnNormalBtn(btn).btn2, true);
        }
        //SimpleUtil.log(btn.name + "   " + x + "," + y + "," + layoutParams.leftMargin + "," + layoutParams.topMargin);
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

        return new RectF(view.getX(), view.getY(), view.getX() + view.getWidth(),
                view.getY() + view.getHeight());
    }

    /**
     * 移除目标按钮及按钮相关的配置
     */
    private void removeBtn(final BtnParams btnParams) {
        Log.e(TAG, "removeBtn:     移除目标按钮及按钮相关的配置 btn=" + btnParams.toString());
        if (btnParams == null) {
            return;
        }

        //该按钮使附属按钮
        if (btnParams.isBelongButton()) {
            mFlMain.removeView(btnParams.img);
            InjectUtil.resetRepeatBtnParams(btnParams.getBelongBtn());
            btnParams.img = null;
        } else {//常规按钮
            mFlMain.removeView(btnParams.img);
            InjectUtil.resetBtnParams(btnParams.getBelongBtn());
            btnParams.img = null;

            //还有附属按键，则一起死！！！！！！
            if (btnParams.getKeyRepeatType() != 0) {
                BtnParams subParams = btnParams.getBtn2();
                mFlMain.removeView(subParams.img);
                InjectUtil.resetRepeatBtnParams(subParams.getBelongBtn());
                subParams.img = null;
            }

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

        KeyboardEditWindowManager.getInstance().addView(new BtnDialogActivity().create(getContext()), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onDragImageViewClick(View v) {
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
                break;
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
    }

    private DragImageView whatImg;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("touch event", "getX=" + event.getX() + ",getY=" + event.getY());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (v == mBarWhat) {
                if (InjectUtil.getBtnNormalBtn(Btn.Q).img != null) {
                    SimpleUtil.addMsgBottomToTop(getContext(), "已经有一个自定义按钮", true);
                    return true;
                }
                // TODO: 2018/7/25 拖出自定义按钮  1. 触摸出现坐标; 2. 直接拖出
                if (whatImg == null) {
                    whatImg = new DragImageView(getContext());
                    whatImg.setTag(-1);

                    BtnParams param = new BtnParams();
                    param.setBelongBtn(Btn.Q);
                    Drawable drawable = getBtnDrawable(param);
                    if (drawable != null) {
                        whatImg.setImageDrawable(drawable);
                    }
                    whatImg.setDragListener(this);
                    whatImg.setScaleListener(this);
                    whatImg.setClickListener(this);


                    int[] position = new int[2];
                    v.getLocationInWindow(position);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(64, 64);
                    params.leftMargin = SimpleUtil.zoomy / 2;
                    params.topMargin = SimpleUtil.zoomx / 3;

                    mFlMain.addView(whatImg, params);


                } else {
                    //whatImg.setLayoutParams(layoutParams);
                    //mFlMain.postInvalidate();
                }


                Log.i("touch event", "onTouch: event=" + event.toString());
                return true;

            } else if (v == mIvMenuBtnL) {

                if (InjectUtil.getBtnNormalBtn(Btn.L).img != null) {
                    Toast.makeText(getContext(), "已经存在",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

            } else if (v == mIvMenuBtnR) {
                if (InjectUtil.getBtnNormalBtn(Btn.R).img != null) {
                    Toast.makeText(getContext(), "已经存在",
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            } else if (v == mIvMenuBtnSetting) {
                showTab();
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
        }
        return false;

    }

    private void showTab() {
        IniTab.getInstance().init(getContext(), this).show();
        IniTab.getInstance().addNotify(new IniTab.IButtonMenuCallback() {
            @Override
            public void back(int type, Object carryData) {
                if (type == 0) {
                    clearAllView();
                    InjectUtil.loadBtnParamsFromPrefs(getContext(), true);
                    loadUi();
                    IniTab.getInstance().removeNotify(this);
                }
            }
        });
    }
    private void initEyes() {
        if (InjectUtil.isShowKbFloatView(getContext())) {
            mIvKeymap.setImageResource(R.mipmap.keymap_show);
            mTvKeymap.setText("显示");
            KeyboardFloatView.getInstance(getContext()).show();
        } else {
            mIvKeymap.setImageResource(R.mipmap.keymap_dismiss);
            mTvKeymap.setText("隐藏");
            KeyboardFloatView.getInstance(getContext()).dismiss();
        }
    }

    private void refreshKeymapBtn() {

        if (mTvKeymap.getText().equals("显示")) {
            mIvKeymap.setImageResource(R.mipmap.keymap_dismiss);
            mTvKeymap.setText("隐藏");
            InjectUtil.setIsShowKbFloatView(getContext(), false);
            KeyboardFloatView.getInstance(getContext()).dismiss();
        } else {
            mIvKeymap.setImageResource(R.mipmap.keymap_show);
            mTvKeymap.setText("显示");
            InjectUtil.setIsShowKbFloatView(getContext(), true);
            KeyboardFloatView.getInstance(getContext()).show();
        }

    }

    @Override
    public void back(int id, Object obj) {
        if (id == 10004) {
            SimpleUtil.removeINormalCallback(this);
        } else if (id == 10013) {
            setUse((AOAConfigTool.Config) obj);
        }
    }

    private void setUse(AOAConfigTool.Config config) {
        SimpleUtil.saveToShare(getContext(), "ini", "gloabkeyconfig", "default#Z%W#" + config.getmConfigName() + "#Z%W#" + config.getmTabValue() + "#Z%W#" + config.getmContent());
        InjectUtil.setComfirGame(config.getmContent());
        InjectUtil.loadBtnParamsFromPrefs(getContext());
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

        public String getPrefX() {
            return "pref_" + this.name + "_x";
        }

        public String getPrefY() {
            return "pref_" + this.name + "_y";
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

    /**
     * @created by mk on 2017/12/2 18:33
     */


    /**
     * 回调接口
     */
    public interface KeyboardViewCallback {
        /**
         * 返回按钮点击
         */
        boolean onBackBtnClick();
    }

    /**
     * 模式变化监听
     */
    public interface OnModeChangeListener {

        /**
         * 进入测试模式
         */
        void enterTestMode();

        /**
         * 退出测试模式
         */
        void exitTestMode();
    }


}
