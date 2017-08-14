package com.example.michaelaki.safespot;

import com.mapbox.mapboxsdk.constants.Style;

/**
 * Created by michaelaki on 8/10/17.
 */

public class Settings {
    private int year;
    private double radius;
    private String mapType;
    public Settings() {
        year = 2014;
        radius = 0.5;
        mapType = Style.MAPBOX_STREETS;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getRadius() {
        return radius;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public String getMapType() {
        return mapType;
    }
}
