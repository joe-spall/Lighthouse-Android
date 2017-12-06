package com.example.michaelaki.lighthouseandroid.model;


import com.google.android.gms.maps.GoogleMap;

import java.io.Serializable;

/**
 * Created by michaelaki on 8/10/17.
 */

public class Settings implements Serializable{
    private int year;
    private int radius;
    private int mapType;
    private String crimeTime;
    private CrimeWeightSettings crimeWeights;

    public Settings() {
        year = 2014;
        radius = 150;
        mapType = GoogleMap.MAP_TYPE_NORMAL;
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

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public int getMapType() {
        return mapType;
    }

    public void setCrimeTime(String crimeTime) {
        this.crimeTime = crimeTime;
    }

    public String getCrimeTime() {
        return crimeTime;
    }
}
