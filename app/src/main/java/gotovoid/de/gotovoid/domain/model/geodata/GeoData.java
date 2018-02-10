package gotovoid.de.gotovoid.domain.model.geodata;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import gotovoid.de.gotovoid.domain.model.units.DistanceUnit;
import gotovoid.de.gotovoid.domain.model.units.UnitValue;

/**
 * Object holding {@link GeoData}.
 * Contains a {@link List} of {@link GeoCoordinate}s and a {@link GeoBounds} object defining
 * their bounds.
 * Provides utilities to draw these {@link GeoCoordinate}s.
 * <p>
 * Created by DJ on 04/01/18.
 */

public class GeoData {
    private static final String TAG = GeoData.class.getSimpleName();

    /**
     * {@link List} of {@link GeoCoordinate}s
     */
    private final List<GeoCoordinate> mCoordinates;
    /**
     * Bounds of the {@link GeoCoordinate}s.
     */
    private final GeoBounds mGeoBounds;
    /**
     * {@link Painter} to draw the {@link GeoCoordinate}s.
     */
    private Painter mPainter;

    /**
     * Constructor taking the {@link GeoCoordinate}s.
     *
     * @param coordinates {@link GeoCoordinate}s to manage
     */
    public GeoData(@NonNull final List<GeoCoordinate> coordinates) {
        mCoordinates = coordinates;
        mGeoBounds = new GeoBounds(coordinates);
    }

    /**
     * Format the given {@link UnitValue} into the {@link UnitValue} fitting the legend format
     * for {@link GeoBounds}.
     *
     * @param distance {@link UnitValue} to be formatted
     * @return formatted {@link UnitValue}
     */
    public static UnitValue<DistanceUnit> formatLegend(final UnitValue distance) {
        UnitValue<DistanceUnit> convertedDistance = DistanceUnit.METERS.convert(distance);
        // Convert to kilometers if appropriate
        if (convertedDistance.getValue() > 1500) {
            convertedDistance = DistanceUnit.KILOMETERS.convert(convertedDistance);
        }
        double value = convertedDistance.getValue();
        Log.d(TAG, "formatLegend: convertedValue: " + convertedDistance);
        int result = 0;
        if (value < 10) {
            result = 1;
        } else if (value <= 20) {
            result = 5;
        } else if (value <= 100) {
            result = 10;
        } else if (value <= 200) {
            result = 50;
        } else if (value <= 1000) {
            result = 100;
        } else {
            result = 500;
        }
        Log.d(TAG, "formatLegend: result: " + result);
        return new UnitValue<>(result, convertedDistance.getUnit());
    }

    /**
     * Returns the {@link Painter} for the {@link GeoData}.
     *
     * @param canvasWidth  width of the canvas to draw on
     * @param canvasHeight height of the canvas to draw on
     * @return the {@link Painter}
     */
    @NonNull
    public Painter getPainter(final float canvasWidth, final float canvasHeight) {
        if (mPainter == null || !mPainter.isValid(canvasWidth, canvasHeight, mGeoBounds)) {
            mPainter = new Painter(canvasWidth, canvasHeight, mGeoBounds);
        }
        return mPainter;
    }

    /**
     * Returns the horizontal distance for the {@link GeoData} as
     * a {@link UnitValue<DistanceUnit>} object.
     *
     * @return the horizontal distance
     */
    @NonNull
    public UnitValue<DistanceUnit> getHorizonalDistance() {
        return mGeoBounds.getLngHaversineDistance();
    }

    /**
     * This class provides the relative {@link Position} of a {@link GeoCoordinate} on a canvas.
     */
    public static class Position {
        private final float mXPos;
        private final float mYPos;

