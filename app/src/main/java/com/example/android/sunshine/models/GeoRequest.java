package com.example.android.sunshine.models;

import android.content.Context;

import com.example.android.sunshine.R;

import java.util.Locale;

/**
 * Created by tiagooliveira95 on 18/02/18.
 */

public class GeoRequest {
    public static final String FORMAT_JSON = "json";

    private double lat,lng;

    private final String KEY;
    private String format = FORMAT_JSON;

    private String address;

    public GeoRequest(Context context, String address){
        KEY = context.getString(R.string.KEY);
        this.address = address;
    }

    public GeoRequest(Context context, double lat, double lng){
        KEY = context.getString(R.string.KEY);
        this.lat = lat;
        this.lng = lng;
    }

    public String getKEY() {
        return KEY;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
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
