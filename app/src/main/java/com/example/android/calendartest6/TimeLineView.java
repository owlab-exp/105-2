package com.example.android.calendartest6;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.concurrent.TimeUnit;

/**
 * Created by ernest on 7/19/16.
 */
public class TimeLineView  extends View {
    private static LOG _log = new LOG(TimeLineView.class);

    private float mNowLineThickness;
    private int mNowLineColor;
    private Paint mNowLinePaint;

    private int mMinuteUnitDp = 2;

    private Rect mContentRect = new Rect();

    private PointF mNowPoint;// = new PointF();
    private long mNowInMinutes;//

    public TimeLineView(Context context) {
        super(context, null);
    }

    public TimeLineView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeLineView, defStyleAttr, defStyleAttr);
        try {
            mNowLineThickness = a.getDimension(R.styleable.TimeLineView_nowLineThickness, mNowLineThickness);
            mNowLineColor = a.getColor(R.styleable.TimeLineView_nowLineColor, mNowLineColor);
        } finally {
            a.recycle();
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);

        //Viewable rect
        mContentRect.set(
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom()
        );

        _log.d("mContentRect: " + mContentRect.toString());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minPlaneSize = getResources().getDimensionPixelSize(R.dimen.min_time_plane_size);
        int w = Math.max(getSuggestedMinimumWidth(), resolveSize(minPlaneSize + getPaddingLeft() + getPaddingRight(), widthMeasureSpec));
        int h = Math.max(getSuggestedMinimumHeight(), resolveSize(minPlaneSize + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));
        _log.d("(w, h) = (" + w + ", " + h + ")");
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        initPaints();

        if(mNowPoint == null) {
            //initialize with center
            mNowPoint = new PointF();
            mNowPoint.x = mContentRect.exactCenterX();
            mNowPoint.y = mContentRect.top;

            mNowInMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
        }

        _log.d("mNowPoint: " + mNowPoint.toString());
        _log.d("mNowPoint.x: " + mNowPoint.x);
        _log.d("mContentRect.top: " + mContentRect.top);
        _log.d("mContentRect.bottom: " + mContentRect.bottom);
        _log.d("mNowLinePaint: " + mNowLinePaint.toString());
        canvas.drawLine(mNowPoint.x, mContentRect.top, mNowPoint.x, mContentRect.bottom, mNowLinePaint);
        canvas.drawText("blablaaaaaaaaaaaaaaaaaaaa", mNowPoint.x, mNowPoint.y, mNowLinePaint);

        //int clipRestoreCount = canvas.save();
    }

    private void initPaints() {
        mNowLinePaint = new Paint();
        mNowLinePaint.setAntiAlias(true);
        mNowLinePaint.setColor(mNowLineColor);
        mNowLinePaint.setStrokeWidth(mNowLineThickness);
        mNowLinePaint.setStyle(Paint.Style.STROKE);
    }

    //Time to position, position to time
    //Knowing
}