        /**
         * Constructor taking the {@link GeoBounds.RelativePosition}
         * of a {@link GeoCoordinate} in {@link GeoBounds} and translates them
         * to the relative position in a {@link DrawingArea} in pixels.
         *
         * @param bounds   {@link DrawingArea} defining the canvas bounds to draw in
         * @param position {@link GeoBounds.RelativePosition} in {@link GeoBounds}
         */
        protected Position(@NonNull final DrawingArea bounds,
                           @NonNull final GeoBounds.RelativePosition position) {
            mXPos = (float) (bounds.getStartX() + position.getLongitude() * bounds.getPixelWidth());
            // Geo coordinate system works from top left whereas computer graphics from bottom.
            mYPos = bounds.getStartY() +
                    bounds.getPixelHeight() - (float) (position.getLatitude() * bounds.getPixelHeight());
        }

        /**
         * Returns the position on the x axis in pixels.
         *
         * @return x axis position
         */
        public float getXPos() {
            return mXPos;
        }

        /**
         * Returns the position on the y axis in pixels.
         *
         * @return y axis position
         */
        public float getYPos() {
            return mYPos;
        }
    }

    /**
     * This class can compute the relative {@link Position} in pixels
     * for {@link GeoCoordinate}s on the dimensions of a canvas,
     * provided by the width and height of a canvas
     */
    public class Painter {
        private final GeoBounds mGeoBounds;

        private final float mCanvasWidth;
        private final float mCanvasHeight;
        private final DrawingArea mBounds;
        private List<Position> mPoints;

        /**
         * Constructor taking the canvas width and canvas height, as well as the {@link GeoBounds}
         * in oder to compute the {@link DrawingArea} for the {@link GeoCoordinate}s stored
         * in the {@link GeoData}.
         *
         * @param canvasWidth  canvas wdith
         * @param canvasHeight canvas height
         * @param bounds       {@link GeoBounds}
         */
        private Painter(final float canvasWidth,
                        final float canvasHeight,
                        @NonNull final GeoBounds bounds) {
            mGeoBounds = bounds;
            mCanvasWidth = canvasWidth;
            mCanvasHeight = canvasHeight;
            mBounds = new DrawingArea(canvasWidth, canvasHeight, bounds);
        }

        /**
         * Returns true if the {@link Painter} is still valid.
         *
         * @param canvasWidth  width of the canvas
         * @param canvasHeight height of the canvas
         * @param bounds       {@link GeoBounds}
         * @return true if still valid
         */
        private boolean isValid(final float canvasWidth,
                                final float canvasHeight,
                                @NonNull final GeoBounds bounds) {
            if (canvasWidth != mCanvasWidth) {
                return false;
            }
            if (canvasHeight != mCanvasHeight) {
                return false;
            }
            /*
            If the GeoBounds did not change too much we can still use it for computing the relative
            Position of the GeoCoordinates.
             */
            if (!mGeoBounds.isEqualDimension(bounds, 0.00001)) {
                return false;
            }
            return true;
        }

        /**
         * Returns the {@link List} of {@link Position}s in pixel coordinates for the {@link List}
         * of {@link GeoCoordinate}s stored in the {@link GeoData} object.
         *
         * @return the {@link List} of {@link Position}s
         */
        @NonNull
        public List<Position> getPoints() {
            final long timestamp = System.currentTimeMillis();
            if (mPoints == null) {
                mPoints = new ArrayList<>();
                if (mCoordinates != null) {
                    for (GeoCoordinate coordinate : mCoordinates) {
                        mPoints.add(new Position(mBounds,
                                mGeoBounds.getPositionInBounds(coordinate)));
                    }
                }
            }
            Log.d(TAG, "getPoints: time: " + (System.currentTimeMillis() - timestamp));
            return mPoints;
        }

        /**
         * Returns the {@link Position} in pixels for the given {@link GeoCoordinate}.
         *
         * @param coordinate {@link GeoCoordinate} to get {@link}
         * @return the {@link Position}
         */
        @Nullable
        public Position getPoint(final GeoCoordinate coordinate) {
            if (mGeoBounds.isInBounds(coordinate)) {
                return new Position(mBounds, mGeoBounds.getPositionInBounds(coordinate));
            }
            return null;
        }

