package net.xeems.color2gether;

import android.content.Context;
import android.gesture.Gesture;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.DropBoxManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ScaleGestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.MotionEventCompat;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anden11 on 2014-11-11.
 */
public class DrawingView extends View implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener{

    private static final String DEBUG_TAG = "Gestures";
    public GestureDetectorCompat mDetector;
    private ScaleGestureDetector mScaleDetector;

    private boolean isLongPress;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint mBitmapPaint;
    Context context;
    private HashMap<Integer, DrawingAction> drawingActionMap = new HashMap<Integer, DrawingAction>();

    private int height;
    private int width;

    private int scrollX;
    private int scrollY;
    private float mScaleFactor = 0.5f;
    private boolean isScaling;

    private Paint textPaint = new Paint();
    private boolean isToolMode;

    public DrawingView(Context c) {
        super(c);

        context = c;

        mDetector = new GestureDetectorCompat(context, this);
        mScaleDetector = new ScaleGestureDetector(context, this);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        textPaint.setColor(0);
        textPaint.setAlpha(255);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w * 2;
        height = h * 2;

        drawingActionMap.clear();
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        Log.d(DEBUG_TAG, "SizeChanged");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.scale(mScaleFactor, mScaleFactor);

        super.onDraw(canvas);

        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

        for (Map.Entry<Integer, DrawingAction> entry : drawingActionMap.entrySet()) {
            DrawingAction fa = entry.getValue();

            canvas.drawPath(fa.mPath, fa.mPaint);
            canvas.drawText(entry.getKey().toString(), fa.mX, fa.mY, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {

            mDetector.onTouchEvent(event);

            int actionMasked = MotionEventCompat.getActionMasked(event);
            int actionIndex = event.getActionIndex();

            int pointerId = event.getPointerId(actionIndex);
            int pointerIndex = event.findPointerIndex(pointerId);

            if (isToolMode)
            {
                mScaleDetector.onTouchEvent(event);
            }
            else if (!isToolMode){
                float x = -1;
                float y = -1;

                switch (actionMasked) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        x = MotionEventCompat.getX(event, pointerIndex);
                        y = MotionEventCompat.getY(event, pointerIndex);

                        if (!drawingActionMap.containsKey(pointerId))
                            drawingActionMap.put(pointerId, new DrawingAction(mCanvas));

                            drawingActionMap.get(pointerId).touch_start(x / mScaleFactor + this.getScrollX() / mScaleFactor, y / mScaleFactor + this.getScrollY() / mScaleFactor, mScaleFactor);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        for (int i = 0; i < MotionEventCompat.getPointerCount(event); i++) {
                            pointerId = event.getPointerId(i);
                            pointerIndex = event.findPointerIndex(pointerId);

                            x = MotionEventCompat.getX(event, pointerIndex);
                            y = MotionEventCompat.getY(event, pointerIndex);

                            drawingActionMap.get(pointerId).touch_move(x / mScaleFactor + this.getScrollX() / mScaleFactor, y / mScaleFactor + this.getScrollY() / mScaleFactor, mScaleFactor);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        drawingActionMap.get(pointerId).touch_up();
                        break;
                }
                invalidate();
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
            return false;
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return false;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
        return false;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        if(isLongPress)
            isLongPress = false;
        else
            isLongPress = true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        if(isToolMode && !isScaling)
        {
            scrollX = Math.max(0, Math.min(scrollX += (int)distanceX, (int)(width*mScaleFactor)-this.getWidth()));
            scrollY = Math.max(0, Math.min(scrollY += (int)distanceY, (int)(height*mScaleFactor)-this.getHeight()));
            this.scrollTo(scrollX, scrollY);

            invalidate();
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());

        if(isToolMode)
            isToolMode = false;
        else
            isToolMode=true;

            invalidate();

        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

        scrollX = (int)(scaleGestureDetector.getFocusX()*mScaleFactor);
        scrollY = (int)(scaleGestureDetector.getFocusY()*mScaleFactor);


        //scrollX = (int)(scaleGestureDetector.getFocusX());
        //scrollY = (int)(scaleGestureDetector.getFocusY());


        //this.scrollTo((int)(scrollX*mScaleFactor), (int)(scrollY*mScaleFactor));
        this.scrollTo(scrollX, scrollY);


        mScaleFactor *= scaleGestureDetector.getScaleFactor();

        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));

        Log.d(DEBUG_TAG, "Focus X: " + scaleGestureDetector.getFocusX());
        Log.d(DEBUG_TAG, "Focus Y: " + scaleGestureDetector.getFocusY());

        //scrollX = Math.max(0, Math.min((int)scaleGestureDetector.getFocusX(), (int)(width*mScaleFactor)-this.getWidth()));
        //scrollY = Math.max(0, Math.min((int)scaleGestureDetector.getFocusY(), (int)(height*mScaleFactor)-this.getHeight()));



        invalidate();

        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        isScaling = true;
        Log.d(DEBUG_TAG, "onScaleBegin: " + scaleGestureDetector.toString());

        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        isScaling = false;
    }
}
