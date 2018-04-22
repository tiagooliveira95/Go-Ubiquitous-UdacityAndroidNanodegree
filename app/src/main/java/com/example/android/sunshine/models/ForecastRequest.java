package com.example.android.sunshine.models;

import android.content.Context;

import com.example.android.sunshine.R;
import com.example.android.sunshine.data.SunshinePreferences;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */

public class ForecastRequest {

    private String APPID;

    public static final String UNITS_SI = "si";
    public static final String UNITS_US = "us";
    private String lat,lon;

    /**
     * Load Defaults
     */
    public ForecastRequest(Context context){
        APPID = context.getString(R.string.SKYKEY);
        double[] preferredCoordinates = SunshinePreferences.getLocationCoordinates(context);
        lat = String.valueOf(preferredCoordinates[0]);
        lon = String.valueOf(preferredCoordinates[1]);
    }



    public String getAPPID() {
        return APPID;
    }

    public String getUnits(){
        return UNITS_SI;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }
}
