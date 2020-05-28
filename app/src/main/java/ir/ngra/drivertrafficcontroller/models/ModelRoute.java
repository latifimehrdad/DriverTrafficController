package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ModelRoute {

    @SerializedName("code")
    String code;

    @SerializedName("waypoints")
    ArrayList<ModelRouteWayPoint> waypoints;

    @SerializedName("routes")
    ArrayList<ModelRouteRoute> routes;

    public String getCode() {
        return code;
    }

    public ArrayList<ModelRouteWayPoint> getWaypoints() {
        return waypoints;
    }

    public ArrayList<ModelRouteRoute> getRoutes() {
        return routes;
    }
}
