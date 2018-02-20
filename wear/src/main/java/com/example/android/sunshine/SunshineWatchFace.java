package com.example.android.sunshine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.ComplicationDrawable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.example.wear.R;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class SunshineWatchFace extends CanvasWatchFaceService {

    /*
     * Updates rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<SunshineWatchFace.Engine> mWeakReference;

        public EngineHandler(SunshineWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine{
        private static final String DATA_ITEM_RECEIVED_PATH = "/data-item-received";

        private static final float HOUR_STROKE_WIDTH = 5f;
        private static final float MINUTE_STROKE_WIDTH = 3f;
        private static final float SECOND_TICK_STROKE_WIDTH = 2f;


        private static final float CENTER_GAP_AND_CIRCLE_RADIUS = 4f;

        private static final int SHADOW_RADIUS = 6;
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
        private boolean mMuteMode;
        private float mCenterX;
        private float mCenterY;
        private float mTemperatureHighHeight;

        private float mSecondHandLength;
        private float mMinuteHandLength;
        private float mHourHandLength;

        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private int mWatchHandColor;
        private int mWatchHandHighlightColor;
        private int mWatchHandShadowColor;
        private Paint mHourPaint;
        private Paint mClockPaint;
        private Paint mMinutePaint;
        private Paint mSecondPaint;
        private Paint mSecondAndHighlightPaint;
        private Paint mTickAndCirclePaint;
        private Paint mBackgroundPaint;

        private Paint mLeftIndicator;
        private Paint mRightIndicator;

        private Paint mTemperatureHighPaint;
        private Paint mTemperatureLowPaint;

        private Paint mTemperatureHighAmbientPaint;
        private Paint mTemperatureLowAmbientPaint;

        Paint mWeatherStatePaint;

        Paint mColonPaint;
        private Paint mWeatherTextPaint;

        int tempHigh;
        int tempLow;

        String icon;

        float dp24;

        //private Bitmap mBackgroundBitmap;
        private Bitmap mRainBitmap;
        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)

                    .build());

            mCalendar = Calendar.getInstance();


            initializeBackground();
            initializeWatchFace();

            updateWeatherData(getBaseContext());

            LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mWeatherReceiver,
                    new IntentFilter("weather_changed"));
        }



        private void initializeBackground() {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(getColor(R.color.primaryColor));
        }

        private void initializeWatchFace() {
            /* Set defaults for colors */
            mWatchHandColor = Color.GRAY;
            mWatchHandHighlightColor = getResources().getColor(R.color.primaryColor,getTheme());
            mWatchHandShadowColor = Color.BLACK;

            mHourPaint = new Paint();
            mHourPaint.setColor(mWatchHandColor);
            mHourPaint.setStrokeWidth(HOUR_STROKE_WIDTH);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.ROUND);
            //mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mMinutePaint = new Paint();
            mMinutePaint.setColor(mWatchHandColor);
            mMinutePaint.setStrokeWidth(MINUTE_STROKE_WIDTH);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.ROUND);
           // mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mSecondPaint = new Paint();
            mSecondPaint.setColor(mWatchHandHighlightColor);
            mSecondPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.ROUND);
          //  mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTickAndCirclePaint = new Paint();
            mTickAndCirclePaint.setColor(mWatchHandColor);
            mTickAndCirclePaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickAndCirclePaint.setAntiAlias(true);
            mTickAndCirclePaint.setStyle(Paint.Style.STROKE);
            mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,45f,getResources().getDisplayMetrics());
            mClockPaint = new Paint();
            mClockPaint.setColor(Color.WHITE);
            mClockPaint.setTextSize(textSize);
            mClockPaint.setFakeBoldText(true);
            mClockPaint.setAntiAlias(true);

            mColonPaint = new Paint();
            mColonPaint.setTextSize(textSize);

            mWeatherStatePaint = new Paint();
            float sp12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12f,getResources().getDisplayMetrics());
            float sp18 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,18f,getResources().getDisplayMetrics());
            float sp24 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,24f,getResources().getDisplayMetrics());

            dp24 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,28f,getResources().getDisplayMetrics());


            mTemperatureHighPaint = new Paint();
            mTemperatureHighPaint.setColor(Color.BLACK);
            mTemperatureHighPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            mTemperatureHighPaint.setTextSize(sp18);
            mTemperatureHighPaint.setAntiAlias(true);

            mTemperatureLowPaint = new Paint();
            mTemperatureLowPaint.setColor(Color.BLACK);
            mTemperatureLowPaint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            mTemperatureLowPaint.setTextSize(sp12);
            mTemperatureLowPaint.setAntiAlias(true);



            mTemperatureHighAmbientPaint = new Paint();
            mTemperatureHighAmbientPaint.setColor(Color.WHITE);
            mTemperatureHighAmbientPaint.setTextSize(sp18);
            mTemperatureHighAmbientPaint.setStyle(Paint.Style.STROKE);
            mTemperatureHighAmbientPaint.setStrokeWidth(0.4f);
            mTemperatureHighAmbientPaint.setAntiAlias(true);

            mTemperatureLowAmbientPaint = new Paint();
            mTemperatureLowAmbientPaint.setColor(Color.WHITE);
            mTemperatureLowAmbientPaint.setTextSize(sp12);
            mTemperatureLowAmbientPaint.setStyle(Paint.Style.STROKE);
            mTemperatureLowAmbientPaint.setStrokeWidth(0.6f);
            mTemperatureLowAmbientPaint.setAntiAlias(true);




            mWeatherTextPaint = new Paint();
            mWeatherTextPaint.setColor(Color.WHITE);
            mWeatherTextPaint.setAntiAlias(true);
            mWeatherTextPaint.setTypeface(Typeface.SERIF);
            mWeatherTextPaint.setTextSize(sp18);
            mWeatherTextPaint.setStyle(Paint.Style.STROKE);
            mWeatherTextPaint.setStrokeWidth(0.6f);



            mSecondAndHighlightPaint = new Paint();
            mSecondAndHighlightPaint.setColor(mWatchHandHighlightColor);
            mSecondAndHighlightPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mSecondAndHighlightPaint.setAntiAlias(true);
            mSecondAndHighlightPaint.setStrokeCap(Paint.Cap.ROUND);

            mLeftIndicator = new Paint();
            mLeftIndicator.setColor(Color.GRAY);
            mLeftIndicator.setAlpha(36);



            Paint.FontMetrics fm = mTemperatureHighAmbientPaint.getFontMetrics();
            mTemperatureHighHeight = fm.descent - fm.ascent;
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mWeatherReceiver);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }


        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            mAmbient = inAmbientMode;

            updateWatchHandStyle();


            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void updateWatchHandStyle() {
            if (mAmbient) {
                mHourPaint.setColor(Color.WHITE);
                mMinutePaint.setColor(Color.WHITE);
                mSecondPaint.setColor(Color.WHITE);
                mTickAndCirclePaint.setColor(Color.WHITE);

                mHourPaint.setAntiAlias(false);
                mMinutePaint.setAntiAlias(false);
                mSecondPaint.setAntiAlias(false);
                mTickAndCirclePaint.setAntiAlias(false);

                mHourPaint.clearShadowLayer();
                mMinutePaint.clearShadowLayer();
                mSecondPaint.clearShadowLayer();
                mTickAndCirclePaint.clearShadowLayer();

            } else {
                mHourPaint.setColor(mWatchHandColor);
                mMinutePaint.setColor(mWatchHandColor);
                mSecondPaint.setColor(mWatchHandHighlightColor);
                mTickAndCirclePaint.setColor(mWatchHandColor);

                mHourPaint.setAntiAlias(true);
                mMinutePaint.setAntiAlias(true);
                mSecondPaint.setAntiAlias(true);
                mTickAndCirclePaint.setAntiAlias(true);

               /* mHourPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mMinutePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mSecondPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
                mTickAndCirclePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);*/
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                mHourPaint.setAlpha(inMuteMode ? 100 : 255);
                mMinutePaint.setAlpha(inMuteMode ? 100 : 255);
                mSecondPaint.setAlpha(inMuteMode ? 80 : 255);
                invalidate();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            mCenterX = width / 2f;
            mCenterY = height / 2f;

             /*
             * Calculate lengths of different hands based on watch screen size.
             */
            mSecondHandLength = (float) (mCenterX * 0.875);
            mMinuteHandLength = (float) (mCenterX * 0.75);
            mHourHandLength = (float) (mCenterX * 0.5);
            

            /* Scale loaded background image (more efficient) if surface dimensions change.
            float scale = ((float) width) / (float) mBackgroundBitmap.getWidth();

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int) (mBackgroundBitmap.getWidth() * scale),
                    (int) (mBackgroundBitmap.getHeight() * scale), true);

            /*
             * Create a gray version of the image only if it will look nice on the device in
             * ambient mode. That means we don't want devices that support burn-in
             * protection (slight movements in pixels, not great for images going all the way to
             * edges) and low ambient mode (degrades image quality).
             *
             * Also, if your watch face will know about all images ahead of time (users aren't
             * selecting their own photos for the watch face), it will be more
             * efficient to create a black/white version (png, etc.) and load that when you need it.
             */
            if (!mBurnInProtection && !mLowBitAmbient) {
                initGrayBackgroundBitmap();
            }

        }

        private void initGrayBackgroundBitmap() {
            /*mGrayBackgroundBitmap = Bitmap.createBitmap(
                    mBackgroundBitmap.getWidth(),
                    mBackgroundBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mGrayBackgroundBitmap);
            Paint grayPaint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
            grayPaint.setColorFilter(filter);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, grayPaint);*/
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            drawBackground(canvas, bounds);
            drawWatchFace(canvas,bounds);
        }


        private void drawBackground(Canvas canvas, Rect bounds) {

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.WHITE);
              //  canvas.drawRect(0,0,bounds.width(), bounds.height()/2,mBackgroundPaint);
            } else if (mAmbient) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawColor(Color.WHITE);
                //canvas.drawRect(0,0,bounds.width(), bounds.height()/1.6f,mBackgroundPaint);
            }
        }

        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        private void drawWatchFace(Canvas canvas, Rect bounds) {

            /*
             * Draw ticks. Usually you will want to bake this directly into the photo, but in
             * cases where you want to allow users to select their own photos, this dynamically
             * creates them on top of the photo.
             */
            float innerTickRadius = mCenterX - 10;
            float outerTickRadius = mCenterX;
            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;

                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;

                canvas.drawLine(mCenterX + innerX, mCenterY + innerY,
                        mCenterX + outerX, mCenterY + outerY, mTickAndCirclePaint);
            }

            if (!mAmbient) {
                canvas.drawCircle(mCenterX, mCenterY /2, dp24, mLeftIndicator);
                canvas.drawBitmap(
                        mRainBitmap,
                        mCenterX - mRainBitmap.getWidth() / 2,
                        mCenterY / 2 - mRainBitmap.getHeight() / 2,
                        mWeatherStatePaint);
            }





            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = seconds * 6f;

            final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f;

            final float hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f;
            final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + hourHandOffset;

            /*
             * Save the canvas state before we can begin to rotate it.
             */
            canvas.save();

            canvas.rotate(hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(
                    mCenterX,
                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX,
                    mCenterY - mHourHandLength,
                    mHourPaint);

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
            canvas.drawLine(
                    mCenterX,
                    mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                    mCenterX,
                    mCenterY - mMinuteHandLength,
                    mMinutePaint);

            /*
             * Ensure the "seconds" hand is drawn only when we are in interactive mode.
             * Otherwise, we only update the watch face once a minute.
             */
            if (!mAmbient) {
                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
                canvas.drawLine(
                        mCenterX,
                        mCenterY - CENTER_GAP_AND_CIRCLE_RADIUS,
                        mCenterX,
                        mCenterY - mSecondHandLength,
                        mSecondAndHighlightPaint);
            }
            canvas.drawCircle(
                    mCenterX, mCenterY, CENTER_GAP_AND_CIRCLE_RADIUS, mTickAndCirclePaint);

            /* Restore the canvas' original orientation. */
            canvas.restore();

 /*
            float x = mCenterX- mClockPaint.measureText("00:00")/2 ;

            String hourString;
            hourString = formatTwoDigitNumber(mCalendar.get(Calendar.HOUR_OF_DAY));

            canvas.drawText(hourString, x, mCenterY, mClockPaint);
            x += mClockPaint.measureText(hourString);

            canvas.drawText(":", x, mCenterY, mClockPaint);
            x += mColonPaint.measureText(":");

            // Draw the minutes.
            String minuteString = formatTwoDigitNumber(mCalendar.get(Calendar.MINUTE));

            canvas.drawText(minuteString, x, mCenterY, mClockPaint);

*/




            String high = String.valueOf(tempHigh) + "ºC";
            String low = String.valueOf(tempLow) + "ºC";



            Paint p = mAmbient ? mTemperatureHighAmbientPaint : mTemperatureHighPaint;
            Paint pLow = mAmbient ? mTemperatureLowAmbientPaint : mTemperatureLowPaint;

            float tempY = bounds.height()/1.3f;

            canvas.drawText(
                    high,
                    mCenterX - p.measureText(high)/2,
                    tempY,
                    p
            );

            canvas.drawText(low,
                    mCenterX - pLow.measureText(low)/2,
                    tempY + mTemperatureHighHeight,
                    pLow
            );



        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();
                /* Update time zone in case it changed while we weren't visible. */
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !mAmbient;
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        private BroadcastReceiver mWeatherReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateWeatherData(context);
                Log.d("WatchFace", "Reacived BroadCast");
            }
        };

        private void updateWeatherData(Context context){
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            tempHigh = preferences.getInt("high",-0);
            tempLow = preferences.getInt("low",-0);
            icon = preferences.getString("icon","storm");
            mRainBitmap = BitmapFactory.decodeResource(getResources(),getSmallArtResourceIdForWeatherCondition(icon));
        }


        int getSmallArtResourceIdForWeatherCondition(String weatherIcon) {

        /*
         * Based on weather code data for Open Weather Map.
         */
            if (weatherIcon.equals("thunderstorm")) {
                return R.drawable.ic_storm;
                //} else if (weatherIcon >= 300 && weatherIcon <= 321) {
                //    return R.drawable.ic_light_rain;
            } else if (weatherIcon.equals("rain")) {
                return R.drawable.ic_rain;
            } else if (weatherIcon.equals("snow")) {
                return R.drawable.ic_snow;
            } else if (weatherIcon.equals("fog")) {
                return R.drawable.ic_fog;
            } else if (weatherIcon.equals("clear-day")) {
                return R.drawable.ic_clear;
            } else if (weatherIcon.equals("partly-cloudy-day") || weatherIcon.equals("partly-cloudy-night")) {
                return R.drawable.ic_light_clouds;
            } else if (weatherIcon.equals("cloudy")) {
                return R.drawable.ic_cloudy;
            }

            return R.drawable.ic_storm;
        }


    }
}
