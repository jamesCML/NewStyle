package com.uubox.tools;

import android.content.Context;
import android.os.AsyncTask;

import com.uubox.padtool.R;
import com.uubox.views.KeyboardEditWindowManager;
import com.uubox.views.KeyboardView;


/**
 * Created by CG_Dawson on 2018/3/1.
 */

public class SaveBtnParamsTask extends AsyncTask<String, Integer, String> {
    private Context mContext;
    private KeyboardView mKeyboardView;
    private boolean isNewIni;

    public SaveBtnParamsTask(Context context) {
        mContext = context;
    }

    public void setmKeyboardView(KeyboardView keyboardView) {
        mKeyboardView = keyboardView;
    }

    @Override
    protected void onPreExecute() {
        SimpleUtil.addWaitToTop(mContext, mContext.getString(R.string.sbp_saveloading));
    }

    @Override
    protected String doInBackground(String... args) {
        args[0] = SimpleUtil.entozhChange(mContext, args[0]);
        BtnParamTool.setComfirGame(SimpleUtil.entozhChange(mContext, BtnParamTool.getComfirGame()));
        String[] sp = args[0].split("#Z%W#", -1);
        isNewIni = sp[1].equals(sp[2]);
        BtnParamTool.saveBtnParams(mContext, args[0]);
        return sp[1];
    }

    @Override
    protected void onPostExecute(String aVoid) {
        if (mKeyboardView != null) {
            // SimpleUtil.notifyall_(1, null);
        }
        SimpleUtil.resetWaitTop(mContext);
        SimpleUtil.addMsgBottomToTop(mContext, mContext.getString(R.string.sbp_savelocal), false);

        if (isNewIni) {
            BtnParamTool.loadBtnParamsFromPrefs(mContext);
            mKeyboardView.loadUi();
            SimpleUtil.saveToShare(mContext, "ini", "NewConfigNotWrite", aVoid);

        }
        SimpleUtil.notifyall_(10003, null);
        KeyboardEditWindowManager.getInstance().close();

    }

}
