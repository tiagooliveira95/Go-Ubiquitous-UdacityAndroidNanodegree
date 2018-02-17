package com.example.android.sunshine.models;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by tiagooliveira95 on 06/02/18.
 */

public class ForecastResult implements Parcelable {

    @SerializedName("latitude")
    @Expose
    private double latitude;
    @SerializedName("longitude")
    @Expose
    private double longitude;
    @SerializedName("timezone")
    @Expose
    private String timezone;
    @SerializedName("currently")
    @Expose
    private Currently currently;
    @SerializedName("hourly")
    @Expose
    private Hourly hourly;
    @SerializedName("daily")
    @Expose
    private Daily daily;
    @SerializedName("flags")
    @Expose
    private Flags flags;
    @SerializedName("offset")
    @Expose
    private int offset;
    public final static Parcelable.Creator<ForecastResult> CREATOR = new Creator<ForecastResult>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ForecastResult createFromParcel(Parcel in) {
            return new ForecastResult(in);
        }

        public ForecastResult[] newArray(int size) {
            return (new ForecastResult[size]);
        }

    };

    protected ForecastResult(Parcel in) {
        this.latitude = ((double) in.readValue((double.class.getClassLoader())));
        this.longitude = ((double) in.readValue((double.class.getClassLoader())));
        this.timezone = ((String) in.readValue((String.class.getClassLoader())));
        this.currently = ((Currently) in.readValue((Currently.class.getClassLoader())));
        this.hourly = ((Hourly) in.readValue((Hourly.class.getClassLoader())));
        this.daily = ((Daily) in.readValue((Daily.class.getClassLoader())));
//        this.flags = ((Flags) in.readValue((Flags.class.getClassLoader())));
        this.offset = ((int) in.readValue((long.class.getClassLoader())));
    }

    public ForecastResult() {
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public Currently getCurrently() {
        return currently;
    }

    public Hourly getHourly() {
        return hourly;
    }

    public Daily getDaily() {
        return daily;
    }


    public Flags getFlags() {
        return flags;
    }


    public long getOffset() {
        return offset;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(latitude);
        dest.writeValue(longitude);
        dest.writeValue(timezone);
        dest.writeValue(currently);
        dest.writeValue(hourly);
        dest.writeValue(daily);
        dest.writeValue(flags);
        dest.writeValue(offset);
    }

    public int describeContents() {
        return 0;
    }

    public ContentValues[] getContentValues() {
        ContentValues[] weatherContentValues = new ContentValues[daily.getData().size()];

        long normalizedUtcStartDay = SunshineDateUtils.getNormalizedUtcDateForToday();

        Daily daily = getDaily();


        for (int i = 0; i < daily.getData().size(); i++) {
            Datum_ data = daily.getData().get(i);


            long dateTimeMillis;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            double high;
            double low;

            String weatherIcon;

            dateTimeMillis = normalizedUtcStartDay + SunshineDateUtils.DAY_IN_MILLIS * i;

            pressure = data.getPressure();
            humidity = (int) data.getHumidity();
            windSpeed = data.getWindSpeed();
            windDirection = data.getWindBearing();

            high = data.getTemperatureHigh();
            low = data.getTemperatureLow();

            weatherIcon = data.getIcon();

            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, dateTimeMillis);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ICON, weatherIcon);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_SUMMARY, weatherIcon.replace("-"," ").substring(0, 1).toUpperCase() + weatherIcon.substring(1));

            weatherContentValues[i] = weatherValues;
        }
        return weatherContentValues;
    }


    public static class Currently implements Parcelable {

        @SerializedName("time")
        @Expose
        private long time;
        @SerializedName("summary")
        @Expose
        private String summary;
        @SerializedName("icon")
        @Expose
        private String icon;
        @SerializedName("precipIntensity")
        @Expose
        private double precipIntensity;
        @SerializedName("precipProbability")
        @Expose
        private double precipProbability;
        @SerializedName("precipType")
        @Expose
        private String precipType;
        @SerializedName("temperature")
        @Expose
        private double temperature;
        @SerializedName("apparentTemperature")
        @Expose
        private double apparentTemperature;
        @SerializedName("dewPoint")
        @Expose
        private double dewPoint;
        @SerializedName("humidity")
        @Expose
        private double humidity;
        @SerializedName("pressure")
        @Expose
        private double pressure;
        @SerializedName("windSpeed")
        @Expose
        private double windSpeed;
        @SerializedName("windGust")
        @Expose
        private double windGust;
        @SerializedName("windBearing")
        @Expose
        private long windBearing;
        @SerializedName("cloudCover")
        @Expose
        private double cloudCover;
        @SerializedName("uvIndex")
        @Expose
        private long uvIndex;
        @SerializedName("visibility")
        @Expose
        private double visibility;
        @SerializedName("ozone")
        @Expose
        private double ozone;
        public final static Parcelable.Creator<Currently> CREATOR = new Creator<Currently>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public Currently createFromParcel(Parcel in) {
                return new Currently(in);
            }

            public Currently[] newArray(int size) {
                return (new Currently[size]);
            }

        };

        protected Currently(Parcel in) {
            this.time = ((long) in.readValue((long.class.getClassLoader())));
            this.summary = ((String) in.readValue((String.class.getClassLoader())));
            this.icon = ((String) in.readValue((String.class.getClassLoader())));
            this.precipIntensity = ((double) in.readValue((double.class.getClassLoader())));
            this.precipProbability = ((double) in.readValue((double.class.getClassLoader())));
            this.precipType = ((String) in.readValue((String.class.getClassLoader())));
            this.temperature = ((double) in.readValue((double.class.getClassLoader())));
            this.apparentTemperature = ((double) in.readValue((double.class.getClassLoader())));
            this.dewPoint = ((double) in.readValue((double.class.getClassLoader())));
            this.humidity = ((double) in.readValue((double.class.getClassLoader())));
            this.pressure = ((double) in.readValue((double.class.getClassLoader())));
            this.windSpeed = ((double) in.readValue((long.class.getClassLoader())));
            this.windGust = ((double) in.readValue((double.class.getClassLoader())));
            this.windBearing = ((long) in.readValue((long.class.getClassLoader())));
            this.cloudCover = ((double) in.readValue((double.class.getClassLoader())));
            this.uvIndex = ((long) in.readValue((long.class.getClassLoader())));
            this.visibility = ((double) in.readValue((double.class.getClassLoader())));
            this.ozone = ((double) in.readValue((double.class.getClassLoader())));
        }

        public Currently() {
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public double getPrecipIntensity() {
            return precipIntensity;
        }

        public void setPrecipIntensity(double precipIntensity) {
            this.precipIntensity = precipIntensity;
        }

        public double getPrecipProbability() {
            return precipProbability;
        }

        public void setPrecipProbability(double precipProbability) {
            this.precipProbability = precipProbability;
        }

        public String getPrecipType() {
            return precipType;
        }

        public void setPrecipType(String precipType) {
            this.precipType = precipType;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getApparentTemperature() {
            return apparentTemperature;
        }

        public void setApparentTemperature(double apparentTemperature) {
            this.apparentTemperature = apparentTemperature;
        }

        public double getDewPoint() {
            return dewPoint;
        }

        public void setDewPoint(double dewPoint) {
            this.dewPoint = dewPoint;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(long windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindGust() {
            return windGust;
        }

        public void setWindGust(double windGust) {
            this.windGust = windGust;
        }

        public long getWindBearing() {
            return windBearing;
        }

        public void setWindBearing(long windBearing) {
            this.windBearing = windBearing;
        }

        public double getCloudCover() {
            return cloudCover;
        }

        public void setCloudCover(double cloudCover) {
            this.cloudCover = cloudCover;
        }

        public long getUvIndex() {
            return uvIndex;
        }

        public void setUvIndex(long uvIndex) {
            this.uvIndex = uvIndex;
        }

        public double getVisibility() {
            return visibility;
        }

        public void setVisibility(double visibility) {
            this.visibility = visibility;
        }

        public double getOzone() {
            return ozone;
        }

        public void setOzone(double ozone) {
            this.ozone = ozone;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(time);
            dest.writeValue(summary);
            dest.writeValue(icon);
            dest.writeValue(precipIntensity);
            dest.writeValue(precipProbability);
            dest.writeValue(precipType);
            dest.writeValue(temperature);
            dest.writeValue(apparentTemperature);
            dest.writeValue(dewPoint);
            dest.writeValue(humidity);
            dest.writeValue(pressure);
            dest.writeValue(windSpeed);
            dest.writeValue(windGust);
            dest.writeValue(windBearing);
            dest.writeValue(cloudCover);
            dest.writeValue(uvIndex);
            dest.writeValue(visibility);
            dest.writeValue(ozone);
        }

        public int describeContents() {
            return 0;
        }

    }

    public static class Daily implements Parcelable {

        @SerializedName("summary")
        @Expose
        private String summary;
        @SerializedName("icon")
        @Expose
        private String icon;
        @SerializedName("data")
        @Expose
        private List<Datum_> data = new ArrayList<>();
        public final static Parcelable.Creator<Daily> CREATOR = new Creator<Daily>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public Daily createFromParcel(Parcel in) {
                return new Daily(in);
            }

            public Daily[] newArray(int size) {
                return (new Daily[size]);
            }

        };

        protected Daily(Parcel in) {
            this.summary = ((String) in.readValue((String.class.getClassLoader())));
            this.icon = ((String) in.readValue((String.class.getClassLoader())));
            in.readList(this.data, (Datum_.class.getClassLoader()));
        }

        public Daily() {
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public List<Datum_> getData() {
            return data;
        }

        public void setData(List<Datum_> data) {
            this.data = data;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(summary);
            dest.writeValue(icon);
            dest.writeList(data);
        }

        public int describeContents() {
            return 0;
        }

    }

    public static class Datum implements Parcelable {

        @SerializedName("time")
        @Expose
        private long time;
        @SerializedName("summary")
        @Expose
        private String summary;
        @SerializedName("icon")
        @Expose
        private String icon;
        @SerializedName("precipIntensity")
        @Expose
        private double precipIntensity;
        @SerializedName("precipProbability")
        @Expose
        private double precipProbability;
        @SerializedName("precipType")
        @Expose
        private String precipType;
        @SerializedName("temperature")
        @Expose
        private double temperature;
        @SerializedName("apparentTemperature")
        @Expose
        private double apparentTemperature;
        @SerializedName("dewPoint")
        @Expose
        private double dewPoint;
        @SerializedName("humidity")
        @Expose
        private double humidity;
        @SerializedName("pressure")
        @Expose
        private double pressure;
        @SerializedName("windSpeed")
        @Expose
        private double windSpeed;
        @SerializedName("windGust")
        @Expose
        private double windGust;
        @SerializedName("windBearing")
        @Expose
        private long windBearing;
        @SerializedName("cloudCover")
        @Expose
        private double cloudCover;
        @SerializedName("uvIndex")
        @Expose
        private long uvIndex;
        @SerializedName("visibility")
        @Expose
        private double visibility;
        @SerializedName("ozone")
        @Expose
        private double ozone;
        public final static Parcelable.Creator<Datum> CREATOR = new Creator<Datum>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public Datum createFromParcel(Parcel in) {
                return new Datum(in);
            }

            public Datum[] newArray(int size) {
                return (new Datum[size]);
            }

        };

        protected Datum(Parcel in) {
            this.time = ((long) in.readValue((long.class.getClassLoader())));
            this.summary = ((String) in.readValue((String.class.getClassLoader())));
            this.icon = ((String) in.readValue((String.class.getClassLoader())));
            this.precipIntensity = ((double) in.readValue((double.class.getClassLoader())));
            this.precipProbability = ((double) in.readValue((double.class.getClassLoader())));
            this.precipType = ((String) in.readValue((String.class.getClassLoader())));
            this.temperature = ((double) in.readValue((double.class.getClassLoader())));
            this.apparentTemperature = ((double) in.readValue((double.class.getClassLoader())));
            this.dewPoint = ((double) in.readValue((double.class.getClassLoader())));
            this.humidity = ((double) in.readValue((double.class.getClassLoader())));
            this.pressure = ((double) in.readValue((double.class.getClassLoader())));
            this.windSpeed = ((double) in.readValue((double.class.getClassLoader())));
            this.windGust = ((double) in.readValue((double.class.getClassLoader())));
            this.windBearing = ((long) in.readValue((long.class.getClassLoader())));
            this.cloudCover = ((double) in.readValue((double.class.getClassLoader())));
            this.uvIndex = ((long) in.readValue((long.class.getClassLoader())));
            this.visibility = ((double) in.readValue((double.class.getClassLoader())));
            this.ozone = ((double) in.readValue((double.class.getClassLoader())));
        }

        public Datum() {
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public double getPrecipIntensity() {
            return precipIntensity;
        }

        public void setPrecipIntensity(long precipIntensity) {
            this.precipIntensity = precipIntensity;
        }

        public double getPrecipProbability() {
            return precipProbability;
        }

        public void setPrecipProbability(double precipProbability) {
            this.precipProbability = precipProbability;
        }

        public String getPrecipType() {
            return precipType;
        }

        public void setPrecipType(String precipType) {
            this.precipType = precipType;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public double getApparentTemperature() {
            return apparentTemperature;
        }

        public void setApparentTemperature(double apparentTemperature) {
            this.apparentTemperature = apparentTemperature;
        }

        public double getDewPoint() {
            return dewPoint;
        }

        public void setDewPoint(double dewPoint) {
            this.dewPoint = dewPoint;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindGust() {
            return windGust;
        }

        public void setWindGust(double windGust) {
            this.windGust = windGust;
        }

        public long getWindBearing() {
            return windBearing;
        }

        public void setWindBearing(long windBearing) {
            this.windBearing = windBearing;
        }

        public double getCloudCover() {
            return cloudCover;
        }

        public void setCloudCover(double cloudCover) {
            this.cloudCover = cloudCover;
        }

        public long getUvIndex() {
            return uvIndex;
        }

        public void setUvIndex(long uvIndex) {
            this.uvIndex = uvIndex;
        }

        public double getVisibility() {
            return visibility;
        }

        public void setVisibility(double visibility) {
            this.visibility = visibility;
        }

        public double getOzone() {
            return ozone;
        }

        public void setOzone(double ozone) {
            this.ozone = ozone;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(time);
            dest.writeValue(summary);
            dest.writeValue(icon);
            dest.writeValue(precipIntensity);
            dest.writeValue(precipProbability);
            dest.writeValue(precipType);
            dest.writeValue(temperature);
            dest.writeValue(apparentTemperature);
            dest.writeValue(dewPoint);
            dest.writeValue(humidity);
            dest.writeValue(pressure);
            dest.writeValue(windSpeed);
            dest.writeValue(windGust);
            dest.writeValue(windBearing);
            dest.writeValue(cloudCover);
            dest.writeValue(uvIndex);
            dest.writeValue(visibility);
            dest.writeValue(ozone);
        }

        public int describeContents() {
            return 0;
        }

    }

    public static class Datum_ implements Parcelable {

        @SerializedName("time")
        @Expose
        private long time;
        @SerializedName("summary")
        @Expose
        private String summary;
        @SerializedName("icon")
        @Expose
        private String icon;
        @SerializedName("sunriseTime")
        @Expose
        private long sunriseTime;
        @SerializedName("sunsetTime")
        @Expose
        private long sunsetTime;
        @SerializedName("moonPhase")
        @Expose
        private double moonPhase;
        @SerializedName("precipIntensity")
        @Expose
        private double precipIntensity;
        @SerializedName("precipIntensityMax")
        @Expose
        private double precipIntensityMax;
        @SerializedName("precipIntensityMaxTime")
        @Expose
        private long precipIntensityMaxTime;
        @SerializedName("precipProbability")
        @Expose
        private double precipProbability;
        @SerializedName("temperatureHigh")
        @Expose
        private double temperatureHigh;
        @SerializedName("temperatureHighTime")
        @Expose
        private long temperatureHighTime;
        @SerializedName("temperatureLow")
        @Expose
        private double temperatureLow;
        @SerializedName("temperatureLowTime")
        @Expose
        private long temperatureLowTime;
        @SerializedName("apparentTemperatureHigh")
        @Expose
        private double apparentTemperatureHigh;
        @SerializedName("apparentTemperatureHighTime")
        @Expose
        private long apparentTemperatureHighTime;
        @SerializedName("apparentTemperatureLow")
        @Expose
        private double apparentTemperatureLow;
        @SerializedName("apparentTemperatureLowTime")
        @Expose
        private long apparentTemperatureLowTime;
        @SerializedName("dewPoint")
        @Expose
        private double dewPoint;
        @SerializedName("humidity")
        @Expose
        private double humidity;
        @SerializedName("pressure")
        @Expose
        private double pressure;
        @SerializedName("windSpeed")
        @Expose
        private double windSpeed;
        @SerializedName("windGust")
        @Expose
        private double windGust;
        @SerializedName("windGustTime")
        @Expose
        private long windGustTime;
        @SerializedName("windBearing")
        @Expose
        private long windBearing;
        @SerializedName("cloudCover")
        @Expose
        private double cloudCover;
        @SerializedName("uvIndex")
        @Expose
        private long uvIndex;
        @SerializedName("uvIndexTime")
        @Expose
        private long uvIndexTime;
        @SerializedName("visibility")
        @Expose
        private double visibility;
        @SerializedName("ozone")
        @Expose
        private double ozone;
        @SerializedName("temperatureMin")
        @Expose
        private double temperatureMin;
        @SerializedName("temperatureMinTime")
        @Expose
        private long temperatureMinTime;
        @SerializedName("temperatureMax")
        @Expose
        private double temperatureMax;
        @SerializedName("temperatureMaxTime")
        @Expose
        private long temperatureMaxTime;
        @SerializedName("apparentTemperatureMin")
        @Expose
        private double apparentTemperatureMin;
        @SerializedName("apparentTemperatureMinTime")
        @Expose
        private long apparentTemperatureMinTime;
        @SerializedName("apparentTemperatureMax")
        @Expose
        private double apparentTemperatureMax;
        @SerializedName("apparentTemperatureMaxTime")
        @Expose
        private long apparentTemperatureMaxTime;
        @SerializedName("precipType")
        @Expose
        private String precipType;
        public final static Parcelable.Creator<Datum_> CREATOR = new Creator<Datum_>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public Datum_ createFromParcel(Parcel in) {
                return new Datum_(in);
            }

            public Datum_[] newArray(int size) {
                return (new Datum_[size]);
            }

        };

        protected Datum_(Parcel in) {
            this.time = ((long) in.readValue((long.class.getClassLoader())));
            this.summary = ((String) in.readValue((String.class.getClassLoader())));
            this.icon = ((String) in.readValue((String.class.getClassLoader())));
            this.sunriseTime = ((long) in.readValue((long.class.getClassLoader())));
            this.sunsetTime = ((long) in.readValue((long.class.getClassLoader())));
            this.moonPhase = ((double) in.readValue((double.class.getClassLoader())));
            this.precipIntensity = ((double) in.readValue((double.class.getClassLoader())));
            this.precipIntensityMax = ((double) in.readValue((double.class.getClassLoader())));
            this.precipIntensityMaxTime = ((long) in.readValue((long.class.getClassLoader())));
            this.precipProbability = ((double) in.readValue((double.class.getClassLoader())));
            this.temperatureHigh = ((double) in.readValue((double.class.getClassLoader())));
            this.temperatureHighTime = ((long) in.readValue((long.class.getClassLoader())));
            this.temperatureLow = ((double) in.readValue((double.class.getClassLoader())));
            this.temperatureLowTime = ((long) in.readValue((long.class.getClassLoader())));
            this.apparentTemperatureHigh = ((double) in.readValue((double.class.getClassLoader())));
            this.apparentTemperatureHighTime = ((long) in.readValue((long.class.getClassLoader())));
            this.apparentTemperatureLow = ((double) in.readValue((double.class.getClassLoader())));
            this.apparentTemperatureLowTime = ((long) in.readValue((long.class.getClassLoader())));
            this.dewPoint = ((double) in.readValue((double.class.getClassLoader())));
            this.humidity = ((double) in.readValue((double.class.getClassLoader())));
            this.pressure = ((double) in.readValue((double.class.getClassLoader())));
            this.windSpeed = ((double) in.readValue((double.class.getClassLoader())));
            this.windGust = ((double) in.readValue((double.class.getClassLoader())));
            this.windGustTime = ((long) in.readValue((long.class.getClassLoader())));
            this.windBearing = ((long) in.readValue((long.class.getClassLoader())));
            this.cloudCover = ((double) in.readValue((double.class.getClassLoader())));
            this.uvIndex = ((long) in.readValue((long.class.getClassLoader())));
            this.uvIndexTime = ((long) in.readValue((long.class.getClassLoader())));
            this.visibility = ((double) in.readValue((double.class.getClassLoader())));
            this.ozone = ((double) in.readValue((double.class.getClassLoader())));
            this.temperatureMin = ((double) in.readValue((double.class.getClassLoader())));
            this.temperatureMinTime = ((long) in.readValue((long.class.getClassLoader())));
            this.temperatureMax = ((double) in.readValue((double.class.getClassLoader())));
            this.temperatureMaxTime = ((long) in.readValue((long.class.getClassLoader())));
            this.apparentTemperatureMin = ((double) in.readValue((double.class.getClassLoader())));
            this.apparentTemperatureMinTime = ((long) in.readValue((long.class.getClassLoader())));
            this.apparentTemperatureMax = ((double) in.readValue((double.class.getClassLoader())));
            this.apparentTemperatureMaxTime = ((long) in.readValue((long.class.getClassLoader())));
            this.precipType = ((String) in.readValue((String.class.getClassLoader())));
        }

        public Datum_() {
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public long getSunriseTime() {
            return sunriseTime;
        }

        public void setSunriseTime(long sunriseTime) {
            this.sunriseTime = sunriseTime;
        }

        public long getSunsetTime() {
            return sunsetTime;
        }

        public void setSunsetTime(long sunsetTime) {
            this.sunsetTime = sunsetTime;
        }

        public double getMoonPhase() {
            return moonPhase;
        }

        public void setMoonPhase(double moonPhase) {
            this.moonPhase = moonPhase;
        }

        public double getPrecipIntensity() {
            return precipIntensity;
        }

        public void setPrecipIntensity(double precipIntensity) {
            this.precipIntensity = precipIntensity;
        }

        public double getPrecipIntensityMax() {
            return precipIntensityMax;
        }

        public void setPrecipIntensityMax(double precipIntensityMax) {
            this.precipIntensityMax = precipIntensityMax;
        }

        public long getPrecipIntensityMaxTime() {
            return precipIntensityMaxTime;
        }

        public void setPrecipIntensityMaxTime(long precipIntensityMaxTime) {
            this.precipIntensityMaxTime = precipIntensityMaxTime;
        }

        public double getPrecipProbability() {
            return precipProbability;
        }

        public void setPrecipProbability(double precipProbability) {
            this.precipProbability = precipProbability;
        }

        public double getTemperatureHigh() {
            return temperatureHigh;
        }

        public void setTemperatureHigh(double temperatureHigh) {
            this.temperatureHigh = temperatureHigh;
        }

        public long getTemperatureHighTime() {
            return temperatureHighTime;
        }

        public void setTemperatureHighTime(long temperatureHighTime) {
            this.temperatureHighTime = temperatureHighTime;
        }

        public double getTemperatureLow() {
            return temperatureLow;
        }

        public void setTemperatureLow(double temperatureLow) {
            this.temperatureLow = temperatureLow;
        }

        public long getTemperatureLowTime() {
            return temperatureLowTime;
        }

        public void setTemperatureLowTime(long temperatureLowTime) {
            this.temperatureLowTime = temperatureLowTime;
        }

        public double getApparentTemperatureHigh() {
            return apparentTemperatureHigh;
        }

        public void setApparentTemperatureHigh(double apparentTemperatureHigh) {
            this.apparentTemperatureHigh = apparentTemperatureHigh;
        }

        public long getApparentTemperatureHighTime() {
            return apparentTemperatureHighTime;
        }

        public void setApparentTemperatureHighTime(long apparentTemperatureHighTime) {
            this.apparentTemperatureHighTime = apparentTemperatureHighTime;
        }

        public double getApparentTemperatureLow() {
            return apparentTemperatureLow;
        }

        public void setApparentTemperatureLow(double apparentTemperatureLow) {
            this.apparentTemperatureLow = apparentTemperatureLow;
        }

        public long getApparentTemperatureLowTime() {
            return apparentTemperatureLowTime;
        }

        public void setApparentTemperatureLowTime(long apparentTemperatureLowTime) {
            this.apparentTemperatureLowTime = apparentTemperatureLowTime;
        }

        public double getDewPoint() {
            return dewPoint;
        }

        public void setDewPoint(double dewPoint) {
            this.dewPoint = dewPoint;
        }

        public double getHumidity() {
            return humidity;
        }

        public void setHumidity(double humidity) {
            this.humidity = humidity;
        }

        public double getPressure() {
            return pressure;
        }

        public void setPressure(double pressure) {
            this.pressure = pressure;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public void setWindSpeed(double windSpeed) {
            this.windSpeed = windSpeed;
        }

        public double getWindGust() {
            return windGust;
        }

        public void setWindGust(double windGust) {
            this.windGust = windGust;
        }

        public long getWindGustTime() {
            return windGustTime;
        }

        public void setWindGustTime(long windGustTime) {
            this.windGustTime = windGustTime;
        }

        public long getWindBearing() {
            return windBearing;
        }

        public void setWindBearing(long windBearing) {
            this.windBearing = windBearing;
        }

        public double getCloudCover() {
            return cloudCover;
        }

        public void setCloudCover(double cloudCover) {
            this.cloudCover = cloudCover;
        }

        public long getUvIndex() {
            return uvIndex;
        }

        public void setUvIndex(long uvIndex) {
            this.uvIndex = uvIndex;
        }

        public long getUvIndexTime() {
            return uvIndexTime;
        }

        public void setUvIndexTime(long uvIndexTime) {
            this.uvIndexTime = uvIndexTime;
        }

        public double getVisibility() {
            return visibility;
        }

        public void setVisibility(double visibility) {
            this.visibility = visibility;
        }

        public double getOzone() {
            return ozone;
        }

        public void setOzone(double ozone) {
            this.ozone = ozone;
        }

        public double getTemperatureMin() {
            return temperatureMin;
        }

        public void setTemperatureMin(double temperatureMin) {
            this.temperatureMin = temperatureMin;
        }

        public long getTemperatureMinTime() {
            return temperatureMinTime;
        }

        public void setTemperatureMinTime(long temperatureMinTime) {
            this.temperatureMinTime = temperatureMinTime;
        }

        public double getTemperatureMax() {
            return temperatureMax;
        }

        public void setTemperatureMax(double temperatureMax) {
            this.temperatureMax = temperatureMax;
        }

        public long getTemperatureMaxTime() {
            return temperatureMaxTime;
        }

        public void setTemperatureMaxTime(long temperatureMaxTime) {
            this.temperatureMaxTime = temperatureMaxTime;
        }

        public double getApparentTemperatureMin() {
            return apparentTemperatureMin;
        }

        public void setApparentTemperatureMin(double apparentTemperatureMin) {
            this.apparentTemperatureMin = apparentTemperatureMin;
        }

        public long getApparentTemperatureMinTime() {
            return apparentTemperatureMinTime;
        }

        public void setApparentTemperatureMinTime(long apparentTemperatureMinTime) {
            this.apparentTemperatureMinTime = apparentTemperatureMinTime;
        }

        public double getApparentTemperatureMax() {
            return apparentTemperatureMax;
        }

        public void setApparentTemperatureMax(double apparentTemperatureMax) {
            this.apparentTemperatureMax = apparentTemperatureMax;
        }

        public long getApparentTemperatureMaxTime() {
            return apparentTemperatureMaxTime;
        }

        public void setApparentTemperatureMaxTime(long apparentTemperatureMaxTime) {
            this.apparentTemperatureMaxTime = apparentTemperatureMaxTime;
        }

        public String getPrecipType() {
            return precipType;
        }

        public void setPrecipType(String precipType) {
            this.precipType = precipType;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(time);
            dest.writeValue(summary);
            dest.writeValue(icon);
            dest.writeValue(sunriseTime);
            dest.writeValue(sunsetTime);
            dest.writeValue(moonPhase);
            dest.writeValue(precipIntensity);
            dest.writeValue(precipIntensityMax);
            dest.writeValue(precipIntensityMaxTime);
            dest.writeValue(precipProbability);
            dest.writeValue(temperatureHigh);
            dest.writeValue(temperatureHighTime);
            dest.writeValue(temperatureLow);
            dest.writeValue(temperatureLowTime);
            dest.writeValue(apparentTemperatureHigh);
            dest.writeValue(apparentTemperatureHighTime);
            dest.writeValue(apparentTemperatureLow);
            dest.writeValue(apparentTemperatureLowTime);
            dest.writeValue(dewPoint);
            dest.writeValue(humidity);
            dest.writeValue(pressure);
            dest.writeValue(windSpeed);
            dest.writeValue(windGust);
            dest.writeValue(windGustTime);
            dest.writeValue(windBearing);
            dest.writeValue(cloudCover);
            dest.writeValue(uvIndex);
            dest.writeValue(uvIndexTime);
            dest.writeValue(visibility);
            dest.writeValue(ozone);
            dest.writeValue(temperatureMin);
            dest.writeValue(temperatureMinTime);
            dest.writeValue(temperatureMax);
            dest.writeValue(temperatureMaxTime);
            dest.writeValue(apparentTemperatureMin);
            dest.writeValue(apparentTemperatureMinTime);
            dest.writeValue(apparentTemperatureMax);
            dest.writeValue(apparentTemperatureMaxTime);
            dest.writeValue(precipType);
        }

        public int describeContents() {
            return 0;
        }

    }

    public static class Flags implements Parcelable {

        @SerializedName("sources")
        @Expose
        private List<String> sources = new ArrayList<String>();
        @SerializedName("isd-stations")
        @Expose
        private List<String> isdStations = new ArrayList<String>();
        @SerializedName("units")
        @Expose
        private String units;
        public final static Parcelable.Creator<Flags> CREATOR = new Creator<Flags>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public Flags createFromParcel(Parcel in) {
                return new Flags(in);
            }

            public Flags[] newArray(int size) {
                return (new Flags[size]);
            }

        };

        protected Flags(Parcel in) {
            in.readList(this.sources, (java.lang.String.class.getClassLoader()));
            in.readList(this.isdStations, (java.lang.String.class.getClassLoader()));
            this.units = ((String) in.readValue((String.class.getClassLoader())));
        }

        public Flags() {
        }

        public List<String> getSources() {
            return sources;
        }

        public void setSources(List<String> sources) {
            this.sources = sources;
        }

        public List<String> getIsdStations() {
            return isdStations;
        }

        public void setIsdStations(List<String> isdStations) {
            this.isdStations = isdStations;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeList(sources);
            dest.writeList(isdStations);
            dest.writeValue(units);
        }

        public int describeContents() {
            return 0;
        }

    }

    public static class Hourly implements Parcelable {

        @SerializedName("summary")
        @Expose
        private String summary;
        @SerializedName("icon")
        @Expose
        private String icon;
        @SerializedName("data")
        @Expose
        private List<Datum> data = new ArrayList<>();
        public final static Parcelable.Creator<Hourly> CREATOR = new Creator<Hourly>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public Hourly createFromParcel(Parcel in) {
                return new Hourly(in);
            }

            public Hourly[] newArray(int size) {
                return (new Hourly[size]);
            }

        };

        protected Hourly(Parcel in) {
            this.summary = ((String) in.readValue((String.class.getClassLoader())));
            this.icon = ((String) in.readValue((String.class.getClassLoader())));
            in.readList(this.data, Datum.class.getClassLoader());
        }

        public Hourly() {
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public List<Datum> getData() {
            return data;
        }

        public void setData(List<Datum> data) {
            this.data = data;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(summary);
            dest.writeValue(icon);
            dest.writeList(data);
        }

        public int describeContents() {
            return 0;
        }

    }
}



