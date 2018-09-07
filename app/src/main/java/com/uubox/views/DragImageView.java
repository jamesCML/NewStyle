package com.uubox.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.uubox.tools.SimpleUtil;


/**
 * 支持缩放和拖拽的ImageView
 *
 * @author 李剑波
 * @date 2015/8/13
 */
public class DragImageView extends android.support.v7.widget.AppCompatImageView {

    private static final String TAG = "DragImageView";
    private static final boolean DEBUG = false;

    /**
     * 触摸位置
     */
    private int mCurrentX, mCurrentY;
    /**
     * 触摸按下时间
     */
    private long mDownTime;
    /**
     * 两触点距离
     */
    private float mBeforeLength,
            mAfterLength;
    /**
     * 缩放比例
     */
    /**
     * 模式
     */
    private MODE mMode = MODE.NONE;
    /**
     * 拖拽事件监听
     */
    private DragListener mDragListener;
    /**
     * 缩放事件监听
     */
    private ScaleListener mScaleListener;
    /**
     * 点击事件监听
     */
    private ClickListener mClickListener;


    public DragImageView(Context context) {
        super(context);
        initScreenParams();
    }

    public DragImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScreenParams();
    }

    public void setScaleListener(ScaleListener scaleListener) {
        mScaleListener = scaleListener;
    }

    public void setDragListener(DragListener dragListener) {
        mDragListener = dragListener;
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    /**
     * touch 事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.i(TAG, "onTouchEvent: " + event.toString());
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event);
                mDownTime = System.currentTimeMillis();
                break;
            // 多点触摸
            case MotionEvent.ACTION_POINTER_DOWN:
                onPointerDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event);
                break;
            case MotionEvent.ACTION_UP:
                if (mMode == MODE.DRAG && mDragListener != null) {
                    mDragListener.onDragFinish(this);
                }
                long duration = System.currentTimeMillis() - mDownTime;
                if (duration < 150 && mClickListener != null) {
                    mClickListener.onDragImageViewClick(this);
                }
                mMode = MODE.NONE;
                break;
            // 多点松开
            case MotionEvent.ACTION_POINTER_UP:
                if (mMode == MODE.ZOOM && mScaleListener != null) {
                    mScaleListener.onScaleFinish(this);
                }
                mMode = MODE.NONE;
                break;
        }

        return true;
    }

    /**
     * 按下
     */
    void onTouchDown(final MotionEvent event) {
        Log.i(TAG, "onTouchDown: " + event);
        mMode = MODE.DRAG;
        mCurrentX = (int) event.getRawX();
        mCurrentY = (int) event.getRawY();

        if (mDragListener != null) {
            mDragListener.onDragStart(this);
        }
        // 对控件进行操作时，将控件置于UI最顶级

       /* SimpleUtil.runOnThread(new Runnable() {
            @Override
            public void run() {
                int i = 5;
                while(i-->0)
                {
                    SimpleUtil.sleep(500);
                    SimpleUtil.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            int[] beforep = new int[2];
                            measure(0,0);
                            getLocationInWindow(beforep);
                            SimpleUtil.log("before:"+(beforep[0])+","+(beforep[1]));
                            //setX(beforep[0]);
                            //setY(beforep[1]+5);

                            ViewGroup.LayoutParams params = getLayoutParams();
                            ((FrameLayout.LayoutParams) params).leftMargin = beforep[0];
                            ((FrameLayout.LayoutParams) params).topMargin = beforep[1]+5;
                            setLayoutParams(params);



                        }
                    });
                }
            }
        });*/


        this.bringToFront();
    }

    /**
     * 多个手指按下处理
     */
    void onPointerDown(MotionEvent event) {
        Log.i(TAG, "onPointerDown: Multiple fingers pressed to handle" + event);
        // 两个手指，放大缩小
        if (event.getPointerCount() == 2) {
            mMode = MODE.ZOOM;
            // 获取两点的距离
            mBeforeLength = getDistance(event);
            if (mScaleListener != null) {
                mScaleListener.onScaleStart(this);
            }
        }
    }

    /**
     * 移动处理
     */
    void onTouchMove(MotionEvent event) {
        //Log.i(TAG, "onTouchMove: event=" + event.toString());
        int left, top, right, bottom;
        // 拖拽
        if (mMode == MODE.DRAG) {

            // 拖拽越界处理
           /* if (left <= 0) {
                left = 0;
                right = this.getWidth();
            }
            if (right >= SimpleUtil.zoomy) {
                left = SimpleUtil.zoomy - this.getWidth();
            }
            if (top <= 0) {
                top = 0;
                bottom = this.getHeight();
            }
            if (bottom >= SimpleUtil.zoomx) {



                top = SimpleUtil.zoomx - this.getHeight();
            }*/
           /* if (DEBUG) {
                Log.d(TAG, "left = " + left + "  ,  " + right + "  ,  " + top + "  ,  " + bottom);
            }
            // 更新坐标
            ViewGroup.LayoutParams params = this.getLayoutParams();
            if (params instanceof FrameLayout.LayoutParams) {

                int[] beforep = new int[2];
                this.getLocationOnScreen(beforep);

                SimpleUtil.log(("before:"+((FrameLayout.LayoutParams) params).leftMargin+","+((FrameLayout.LayoutParams) params).topMargin)+","+beforep[0]+","+beforep[1]);
               // ((FrameLayout.LayoutParams) params).leftMargin = (int) event.getRawX();
                //((FrameLayout.LayoutParams) params).topMargin = (int) event.getRawY();
            }*/

            //this.setLayoutParams(params);
            int[] beforep = new int[2];


            int distx = (int) event.getRawX() - mCurrentX;
            int disty = (int) event.getRawY() - mCurrentY;

            measure(0, 0);
            getLocationInWindow(beforep);
            // SimpleUtil.log("before:"+(beforep[0])+","+(beforep[1]));
            //setX(beforep[0]);
            //setY(beforep[1]+5);

            ViewGroup.LayoutParams params = getLayoutParams();
            ((FrameLayout.LayoutParams) params).leftMargin = beforep[0] + distx;
            ((FrameLayout.LayoutParams) params).topMargin = beforep[1] + disty;
            setLayoutParams(params);
            //SimpleUtil.log("position:" + ((FrameLayout.LayoutParams) params).leftMargin + "," + ((FrameLayout.LayoutParams) params).topMargin);


            mCurrentX = (int) event.getRawX();
            mCurrentY = (int) event.getRawY();
        }
        // 缩放
        else if (mMode == MODE.ZOOM && mScaleListener != null) {
            // 获取两点的距离
            mAfterLength = getDistance(event);
            // 变化的长度
            float gapLenght = mAfterLength - mBeforeLength;

            if (Math.abs(gapLenght) > 5f) {
                // 缩放的比例
                float mScaleRatio = mAfterLength / mBeforeLength;
                ViewGroup.LayoutParams params = this.getLayoutParams();
                params.width = (int) (this.getWidth() * mScaleRatio);
                params.height = (int) (this.getHeight() * mScaleRatio);
                if (params instanceof FrameLayout.LayoutParams) {
                    ((FrameLayout.LayoutParams) params).leftMargin -=
                            (params.width - this.getWidth()) / 2;
                    ((FrameLayout.LayoutParams) params).topMargin -=
                            (params.height - this.getWidth()) / 2;
                }
                this.setLayoutParams(params);
                mBeforeLength = mAfterLength;
            }
        }
    }

    /**
     * 获取两点的距离
     */
    float getDistance(MotionEvent event) {
        Log.i(TAG, "getDistance: " + event.toString());
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);

        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 初始化屏幕的宽度和高度
     */
    private void initScreenParams() {
    }

    /**
     * 模式 NONE：无 DRAG：拖拽. ZOOM: 缩放
     *
     * @author zhangjia
     */
    private enum MODE {
        NONE,
        DRAG,
        ZOOM
    }


    /**
     * 拖拽事件监听
     */
    public interface DragListener {
        /**
         * 拖拽开始
         *
         * @param v 被拖拽的控件
         */
        void onDragStart(View v);

        /**
         * 拖拽完成
         *
         * @param v 被拖拽的控件
         */
        void onDragFinish(View v);
    }


    /**
     * 缩放事件监听
     */
    public interface ScaleListener {
        /**
         * 缩放开始
         *
         * @param v 被缩放的控件
         */
        void onScaleStart(View v);

        /**
         * 缩放完成
         *
         * @param v 被缩放的控件
         */
        void onScaleFinish(View v);
    }

    /**
     * 点击事件监听
     */
    public interface ClickListener {
        void onDragImageViewClick(View v);
    }

}
