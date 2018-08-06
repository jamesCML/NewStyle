package com.uubox.tools;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SocketLogEx {
    private BufferedWriter mBufferWriter;
    private static SocketLogEx mInstance;
    private static Object object = new Object();

    private SocketLogEx() {
        try {
            Socket socket = new Socket("192.168.18.198", 10086);
            mBufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SocketLogEx getInstance() {
        synchronized (object) {
            if (mInstance == null) {
                mInstance = new SocketLogEx();
            }
            return mInstance;
        }
    }

    public void sendLog(String msg) {
        if (!write(msg)) {
            Log.e("CJLOG", "XXXXXXXXXXXXXXXXXXXXXXXXX socket log write error!");
        }
    }

    private boolean write(String line) {
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
