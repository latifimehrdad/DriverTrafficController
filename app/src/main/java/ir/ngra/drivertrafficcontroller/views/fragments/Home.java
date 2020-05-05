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
import ir.ngra.drivertrafficcontroller.utility.MehrdadLatifiMap;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Home;

public class Home extends Fragment implements OnMapReadyCallback {

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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                GetCurrentLocationListener();
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


    private class MyLocationListener implements LocationListener {//_________________________________ Start MyLocationListener

        public void onLocationChanged(final Location loc) {


            if (OldLatLng == null) {
                OldLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());

                if (marker == null)
                    marker = mMap.addMarker(new MarkerOptions()
                            .position(OldLatLng)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.maparrow)));

                marker.setPosition(OldLatLng);
                float zoom = (float) 18;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(OldLatLng, zoom));

                float bearing = (float) mehrdadLatifiMap.GetBearing(OldLatLng, OldLatLng);
                CameraPosition oldPos = mMap.getCameraPosition();
                CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
            }
            else {
                    OldLatLng = CurrentLatLng;
            }


            CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());



            if (OldLatLng.latitude != CurrentLatLng.latitude || OldLatLng.longitude != CurrentLatLng.longitude) {

                CameraPosition currentposition=mMap.getCameraPosition();
                float currentBearing = currentposition.bearing;
                Log.i("meri", "Old bearing : " + OldLatLng);

                marker.setPosition(CurrentLatLng);
                float zoom = (float) 18;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatLng, zoom));

                float bearing = (float) mehrdadLatifiMap.GetBearing(OldLatLng, CurrentLatLng);
                Log.i("meri", "New bearing : " + OldLatLng);
                CameraPosition oldPos = mMap.getCameraPosition();
                CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
            }

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
    }//_____________________________________________________________________________________________ End MyLocationListener


    @Override
    public void onDestroy() {//_____________________________________________________________________ onDestroy
        super.onDestroy();
    }//_____________________________________________________________________________________________ onDestroy
}
