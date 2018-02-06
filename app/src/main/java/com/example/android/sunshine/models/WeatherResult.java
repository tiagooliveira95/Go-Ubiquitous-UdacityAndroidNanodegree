package com.example.android.sunshine.models;

/**
 * Created by tiagooliveira95 on 05/02/18.
 */



import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WeatherResult {

    @SerializedName("coord")
    @Expose
    private Coord coord;
    @SerializedName("weather")
    @Expose
    private List<Weather> weather = null;
    @SerializedName("base")
    @Expose
    private String base;
    @SerializedName("main")
    @Expose
    private Main main;
    @SerializedName("wind")
    @Expose
    private Wind wind;
    @SerializedName("clouds")
    @Expose
    private Clouds clouds;
    @SerializedName("rain")
    @Expose
    private Rain rain;
    @SerializedName("dt")
    @Expose
    private Integer dt;
    @SerializedName("sys")
    @Expose
    private Sys sys;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("cod")
    @Expose
    private Integer cod;

    public Coord getCoord() {
        return coord;
    }

    public void setCoord(Coord coord) {
        this.coord = coord;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Clouds getClouds() {
        return clouds;
    }

    public void setClouds(Clouds clouds) {
        this.clouds = clouds;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCod() {
        return cod;
    }

    public void setCod(Integer cod) {
        this.cod = cod;
    }


    private class Clouds {

        @SerializedName("all")
        @Expose
        private Integer all;

        public Integer getAll() {
            return all;
        }

        public void setAll(Integer all) {
            this.all = all;
        }

    }

    private class Coord {

        @SerializedName("lon")
        @Expose
        private Float lon;
        @SerializedName("lat")
        @Expose
        private Float lat;

        public Float getLon() {
            return lon;
        }

        public void setLon(Float lon) {
            this.lon = lon;
        }

        public Float getLat() {
            return lat;
        }

        public void setLat(Float lat) {
            this.lat = lat;
        }
    }

    private class Main {

        @SerializedName("temp")
        @Expose
        private Float temp;
        @SerializedName("pressure")
        @Expose
        private Integer pressure;
        @SerializedName("humidity")
        @Expose
        private Integer humidity;
        @SerializedName("temp_min")
        @Expose
        private Float tempMin;
        @SerializedName("temp_max")
        @Expose
        private Float tempMax;

        public Float getTemp() {
            return temp;
        }

        public void setTemp(Float temp) {
            this.temp = temp;
        }

        public Integer getPressure() {
            return pressure;
        }

        public void setPressure(Integer pressure) {
            this.pressure = pressure;
        }

        public Integer getHumidity() {
            return humidity;
        }

        public void setHumidity(Integer humidity) {
            this.humidity = humidity;
        }

        public Float getTempMin() {
            return tempMin;
        }

        public void setTempMin(Float tempMin) {
            this.tempMin = tempMin;
        }

        public Float getTempMax() {
            return tempMax;
        }

        public void setTempMax(Float tempMax) {
            this.tempMax = tempMax;
        }
    }

    private class Rain {

        @SerializedName("3h")
        @Expose
        private Integer _3h;

        public Integer get3h() {
            return _3h;
        }

        public void set3h(Integer _3h) {
            this._3h = _3h;
        }
    }

    private class Sys {

        @SerializedName("type")
        @Expose
        private Integer type;
        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("message")
        @Expose
        private Float message;
        @SerializedName("country")
        @Expose
        private String country;
        @SerializedName("sunrise")
        @Expose
        private Integer sunrise;
        @SerializedName("sunset")
        @Expose
        private Integer sunset;

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public Float getMessage() {
            return message;
        }

        public void setMessage(Float message) {
            this.message = message;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public Integer getSunrise() {
            return sunrise;
        }

        public void setSunrise(Integer sunrise) {
            this.sunrise = sunrise;
        }

        public Integer getSunset() {
            return sunset;
        }

        public void setSunset(Integer sunset) {
            this.sunset = sunset;
        }
    }

    private class Weather {

        @SerializedName("id")
        @Expose
        private Integer id;
        @SerializedName("main")
        @Expose
        private String main;
        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("icon")
        @Expose
        private String icon;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getMain() {
            return main;
        }

        public void setMain(String main) {
            this.main = main;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }
    }

    public class Wind {

        @SerializedName("speed")
        @Expose
        private Float speed;
        @SerializedName("deg")
        @Expose
        private Integer deg;

        public Float getSpeed() {
            return speed;
        }

        public void setSpeed(Float speed) {
            this.speed = speed;
        }

        public Integer getDeg() {
            return deg;
        }

        public void setDeg(Integer deg) {
            this.deg = deg;
        }

    }

}


