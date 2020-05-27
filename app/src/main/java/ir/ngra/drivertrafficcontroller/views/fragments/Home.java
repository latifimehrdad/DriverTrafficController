package ir.ngra.drivertrafficcontroller.views.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.cunoraz.gifview.library.GifView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.tileprovider.util.StorageUtils;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;
import ir.ngra.drivertrafficcontroller.databinding.FragmentHomeBinding;
import ir.ngra.drivertrafficcontroller.utility.BearingToNorthProvider;
import ir.ngra.drivertrafficcontroller.utility.MehrdadLatifiMap;
import ir.ngra.drivertrafficcontroller.utility.TouchableWrapper;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Home;

public class Home extends Fragment implements BearingToNorthProvider.ChangeEventListener {

    private View view;
    private Context context;
    private VM_Home vm_home;
    //    private GoogleMap mMap;
    private LatLng CurrentLatLng;
    private Location CurrentLocation;
    private boolean MapMove = false;
    //    private Marker marker;
    private BearingToNorthProvider mBearingProvider;
    private double OldBearing = 0;
    private double NewBearing = 0;
    private DisposableObserver<String> observer;
    private Integer ErrorCount = 0;
    private final double degreesPerRadian = 180.0 / Math.PI;
    private MapView map = null;
    AlertDialog alertDialog = null;
    private float bearing = 0;
    private boolean ClickForRouting = false;
    private boolean AccessToGoneDirection = true;
    private boolean AccessToRemoveMarker = true;
    private LatLng OldLatLng;
    private Marker pointMarket = null;
    private Marker currentMarker = null;
    private LatLng pointLatLng;
    private boolean GetDirection = false;
    private boolean OnStop = false;


    @BindView(R.id.BtnMove)
    Button BtnMove;

    @BindView(R.id.RelativeLayoutDirection)
    RelativeLayout RelativeLayoutDirection;

    @BindView(R.id.TextViewAddress)
    TextView TextViewAddress;

    @BindView(R.id.LinearLayoutRouter)
    LinearLayout LinearLayoutRouter;

    @BindView(R.id.imageViewRouter)
    ImageView imageViewRouter;

    @BindView(R.id.GifViewRouter)
    GifView GifViewRouter;


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
        if (OnStop)
            getActivity().onBackPressed();

