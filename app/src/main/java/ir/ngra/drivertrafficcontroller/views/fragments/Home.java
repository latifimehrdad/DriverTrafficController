package ir.ngra.drivertrafficcontroller.views.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.cunoraz.gifview.library.GifView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.TileOverlayOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;
import ir.ngra.drivertrafficcontroller.databinding.FragmentHomeBinding;
import ir.ngra.drivertrafficcontroller.models.ModelRoute;
import ir.ngra.drivertrafficcontroller.models.ModelRouteIntersection;
import ir.ngra.drivertrafficcontroller.models.ModelRouteStep;
import ir.ngra.drivertrafficcontroller.models.ModelRoutesSteps;
import ir.ngra.drivertrafficcontroller.utility.BearingToNorthProvider;
import ir.ngra.drivertrafficcontroller.utility.MehrdadLatifiMap;
import ir.ngra.drivertrafficcontroller.utility.MyUrlTileProvider;
import ir.ngra.drivertrafficcontroller.utility.polyutil.ML_PolyUtil;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Home;

public class Home extends Fragment implements BearingToNorthProvider.ChangeEventListener,
        OnMapReadyCallback {

    private View view;
    private Context context;
    private VM_Home vm_home;
    private GoogleMap mMap;
    private LatLng CurrentLatLng;
    private Location CurrentLocation;
    private boolean MapMove = false;
    private BearingToNorthProvider mBearingProvider;
    private double OldBearing = 0;
    private double NewBearing = 0;
    private DisposableObserver<String> observer;
    private Integer ErrorCount = 0;
    private final double degreesPerRadian = 180.0 / Math.PI;
    private float bearing = 0;
    private float BeforeBearing = 0;
    private boolean AccessToGoneDirection = true;
    private boolean AccessToRemoveMarker = true;
    private LatLng OldLatLng;
    private Marker pointMarket = null;
    private Marker currentMarker = null;
    private LatLng pointLatLng;
    private boolean GetDirection = false;
    private ModelRoute routes;
    private List<ModelRoutesSteps> RoutesLatLng;
    private Integer DrivingStep;
    private List<Polyline> polylineList;
    private Integer PolyIndex;


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

    @BindView(R.id.CarMarker)
    ImageView CarMarker;


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

        GoogleConfig();
        ConfigFirst();

        turnOnScreen();
        if (observer != null)
            observer.dispose();
        observer = null;
        ObserverObservable();
        RetrofitModule.isCancel = true;
    }//_____________________________________________________________________________________________ onStart


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {//_________________________________________________ Start onMapReady
        mMap = googleMap;
        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setMaxZoomPreference(19);
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);




        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                String mUrl = "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
                MyUrlTileProvider mTileProvider = new MyUrlTileProvider(256, 256, mUrl);
                mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mTileProvider).zIndex(0));
                mBearingProvider = new BearingToNorthProvider(context);
                mBearingProvider.setChangeEventListener(Home.this);
                mBearingProvider.start();

            }
        });


        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {

                if (GetDirection) {
                    BtnMove.setVisibility(View.VISIBLE);
                    return;
                }

                if (AccessToGoneDirection) {
                    RelativeLayoutDirection.setVisibility(View.GONE);
                    if (pointMarket != null)
                        pointMarket.remove();
                }

//                textChoose.setVisibility(View.GONE);
//                MarkerGif.setVisibility(View.VISIBLE);
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {

                return false;
            }
        });


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (GetDirection)
                    return;
                if (CurrentLatLng == null)
                    return;

                AccessToGoneDirection = false;
                pointLatLng = latLng;
                pointMarket = mMap.addMarker(new MarkerOptions()
                        .position(pointLatLng)
                        .zIndex(1)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_point)));
                AccessToRemoveMarker = true;
                TextViewAddress.setText("درحال یافتن آدرس، شکیبا باشید ...");
                RelativeLayoutDirection.setVisibility(View.VISIBLE);
                LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(pointLatLng)      // Sets the center of the map to Mountain View
                        .zoom(18)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (RetrofitModule.isCancel)
