package com.uubox.padtool;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.SimpleUtil;
import com.uubox.tools.SocketLog;

public class MainActivity extends Activity implements View.OnClickListener {
    private ProgressBar mProgress;
    private TextView mLoadMsg;
    private Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SimpleUtil.DEBUG = CommonUtils.getAppVersionName(this).contains("debug");
        //SimpleUtil.log("MainActivity-------------create------------" + hashCode());
        setContentView(R.layout.activity_main);
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getRealSize(point);

        SimpleUtil.zoomx = Math.min(point.x, point.y);
        SimpleUtil.zoomy = Math.max(point.x, point.y);
        SimpleUtil.saveToShare(this, "ini", "zoomx", SimpleUtil.zoomx);
        SimpleUtil.saveToShare(this, "ini", "zoomy", SimpleUtil.zoomy);
        SimpleUtil.log("屏幕分辨率:" + SimpleUtil.zoomx + "," + SimpleUtil.zoomy);
        mProgress = findViewById(R.id.loading_pro);
        mLoadMsg = findViewById(R.id.loading_msg);
        mButton = findViewById(R.id.loading_bt);
        mButton.setOnClickListener(this);

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

    }

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

        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(this)) {
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
            arrayOfObject1[0] = Integer.valueOf(24);
            arrayOfObject1[1] = Integer.valueOf(Binder.getCallingUid());
            arrayOfObject1[2] = context.getPackageName();
            int m = ((Integer) method.invoke(object, arrayOfObject1)).intValue();
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
}
