package com.uubox.tools;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.uubox.padtool.R;
import com.uubox.views.KeyboardEditWindowManager;
import com.uubox.views.KeyboardView;


/**
 * Created by CG_Dawson on 2018/3/1.
 */

public class SaveBtnParamsTask extends AsyncTask<String, Integer, Void> {
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
        SimpleUtil.addWaitToTop(mContext, "正在保存，请稍后...");
    }

    @Override
    protected Void doInBackground(String... args) {
        String[] sp = args[0].split("#Z%W#", -1);
        isNewIni = sp[1].equals(sp[2]);
        InjectUtil.saveBtnParams(mContext, args[0]);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mKeyboardView != null) {
            // SimpleUtil.notifyall_(1, null);
        }
        SimpleUtil.resetWaitTop();
        Toast.makeText(mContext, "保存成功！",
                Toast.LENGTH_SHORT).show();

        if (isNewIni) {
            InjectUtil.loadBtnParamsFromPrefs(mContext);
            mKeyboardView.loadUi();
        }
        KeyboardEditWindowManager.getInstance().close();
        SimpleUtil.notifyall_(10003, null);

    }

}
