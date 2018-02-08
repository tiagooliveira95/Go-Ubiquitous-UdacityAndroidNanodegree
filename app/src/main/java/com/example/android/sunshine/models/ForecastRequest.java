package com.example.android.sunshine.models;

import android.content.Context;

import com.example.android.sunshine.R;
import com.example.android.sunshine.data.SunshinePreferences;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */

public class ForecastRequest {

    private String APPID;
    private String cityName;
    private String countryCode;
    private String lat;
    private String lon;
    private int numDays = 14;
    private String units = "metric";
    private String format = "json";

    /**
     * Load Defaults
     */
    public ForecastRequest(Context context){
        APPID = context.getString(R.string.APPID);
        double[] preferredCoordinates = SunshinePreferences.getLocationCoordinates(context);
        lat = String.valueOf(preferredCoordinates[0]);
        lon = String.valueOf(preferredCoordinates[1]);
        cityName = SunshinePreferences.getPreferredWeatherLocation(context);
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setLatLong(String lat, String lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public String getCityName() {
        return cityName;
    }

    public String getAPPID() {
        return APPID;
    }

    public void setNumDays(int numDays) {
        this.numDays = numDays;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getUnits(){
        return units;
    }

    public String getNumDays() {
        return String.valueOf(numDays);
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }
}