        OSMConfig();
        turnOnScreen();
        if (observer != null)
            observer.dispose();
        observer = null;
        ObserverObservable();
        RetrofitModule.isCancel = true;
    }//_____________________________________________________________________________________________ onStart


    private void OSMConfig() {//____________________________________________________________________ OSMConfig

        RelativeLayoutDirection.setVisibility(View.GONE);
        imageViewRouter.setVisibility(View.VISIBLE);
        GifViewRouter.setVisibility(View.GONE);
        BtnMove.setVisibility(View.INVISIBLE);
        currentMarker = null;

        ClickForRouting = false;
        AccessToGoneDirection = true;
        AccessToRemoveMarker = true;
        GetDirection = false;
        LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.onResume();

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);


        IMapController mapController = map.getController();
        GeoPoint startPoint = new GeoPoint(35.830031, 50.962803);
        mapController.animateTo(startPoint, 8.0, Long.valueOf(1000));
        map.setMapOrientation(bearing);

        mBearingProvider = new BearingToNorthProvider(context);
        mBearingProvider.setChangeEventListener(Home.this);
        mBearingProvider.start();

        map.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                Log.i("meri", "onHover");
                return false;
            }
        });

        map.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MapMove = true;
                BtnMove.setVisibility(View.VISIBLE);

                if(GetDirection)
                    return false;
                map.getOverlays().remove(currentMarker);
                currentMarker = null;

                if (AccessToGoneDirection) {
                    RelativeLayoutDirection.setVisibility(View.GONE);
                    RemoveMarker();
                }

                return false;
            }
        });

        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                if (GetDirection)
                    return false;
                AccessToGoneDirection = false;
                pointLatLng = new LatLng(p.getLatitude(), p.getLongitude());
                GeoPoint point = new GeoPoint(p.getLatitude(), p.getLongitude());
                pointMarket = new Marker(map);
                pointMarket.setPosition(point);
                pointMarket.setIcon(getResources().getDrawable(R.drawable.marker_point));
                pointMarket.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(pointMarket);
                map.getController().animateTo(point, map.getZoomLevelDouble(), Long.valueOf(1000));
                AccessToRemoveMarker = true;
                TextViewAddress.setText("درحال یافتن آدرس، شکیبا باشید ...");
                RelativeLayoutDirection.setVisibility(View.VISIBLE);
                ClickForRouting = false;
                LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (RetrofitModule.isCancel)
                            vm_home.GetAddress(p.getLatitude(), p.getLongitude());
                    }
                }, 500);
                return false;
            }
        };
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(OverlayEvents);


        BtnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BtnMove.setVisibility(View.INVISIBLE);
                MapMove = false;
                if (CurrentLatLng != null) {
                    IMapController mapController = map.getController();
                    GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
                    mapController.animateTo(currentPoint, 19.0, Long.valueOf(1000), bearing, true);
                    if (currentMarker == null) {
                        currentMarker = new Marker(map);
                        currentMarker.setPosition(currentPoint);
                        currentMarker.setIcon(getResources().getDrawable(R.drawable.truck_marker));
                        currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        map.getOverlays().add(currentMarker);
                    } else {
                        currentMarker.setPosition(currentPoint);
                    }
                }
            }
        });


        LinearLayoutRouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!RetrofitModule.isCancel) {
                    RetrofitModule.isCancel = true;
                    imageViewRouter.setVisibility(View.VISIBLE);
                    GifViewRouter.setVisibility(View.GONE);
                } else {
                    imageViewRouter.setVisibility(View.GONE);
                    GifViewRouter.setVisibility(View.VISIBLE);
                    vm_home.Direction(CurrentLatLng.latitude, CurrentLatLng.longitude,
                            pointLatLng.latitude, pointLatLng.longitude);
                }
            }
        });

    }//_____________________________________________________________________________________________ OSMConfig


    private void AccessToGoneDirectionTrue() {//____________________________________________________ AccessToGoneDirectionTrue
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AccessToGoneDirection = true;
            }
        }, 2000);
    }//_____________________________________________________________________________________________ AccessToGoneDirectionTrue


    private void RemoveMarker() {//_________________________________________________________________ RemoveMarker
        if (!AccessToRemoveMarker)
            return;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pointMarket != null) {
                    map.getOverlays().remove(pointMarket);
                    pointMarket = null;
                    AccessToRemoveMarker = false;
                }
            }
        },200);
    }//_____________________________________________________________________________________________ RemoveMarker


    private void ObserverObservable() {//___________________________________________________________ Start ObserverObservable

        observer = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                getActivity()
                        .runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (s) {
                                    case "SendSuccess":
                                        ErrorCount = 0;
                                        break;
                                    case "CurrentLocation":
                                        vm_home.SendLocatoinToServer(CurrentLocation);
                                        break;
                                    case "SendError":
                                        ErrorCount++;
                                        if (ErrorCount > 10) {
                                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case "GetAddress":
                                        TextViewAddress.setText(vm_home.getTextAddress());
                                        LinearLayoutRouter.setBackgroundResource(R.drawable.button_bg);
                                        ClickForRouting = true;
                                        RetrofitModule.isCancel = true;
                                        AccessToRemoveMarker = true;
                                        AccessToGoneDirectionTrue();
                                        break;
                                    case "onFailureAddress":
                                        TextViewAddress.setText("");
                                        LinearLayoutRouter.setBackgroundResource(R.drawable.button_bg);
                                        ClickForRouting = true;
                                        AccessToGoneDirectionTrue();
                                        AccessToRemoveMarker = true;
                                        RetrofitModule.isCancel = true;
                                        break;
                                    case "GetDirection":
                                        RetrofitModule.isCancel = true;
                                        RelativeLayoutDirection.setVisibility(View.GONE);
                                        AccessToGoneDirectionTrue();
                                        AccessToRemoveMarker = false;
                                        GetDirection = true;
                                        DrawRoute();
                                        break;
                                    case "onFailure":
                                        RetrofitModule.isCancel = true;
                                        RelativeLayoutDirection.setVisibility(View.GONE);
                                        if (pointMarket != null) {
                                            map.getOverlays().remove(pointMarket);
                                            pointMarket = null;
                                        }
                                        AccessToGoneDirectionTrue();
                                        AccessToRemoveMarker = true;
                                        RemoveMarker();
                                        GetDirection = false;
                                        break;

                                }
                            }
                        });
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };


        vm_home
                .getPublishSubject()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }//_____________________________________________________________________________________________ End ObserverObservable



    private void DrawRoute() {//____________________________________________________________________ Start Void DrawRoute

        BtnMove.setVisibility(View.INVISIBLE);
        MapMove = false;
//        CarMarker.setVisibility(View.VISIBLE);
        if (CurrentLatLng != null) {
            IMapController mapController = map.getController();
            GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
            mapController.animateTo(currentPoint, 19.0, Long.valueOf(1000), bearing, true);
            if (currentMarker == null) {
                currentMarker = new Marker(map);
                currentMarker.setPosition(currentPoint);
                currentMarker.setIcon(getResources().getDrawable(R.drawable.truck_marker));
                currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(currentMarker);
            } else {
                currentMarker.setPosition(currentPoint);
            }
        }

    }//_____________________________________________________________________________________________ End DrawRoute



    @Override
    public void onCurrentLocationChange(Location loc) {


        CurrentLocation = loc;
        vm_home.getPublishSubject().onNext("CurrentLocation");
        if (CurrentLatLng != null) {
            OldLatLng = CurrentLatLng;
        } else
            OldLatLng = new LatLng(0, 0);

        CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        if (MapMove)
            return;

        bearing = bearing + 30;
        if (bearing > 360)
            bearing = bearing - 360;

        GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
        if (currentMarker == null) {
            currentMarker = new Marker(map);
            currentMarker.setPosition(currentPoint);
            currentMarker.setIcon(getResources().getDrawable(R.drawable.truck_marker));
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(currentMarker);
        } else {
            currentMarker.setPosition(currentPoint);
        }

        IMapController mapController = map.getController();
        mapController.animateTo(currentPoint, 19.0, Long.valueOf(1000), bearing, true);

    }


    @Override
    public void onBearingChanged(double bearing) {

    }


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


    private void updateCameraBearing(GoogleMap googleMap, double bearing) {//_______________________ updateCameraBearing
        if (googleMap == null) return;

        NewBearing = bearing;
        double DifferentBearing = Math.abs(OldBearing - NewBearing);
        if (DifferentBearing > 7) {
            OldBearing = NewBearing;
            CameraPosition camPos = CameraPosition
                    .builder(
                            googleMap.getCameraPosition() // current Camera
                    )
                    .bearing((float) bearing)
                    .build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(camPos), 1000, null);
            Log.i("meri", "onBearingChanged : " + bearing);
        }

    }//_____________________________________________________________________________________________ updateCameraBearing


    @Override
    public void onDestroy() {//_____________________________________________________________________ onDestroy
        super.onDestroy();
        mBearingProvider.stop();
        map.onPause();
    }//_____________________________________________________________________________________________ onDestroy




    private void turnOnScreen() {//_________________________________________________________________ Start turnOnScreen
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // turn on screen
//        PowerManager mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//        PowerManager.WakeLock mWakeLock;
//        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "AppName:tag");
//        mWakeLock.acquire();
    }//_____________________________________________________________________________________________ End turnOnScreen



    @Override
    public void onStop() {
        super.onStop();
        mBearingProvider.stop();
        map.onPause();
        OnStop = true;
    }
}
