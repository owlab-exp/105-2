package com.example.android.calendartest6;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by ernest on 7/19/16.
 */
public class TimeLineView  extends View {
    private static LOG _log = new LOG(TimeLineView.class);

    private float mCenterLineThickness;
    private int mCenterLineColor;
    private Paint mCenterLinePaint;

    private float mTimeLabelTextSize;
    private int mTimeLabelTextColor;
    private Paint mTimeLabelTextPaint;
    private int mTimeLabelHeight;
    private int mTimeLabelMaxWidth;

    private float mTimeLineXThickness;
    private int mTimeLineXColor;
    private Paint mTimeLineXPaint;

    private float mHalfTimeLineXThickness;
    private int mHalfTimeLineXColor;
    private Paint mHalfTimeLineXPaint;

    private Rect mContentRect = new Rect();

    private static final float AXIS_X_MIN = -1f;
    private static final float AXIS_X_MAX = 1f;
    private static final float AXIS_Y_MIN = -1f;
    private static final float AXIS_Y_MAX = 1f;

    private RectF mCurrentViewport = new RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX);

    public TimeLineView(Context ctx) {
        this(ctx, null, 0);
    }

    public TimeLineView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, 0);
    }


    public TimeLineView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);

        TypedArray ta = ctx.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeLineView, defStyle, defStyle);

        try {
            mCenterLineThickness = ta.getDimension(R.styleable.TimeLineView_centerLineThickness, mCenterLineThickness);
            mCenterLineColor = ta.getColor(R.styleable.TimeLineView_centerLineColor, mCenterLineColor);

            mTimeLabelTextSize = ta.getDimension(R.styleable.TimeLineView_timeLabelTextSize, mTimeLabelTextSize);
            mTimeLabelTextColor = ta.getColor(R.styleable.TimeLineView_timeLabelTextColor, mTimeLabelTextColor);
            mTimeLineXThickness = ta.getDimension(R.styleable.TimeLineView_timeLineThickness, mTimeLineXThickness);
            mTimeLineXColor = ta.getColor(R.styleable.TimeLineView_timeLineColor, mTimeLineXColor);
            mHalfTimeLineXThickness = ta.getDimension(R.styleable.TimeLineView_halfTimeLineThickness, mHalfTimeLineXThickness);
            mHalfTimeLineXColor = ta.getColor(R.styleable.TimeLineView_halfTimeLineColor, mHalfTimeLineXColor);
        } finally {
            ta.recycle();
        }

        initPaints();

        // TODO Sets up interactions

        // TODO sets up edge effects
    }

    private void initPaints() {
        mCenterLinePaint = new Paint();
        mCenterLinePaint.setAntiAlias(true);
        mCenterLinePaint.setStrokeWidth(mCenterLineThickness);
        mCenterLinePaint.setColor(mCenterLineColor);


        mTimeLabelTextPaint = new Paint();
        mTimeLabelTextPaint.setAntiAlias(true);
        mTimeLabelTextPaint.setTextSize(mTimeLabelTextSize);
        mTimeLabelTextPaint.setColor(mTimeLabelTextColor);
        mTimeLabelHeight = (int) Math.abs(mTimeLabelTextPaint.getFontMetrics().top);
        mTimeLabelMaxWidth = (int) mTimeLabelTextPaint.measureText("00");

        mTimeLineXPaint =  new Paint();
        mTimeLineXPaint.setStrokeWidth(mTimeLineXThickness);
        mTimeLineXPaint.setColor(mTimeLineXColor);
        mTimeLineXPaint.setStyle(Paint.Style.STROKE);

        mHalfTimeLineXPaint =  new Paint();
        mHalfTimeLineXPaint.setStrokeWidth(mHalfTimeLineXThickness);
        mHalfTimeLineXPaint.setColor(mHalfTimeLineXColor);
        mHalfTimeLineXPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);

        mContentRect.set(
                getPaddingLeft() + mTimeLabelMaxWidth,
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom() - mTimeLabelHeight
        );
    }

    @Override
    protected  void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minTimePlaneSize = getResources().getDimensionPixelSize(R.dimen.min_time_plane_size);

        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(), resolveSize(minTimePlaneSize + getPaddingLeft() + mTimeLabelMaxWidth + getPaddingRight(), widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(), resolveSize(minTimePlaneSize + getPaddingTop() + mTimeLabelHeight + getPaddingBottom(), heightMeasureSpec))
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        example();
        //TODO draws axes and text labels
        drawTimeLines(canvas);

        //TODO what is this?
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mContentRect);

        canvas.restoreToCount(clipRestoreCount);

        //canvas.drawRect(mContentRect, mTimeLineXPaint);
    }

    private void drawTimeLines(Canvas canvas) {

        _log.d("drawing center Y line");
        //draws center Y line, temporarily
        float centerX = getCenterX();
        canvas.drawLine(centerX, mContentRect.top, centerX, mContentRect.bottom, mCenterLinePaint);

        long nowInMillis = System.currentTimeMillis();

        float previousHourX = 0f;
        float nextHourX = 0f;

        for(int i = 0; (previousHourX = getPreviousHourX(nowInMillis, i)) > mContentRect.left; i++) {
            canvas.drawLine(previousHourX, mContentRect.top, previousHourX, mContentRect.bottom, mTimeLineXPaint);
        }

        for(int i = 0; (nextHourX = getNextHourX(nowInMillis, i)) < mContentRect.right; i++) {
            canvas.drawLine(nextHourX, mContentRect.top, nextHourX, mContentRect.bottom, mTimeLineXPaint);
        }
    }

    private float getCenterX() {
        float centerX = (mContentRect.right  - mContentRect.left) / 2;
        //_log.d("centerX = " + centerX);
        return centerX;
    }

    private static final int MINUTE_DP_UNIT = 4;

    private float getPreviousHourX(long nowInMillis, int nth) {
        long nowInMinutes = TimeUnit.MILLISECONDS.toMinutes(nowInMillis);
        long remainder = nowInMinutes % 60;
        return getCenterX() - remainder * MINUTE_DP_UNIT - nth * 60 * MINUTE_DP_UNIT;
    }

    private float getNextHourX(long nowInMillis, int nth) {
        long nowInMinutes = TimeUnit.MILLISECONDS.toMinutes(nowInMillis);
        long remainder = nowInMinutes % 60;
        return getCenterX() + (60 - remainder) * MINUTE_DP_UNIT + nth * 60 * MINUTE_DP_UNIT;
    }

    private void example() {
        long nowInMillis = System.currentTimeMillis();
        long nowInMinutes = TimeUnit.MILLISECONDS.toMinutes(nowInMillis);
        long nowInHours = TimeUnit.MILLISECONDS.toHours(nowInMillis);
        _log.d("Now in Date = " + new Date(nowInMillis));
        _log.d("Now in minutes = " + nowInMinutes%60);
        _log.d("Now in hours = " + nowInHours%24);
    }
}
