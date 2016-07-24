package com.example.android.calendartest6;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;

import java.util.Date;
import java.util.Objects;
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

    private float mTimeLineThickness;
    private int mTimeLineColor;
    private Paint mTimeLinePaint;

    private float mHalfTimeLineThickness;
    private int mHalfTimeLineColor;
    private Paint mHalfTimeLinePaint;

    private Rect mContentRect = new Rect();

    private static final float AXIS_X_MIN = -1f;
    private static final float AXIS_X_MAX = 1f;
    private static final float AXIS_Y_MIN = -1f;
    private static final float AXIS_Y_MAX = 1f;

    private RectF mCurrentViewport = new RectF(AXIS_X_MIN, AXIS_Y_MIN, AXIS_X_MAX, AXIS_Y_MAX);

    private GestureDetectorCompat mGestureDetector;
    private OverScroller mScroller;
    private RectF mScrollerStartViewPort = new RectF();
    private Point mSurfaceSizeBuffer = new Point();

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
            mTimeLineThickness = ta.getDimension(R.styleable.TimeLineView_timeLineThickness, mTimeLineThickness);
            mTimeLineColor = ta.getColor(R.styleable.TimeLineView_timeLineColor, mTimeLineColor);
            mHalfTimeLineThickness = ta.getDimension(R.styleable.TimeLineView_halfTimeLineThickness, mHalfTimeLineThickness);
            mHalfTimeLineColor = ta.getColor(R.styleable.TimeLineView_halfTimeLineColor, mHalfTimeLineColor);
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

        mTimeLinePaint =  new Paint();
        mTimeLinePaint.setStrokeWidth(mTimeLineThickness);
        mTimeLinePaint.setColor(mTimeLineColor);
        mTimeLinePaint.setStyle(Paint.Style.STROKE);

        mHalfTimeLinePaint =  new Paint();
        mHalfTimeLinePaint.setStrokeWidth(mHalfTimeLineThickness);
        mHalfTimeLinePaint.setColor(mHalfTimeLineColor);
        mHalfTimeLinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);

        mContentRect.set(
                //getPaddingLeft() + mTimeLabelMaxWidth,
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                //getHeight() - getPaddingBottom() - mTimeLabelHeight
                getHeight() - getPaddingBottom()
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

        //example();
        //TODO draws axes and text labels
        drawTimeLines(canvas);

        //TODO what is this?
        int clipRestoreCount = canvas.save();
        canvas.clipRect(mContentRect);

        canvas.restoreToCount(clipRestoreCount);

        //canvas.drawRect(mContentRect, mTimeLinePaint);
    }

    float mTimeLabelSeparation = 9f;
    private void drawTimeLines(Canvas canvas) {

        _log.d("drawing center Y line");
        long nowInMillis = System.currentTimeMillis();

        //float previousHourX = 0f;
        float nextHourX = 0f;
        float nextHalfHourX = 0f;

        //draws previous times
        for(int i = -1; (nextHourX = getNextHourX(nowInMillis, i)) >= mContentRect.left; i--) {
            canvas.drawLine(nextHourX, mContentRect.top, nextHourX, mContentRect.bottom, mTimeLinePaint);
            long hour = getNextHourIn24(nowInMillis, i);
            String hourStr = (hour <= 12) ? Objects.toString(hour) + " am" : Objects.toString(hour - 12) + " pm";
            canvas.drawText(hourStr, nextHourX + mTimeLabelSeparation, mContentRect.top + mTimeLabelHeight, mTimeLabelTextPaint);
        }

        //draws afterward
        for(int i = 0; (nextHourX = getNextHourX(nowInMillis, i)) <= mContentRect.right; i++) {
            canvas.drawLine(nextHourX, mContentRect.top, nextHourX, mContentRect.bottom, mTimeLinePaint);
            long hour = getNextHourIn24(nowInMillis, i);
            String hourStr = (hour <= 12) ? Objects.toString(hour) + " am" : Objects.toString(hour - 12) + " pm";
            canvas.drawText(hourStr, nextHourX + mTimeLabelSeparation, mContentRect.top + mTimeLabelHeight, mTimeLabelTextPaint);
        }

        //draws half time lines
        for(int i = -1; (nextHalfHourX = getNextHalfHourX(nowInMillis, i)) >= mContentRect.left; i--) {
            _log.d("drawing half time line");
            canvas.drawLine(nextHalfHourX, mContentRect.top, nextHalfHourX, mContentRect.bottom, mHalfTimeLinePaint);
        }
        for(int i = 0; (nextHalfHourX = getNextHalfHourX(nowInMillis, i)) <= mContentRect.right; i++) {
            canvas.drawLine(nextHalfHourX, mContentRect.top, nextHalfHourX, mContentRect.bottom, mHalfTimeLinePaint);
        }

        //draws center Y line, temporarily
        float centerX = getCenterX();
        canvas.drawLine(centerX, mContentRect.top, centerX, mContentRect.bottom, mCenterLinePaint);
    }

    private float getCenterX() {
        float centerX = (mContentRect.right  - mContentRect.left) / 2;
        //_log.d("centerX = " + centerX);
        return centerX;
    }

    private static final int MINUTE_DP_UNIT = 4;

    //TODO
    //

    private float getNextHourX(long nowInMillis, int nextNth) {
        long nowInMinutes = TimeUnit.MILLISECONDS.toMinutes(nowInMillis);
        long remainder = nowInMinutes % 60;

        return getCenterX() + (60 - remainder) * MINUTE_DP_UNIT + nextNth * 60 * MINUTE_DP_UNIT;
    }

    private float getNextHalfHourX(long nowInMillis, int nextNth) {
        return getNextHourX(nowInMillis, nextNth) + 30 * MINUTE_DP_UNIT;
    }

    private long getNextHourIn24(long nowInMillis, int nextNth) {
        long nowInHours = TimeUnit.MILLISECONDS.toHours(nowInMillis);
        long nextIn24Hours = (nowInHours + 1 + nextNth) % 24;

        return nextIn24Hours;
    }

    private void example() {
        long nowInMillis = System.currentTimeMillis();
        long nowInMinutes = TimeUnit.MILLISECONDS.toMinutes(nowInMillis);
        long nowInHours = TimeUnit.MILLISECONDS.toHours(nowInMillis);
        _log.d("Now in Date = " + new Date(nowInMillis));
        _log.d("Now in minutes = " + nowInMinutes%60);
        _log.d("Now in hours = " + nowInHours%24);
    }

    /////////////////////////
    //
    //  Gestures
    //
    /////////////////////////

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        _log.d("onTouchEvent: " + Objects.toString(event));
        return super.onTouchEvent(event);
    }

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent event) {
            mScrollerStartViewPort.set(mCurrentViewport);
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(TimeLineView.this);
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
            float viewportOffsetX = distanceX * mCurrentViewport.width() / mContentRect.width();
            float viewportOffsetY = -distanceY * mCurrentViewport.height() / mContentRect.height();
            computeScrollSurfaceSize(mSurfaceSizeBuffer);
            int scrolledX = (int) (mSurfaceSizeBuffer.x * (mCurrentViewport.left + viewportOffsetX - AXIS_X_MIN) / (AXIS_X_MAX - AXIS_X_MIN));
            int scrolledY = (int) (mSurfaceSizeBuffer.y * (mCurrentViewport.bottom + viewportOffsetY) / (AXIS_Y_MAX - AXIS_Y_MIN));
            boolean canScrollX = mCurrentViewport.left > AXIS_X_MIN || mCurrentViewport.right < AXIS_X_MAX;
            boolean canScrollY = mCurrentViewport.top > AXIS_Y_MIN || mCurrentViewport.bottom < AXIS_Y_MAX;
            setViewportBottomLeft(mCurrentViewport.left + viewportOffsetX, mCurrentViewport.bottom + viewportOffsetY);

            if(canScrollX && scrolledX < 0) {
                //Edge handling
            }
            if(canScrollY && scrolledY < 0) {
                //Edge handling
            }
            //return super.onScroll(event1, event2, distanceX, distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };

    private void computeScrollSurfaceSize(Point out) {
        out.set(
                (int) (mContentRect.width() * (AXIS_X_MAX - AXIS_X_MIN)/ mCurrentViewport.width()),
                (int) (mContentRect.height() * (AXIS_Y_MAX - AXIS_Y_MIN) / mCurrentViewport.height())
        );
    }

    private void setViewportBottomLeft(float x, float y) {
        float curWidth = mCurrentViewport.width();
        float curHeight = mCurrentViewport.height();
        x = Math.max(AXIS_X_MIN, Math.min(x, AXIS_X_MAX - curWidth));
        y = Math.max(AXIS_Y_MIN + curHeight, Math.min(y, AXIS_Y_MAX));

        mCurrentViewport.set(x, y - curHeight, x + curWidth, y);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void fling(int velocityX, int velocityY) {
        //TODO release edge effect

        computeScrollSurfaceSize(mSurfaceSizeBuffer);
        mScrollerStartViewPort.set(mCurrentViewport);
        int startX = (int) (mSurfaceSizeBuffer.x * (mScrollerStartViewPort.left - AXIS_X_MIN)/ (AXIS_X_MAX - AXIS_X_MIN));
        int startY = (int) (mSurfaceSizeBuffer.y * (AXIS_Y_MAX - mScrollerStartViewPort.bottom) / (AXIS_Y_MAX - AXIS_Y_MIN));
        mScroller.forceFinished(true);
        mScroller.fling(
                startX,
                startY,
                velocityX,
                velocityY,
                0, mSurfaceSizeBuffer.x - mContentRect.width(),
                0, mSurfaceSizeBuffer.y - mContentRect.height(),
                mContentRect.width() / 2,
                mContentRect.height() /2
        );

        ViewCompat.postInvalidateOnAnimation(this);
    }
}
