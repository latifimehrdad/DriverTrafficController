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

}
