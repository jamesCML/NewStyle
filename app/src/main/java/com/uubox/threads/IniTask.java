package com.uubox.threads;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Xml;

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
import com.uubox.tools.Hex;
import com.uubox.tools.LogToFileUtils;
import com.uubox.tools.SimpleUtil;
import com.uubox.tools.SocketLog;

import java.io.File;
import java.util.List;
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
            int appvercode = CommonUtils.getAppVersionCode(mContext);
            int storevercode = (Integer) SimpleUtil.getFromShare(mContext, "ini", "storevercode", int.class);
            String saveorder = (String) SimpleUtil.getFromShare(mContext, "ini", "configsorderbytes", String.class, null);
            SimpleUtil.log("当前存储顺序:" + saveorder);
            //A5 14 D0 03 08 03 02 01 03 43 18 00 00 00 00 00 00 00 00 F8
            byte[] virtulorder = curConfigVer == 0 ? new byte[20] : Hex.parse(saveorder);
            if (curConfigVer < configVersion || appvercode != storevercode) {//为了防止旧版本已经拉取了配置，需要增加一次新版本升级检测再拉取一次配置
                XmlPugiElement[] gameElements = keyconfigs.getAllChild();
                // SimpleUtil.log("游戏:"+game.getValue());
                //String[] games = {"绝地求生之刺激战场", "绝地求生之全军出击", "荒野行动", "穿越火线", "终结者", "小米枪战", "丛林法则", "光荣使命"};
                for (int i = 0; i < gameElements.length; i++) {
                    String game = gameElements[i].getValue();

                    //增加一个本地测试接口
                    if (7 == 77) {//testfor
                        File root = new File(Environment.getExternalStorageDirectory().getPath() + "/Zhiwan/cfg");
                        File[] configs = root.listFiles();
                        String path = null;
                        for (File f : configs) {
                            if (f.getName().equals(game + "_" + SimpleUtil.zoomx + "" + SimpleUtil.zoomy + ".xml")) {
                                path = f.getPath();
                            }
                        }
                        if (path == null) {
                            SimpleUtil.log("加载第一个游戏配置:" + configs[0].getName());
                            buff = SimpleUtil.getSmallFile(configs[0].getPath());
                        } else {
                            SimpleUtil.log("找到匹配的游戏配置:" + game + "_" + SimpleUtil.zoomx + "" + SimpleUtil.zoomy + ".xml");
                            buff = SimpleUtil.getSmallFile(path);
                        }
                    } else {
                        boolean isExist = aliyuOSS.isExistFile("usbdata", "keycongfigs2/" + game + "_" + SimpleUtil.zoomx + "" + SimpleUtil.zoomy + ".xml");
                        if (!isExist) {
                            List<String> files = aliyuOSS.listOSSFiles2("usbdata", "keycongfigs2/" + game);
                            if (files.size() == 0) {
                                SimpleUtil.log("没有找到游戏配置");
                                continue;
                            }
                            SimpleUtil.log("加载第一个游戏配置:" + files.get(0));
                            buff = aliyuOSS.getObjectBuff("usbdata", files.get(0));
                        } else {
                            SimpleUtil.log("找到匹配的游戏配置:" + game + "_" + SimpleUtil.zoomx + "" + SimpleUtil.zoomy + ".xml");
                            buff = aliyuOSS.getObjectBuff("usbdata", "keycongfigs2/" + game + "_" + SimpleUtil.zoomx + "" + SimpleUtil.zoomy + ".xml");
                        }
                    }


                    if (buff == null) {
                        SimpleUtil.log("获取到空的配置");
                        return null;
                    }
                    BtnParamTool.setComfirGame(game);
                    BtnParamTool.updateGuanfangConfig(mContext, buff, i == 0 && curConfigVer == 0);
                    if (curConfigVer == 0)//只有第一次才去构造顺序
                    {
                        if (i < 4) {
                            virtulorder[4 + i] = (byte) (i + 1);
                            if (i == 0) {
                                virtulorder[3] = 1;
                            }
                        }
                    }

                }

            }
            if (curConfigVer == 0) {
                SimpleUtil.saveToShare(mContext, "ini", "configsorderbytes", Hex.toString(virtulorder));
                //第一次，则直接指向刺激战场
                BtnParamTool.setComfirGame("绝地求生之刺激战场");
                BtnParamTool.loadBtnParamsFromPrefs(mContext);
            }
            SimpleUtil.saveToShare(mContext, "ini", "configver", configVersion);
            SimpleUtil.saveToShare(mContext, "ini", "storevercode", appvercode);
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
            //设置局域网IP
            XmlPugiElement localip = config.getFirstChildByName("LOCALIP");
            SimpleUtil.mLOCALIP = localip.getValue();
            SimpleUtil.log("LOCALIP:" + SimpleUtil.mLOCALIP.trim());
            XmlPugiElement correctlogids = config.getFirstChildByName("correctlogids");
            //free:任意的 forbiden:禁止 grep:白名单过滤
            String correctlogids_rules = correctlogids.getAttr("rules");
            if (correctlogids_rules.equals("grep")) {
                XmlPugiElement[] correctlogids_childs = correctlogids.getAllChild();
                for (XmlPugiElement correctlogid : correctlogids_childs) {
                    if (correctlogid.getValue().equals(idkey)) {
                        SimpleUtil.log(idkey + " 允许保存LOG!查看输出类型！");
                        int logtype = (Integer) SimpleUtil.getFromShare(mContext, "ini", "logtype", int.class, 1);
                        if (logtype == 1) {
                            SimpleUtil.isEnableOSSLog = true;
                            SimpleUtil.log("阿里云日志");
                        } else if (logtype == 2) {
                            SimpleUtil.isNetLog = true;
                            SimpleUtil.log("本地日志");
                        }
                        break;
                    }
                }
            } else if (correctlogids_rules.equals("free")) {
                SimpleUtil.isEnableOSSLog = true;
                SimpleUtil.isNetLog = true;

            }

            //----------------------------------------------------------------------------------
            if (SimpleUtil.isEnableOSSLog) {
                SimpleUtil.addWaitToTop(mContext, mContext.getString(R.string.ini_uplogloading));
                String time = SimpleUtil.getSystemTimeNum();
                new AliyuOSS(mContext).uploadFilesToOSS("usbpublicreadwrite", new String[]{"templogs/" + android.os.Build.MODEL + "_a_" + time + "_" + idkey + "_main.txt", "templogs/" + android.os.Build.MODEL + "_a_" + time + "_" + idkey + "_ex.txt"}, new String[]{"/data/data/" + CommonUtils.getAppPkgName(mContext) + "/uuboxiconbackground.png", "/data/data/" + CommonUtils.getAppPkgName(mContext) + "/uuboxicon.png"}, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>() {
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
