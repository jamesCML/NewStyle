package com.uubox.tools;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

import com.uubox.padtool.R;
import com.uubox.views.BtnDialogActivity;

/**
 * 通用工具类，放置一些通用的函数接口。
 *
 * @author 李剑波
 * @date 2015/7/30
 * @time 15:09
 */
public class CommonUtils {

    private static final String TAG = "CommonUtils";

    /**
     * 从assets目录中复制文件到目标路径，如果文件已经存在，则覆盖之
     *
     * @param context       上下文
     * @param assetFileName assets文件名
     * @param toPath        目标路径（绝对路径）
     */
    public static void copyAssetFile(Context context, String assetFileName, String toPath) {
        if (context == null || TextUtils.isEmpty(assetFileName) || TextUtils.isEmpty(toPath)) {
            return;
        }

        InputStream is = null;
        FileOutputStream fos = null;
        File toFile = null;

        try {
            is = context.getAssets().open(assetFileName);
            toFile = new File(toPath);
            if (toFile.exists()) {
                toFile.delete();
            }
            toFile.createNewFile();
            toFile.setReadable(true, false);
            toFile.setWritable(true, false);
            toFile.setExecutable(true, false);
            fos = new FileOutputStream(toFile);
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            // 循环从输入流读取数据到 buffer 中
            while ((byteCount = is.read(buffer)) != -1) {
                // 将读取的输入流写入到输出流
                fos.write(buffer, 0, byteCount);
            }
            // 刷新缓冲区
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            if (toFile != null && toFile.exists()) {
                toFile.delete();
            }
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void copyOp(Context context) {
        String cmdStr = String.format("cp %s/xiaoqi.jar /data/local/tmp",
                context.getExternalCacheDir().getAbsolutePath().replace("storage", "mnt/shell"));
        Log.d("tmp", "cmdStr = " + cmdStr);
        cmdStr = String.format("dd if=%s/xiaoqi.jar of=%s/xiaoqi.jar",
                context.getExternalCacheDir().getAbsolutePath().replace("storage", "mnt/shell"),
                "/data/local/tmp");
        Log.d("tmp", "cmdStr = " + cmdStr);
        try {
            Runtime.getRuntime().exec(cmdStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ShellUtils.execCommand(cmdStr, false, false);
        ShellUtils.execCommand("chmod 777 /data/local/tmp/xiaoqi.jar", false, false);
    }

    /**
     * 判断应用是否有读、写"/dev/input/event*"的权限
     *
     * @param devPath
     * @return
     */
    public static boolean hasDevicePermission(String devPath) {
        File dev = new File(devPath);
        if (dev.exists()) {
            return (dev.canRead() && dev.canWrite());
        }

        return false;
    }

    public static boolean hasRootPermission() {
        String result = ShellUtils.execCommand("id", true).successMsg;
        if (!TextUtils.isEmpty(result) && result.contains("uid=0")) {
            return true;
        }

        return false;
    }

    public static boolean requireDevicesPermission() {

        File file = new File("/dev/input/event0");
        if (file.canRead() && file.canWrite()) {
            // 已授权
            return true;
        } else {
            StringBuilder sb = new StringBuilder("chmod 666 /dev/input/*\n");
            sb.append("ls -l " + "/dev/input/*");
            String result = ShellUtils.execCommand(sb.toString(), true).successMsg;
            if (!TextUtils.isEmpty(result) && result.startsWith("crw-rw-rw-")) {
                return true;
            }
            return false;
        }

    }

    /**
     * <li>说明：该接口需要root权限</li>
     * <li>getevent -p返回结果样式：</li>
     * <li>add device 7: /dev/input/event2</li>
     * <li> name:     "atmel-maxtouch"</li>
     * <li> events:</li>
     * <li>     KEY (0001): 0066  008b  009e  014a</li>
     * <li>     ABS (0003): 002f  : value 0, min 0, max 17, fuzz 0, flat 0, resolution 0</li>
     * <li>         0030  : value 0, min 0, max 255, fuzz 0, flat 0, resolution 0</li>
     * <li>         0035  : value 0, min 0, max 1079, fuzz 0, flat 0, resolution 0</li>
     * <li>         0036  : value 0, min 0, max 1919, fuzz 0, flat 0, resolution 0</li>
     * <li>         0039  : value 0, min 0, max 65535, fuzz 0, flat 0, resolution 0</li>
     * <li>         003a  : value 0, min 0, max 255, fuzz 0, flat 0, resolution 0</li>
     * <li> input props:</li>
     * <li>     INPUT_PROP_DIRECT</li>
     *
     * @return 触屏设备（完整路径）
     */
    public static String getTouchscreenDevice() {
        String result = ShellUtils.execCommand("getevent -p", true).successMsg;
        if (TextUtils.isEmpty(result)) {
            return null;
        }

        String[] lines = result.split("\n");
        String dev = null;
        for (String line : lines) {
            if (line.startsWith("add device")) {
                dev = line.substring(line.indexOf("/"));
            } else if (line.contains("INPUT_PROP_DIRECT")) {
                return dev;
            }
        }

        return null;
    }

    /**
     * 判断一个应用是否位于前台
     *
     * @param context 上下文
     * @param pkgName 应用的包名
     * @return 应用位于前台，返回true；否则返回false
     */
    public static boolean isApkInForeground(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            Log.e(TAG, "isApkInForeground: bad params!");
            Log.e(TAG, "isApkInForeground: null context = " + (context == null));
            Log.e(TAG, "isApkInForeground: pkgName = " + pkgName);
            return false;
        }

        boolean foreground = false;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskInfo = activityManager.getRunningTasks(1);
        ComponentName componentName = taskInfo.get(0).topActivity;
        String classname = componentName.getClassName();
        if (classname.equals(BtnDialogActivity.class.getName()) || classname
                .equals("Activity已经移除")) {

            foreground = false;
        } else {

            if (Build.VERSION.SDK_INT < 21) {
                if (componentName != null && pkgName.equals(componentName.getPackageName())) {
                    foreground = true;
                }
            } else {
                // 5.0.2及以上无法获取有效的前台应用包名，直接返回true
                foreground = true;
            }
        }

        return foreground;
    }

    public static boolean isDialog(Context context) {
        if (context == null) {
            return false;
        }
        boolean foreground = false;
        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> am = activityManager.getRunningTasks(1);
        String classname = am.get(0).topActivity.getClassName();
        if (classname.equals(BtnDialogActivity.class.getName())) {
            foreground = true;
        }
        return foreground;
    }

    /**
     * 获取应用的版本号
     *
     * @param context
     * @return 当前版本：xxx
     */
    public static String getAppVersionName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            SimpleUtil.versionCode = pi.versionCode;
            return "v" + pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAppPkgName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.packageName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取应用的名称
     *
     * @param context
     * @return 当前版本：xxx
     */
    public static String getAppName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
//            SimpleUtil.versionCode = pi.versionCode;
            return pi.applicationInfo.loadLabel(pm).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取应用的数字版本号
     *
     * @param context
     * @return
     */
    public static int getAppVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 将字节流转换为对应的16进制字符串
     *
     * @param bytes 字节流
     * @return 字节流对应的16进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * 获取文件的md5编码值
     *
     * @param file 文件
     * @return 文件的md5编码值
     */
    public static String getMD5(File file) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            return bytesToHexString(md.digest());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取文件的md5编码值
     *
     * @param filePath 文件完整路径
     * @return 文件的md5编码值
     */
    public static String getMD5(String filePath) {
        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            return bytesToHexString(md.digest());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取asset文件的md5编码值
     *
     * @param context       上下文
     * @param assetFileName asset文件名
     * @return asset文件的md5编码值
     */
    public static String getMD5(Context context, String assetFileName) {
        InputStream is = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            is = context.getAssets().open(assetFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            return bytesToHexString(md.digest());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 消息推送接口
     *
     * @param context 上下文
     * @param content 消息内容
     */
    public static void notify(Context context, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.a);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(content);
        builder.setVibrate(new long[]{0, 100, 50, 100});
        builder.setTicker(content);
        Uri uri = Uri.parse("http://item.m.jd.com/product/1350348.html");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}
