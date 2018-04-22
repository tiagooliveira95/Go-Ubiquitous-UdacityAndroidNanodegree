package com.example.android.sunshine.utilities;

import android.app.Activity;
import android.content.Intent;

import com.example.android.sunshine.sync.LocationService;

public class SunshineLocationUtils {


    public static void autoLocationService(Activity activity, boolean isEnabled){
        Intent intent = new Intent(activity, LocationService.class);

        if(isEnabled){
            activity.startService(intent);
        }else{
            activity.stopService(intent);
        }
    }
}
