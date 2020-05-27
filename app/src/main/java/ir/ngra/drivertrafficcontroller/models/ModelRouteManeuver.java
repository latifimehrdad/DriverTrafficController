package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModelRouteManeuver {

    @SerializedName("bearing_after")
    Integer bearing_after;

    @SerializedName("location")
    List<Double> location;

    @SerializedName("type")
    String type;

    @SerializedName("bearing_before")
    Integer bearing_before;

    @SerializedName("modifier")
    String modifier;
}
