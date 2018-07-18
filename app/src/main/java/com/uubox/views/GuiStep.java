package com.uubox.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.uubox.padtool.R;
import com.uubox.tools.SimpleUtil;


public class GuiStep {

    private LinkedList<GuiObject> mViewStack;
    private Context mContext;
    private FrameLayout mParant;
    private List<View> mCurViews;
    private Handler mHandler;
    private final int FRESH_ON = 0xAA0000;
    private final int FRESH_OFF = 0xAA0001;
    private HashMap<String, List<View>> mBuff;
    private TextView mTvMsg;
    private List<Bitmap> mBitmap;
    private boolean isTop;
    private String mKey;

    private GuiStep() {
    }

    public static GuiStep getInstance() {
        return Holder.instance;
    }

    public GuiStep init(Context context, String key, FrameLayout myFrameLayout) {
        mKey = key;
        boolean isSkip = (Boolean) SimpleUtil.getFromShare(context, "ini", mKey, boolean.class, false);
        if (isSkip) {
            mViewStack = null;
            mParant = null;
            return Holder.instance;
        }
        mContext = context;
        if (myFrameLayout != null) {
            mParant = myFrameLayout;
        } else {
            mParant = new FrameLayout(mContext);
        }
        mParant.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                return true;
            }
        });
        mParant.setBackgroundColor(Color.parseColor("#bb000000"));
        mTvMsg = new TextView(context);
        mTvMsg.setTextColor(context.getResources().getColor(R.color.gold));
        mTvMsg.setTextSize(20);
        mTvMsg.setText(R.string.welcome_tips);
        mTvMsg.setTypeface(Typeface.DEFAULT_BOLD);

        Button skip = new Button(context);
        skip.setTextColor(context.getResources().getColor(R.color.truered));
        skip.setBackgroundColor(Color.TRANSPARENT);
        skip.setTextSize(20);
        skip.setText(R.string.jump_over_str);
        skip.setTypeface(Typeface.DEFAULT_BOLD);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SimpleUtil.saveToShare(mContext, "ini", mKey, true);
                mHandler.removeCallbacks(null);
                if (isTop) {
                    KeyboardEditWindowManager.getInstance().removeView(mParant);
                } else {
                    mParant.setVisibility(View.GONE);
                }
                mParant = null;
                mViewStack = null;
            }
        });

        FrameLayout.LayoutParams skipParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        skipParam.gravity = Gravity.LEFT | Gravity.TOP;
        skipParam.rightMargin = 10;
        skipParam.bottomMargin = 10;
        mParant.addView(skip, skipParam);


        setAnimotion(10000);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mParant.addView(mTvMsg, params);
        mViewStack = new LinkedList<>();
        mCurViews = new ArrayList<>();
        mBuff = new HashMap<>();
        mBitmap = new ArrayList<>();
        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                if (what == FRESH_ON) {
                    for (View view : mCurViews) {
                        view.setVisibility(View.VISIBLE);
                    }
                    mHandler.sendEmptyMessageDelayed(FRESH_OFF, 500);
                } else if (what == FRESH_OFF) {
                    for (View view : mCurViews) {
                        view.setVisibility(View.INVISIBLE);
                    }
                    mHandler.sendEmptyMessageDelayed(FRESH_ON, 500);
                }
            }
        };
        mParant.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    boolean b = next();
                    if (!b) {
                        mHandler.removeCallbacks(null);
                        if (isTop) {
                            KeyboardEditWindowManager.getInstance().removeView(mParant);
                        } else {
                            mParant.setVisibility(View.GONE);
                            mParant = null;
                        }
                        return true;
                    }
                }
                return true;
            }
        });


        return Holder.instance;
    }

    public GuiStep init(Context context, String key) {
        return init(context, key, null);
    }

    private void setAnimotion(int reCount) {
        mTvMsg.clearAnimation();
        AnimationSet animationSet = new AnimationSet(false);
        AlphaAnimation alphato = new AlphaAnimation(0, 1);
        alphato.setDuration(1000);
        alphato.setRepeatCount(reCount);
        alphato.setFillAfter(true);
        animationSet.addAnimation(alphato);
        if (reCount == 0) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            scaleAnimation.setDuration(300);
            alphato.setDuration(300);
            scaleAnimation.setRepeatCount(reCount);
            scaleAnimation.setFillAfter(true);
            animationSet.addAnimation(scaleAnimation);
        }
        mTvMsg.startAnimation(animationSet);

        //mTvMsg.setBackgroundColor(Color.parseColor("#5500ff00"));
    }

    public void show(boolean isPortrait, boolean isTop_) {
        if (mParant == null || SimpleUtil.zoomx == 0) {
            return;
        }
        this.isTop = isTop_;
        if (isTop) {
            if (isPortrait) {
                KeyboardEditWindowManager.getInstance().addView(mParant, SimpleUtil.zoomx, SimpleUtil.zoomy);
            } else {
                KeyboardEditWindowManager.getInstance().addView(mParant, SimpleUtil.zoomy, SimpleUtil.zoomx);
            }
        } else {
            mParant.setVisibility(View.VISIBLE);
        }
    }

    public synchronized void addToGui(List<View> views, String guiMsg) {
        if (mViewStack == null) {
            return;
        }
        GuiObject object = new GuiObject();
        object.mViews = views;
        object.mGuiMsg = guiMsg;
        mViewStack.addLast(object);

    }

    public synchronized void addToGui(View view, String guiMsg) {
        if (mViewStack == null) {
            return;
        }
        List<View> list = new ArrayList<>();
        list.add(view);

        GuiObject object = new GuiObject();
        object.mViews = list;
        object.mGuiMsg = guiMsg;

        mViewStack.addLast(object);

    }

    public synchronized void addtToBuff(String buffName, View view) {
        if (mViewStack == null) {
            return;
        }
        if (mBuff.containsKey(buffName)) {
            mBuff.get(buffName).add(view);
        } else {
            List<View> list = new ArrayList<>();
            list.add(view);
            mBuff.put(buffName, list);
        }


    }

    public synchronized void pushBuffToGui(String buffName, String guiMsg) {
        if (mViewStack == null) {
            return;
        }
        if (!mBuff.containsKey(buffName)) {
            return;
        }

        GuiObject object = new GuiObject();
        object.mViews = mBuff.get(buffName);
        object.mGuiMsg = guiMsg;
        mViewStack.addLast(object);
        mBuff.remove(buffName);

    }

    private static Bitmap getViewBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (view instanceof TextView) {

            TextView tempTextView = (TextView) view;
            ColorStateList colorList = tempTextView.getTextColors();
            Drawable bg = tempTextView.getBackground();
            tempTextView.setTextColor(Color.YELLOW);
            //tempTextView.setBackgroundResource(R.drawable.dialog_message_box_r);

            tempTextView.draw(canvas);
            Paint paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            canvas.drawRect(0, 0, view.getWidth(), view.getHeight(), paint);


            tempTextView.setTextColor(colorList);
            //tempTextView.setBackground(bg);
            return bitmap;
        }

        view.draw(canvas);
        return bitmap;

    }

    private boolean next() {
        if (mViewStack.size() == 0) {
            return false;
        }
        mHandler.removeCallbacksAndMessages(null);
        GuiObject element = mViewStack.pop();

        setAnimotion(0);
        mTvMsg.setText(element.mGuiMsg);
        SimpleUtil.log(element.mGuiMsg + "");
        int len = mCurViews.size();
        for (int i = 0; i < len; i++) {
            mParant.removeView(mCurViews.get(i));
            mBitmap.get(i).recycle();
        }

        mCurViews.clear();
        mBitmap.clear();
        List<View> list = element.mViews;
        for (View view : list) {
            ImageView imageView = new ImageView(mContext);
            Bitmap bitmap = getViewBitmap(view);
            mBitmap.add(bitmap);
            imageView.setImageBitmap(bitmap);
            int[] position = {0, 0};
            view.getLocationOnScreen(position);
            FrameLayout.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.width = view.getWidth();
            layoutParams.height = view.getHeight();
            layoutParams.leftMargin = position[0];
            layoutParams.topMargin = position[1];

            mParant.addView(imageView, layoutParams);
            mCurViews.add(imageView);

        }
        mHandler.sendEmptyMessage(FRESH_ON);

        return true;
    }

    private static class Holder {
        public static GuiStep instance = new GuiStep();
    }

    private class GuiObject {
        List<View> mViews;
        String mGuiMsg;
    }
}
