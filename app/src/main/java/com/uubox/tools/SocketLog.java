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
            Socket socket = new Socket("192.168.18.234", 10087);

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
            int curMinutes = Calendar.getInstance().getTime().getMinutes();
            while ((line = bufferedReader.readLine()) != null) {

                String[] sp = line.split(":", -1);
                if (sp.length >= 2) {
                    int fen = Integer.parseInt(sp[1]);
                    // SimpleUtil.log("curMinutes:" + curMinutes + ".fen:" + fen);
                    if (fen >= curMinutes && !write(line)) {
                        SimpleUtil.log("XXXXXXXXXXXXXXXXXXXXXXXXX socket log write error!");
                        break;
                    }
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
