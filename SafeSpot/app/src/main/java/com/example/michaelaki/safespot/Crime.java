package com.example.michaelaki.safespot;

/**
 * Created by jspall16 on 6/23/17.
 */

public class Crime {
    private String id;
    private String date;
    private String typeCrime;
    private double lat;
    private double lng;

    public Crime(String id, String date, String typeCrime, double lat, double lng)
    {
        this.id = id;
        this.date = date;
        this.typeCrime = typeCrime;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getDate(){
        return date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getTypeCrime(){
        return typeCrime;
    }

    public void setTypeCrime(String typeCrime){
        this.typeCrime = typeCrime;
    }

    public double getLat(){
        return lat;
    }

    public void setLat(double lat){
        this.lat = lat;
    }

    public double getLng(){
        return lng;
    }

    public void setLong(double lng){
        this.lng = lng;
    }





}
