package com.example.michaelaki.lighthouseandroid.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by michaelaki on 10/8/17.
 */

public class Crime implements ClusterItem {
    private String crimeType;
    private String id;
    private String date;
    private LatLng latLng;

    public Crime(String crimeType, String id, String date, LatLng latLng) {
        this.crimeType = crimeType;
        this.id = id;
        this.date = date;
        this.latLng = latLng;

    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return crimeType;
    }

    @Override
    public String getSnippet() {
        return date;
    }
}
