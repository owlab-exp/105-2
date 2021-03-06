package com.example.android.calendartest6;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;

import java.util.concurrent.TimeUnit;

/**
 * Created by ernest on 7/19/16.
 */
public class TimeLineViewSimple extends View {
    private static LOG _log = new LOG(TimeLineViewSimple.class);

    private int mTouchSlop;

    private float mNowLineThickness;
    private int mNowLineColor;
    private Paint mNowLinePaint;

    private int mMinuteUnitDp = 1;

    private Rect mContentRect = new Rect();
    private RectF mCurrentViewPort = new RectF();

    private PointF mBaseCenterPoint;// = new PointF();
    private long mNowInMinutes;//

    private float mDownX;
    private float mDownY;

    private GestureDetector mGestureDetector; //= new GestureDetector(getContext(), new GestureListener());
    private OverScroller mScroller;

    public TimeLineViewSimple(Context context) {
        this(context, null);
    }

    public TimeLineViewSimple(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeLineViewSimple(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr);

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

        _log.d("onSizeChanged::mContentRect: " + mContentRect.toString());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int minPlaneSize = getResources().getDimensionPixelSize(R.dimen.min_time_plane_size);
        int w = Math.max(getSuggestedMinimumWidth(), resolveSize(minPlaneSize + getPaddingLeft() + getPaddingRight(), widthMeasureSpec));
        int h = Math.max(getSuggestedMinimumHeight(), resolveSize(minPlaneSize + getPaddingTop() + getPaddingBottom(), heightMeasureSpec));
        //_log.d("(w, h) = (" + w + ", " + h + ")");
        setMeasuredDimension(w, h);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimeLineViewSimple, defStyleAttr, defStyleAttr);
        try {
            mNowLineThickness = a.getDimension(R.styleable.TimeLineViewSimple_nowLineThickness, mNowLineThickness);
            mNowLineColor = a.getColor(R.styleable.TimeLineViewSimple_nowLineColor, mNowLineColor);
        } finally {
            a.recycle();
        }
        //init paints
        mNowLinePaint = new Paint();
        mNowLinePaint.setAntiAlias(true);
        mNowLinePaint.setColor(mNowLineColor);
        mNowLinePaint.setStrokeWidth(mNowLineThickness);
        mNowLinePaint.setStyle(Paint.Style.STROKE);
        mNowLinePaint.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 0));

        mGestureDetector = new GestureDetector(getContext(), new GestureListener());
        mGestureDetector.setIsLongpressEnabled(false);

        mScroller = new OverScroller(getContext());

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBaseCenterPoint == null) {
            //initialize with center of the rect to now
            mBaseCenterPoint = new PointF();
            mBaseCenterPoint.x = mContentRect.exactCenterX();
            mBaseCenterPoint.y = mContentRect.top;

            mNowInMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis());
            //for the starting minutes
            mCurrentViewPort.left = mNowInMinutes - mContentRect.exactCenterX() / mMinuteUnitDp;
            //for the ending minutes
            mCurrentViewPort.right = mNowInMinutes + mContentRect.exactCenterX() / mMinuteUnitDp;
            //top and bottom is not about time here
            mCurrentViewPort.top = mContentRect.top;
            mCurrentViewPort.bottom = mContentRect.bottom;
        }

        //_log.d("mBaseCenterPoint: " + mBaseCenterPoint.toString());
        //_log.d("mBaseCenterPoint.x: " + mBaseCenterPoint.x);
        //_log.d("mContentRect.top: " + mContentRect.top);
        //_log.d("mContentRect.bottom: " + mContentRect.bottom);
        //_log.d("mNowLinePaint: " + mNowLinePaint.toString());
        drawTimeLines(canvas);
        drawNowLine(canvas, mBaseCenterPoint.x);
        //canvas.drawRect(mBaseCenterPoint.x* 1/3, mContentRect.bottom* 1/3, mBaseCenterPoint.x*2/3, mContentRect.bottom * 2/3, mNowLinePaint);

        _log.d("onDraw");
    }

    private void drawTimeLines(Canvas canvas) {

    }

    private void drawNowLine(Canvas canvas, float nowX) {
        _log.d("drawNowLine");
        canvas.drawLine(nowX, mContentRect.top, nowX, mContentRect.bottom, mNowLinePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //_log.d("onTouchEvent: " + event.toString());
        boolean ret =  mGestureDetector.onTouchEvent(event);
        return ret || super.onTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        _log.d("computeScroll");
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            _log.d("onDown: " + e.toString());

            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(TimeLineViewSimple.this);

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distX, float distY) {
            _log.d("onScroll: e1: " + e1.toString());
            _log.d("onScroll: e2: " + e2.toString());
            _log.d("onScroll: distX: " + distX);
            _log.d("onScroll: distY: " + distY);

            //mBaseCenterPoint.x -= distX;
            //mBaseCenterPoint.y -= distY;
            //ViewCompat.postInvalidateOnAnimation(TimeLineView.this);
            scrollBy((int)distX, (int)distY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float veloX, float veloY) {
            _log.d("onFling: e1: " + e1.toString());
            _log.d("onFling: e2: " + e2.toString());
            _log.d("onFling: veloX: " + veloX);
            _log.d("onFling: veloY: " + veloY);
            mScroller.forceFinished(true);
            mScroller.fling((int)e1.getX(), (int)e2.getY(), (int)veloX/2, (int)veloY/2, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            //TODO something with scroller.curry
            ViewCompat.postInvalidateOnAnimation(TimeLineViewSimple.this);


            return true;
        }
    }

    //public static class TimeStopsBuffer {
    //    List<Tuple2<Float, Long>> timeLineList =  new ArrayList<>();
    //}
//

    //If this is a ViewGroup, the following will be valid
    //@Override
    //public boolean onInterceptTouchEvent(MotionEvent e) {
    //    switch(e.getAction()) {
    //        case MotionEvent.ACTION_DOWN:
    //            mDownX = e.getX();
    //            mDownY = e.getY();
    //            mGestureDetector.onTouchEvent(e);
    //            break;
    //        case MotionEvent.ACTION_MOVE:
    //            final float x = e.getX();
    //            final float y = e.getY();
    //            final int xDiff = (int) Math.abs(x - mDownX);
    //            final int yDiff = (int) Math.abs(y - mDownY);
    //            if(xDiff > mTouchSlop || yDiff > mTouchSlop) {
    //                return true;
    //            }
    //            break;
    //    }

    //    return super.onInterceptTouchEvent(e);
    //}
}
