package ir.ngra.drivertrafficcontroller.views.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.cunoraz.gifview.library.GifView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import org.nocrala.tools.gis.data.esri.shapefile.shape.shapes.PolylineMShape;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.shape.ShapeConverter;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.File;
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
    //    private Marker marker;
    private BearingToNorthProvider mBearingProvider;
    private double OldBearing = 0;
    private double NewBearing = 0;
    private DisposableObserver<String> observer;
    private Integer ErrorCount = 0;
    private final double degreesPerRadian = 180.0 / Math.PI;
//    private MapView map = null;
    AlertDialog alertDialog = null;
    private float bearing = 0;
    private float BeforeBearing = 0;
    private boolean ClickForRouting = false;
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

        OSMConfig();
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                mBearingProvider = new BearingToNorthProvider(context);
                mBearingProvider.setChangeEventListener(Home.this);
                mBearingProvider.start();

            }
        });


        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
//                textChoose.setVisibility(View.GONE);
//                MarkerGif.setVisibility(View.VISIBLE);
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
//                if (getLocation) {
//                    textChoose.setVisibility(View.VISIBLE);
//                    MarkerGif.setVisibility(View.GONE);
//                }
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

            }
        });

    }//_____________________________________________________________________________________________ End onMapReady

    private void OSMConfig() {//____________________________________________________________________ OSMConfig
//
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
//
//        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
//        map = (MapView) view.findViewById(R.id.map);
//        map.setUseDataConnection(true);
//        map.setTileSource(TileSourceFactory.MAPNIK);
//        map.setBuiltInZoomControls(false);
//        map.setMultiTouchControls(true);
//        map.setMinZoomLevel(10.0);
//        map.onResume();
//
//
//        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, map);
//        mRotationGestureOverlay.setEnabled(true);
//        map.setMultiTouchControls(true);
//        map.getOverlays().add(mRotationGestureOverlay);
//
//
//        IMapController mapController = map.getController();
//
//        GeoPoint startPoint = new GeoPoint(35.830031, 50.962803);
//        mapController.animateTo(startPoint, 8.0, Long.valueOf(1000));
//        mapController.animateTo(startPoint, 18.0, Long.valueOf(1000), 0.0f);
//
//        mBearingProvider = new BearingToNorthProvider(context);
//        mBearingProvider.setChangeEventListener(Home.this);
//        mBearingProvider.start();
//
//        map.setOnHoverListener(new View.OnHoverListener() {
//            @Override
//            public boolean onHover(View v, MotionEvent event) {
//                Log.i("meri", "onHover");
//                return false;
//            }
//        });
//
//        map.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                MapMove = true;
//
//
//                if (GetDirection) {
//                    BtnMove.setVisibility(View.VISIBLE);
//                    return false;
//                }
//                map.getOverlays().remove(currentMarker);
//                currentMarker = null;
//
//                if (AccessToGoneDirection) {
//                    RelativeLayoutDirection.setVisibility(View.GONE);
//                    RemoveMarker();
//                }
//
//                return false;
//            }
//        });
//
//        MapEventsReceiver mReceive = new MapEventsReceiver() {
//            @Override
//            public boolean singleTapConfirmedHelper(GeoPoint p) {
//
//                return false;
//            }
//
//            @Override
//            public boolean longPressHelper(GeoPoint p) {
//                if (GetDirection)
//                    return false;
//                AccessToGoneDirection = false;
//                pointLatLng = new LatLng(p.getLatitude(), p.getLongitude());
//                GeoPoint point = new GeoPoint(p.getLatitude(), p.getLongitude());
//                pointMarket = new Marker(map);
//                pointMarket.setPosition(point);
//                pointMarket.setIcon(getResources().getDrawable(R.drawable.marker_point));
//                pointMarket.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                map.getOverlays().add(pointMarket);
//                map.getController().animateTo(point, map.getZoomLevelDouble(), Long.valueOf(1000));
//                AccessToRemoveMarker = true;
//                TextViewAddress.setText("درحال یافتن آدرس، شکیبا باشید ...");
//                RelativeLayoutDirection.setVisibility(View.VISIBLE);
//                ClickForRouting = false;
//                LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (RetrofitModule.isCancel)
//                            vm_home.GetAddress(p.getLatitude(), p.getLongitude());
//                    }
//                }, 500);
//                return false;
//            }
//        };
//        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
//        map.getOverlays().add(OverlayEvents);
//
//        BtnMove.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MapMove = false;
//                GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
//                IMapController mapController = map.getController();
//                mapController.animateTo(currentPoint, 19.5, Long.valueOf(1000), getBearing());
//                BtnMove.setVisibility(View.INVISIBLE);
//
//            }
//        });
//
//
//        LinearLayoutRouter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!RetrofitModule.isCancel) {
//                    RetrofitModule.isCancel = true;
//                    imageViewRouter.setVisibility(View.VISIBLE);
//                    GifViewRouter.setVisibility(View.GONE);
//                } else {
//                    imageViewRouter.setVisibility(View.GONE);
//                    GifViewRouter.setVisibility(View.VISIBLE);
//                    vm_home.Direction(CurrentLatLng.latitude, CurrentLatLng.longitude,
//                            pointLatLng.latitude, pointLatLng.longitude);
//                }
//            }
//        });
//
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
                                        RoutesLatLng = new ArrayList<>();
                                        ConfigRoute();
                                        break;
                                    case "onFailure":
                                        RetrofitModule.isCancel = true;
                                        imageViewRouter.setVisibility(View.VISIBLE);
                                        GifViewRouter.setVisibility(View.GONE);
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


    private void ConfigRoute() {//__________________________________________________________________ Start Void ConfigRoute

        BtnMove.setVisibility(View.INVISIBLE);
        MapMove = false;
        DrivingStep = 0;
        OldBearing = 0;
        NewBearing = 0;
        BeforeBearing = 0;
        PolyIndex = -1;
        routes = vm_home.getRoute();
        polylineList = new ArrayList<>();
        ModelRouteStep startStep = routes.getRoutes().get(0).getLegs().get(0).getSteps().get(0);
        ModelRouteIntersection startIntersection = startStep.getIntersections().get(0);
        GeoPoint StartPoint = new GeoPoint(startIntersection.getLocation().get(1), startIntersection.getLocation().get(0));
        bearing = CalcBearing(startStep.getManeuver().getBearing_after());
        IMapController mapController = map.getController();
        mapController.animateTo(StartPoint, 19.5, Long.valueOf(1000), getBearing());

        if (startStep.getIntersections().size() > 1) {
            DrawRoutes();
        } else {
            DrawRoutes();
        }


    }//_____________________________________________________________________________________________ End ConfigRoute


    private void DrawRoutes() {//___________________________________________________________________ DrawRoutes
        GeoPoint StartPoint = null;
        GeoPoint EndPoint = null;
        int stepCount = routes.getRoutes().get(0).getLegs().get(0).getSteps().size();
        List<LatLng> latLngPoly = new ArrayList<>();
        for (int st = 0; st < stepCount; st++) {
            ModelRouteStep step = routes.getRoutes().get(0).getLegs().get(0).getSteps().get(st);
            List<LatLng> latLngs = ML_PolyUtil.decode(step.getGeometry());
            RoutesLatLng.add(new ModelRoutesSteps(latLngs, step.getManeuver().getBearing_before(), step.getManeuver().getBearing_after()));
            for (int i = 0; i < latLngs.size() - 1; i = i + 1) {
                StartPoint = new GeoPoint(latLngs.get(i).latitude, latLngs.get(i).longitude);
                EndPoint = new GeoPoint(latLngs.get(i + 1).latitude, latLngs.get(i + 1).longitude);
                List<GeoPoint> poly = new ArrayList<>();
                poly.add(StartPoint);
                poly.add(EndPoint);
                latLngPoly.add(new LatLng(latLngs.get(i).latitude, latLngs.get(i).longitude));
                latLngPoly.add(new LatLng(latLngs.get(i + 1).latitude, latLngs.get(i + 1).longitude));
                DrawPolyLine(StartPoint, EndPoint, getResources().getColor(R.color.ML_PolyLine));
            }

        }
        StartPoint = EndPoint;
        EndPoint = new GeoPoint(pointLatLng.latitude, pointLatLng.longitude);
        DrawPolyLine(StartPoint, EndPoint, getResources().getColor(R.color.ML_PolyLine));
        latLngPoly.add(new LatLng(pointLatLng.latitude, pointLatLng.longitude));

        if (CurrentLatLng != null) {
            GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
            if (currentMarker != null) {
                map.getOverlays().remove(currentMarker);
                currentMarker = null;
            }

            currentMarker = new Marker(map);
            currentMarker.setPosition(currentPoint);
            currentMarker.setIcon(getResources().getDrawable(R.drawable.navi_marker));
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(currentMarker);

        }
        GetDirection = true;


    }//_____________________________________________________________________________________________ DrawRoutes


    private void DrawPolyLine(GeoPoint start, GeoPoint end, int color) {
        Polyline line = new Polyline(map);
//        line.addPoint(start);
//        line.addPoint(end);
//        line.setColor(color);
//        line.setWidth(21.0f);
//        map.getOverlays().add(line);

        polylineList.add(line);
    }



    @Override
    public void onCurrentLocationChange(Location loc) {


        Integer LineStep = 0;
        CurrentLocation = loc;
//        vm_home.getPublishSubject().onNext("CurrentLocation");
        if (CurrentLatLng != null) {
            OldLatLng = CurrentLatLng;
        } else
            OldLatLng = new LatLng(0, 0);

        CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
        if (MapMove)
            return;


        GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
        if (currentMarker == null) {
            currentMarker = new Marker(map);
            currentMarker.setPosition(currentPoint);
            currentMarker.setIcon(getResources().getDrawable(R.drawable.navi_marker));
            currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            map.getOverlays().add(currentMarker);
        } else {
            currentMarker.setPosition(currentPoint);
        }

        IMapController mapController = map.getController();

        if (GetDirection) {
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
                    isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngs, true, 7);
                    if (isInside) {
                        bearing = (float) GetBearing(start, end);
                        Toast.makeText(context, "isInside : " + isInside + " in Step : " + st + " in Line  : " + line, Toast.LENGTH_LONG).show();
                        CheckNext = true;
                        DrivingStep = st;
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

//                    if (PolyIndex > -1) {
//                        Polyline p = polylineList.get(PolyIndex);
//                        p.setColor(getResources().getColor(R.color.ML_PolyLineEnd));
//                    }
                    mapController.animateTo(currentPoint, 19.5, Long.valueOf(1000), CalcBearing(bearing));
                    break;
                } else
                    mapController.animateTo(currentPoint, 19.5, Long.valueOf(1000));
            }

            for (int i = 0 ; i < DrivingStep; i++) {
                for (int j = 0; j < RoutesLatLng.get(0).getLatLngs().size() - 1; j++) {
                    Polyline p = polylineList.get(0);
                    map.getOverlays().remove(p);
                    polylineList.remove(0);
                }
                RoutesLatLng.remove(i);
            }

        } else
            mapController.animateTo(currentPoint, 19.5, Long.valueOf(1000));

    }


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
        map.onPause();
    }//_____________________________________________________________________________________________ onDestroy


    private void turnOnScreen() {//_________________________________________________________________ Start turnOnScreen
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }//_____________________________________________________________________________________________ End turnOnScreen


    public float getBearing() {
        return bearing;
    }


    private float CalcBearing(float bearing) {
        float MapOrientation = map.getMapOrientation();
        return 360 - bearing;
    }

    @Override
    public void onStop() {//________________________________________________________________________ Start onStop
        super.onStop();
        mBearingProvider.stop();
        map.onPause();
    }//_____________________________________________________________________________________________ End onStop



}
