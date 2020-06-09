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

    @SerializedName("exit")
    Integer exit;


    public Integer getBearing_after() {
        return bearing_after;
    }

    public void setBearing_after(Integer bearing_after) {
        this.bearing_after = bearing_after;
    }

    public List<Double> getLocation() {
        return location;
    }

    public void setLocation(List<Double> location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getBearing_before() {
        return bearing_before;
    }

    public void setBearing_before(Integer bearing_before) {
        this.bearing_before = bearing_before;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }

    public Integer getExit() {
        return exit;
    }

    public void setExit(Integer exit) {
        this.exit = exit;
    }
}
