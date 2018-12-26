package com.uubox.cjble.ota;

import com.example.cgodawson.xml.XmlPugiElement;
import com.uubox.cjble.BTJobsManager;
import com.uubox.tools.ByteArrayList;
import com.uubox.tools.Hex;
import com.uubox.tools.SimpleUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WisegaHttpRemoteOTA extends BaseRemoteOTA {

    private String mImgURL;
    private LinkedHashMap<String, String> mAttrs;

    public WisegaHttpRemoteOTA(String mFWVersion, String curImg) {
        super(mFWVersion, curImg);
    }

    @Override
    public void task() {
        final OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://wisega-public.oss-cn-beijing.aliyuncs.com/wisega-ota/ini.xml").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                SimpleUtil.log("请求失败！" + e);
                SimpleUtil.notifyall_(DOWNLOADEER_CALLBACK, null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String btName = BTJobsManager.getInstance().getDevice().getName();
                InputStream in = response.body().byteStream();
                ByteArrayList byteArrayList = new ByteArrayList();
                byte[] buff = new byte[1024];
                int len = 0;
                while ((len = in.read(buff)) > 0) {
                    byteArrayList.add(Arrays.copyOfRange(buff, 0, len));
                }
                String s = new String(byteArrayList.all2Bytes());
                XmlPugiElement root = new XmlPugiElement(byteArrayList.all2Bytes());
                List<XmlPugiElement> names = getChild(root);
                boolean isFind = false;
                String btname = BTJobsManager.getInstance().getDevice().getName();
                for (XmlPugiElement name : names)//蓝牙名称集合
                {
                    if (isFind) break;
                    String serbtname = name.getName().replace("space", " ");
                    SimpleUtil.log("checkname:" + btname + "," + serbtname);
                    if (btname.contains(serbtname)) {
                        List<XmlPugiElement> models = getChild(name);
                        for (XmlPugiElement mode : models)//mode集合
                        {
                            String btmod = BTJobsManager.getInstance().getDevMod();
                            String sermod = mode.getName();
                            SimpleUtil.log("checkmode:" + btmod + "," + sermod);
                            if (sermod.equals(btmod)) {
                                mServerVer = mode.getAttr("version");
                                if (Integer.valueOf(mServerVer) >= Integer.valueOf(mFWVersion)) {
                                    List<XmlPugiElement> abTypes = getChild(mode);
                                    for (XmlPugiElement abType : abTypes) {//AB类型集合

                                        String ab = abType.getName();
                                        if (!ab.equals(mCurImg)) {
                                            mAttrs = abType.getAllAttrs();
                                            mImgURL = abType.getValue();
                                            SimpleUtil.log("filename: " + mAttrs.get("name"));
                                            break;
                                        }
                                    }
                                    okHttpClient.dispatcher().executorService().shutdown();   //清除并关闭线程池
                                    okHttpClient.connectionPool().evictAll();                 //清除并关闭连接池
                                    isFind = true;
                                    break;
                                }
                            }
                        }

                    }
                }
                SimpleUtil.notifyall_(FWVERSION_CALLBACK, mServerVer);
            }
        });

    }

    private List<XmlPugiElement> getChild(XmlPugiElement element) {
        List<XmlPugiElement> results = new ArrayList<>();
        String parentName = element.getName();
        XmlPugiElement[] subs = element.getAllChild();

        for (XmlPugiElement e : subs) {
            if (e.getParent().getName().equals(parentName)) {
                results.add(e);
            }
        }
        return results;
    }

    private String getOTAURL(String btName, XmlPugiElement element) {
        if (btName.contains(element.getName())) {

        }
        return null;
    }

    @Override
    public void downloadError(String msg) {

    }

    @Override
    public boolean writeRemote(byte[] data) {
        return false;
    }

    @Override
    public void close() {

    }

    @Override

    public void wantImg() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final OkHttpClient okHttpImgClient = new OkHttpClient();
                Request request = new Request.Builder().url(mImgURL).build();
                SimpleUtil.log("下载地址:" + mImgURL);
                okHttpImgClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        SimpleUtil.log("请求失败！" + e);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        InputStream inputStream = response.body().byteStream();
                        byte[] buff = new byte[4096];
                        int len = 0;
                        int recCount = 0, totalLen = Integer.parseInt(mAttrs.get("length"));

                        while ((len = inputStream.read(buff)) > 0) {
                            mBuff.add(Arrays.copyOfRange(buff, 0, len));
                            recCount += len;
                        }
                        try {
                            MessageDigest digest = MessageDigest.getInstance("MD5");
                            digest.update(mBuff.all2Bytes());
                            byte[] hashedByte = digest.digest();
                            String crcMd5 = Hex.toString(hashedByte);
                            if (recCount != totalLen || !crcMd5.replace(" ", "").equalsIgnoreCase(mAttrs.get("md5").replace(" ", ""))) {
                                String errorMsg = "Download Error!\ncrc1:" + crcMd5 + "\ncrc2:" + mAttrs.get("md5").replace(" ", "") + "\nlen1:" + recCount + "\nlen2:" + totalLen;
                                errorMsg = errorMsg.toLowerCase().replace(" ", "");
                                SimpleUtil.log(errorMsg);
                                SimpleUtil.notifyall_(CRCEER_CALLBACK, errorMsg);
                                mBuff.clear();
                            } else {
                                SimpleUtil.notifyall_(OKBUFF_CALLBACK, mBuff);
                            }
                            inputStream.close();

                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                        okHttpImgClient.dispatcher().executorService().shutdown();   //清除并关闭线程池
                        okHttpImgClient.connectionPool().evictAll();                 //清除并关闭连接池

                    }
                });

            }
        }).start();

    }
}
