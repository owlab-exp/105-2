package com.example.android.calendartest6;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ernest on 7/19/16.
 */
public class TimeLineView  extends View {
    private static LOG _log = new LOG(TimeLineView.class);

    private float mTimeLabelTextSize;
    private int mTimeLabelTextColor;
    private Paint mTimeLabelTextPaint;
    private int mTimeLabelHeight;
    private int mTimeLabelMaxWidth;

    private float mTimeAxisXThickness;
    private int mTimeAxisXColor;
    private Paint mTimeAxisXPaint;

    private float mHalfTimeAxisXThickness;
    private int mHalfTimeAxisXColor;
    private Paint mHalfTimeAxisXPaint;

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
            mTimeLabelTextSize = ta.getDimension(R.styleable.TimeLineView_timeLabelTextSize, mTimeLabelTextSize);
            mTimeLabelTextColor = ta.getColor(R.styleable.TimeLineView_timeLabelTextColor, mTimeLabelTextColor);
            mTimeAxisXThickness = ta.getDimension(R.styleable.TimeLineView_timeAxisXThickness, mTimeAxisXThickness);
            mTimeAxisXColor = ta.getColor(R.styleable.TimeLineView_timeAxisXColor, mTimeAxisXColor);
            mHalfTimeAxisXThickness = ta.getDimension(R.styleable.TimeLineView_halfTimeAxisXThickness, mHalfTimeAxisXThickness);
            mHalfTimeAxisXColor = ta.getColor(R.styleable.TimeLineView_halfTimeAxisXColor, mHalfTimeAxisXColor);
        } finally {
            ta.recycle();
        }

        initPaints();

        // TODO Sets up interactions

        // TODO sets up edge effects
    }

    private void initPaints() {
        mTimeLabelTextPaint = new Paint();
        mTimeLabelTextPaint.setAntiAlias(true);
        mTimeLabelTextPaint.setTextSize(mTimeLabelTextSize);
        mTimeLabelTextPaint.setColor(mTimeLabelTextColor);
        mTimeLabelHeight = (int) Math.abs(mTimeLabelTextPaint.getFontMetrics().top);
        mTimeLabelMaxWidth = (int) mTimeLabelTextPaint.measureText("00");

        mTimeAxisXPaint =  new Paint();
        mTimeAxisXPaint.setStrokeWidth(mTimeAxisXThickness);
        mTimeAxisXPaint.setColor(mTimeAxisXColor);
        mTimeAxisXPaint.setStyle(Paint.Style.STROKE);

        mHalfTimeAxisXPaint =  new Paint();
        mHalfTimeAxisXPaint.setStrokeWidth(mHalfTimeAxisXThickness);
        mHalfTimeAxisXPaint.setColor(mHalfTimeAxisXColor);
        mHalfTimeAxisXPaint.setStyle(Paint.Style.STROKE);
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

        //TODO draws axes and text labels
        drawYAxes(canvas);

        //TODO what is this?
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mContentRect);

        canvas.restoreToCount(clipRestoreCount);

        //canvas.drawRect(mContentRect, mTimeAxisXPaint);
    }

    private void drawYAxes(Canvas canvas) {

        _log.d("drawing center Y line");
        //draws center Y line, temporarily
        canvas.drawLine(getCenterX(), mContentRect.top, getCenterX(), mContentRect.bottom, mTimeAxisXPaint);

    }

    private float getCenterX() {
        return (mContentRect.right  - mContentRect.left) / 2;
    }

}
