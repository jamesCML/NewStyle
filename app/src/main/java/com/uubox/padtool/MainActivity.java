package com.uubox.padtool;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pgyersdk.feedback.PgyerFeedbackManager;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.uubox.threads.IniTask;
import com.uubox.toolex.ScreenUtils;

import com.uubox.tools.BtnParamTool;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.SimpleUtil;
import com.uubox.views.KeyboardEditWindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
public class MainActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {
    private ProgressBar mProgress;
    private TextView mLoadMsg;
    private TextView mButton;
    private boolean mIsJugeFloat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SimpleUtil.DEBUG = CommonUtils.getAppVersionName(this).contains("debug");
        SimpleUtil.mFactoryMode = (Boolean) SimpleUtil.getFromShare(this, "ini", "factorymode", boolean.class);
        SimpleUtil.log("MainActivity-------------create------------>" + CommonUtils.getAppPkgName(this) + " " + CommonUtils.getAppVersionCode(this));
        setContentView(R.layout.activity_main);
        mProgress = findViewById(R.id.loading_pro);
        mLoadMsg = findViewById(R.id.loading_msg);
        mButton = findViewById(R.id.loading_bt);
        divAPPUSER();
        if(SimpleUtil.mAPPUSER== SimpleUtil.APPUSER.AGP)
        {
            findViewById(R.id.main_log).setVisibility(View.VISIBLE);
            mButton.setTextColor(getResources().getColor(R.color.grey_4));
            if (getWindowManager().getDefaultDisplay().getRotation() * Surface.ROTATION_90 == 1) {
                findViewById(R.id.main_parent).setBackgroundResource(R.mipmap.start_bg_l);
            } else {
                findViewById(R.id.main_parent).setBackgroundResource(R.mipmap.start_bg_p);
            }
        }
        else{
            findViewById(R.id.main_parent).setBackgroundResource(R.mipmap.load_bg);
            mButton.setBackgroundColor(Color.TRANSPARENT);
        }

        int a = ScreenUtils.getScreenWidth();
        int b = ScreenUtils.getScreenHeight();
        SimpleUtil.zoomx = Math.min(a, b);
        SimpleUtil.zoomy = Math.max(a, b);
        SimpleUtil.putOneInfoToMap("devpix", SimpleUtil.zoomx + "*" + SimpleUtil.zoomy);
        SimpleUtil.putOneInfoToMap("liuhai", (Integer) SimpleUtil.getFromShare(this, "ini", "LH", int.class, -1) + "");
        int saveY = (Integer) SimpleUtil.getFromShare(getBaseContext(), "ini", "zoomy", int.class);
        SimpleUtil.log("readY:" + SimpleUtil.zoomy + ",saveY:" + saveY);
        SimpleUtil.zoomy = Math.max(saveY, SimpleUtil.zoomy);

        SimpleUtil.saveToShare(this, "ini", "zoomx", SimpleUtil.zoomx);
        SimpleUtil.saveToShare(this, "ini", "zoomy", SimpleUtil.zoomy);

        SimpleUtil.log("屏幕分辨率:" + SimpleUtil.zoomx + "," + SimpleUtil.zoomy);

