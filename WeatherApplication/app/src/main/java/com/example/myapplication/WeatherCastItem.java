package com.example.myapplication;

import android.graphics.drawable.Drawable;

public class WeatherCastItem {
    private int weather;
    private int day;
    private int time;
    private double temp;

    public WeatherCastItem(int weather, int day, int time, double temp) {
        this.weather = weather;
        this.day = day;
        this.time = time;
        this.temp = temp;
    }

    public int getWeather() {
        return weather;
    }
    public int getDay() { return day; }
    public int getTime() {
        return time;
    }
    public double getTemp() {
        return temp;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }
    public void setDay(int day) { this.day = day; }
    public void setTime(int time) {
        this.time = time;
    }
    public void setTemp(double temp) {
        this.temp = temp;
    }
}
