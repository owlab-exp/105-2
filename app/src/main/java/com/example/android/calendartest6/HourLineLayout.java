package com.example.android.calendartest6;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ernest on 8/7/16.
 */
public class HourLineLayout extends FrameLayout {
    private static final LOG _log = new LOG(HourLineLayout.class);

    public HourLineLayout(Context context) {
        this(context, null);
    }

    public HourLineLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HourLineLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HourLineLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
    private Tuple2<Long, Float> mVisibleStartHalfHourX;
    private Tuple2<Long, Float> mVisibleEndHalfHourX;

    private float   mDpPerMinute;

    private float   mLabelPadding;

    private Paint   mDayLabelPaint;
    private float   mDayLabelTextSize;
    private int     mDayLabelTextColor;
    private int     mDayLabelHeight;
    private int     mDayLabelMaxWidth;
    private int     mDayLabelSeparation;

    private Paint   mHourLabelPaint;
    private float   mHourLabelTextSize;
    private int     mHourLabelTextColor;
    private int     mHourLabelHeight;
    private int     mHourLabelMaxWidth;
    private int     mHourLabelSeparation;

    private Paint   mHourLinePaint;
    private float   mHourLineThickness;
    private int     mHourLineColor;

    private Paint   mHalfHourLinePaint;
    private float   mHalfHourLineThickness;
    private int     mHalfHourLineColor;

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
            fling((int) -velocityX / 5, (int) -velocityY / 5);
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

        TypedArray ta = ctx.getTheme().obtainStyledAttributes(attrs, R.styleable.HourLineLayout, defStyleAttr, defStyleRes);
        try {
            mDpPerMinute =      ta.getDimension(R.styleable.HourLineLayout_dipPerMinute, mDpPerMinute);

            mLabelPadding =     ta.getDimension(R.styleable.HourLineLayout_labelPadding, mLabelPadding);

            mDayLabelTextSize = ta.getDimension(R.styleable.HourLineLayout_dayLabelTextSize, mDayLabelTextSize);
            mDayLabelTextColor =ta.getColor(R.styleable.HourLineLayout_dayLabelTextColor, mDayLabelTextColor);
            mDayLabelSeparation =   ta.getDimensionPixelSize(R.styleable.HourLineLayout_dayLabelSeparation, mDayLabelSeparation);

            mHourLabelTextSize =    ta.getDimension(R.styleable.HourLineLayout_hourLabelTextSize, mHourLabelTextSize);
            mHourLabelTextColor =   ta.getColor(R.styleable.HourLineLayout_hourLabelTextColor, mHourLabelTextColor);
            mHourLabelSeparation =  ta.getDimensionPixelSize(R.styleable.HourLineLayout_hourLabelSeparation, mHourLabelSeparation);

            mHourLineThickness =    ta.getDimension(R.styleable.HourLineLayout_hourLineThickness, mHourLineThickness);
            mHourLineColor =        ta.getColor(R.styleable.HourLineLayout_hourLineColor, mHourLineColor);

            mHalfHourLineThickness =    ta.getDimension(R.styleable.HourLineLayout_halfHourLineThickness, mHalfHourLineThickness);
            mHalfHourLineColor =        ta.getColor(R.styleable.HourLineLayout_halfHourLineColor, mHalfHourLineColor);
        } finally {
            ta.recycle();
        }

        mDayLabelPaint = new Paint();
        mDayLabelPaint.setAntiAlias(true);
        mDayLabelPaint.setTextSize(mDayLabelTextSize);
        mDayLabelPaint.setTextAlign(Paint.Align.CENTER);
        mDayLabelPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        mDayLabelPaint.setColor(mDayLabelTextColor);
        mDayLabelHeight = (int)Math.abs(mDayLabelPaint.getFontMetrics().top);
        mDayLabelMaxWidth = (int)mDayLabelPaint.measureText("WWW AAA 00, 0000"); //month-day-year

        mHourLabelPaint = new Paint();
        mHourLabelPaint.setAntiAlias(true);
        mHourLabelPaint.setTextSize(mHourLabelTextSize);
        mHourLabelPaint.setTextAlign(Paint.Align.LEFT);
        mHourLabelPaint.setColor(mHourLabelTextColor);
        mHourLabelHeight = (int)Math.abs(mHourLabelPaint.getFontMetrics().top);
        mHourLabelMaxWidth = (int)mHourLabelPaint.measureText("00"); //time - hour


