package com.uubox.tools;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

public class SocketLogEx extends Thread {
    private static BufferedWriter mBufferWriter;
    private static SocketLogEx mInstance;
    private volatile Vector<String> mBuffer = new Vector<>();
    private SocketLogEx() {
        start();
    }

    public static SocketLogEx getInstance() {
        synchronized (SocketLogEx.class) {
            if (mInstance == null) {
                mInstance = new SocketLogEx();
            }
            return mInstance;
        }
    }

    @Override
    public void run() {
        while (true) {

            if (mBufferWriter == null) {
                try {
                    Socket socket = new Socket("192.168.18.198", 10087);
                    mBufferWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (mBuffer.size() != 0) {
                if (!write(mBuffer.get(0))) {
                    mBufferWriter = null;
                    // Log.e("CJLOG", "XXXXXXXXXXXXXXXXXXXXXXXXX socket log write error!");
                }
                mBuffer.remove(0);
            }
        }
    }

    public void sendLog(String msg) {
        if (mBuffer == null) {
            return;
        }
        mBuffer.add(msg);
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
