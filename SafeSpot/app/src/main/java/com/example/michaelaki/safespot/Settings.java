package com.example.michaelaki.safespot;

import com.mapbox.mapboxsdk.constants.Style;

import java.io.Serializable;

/**
 * Created by michaelaki on 8/10/17.
 */

public class Settings implements Serializable{
    private int year;
    private double radius;
    private String mapType;
    private String crimeTime;
    private CrimeWeightSettings crimeWeights;

    public Settings() {
        year = 2014;
        radius = 0.5;
        mapType = Style.MAPBOX_STREETS;
        crimeWeights = new CrimeWeightSettings();
        crimeTime = "MAN";
    }

    public CrimeWeightSettings getCrimeWeights() {
        return crimeWeights;
    }

    public void setCrimeWeights(CrimeWeightSettings crimeWeights) {
        this.crimeWeights = crimeWeights;
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

    public void setCrimeTime(String crimeTime) {
        this.crimeTime = crimeTime;
    }

    public String getCrimeTime() {
        return crimeTime;
    }
}
