package com.example.android.sunshine.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.android.sunshine.BuildConfig;
import com.example.android.sunshine.Sunshine;
import com.example.android.sunshine.models.WeatherResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */


public class RestApiWeather {
    private static final String URL = "https://api.openweathermap.org/data/2.5/";
    private static RestApiWeather INSTANCE;

    private WeatherService weatherService;

    private RestApiWeather() {
        Retrofit.Builder retrofitBuilder = new Retrofit
                .Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create());

        if (BuildConfig.DEBUG)
            retrofitBuilder.client(
                    new OkHttpClient.Builder().addInterceptor(
                            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                    ).build());

        weatherService = retrofitBuilder.build().create(WeatherService.class);
    }

    public static RestApiWeather getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RestApiWeather();
        }

        return INSTANCE;
    }

    public Call<WeatherResult> getWeather(WeatherRequest weatherRequest) {
        HashMap<String, String> map = new HashMap<>();
        map.put("q", weatherRequest.getCityName());
        map.put("APPID", weatherRequest.getAPPID());

        return weatherService.weather(map);
    }

    interface WeatherService {
        @GET("weather")
        Call<WeatherResult> weather(@QueryMap Map<String, String> options);
    }
}