        mHourLinePaint = new Paint();
        mHourLinePaint.setAntiAlias(true);
        mHourLinePaint.setColor(mHourLineColor);
        mHourLinePaint.setStrokeWidth(mHourLineThickness);
        mHourLinePaint.setStyle(Paint.Style.STROKE);

        mHalfHourLinePaint = new Paint();
        mHalfHourLinePaint.setAntiAlias(true);
        mHalfHourLinePaint.setColor(mHalfHourLineColor);
        mHalfHourLinePaint.setStrokeWidth(mHalfHourLineThickness);
        mHalfHourLinePaint.setStyle(Paint.Style.STROKE);
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
        //_log.d("fling: (velocityX, velocityY) = (" + velocityX + ", " + velocityY + ")");
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
        // To draw children
        int count = getChildCount();
        _log.d("onMeasure: child count: " + count);
        // Measurement will ultimately be computing these values.
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        int mLeftWidth = 0;
        int rowCount = 0;

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        final Display display = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point deviceDisplay = new Point();
        display.getSize(deviceDisplay);

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE)
                continue;

            // Measure the child.
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            maxWidth += Math.max(maxWidth, child.getMeasuredWidth());
            mLeftWidth += child.getMeasuredWidth();


            if ((mLeftWidth / deviceDisplay.x) > rowCount) {
                maxHeight += child.getMeasuredHeight();
                rowCount++;
            } else {
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
            }
            childState = combineMeasuredStates(childState, child.getMeasuredState());
        }

        // To draw THIS
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

        long startHalfHourReminder = mVisibleStartMinutesX.t % 30;
        long startHalfHourMinutes = mVisibleStartMinutesX.t + (30 - startHalfHourReminder);
        float startHalfHourX = mVisibleStartMinutesX.s + (30 - startHalfHourReminder)*mDpPerMinute;
        mVisibleStartHalfHourX = new Tuple2<Long, Float>(startHalfHourMinutes, startHalfHourX);

        long endHalfHourReminder = mVisibleEndMinutesX.t % 30;
        long endHalfHourMinutes = mVisibleEndMinutesX.t - endHalfHourReminder;
        float endHalfHourX = mVisibleEndMinutesX.s - endHalfHourReminder * mDpPerMinute;
        mVisibleEndHalfHourX = new Tuple2<Long, Float>(endHalfHourMinutes, endHalfHourX);


        _log.d("mVisibleStartMinuetsX = " + mVisibleStartMinutesX.toString());
        _log.d("mVisibleEndMinuetsX = " + mVisibleEndMinutesX.toString());
        _log.d("mVisibleStartHalfHourX = " + mVisibleStartHalfHourX.toString());
        _log.d("mVisibleEndHalfHourX = " + mVisibleEndHalfHourX.toString());
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

        float startHalfHourX = mVisibleStartHalfHourX.s;
        int index = 0;

        int dayLabelOffsetY = mCurrentContentRect.top + (int)Math.ceil(mLabelPadding) + mDayLabelHeight;
        int dayLineStartY = mCurrentContentRect.top;
        //int timeLabelOffsetY = mCurrentContentRect.top + mHourLabelHeight;
        int hourLabelOffsetY = dayLabelOffsetY + (int)Math.ceil(mHourLineThickness) + (int)Math.ceil(mLabelPadding) + mHourLabelHeight;
        //int timeLineStartY = mCurrentContentRect.top + mHourLabelSeparation + mHourLabelHeight;
        int hourLineStartY = dayLabelOffsetY + (int)Math.ceil(mLabelPadding) + (int)Math.ceil(mHourLineThickness);
        int halfHourLineStartY = hourLabelOffsetY + (int)Math.ceil(mLabelPadding) + (int)Math.ceil(mHourLineThickness);

        // Draw horizontal lines
        canvas.drawLine(mCurrentContentRect.left, hourLineStartY, mCurrentContentRect.right, hourLineStartY, mHourLinePaint);
        canvas.drawLine(mCurrentContentRect.left, halfHourLineStartY, mCurrentContentRect.right, halfHourLineStartY, mHourLinePaint);

        Calendar cal = Calendar.getInstance();
        //DateFormat df = DateFormat.getDateInstance();
        DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
        String[] weekdays = new DateFormatSymbols().getShortWeekdays();

        List<Tuple2<Float, Float>> dayLabelXOffsets = new ArrayList<>();
        //dayLabelXStarts.add(mCurrentContentRect.left);

        List<String> dayLabelTexts = new ArrayList<>();
        long startMillis = TimeUnit.MINUTES.toMillis(mVisibleStartHalfHourX.t); // this is not really the starting minutes
        cal.setTimeInMillis(startMillis);
        String startWeekDayShort = weekdays[cal.get(Calendar.DAY_OF_WEEK)];
        String startDayStr = startWeekDayShort + " " + df.format(cal.getTime());
        dayLabelTexts.add(startDayStr);

        float dayLabelXStart = (float)mCurrentContentRect.left;

        for(long minutes = mVisibleStartHalfHourX.t; minutes <= mVisibleEndHalfHourX.t; minutes += 30) {
            float x = mVisibleStartHalfHourX.s + (index++) * mDpPerMinute * 30;
            //_log.d("drawTimeLines: x = " + x);
            if(minutes % 60 == 0) {
                //hour
                long hourOfDay = TimeUnit.MINUTES.toHours(minutes) % 24;
                if(hourOfDay == 0) {
                    long millis = TimeUnit.MINUTES.toMillis(minutes);
                    cal.setTimeInMillis(millis);
                    String weekDayShort = weekdays[cal.get(Calendar.DAY_OF_WEEK)];
                    String dayStr = weekDayShort + " " + df.format(cal.getTime());

                    //Day text and starting position
                    //previous center
                    _log.d("intermediate slice: " + dayLabelXStart + ", " + x);
                    dayLabelXOffsets.add(new Tuple2<Float, Float>(dayLabelXStart, x));
                    dayLabelXStart = x;
                    //next
                    dayLabelTexts.add(dayStr);
                    //int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    //canvas.drawText(String.valueOf(dayOfMonth), x, dayLabelOffsetY, mDayLabelPaint);

                    // day line vertical
                    canvas.drawLine(x, dayLineStartY, x, mCurrentContentRect.bottom, mHourLinePaint);
                    //canvas.drawText(dayStr, x + mLabelPadding, dayLabelOffsetY, mDayLabelPaint);

                    // draw text hour 0
                    canvas.drawText(String.valueOf(hourOfDay), x + mLabelPadding, hourLabelOffsetY, mHourLabelPaint);
                } else {
                    // hour line vertical
                    canvas.drawLine(x, hourLineStartY, x, mCurrentContentRect.bottom, mHourLinePaint);
                    canvas.drawText(String.valueOf(hourOfDay), x + mLabelPadding, hourLabelOffsetY, mHourLabelPaint);
                }
            } else {
                // half hour line vertical
                canvas.drawLine(x, halfHourLineStartY, x, mCurrentContentRect.bottom, mHalfHourLinePaint);
            }
        }
        dayLabelXOffsets.add(new Tuple2<Float, Float>(dayLabelXStart, (float)mCurrentContentRect.right));
        _log.d("last slice: " + dayLabelXStart + ", " + mCurrentContentRect.right);

        // if the label place width is greater than label max width, locate the label on the center of the place,
        // if the label place width is smaller than the label max width,
        // check if the starting point is zero, or if the ending point is right end of the current rect ...
        //
        for(int i = 0; i < dayLabelTexts.size(); i++) {
            String dayLabelText = dayLabelTexts.get(i);
            float dayLabelOffSetStartX = dayLabelXOffsets.get(i).t;
            float dayLabelOffSetEndX = dayLabelXOffsets.get(i).s;
            float labelPlaceWidth = dayLabelOffSetEndX - dayLabelOffSetStartX;
            if(labelPlaceWidth >= mDayLabelMaxWidth) {
                _log.d("slice center: " + (dayLabelOffSetEndX - dayLabelOffSetStartX)/2);
                canvas.drawText(dayLabelText, (dayLabelOffSetEndX + dayLabelOffSetStartX)/2, dayLabelOffsetY, mDayLabelPaint);
            }
            if(labelPlaceWidth < mDayLabelMaxWidth) {
                if(dayLabelOffSetStartX == (int) mCurrentContentRect.left) {
                    canvas.drawText(dayLabelText, dayLabelOffSetEndX - mDayLabelMaxWidth/2, dayLabelOffsetY, mDayLabelPaint);
                }
                if(dayLabelOffSetEndX == (int) mCurrentContentRect.right) {
                    canvas.drawText(dayLabelText, dayLabelOffSetStartX + mDayLabelMaxWidth/2, dayLabelOffsetY, mDayLabelPaint);
                }
            }
        }
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

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        _log.d("onLayout: (" + changed + ", " + left + ", " + top + ", " + right + ", " + bottom + ")");
    }
}
