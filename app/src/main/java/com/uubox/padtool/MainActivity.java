package com.uubox.padtool;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pgyersdk.feedback.PgyerFeedbackManager;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.SimpleUtil;
import com.uubox.tools.SocketLog;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * 1.处理未获得权限时APK崩溃
 */
public class MainActivity extends Activity implements View.OnClickListener {
    private ProgressBar mProgress;
    private TextView mLoadMsg;
    private Button mButton;
    private boolean mIsJugeFloat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleUtil.DEBUG = CommonUtils.getAppVersionName(this).contains("debug");
        new SocketLog().start();
        SimpleUtil.log("MainActivity-------------create------------" + hashCode());
        setContentView(R.layout.activity_main);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);

        SimpleUtil.zoomx = Math.min(point.x, point.y);
        SimpleUtil.zoomy = Math.max(point.x, point.y);

        SimpleUtil.putOneInfoToMap("devpix", SimpleUtil.zoomx + "*" + SimpleUtil.zoomy);
        SimpleUtil.putOneInfoToMap("liuhai", (Integer) SimpleUtil.getFromShare(this, "ini", "LH", int.class, -1) + "");
        int saveY = (Integer) SimpleUtil.getFromShare(getBaseContext(), "ini", "zoomy", int.class);
        SimpleUtil.log("readX:" + SimpleUtil.zoomy + ",saveX:" + saveY);
        SimpleUtil.zoomy = Math.max(saveY, SimpleUtil.zoomy);

        SimpleUtil.saveToShare(this, "ini", "zoomx", SimpleUtil.zoomx);
        SimpleUtil.saveToShare(this, "ini", "zoomy", SimpleUtil.zoomy);
        SimpleUtil.log("屏幕分辨率:" + SimpleUtil.zoomx + "," + SimpleUtil.zoomy);
        mProgress = findViewById(R.id.loading_pro);
        mLoadMsg = findViewById(R.id.loading_msg);
        mButton = findViewById(R.id.loading_bt);
        mButton.setOnClickListener(this);
        findViewById(R.id.loading_feedback).setOnClickListener(this);
        if (getWindowManager().getDefaultDisplay().getRotation() * Surface.ROTATION_90 == 1)//检测到横屏状态
        {
            SimpleUtil.log("启动检测到横屏");
            if ((Boolean) SimpleUtil.getFromShare(this, "ini", "init", boolean.class))//已经初始化则直接进入
            {
                SimpleUtil.log("已经初始化，直接进入");
                SimpleUtil.LIUHAI = (Integer) SimpleUtil.getFromShare(this, "ini", "LH", int.class, -1);
                Intent intent = new Intent(MainActivity.this, MainService.class);
                startService(intent);
                finish();
                //moveTaskToBack(true);
                return;
            }
            mButton.setText("点击我后台运行");
        }

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
        //setMaxAspect();
    }

    /* public void setMaxAspect() {
         ApplicationInfo applicationInfo = null;
         try {
             SimpleUtil.log("设置全面屏参数1");
             applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
             SimpleUtil.log("设置全面屏参数2");
         } catch (PackageManager.NameNotFoundException e) {
             e.printStackTrace();
         }
         if(applicationInfo == null){
             SimpleUtil.log("设置全面屏参数失败");
             throw new IllegalArgumentException(" get application info = null, has no meta data! ");
         }
         SimpleUtil.log("设置全面屏参数成功");
         applicationInfo.metaData.putString("android.max_aspect", "3.2");
     }*/
    @Override
    protected void onResume() {
        super.onResume();
        SimpleUtil.log("MainActivity-------------resume------------" + hashCode() + " pid:" + Process.myPid());

        int jLH = (Integer) SimpleUtil.getFromShare(this, "ini", "LH", int.class, -1);
        //jLH = -1;
        if (jLH == -1)//需要判断一下刘海屏
        {
            startActivityForResult(new Intent(this, InitActivity.class), 11111);
            return;
        }
        SimpleUtil.LIUHAI = jLH;
        SimpleUtil.log("resume Liuhai:" + SimpleUtil.LIUHAI);
        if (isFloatPermissionOK()) {
            SimpleUtil.log("isFloatPermissionOK");
            mLoadMsg.setText("进入游戏会自动显示游戏键位图!");
            runInit();
        } else {
            SimpleUtil.log("isFloatPermissionnotok");
        }
        SimpleUtil.log("MainActivity-------------resume over------------" + hashCode() + " pid:" + Process.myPid());

    }

    private void runInit() {
        if (!(Boolean) SimpleUtil.getFromShare(this, "ini", "init", boolean.class)) {
            SimpleUtil.log("runInit initask execute");
            new IniTask().execute();
        } else {
            mButton.setText("点击我后台运行");
            mButton.setVisibility(View.VISIBLE);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
            alphaAnimation.setDuration(300);
            alphaAnimation.setRepeatCount(Animation.INFINITE);
            alphaAnimation.setRepeatMode(Animation.REVERSE);
            mButton.startAnimation(alphaAnimation);
        }
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
                mLoadMsg.setText("为了让应用正常运行，请开启【悬浮窗权限】");
                mButton.setText("现在开启");
                return false;
            }

        } else {
            if (!getAppOps(this)) {
                SimpleUtil.toast(MainActivity.this, "【悬浮窗权限】未开启！");
                mLoadMsg.setText("为了让应用正常运行，请开启【悬浮窗权限】");
                mButton.setText("现在开启");
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
                if (mLoadMsg.getText().toString().contains("悬浮窗")) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, 789);
                    } else {
                        Toast.makeText(MainActivity.this, "请打开悬浮窗", Toast.LENGTH_LONG).show();
                    }
                    return;
                }

                Intent intent = new Intent(MainActivity.this, MainService.class);
                startService(intent);
                finish();
                //moveTaskToBack(true);
                break;
            case R.id.loading_feedback:
                feedback();
                break;
        }
    }

    class IniTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {

            SimpleUtil.log("IniTask onPreExecute");
            mProgress.setVisibility(View.VISIBLE);
            mButton.clearAnimation();
            mButton.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                int proc = 0;
                String[] keyConfigFiles = MainActivity.this.getAssets().list("keyconfigs");
                publishProgress(0, keyConfigFiles.length);
                SimpleUtil.sleep(500);
                String use = null;
                for (String ini : keyConfigFiles) {
                    if (ini.contains("刺激战场")) {
                        use = ini;
                        continue;
                    }
                    BtnParamTool.setComfirGame(ini.substring(0, ini.length() - 4));
                    BtnParamTool.loadBtnParamsFromPrefs(MainActivity.this, false);
                    proc++;
                    publishProgress(proc, keyConfigFiles.length);
                }
                BtnParamTool.setComfirGame(use.substring(0, use.length() - 4));
                BtnParamTool.loadBtnParamsFromPrefs(MainActivity.this, false);
                publishProgress(keyConfigFiles.length, keyConfigFiles.length);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgress.setMax(values[1]);
            mProgress.setProgress(values[0]);
            mLoadMsg.setText("正在初始化 " + values[0] + "/" + values[1]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            SimpleUtil.saveToShare(MainActivity.this, "ini", "init", true);
            SimpleUtil.log("第一次初始化需要手动进入");
           /* if(getWindowManager().getDefaultDisplay().getRotation() * Surface.ROTATION_90==1)//检测到横屏状态
            {
                SimpleUtil.log("已经初始化，直接进入");
                Intent intent = new Intent(MainActivity.this, MainService.class);
                startService(intent);
                finish();
                return;
            }*/
            mLoadMsg.setText("初始化完成！进入游戏会自动显示游戏键位图！");
            mProgress.setVisibility(View.GONE);
            mButton.setText("点击我后台运行");
            mButton.setVisibility(View.VISIBLE);
            AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0);
            alphaAnimation.setDuration(300);
            alphaAnimation.setRepeatCount(Animation.INFINITE);
            alphaAnimation.setRepeatMode(Animation.REVERSE);
            mButton.startAnimation(alphaAnimation);

        }
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

    @Override
    public void onBackPressed() {
        System.exit(0);
    }

    private void feedback() {
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
                        editText.setHint("请输入您的反馈，最少10个文字，谢谢!");
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
                    if (button.getText().toString().contains("发送")) {
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
}
