package com.uubox.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.uubox.tools.SimpleUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cj on 2016/11/30.
 */

public class FactroyTestView extends View {

    public FactroyTestView(Context context) {
        super(context);
    }


    Paint paint;
    private Bitmap mMap;
    private Canvas mCanvas;
    private Handler mHandler;
    private final int HANDLE_CLEAR = 1;
    private final int[] COLORS = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.MAGENTA, Color.BLACK, Color.MAGENTA, Color.LTGRAY, Color.CYAN, Color.GRAY};
    private final int MAPBG = Color.WHITE;
    public FactroyTestView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(30);
        mMap = Bitmap.createBitmap(SimpleUtil.zoomy, SimpleUtil.zoomx, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mMap);
        mCanvas.drawColor(MAPBG);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLE_CLEAR:
                        mCanvas.drawColor(MAPBG);
                        invalidate();
                        break;
                }
            }
        };
    }

    //SparseArray<Path> mActivePointers = new SparseArray<>();
    private List<PathItem> items = new ArrayList<>();
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int curPointerIndex = event.getActionIndex();
        int curPointerId = event.getPointerId(curPointerIndex);
        int actionMasked = event.getActionMasked();
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                mHandler.removeMessages(HANDLE_CLEAR);
                PointF pointF = new PointF();
                pointF.x = event.getX(curPointerIndex);
                pointF.y = event.getY(curPointerIndex);
                Path p = new Path();
                p.moveTo(pointF.x, pointF.y);
                PathItem item = new PathItem();
                item.id = curPointerId;
                item.color = COLORS[curPointerId];
                item.path = p;
                paint.setColor(item.color);
                items.add(item);
                mCanvas.drawPoint(pointF.x, pointF.y, paint);
                break;

            case MotionEvent.ACTION_MOVE:
                mHandler.removeMessages(HANDLE_CLEAR);
                Log.i("test", "move:" + items.size() + "  " + curPointerId);
                for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                    curPointerId = event.getPointerId(i);
                    for (int k = 0; k < items.size(); k++) {
                        if (items.get(k).id == curPointerId) {
                            PathItem item2 = items.get(k);
                            paint.setColor(item2.color);
                            if (item2 != null) {
                                item2.path.lineTo(event.getX(i), event.getY(i));
                            }
                            mCanvas.drawPath(item2.path, paint);
                            break;
                        }
                    }
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                int index = -1;
                for (int j = 0; j < items.size(); j++) {
                    if (items.get(j).id == curPointerId) {
                        index = j;
                        break;
                    }
                }
                items.remove(index);
                if (items.size() == 0) {
                    mHandler.sendEmptyMessageDelayed(HANDLE_CLEAR, 2000);
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*canvas.drawColor(Color.WHITE);
        for (int size = mActivePointers.size(), i = 0; i < size; i++) {
            Path path = mActivePointers.valueAt(i);
            canvas.drawPath(path, paint);
        }*/

        canvas.drawBitmap(mMap, 0, 0, paint);

    }

    class PathItem {
        int id;
        int color;
        Path path;

        @Override
        public boolean equals(Object obj) {
            return id == ((PathItem) obj).id;
        }
    }
}
