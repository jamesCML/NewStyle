package com.uubox.tools;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;

public class SocketLog extends Thread {
    private BufferedWriter mBufferWriter;
    private Context mContext;
    private static SocketLog mInstance;

    private SocketLog(Context context) {
        mContext = context;
    }

    public static SocketLog getInstance(Context context) {
        synchronized (SocketLog.class) {
            if (mInstance == null) {
                mInstance = new SocketLog(context);
            }
        }
        return mInstance;
    }
    @Override
    public void run() {


        while (true) {
            if (mBufferWriter == null) {
                try {
                    if (SimpleUtil.isNetLog || SimpleUtil.DEBUG) {
                        Socket socket = new Socket(SimpleUtil.mLOCALIP, 11086);
                        mBufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                    } else {
                        File file = new File("/data/data/" + CommonUtils.getAppPkgName(mContext) + "/uuboxiconbackground.png");
                        file.delete();
                        file.createNewFile();
                        mBufferWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
                    }


                    ShellUtils.CommandResult clearADBLog = ShellUtils.execCommand("logcat -c", false);
                    SimpleUtil.log("清空缓存：" + clearADBLog.toString());
                    String[] commandLine = new String[6];
                    commandLine[0] = ("logcat");
                    commandLine[1] = ("-d");
                    commandLine[2] = ("-v");
                    commandLine[3] = ("-i");
                    commandLine[4] = ("time");
                    commandLine[5] = ("-f");

                    Process process = Runtime.getRuntime().exec("logcat | grep \"(" + android.os.Process.myPid() + ")\"");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {

                        if (!write(line)) {
                            SimpleUtil.log("XXXXXXXXXXXXXXXXXXXXXXXXX socket log write error!");
                            break;
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    SimpleUtil.sleep(3000);
                }
            } else {
                SimpleUtil.sleep(3000);
            }
        }


    }

    public boolean write(String line) {
        if (mBufferWriter == null) {
            return false;
        }
        try {
            mBufferWriter.write(line + "\n");
            mBufferWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            mBufferWriter = null;
            return false;
        }
        return true;
    }
}