        mButton.setOnClickListener(this);
        findViewById(R.id.loading_feedback).setOnClickListener(this);
        findViewById(R.id.loading_feedback).setOnLongClickListener(this);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        mButton.startAnimation(alphaAnimation);
        SimpleUtil.log("MainActivity-------------create over------------" + hashCode());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 79009);
            }
        }

    }

    private void divAPPUSER() {
        String appName = CommonUtils.getAppName(this);
        if (appName.equals("UUBOX")) {
            SimpleUtil.mAPPUSER = SimpleUtil.APPUSER.WISEGA;
        } else if (appName.equals("FPSBOX") || appName.equals("FPSDOCK")) {
            SimpleUtil.mAPPUSER = SimpleUtil.APPUSER.FPS;
        }else if (appName.equals("AGP")) {
            SimpleUtil.mAPPUSER = SimpleUtil.APPUSER.AGP;
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        SimpleUtil.log("MainActivity-------------resume------------" + hashCode() + " pid:" + Process.myPid());
        /*int jLH = (Integer) SimpleUtil.getFromShare(this, "ini", "LH", int.class, -1);
        //jLH = -1;
        if (jLH == -1)//需要判断一下刘海屏
        {
            startActivityForResult(new Intent(this, InitActivity.class), 11111);
            return;
        }*/
        SimpleUtil.LIUHAI = 0;
        if (isFloatPermissionOK()) {
            mButton.setVisibility(View.GONE);
            SimpleUtil.log("isFloatPermissionOK");
            mLoadMsg.setText(R.string.main_entergameshowviews);
            runInit();
        } else {
            SimpleUtil.log("isFloatPermissionnotok");
        }
        SimpleUtil.log("MainActivity-------------resume over------------" + hashCode() + " pid:" + Process.myPid());

    }

    private void runInit() {
        SimpleUtil.log("runInit initask execute");
        if (SimpleUtil.mFactoryMode) {
            startActivity(new Intent(this, FactoryAct.class));
            startMainService();
            return;
        }
        exeIniTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 789:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    runInit();
                } else {
                    System.exit(0);
                }
                break;
            case 79009:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SimpleUtil.log("赋予存储权限OK");
                } else {
                    SimpleUtil.log("赋予存储权限fail");
                }
                break;
        }
    }

    public boolean isFloatPermissionOK() {

        if (mIsJugeFloat) {
            return true;
        }
        mIsJugeFloat = true;
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(getBaseContext())) {
                mLoadMsg.setText(R.string.main_openfloat);
                mButton.setText(R.string.main_opennow);
                mButton.setVisibility(View.VISIBLE);
                return false;
            }

        } else {
            if (!getAppOps(this)) {
                SimpleUtil.toast(MainActivity.this, getString(R.string.main_floatnot));
                mLoadMsg.setText(R.string.main_openfloat);
                mButton.setText(R.string.main_opennow);
                mButton.setVisibility(View.VISIBLE);
                return false;
            }
        }
        return true;
    }

    private boolean getAppOps(Context context) {
        try {
            Object object = context.getSystemService(Context.APP_OPS_SERVICE);
            if (object == null) {
                return false;
            }
            Class localClass = object.getClass();
            Class[] arrayOfClass = new Class[3];
            arrayOfClass[0] = Integer.TYPE;
            arrayOfClass[1] = Integer.TYPE;
            arrayOfClass[2] = String.class;
            Method method = localClass.getMethod("checkOp", arrayOfClass);
            if (method == null) {
                return false;
            }
            Object[] arrayOfObject1 = new Object[3];
            arrayOfObject1[0] = 24;
            arrayOfObject1[1] = Binder.getCallingUid();
            arrayOfObject1[2] = context.getPackageName();
            int m = (Integer) method.invoke(object, arrayOfObject1);
            return m == AppOpsManager.MODE_ALLOWED;
        } catch (Exception ex) {

        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loading_bt:
                if (mLoadMsg.getText().toString().contains(getString(R.string.main_float))) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 789);
                    } else {
                        Toast.makeText(MainActivity.this, R.string.main_floatplease, Toast.LENGTH_LONG).show();
                    }
                    return;
                }
                startMainService();

                //moveTaskToBack(true);
                break;
            case R.id.loading_feedback:
                feedback();
                break;
        }
    }

    private void startMainService() {
        Intent intent = new Intent(MainActivity.this, MainService.class);
        startService(intent);
        finish();
    }


    /**
     * 进入沉浸模式
     *
     * @param hasFocus
     * @created by mk on 2017/8/2 下午12:18
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private boolean mIsShowUpdate;

    private void checkUpdate(final int delay) {
        if (mIsShowUpdate) {
            return;
        }
        if(SimpleUtil.mAPPUSER== SimpleUtil.APPUSER.FPS)//小鸡用自己的平台
        {
            //exeIniTask();
            return;
        }
        SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                SimpleUtil.sleep(delay);
                boolean isNetOK = false;
                ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (connectivity != null) {
                    NetworkInfo info = connectivity.getActiveNetworkInfo();
                    if (info != null && info.isConnected()) {
                        if (info.getState() == NetworkInfo.State.CONNECTED) {
                            isNetOK = true;
                        }
                    }
                }
                SimpleUtil.log("开始检查新版本:" + isNetOK);
                if (!isNetOK) {
                    SimpleUtil.addMsgBottomToTop(MainActivity.this, getString(R.string.netdisable), true);
                    //exeIniTask();
                    return;
                }
                new PgyUpdateManager.Builder()
                        .setForced(true)
                        .setUserCanRetry(false)
                        .setDeleteHistroyApk(true)
                        .setUpdateManagerListener(new UpdateManagerListener() {
                            @Override
                            public void onNoUpdateAvailable() {
                                //没有更新是回调此方法
                                SimpleUtil.log("there is no new version");
                                //exeIniTask();
                        }

                            @Override
                            public void onUpdateAvailable(final AppBean appBean) {
                                mIsShowUpdate = true;
                                SimpleUtil.log("蒲公英版本:" + appBean.getVersionCode());
                                final boolean isForce = false;

                                SimpleUtil.addMsgtoTop(MainActivity.this, getString(R.string.main_verupdate), getString(R.string.main_findnewver) + appBean.getVersionName() + getString(R.string.main_updateenable) + "\n" + getString(R.string.main_updatemsg) + "\n" + appBean.getReleaseNote(),
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                mIsShowUpdate = false;
                                                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                    SimpleUtil.addMsgBottomToTop(MainActivity.this, getString(R.string.main_updatefail), true);
                                                    return;
                                                }
                                                SimpleUtil.addMsgBottomToTop(MainActivity.this, getString(R.string.main_loadstart), false);
                                                SimpleUtil.addWaitToTop(MainActivity.this, getString(R.string.main_loadproce0));
                                                PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                                            }
                                        }, new Runnable() {
                                            @Override
                                            public void run() {
                                                mIsShowUpdate = false;
                                                if (isForce) {
                                                    System.exit(0);
                                                } else {
                                                    //exeIniTask();
                                                }

                                            }
                                        }, false);//说明:双数表示强制升级，主要涉及一些重要结构调整必须升级 单数则不强制升级


                                //PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                            }

                            @Override
                            public void checkUpdateFailed(Exception e) {
                                //更新检测失败回调

                                e.printStackTrace();
                                SimpleUtil.log("检查更新失败 ");
                                // exeIniTask();
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
                                SimpleUtil.resetWaitTop(MainActivity.this);
                                SimpleUtil.addMsgBottomToTop(MainActivity.this, getString(R.string.main_updateexec), true);

                            }

                            @Override
                            public void downloadSuccessful(final Uri uri) {
                                SimpleUtil.log("download apk ok");
                                // 使用蒲公英提供的安装方法提示用户 安装apk
                                SimpleUtil.resetWaitTop(MainActivity.this);
                                SimpleUtil.addMsgBottomToTop(MainActivity.this, getString(R.string.main_updateok), false);
                                SimpleUtil.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        PgyUpdateManager.installApk(uri);
                                    }
                                }, 2000);
                            }

                            @Override
                            public void onProgressUpdate(Integer... integers) {
                                SimpleUtil.updateWaitTopMsg(getString(R.string.main_updateproc) + integers[0] + "%");
                                SimpleUtil.log("apkupdate download apk progress" + integers[0]);
                                //SimpleUtil.updateWaiting(MainActivity.this,"升级中 "+integers[0]+"/100");
                            }
                        }).register();
            }
        });
    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void exeIniTask() {
        SimpleUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                new IniTask(MainActivity.this, new IniTask.IIniexecallback() {
                    @Override
                    public void onPreExecute() {
                        SimpleUtil.log("IniTask onPreExecute");
                        mProgress.setVisibility(View.VISIBLE);
                        mButton.clearAnimation();
                        mButton.setVisibility(View.GONE);
                        mLoadMsg.setText(R.string.main_loadres);
                    }

                    @Override
                    public void onPostExecute() {
                        checkUpdate(0);
                        if (getWindowManager().getDefaultDisplay().getRotation() * Surface.ROTATION_90 == 1)//检测到横屏状态
                        {
                            SimpleUtil.log("\n\n\n\n\n****************启动检测到横屏***********************");
                            SimpleUtil.log("已经初始化，直接进入");
                            SimpleUtil.screenstate = true;
                            SimpleUtil.LIUHAI = (Integer) SimpleUtil.getFromShare(MainActivity.this, "ini", "LH", int.class, -1);
                            startMainService();
                            return;
                        }

                        mLoadMsg.setText(getString(R.string.main_entergameshowviews));
                        mProgress.setVisibility(View.GONE);
                        mButton.setText(R.string.main_backrun);
                        mButton.setVisibility(View.VISIBLE);
                        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
                        alphaAnimation.setDuration(300);
                        alphaAnimation.setRepeatCount(Animation.INFINITE);
                        alphaAnimation.setRepeatMode(Animation.REVERSE);
                        mButton.startAnimation(alphaAnimation);

                    }
                    //检查版本更新放在这里
                }).execute();
            }
        });
    }
    private void feedback() {
        String idkey = (String) SimpleUtil.getFromShare(MainActivity.this, "ini", "idkey", String.class, "");
        SimpleUtil.putOneInfoToMap("idkey", idkey);
        new PgyerFeedbackManager.PgyerFeedbackBuilder()
                .setShakeInvoke(false)       //fasle 则不触发摇一摇，最后需要调用 invoke 方法
                // true 设置需要调用 register 方法使摇一摇生效
                .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
                .setColorDialogTitle("#ffffff")    //设置Dialog 标题栏的背景色，默认为颜色为#ffffff
                .setColorTitleBg("#00ff00")        //设置Dialog 标题的字体颜色，默认为颜色为#2E2D2D
                .setBarBackgroundColor("#aa0000ff")      // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
                .setBarButtonPressedColor("#FF0000")        //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
                .setColorPickerBackgroundColor("#FF0000")   //设置颜色选择器的背景色,默认颜色为 #272828
                .setMoreParam("baseinfo", SimpleUtil.getInfoMapToString())
                .builder()
                .invoke();                  //激活直接显示的方式
        changeView();
    }

    private void changeView() {
        SimpleUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                getView2(MainActivity.this);
            }
        }, 10);
    }

    private EditText editpanel = null;

    private void getView2(Activity activity) {
        Object v0 = activity.getSystemService(Context.WINDOW_SERVICE);
        try {
            Field v2 = Class.forName("android.view.WindowManagerImpl").getDeclaredField("mGlobal");
            v2.setAccessible(true);
            v0 = v2.get(v0);
            v2 = v0.getClass().getDeclaredField("mViews");
            v2.setAccessible(true);
            v0 = v2.get(v0);
            if (v0 == null) {
            }
            if (v0 instanceof ArrayList) {
                ArrayList<View> views = (ArrayList<View>) v0;
                SimpleUtil.log("viwsize:" + views.size());
                if (views.size() < 2) {
                    changeView();
                    return;
                }

                for (View view : views) {
                    if (getViews(view)) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean getViews(View view) {
        boolean find = false;
        ViewGroup viewGroup = (ViewGroup) view;

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view2 = viewGroup.getChildAt(i);
            if (view2 instanceof ViewGroup) {
                getViews(view2);
            } else {
                SimpleUtil.log("sub-view:" + view2.toString());
                if (view2 instanceof TextView) {
                    find = ((TextView) view2).getText().toString().contains("反馈");
                    if (find) {
                        SimpleUtil.log("找到对话框");
                    }
                }
                if (view2 instanceof CheckBox) {
                    CheckBox checkBox = (CheckBox) view2;
                    checkBox.setChecked(false);
                    checkBox.setVisibility(View.GONE);
                    SimpleUtil.log("禁止截图");
                }
                if (view2 instanceof EditText) {
                    final EditText editText = (EditText) view2;
                    String hint = editText.getHint().toString();
                    if (hint.contains("...")) {
                        SimpleUtil.log("找到编辑框");
                        editpanel = editText;
                        editText.setHint(R.string.main_feedbackhint);
                        editText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                ((Button) editText.getTag()).setEnabled(s.length() >= 10);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });
                    }
                }
                if (view2 instanceof Button) {
                    Button button = (Button) view2;
                    if (button.getText().toString().contains(getString(R.string.main_feedbacksend))) {
                        SimpleUtil.log("找到发送按钮");
                        editpanel.setTag(button);
                        if (editpanel.getText().toString().length() < 10) {
                            button.setEnabled(false);
                        }
                    }
                }

            }

        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.loading_feedback:
                LinkedHashMap<String, String> items = new LinkedHashMap<>();
                items.put("指令", "");

                SimpleUtil.addEditToTop(this, "输入工厂指令", items, null, new Runnable() {
                    @Override
                    public void run() {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                KeyboardEditWindowManager.getInstance().close();
                            }
                        }, 100);

                    }
                }, new SimpleUtil.INormalBack() {
                    @Override
                    public void back(int id, Object obj) {
                        if (id == 2) {
                            List<String> backTexts = (List<String>) obj;
                            if (backTexts.get(0).equals("#zktest#")) {
                                boolean factorymode = !(Boolean) SimpleUtil.getFromShare(MainActivity.this, "ini", "factorymode", boolean.class);
                                SimpleUtil.addMsgBottomToTop(MainActivity.this, "已" + (factorymode ? "开启" : "关闭") + "工厂测试", false);
                                SimpleUtil.saveToShare(MainActivity.this, "ini", "factorymode", factorymode);
                                if (factorymode) {
                                    SimpleUtil.saveToShare(MainActivity.this, "ini", "is_show_kb_float_view", false);
                                }
                            }
                            KeyboardEditWindowManager.getInstance().close();
                        }
                    }
                });

                break;
        }
        return true;
    }
}
