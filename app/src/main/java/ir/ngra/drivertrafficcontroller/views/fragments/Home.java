package ir.ngra.drivertrafficcontroller.views.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.databinding.FragmentHomeBinding;
import ir.ngra.drivertrafficcontroller.utility.BearingToNorthProvider;
import ir.ngra.drivertrafficcontroller.utility.MehrdadLatifiMap;
import ir.ngra.drivertrafficcontroller.utility.TouchableWrapper;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Home;

public class Home extends Fragment implements OnMapReadyCallback, BearingToNorthProvider.ChangeEventListener{

    private View view;
    private Context context;
    private VM_Home vm_home;
    private GoogleMap mMap;
    private LatLng CurrentLatLng;
    public static boolean MapMove = false;
//    private Marker marker;
    private BearingToNorthProvider mBearingProvider;
    private double OldBearing = 0;
    private double NewBearing = 0;


    @BindView(R.id.BtnMove)
    Button BtnMove;

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
        BtnMove.setVisibility(View.GONE);
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        });



        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {

            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

            }
        });

        BtnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnMove.setVisibility(View.GONE);
                MapMove = false;
            }
        });

    }//_____________________________________________________________________________________________ End Void onMapReady


    @Override
    public void onCurrentLocationChange(Location loc) {

        CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        float zoom = 15;
        if (MapMove) {
            BtnMove.setVisibility(View.VISIBLE);
            zoom = mMap.getCameraPosition().zoom;
        } else {
            zoom = (float) 18.5;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatLng, zoom));
        }

//        if (marker == null) {
//            marker = mMap.addMarker(new MarkerOptions()
//                    .position(CurrentLatLng)
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.green_arrow)));
//            marker.setPosition(CurrentLatLng);
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatLng, zoom));
//        } else {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CurrentLatLng, zoom));
//        }
    }

    @Override
    public void onBearingChanged(double bearing) {
        updateCameraBearing(mMap,bearing);
    }


    private void updateCameraBearing(GoogleMap googleMap, double bearing) {//_______________________ updateCameraBearing
        if ( googleMap == null) return;

        NewBearing = bearing;
        double DifferentBearing = Math.abs(OldBearing - NewBearing);
        if (DifferentBearing > 10) {
            OldBearing = NewBearing;
            CameraPosition camPos = CameraPosition
                    .builder(
                            googleMap.getCameraPosition() // current Camera
                    )
                    .bearing((float) bearing)
                    .build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos),1000,null);
        }

    }//_____________________________________________________________________________________________ updateCameraBearing



    @Override
    public void onDestroy() {//_____________________________________________________________________ onDestroy
        super.onDestroy();
        mBearingProvider.stop();
    }//_____________________________________________________________________________________________ onDestroy
}
