package com.example.android.sunshine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.example.wear.R;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
        
        private static final float SECOND_TICK_STROKE_WIDTH = 2f;
        
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

        private float sWidth,sHeight;

        private float highTemperatureLabelHeight;
        private float lowTemperatureLableHeight;
        private float hourLabelHeight;

        private float cityLabelHeight;

        private float verticalRightGuideLine;
        private float verticalLeftGuideLine;

        private float cardY;
        private float cardY2;

        private float cardCenterRelativeY;

        private float rightIndicatorCX;
        private float rightIndicatorCY;

        private float rightIndicatorRadius ;
        private float rightIndicatorInnerShadowRadius;

        private float mWeatherIconX;
        private float mWeatherIconY;


        private float cityTextY;
        private float cityTextX;

        private float highTemperatureLabelY;
        private float highTemperatureLabelX;

        private float lowTemperatureLabelY;
        private float lowTemperatureLabelX;

        private float hourLabelY;
        private float minLabelY;

        private float mWeatherBitmapWidth,mWeatherBitmapHeight;



        /* Colors for all hands (hour, minute, seconds, ticks) based on photo loaded. */
        private int mWatchHandColor;
        private int mWatchHandHighlightColor;
        private int mWatchHandShadowColor;
        private int mPrimaryColor;

        private Paint mHourLabelPaint;
        private Paint mMinutePaint;

        private Paint mTickPrimaryPaint;
        private Paint mTickSecondaryPaint;
        private Paint mTickThirdSecondaryPaint;

        private Paint mCityLabelPaint;

        private Paint mBackgroundPaint, mBackgroundPaintStroke;

        private Paint mRightIndicatorStroke;
        private Paint mRightIndicator;

        private Paint mTemperatureHighActivePaint;
        private Paint mTemperatureLowActivePaint;

        private Paint highTemperatureLabelPaint, lowTemperatureLabelPaint;

        private Paint mTemperatureHighAmbientPaint;
        private Paint mTemperatureLowAmbientPaint;

        private Paint mWeatherStatePaint;

        private Paint mWeatherTextPaint;

        private int tempHigh;
        private int tempLow;

        private String sHighFormatted;
        private String sLowFormatted;

        private String icon;

        private float dp28;
        private float sp12,sp18, sp32;

        //private Bitmap mBackgroundBitmap;
        private Bitmap mWeatherBitmap;

        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        @SuppressWarnings("SuspiciousNameCombination")
        private void setValues(float mCenterX, float mCenterY){
            verticalRightGuideLine =  mCenterX  *  1.5f;
            verticalLeftGuideLine  =  mCenterX  *  0.5f;

            float sHighFormattedWidth = highTemperatureLabelPaint.measureText(sHighFormatted);
            float sLowFormattedWidth  = lowTemperatureLabelPaint.measureText(sLowFormatted);

            float k = highTemperatureLabelPaint.measureText("°");

            cardY =  mCenterY  *  0.9f;
            cardY2 =  mCenterY  *  1.7f;

            cardCenterRelativeY = (cardY + cardY2) / 2f;

            rightIndicatorCX = verticalRightGuideLine;
            rightIndicatorCY = cardCenterRelativeY;

            rightIndicatorRadius = dp28;
            rightIndicatorInnerShadowRadius = rightIndicatorRadius - 2f;

            mWeatherIconX =  mWeatherBitmapWidth  /  2f;
            mWeatherIconY =  mWeatherBitmapHeight /  2f;


            highTemperatureLabelY  =  cardCenterRelativeY     +  (highTemperatureLabelHeight / 2f);
            highTemperatureLabelX  =  verticalLeftGuideLine   -  highTemperatureLabelPaint.measureText(sHighFormatted) / 2f;

            lowTemperatureLabelY   =  (highTemperatureLabelY  +  highTemperatureLabelHeight + cardY2) / 2f - (lowTemperatureLableHeight / 2f) ;
            lowTemperatureLabelX   =  highTemperatureLabelX;// highTemperatureLabelX   +  sHighFormattedWidth - sLowFormattedWidth - k ;

            cityTextY = ((highTemperatureLabelY + cardY) / 2f) - (cityLabelHeight * 0.5f);
            cityTextX =  verticalLeftGuideLine - sHighFormattedWidth / 2f;

            hourLabelY =  (mCenterY *  0.60f + hourLabelHeight) / 2f;
            minLabelY  =   mCenterY *  0.85f / 2f + hourLabelHeight;
        }

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
            mBackgroundPaint.setColor(Color.WHITE);

            mBackgroundPaintStroke = new Paint();
            mBackgroundPaintStroke.setColor(Color.WHITE);
            mBackgroundPaintStroke.setStrokeWidth(1);
            mBackgroundPaintStroke.setAntiAlias(true);
            mBackgroundPaintStroke.setStyle(Paint.Style.STROKE);
            mBackgroundPaintStroke.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.BLACK);
        }

        private void initializeWatchFace() {
            /* Set defaults for colors */
            mPrimaryColor = getResources().getColor(R.color.primaryColor,getTheme());
            mWatchHandColor = Color.GRAY;
            mWatchHandHighlightColor = mPrimaryColor;
            mWatchHandShadowColor = Color.BLACK;


            /* *****************
             ** Seconds Tick  **
             *******************/

            mTickPrimaryPaint = new Paint();
            mTickPrimaryPaint.setColor(Color.WHITE);
            mTickPrimaryPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickPrimaryPaint.setAntiAlias(true);
            mTickPrimaryPaint.setStyle(Paint.Style.STROKE);
            mTickPrimaryPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTickSecondaryPaint = new Paint();
            mTickSecondaryPaint.setColor(mWatchHandColor);
            mTickSecondaryPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickSecondaryPaint.setAntiAlias(true);
            mTickSecondaryPaint.setStyle(Paint.Style.STROKE);
            mTickSecondaryPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);

            mTickThirdSecondaryPaint = new Paint();
            mTickThirdSecondaryPaint.setColor(Color.parseColor("#FFDB19"));
            mTickThirdSecondaryPaint.setStrokeWidth(SECOND_TICK_STROKE_WIDTH);
            mTickThirdSecondaryPaint.setAntiAlias(true);
            mTickThirdSecondaryPaint.setStyle(Paint.Style.STROKE);
            mTickThirdSecondaryPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);


            mWeatherStatePaint = new Paint();


            /* ******************************
             ** Dimensions for text sizes  **
             ********************************/


            sp12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12f,getResources().getDisplayMetrics());
            sp18 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,18f,getResources().getDisplayMetrics());
            dp28 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,28f,getResources().getDisplayMetrics());
            sp32 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,32f,getResources().getDisplayMetrics());



            /* *******************
             ** Labels Typeface **
             *********************/

            Typeface typeface = Typeface.createFromAsset(getBaseContext().getAssets(), "PoiretOne-Regular.ttf");


            /* **********************
             ** Temperature Labels **
             ************************/

            mTemperatureHighActivePaint = new Paint();
            mTemperatureHighActivePaint.setColor(Color.BLACK);
            mTemperatureHighActivePaint.setTypeface(typeface);
            mTemperatureHighActivePaint.setTextSize(sp32);
            mTemperatureHighActivePaint.setAntiAlias(true);

            mTemperatureLowActivePaint = new Paint();
            mTemperatureLowActivePaint.setColor(Color.BLACK);
            mTemperatureLowActivePaint.setTypeface(typeface);
            mTemperatureLowActivePaint.setTextSize(sp12);
            mTemperatureLowActivePaint.setAntiAlias(true);

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

            highTemperatureLabelPaint = mTemperatureHighActivePaint;
            lowTemperatureLabelPaint = mTemperatureLowActivePaint;


            /* **************
             ** City Label **
             ****************/

            mCityLabelPaint = new Paint();
            mCityLabelPaint.setColor(Color.BLACK);
            mCityLabelPaint.setTypeface(typeface);
            mCityLabelPaint.setTextSize(sp12);
            mCityLabelPaint.setAntiAlias(true);

            /* *******************
             ** Hour/Min Labels **
             *********************/

            mHourLabelPaint = new Paint();
            mHourLabelPaint.setColor(Color.WHITE);
            mHourLabelPaint.setTypeface(typeface);
            mHourLabelPaint.setTextSize(sp32);
            mHourLabelPaint.setAntiAlias(true);


            mMinutePaint = new Paint();
            mMinutePaint.setColor(Color.WHITE);
            mMinutePaint.setTypeface(typeface);
            mMinutePaint.setTextSize(sp32);
            mMinutePaint.setAntiAlias(true);

            /* *******************
             ** Hour/Min Labels **
             *********************/

            mWeatherTextPaint = new Paint();
            mWeatherTextPaint.setColor(Color.WHITE);
            mWeatherTextPaint.setAntiAlias(true);
            mWeatherTextPaint.setTypeface(Typeface.SERIF);
            mWeatherTextPaint.setTextSize(sp18);
            mWeatherTextPaint.setStyle(Paint.Style.STROKE);
            mWeatherTextPaint.setStrokeWidth(0.6f);


            /* ***********************************
             ** Right Indicator (Right Circle)  **
             *************************************/

            mRightIndicator = new Paint();
            mRightIndicator.setColor(getResources().getColor(R.color.primaryColor));
            mRightIndicator.setAntiAlias(true);

            mRightIndicatorStroke = new Paint();
            mRightIndicatorStroke.setColor(mWatchHandShadowColor);
            mRightIndicatorStroke.setStrokeWidth(3f);
            mRightIndicatorStroke.setStyle(Paint.Style.STROKE);
            mRightIndicatorStroke.setAntiAlias(true);
            mRightIndicatorStroke.setMaskFilter(new BlurMaskFilter(dp28 -7f, BlurMaskFilter.Blur.INNER));


            /* *********************************************
             ** Calculating font metrics for positioning  **
             ***********************************************/

            Paint.FontMetrics fm = mTemperatureHighAmbientPaint.getFontMetrics();
            highTemperatureLabelHeight = fm.descent - fm.ascent;

            fm = mHourLabelPaint.getFontMetrics();
            hourLabelHeight = fm.descent - fm.ascent;

            fm = mCityLabelPaint.getFontMetrics();
            cityLabelHeight = fm.descent - fm.ascent;

            fm = lowTemperatureLabelPaint.getFontMetrics();
            lowTemperatureLableHeight = fm.descent - fm.ascent;
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
                highTemperatureLabelPaint = mTemperatureHighAmbientPaint;
                lowTemperatureLabelPaint = mTemperatureLowAmbientPaint;
            } else {
                highTemperatureLabelPaint = mTemperatureHighActivePaint;
                lowTemperatureLabelPaint = mTemperatureLowActivePaint;
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
            boolean inMuteMode = (interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE);

            /* Dim display in mute mode. */
            if (mMuteMode != inMuteMode) {
                mMuteMode = inMuteMode;
                //TODO
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
            sWidth = width;
            sHeight = height;
            setValues(mCenterX,mCenterY);
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

            setValues(mCenterX,mCenterY);
            drawBackground(canvas);
            drawWatchFace(canvas);
        }


        private void drawBackground(Canvas canvas) {
            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.WHITE);
              //  canvas.drawRect(0,0,bounds.width(), bounds.height()/2,mBackgroundPaint);
            } else if (mAmbient) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawColor(mPrimaryColor);
                canvas.drawRect(0, cardY, sWidth, cardY2, mBackgroundPaintStroke);
                canvas.drawRect(0, cardY, sWidth, cardY2, mBackgroundPaint);
            }
        }


        private void drawWatchFace(Canvas canvas) {

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
                        mCenterX + outerX, mCenterY + outerY,
                        (tickIndex == 3 || tickIndex == 4 || tickIndex == 8 || tickIndex == 9) ? mTickSecondaryPaint : mTickPrimaryPaint
                );
            }


            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);


            if (!mAmbient) {
                float tickRot = (float) (seconds * Math.PI * 2 / 60);
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;

                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;

                canvas.drawLine(mCenterX + innerX, mCenterY + innerY,
                        mCenterX + outerX, mCenterY + outerY,
                        seconds >= 0 && seconds < 1 ? mTickThirdSecondaryPaint :
                        (seconds >= 38 && seconds <= 46 || seconds >= 14 && seconds <= 23 ) ? mTickSecondaryPaint : mTickPrimaryPaint
                );
            }



            if (!mAmbient) {
                canvas.drawCircle(rightIndicatorCX, rightIndicatorCY, rightIndicatorRadius, mRightIndicator);
                canvas.drawCircle(rightIndicatorCX, rightIndicatorCY,rightIndicatorInnerShadowRadius, mRightIndicatorStroke);


                canvas.drawBitmap(
                        mWeatherBitmap,
                        rightIndicatorCX - mWeatherIconX,
                        cardCenterRelativeY - mWeatherIconY,
                        mWeatherStatePaint);
            }



            canvas.drawText("Los Angeles",
                    cityTextX,
                    cityTextY,
                    mCityLabelPaint);


            canvas.drawText(
                    sHighFormatted,
                    highTemperatureLabelX,
                    highTemperatureLabelY,
                    highTemperatureLabelPaint
            );


            canvas.drawText(sLowFormatted,
                    lowTemperatureLabelX,
                    lowTemperatureLabelY,
                    lowTemperatureLabelPaint
            );



            String min = String.valueOf(mCalendar.get(Calendar.MINUTE));
            String hour = String.valueOf(mCalendar.get(Calendar.HOUR));

            canvas.drawText(hour, mCenterX - (mHourLabelPaint.measureText(hour) / 2f),  hourLabelY , mHourLabelPaint);
            canvas.drawText(min,  mCenterX - (mMinutePaint.measureText(hour) / 2f), minLabelY, mMinutePaint);

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
            mWeatherBitmap = BitmapFactory.decodeResource(getResources(),getSmallArtResourceIdForWeatherCondition(icon));
            sHighFormatted = SunshineWearUtils.formatTemperature(getBaseContext(),tempHigh);
            sLowFormatted = SunshineWearUtils.formatTemperature(getBaseContext(),tempLow);

            mWeatherBitmapHeight = mWeatherBitmap.getHeight();
            mWeatherBitmapWidth = mWeatherBitmap.getWidth();
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
