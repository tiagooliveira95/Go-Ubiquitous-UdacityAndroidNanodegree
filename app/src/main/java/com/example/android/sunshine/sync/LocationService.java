package com.example.android.sunshine.sync;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.models.GeoRequest;
import com.example.android.sunshine.models.GeoResult;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationService extends Service implements LocationListener {

    @Override
    public void onCreate() {
        super.onCreate();

        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 30000, 30000, this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        SunshinePreferences.setLocationDetails(this,latitude,longitude);

        GeoRequest geoRequest = new GeoRequest(this,latitude,longitude);
        RestApiWeather.getInstance().getAddressFromLatLng(geoRequest).enqueue(new Callback<GeoResult>() {
            @Override
            public void onResponse(Call<GeoResult> call, Response<GeoResult> response) {
                GeoResult geoResult = response.body();
                List<GeoResult.Result> results = geoResult.getResults();

                SunshinePreferences.setLocationAddress(
                        LocationService.this,
                        results.get(0).getAddressComponents().get(2).getLongName());

                SunshineSyncUtils.startImmediateSync(LocationService.this);
            }

            @Override
            public void onFailure(Call<GeoResult> call, Throwable t) {

            }
        });

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
