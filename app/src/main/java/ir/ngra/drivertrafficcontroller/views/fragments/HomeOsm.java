package ir.ngra.drivertrafficcontroller.views.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.location.Location;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;
import ir.ngra.drivertrafficcontroller.databinding.FragmentHomeOsmBinding;
import ir.ngra.drivertrafficcontroller.models.ModelRoute;
import ir.ngra.drivertrafficcontroller.models.ModelRouteIntersection;
import ir.ngra.drivertrafficcontroller.models.ModelRouteStep;
import ir.ngra.drivertrafficcontroller.models.ModelRoutesSteps;
import ir.ngra.drivertrafficcontroller.utility.BearingToNorthProvider;
import ir.ngra.drivertrafficcontroller.utility.MehrdadLatifiMap;
import ir.ngra.drivertrafficcontroller.utility.MyUrlTileProvider;
import ir.ngra.drivertrafficcontroller.utility.StaticFunctions;
import ir.ngra.drivertrafficcontroller.utility.polyutil.ML_PolyUtil;
import ir.ngra.drivertrafficcontroller.utility.polyutil.ML_SphericalUtil;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Home;

public class HomeOsm extends Fragment implements BearingToNorthProvider.ChangeEventListener {

    private View view;
    private Context context;
    private VM_Home vm_home;
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
    private boolean OSM = true;


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


