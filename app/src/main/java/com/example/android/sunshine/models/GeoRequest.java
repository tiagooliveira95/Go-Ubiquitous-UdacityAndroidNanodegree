package com.example.android.sunshine.models;

import android.content.Context;

import com.example.android.sunshine.R;

/**
 * Created by tiagooliveira95 on 18/02/18.
 */

public class GeoRequest {
    public static final String FORMAT_JSON = "json";
    public static final String FORMAT_XML = "xml";

    private final String KEY;
    private String format = FORMAT_JSON;

    private String address;

    public GeoRequest(Context context, String address){
        KEY = context.getString(R.string.KEY);
        this.address = address;
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
}
