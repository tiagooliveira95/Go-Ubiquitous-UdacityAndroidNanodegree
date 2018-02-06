package com.example.android.sunshine.sync;

import android.content.Context;

import com.example.android.sunshine.R;
import com.google.gson.annotations.SerializedName;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */

public class WeatherRequest {

    private String APPID;
    private String cityName;
    private String countryCode;
    private String lat;
    private String lon;

    public WeatherRequest(Context context){
        APPID = context.getString(R.string.APPID);
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
}
