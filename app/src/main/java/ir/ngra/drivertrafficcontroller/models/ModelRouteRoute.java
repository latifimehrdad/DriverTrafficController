package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ModelRouteRoute {

    @SerializedName("legs")
    ArrayList<ModelRouteLeg> legs;

    @SerializedName("weight_name")
    String weight_name;

    @SerializedName("weight")
    float weight;

    @SerializedName("distance")
    float distance;

    @SerializedName("duration")
    float duration;

    public ArrayList<ModelRouteLeg> getLegs() {
        return legs;
    }

    public String getWeight_name() {
        return weight_name;
    }

    public float getWeight() {
        return weight;
    }

    public float getDistance() {
        return distance;
    }

    public float getDuration() {
        return duration;
    }
}
