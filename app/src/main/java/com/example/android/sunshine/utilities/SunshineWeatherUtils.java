/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.example.android.sunshine.R;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.models.ForecastResult;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Contains useful utilities for a weather app, such as conversion between Celsius and Fahrenheit,
 * from kph to mph, and from degrees to NSEW.  It also contains the mapping of weather condition
 * codes in OpenWeatherMap to strings.  These strings are contained
 */
public final class SunshineWeatherUtils {

    private static final String LOG_TAG = SunshineWeatherUtils.class.getSimpleName();

    /**
     * This method will convert a temperature from Celsius to Fahrenheit.
     *
     * @param temperatureInCelsius Temperature in degrees Celsius(°C)
     *
     * @return Temperature in degrees Fahrenheit (°F)
     */
    private static double celsiusToFahrenheit(double temperatureInCelsius) {
        double temperatureInFahrenheit = (temperatureInCelsius * 1.8) + 32;
        return temperatureInFahrenheit;
    }

    /**
     * Temperature data is stored in Celsius by our app. Depending on the user's preference,
     * the app may need to display the temperature in Fahrenheit. This method will perform that
     * temperature conversion if necessary. It will also format the temperature so that no
     * decimal points show. Temperatures will be formatted to the following form: "21°"
     *
     * @param context     Android Context to access preferences and resources
     * @param temperature Temperature in degrees Celsius (°C)
     *
     * @return Formatted temperature String in the following form:
     * "21°"
     */
    public static String formatTemperature(Context context, double temperature) {
        if (!SunshinePreferences.isMetric(context)) {
            temperature = celsiusToFahrenheit(temperature);
        }

        int temperatureFormatResourceId = R.string.format_temperature;

        /* For presentation, assume the user doesn't care about tenths of a degree. */
        return String.format(context.getString(temperatureFormatResourceId), temperature);
    }

    /**
     * This method will format the temperatures to be displayed in the
     * following form: "HIGH° / LOW°"
     *
     * @param context Android Context to access preferences and resources
     * @param high    High temperature for a day in user's preferred units
     * @param low     Low temperature for a day in user's preferred units
     *
     * @return String in the form: "HIGH° / LOW°"
     */
    public static String formatHighLows(Context context, double high, double low) {
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String formattedHigh = formatTemperature(context, roundedHigh);
        String formattedLow = formatTemperature(context, roundedLow);

        String highLowStr = formattedHigh + " / " + formattedLow;
        return highLowStr;
    }

    /**
     * This method uses the wind direction in degrees to determine compass direction as a
     * String. (eg NW) The method will return the wind String in the following form: "2 km/h SW"
     *
     * @param context   Android Context to access preferences and resources
     * @param windSpeed Wind speed in kilometers / hour
     * @param degrees   Degrees as measured on a compass, NOT temperature degrees!
     *                  See https://www.mathsisfun.com/geometry/degrees.html
     *
     * @return Wind String in the following form: "2 km/h SW"
     */
    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
        int windFormat = R.string.format_wind_kmh;

        if (!SunshinePreferences.isMetric(context)) {
            windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        /*
         * You know what's fun? Writing really long if/else statements with tons of possible
         * conditions. Seriously, try it!
         */
        String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
        }

        return String.format(context.getString(windFormat), windSpeed, direction);
    }



    public static int getSmallArtResourceIdForWeatherCondition(String weatherIcon) {

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

        Log.e(LOG_TAG, "Unknown Weather: " + weatherIcon);
        return R.drawable.ic_storm;
    }

    public static String getWeatherDescription(Resources res, String weatherIcon) {
        if (weatherIcon.equals("thunderstorm")) {
            return res.getString(R.string.weather_condition_thunderstorm);
            //} else if (weatherIcon >= 300 && weatherIcon <= 321) {
            //    return R.drawable.ic_light_rain;
        } else if (weatherIcon.equals("rain")) {
            return res.getString(R.string.weather_condition_rain);
        } else if (weatherIcon.equals("snow")) {
            return res.getString(R.string.weather_condition_snow);
        } else if (weatherIcon.equals("fog")) {
            return res.getString(R.string.weather_condition_fog);
        } else if (weatherIcon.equals("clear-day")) {
            return res.getString(R.string.weather_condition_clear);
        } else if (weatherIcon.equals("partly-cloudy-day") || weatherIcon.equals("partly-cloudy-night")) {
            return res.getString(R.string.weather_condition_partly_cloudy);
        } else if (weatherIcon.equals("cloudy")) {
            return res.getString(R.string.weather_condition_cloudy);
        }

        Log.e(LOG_TAG, "Unknown Weather: " + weatherIcon);
        return res.getString(R.string.weather_condition_fog);
    }

    public static int getLargeArtResourceIdForWeatherCondition(String weatherIcon) {
        if (weatherIcon.equals("thunderstorm")) {
            return R.drawable.art_storm;
            //} else if (weatherIcon >= 300 && weatherIcon <= 321) {
            //    return R.drawable.ic_light_rain;
        } else if (weatherIcon.equals("rain")) {
            return R.drawable.art_rain;
        } else if (weatherIcon.equals("snow")) {
            return R.drawable.art_snow;
        } else if (weatherIcon.equals("fog")) {
            return R.drawable.art_fog;
        } else if (weatherIcon.equals("clear-day")) {
            return R.drawable.art_clear;
        } else if (weatherIcon.equals("partly-cloudy-day") || weatherIcon.equals("partly-cloudy-night")) {
            return R.drawable.art_light_clouds;
        } else if (weatherIcon.equals("cloudy")) {
            return R.drawable.art_clouds;
        }

        Log.e(LOG_TAG, "Unknown Weather: " + weatherIcon);
        return R.drawable.art_storm;
    }

    public static void notifyWearUnitsChanged(Context context) {
            PutDataMapRequest dataMap = PutDataMapRequest.create("/weather");
            dataMap.setUrgent();

            dataMap.getDataMap().putBoolean("units", SunshinePreferences.isMetric(context));

            PutDataRequest request = dataMap.asPutDataRequest();
            Wearable.getDataClient(context).putDataItem(request).isSuccessful();
    }
}