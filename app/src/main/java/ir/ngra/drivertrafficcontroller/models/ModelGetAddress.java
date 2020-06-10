package ir.ngra.drivertrafficcontroller.models;


import com.google.gson.annotations.SerializedName;

public class ModelGetAddress {


    @SerializedName("lat")
    String lat;

    @SerializedName("lon")
    String lon;

    @SerializedName("address")
    ModelAddress address;

    public ModelAddress getAddress() {
        return address;
    }

    public void setAddress(ModelAddress address) {
        this.address = address;
    }


    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }


}
