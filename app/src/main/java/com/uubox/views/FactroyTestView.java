package com.uubox.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by cj on 2016/11/30.
 */

public class FactroyTestView extends View {

    public FactroyTestView(Context context) {
        super(context);
    }


    Paint paint;

    public FactroyTestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(30);

    }

    SparseArray<Path> mActivePointers = new SparseArray<>();

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //当前DOWN或者UP的是手指的index
        int curPointerIndex = event.getActionIndex();
        //通过index获得当前手指的id
        int curPointerId = event.getPointerId(curPointerIndex);

        int actionMasked = event.getActionMasked();

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                PointF pointF = new PointF();
                pointF.x = event.getX(curPointerIndex);
                pointF.y = event.getY(curPointerIndex);
                Path p = new Path();
                p.moveTo(pointF.x, pointF.y);
                mActivePointers.append(curPointerId, p);
                break;

            case MotionEvent.ACTION_MOVE:
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    Path path = mActivePointers.get(event.getPointerId(i));
                    if (path != null) {
                        path.lineTo(event.getX(i), event.getY(i));
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mActivePointers.remove(curPointerId);
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        for (int size = mActivePointers.size(), i = 0; i < size; i++) {
            Path path = mActivePointers.valueAt(i);
            canvas.drawPath(path, paint);
        }
    }
}
