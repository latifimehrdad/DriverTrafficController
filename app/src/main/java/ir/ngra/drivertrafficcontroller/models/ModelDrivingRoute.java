package ir.ngra.drivertrafficcontroller.models;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.views.overlay.Polyline;

import java.util.List;

public class ModelDrivingRoute {

    private List<LatLng> Maneuvers;

    private List<Polyline> polylines;

    private float Duration;

    private float Distance;

    private String StreetName;

    private ModelRouteManeuver RouteManeuver;


    public ModelDrivingRoute(List<LatLng> maneuvers, List<Polyline> polylines, float duration, float distance, String streetName, ModelRouteManeuver routeManeuver) {
        Maneuvers = maneuvers;
        this.polylines = polylines;
        Duration = duration;
        Distance = distance;
        StreetName = streetName;
        RouteManeuver = routeManeuver;
    }

    public List<LatLng> getManeuvers() {
        return Maneuvers;
    }

    public List<Polyline> getPolylines() {
        return polylines;
    }

    public void setPolylines(List<Polyline> polylines) {
        this.polylines = polylines;
    }

    public void setManeuvers(List<LatLng> maneuvers) {
        Maneuvers = maneuvers;
    }

    public float getDuration() {
        return Duration;
    }

    public void setDuration(float duration) {
        Duration = duration;
    }

    public float getDistance() {
        return Distance;
    }

    public void setDistance(float distance) {
        Distance = distance;
    }

    public String getStreetName() {
        return StreetName;
    }

    public void setStreetName(String streetName) {
        StreetName = streetName;
    }

    public ModelRouteManeuver getRouteManeuver() {
        return RouteManeuver;
    }

    public void setRouteManeuver(ModelRouteManeuver routeManeuver) {
        RouteManeuver = routeManeuver;
    }
}