    public HomeOsm() {//____________________________________________________________________________ Home
    }//_____________________________________________________________________________________________ Home


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {//________________________________________________ onCreateView
        context = getActivity();
        vm_home = new VM_Home(context);
        FragmentHomeOsmBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_home_osm, container, false);
        binding.setHome(vm_home);
        view = binding.getRoot();
        ButterKnife.bind(this, view);
        return view;
    }//_____________________________________________________________________________________________ onCreateView


    @Override
    public void onStart() {//_______________________________________________________________________ onStart
        super.onStart();

        ConfigFirst();
        OSMConfig();

        turnOnScreen();
        if (observer != null)
            observer.dispose();
        observer = null;
        ObserverObservable();
        RetrofitModule.isCancel = true;
    }//_____________________________________________________________________________________________ onStart


    private void ConfigFirst() {//__________________________________________________________________ ConfigFirst

        RelativeLayoutDirection.setVisibility(View.GONE);
        imageViewRouter.setVisibility(View.VISIBLE);
        GifViewRouter.setVisibility(View.GONE);
        BtnMove.setVisibility(View.INVISIBLE);
        CarMarker.setVisibility(View.GONE);
        ClickForRouting = false;
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


    private void OSMConfig() {//____________________________________________________________________ OSMConfig

        currentMarker = null;

        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        map = (MapView) view.findViewById(R.id.map);
        map.setUseDataConnection(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        map.setMinZoomLevel(10.0);
        map.onResume();

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(context, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);

        IMapController mapController = map.getController();
        GeoPoint startPoint = new GeoPoint(35.830031, 50.962803);
        mapController.animateTo(startPoint, 5.0, Long.valueOf(1000));

        mBearingProvider = new BearingToNorthProvider(context);
        mBearingProvider.setChangeEventListener(HomeOsm.this);
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

                if (GetDirection) {
                    BtnMove.setVisibility(View.VISIBLE);
                    CarMarker.setVisibility(View.INVISIBLE);
                    MapMove = true;
                    return false;
                }
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

                MapMove = false;
                GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
                IMapController mapController = map.getController();
                mapController.animateTo(currentPoint, 19.5, Long.valueOf(1000), getBearing());
                BtnMove.setVisibility(View.INVISIBLE);
                CarMarker.setVisibility(View.VISIBLE);

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

        if (currentMarker != null) {
            map.getOverlays().remove(currentMarker);
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
        GeoPoint StartPoint = null;
        GeoPoint EndPoint = null;
        int stepCount = routes.getRoutes().get(0).getLegs().get(0).getSteps().size();
        for (int st = 0; st < stepCount; st++) {
            ModelRouteStep step = routes.getRoutes().get(0).getLegs().get(0).getSteps().get(st);
            List<LatLng> latLngs = ML_PolyUtil.decode(step.getGeometry());
            RoutesLatLng.add(new ModelRoutesSteps(latLngs, step.getManeuver().getBearing_before(), step.getManeuver().getBearing_after()));
            for (int i = 0; i < latLngs.size() - 1; i = i + 1) {
                StartPoint = new GeoPoint(latLngs.get(i).latitude, latLngs.get(i).longitude);
                EndPoint = new GeoPoint(latLngs.get(i + 1).latitude, latLngs.get(i + 1).longitude);
                DrawPolyLine(StartPoint, EndPoint);
            }

        }
        StartPoint = EndPoint;
        EndPoint = new GeoPoint(pointLatLng.latitude, pointLatLng.longitude);
        DrawPolyLine(StartPoint, EndPoint);

        if (CurrentLatLng != null) {
            List<GeoPoint> point = polylineList.get(0).getActualPoints();
            GeoPoint start = new GeoPoint(point.get(0).getLatitude(), point.get(0).getLongitude());
            GeoPoint end = new GeoPoint(point.get(1).getLatitude(), point.get(1).getLongitude());
            List<GeoPoint> Start = new ArrayList<>();
            Start.add(start);
            Start.add(end);
            GeoPoint current = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
            GeoPoint car = StaticFunctions.getMarkerProjectionOnSegment(current, Start,map.getProjection());
            IMapController mapController = map.getController();
            mapController.animateTo(car, 19.5, Long.valueOf(1000), getBearing());

        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GetDirection = true;
            }
        }, 1000);


    }//_____________________________________________________________________________________________ DrawRoutes


    private void DrawPolyLine(GeoPoint start, GeoPoint end) {
        Polyline line = new Polyline(map);
        line.setGeodesic(true);
        line.addPoint(start);
        line.addPoint(end);
        line.setColor(getResources().getColor(R.color.ML_PolyLine));
        line.setWidth(40.0f);
        map.getOverlays().add(line);

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

        if (MapMove)
            return;

        Toast.makeText(context, "LocationChange", Toast.LENGTH_SHORT).show();


        if (GetDirection) {
            CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            List<GeoPoint> latLngsLine = null;
            MehrdadLatifiMap latifiMap = new MehrdadLatifiMap();
            boolean isInside = false;
            boolean CheckNext = false;
            for (int st = 0; st < RoutesLatLng.size(); st++) {
                for (int line = 0; line < RoutesLatLng.get(st).getLatLngs().size() - 1; line++) {
                    List<LatLng> latLngs = new ArrayList<>();
                    List<GeoPoint> latLngsGeoPoint = new ArrayList<>();
                    LatLng start = new LatLng(RoutesLatLng.get(st).getLatLngs().get(line).latitude, RoutesLatLng.get(st).getLatLngs().get(line).longitude);
                    latLngs.add(start);
                    latLngsGeoPoint.add(new GeoPoint(start.latitude,start.longitude));
                    LatLng end = new LatLng(RoutesLatLng.get(st).getLatLngs().get(line + 1).latitude, RoutesLatLng.get(st).getLatLngs().get(line + 1).longitude);
                    latLngs.add(end);
                    latLngsGeoPoint.add(new GeoPoint(end.latitude,end.longitude));
                    if (CheckNext)
                        isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngs, true, 4);
                    else
                        isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngs, true, 13);

                    if (isInside) {
                        bearing = (float) GetBearing(start, end);
                        Toast.makeText(context, "isInside : " + isInside + " in Step : " + st + " in Line  : " + line, Toast.LENGTH_SHORT).show();
                        CheckNext = true;
                        DrivingStep = st;
                        latLngsLine = latLngsGeoPoint;

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

                    GeoPoint current = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
                    GeoPoint car = StaticFunctions.getMarkerProjectionOnSegment(current, latLngsLine, map.getProjection());
                    IMapController mapController = map.getController();
                    mapController.animateTo(car, 19.5, Long.valueOf(1000), getBearing());
                    break;
                } else {
                    GeoPoint StartPointCenter = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
                    IMapController mapController = map.getController();
                    mapController.animateTo(StartPointCenter, 19.5, Long.valueOf(1000), getBearing());
                }
            }

            if (!isInside) {
                GetDirection = false;
                for (Polyline p : polylineList)
                    map.getOverlays().remove(p);
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
                    map.getOverlays().remove(p);
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
                        map.getOverlays().remove(p);
                    polylineList.clear();
                    RoutesLatLng.clear();
                    if (pointMarket != null)
                        map.getOverlays().remove(pointMarket);
                    GeoPoint StartPointCenter = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
                    IMapController mapController = map.getController();
                    mapController.animateTo(StartPointCenter, 18.0, Long.valueOf(1000), getBearing());
                    RelativeLayoutDirection.setVisibility(View.GONE);
                    imageViewRouter.setVisibility(View.VISIBLE);
                    GifViewRouter.setVisibility(View.GONE);
                    BtnMove.setVisibility(View.INVISIBLE);
                    ClickForRouting = false;
                    AccessToGoneDirection = true;
                    AccessToRemoveMarker = true;
                    GetDirection = false;
                    LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);

                }


        } else {

            if (CurrentLatLng == null) {
                CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                GeoPoint StartPointCenter = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
                if (currentMarker == null) {
                    currentMarker = new Marker(map);
                    currentMarker.setPosition(StartPointCenter);
                    currentMarker.setIcon(getResources().getDrawable(R.drawable.navi_marker));
                    currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    map.getOverlays().add(currentMarker);
                } else
                    currentMarker.setPosition(StartPointCenter);

                IMapController mapController = map.getController();
                mapController.animateTo(StartPointCenter, 18.0, Long.valueOf(1000), getBearing());
            }

        }

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
        return 360 - bearing;
    }


    @Override
    public void onStop() {//________________________________________________________________________ Start onStop
        super.onStop();
        mBearingProvider.stop();
        map.onPause();
    }//_____________________________________________________________________________________________ End onStop


}

