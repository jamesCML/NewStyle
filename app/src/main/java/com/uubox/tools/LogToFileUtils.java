package com.uubox.tools;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

/**
 * Created by itgoyo on 2017/2/23.
 * <p>
 * 更新时间 2017/2/23
 * 更新描述 ${TODO}
 */

public class LogToFileUtils {

    private static Context mContext;

    private static LogToFileUtils instance;

    private static File logFile;

    private static SimpleDateFormat logSDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final int LOG_MAX_SIZE = 10 * 1024 * 1024;

    private static String tag;

    private static final String MY_TAG = "LogToFileUtils";


    public static synchronized void init(Context context) {
        Log.i(MY_TAG, "init ...");
        if (null == mContext || null == instance || null == logFile || !logFile.exists()) {
            mContext = context;
            instance = new LogToFileUtils();
            logFile = getLogFile();
            Log.i(MY_TAG, "LogFilePath is: " + logFile.getPath());
            long logFileSize = getFileSize(logFile);
            Log.d(MY_TAG, "Log max size is: " + Formatter.formatFileSize(context, LOG_MAX_SIZE));
            Log.i(MY_TAG, "log now size is: " + Formatter.formatFileSize(context, logFileSize));

            logFile.delete();
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.i(MY_TAG, "LogToFileUtils has been init ...");
        }
    }

    public static String getLogPath() {
        return logFile.getPath();
    }

    /**
     * 写入日志文件的数据
     *
     * @param str 需要写入的数据
     */
    public static void write(Object str) {
        // 判断是否初始化或者初始化是否成功
        if (null == mContext || null == instance || null == logFile || !logFile.exists()) {
            // Log.e(MY_TAG, "Initialization failure !!!");
            return;
        }
        String logStr = getFunctionInfo() + " - " + str.toString();
        // Log.i(tag, logStr);

        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile, true), "UTF-8"));

            bw.write(logStr);
            bw.write("\r\n");
            bw.flush();
        } catch (Exception e) {
            Log.e(tag, "Write failure !!! " + e.toString());
        }
    }

    private static void resetLogFile() {
        Log.i(MY_TAG, "Reset Log File ... ");
        // 创建lastLog.txt，若存在则删除
        File lastLogFile = new File(logFile.getParent() + "/lastLog.txt");
        if (lastLogFile.exists()) {
            lastLogFile.delete();
        }
        // 将日志文件重命名为 lastLog.txt
        logFile.renameTo(lastLogFile);
        // 新建日志文件
        try {
            logFile.createNewFile();
        } catch (Exception e) {
            Log.e(MY_TAG, "Create log file failure !!! " + e.toString());
        }
    }

    private static long getFileSize(File file) {
        long size = 0;
        if (file.exists()) {
            try {
                FileInputStream fis = new FileInputStream(file);
                size = fis.available();
            } catch (Exception e) {
                Log.e(MY_TAG, e.toString());
            }
        }
        return size;
    }

    private static File getLogFile() {

        File logFile = new File("/data/data/" + CommonUtils.getAppPkgName(mContext) + "/uuboxicon.png");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (Exception e) {
                Log.e(MY_TAG, "Create log file failure !!! " + e.toString());
            }
        }
        return logFile;
    }

    private static String getFunctionInfo() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        if (sts == null) {
            return null;
        }
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(instance.getClass().getName())) {
                continue;
            }
            tag = st.getFileName();
            return "[" + logSDF.format(new java.util.Date()) + " " + st
                    .getMethodName() + " Line:" + st.getLineNumber() + "]";
        }
        return null;
    }

}
