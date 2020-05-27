package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ModelRouteLeg {

    @SerializedName("steps")
    ArrayList<ModelRouteStep> steps;

    @SerializedName("weight")
    float weight;

    @SerializedName("distance")
    float distance;

    @SerializedName("summary")
    String summary;

    @SerializedName("duration")
    float duration;

}
