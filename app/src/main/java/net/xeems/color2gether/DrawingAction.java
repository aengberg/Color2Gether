package net.xeems.color2gether;

import java.util.Random;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Canvas;

/**
 * Created by anden11 on 2014-11-11.
 */
public class DrawingAction {
    public Path mPath;
    public Paint mPaint;
    private Random random = new Random();
    public float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private Canvas mCanvas;

    public DrawingAction(Canvas _c)
    {
        mCanvas = _c;
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(32);
    }

    public void touch_start(float x, float y, float scale) {
        mPaint.setColor(random.nextInt());
        mPaint.setAlpha(255);
        mPath.moveTo(x, y);
        mPaint.setStrokeWidth(32 / scale);
        mX = x;
        mY = y;
    }

    public void touch_move(float x, float y, float scale) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        mPaint.setStrokeWidth(32 / scale);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    public void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
    }
}
