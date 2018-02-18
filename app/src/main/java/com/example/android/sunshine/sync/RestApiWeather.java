package com.example.android.sunshine.sync;

import com.example.android.sunshine.BuildConfig;
import com.example.android.sunshine.models.ForecastResult;
import com.example.android.sunshine.models.ForecastRequest;
import com.example.android.sunshine.models.GeoRequest;
import com.example.android.sunshine.models.GeoResult;

import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */


public class RestApiWeather {
    private static final String WEATHER_URL = "https://api.darksky.net/";
    private static final String GEO_URL = "https://maps.googleapis.com/maps/api/geocode/";
    private static RestApiWeather INSTANCE;

    private WeatherService weatherService;
    private GeoService geoService;

    private static final String UNITS_PARAM = "units";
    private static final String FORMAT_PARAM = "mode";
    private static final String DAYS_PARAM = "cnt";


    private RestApiWeather() {
        Retrofit.Builder retrofitBuilder = new Retrofit
                .Builder()
                .baseUrl(WEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit.Builder geoRetrofitBuilder = new Retrofit
                .Builder()
                .baseUrl(GEO_URL)
                .addConverterFactory(GsonConverterFactory.create());

        /*
          Interceptor is used in debug only to help track issues
         */
        if (BuildConfig.DEBUG){
            retrofitBuilder.client(
                    new OkHttpClient.Builder().addInterceptor(
                            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                    ).build());

            geoRetrofitBuilder.client(
                    new OkHttpClient.Builder().addInterceptor(
                            new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                    ).build());
        }

        geoService = geoRetrofitBuilder.build().create(GeoService.class);
        weatherService = retrofitBuilder.build().create(WeatherService.class);
    }

    public static RestApiWeather getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RestApiWeather();
        }

        return INSTANCE;
    }


    Call<ForecastResult> getForecast(ForecastRequest weatherRequest) {
        return weatherService.forecast(
                weatherRequest.getAPPID(),
                String.format(Locale.getDefault(),
                        "%s,%s",weatherRequest.getLat(),weatherRequest.getLon()),
                "auto"
        );
    }

    public Call<GeoResult> getGeoData(GeoRequest geoRequest){
        return geoService.geoData(geoRequest.getFormat(),geoRequest.getAddress(),geoRequest.getKEY());
    }

    interface WeatherService {
        @GET("forecast/{key}/{latlong}")
        Call<ForecastResult> forecast(@Path("key") String APIKEY, @Path("latlong") String latLong, @Query("units") String units);
    }

    interface GeoService {
        @GET("{format}")
        Call<GeoResult> geoData(@Path("format") String format, @Query("address") String address, @Query("key") String key);
    }
}