package ir.ngra.drivertrafficcontroller.utility;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import io.reactivex.subjects.PublishSubject;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.utility.polyutil.ML_PolyUtil;

public class MehrdadLatifiMap {

    private Context context;
    private GoogleMap googleMap;
    private List<LatLng> ML_LatLongs;
    private int ML_Stroke_Color = 0;
    private Float ML_Stroke_Width = 3f;
    private int ML_PATTERN_DASH_LENGTH_PX = 0;
    private int ML_PATTERN_GAP_LENGTH_PX = 0;
    private int ML_PATTERN_DASH_LENGTH_Color = 0;
    private int ML_PATTERN_GAP_LENGTH_Color = 0;
    private int ML_Fill_Color = 0;
    private LatLng ML_FindAddress;
    private final double degreesPerRadian = 180.0 / Math.PI;


    @SuppressLint("ResourceType")
    public void DrawPolyLinesWithArrow() {//________________________________________________________ Start DrawPolyLinesWithArrow

        for (int i = 0; i < getML_LatLongs().size() - 1; i++) {
            LatLng from = getML_LatLongs().get(i);
            LatLng to = getML_LatLongs().get(i + 1);
            PolylineOptions polylines = new PolylineOptions();
            polylines.add(from, to).color(getML_Stroke_Color()).width(getML_Stroke_Width());

            getGoogleMap().addPolyline(polylines);
            float bearing = (float) GetBearing(from,to);
            int icon = R.drawable.navi_marker;
            LatLng centerLatLng = null;
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(from);
            builder.include(to);
            LatLngBounds bounds = builder.build();
            centerLatLng =  bounds.getCenter();
            AddMarker(centerLatLng,"","",icon,bearing);
            //DrawArrowHead(getGoogleMap(), from, to);
        }

    }//_____________________________________________________________________________________________ End DrawPolyLinesWithArrow


