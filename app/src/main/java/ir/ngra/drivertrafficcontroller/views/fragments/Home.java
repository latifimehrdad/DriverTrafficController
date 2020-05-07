package ir.ngra.drivertrafficcontroller.views.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.ButterKnife;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.databinding.FragmentHomeBinding;
import ir.ngra.drivertrafficcontroller.utility.BearingToNorthProvider;
import ir.ngra.drivertrafficcontroller.utility.MehrdadLatifiMap;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Home;

public class Home extends Fragment implements OnMapReadyCallback, BearingToNorthProvider.ChangeEventListener {

    private View view;
    private Context context;
    private VM_Home vm_home;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private MyLocationListener listener;
    private LatLng CurrentLatLng;
    private LatLng OldLatLng;
    private MehrdadLatifiMap mehrdadLatifiMap = new MehrdadLatifiMap();
    private Marker marker;

    private BearingToNorthProvider mBearingProvider;


    public Home() {//_______________________________________________________________________________ Home
    }//_____________________________________________________________________________________________ Home


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {//________________________________________________ onCreateView
        context = getActivity();
        vm_home = new VM_Home(context);
        FragmentHomeBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_home, container, false);
        binding.setHome(vm_home);
        view = binding.getRoot();
        ButterKnife.bind(this, view);
        return view;
    }//_____________________________________________________________________________________________ onCreateView


    @Override
    public void onStart() {//_______________________________________________________________________ onStart
        super.onStart();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fpraMap);
        mapFragment.getMapAsync(this);
    }//_____________________________________________________________________________________________ onStart


    @Override
    public void onMapReady(GoogleMap googleMap) {//_________________________________________________ Start Void onMapReady
        mMap = googleMap;


        //mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mBearingProvider = new BearingToNorthProvider(context);
                mBearingProvider.setChangeEventListener(Home.this);
                mBearingProvider.start();
            }
        });

    }//_____________________________________________________________________________________________ End Void onMapReady


    private void GetCurrentLocationListener() {//___________________________________________________ GetCurrentLocationListener

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

        } else {
            locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
            listener = new MyLocationListener();
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, (LocationListener) listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, listener);
        }

    }//_____________________________________________________________________________________________ GetCurrentLocationListener

    @Override
    public void onCurrentLocationChange(Location loc) {
        Log.i("meri", ""+ loc.getLatitude() + " - " + loc.getLongitude());
        CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        if (marker == null)
            marker = mMap.addMarker(new MarkerOptions()
                    .position(CurrentLatLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.maparrow)));
        marker.setPosition(CurrentLatLng);
        float zoom = (float) 18;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatLng, zoom));
    }

    @Override
    public void onBearingChanged(double bearing) {
        updateCameraBearing(mMap,bearing);
    }


    private class MyLocationListener implements LocationListener {//________________________________ MyLocationListener

        public void onLocationChanged(final Location loc) {

            Log.i("meri", "loc : " + loc.getProvider());
            if (loc.getProvider().equalsIgnoreCase("network"))
                return;

            CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

            if (marker == null)
                marker = mMap.addMarker(new MarkerOptions()
                        .position(CurrentLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.maparrow)));

            marker.setPosition(CurrentLatLng);
            float zoom = (float) 18;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatLng, zoom));
            Log.i("meri", "Bearing : " + loc.getBearing());
            updateCameraBearing(mMap,loc.getBearing());


//            if (OldLatLng == null) {
//                OldLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
//
//                marker.setPosition(OldLatLng);
//                float zoom = (float) 18;
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(OldLatLng, zoom));
//
//                float bearing = (float) mehrdadLatifiMap.GetBearing(OldLatLng, OldLatLng);
//                CameraPosition oldPos = mMap.getCameraPosition();
//                CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
//                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
//            }
//            else {
//                    OldLatLng = CurrentLatLng;
//            }
//
//
//            CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
//
//            Location locationA = new Location("point A");
//
//            locationA.setLatitude(OldLatLng.latitude);
//            locationA.setLongitude(OldLatLng.longitude);
//
//            Location locationB = new Location("point B");
//
//            locationB.setLatitude(CurrentLatLng.latitude);
//            locationB.setLongitude(CurrentLatLng.longitude);
//            float distance = locationA.distanceTo(locationB);
//            Log.i("meri","distance : " + distance);
//
//
//
//
//            if (distance > 1) {
//
//                CameraPosition currentposition=mMap.getCameraPosition();
//                float currentBearing = currentposition.bearing;
//
//
//
//                Log.i("meri", "Old bearing : " + OldLatLng);
//                Log.i("meri", "New bearing : " + CurrentLatLng);



//                float bearing = (float) mehrdadLatifiMap.GetBearing(OldLatLng, CurrentLatLng);

//                CameraPosition oldPos = mMap.getCameraPosition();
//                CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
//                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
//            }

//            if (loc.getProvider().equalsIgnoreCase("gps"))
//                GPSLocation = loc;
//            else
//                NetworkLocation = loc;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderDisabled(String provider) {

        }


        public void onProviderEnabled(String provider) {

        }
    }//_____________________________________________________________________________________________ MyLocationListener



    private void updateCameraBearing(GoogleMap googleMap, double bearing) {//________________________ updateCameraBearing
        if ( googleMap == null) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        googleMap.getCameraPosition() // current Camera
                )
                .bearing((float) bearing)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }//_____________________________________________________________________________________________ updateCameraBearing



    @Override
    public void onDestroy() {//_____________________________________________________________________ onDestroy
        super.onDestroy();
    }//_____________________________________________________________________________________________ onDestroy
}
