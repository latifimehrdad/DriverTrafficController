package ir.ngra.drivertrafficcontroller.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class ModelRoutesSteps {

    private List<LatLng> LatLngs;

    private Integer BearingBefore;

    private Integer BearingAfter;

    public ModelRoutesSteps(List<LatLng> latLngs, Integer bearingBefore, Integer bearingAfter) {
        LatLngs = latLngs;
        BearingBefore = bearingBefore;
        BearingAfter = bearingAfter;
    }

    public List<LatLng> getLatLngs() {
        return LatLngs;
    }

    public Integer getBearingBefore() {
        return BearingBefore;
    }

    public Integer getBearingAfter() {
        return BearingAfter;
    }
}
