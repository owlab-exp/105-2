package com.example.android.calendartest6;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.concurrent.TimeUnit;

/**
 * Created by ernest on 8/7/16.
 */
public class TimeLineView extends FrameLayout {
    private static final LOG _log = new LOG(TimeLineView.class);

    public TimeLineView(Context context) {
        this(context, null);
    }

    public TimeLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimeLineView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private GestureDetector mDetector;
    private Scroller mScroller;
    /* Positions of the last motion event */
    private float mMotionInitX, mMotionInitY;
    /* Drag threshold */
    private int mTouchSlop;

    private Rect mCurrentContentRect = new Rect();
    //private PointF mCurrentTimePoint;
    //private long mCurrentTimeInMinutes;
    private Tuple2<Long, Integer> mCurrentTimeAndX;
    private int mXDiff = 0;
    //private SortedMap<Long, Integer> mVisibleTimeAndXMap = new TreeMap<>();
    private Tuple2<Long, Float> mVisibleStartMinutesX;
    private Tuple2<Long, Float> mVisibleEndMinutesX;
    private Tuple2<Long, Float> mVisibleStartHalfTimeX;
    private Tuple2<Long, Float> mVisibleEndHalfTimeX;

    private float mDpPerMinute;

    private Paint   mTimeLabelPaint;
    private float   mTimeLabelTextSize;
    private int     mTimeLabelTextColor;
    private int     mTimeLabelHeight;
    private int     mTimeLabelMaxWidth;
    private int     mTimeLabelSeparation;

    private Paint   mTimeLinePaint;
    private float   mTimeLineThickness;
    private int     mTimeLineColor;

    private Paint   mHalfTimeLinePaint;
    private float   mHalfTimeLineThickness;
    private int     mHalfTimeLineColor;

