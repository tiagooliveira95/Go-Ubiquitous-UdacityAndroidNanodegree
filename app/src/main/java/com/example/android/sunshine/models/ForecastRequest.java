package com.example.android.sunshine.models;

import android.content.Context;

import com.example.android.sunshine.R;
import com.example.android.sunshine.data.SunshinePreferences;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */

public class ForecastRequest {

    private String APPID;

    private int numDays = 14;
    private final String UNITS_SI = "si";
    private final String UNITS_US = "us";
    private String lat,lon;
    private String units;

    /**
     * Load Defaults
     */
    public ForecastRequest(Context context){
        APPID = context.getString(R.string.APPID);
        double[] preferredCoordinates = SunshinePreferences.getLocationCoordinates(context);
        lat = String.valueOf(preferredCoordinates[0]);
        lon = String.valueOf(preferredCoordinates[1]);
        units = SunshinePreferences.isMetric(context) ? UNITS_SI : UNITS_US;
    }



    public String getAPPID() {
        return APPID;
    }

    public String getUnits(){
        return units;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }
}
