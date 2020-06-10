package ir.ngra.drivertrafficcontroller.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ModelAddress {

    @SerializedName("neighbourhood")
    String neighbourhood;

    @SerializedName("suburb")
    String suburb;

    @SerializedName("city")
    String city;

    @SerializedName("state_district")
    String state_district;

    @SerializedName("district")
    String district;

    @SerializedName("county")
    String county;

    @SerializedName("state")
    String state;

    @SerializedName("country")
    String country;

    @SerializedName("road")
    @Expose
    String road;

    @SerializedName("building")
    @Expose
    String building;

    public ModelAddress() {
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public void setNeighbourhood(String neighbourhood) {
        this.neighbourhood = neighbourhood;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState_district() {
        return state_district;
    }

    public void setState_district(String state_district) {
        this.state_district = state_district;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRoad() {
        return road;
    }

    public void setRoad(String road) {
        this.road = road;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}