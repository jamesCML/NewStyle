package com.uubox.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;

import com.uubox.padtool.R;
import com.uubox.tools.AOAConfigTool;
import com.uubox.tools.BtnParamTool;
import com.uubox.tools.SimpleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BtnParamsHolder {
    private Context mContext;

    public BtnParamsHolder(Context context) {
        mContext = context;
    }

    public void preareLoadConfigs(final List<AOAConfigTool.Config> configs, @NonNull final IMeasureResult iMeasureResult) {
        final List<KeyboardView> keyboardViews = new ArrayList<>();
        SimpleUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {

                AOAConfigTool.Config configUsed = null;
                for (AOAConfigTool.Config config : configs) {
                    if (config.getIsUsed()) {
                        SimpleUtil.log("preareLoadConfigs 当前使用:" + config.getmBelongGame() + "/" + config.getmConfigName());
                        configUsed = config;
                        continue;
                    }

                    keyboardViews.add(loadConfigToMeasure(config));
                }
                keyboardViews.add(loadConfigToMeasure(configUsed));
            }
        });
        SimpleUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                for (KeyboardView keyboardView : keyboardViews) {
                    List<View> allviews = keyboardView.getAllButtonViews();
                    AOAConfigTool.Config config = (AOAConfigTool.Config) keyboardView.getTag();
                    SharedPreferences sharedPreferences = mContext.getSharedPreferences(config.getmTabValue(), 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    SimpleUtil.log("configsha:" + config.getmTabValue());
                    for (View view : allviews) {
                        int[] position = new int[2];
                        view.getLocationOnScreen(position);
                        BtnParams params = (BtnParams) view.getTag();
                        params.setEx(position[0] + view.getWidth() / 2);
                        params.setEy(position[1] + view.getHeight() / 2);
                        editor.putInt(params.getBelongBtn().getPrefEX() + (params.iAnChild() ? "_2" : ""), params.getEx());
                        editor.putInt(params.getBelongBtn().getPrefEY() + (params.iAnChild() ? "_2" : ""), params.getEy());
                        SimpleUtil.log(config.getmBelongGame() + "/" + config.getmConfigName() + " preload:" + params.toString() + ",getexykey:" + (params.getBelongBtn().getPrefEX() + (params.iAnChild() ? "_2" : "")));
                    }
                    editor.commit();
                    KeyboardEditWindowManager.getInstance().removeView(keyboardView);
                }
                iMeasureResult.measurefinish();
            }
        }, 200);
    }

    private KeyboardView loadConfigToMeasure(AOAConfigTool.Config config) {
        BtnParamTool.setDefaultUseConfig(mContext, config.getmConfigName(), config.getmTabValue(), config.getmBelongGame());
        KeyboardView mKeyboardView2 = new KeyboardView(mContext);
        Random random = new Random();
        mKeyboardView2.setId(R.id.aoaparam_ok + random.nextInt());
        mKeyboardView2.setVisibility(View.INVISIBLE);
        KeyboardEditWindowManager.getInstance().addView(mKeyboardView2);
        mKeyboardView2.setTag(config);
        return mKeyboardView2;
    }

    public interface IMeasureResult {
        void measurefinish();
    }

}
