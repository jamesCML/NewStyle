package com.uubox.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.uubox.tools.SimpleUtil;


/**
 * Created by CG_Dawson on 2018/4/12.
 */

public class InjectTestView extends View {

    private Paint mDrawPaint;
    private Paint mCanvasPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private final int CANVAS_COLOR = Color.parseColor("#aa000000");

    public InjectTestView(Context context) {
        super(context);
        mDrawPaint = new Paint();
        mCanvasPaint = new Paint();
        mCanvasPaint.setColor(CANVAS_COLOR);
        mBitmap = Bitmap.createBitmap(SimpleUtil.zoomy, SimpleUtil.zoomx, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(CANVAS_COLOR);

        setFocusable(true);
        requestFocus();
    }

    public InjectTestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);


    }

    public InjectTestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mCanvasPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int count = event.getPointerCount();
        float[] x = new float[count];
        float[] y = new float[count];
        for (int i = 0; i < count; i++) {
            x[i] = event.getX(i);
            y[i] = event.getY(i);
            SimpleUtil.log("i=" + i + "," + event.getPointerId(i));
        }
        int action = event.getAction();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_1_DOWN || action == MotionEvent.ACTION_POINTER_2_DOWN) {
            mDrawPaint.setColor(Color.GREEN);
            for (int i = 0; i < count; i++) {
                if (x[i] > 0) {
                    mCanvas.drawCircle(x[i], y[i], 20, mDrawPaint);
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {

            mDrawPaint.setColor(Color.YELLOW);
            for (int i = 0; i < count; i++) {
                if (x[i] > 0) {
                    mCanvas.drawCircle(x[i], y[i], 5, mDrawPaint);
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_1_UP || action == MotionEvent.ACTION_POINTER_2_UP) {
            mDrawPaint.setColor(CANVAS_COLOR);
            for (int i = 0; i < count; i++) {
                if (x[i] > 0) {
                    mCanvas.drawCircle(x[i], y[i], 20, mDrawPaint);
                }
            }
        }
        /*for (int i = 0; i < 3; i++) {
            x[i] = -1f;
            y[i] = -1f;
        }*/
        invalidate();
        return true;
    }

}
