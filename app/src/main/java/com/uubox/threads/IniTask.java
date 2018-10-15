package com.uubox.threads;

import android.content.Context;
import android.os.AsyncTask;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.example.cgodawson.xml.XmlPugiElement;
import com.uubox.padtool.R;
import com.uubox.tools.AliyuOSS;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.CommonUtils;
import com.uubox.tools.LogToFileUtils;
import com.uubox.tools.SimpleUtil;
import com.uubox.tools.SocketLog;

import java.util.Random;

public class IniTask extends AsyncTask<Void, Integer, Void> {
    private Context mContext;
    private IIniexecallback mIIniexecallback;

    public IniTask(Context context, IIniexecallback iIniexecallback) {
        mContext = context;
        mIIniexecallback = iIniexecallback;

    }

    @Override
    protected void onPreExecute() {
        mIIniexecallback.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            //先获取唯一key
            String idkey = (String) SimpleUtil.getFromShare(mContext, "ini", "idkey", String.class, "");
            SimpleUtil.log("idkey:" + idkey);
            SimpleUtil.putOneInfoToMap("idkey", idkey);
            if (idkey.isEmpty()) {
                String getidkey = new Random().nextDouble() + "" + new Random().nextDouble();
                SimpleUtil.log("getidkey:" + getidkey + "\n" + SimpleUtil.getSha1(getidkey));
                SimpleUtil.saveToShare(mContext, "ini", "idkey", SimpleUtil.getSha1(getidkey));
            }
            idkey = (String) SimpleUtil.getFromShare(mContext, "ini", "idkey", String.class, "");
            AliyuOSS aliyuOSS = new AliyuOSS(mContext);
            byte[] buff = aliyuOSS.getObjectBuff("usbdata", "keycongfigs2/usbconfig.xml");
            if (buff == null) {
                SimpleUtil.log("加载配置信息错误！");
                return null;
            }
            XmlPugiElement config = new XmlPugiElement(buff);
            if (!config.loadSucess) {
                SimpleUtil.log("解析配置信息错误！");
                return null;
            }
            XmlPugiElement keyconfigs = config.getFirstChildByName("keyconfigs");

            int configVersion = Integer.parseInt(keyconfigs.getAttr("version"));

            int curConfigVer = (Integer) SimpleUtil.getFromShare(mContext, "ini", "configver", int.class);
            SimpleUtil.log("服务器配置版本:" + configVersion + ",当前配置版本:" + curConfigVer);
            if (curConfigVer < configVersion) {
                String[] games = {"荒野行动", "绝地求生之刺激战场", "绝地求生之全军出击", "终结者", "穿越火线", "小米枪战", "丛林法则", "光荣使命"};
                for (String game : games) {
                    boolean isExist = aliyuOSS.isExistFile("usbdata", "keycongfigs2/" + game + "_" + SimpleUtil.zoomx + "" + SimpleUtil.zoomy + ".xml");
                    SimpleUtil.log("正在获取配置:" + game + "  ->" + isExist);
                    buff = aliyuOSS.getObjectBuff("usbdata", isExist ? "keycongfigs2/" + game + "_" + SimpleUtil.zoomx + "" + SimpleUtil.zoomy + ".xml" : "keycongfigs2/" + game + "_10802160.xml");
                    if (buff == null) {
                        return null;
                    }
                    BtnParamTool.setComfirGame(game);
                    BtnParamTool.updateGuanfangConfig(mContext, buff);

                }

                SimpleUtil.saveToShare(mContext, "ini", "configver", configVersion);
            }

            //获取可以配置xml的白名单
            XmlPugiElement savexmlids = config.getFirstChildByName("savexmlids");
            //free:任意的 forbiden:禁止 grep:白名单过滤
            String rules = savexmlids.getAttr("rules");

            if (rules.equals("grep")) {
                XmlPugiElement[] savexmlids_childs = savexmlids.getAllChild();
                for (XmlPugiElement savexmlid : savexmlids_childs) {
                    if (savexmlid.getValue().equals(idkey)) {

                        SimpleUtil.isSaveToXml = true;
                        SimpleUtil.log(idkey + " 允许保存配置文件!");
                        break;
                    }
                }
            } else if (rules.equals("free")) {
                SimpleUtil.isSaveToXml = true;
            }
            //----------------------------------------------------------------------------------
            //获取可以收集log的白名单
            XmlPugiElement correctlogids = config.getFirstChildByName("correctlogids");
            //free:任意的 forbiden:禁止 grep:白名单过滤
            String correctlogids_rules = correctlogids.getAttr("rules");
            if (correctlogids_rules.equals("grep")) {
                XmlPugiElement[] correctlogids_childs = correctlogids.getAllChild();
                for (XmlPugiElement correctlogid : correctlogids_childs) {
                    if (correctlogid.getValue().equals(idkey)) {

                        SimpleUtil.isEnableOSSLog = true;
                        SimpleUtil.isNetLog = false;
                        SimpleUtil.log(idkey + " 允许保存LOG!");
                        break;
                    }
                }
            } else if (correctlogids_rules.equals("free")) {
                SimpleUtil.isEnableOSSLog = true;
                SimpleUtil.isNetLog = false;

            }
            //----------------------------------------------------------------------------------
            if (SimpleUtil.isEnableOSSLog) {
                SimpleUtil.addWaitToTop(mContext, mContext.getString(R.string.ini_uplogloading));
                new AliyuOSS(mContext).uploadFilesToOSS("usbpublicreadwrite", new String[]{"templogs/" + android.os.Build.MODEL + "_" + idkey + "_main.txt", "templogs/" + android.os.Build.MODEL + "_" + idkey + "_ex.txt"}, new String[]{"/data/data/" + CommonUtils.getAppPkgName(mContext) + "/uuboxiconbackground.png", "/data/data/" + CommonUtils.getAppPkgName(mContext) + "/uuboxicon.png"}, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
                    @Override
                    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
                        if (request.getUploadFilePath().endsWith("uuboxicon.png")) {
                            SimpleUtil.resetWaitTop(mContext);
                            SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.ini_uologok), false);
                            openOSSLOG();
                        }
                    }

                    @Override
                    public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
                        if (request.getUploadFilePath().endsWith("uuboxicon.png")) {
                            SimpleUtil.resetWaitTop(mContext);
                            SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.ini_uplogfail), true);
                            openOSSLOG();
                        }
                    }
                });
            } else {
                openOSSLOG();
            }


            config.release();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void openOSSLOG() {
        if (SimpleUtil.isNetLog || SimpleUtil.isEnableOSSLog)
            SocketLog.getInstance(mContext).start();
        if (SimpleUtil.isEnableOSSLog) {
            LogToFileUtils.init(mContext);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
           /* mProgress.setMax(values[1]);
            mProgress.setProgress(values[0]);
            mLoadMsg.setText("正在加载资源 " + values[0] + "/" + values[1]);*/
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mIIniexecallback.onPostExecute();


    }

    public interface IIniexecallback {
        void onPreExecute();

        void onPostExecute();
    }
}
