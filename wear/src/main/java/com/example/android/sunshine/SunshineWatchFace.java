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
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
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
import java.util.Locale;
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

    private class Engine extends CanvasWatchFaceService.Engine {
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

        private float sWidth, sHeight;

        private float highTemperatureLabelHeight;
        private float lowTemperatureLableHeight;
        private float hourLabelHeight, minLabelHeight;
        private int hour,min;

        private float cityLabelHeight;

        private float verticalRightGuideLine;
        private float verticalLeftGuideLine;

        private float horizontalCardGuideline1, horizontalCardGuideline2, horizontalCardGuideline3;

        private float cardY;
        private float cardY2;

        private float cardCenterRelativeY;

        private float rightIndicatorCX;
        private float rightIndicatorCY;

        private float rightIndicatorRadius;
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

        private float mWeatherBitmapWidth, mWeatherBitmapHeight;


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

        private Paint cardPaint, cardStrokePaint;
        private Paint lowBitCardPaint;

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

        private String sCityName;

        private String icon;

        private float dp28;
        private float sp12, sp18, sp32;

        //private Bitmap mBackgroundBitmap;
        private Bitmap mWeatherBitmap;

        private boolean mAmbient;
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        private boolean isRound;

        @SuppressWarnings("SuspiciousNameCombination")
        private void setValues(float mCenterX, float mCenterY) {
            verticalRightGuideLine = mCenterX * 1.5f;
            verticalLeftGuideLine = mCenterX * 0.5f;

            Rect bound = new Rect();
            mHourLabelPaint.getTextBounds(String.valueOf(hour),0,1,bound);
            hourLabelHeight = bound.height();

            mMinutePaint.getTextBounds(String.valueOf(min),0,1,bound);
            minLabelHeight = bound.height();

            cardY = mCenterY * 0.9f;
            cardY2 = mCenterY * 1.7f;

            cardCenterRelativeY = (cardY + cardY2) / 2f;

            horizontalCardGuideline1 = (cardY + cardCenterRelativeY) / 2f;
            horizontalCardGuideline2 = cardCenterRelativeY;
            horizontalCardGuideline3 = (cardY2 + cardCenterRelativeY) / 2f;


            rightIndicatorCX = verticalRightGuideLine;
            rightIndicatorCY = cardCenterRelativeY;

            rightIndicatorRadius = dp28;
            rightIndicatorInnerShadowRadius = rightIndicatorRadius - 2f;

            mWeatherIconX = mWeatherBitmapWidth / 2f;
            mWeatherIconY = mWeatherBitmapHeight / 2f;


            if(mAmbient && (mBurnInProtection || mLowBitAmbient) || sCityName.isEmpty()) {
                highTemperatureLabelY = cardCenterRelativeY;
                lowTemperatureLabelY = horizontalCardGuideline3;

            }else {
                highTemperatureLabelY = horizontalCardGuideline2 + (highTemperatureLabelHeight / 2f);
                lowTemperatureLabelY =  horizontalCardGuideline3 + (lowTemperatureLableHeight);
            }

            highTemperatureLabelX = verticalLeftGuideLine - highTemperatureLabelPaint.measureText(sHighFormatted) / 3f;
            lowTemperatureLabelX = highTemperatureLabelX;


            cityTextY = ((highTemperatureLabelY + cardY) / 2f) - (cityLabelHeight * 0.5f);
            cityTextX = highTemperatureLabelX;

            if(isRound) {
                hourLabelY = cardY / 1.8f;
                minLabelY = cardY / 1.15f;
            }else{
                hourLabelY = cardY / 1.8f;
                minLabelY = hourLabelY;
            }
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineWatchFace.this)
                    .build());

            mCalendar = Calendar.getInstance();

            isRound = getResources().getConfiguration().isScreenRound();

            initializeBackground();
            initializeWatchFace();

            updateWeatherData(getBaseContext());


            LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mWeatherReceiver,
                    new IntentFilter("weather_changed"));
        }


        private void initializeBackground() {
            cardPaint = new Paint();
            cardPaint.setColor(Color.WHITE);

            cardStrokePaint = new Paint();
            cardStrokePaint.setColor(Color.WHITE);
            cardStrokePaint.setStrokeWidth(1.2f);
            cardStrokePaint.setAntiAlias(true);
            cardStrokePaint.setStyle(Paint.Style.STROKE);
            cardStrokePaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.BLACK);

            lowBitCardPaint = new Paint();
            lowBitCardPaint.setColor(Color.parseColor("#212121"));

        }

        private void initializeWatchFace() {
            /* Set defaults for colors */
            mPrimaryColor = getResources().getColor(R.color.primaryColor, getTheme());
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


            sp12 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, getResources().getDisplayMetrics());
            sp18 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, getResources().getDisplayMetrics());
            dp28 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28f, getResources().getDisplayMetrics());
            sp32 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 32f, getResources().getDisplayMetrics());



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
            mTemperatureHighAmbientPaint.setTextSize(sp32);
            mTemperatureHighAmbientPaint.setTypeface(typeface);

            mTemperatureLowAmbientPaint = new Paint();
            mTemperatureLowAmbientPaint.setColor(Color.WHITE);
            mTemperatureLowAmbientPaint.setTextSize(sp12);
            mTemperatureLowAmbientPaint.setTypeface(typeface);

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
            mRightIndicatorStroke.setMaskFilter(new BlurMaskFilter(dp28 - 7f, BlurMaskFilter.Blur.INNER));
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
            mHourLabelPaint.setAntiAlias(!mAmbient);
            mMinutePaint.setAntiAlias(!mAmbient);

            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                ColorMatrix ma = new ColorMatrix();
                ma.setSaturation(0);
                mWeatherStatePaint = new Paint();
                mWeatherStatePaint.setAntiAlias(false);
                mWeatherStatePaint.setColorFilter(new ColorMatrixColorFilter(ma));

                highTemperatureLabelPaint = mTemperatureHighActivePaint;
                lowTemperatureLabelPaint = mTemperatureLowActivePaint;

            }
            if (mAmbient) {
                highTemperatureLabelPaint = mTemperatureHighAmbientPaint;
                lowTemperatureLabelPaint = mTemperatureLowAmbientPaint;
            } else {
                mWeatherStatePaint = new Paint();
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
            setValues(mCenterX, mCenterY);
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

            hour = mCalendar.get(Calendar.HOUR);
            min = mCalendar.get(Calendar.MINUTE);

            setValues(mCenterX, mCenterY);
            drawBackground(canvas);
            drawWatchFace(canvas);
        }


        private void drawBackground(Canvas canvas) {
            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);
                canvas.drawRect(0, cardY, sWidth, cardY2, lowBitCardPaint);
            } else if (mAmbient) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawColor(mPrimaryColor);
                canvas.drawRect(0, cardY, sWidth, cardY2, cardStrokePaint);
                canvas.drawRect(0, cardY, sWidth, cardY2, cardPaint);
            }
        }


        private void drawWatchFace(Canvas canvas) {

            /*
             * Draw ticks. Usually you will want to bake this directly into the photo, but in
             * cases where you want to allow users to select their own photos, this dynamically
             * creates them on top of the photo.
             */


            mLowBitAmbient = true;//TODO REMOVE TEST ONLY

            if (!mAmbient && isRound) {
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


                float tickRot = (float) (seconds * Math.PI * 2 / 60);
                float innerX = (float) Math.sin(tickRot) * innerTickRadius;
                float innerY = (float) -Math.cos(tickRot) * innerTickRadius;

                float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                float outerY = (float) -Math.cos(tickRot) * outerTickRadius;

                canvas.drawLine(mCenterX + innerX, mCenterY + innerY,
                        mCenterX + outerX, mCenterY + outerY,
                        seconds >= 0 && seconds < 1 ? mTickThirdSecondaryPaint :
                                (seconds >= 38 && seconds <= 46 || seconds >= 14 && seconds <= 23) ? mTickSecondaryPaint : mTickPrimaryPaint
                );
            }

            if(!mAmbient){
                canvas.drawCircle(rightIndicatorCX, rightIndicatorCY, rightIndicatorRadius, mRightIndicator);
                canvas.drawCircle(rightIndicatorCX, rightIndicatorCY, rightIndicatorInnerShadowRadius, mRightIndicatorStroke);
            }

            if (!mAmbient || mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawBitmap(
                        mWeatherBitmap,
                        rightIndicatorCX - mWeatherIconX,
                        cardCenterRelativeY - mWeatherIconY,
                        mWeatherStatePaint);
            }

            if(!mAmbient && !sCityName.isEmpty())
                canvas.drawText(
                        sCityName,
                        cityTextX,
                        cityTextY,
                        mCityLabelPaint);

            canvas.drawText(
                    sHighFormatted,
                    highTemperatureLabelX,
                    highTemperatureLabelY,
                    highTemperatureLabelPaint
            );


            canvas.drawText(
                    sLowFormatted,
                    lowTemperatureLabelX,
                    lowTemperatureLabelY,
                    lowTemperatureLabelPaint
            );


            String sHour;
            String sMin = String.format(Locale.getDefault(),"%02d",min);


            if(isRound){
                sHour = String.valueOf(hour);
            }else{
                sHour = String.format(Locale.getDefault(),"%02d",hour);
            }



            if(isRound) {
                canvas.drawText(sHour, mCenterX - mHourLabelPaint.measureText(sHour) / 2f, hourLabelY, mHourLabelPaint);
                canvas.drawText(sMin, mCenterX - mMinutePaint.measureText(sMin) / 2f, minLabelY, mMinutePaint);
            }else{
                canvas.drawText(sHour, mCenterX - mHourLabelPaint.measureText(sHour), hourLabelY, mHourLabelPaint);
                canvas.drawText(":",mCenterX,hourLabelY,mHourLabelPaint);
                canvas.drawText(sMin, mCenterX + mHourLabelPaint.measureText(":"), minLabelY, mMinutePaint);
            }

        }


        /*
        private void showGuideLines(Canvas canvas){
            Paint p = new Paint();
            p.setStrokeWidth(1f);
            p.setColor(Color.RED);

            canvas.drawLine(0, horizontalCardGuideline1,sWidth, horizontalCardGuideline1,p);
            canvas.drawLine(0, horizontalCardGuideline2,sWidth, horizontalCardGuideline2,p);
            canvas.drawLine(0, horizontalCardGuideline3,sWidth, horizontalCardGuideline3,p);

            canvas.drawLine(verticalLeftGuideLine,0,verticalLeftGuideLine,sHeight,p);
            canvas.drawLine(verticalRightGuideLine,0,verticalRightGuideLine,sHeight,p);
            canvas.drawLine(mCenterX,0,mCenterX,sHeight,p);
        }*/

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

        private void updateWeatherData(Context context) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            tempHigh = preferences.getInt("high", -0);
            tempLow = preferences.getInt("low", -0);
            icon = preferences.getString("icon", "storm");
            mWeatherBitmap = BitmapFactory.decodeResource(getResources(), getSmallArtResourceIdForWeatherCondition(icon));
            sHighFormatted = SunshineWearUtils.formatTemperature(getBaseContext(), tempHigh);
            sLowFormatted = SunshineWearUtils.formatTemperature(getBaseContext(), tempLow);

            mWeatherBitmapHeight = mWeatherBitmap.getHeight();
            mWeatherBitmapWidth = mWeatherBitmap.getWidth();

            sCityName = preferences.getString("cityName", "");


            Rect bounds = new Rect();
            highTemperatureLabelPaint.getTextBounds(sHighFormatted, 0, 1, bounds);
            highTemperatureLabelHeight = bounds.height();

            lowTemperatureLabelPaint.getTextBounds(sLowFormatted,0,1,bounds);
            lowTemperatureLableHeight = bounds.height();

            if(!sCityName.isEmpty()){
                mCityLabelPaint.getTextBounds(sCityName,0,1,bounds);
                cityLabelHeight = bounds.height();
            }
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

            return R.drawable.ic_fog;
        }


    }
}
