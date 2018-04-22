package com.example.android.sunshine.models;

import android.content.Context;

import com.example.android.sunshine.R;

import java.util.Locale;

/**
 * Created by tiagooliveira95 on 18/02/18.
 */

public class GeoRequest {
    private static final String FORMAT_JSON = "json";

    private double lat,lng;

    private final String KEY;

    private String address;

    public GeoRequest(Context context, String address){
        KEY = context.getString(R.string.GEOKEY);
        this.address = address;
    }

    public GeoRequest(Context context, double lat, double lng){
        KEY = context.getString(R.string.GEOKEY);
        this.lat = lat;
        this.lng = lng;
    }

    public String getKEY() {
        return KEY;
    }

    public String getFormat() {
        return FORMAT_JSON;
    }

    public String getAddress() {
        return address;
    }


    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getLatLng(){
        return String.format(Locale.getDefault(),
                "%s,%s",getLat(),getLng());
    }
}
