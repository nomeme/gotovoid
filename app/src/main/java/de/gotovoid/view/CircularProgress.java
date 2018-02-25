package de.gotovoid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.wearable.input.RotaryEncoder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import de.gotovoid.R;

/**
 * Created by DJ on 22/12/17.
 */

public class CircularProgress extends View implements IAmbientModeHandler {
    private static final String TAG = CircularProgress.class.getSimpleName();
    private OnProgressChangedListener mOnProgressChangedListener;
    private final float mStrokeWidthUnselected = 2;
    private final float mStrokeWidthSelected = 10;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mTask;
    private Paint mPaint;

    public CircularProgress(final Context context) {
        super(context);
        init();
    }

    public CircularProgress(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgress(final Context context,
                            @Nullable final AttributeSet attrs,
                            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public CircularProgress(final Context context,
                            @Nullable final AttributeSet attrs,
                            final int defStyleAttr,
                            final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(getResources().getColor(R.color.light_grey, null));
        mPaint.setStrokeWidth(mStrokeWidthUnselected);
        mPaint.setAntiAlias(true);
        // Needs to be active for rotary input to be received
        setFocusableInTouchMode(true);
        setOnGenericMotionListener(new OnGenericMotionListener() {
            private boolean mIsActive;

            @Override
            public boolean onGenericMotion(final View view, final MotionEvent motionEvent) {
                Log.d(TAG, "onGenericMotion() called with: view = [" + view
                        + "], motionEvent = [" + motionEvent + "]");
                if (view != CircularProgress.this) {
                    return false;
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_SCROLL
                        && RotaryEncoder.isFromRotaryEncoder(motionEvent)) {
                    float value = -(RotaryEncoder.getRotaryAxisValue(motionEvent)
                            * RotaryEncoder.getScaledScrollFactor(getContext())) / 3;
                    Log.d(TAG, "onGenericMotion: "
                            + motionEvent.getAction()
                            + "axis_value: " +
                            RotaryEncoder.getRotaryAxisValue(motionEvent)
                            + ", "
                            + value);
                    if (mTask != null) {
                        mHandler.removeCallbacks(mTask);
                    }

                    if (!mIsActive) {
                        Log.d(TAG, "onGenericMotion: change stroke width");
                        mPaint.setStrokeWidth(mStrokeWidthSelected);
                        mIsActive = true;
                        invalidate();
                    }
                    mTask = new Runnable() {
                        @Override
                        public void run() {
                            if (mIsActive) {
                                mPaint.setStrokeWidth(mStrokeWidthUnselected);
                                mIsActive = false;
                                invalidate();
                            }
                        }
                    };
                    mHandler.postDelayed(mTask, 500);
                    if (mOnProgressChangedListener != null) {
                        mOnProgressChangedListener.onProgressChanged(value);
                    }
                }
                /*
                if (motionEvent.getAction() == MotionEvent.ACTION_UP
                        || motionEvent.getAction() == MotionEvent.ACTION_CANCEL) {
                    if (mIsActive) {
                        mStrokeWidth = mStrokeWidthUnselected;
                        mIsActive = false;
                        invalidate();
                    }
                    Log.d(TAG, "onGenericMotion: removeObserver");
                }
                */
                return false;
            }
        });
    }

    public void setOnProgressChangedListener(final OnProgressChangedListener listener) {
        mOnProgressChangedListener = listener;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: ");
        final int width = getWidth();
        final int height = getHeight();
        final int radius = width - 10;

        canvas.drawCircle(width / 2f, height / 2f, radius / 2f, mPaint);
    }

    @Override
    public void setIsAmbient(final boolean isAmbient) {
        if (isAmbient) {
            mPaint.setAntiAlias(false);
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
            invalidate();
        } else {
            mPaint.setAntiAlias(true);
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.light_grey));
            invalidate();
        }
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(final float progress);
    }
}
