package com.uubox.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Calendar;

public class SocketLog extends Thread {
    private BufferedWriter mBufferWriter;
    @Override
    public void run() {
        try {
            Socket socket = new Socket("192.168.18.198", 10086);
            ShellUtils.CommandResult clearADBLog = ShellUtils.execCommand("logcat -c", false);
            SimpleUtil.log("清空缓存：" + clearADBLog.toString());
            mBufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            String[] commandLine = new String[5];
            commandLine[0] = ("logcat");
            commandLine[1] = ("-d");
            commandLine[2] = ("-v");
            commandLine[3] = ("time");
            commandLine[4] = ("-f");

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
            return false;
        }
        return true;
    }
}
