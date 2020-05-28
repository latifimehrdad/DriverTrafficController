package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModelRouteWayPoint {

    @SerializedName("hint")
    String hint;

    @SerializedName("location")
    List<Double> location;

    @SerializedName("name")
    String name;

    public String getHint() {
        return hint;
    }

    public List<Double> getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
