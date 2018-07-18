package com.uubox.padtool;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

import com.uubox.tools.CommonUtils;
import com.uubox.tools.InjectUtil;
import com.uubox.tools.SimpleUtil;
import com.uubox.tools.SocketLog;

public class MainActivity extends Activity {
    private ProgressBar mProgress;
    private TextView mLoadMsg;
    private Button mButton;
    private FrameLayout mParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgress = findViewById(R.id.loading_pro);
        mLoadMsg = findViewById(R.id.loading_msg);
        mButton = findViewById(R.id.loading_bt);
        mParent = findViewById(R.id.main_parent);
        SimpleUtil.log("MainActivity-------------create------------" + hashCode());
        SimpleUtil.DEBUG = CommonUtils.getAppVersionName(this).contains("debug");

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        if (!(Boolean) SimpleUtil.getFromShare(MainActivity.this, "ini", "loading", boolean.class) && (Boolean) SimpleUtil.getFromShare(this, "ini", "init", boolean.class)) {
            mLoadMsg.setText("进入游戏会自动显示游戏键位图！");
            mProgress.setProgress(mProgress.getMax());
            mButton.setVisibility(View.VISIBLE);
        } else {
            mLoadMsg.setText("进入游戏会自动显示游戏键位图!");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SimpleUtil.log("MainActivity-------------resume------------" + hashCode() + " pid:" + Process.myPid());
        if (isFloatPermissionOK()) {
            mLoadMsg.setText("进入游戏会自动显示游戏键位图!");
            runInit();
        }
    }

    private void runInit() {

        if (!(Boolean) SimpleUtil.getFromShare(this, "ini", "init", boolean.class)) {
            new IniTask().execute();
        } else {
            mButton.setVisibility(View.VISIBLE);
            mButton.setText("好的");
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

    class IniTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {

        /*Intent intent = new Intent(MainActivity.this,MainService.class);
        startService(intent);
        MainActivity.this.finish();*/
            mProgress.setVisibility(View.VISIBLE);
            mButton.setVisibility(View.GONE);
            SimpleUtil.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    int a1 = findViewById(R.id.main_parent).getHeight();
                    int a2 = findViewById(R.id.main_parent).getWidth();
                    SimpleUtil.zoomx = Math.min(a1, a2);
                    SimpleUtil.zoomy = Math.max(a1, a2);
                    SimpleUtil.saveToShare(MainActivity.this, "ini", "zoomx", SimpleUtil.zoomx);
                    SimpleUtil.saveToShare(MainActivity.this, "ini", "zoomy", SimpleUtil.zoomy);
                    SimpleUtil.toast(MainActivity.this, SimpleUtil.zoomx + "," + SimpleUtil.zoomy);
                }
            }, 200);
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
                    SimpleUtil.log("asyhash:" + hashCode());
                    if (ini.contains("刺激战场")) {
                        use = ini;
                        continue;
                    }
                    InjectUtil.setComfirGame(ini);
                    InjectUtil.loadBtnParamsFromPrefs(MainActivity.this, false);
                    proc++;
                    publishProgress(proc, keyConfigFiles.length);
                }
                InjectUtil.setComfirGame(use);
                InjectUtil.loadBtnParamsFromPrefs(MainActivity.this, false);
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
            mLoadMsg.setText("初始化完成！进入游戏会自动显示游戏键位图！");
            mProgress.setVisibility(View.GONE);
            mButton.setText("好的");
            mButton.setVisibility(View.VISIBLE);

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
