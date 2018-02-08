package com.example.android.sunshine.sync;

import com.example.android.sunshine.BuildConfig;
import com.example.android.sunshine.models.ForecastResult;
import com.example.android.sunshine.models.ForecastRequest;
import com.example.android.sunshine.models.WeatherResult;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */


public class RestApiWeather {
    private static final String URL = "https://api.darksky.net/";
    private static RestApiWeather INSTANCE;

    private WeatherService weatherService;

    private static final String UNITS_PARAM = "units";
    private static final String FORMAT_PARAM = "mode";
    private static final String DAYS_PARAM = "cnt";


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

    public Call<WeatherResult> getWeather(ForecastRequest weatherRequest) {
        HashMap<String, String> map = new HashMap<>();
        map.put("q", weatherRequest.getCityName());
        map.put("APPID", weatherRequest.getAPPID());
        map.put(DAYS_PARAM, weatherRequest.getNumDays());
        map.put(FORMAT_PARAM, weatherRequest.getFormat());
        map.put(UNITS_PARAM,weatherRequest.getUnits());


        return weatherService.weather(map);
    }

    public Call<ForecastResult> getForecast(ForecastRequest weatherRequest) {
        return weatherService.forecast(weatherRequest.getAPPID(),"40.6837,-8.5975","auto");
    }

    interface WeatherService {
        @GET("weather")
        Call<WeatherResult> weather(@QueryMap Map<String, String> options);

        @GET("forecast/{key}/{latlong}")
        Call<ForecastResult> forecast(@Path("key") String APIKEY, @Path("latlong") String latLong, @Query("units") String units);

    }
}