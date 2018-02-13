package gotovoid.de.gotovoid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

import gotovoid.de.gotovoid.R;
import gotovoid.de.gotovoid.domain.model.geodata.GeoCoordinate;
import gotovoid.de.gotovoid.domain.model.geodata.GeoData;
import gotovoid.de.gotovoid.domain.model.units.DistanceUnit;
import gotovoid.de.gotovoid.domain.model.units.UnitValue;

/**
 * Created by DJ on 05/01/18.
 */

public class GeoCoordinateView extends View implements IAmbientModeHandler {
    private static final String TAG = GeoCoordinateView.class.getSimpleName();

    private final PaintHandler mPaint = new PaintHandler();

    private boolean mIsAmbient = false;

    private GeoData mPainter;
    private Location mCurrentPosition;


    public GeoCoordinateView(final Context context) {
        super(context);
        init();
    }

    public GeoCoordinateView(final Context context,
                             final @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GeoCoordinateView(final Context context,
                             @Nullable final AttributeSet attrs,
                             final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GeoCoordinateView(final Context context,
                             final @Nullable AttributeSet attrs,
                             final int defStyleAttr,
                             final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        mPaint.setPaint(mIsAmbient);
    }

    public void setGeoData(final List<GeoCoordinate> coordinates) {
        Log.d(TAG, "setGeoData() called with: coordinates = [" + coordinates + "]");
        Log.d(TAG, "setGeoData: isAmbient: " + mIsAmbient);
        mPainter = new GeoData(coordinates);
    }

    public void setCurrentPosition(final Location position) {
        Log.d(TAG, "setCurrentPosition() called with: position = [" + position + "]");
        if (mPainter == null) {
            return;
        }
        mCurrentPosition = position;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final long timestamp = System.currentTimeMillis();
        super.onDraw(canvas);

        if (mPainter == null || canvas == null) {
            return;
        }
        GeoData.Painter painter = mPainter.getPainter(canvas.getWidth(), canvas.getHeight());
        drawPath(canvas, painter);
        drawLegend(canvas, painter);
        if (!mIsAmbient) {
            drawCurrentPosition(canvas, painter);
        }
        Log.d(TAG, "onDraw: time: " + (System.currentTimeMillis() - timestamp));
    }

    private void drawCurrentPosition(final Canvas canvas, final GeoData.Painter painter) {
        final long timestamp = System.currentTimeMillis();
        final float pointRadius = getResources().getDimension(R.dimen.track_path_location_radius);
        if (mCurrentPosition == null) {
            return;
        }

        GeoCoordinate position = new GeoCoordinate(mCurrentPosition.getLatitude(),
                mCurrentPosition.getLongitude());
        final GeoData.Position point = painter.getPoint(position);

        if (point != null) {
            canvas.drawCircle(point.getXPos(),
                    point.getYPos(),
                    pointRadius,
                    mPaint.mLocationCenter);
            final float radius = painter.getLength(
                    new UnitValue<DistanceUnit>(mCurrentPosition.getAccuracy(),
                            DistanceUnit.METERS));
            if (radius > pointRadius + 4) {
                canvas.drawCircle(point.getXPos(),
                        point.getYPos(),
                        radius,
                        mPaint.mLocationBackgroundBorder);
                canvas.drawCircle(point.getXPos(),
                        point.getYPos(),
                        radius,
                        mPaint.mLocationBackground);
            }
        }
        Log.d(TAG, "drawCurrentPosition: time: " + (System.currentTimeMillis() - timestamp));
    }

    private void drawPath(@NonNull final Canvas canvas, @NonNull final GeoData.Painter painter) {
        final long timestamp = System.currentTimeMillis();
        List<GeoData.Position> points = painter.getPoints();
        Path path = new Path();
        boolean isStarted = false;
        for (GeoData.Position point : points) {
            if (!isStarted) {
                path.moveTo(point.getXPos(), point.getYPos());
                isStarted = true;
            } else {
                path.lineTo(point.getXPos(), point.getYPos());
            }
        }
        canvas.drawPath(path, mPaint.mPath);
        Log.d(TAG, "drawPath: time: " + (System.currentTimeMillis() - timestamp));
    }

    private void drawLegend(@NonNull final Canvas canvas, final GeoData.Painter painter) {
        final long timestamp = System.currentTimeMillis();
        final float space = 5;
        // Get the horizontal distance of the GeoData and format it according to legend rule.
        final UnitValue<DistanceUnit> distance = mPainter.getHorizonalDistance();
        final UnitValue<DistanceUnit> dimension = GeoData.formatLegend(distance);
        // Compute the pixel width for text and lines.
        final String text = dimension.getShortString();
        final float legendWidth = painter.getLength(dimension);

        final float yPos = canvas.getHeight() - 10;
        final float textWidth = mPaint.mHighLightText.measureText(text);

        PointF end = new PointF(canvas.getWidth() - textWidth - space, yPos);
        PointF start = new PointF(end.x - legendWidth, yPos);

        // Draw the legend addObserver line.
        canvas.drawLine(start.x, start.y, start.x, start.y - 5, mPaint.mHighlight);
        // Draw the legend distance line.
        canvas.drawLine(start.x, start.y, end.x, end.y, mPaint.mHighlight);
        // Draw the legend end line.
        canvas.drawLine(end.x, end.y, end.x, end.y - 5, mPaint.mHighlight);
        // Draw the legend text.
        canvas.drawText(text, end.x + space, end.y, mPaint.mHighLightText);
        Log.d(TAG, "drawLegend: time: " + (System.currentTimeMillis() - timestamp));
    }

    @Override
    public void setIsAmbient(boolean isAmbient) {
        Log.d(TAG, "setIsAmbient() called with: isAmbient = [" + isAmbient + "]");
        mIsAmbient = isAmbient;
        mPaint.setPaint(mIsAmbient);
    }

    @Override
    public void onUpdateAmbient() {
        // Do nothing
    }

    private class PaintHandler {
        private Paint mPath;
        private Paint mHighlight;
        private Paint mHighLightText;
        private Paint mLocationCenter;
        private Paint mLocationBackground;
        private Paint mLocationBackgroundBorder;

        private PaintHandler() {
            Log.d(TAG, "PaintHandler: new paint handler");
            mPath = new Paint();
            mPath.setStyle(Paint.Style.STROKE);
            mPath.setStrokeCap(Paint.Cap.ROUND);

            mHighlight = new Paint();
            mHighlight.setStyle(Paint.Style.STROKE);
            mHighlight.setStrokeCap(Paint.Cap.SQUARE);

            mHighLightText = new Paint();
            mHighLightText.setTextSize(8 * getResources().getDisplayMetrics().density);
            mHighLightText.setStyle(Paint.Style.FILL);

            mLocationCenter = new Paint();
            mLocationCenter.setStyle(Paint.Style.FILL);

            mLocationBackground = new Paint();
            mLocationBackground.setStyle(Paint.Style.FILL);

            mLocationBackgroundBorder = new Paint();
            mLocationBackgroundBorder.setStyle(Paint.Style.STROKE);
        }

        public void setPaint(final boolean isAmbient) {
            if (isAmbient) {
                setAmbientModePaint();
            } else {
                setActivePaint();
            }
        }

        private void setActivePaint() {
            Log.d(TAG, "setActivePaint() called");
            setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.background_default));

            mPath.setColor(ContextCompat.getColor(getContext(),
                    R.color.track_path));
            mPath.setStrokeWidth(getResources()
                    .getDimension(R.dimen.track_path_stroke_width));
            mPath.setAntiAlias(true);

            mHighlight.setColor(ContextCompat.getColor(getContext(),
                    R.color.primary_text));
            mHighlight.setStrokeWidth(getResources()
                    .getDimension(R.dimen.track_legend_stroke_width));
            mHighlight.setAntiAlias(true);

            mHighLightText.setColor(ContextCompat.getColor(getContext(),
                    R.color.primary_text));
            mHighLightText.setAntiAlias(true);

            mLocationCenter.setColor(ContextCompat.getColor(getContext(),
                    R.color.track_location_primary));
            mLocationCenter.setAntiAlias(true);

            mLocationBackground.setColor(ContextCompat.getColor(getContext(),
                    R.color.track_location_background));
            mLocationBackground.setAntiAlias(true);

            mLocationBackgroundBorder.setColor(ContextCompat.getColor(getContext(),
                    R.color.track_location_primary));
            mLocationBackgroundBorder.setAntiAlias(true);
        }

        private void setAmbientModePaint() {
            Log.d(TAG, "setAmbientModePaint() called");
            setBackgroundColor(ContextCompat.getColor(getContext(),
                    R.color.background_default_ambient));

            mPath.setColor(ContextCompat.getColor(getContext(),
                    R.color.track_path_ambient));
            mPath.setStrokeWidth(getResources()
                    .getDimension(R.dimen.track_path_stroke_width_ambient));
            mPath.setAntiAlias(false);

            mHighlight.setColor(ContextCompat.getColor(getContext(),
                    R.color.primary_text));
            mHighlight.setStrokeWidth(getResources()
                    .getDimension(R.dimen.track_legend_stroke_width_ambient));
            mHighlight.setAntiAlias(false);

            mHighLightText.setColor(ContextCompat.getColor(getContext(),
                    R.color.primary_text));
            mHighLightText.setAntiAlias(false);
        }
    }
}
