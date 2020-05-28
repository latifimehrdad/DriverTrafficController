package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ModelRouteStep {

    @SerializedName("intersections")
    ArrayList<ModelRouteIntersection> intersections;

    @SerializedName("driving_side")
    String driving_side;

    @SerializedName("geometry")
    String geometry;

    @SerializedName("duration")
    float duration;

    @SerializedName("distance")
    float distance;

    @SerializedName("name")
    String name;

    @SerializedName("weight")
    float weight;

    @SerializedName("mode")
    String mode;

    @SerializedName("maneuver")
    ModelRouteManeuver maneuver;

    public ArrayList<ModelRouteIntersection> getIntersections() {
        return intersections;
    }

    public String getDriving_side() {
        return driving_side;
    }

    public String getGeometry() {
        return geometry;
    }

    public float getDuration() {
        return duration;
    }

    public float getDistance() {
        return distance;
    }

    public String getName() {
        return name;
    }

    public float getWeight() {
        return weight;
    }

    public String getMode() {
        return mode;
    }

    public ModelRouteManeuver getManeuver() {
        return maneuver;
    }
}
