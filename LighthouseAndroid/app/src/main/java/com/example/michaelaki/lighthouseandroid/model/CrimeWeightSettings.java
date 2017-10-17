package com.example.michaelaki.lighthouseandroid.model;

import java.io.Serializable;

/**
 * Created by michaelaki on 9/11/17.
 */

public class CrimeWeightSettings implements Serializable {
    private double homicide, rape, assault, pedestrianTheft, vehicularTheft;

    public CrimeWeightSettings() {
        homicide = 1.0;
        rape = 1.0;
        assault = 1.0;
        pedestrianTheft = 0.5;
        vehicularTheft = 0.25;
    }

    public double getHomicide() {
        return homicide;
    }

    public double getRape() {
        return rape;
    }

    public double getAssault() {
        return assault;
    }

    public double getPedestrianTheft() {
        return pedestrianTheft;
    }

    public double getVehicularTheft() {
        return vehicularTheft;
    }

    public void setCrimeWeights(double homicide, double assault, double rape, double pedestrianTheft,
                                double vehicularTheft) {
        this.homicide = homicide;
        this.rape = rape;
        this.assault = assault;
        this.pedestrianTheft = pedestrianTheft;
        this.vehicularTheft = vehicularTheft;
    }
}