//                            vm_home.GetAddress(latLng.latitude, latLng.longitude);
//                    }
//                }, 500);

            }
        });

    }//_____________________________________________________________________________________________ End onMapReady


    private void ConfigFirst() {//__________________________________________________________________ ConfigFirst

        RelativeLayoutDirection.setVisibility(View.GONE);
        imageViewRouter.setVisibility(View.VISIBLE);
        GifViewRouter.setVisibility(View.GONE);
        BtnMove.setVisibility(View.INVISIBLE);
        CarMarker.setVisibility(View.GONE);
        AccessToGoneDirection = true;
        AccessToRemoveMarker = true;
        GetDirection = false;
        LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);


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


    }//_____________________________________________________________________________________________ ConfigFirst


    private void GoogleConfig() {//_________________________________________________________________ GoogleConfig

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.fpraMap);
        mapFragment.getMapAsync(this);

        BtnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapMove = false;
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(CurrentLatLng)      // Sets the center of the map to Mountain View
                        .zoom(19)                   // Sets the zoom
                        .bearing(bearing)                // Sets the orientation of the camera to east
                        .tilt(80)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                BtnMove.setVisibility(View.INVISIBLE);

            }
        });

    }//_____________________________________________________________________________________________ GoogleConfig


    private void AccessToGoneDirectionTrue() {//____________________________________________________ AccessToGoneDirectionTrue
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                AccessToGoneDirection = true;
            }
        }, 1000);
    }//_____________________________________________________________________________________________ AccessToGoneDirectionTrue


    private void RemoveMarker() {//_________________________________________________________________ RemoveMarker
        if (!AccessToRemoveMarker)
            return;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (pointMarket != null) {
                    pointMarket = null;
                    AccessToRemoveMarker = false;
                }
            }
        }, 200);
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
                                        RetrofitModule.isCancel = true;
                                        AccessToRemoveMarker = true;
                                        AccessToGoneDirectionTrue();
                                        break;
                                    case "onFailureAddress":
                                        TextViewAddress.setText("");
                                        LinearLayoutRouter.setBackgroundResource(R.drawable.button_bg);
                                        AccessToGoneDirectionTrue();
                                        AccessToRemoveMarker = true;
                                        RetrofitModule.isCancel = true;
                                        break;
                                    case "GetDirection":
                                        RetrofitModule.isCancel = true;
                                        RelativeLayoutDirection.setVisibility(View.GONE);
                                        AccessToGoneDirectionTrue();
                                        AccessToRemoveMarker = false;
                                        ConfigRoute();
                                        break;
                                    case "onFailure":
                                        RetrofitModule.isCancel = true;
                                        imageViewRouter.setVisibility(View.VISIBLE);
                                        GifViewRouter.setVisibility(View.GONE);
                                        RelativeLayoutDirection.setVisibility(View.GONE);
                                        if (pointMarket != null) {
                                            pointMarket.remove();
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


    private void ConfigRoute() {//__________________________________________________________________ Start Void ConfigRoute

        if(currentMarker != null) {
            currentMarker.remove();
            currentMarker = null;
        }
        CarMarker.setVisibility(View.VISIBLE);
        BtnMove.setVisibility(View.INVISIBLE);
        MapMove = false;
        DrivingStep = 0;
        OldBearing = 0;
        NewBearing = 0;
        BeforeBearing = 0;
        PolyIndex = -1;
        routes = vm_home.getRoute();
        if (polylineList != null) {
            polylineList.clear();
            polylineList = null;
        }
        if (RoutesLatLng != null) {
            RoutesLatLng.clear();
            RoutesLatLng = null;
        }
        RoutesLatLng = new ArrayList<>();
        polylineList = new ArrayList<>();
        ModelRouteStep startStep = routes.getRoutes().get(0).getLegs().get(0).getSteps().get(0);
        ModelRouteIntersection startIntersection = startStep.getIntersections().get(0);
        bearing = startStep.getManeuver().getBearing_after();

        if (startStep.getIntersections().size() > 1) {
            DrawRoutes();
        } else {
            DrawRoutes();
        }


    }//_____________________________________________________________________________________________ End ConfigRoute


    private void DrawRoutes() {//___________________________________________________________________ DrawRoutes

        LatLng StartPoint = null;
        LatLng EndPoint = null;
        int stepCount = routes.getRoutes().get(0).getLegs().get(0).getSteps().size();
        for (int st = 0; st < stepCount; st++) {
            ModelRouteStep step = routes.getRoutes().get(0).getLegs().get(0).getSteps().get(st);
            List<LatLng> latLngs = ML_PolyUtil.decode(step.getGeometry());
            RoutesLatLng.add(new ModelRoutesSteps(latLngs, step.getManeuver().getBearing_before(), step.getManeuver().getBearing_after()));
            for (int i = 0; i < latLngs.size() - 1; i = i + 1) {
                StartPoint = new LatLng(latLngs.get(i).latitude, latLngs.get(i).longitude);
                EndPoint = new LatLng(latLngs.get(i + 1).latitude, latLngs.get(i + 1).longitude);
                List<LatLng> poly = new ArrayList<>();
                poly.add(StartPoint);
                poly.add(EndPoint);
                DrawPolyLine(StartPoint, EndPoint, getResources().getColor(R.color.ML_PolyLine));
            }

        }
        StartPoint = EndPoint;
        EndPoint = new LatLng(pointLatLng.latitude, pointLatLng.longitude);
        DrawPolyLine(StartPoint, EndPoint, getResources().getColor(R.color.ML_PolyLine));

        if (CurrentLatLng != null) {
            List<LatLng> start = polylineList.get(0).getPoints();
            LatLng car = ML_PolyUtil.getMarkerProjectionOnSegment(CurrentLatLng, start, mMap.getProjection());
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(car)      // Sets the center of the map to Mountain View
                    .zoom(19)                   // Sets the zoom
                    .bearing(bearing)                // Sets the orientation of the camera to east
                    .tilt(80)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GetDirection = true;
            }
        }, 1000);


    }//_____________________________________________________________________________________________ DrawRoutes


    private void DrawPolyLine(LatLng start, LatLng end, int color) {//______________________________ DrawPolyLine
        List<LatLng> line = new ArrayList<>();
        line.add(start);
        line.add(end);
        Polyline polyline = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .addAll(line)
                .zIndex(1));
        stylePolyline(polyline);

        polylineList.add(polyline);
    }//_____________________________________________________________________________________________ DrawPolyLine



    private void stylePolyline(Polyline polyline) {//_______________________________________________ Start stylePolyline
        polyline.setStartCap(new RoundCap());
        polyline.setEndCap(new RoundCap());
        polyline.setWidth(40);
        polyline.setColor(getResources().getColor(R.color.ML_PolyLine));
        polyline.setJointType(JointType.ROUND);

    }//_____________________________________________________________________________________________ End stylePolyline


    @Override
    public void onCurrentLocationChange(Location loc) {//___________________________________________ onCurrentLocationChange

        Integer LineStep = 0;
        CurrentLocation = loc;
//        vm_home.getPublishSubject().onNext("CurrentLocation");
        if (CurrentLatLng != null) {
            OldLatLng = CurrentLatLng;
        } else
            OldLatLng = new LatLng(0, 0);

        if (MapMove)
            return;

        if (GetDirection) {
            CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            List<LatLng> latLngsLine = null;
            MehrdadLatifiMap latifiMap = new MehrdadLatifiMap();
            boolean isInside = false;
            boolean CheckNext = false;
            for (int st = 0; st < RoutesLatLng.size(); st++) {
                for (int line = 0; line < RoutesLatLng.get(st).getLatLngs().size() - 1; line++) {
                    List<LatLng> latLngs = new ArrayList<>();
                    LatLng start = new LatLng(RoutesLatLng.get(st).getLatLngs().get(line).latitude, RoutesLatLng.get(st).getLatLngs().get(line).longitude);
                    latLngs.add(start);
                    LatLng end = new LatLng(RoutesLatLng.get(st).getLatLngs().get(line + 1).latitude, RoutesLatLng.get(st).getLatLngs().get(line + 1).longitude);
                    latLngs.add(end);
                    if (CheckNext)
                        isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngs, true, 4);
                    else
                        isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngs, true, 13);

                    if (isInside) {
                        bearing = (float) GetBearing(start, end);
                        CheckNext = true;
                        DrivingStep = st;
                        latLngsLine = latLngs;
                    } else {
                        if (CheckNext) {
                            isInside = true;
                            CheckNext = false;
                            break;
                        }
                    }

                }


                if (st == RoutesLatLng.size() - 1) {
                    CheckNext = false;
                }

                if (isInside && !CheckNext) {

                    LatLng car = ML_PolyUtil.getMarkerProjectionOnSegment(CurrentLatLng, latLngsLine, mMap.getProjection());
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(car)      // Sets the center of the map to Mountain View
                            .zoom(19)                   // Sets the zoom
                            .bearing(bearing)                // Sets the orientation of the camera to east
                            .tilt(80)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    break;
                } else {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(CurrentLatLng)      // Sets the center of the map to Mountain View
                            .zoom(19)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }

            if (!isInside) {
                GetDirection = false;
                for (Polyline p : polylineList)
                    p.remove();
                TextViewAddress.setText("در حال یافتن مسیر جدید ...");
                RelativeLayoutDirection.setVisibility(View.VISIBLE);
                imageViewRouter.setVisibility(View.GONE);
                GifViewRouter.setVisibility(View.VISIBLE);
                vm_home.Direction(CurrentLatLng.latitude, CurrentLatLng.longitude,
                        pointLatLng.latitude, pointLatLng.longitude);
            }

            for (int i = 0; i < DrivingStep; i++) {
                for (int j = 0; j < RoutesLatLng.get(0).getLatLngs().size() - 1; j++) {
                    Polyline p = polylineList.get(0);
                    p.remove();
                    polylineList.remove(0);
                }
                RoutesLatLng.remove(i);
            }


            float[] results = new float[1];
            Location.distanceBetween(CurrentLatLng.latitude, CurrentLatLng.longitude,
                    pointLatLng.latitude, pointLatLng.longitude, results);
            if (results.length > 0)
                if (results[0] < 20) {
                    for (Polyline p : polylineList)
                        p.remove();
                    polylineList.clear();
                    RoutesLatLng.clear();
                    if (pointMarket != null)
                        pointMarket.remove();
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(CurrentLatLng)      // Sets the center of the map to Mountain View
                            .zoom(19)                   // Sets the zoom
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    RelativeLayoutDirection.setVisibility(View.GONE);
                    imageViewRouter.setVisibility(View.VISIBLE);
                    GifViewRouter.setVisibility(View.GONE);
                    BtnMove.setVisibility(View.INVISIBLE);
                    AccessToGoneDirection = true;
                    AccessToRemoveMarker = true;
                    GetDirection = false;
                    LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);

                }


        } else {
            if (CurrentLatLng == null) {
                CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                if (currentMarker == null) {
                    currentMarker = mMap.addMarker(new MarkerOptions()
                            .position(CurrentLatLng)
                            .zIndex(1)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.navi_marker)));
                } else
                    currentMarker.setPosition(CurrentLatLng);

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(CurrentLatLng)      // Sets the center of the map to Mountain View
                        .zoom(19)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

        }

    }//_____________________________________________________________________________________________ onCurrentLocationChange


    @Override
    public void onBearingChanged(double bearing) {

    }


    public double GetBearing(LatLng from, LatLng to) {//____________________________________________ Start GetBearing
        double lat1 = from.latitude * Math.PI / 180.0;
        double lon1 = from.longitude * Math.PI / 180.0;
        double lat2 = to.latitude * Math.PI / 180.0;
        double lon2 = to.longitude * Math.PI / 180.0;
        double angle = -Math.atan2(Math.sin(lon1 - lon2) * Math.cos(lat2), Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        if (angle < 0.0)
            angle += Math.PI * 2.0;

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
    }//_____________________________________________________________________________________________ onDestroy


    private void turnOnScreen() {//_________________________________________________________________ Start turnOnScreen
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }//_____________________________________________________________________________________________ End turnOnScreen


    @Override
    public void onStop() {//________________________________________________________________________ Start onStop
        super.onStop();
        mBearingProvider.stop();
    }//_____________________________________________________________________________________________ End onStop


}