    public void DrawPolylines() {//_________________________________________________________________ Start DrawPolylines
        Polyline polyline = getGoogleMap().addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(getML_LatLongs()));
        stylePolyline(polyline);
    }//_____________________________________________________________________________________________ End DrawPolylines


    public void DrawPolygon(boolean Pattern) {//_______________________________ Start DrawPolylines
        Polygon polygon = getGoogleMap().addPolygon(new PolygonOptions()
                .clickable(true)
                .addAll(getML_LatLongs()));
        stylePolygon(polygon, Pattern);
    }//_____________________________________________________________________________________________ End DrawPolylines


    private void stylePolyline(Polyline polyline) {//_______________________________________________ Start stylePolyline
        polyline.setStartCap(new RoundCap());
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(getML_Stroke_Width());
        polyline.setColor(getML_Stroke_Color());
        polyline.setJointType(JointType.ROUND);

    }//_____________________________________________________________________________________________ End stylePolyline


    private void stylePolygon(Polygon polygon, boolean Pattern) {//_________________________________ Start stylePolygon

        if (Pattern) {
            PatternItem DASH = new Dash(getML_PATTERN_DASH_LENGTH_PX());
            PatternItem GAP = new Gap(getML_PATTERN_GAP_LENGTH_PX());
            List<PatternItem> PATTERN_POLYGON = Arrays.asList(GAP, DASH);
            List<PatternItem> pattern = PATTERN_POLYGON;
            polygon.setStrokePattern(pattern);
        }
        polygon.setStrokeWidth(getML_Stroke_Width());
        polygon.setStrokeColor(getML_Stroke_Color());
        polygon.setFillColor(getML_Fill_Color());
    }//_____________________________________________________________________________________________ End stylePolygon


    public float[] MeasureDistance(LatLng Old, LatLng New) {//______________________________________ Start MeasureDistance
        float[] results = new float[1];
        Location.distanceBetween(Old.latitude, Old.longitude,
                New.latitude, New.longitude, results);
        return results;
    }//_____________________________________________________________________________________________ End MeasureDistance


    public Marker AddMarker(
            LatLng latLng,
            String title,
            String tag,
            int icon,
            float rotation) {//_____________________________________________________________________ Start AddMarker
        Marker marker = getGoogleMap().addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .rotation(rotation)
                .icon(BitmapDescriptorFactory.fromResource(icon)));
        marker.setTag(tag);
        return marker;
    }//_____________________________________________________________________________________________ End AddMarker


    public void DrawCircle(LatLng latLng, double radius) {//________________________________________ Start DrawCircle
        Circle circle = getGoogleMap().addCircle(new CircleOptions()
                .center(latLng)
                .radius(radius)
                .strokeWidth(getML_Stroke_Width())
                .strokeColor(getML_Stroke_Color())
                .fillColor(getML_Fill_Color()));
    }//_____________________________________________________________________________________________ End DrawCircle


    public void findAddress(Context c, String location, PublishSubject<String> Observables) {//_____ Start findAddress
        if (location != null && !location.equalsIgnoreCase("")) {
            Locale locale = new Locale("fa_IR");
            Locale.setDefault(locale);
            Geocoder geocoder = new Geocoder(c, locale);

            try {
                List<Address> addressList;
                addressList = geocoder.getFromLocationName(location, 1);
                if (addressList.size() > 0) {
                    setML_FindAddress(new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude()));
                    Observables.onNext("FindAddress");
                } else
                    Observables.onNext("NoAddress");

            } catch (IOException e) {
                e.printStackTrace();
                Observables.onNext("NoAddress");
            }
        }
    }//_____________________________________________________________________________________________ End findAddress

    public static Boolean MlMap_isInside(LatLng point, List<LatLng> latLngs) {//____________________ Start isInside
        boolean in = ML_PolyUtil.containsLocation(point, latLngs, true);
        if(in)
            return true;
        else {
            return ML_PolyUtil.isLocationOnPath(point,latLngs,true, 15);
        }
        //containsLocation(point, latLngs, true);
    }//_____________________________________________________________________________________________ End isInside




    public ArrayList<LatLng> getCirclePoint(LatLng centre, double radius) {//_______________________ Start getCirclePoint
        ArrayList<LatLng> points = new ArrayList<LatLng>();

        double EARTH_RADIUS = 6378100.0;
        double lat = centre.latitude * Math.PI / 180.0;
        double lon = centre.longitude * Math.PI / 180.0;

        for (double t = 0; t <= Math.PI * 2; t += 0.3) {
            double latPoint = lat + (radius / EARTH_RADIUS) * Math.sin(t);
            double lonPoint = lon + (radius / EARTH_RADIUS) * Math.cos(t) / Math.cos(lat);
            LatLng point = new LatLng(latPoint * 180.0 / Math.PI, lonPoint * 180.0 / Math.PI);
            points.add(point);

        }

        return points;
    }//_____________________________________________________________________________________________ End getCirclePoint


    public void AutoZoom() {//______________________________________________________________________ Start AutoZoom
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : getML_LatLongs()) {
            builder.include(latLng);
        }
        LatLngBounds bounds = builder.build();
        int padding = 20; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        getGoogleMap().animateCamera(cu);
    }//_____________________________________________________________________________________________ End AutoZoom



    private void DrawArrowHead(GoogleMap mMap, LatLng from, LatLng to) {//__________________________ Start DrawArrowHead
        // obtain the bearing between the last two points
        double bearing = GetBearing(from, to);

        // round it to a multiple of 3 and cast out 120s
        double adjBearing = Math.round(bearing / 3) * 3;
        while (adjBearing >= 120) {
            adjBearing -= 120;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Get the corresponding triangle marker from Google
        URL url;
        Bitmap image = null;

        try {
            url = new URL("http://www.google.com/intl/en_ALL/mapfiles/dir_" + String.valueOf((int) adjBearing) + ".png");
            try {
                image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (image != null) {

            // Anchor is ratio in range [0..1] so value of 0.5 on x and y will center the marker image on the lat/long
            float anchorX = 0.5f;
            float anchorY = 0.5f;

            int offsetX = 0;
            int offsetY = 0;

            // images are 24px x 24px
            // so transformed image will be 48px x 48px

            //315 range -- 22.5 either side of 315
            if (bearing >= 292.5 && bearing < 335.5) {
                offsetX = 24;
                offsetY = 24;
            }
            //270 range
            else if (bearing >= 247.5 && bearing < 292.5) {
                offsetX = 24;
                offsetY = 12;
            }
            //225 range
            else if (bearing >= 202.5 && bearing < 247.5) {
                offsetX = 24;
                offsetY = 0;
            }
            //180 range
            else if (bearing >= 157.5 && bearing < 202.5) {
                offsetX = 12;
                offsetY = 0;
            }
            //135 range
            else if (bearing >= 112.5 && bearing < 157.5) {
                offsetX = 0;
                offsetY = 0;
            }
            //90 range
            else if (bearing >= 67.5 && bearing < 112.5) {
                offsetX = 0;
                offsetY = 12;
            }
            //45 range
            else if (bearing >= 22.5 && bearing < 67.5) {
                offsetX = 0;
                offsetY = 24;
            }
            //0 range - 335.5 - 22.5
            else {
                offsetX = 12;
                offsetY = 24;
            }

            Bitmap wideBmp;
            Canvas wideBmpCanvas;
            Rect src, dest;

            // Create larger bitmap 4 times the size of arrow head image
            wideBmp = Bitmap.createBitmap(image.getWidth() * 2, image.getHeight() * 2, image.getConfig());

            wideBmpCanvas = new Canvas(wideBmp);

            src = new Rect(0, 0, image.getWidth(), image.getHeight());
            dest = new Rect(src);
            dest.offset(offsetX, offsetY);

            wideBmpCanvas.drawBitmap(image, src, dest, null);

            mMap.addMarker(new MarkerOptions()
                    .position(to)
                    .icon(BitmapDescriptorFactory.fromBitmap(wideBmp))
                    .anchor(anchorX, anchorY));
        }
    }//_____________________________________________________________________________________________ End DrawArrowHead


    public double GetBearing(LatLng from, LatLng to) {//____________________________________________ Start GetBearing
        double lat1 = from.latitude * Math.PI / 180.0;
        double lon1 = from.longitude * Math.PI / 180.0;
        double lat2 = to.latitude * Math.PI / 180.0;
        double lon2 = to.longitude * Math.PI / 180.0;

        // Compute the angle.
        double angle = -Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        if (angle < 0.0)
            angle += Math.PI * 2.0;

        // And convert result to degrees.
        angle = angle * degreesPerRadian;

        return angle;
    }//_____________________________________________________________________________________________ End GetBearing


    //______________________________________________________________________________________________ Start Getter AND Setter

    public List<LatLng> getML_LatLongs() {
        return ML_LatLongs;
    }

    public void setML_LatLongs(List<LatLng> ML_LatLong) {
        this.ML_LatLongs = ML_LatLong;
    }

    public int getML_Stroke_Color() {
        return ML_Stroke_Color;
    }

    public void setML_Stroke_Color(int ML_Stroke_Color) {
        this.ML_Stroke_Color = ML_Stroke_Color;
    }

    public Float getML_Stroke_Width() {
        return ML_Stroke_Width;
    }

    public void setML_Stroke_Width(Float ML_Stroke_Width) {
        this.ML_Stroke_Width = ML_Stroke_Width;
    }

    public int getML_PATTERN_DASH_LENGTH_PX() {
        return ML_PATTERN_DASH_LENGTH_PX;
    }

    public void setML_PATTERN_DASH_LENGTH_PX(int ML_PATTERN_DASH_LENGTH_PX) {
        this.ML_PATTERN_DASH_LENGTH_PX = ML_PATTERN_DASH_LENGTH_PX;
    }

    public int getML_PATTERN_GAP_LENGTH_PX() {
        return ML_PATTERN_GAP_LENGTH_PX;
    }

    public void setML_PATTERN_GAP_LENGTH_PX(int ML_PATTERN_GAP_LENGTH_PX) {
        this.ML_PATTERN_GAP_LENGTH_PX = ML_PATTERN_GAP_LENGTH_PX;
    }

    public int getML_PATTERN_DASH_LENGTH_Color() {
        return ML_PATTERN_DASH_LENGTH_Color;
    }

    public void setML_PATTERN_DASH_LENGTH_Color(int ML_PATTERN_DASH_LENGTH_Color) {
        this.ML_PATTERN_DASH_LENGTH_Color = ML_PATTERN_DASH_LENGTH_Color;
    }

    public int getML_PATTERN_GAP_LENGTH_Color() {
        return ML_PATTERN_GAP_LENGTH_Color;
    }

    public void setML_PATTERN_GAP_LENGTH_Color(int ML_PATTERN_GAP_LENGTH_Color) {
        this.ML_PATTERN_GAP_LENGTH_Color = ML_PATTERN_GAP_LENGTH_Color;
    }

    public int getML_Fill_Color() {
        return ML_Fill_Color;
    }

    public void setML_Fill_Color(int ML_Fill_Color) {
        this.ML_Fill_Color = ML_Fill_Color;
    }

    public GoogleMap getGoogleMap() {
        return googleMap;
    }

    public void setGoogleMap(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public LatLng getML_FindAddress() {
        return ML_FindAddress;
    }

    public void setML_FindAddress(LatLng ML_FindAddress) {
        this.ML_FindAddress = ML_FindAddress;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    //______________________________________________________________________________________________ End Getter AND Setter
}
