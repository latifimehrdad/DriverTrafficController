package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModelRouteIntersection {

    @SerializedName("out")
    Integer out;

    @SerializedName("in")
    Integer in;

    @SerializedName("entry")
    List<Boolean> entry;

    @SerializedName("location")
    List<Double> location;

    @SerializedName("bearings")
    List<Integer> bearings;

    public Integer getOut() {
        return out;
    }

    public Integer getIn() {
        return in;
    }

    public List<Boolean> getEntry() {
        return entry;
    }

    public List<Double> getLocation() {
        return location;
    }

    public List<Integer> getBearings() {
        return bearings;
    }
}