    /* Listener to handle all the touch events */
    private GestureDetector.SimpleOnGestureListener mListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            if(!mScroller.isFinished()) {
                mScroller.abortAnimation();
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            //_log.d("onScroll: (dist X, dist Y) = (" + distanceX + ", " + distanceY + ")");
            //scrollBy((int)distanceX, (int)distanceY);
            mXDiff += distanceX;

            postInvalidateOnAnimation();
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //fling((int) -velocityX / 3, (int) -velocityY / 3);
            fling((int) -velocityX / 2, (int) -velocityY / 2);
            return true;
        }
    };
    //initialize
    private void init(Context ctx, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        //For onDraw in this layout
        setWillNotDraw(false);

        mDetector = new GestureDetector(ctx, mListener);
        mScroller = new Scroller(ctx);
        mTouchSlop = ViewConfiguration.get(ctx).getScaledTouchSlop();

        TypedArray ta = ctx.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeLineView, defStyleAttr, defStyleRes);
        try {
            mDpPerMinute = ta.getDimension(R.styleable.TimeLineView_dipPerMinute, mDpPerMinute);
            mTimeLabelTextSize = ta.getDimension(R.styleable.TimeLineView_timeLabelTextSize, mTimeLabelTextSize);
            mTimeLabelTextColor = ta.getColor(R.styleable.TimeLineView_timeLabelTextColor, mTimeLabelTextColor);
            mTimeLabelSeparation = ta.getDimensionPixelSize(R.styleable.TimeLineView_timeLabelSeparation, mTimeLabelSeparation);

            mTimeLineThickness = ta.getDimension(R.styleable.TimeLineView_timeLineThickness, mTimeLineThickness);
            mTimeLineColor = ta.getColor(R.styleable.TimeLineView_timeLineColor, mTimeLineColor);

            mHalfTimeLineThickness = ta.getDimension(R.styleable.TimeLineView_halfTimeLineThickness, mHalfTimeLineThickness);
            mHalfTimeLineColor = ta.getColor(R.styleable.TimeLineView_halfTimeLineColor, mHalfTimeLineColor);
        } finally {
            ta.recycle();
        }

        mTimeLabelPaint = new Paint();
        mTimeLabelPaint.setAntiAlias(true);
        mTimeLabelPaint.setTextSize(mTimeLabelTextSize);
        mTimeLabelPaint.setTextAlign(Paint.Align.CENTER);
        mTimeLabelPaint.setColor(mTimeLabelTextColor);
        mTimeLabelHeight = (int)Math.abs(mTimeLabelPaint.getFontMetrics().top);
        mTimeLabelMaxWidth = (int)mTimeLabelPaint.measureText("00"); //time - hour


        mTimeLinePaint = new Paint();
        mTimeLinePaint.setAntiAlias(true);
        mTimeLinePaint.setColor(mTimeLineColor);
        mTimeLinePaint.setStrokeWidth(mTimeLineThickness);
        mTimeLinePaint.setStyle(Paint.Style.STROKE);

        mHalfTimeLinePaint = new Paint();
        mHalfTimeLinePaint.setAntiAlias(true);
        mHalfTimeLinePaint.setColor(mHalfTimeLineColor);
        mHalfTimeLinePaint.setStrokeWidth(mHalfTimeLineThickness);
        mHalfTimeLinePaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams marginLayoutParams = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(marginLayoutParams.leftMargin + marginLayoutParams.rightMargin, MeasureSpec.UNSPECIFIED);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(marginLayoutParams.topMargin + marginLayoutParams.bottomMargin, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public void computeScroll() {
        _log.d("computeScroll");

        if(mScroller.computeScrollOffset()) {
            // This is called at drawing time by ViewGroup. This is used to keep the fling animation through to completion
            _log.d("computeScroll:computeScrollOffset - TRUE");
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();

            //if(getChildCount() > 0) {
            //    View child = getChildAt(0);
            //    //TODO clamp child
            //    x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
            //    y = clamp(y, getHeight() - getPaddingTop() - getPaddingBottom(), child.getHeight());

                if(x != oldX || y != oldY) {
                    //scrollTo(x, y);
                    mXDiff += x;
                    postInvalidateOnAnimation();
                }
            //}
            // Keep on drawing untile the animation has finished
        }
    }

    //// Override scrollTo to do bounds checks on any scrolling request
    //@Override
    //public void scrollTo(int x, int y) {
    //    _log.d("scrollTo: x = " + x);

    //    mXDiff += x;
    //    // TODO: ? : We rely on the fact the View.scrollBy calls scrollTo.
    //    //if(getChildCount() > 0) {
    //    //    View child = getChildAt(0);
    //    //    //TODO: clamp child
    //    //    x = clamp(x, getWidth() - getPaddingRight() - getPaddingLeft(), child.getWidth());
    //    //    y = clamp(y, getHeight() - getPaddingTop() - getPaddingBottom(), child.getHeight());

    //    //    if(x != getScrollX() || y != getScrollY()) {
    //    //        super.scrollTo(x, y);

    //    // Only scroll horizontally!
    //    super.scrollTo(x, 0);
    //    //    }
    //    //}
    //}

    /**
     * Monitor touch events passed down to the children and intercept as soon as it is determined we are dragging
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mMotionInitX = e.getX();
                mMotionInitY = e.getY();
                // Feed the down event to the detector so it has context when/if dragging begins
                mDetector.onTouchEvent(e);
                break;
            case MotionEvent.ACTION_MOVE:
                final float x = e.getX();
                final float y = e.getY();
                final int xDiff = (int)Math.abs(x - mMotionInitX);
                final int yDiff = (int)Math.abs(y - mMotionInitY);
                if(xDiff > mTouchSlop || yDiff > mTouchSlop) {
                    //Start capturing events
                    _log.d("onInterceptTouchEvent: captured");
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    /**
     * Feed all touch events to the detector for processing
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return mDetector.onTouchEvent(e);
    }

    /**
     * Utility method to initialize the scroller and start redrawing
     */
    public void fling(int velocityX, int velocityY) {
        _log.d("fling: (velocityX, velocityY) = (" + velocityX + ", " + velocityY + ")");
        //if(getChildCount() > 0) {
            //int height = getHeight() - getPaddingTop() - getPaddingBottom();
            //int width = getWidth() - getPaddingLeft() - getPaddingRight();
            //int bottom = getChildAt(0).getHeight();
            //int right = getChildAt(0).getWidth();
        mScroller.forceFinished(true);
            mScroller.fling(getScrollX(), getScrollY(),
                    velocityX, velocityY,
                    //0, Math.max(0, right - width),
                    //0, Math.max(0, bottom - height));
                    Integer.MIN_VALUE, Integer.MAX_VALUE,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);

            // redraw
            //invalidate();
        //}
        postInvalidateOnAnimation();
    }

   // /**
   //  * Utility method to assist in doing bound checking
   //  */
   // private int clamp(int n, int my, int child) {
   //     if(my >= child || n < 0) {
   //         // The child is beyond one of the parent bounds
   //         // or is smaller than the parent and can't scroll
   //         return 0;
   //     }

   //     if((my + n) > child) {
   //         // Request scroll is beyond right bound of child
   //         return child -my;
   //     }
   //
   //     return n;
   // }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);

        mCurrentContentRect.set(
                getPaddingLeft(),
                getPaddingTop(),
                getWidth() - getPaddingRight(),
                getHeight() - getPaddingBottom()
        );

        _log.d("onSizeChanged: mCurrentContentRect: " + mCurrentContentRect.toString());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minPlaneSize = 1;
        int w = Math.max(getSuggestedMinimumWidth(), resolveSize(minPlaneSize + getPaddingLeft() + getPaddingRight(), widthMeasureSpec));
        int h = Math.max(getSuggestedMinimumHeight(), resolveSize(minPlaneSize + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));

        _log.d("onMeasure: (w, h) = (" + w + ", " + h + ")");
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // If not initialized, initialize it with current time and center on X
        if(mCurrentTimeAndX == null) {
            //determine time and point initially
            // Also mCurrentTimePoint is floating...it can be outside of the current visible region
            mCurrentTimeAndX = new Tuple2<Long, Integer>(TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis()), mCurrentContentRect.centerX());
            //mCurrentTimePoint = new PointF();
            //mCurrentTimePoint.x = mCurrentContentRect.exactCenterX();
            //mCurrentTimePoint.y = mCurrentContentRect.top;
            //_log.d("onDraw: mCurrentTimePoint = " + mCurrentTimePoint.toString());
            //mCurrentTimeInMinutes = System.currentTimeMillis();
            //_log.d("onDraw: mCurrentTimeInMinutes = " + mCurrentTimeInMinutes);
            //TODO make time lines
        }

        _log.d("onDraw: mCurrentTimeAndX = " + mCurrentTimeAndX.toString());
        _log.d("onDraw: mXDiff = " + mXDiff);

        computeVisibleTimes();

        drawTimeLines(canvas);

        //_log.d("mDpPerMinute = " + mDpPerMinute);

    }

    private void computeVisibleTimes() {
        //// Based on the initial current time and X
        //// Compute time (int minutes) and X by differences from moved X (by scroll)
        //mVisibleTimeAndX.clear();
        //mVisibleTimeAndXMap.clear();

        int minutesDiff = mXDiff / (int)mDpPerMinute;
        long currentCenterTimeInMinutes = mCurrentTimeAndX.t + minutesDiff;

        int centerX = mCurrentContentRect.centerX();
        long timeInMinutesBackward =  currentCenterTimeInMinutes;

        _log.d("centerX = " + centerX);
        _log.d("mCurrentContentRect.left = " + mCurrentContentRect.left);
        _log.d("mCurrentContentRect.right = " + mCurrentContentRect.right);
        _log.d("mCurrentContentRect.top = " + mCurrentContentRect.top);
        _log.d("mCurrentContentRect.bottom = " + mCurrentContentRect.bottom);

        float halfWidth = (float) centerX;
        float reminder = halfWidth % mDpPerMinute;
        _log.d("reminder = " + reminder);
        float minutesLength = (halfWidth - reminder) / mDpPerMinute;
        _log.d("minutesLength = " + minutesLength);

        mVisibleStartMinutesX = new Tuple2<Long, Float>(currentCenterTimeInMinutes - (long)minutesLength, reminder);
        mVisibleEndMinutesX = new Tuple2<Long, Float>(currentCenterTimeInMinutes + (long)minutesLength, mCurrentContentRect.right - reminder);

        long startHalfTimeReminder = mVisibleStartMinutesX.t % 30;
        long startHalfTimeMinutes = mVisibleStartMinutesX.t + (30 - startHalfTimeReminder);
        float startHalfTimeX = mVisibleStartMinutesX.s + (30 - startHalfTimeReminder)*mDpPerMinute;
        mVisibleStartHalfTimeX = new Tuple2<Long, Float>(startHalfTimeMinutes, startHalfTimeX);

        long endHalfTimeReminder = mVisibleEndMinutesX.t % 30;
        long endHalfTimeMinutes = mVisibleEndMinutesX.t - endHalfTimeReminder;
        float endHalfTimeX = mVisibleEndMinutesX.s - endHalfTimeReminder * mDpPerMinute;
        mVisibleEndHalfTimeX = new Tuple2<Long, Float>(endHalfTimeMinutes, endHalfTimeX);


        _log.d("mVisibleStartMinuetsX = " + mVisibleStartMinutesX.toString());
        _log.d("mVisibleEndMinuetsX = " + mVisibleEndMinutesX.toString());
        _log.d("mVisibleStartHalfTimeX = " + mVisibleStartHalfTimeX.toString());
        _log.d("mVisibleEndHalfTimeX = " + mVisibleEndHalfTimeX.toString());
        //for(int x = centerX; x > mCurrentContentRect.left; x -= mDpPerMinute) {
        //    //mVisibleTimeAndX.add(new Tuple2<Long, Integer>(timeInMinutesBackward, x));
        //    //mVisibleTimeAndXMap.put(timeInMinutesBackward--, x);
        ////    timeInMinutesBackward--;
        //}

        //long timeInMinutesForward =  currentCenterTimeInMinutes + 1;
        //for(int x = centerX + (int) mDpPerMinute; x < mCurrentContentRect.right; x += mDpPerMinute) {
        ////    mVisibleTimeAndX.add(new Tuple2<Long, Integer>(timeInMinutesForward, x));
        //    //mVisibleTimeAndXMap.put(timeInMinutesForward++, x);
        ////    timeInMinutesForward++;
        //}

        //for(Map.Entry<Long, Integer> entry: mVisibleTimeAndXMap.entrySet()) {
        //    _log.d("computeVisibleTimes: entry : " + entry.toString());
        //}
    }

    private void drawTimeLines(Canvas canvas) {
        //_log.d("drawTimeLines");

        float startHalfTimeX = mVisibleStartHalfTimeX.s;
        int index = 0;
        for(long minutes = mVisibleStartHalfTimeX.t; minutes <= mVisibleEndHalfTimeX.t; minutes += 30) {
            float x = mVisibleStartHalfTimeX.s + (index++) * mDpPerMinute * 30;
            //_log.d("drawTimeLines: x = " + x);
            if(minutes % 60 == 0) {
                //hour
                canvas.drawLine(x, mCurrentContentRect.top + mTimeLabelSeparation + mTimeLabelHeight, x, mCurrentContentRect.bottom, mTimeLinePaint);
                canvas.drawText(String.valueOf(TimeUnit.MINUTES.toHours(minutes) % 24), x, mCurrentContentRect.top + mTimeLabelHeight, mTimeLabelPaint);
            } else {
                // half hour
                canvas.drawLine(x, mCurrentContentRect.top + mTimeLabelSeparation + mTimeLabelHeight, x, mCurrentContentRect.bottom, mHalfTimeLinePaint);
            }
        }
        //for(Map.Entry<Long, Integer> minutePointEntry : mVisibleTimeAndXMap.entrySet()) {
        //    if(minutePointEntry.getKey() % 30 == 0) {
        //        if (minutePointEntry.getKey() % 60 == 0) {
        //            canvas.drawLine(minutePointEntry.getValue(), mCurrentContentRect.top, minutePointEntry.getValue(), mCurrentContentRect.bottom, mTimeLinePaint);
        //            _log.d("drawTimeLines: time x = " + minutePointEntry.getValue());
        //        } else {
        //            canvas.drawLine(minutePointEntry.getValue(), mCurrentContentRect.top, minutePointEntry.getValue(), mCurrentContentRect.bottom, mHalfTimeLinePaint);
        //            _log.d("drawTimeLines: half time x = " + minutePointEntry.getValue());
        //        }
        //    }
        //}
    }

    /**
     * This needs a unique view ID of this view
     * @return
     */
    @Override
    public Parcelable onSaveInstanceState() {
        _log.d("onSaveInstanceState: mCurrentTimeAndX = " + mCurrentTimeAndX.toString());
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putSerializable("mCurrentTimeAndX", mCurrentTimeAndX);
        bundle.putInt("mXDiff", mXDiff);
        return bundle;
    }

    /**
     * Same requirement of unique view ID, like the onSaveInstanceState
     * @param state
     */
    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if(state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            mCurrentTimeAndX = (Tuple2<Long, Integer>)bundle.getSerializable("mCurrentTimeAndX");
            mXDiff = bundle.getInt("mXDiff");
            _log.d("onRestoreInstanceState: mCurrentTimeAndX = " + mCurrentTimeAndX.toString());
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }
}