        /**
         * Returns the length in pixels for the given {@link UnitValue<DistanceUnit>}.
         *
         * @param distance distance as {@link UnitValue<DistanceUnit>}
         * @return distance in pixels
         */
        public float getLength(final UnitValue<DistanceUnit> distance) {
            return mBounds.getPixels(distance);
        }

    }

    /**
     * The drawing area in pixel values.
     * Computes the necessary area depending on the {@link GeoBounds} and the height and width of
     * the available canvas.
     */
    private static class DrawingArea {
        private final float mStartX;
        private final float mStartY;
        private final float mPixelWidth;
        private final float mPixelHeight;
        private final UnitValue<DistanceUnit> mDistanceWidth;
        private final UnitValue<DistanceUnit> mDistanceHeight;

        /**
         * Creates a new {@link DrawingArea} instance using the width and height of the available
         * canvas and the {@link GeoBounds} in order to compute the best layout for the
         * {@link GeoCoordinate}s on the available drawing space.
         *
         * @param width  with of the canvas
         * @param height height of the canvas
         * @param bounds {@link GeoBounds}
         */
        private DrawingArea(final float width,
                            final float height,
                            @NonNull final GeoBounds bounds) {
            mDistanceWidth = bounds.getLngHaversineDistance();
            mDistanceHeight = bounds.getLatHaversineDistance();

            Log.d(TAG, "DrawingArea: distance width: " + mDistanceWidth + ", height: " + mDistanceHeight);
            double coordRatio = mDistanceWidth.getValue() / mDistanceHeight.getValue();
            Log.d(TAG, "DrawingArea: coordRatio: " + coordRatio);

            Log.d(TAG, "DrawingArea: canvas width: " + width + ", height: " + height);
            double canvasRatio = width / height;
            Log.d(TAG, "DrawingArea: canvasRatio: " + canvasRatio);

            Log.d(TAG, "DrawingArea: distance: " + bounds.getLngHaversineDistance() + "m");
            // TODO: fix this, the broader it gets the smaller the width of the draw area
            if (canvasRatio > coordRatio) {
                mPixelHeight = height;
                mPixelWidth = (float) (coordRatio * height);
                mStartX = (width - mPixelWidth) / 2;
                mStartY = 0;
            } else {
                mPixelWidth = width;
                mPixelHeight = (float) (width / coordRatio);
                mStartX = 0;
                mStartY = (height - mPixelHeight) / 2;
            }
        }

        /**
         * Returns the width of the {@link DrawingArea} in pixels.
         *
         * @return width in pixels
         */
        public float getPixelWidth() {
            return mPixelWidth;
        }

        /**
         * Returns the height of the {@link DrawingArea} in pixels
         *
         * @return height in pixels
         */
        public float getPixelHeight() {
            return mPixelHeight;
        }

        /**
         * Returns the addObserver value of the {@link DrawingArea} on the x-axis.
         *
         * @return the addObserver value
         */
        public float getStartX() {
            return mStartX;
        }

        /**
         * Rerturns the addObserver value of the {@link DrawingArea} on the y-axis.
         *
         * @return the addObserver value
         */
        public float getStartY() {
            return mStartY;
        }

        /**
         * Returns the end value of the {@link DrawingArea} on the x-axis.
         *
         * @return the end value
         */
        public float getEndX() {
            return mStartX + mPixelWidth;
        }

        /**
         * Returns the end value of the {@link DrawingArea} on the y-axis.
         *
         * @return the end value
         */
        public float getEndY() {
            return mStartY + mPixelHeight;
        }

        /**
         * Returns the length in Pixels for the given {@link UnitValue<DistanceUnit>}.
         *
         * @param distance the distance as {@link UnitValue<DistanceUnit>}
         * @return the distance in pixels
         */
        public float getPixels(final UnitValue<DistanceUnit> distance) {
            UnitValue<DistanceUnit> meterDistance = DistanceUnit.METERS.convert(distance);
            double pixels = meterDistance.getValue() / mDistanceWidth.getValue()
                    * getPixelWidth();
            return (float) pixels;
        }
    }
}
