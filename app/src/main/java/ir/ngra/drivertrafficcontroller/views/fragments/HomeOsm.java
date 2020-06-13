package ir.ngra.drivertrafficcontroller.views.fragments;

import android.content.Context;
import android.graphics.Paint;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cunoraz.gifview.library.GifView;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;

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
import java.util.concurrent.TimeUnit;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.models.ModelRouteLeg;
import ir.ngra.drivertrafficcontroller.views.adabters.AdabterDestination;
import ir.ngra.drivertrafficcontroller.views.adabters.AdabterSuggestion;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;
import ir.ngra.drivertrafficcontroller.databinding.FragmentHomeOsmBinding;
import ir.ngra.drivertrafficcontroller.models.ModelAdabterSuggestion;
import ir.ngra.drivertrafficcontroller.models.ModelDrivingRoute;
import ir.ngra.drivertrafficcontroller.models.ModelRoute;
import ir.ngra.drivertrafficcontroller.models.ModelRouteManeuver;
import ir.ngra.drivertrafficcontroller.models.ModelRouteStep;
import ir.ngra.drivertrafficcontroller.models.ModelRoutesSteps;
import ir.ngra.drivertrafficcontroller.models.ModelSuggestionAddress;
import ir.ngra.drivertrafficcontroller.utility.BearingToNorthProvider;
import ir.ngra.drivertrafficcontroller.utility.MehrdadLatifiMap;
import ir.ngra.drivertrafficcontroller.utility.StaticFunctions;
import ir.ngra.drivertrafficcontroller.utility.polyutil.ML_PolyUtil;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Home;

public class HomeOsm extends Fragment implements BearingToNorthProvider.ChangeEventListener {

    private View view;
    private Context context;
    private VM_Home vm_home;
    private LatLng CurrentLatLng;
    private Location CurrentLocation;
    private Location OldLocation;
    private boolean MapMove = false;
    private BearingToNorthProvider mBearingProvider;
    //    private double OldBearing = 211;
    private double NewBearing = 0;
    private DisposableObserver<String> observer;
    private Integer ErrorCount = 0;
    private final double degreesPerRadian = 180.0 / Math.PI;
    private MapView map = null;
    private float bearing = 0;
    private boolean AccessToGoneDirection = true;
    private boolean AccessToRemoveMarker = true;
    private Marker pointMarket = null;
    private Marker currentMarker = null;
    private LatLng pointLatLng;
    private boolean GetDirection = false;
    private ModelRoute routes;
    private Integer DrivingStep;
    private List<Polyline> polylineList;
    private Integer PolyIndex;
    private List<ModelDrivingRoute> drivingRoutes;
    private Marker Real;
    private MapEventsReceiver mReceive;
    private GeoPoint CurrentCenter;


    private CompositeDisposable compositeDisposable;
    private AdabterSuggestion adabterSuggestion;
    private AdabterDestination adabterDestination;
    private GeoPoint ChooseFromSuggestion;
    private List<LatLng> LatLngDestination;
    private List<ModelSuggestionAddress> destinationAddresses;
    private Integer PositionChooseSuggestion;
    private float totalDistance;


    @BindView(R.id.TextViewMessage)
    TextView TextViewMessage;

    @BindView(R.id.LinearLayoutDestination)
    LinearLayout LinearLayoutDestination;

    @BindView(R.id.RecyclerViewSuggestion)
    RecyclerView RecyclerViewSuggestion;

    @BindView(R.id.EditTextDestination)
    EditText EditTextDestination;

    @BindView(R.id.GifViewDestination)
    GifView GifViewDestination;

    @BindView(R.id.ImageViewCloseSuggestion)
    ImageView ImageViewCloseSuggestion;

    @BindView(R.id.LinearLayoutChoose)
    LinearLayout LinearLayoutChoose;

    @BindView(R.id.RelativeLayoutChoose)
    RelativeLayout RelativeLayoutChoose;

    @BindView(R.id.RecyclerViewDestinations)
    RecyclerView RecyclerViewDestinations;

    @BindView(R.id.TextViewEndDestination)
    TextView TextViewEndDestination;

    @BindView(R.id.ToggleSwitchDestination)
    ToggleSwitch ToggleSwitchDestination;


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

    @BindView(R.id.LinearLayoutManeuver)
    LinearLayout LinearLayoutManeuver;

    @BindView(R.id.TextViewManeuverDistance)
    TextView TextViewManeuverDistance;

    @BindView(R.id.TextViewNextRoad)
    TextView TextViewNextRoad;

    @BindView(R.id.ImageViewManeuver)
    ImageView ImageViewManeuver;

    @BindView(R.id.TextViewKm)
    TextView TextViewKm;


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
        if (!StaticFunctions.isLocationEnabled(context))
            ShowDialogMessage(context.getResources().getString(R.string.gpsIsOff));

    }//_____________________________________________________________________________________________ onStart


    private void ConfigFirst() {//__________________________________________________________________ ConfigFirst

//        ErrorGetDirect = 0;
//        ErrorGetAddress = 0;
        LinearLayoutManeuver.setVisibility(View.GONE);
        RelativeLayoutDirection.setVisibility(View.GONE);
        imageViewRouter.setVisibility(View.VISIBLE);
        GifViewRouter.setVisibility(View.GONE);
        BtnMove.setVisibility(View.INVISIBLE);
        CarMarker.setVisibility(View.GONE);
        ImageViewCloseSuggestion.setVisibility(View.GONE);
        GifViewDestination.setVisibility(View.GONE);
        LinearLayoutDestination.setVisibility(View.GONE);
        RecyclerViewSuggestion.setVisibility(View.GONE);
        LinearLayoutChoose.setVisibility(View.GONE);
        TextViewMessage.setText(getResources().getString(R.string.WaitForYourLocation));
        TextViewMessage.setVisibility(View.VISIBLE);
        AccessToGoneDirection = true;
        AccessToRemoveMarker = true;
        GetDirection = false;
        LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);
        TextViewKm.setText("0");
        EditTextDestinationChange();
        SetClicks();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBearingProvider = new BearingToNorthProvider(context);
                mBearingProvider.setChangeEventListener(HomeOsm.this);
                mBearingProvider.start();
            }
        }, 1000);


    }//_____________________________________________________________________________________________ ConfigFirst


    private void EditTextDestinationChange() {//____________________________________________________ EditTextDestinationChange

        if (compositeDisposable != null) {
            compositeDisposable.dispose();
            compositeDisposable = null;
        }
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add(RxTextView.textChangeEvents(EditTextDestination)
                .skipInitialValue()
                .debounce(3000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(searchContactsTextWatcher()));

    }//_____________________________________________________________________________________________ EditTextDestinationChange


    private DisposableObserver<TextViewTextChangeEvent> searchContactsTextWatcher() {//_____________ Start searchContactsTextWatcher
        return new DisposableObserver<TextViewTextChangeEvent>() {
            @Override
            public void onNext(TextViewTextChangeEvent textViewTextChangeEvent) {
                if (EditTextDestination.getText().toString().length() == 0)
                    return;
                if (RetrofitModule.isCancel) {
                    GifViewDestination.setVisibility(View.VISIBLE);
                    ImageViewCloseSuggestion.setVisibility(View.GONE);
                    vm_home.GetSuggestionAddress(
                            EditTextDestination.getText().toString(),
                            false,
                            ErrorCount);
                }
                //publishSubject.onNext(textViewTextChangeEvent.text().toString());
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {

            }
        };
    }//_____________________________________________________________________________________________ End searchContactsTextWatcher


    private void SetClicks() {//____________________________________________________________________ SetClicks


        BtnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MapMove = false;
//                if (CurrentCenter != null)
//                    MoveCamera(CurrentCenter, 19.5, Long.valueOf(1000));
//                else {
//                    GeoPoint currentPoint = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
//                    MoveCamera(currentPoint, 19.5,Long.valueOf(1000));
//                }
                BtnMove.setVisibility(View.INVISIBLE);
                CarMarker.setVisibility(View.VISIBLE);
                LinearLayoutManeuver.setVisibility(View.VISIBLE);
                Driving(CurrentLocation);
            }
        });


        EditTextDestination.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                GifViewDestination.setVisibility(View.VISIBLE);
                ImageViewCloseSuggestion.setVisibility(View.GONE);
                return false;
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

                    if (LatLngDestination == null)
                        LatLngDestination = new ArrayList<>();
                    else
                        LatLngDestination.clear();

                    for (ModelSuggestionAddress address : destinationAddresses)
                        LatLngDestination.add(new LatLng(address.getLat(), address.getLon()));

                    imageViewRouter.setVisibility(View.GONE);
                    GifViewRouter.setVisibility(View.VISIBLE);
//                    pointLatLng = LatLngDestination.get(0);
//                    vm_home.DirectionS(
//                            CurrentLatLng.latitude,
//                            CurrentLatLng.longitude,
//                            LatLngDestination
//                    );
//                    OldBearing = GetBearing(
//                            new LatLng(OldLocation.getLatitude(),OldLocation.getLongitude()),
//                            new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude())
//                    );


                    if (ToggleSwitchDestination.getCheckedTogglePosition() == 1) {
                        LatLngDestination = StaticFunctions.sortLocations(LatLngDestination, CurrentLatLng.latitude, CurrentLatLng.longitude);
                    }

                    pointLatLng = LatLngDestination.get(0);
                    vm_home.Direction(CurrentLatLng.latitude, CurrentLatLng.longitude,
                            pointLatLng.latitude, pointLatLng.longitude, OldLocation.getLatitude(), OldLocation.getLongitude());

                }
            }
        });


        ImageViewCloseSuggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StaticFunctions.hideKeyboard(getActivity());
                EditTextDestination.setText("");
                RecyclerViewSuggestion.setVisibility(View.GONE);
                ImageViewCloseSuggestion.setVisibility(View.GONE);
            }
        });

        RelativeLayoutChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.getOverlays().clear();
                LinearLayoutChoose.setVisibility(View.GONE);
                LinearLayoutDestination.setVisibility(View.VISIBLE);
                LinearLayoutRouter.setBackgroundResource(R.drawable.button_bg);

                if (destinationAddresses == null)
                    destinationAddresses = new ArrayList<>();

                destinationAddresses.add(vm_home.getSuggestionAddresses().get(PositionChooseSuggestion));
                destinationAddresses.get(destinationAddresses.size() - 1).setLat(map.getMapCenter().getLatitude());
                destinationAddresses.get(destinationAddresses.size() - 1).setLon(map.getMapCenter().getLongitude());
                destinationAddresses.get(destinationAddresses.size() - 1).setTotalAddress(totalAddress(vm_home.getSuggestionAddresses().get(PositionChooseSuggestion)));

                SetDestinationAdabter();
                RecyclerViewSuggestion.setVisibility(View.GONE);
                EditTextDestination.setText("");
            }
        });

    }//_____________________________________________________________________________________________ SetClicks


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

        GeoPoint startPoint = new GeoPoint(35.830031, 50.962803);
        MoveCamera(startPoint, 7.0, Long.valueOf(10));


//        Polyline line = new Polyline(map);
//        line.setTitle("Central Park, NYC");
//        line.setSubDescription(Polyline.class.getCanonicalName());
//        line.setWidth(20f);
//        List<GeoPoint> pts = new ArrayList<>();
//        //here, we create a polygon, note that you need 5 points in order to make a closed polygon (rectangle)
//
//        pts.add(new GeoPoint(35.830031, 50.962803));
//        pts.add(new GeoPoint(35.829585, 50.962448));
//        pts.add(new GeoPoint(35.829341, 50.963054));
//        pts.add(new GeoPoint(35.829793, 50.963403));
//        line.setPoints(pts);
//        line.setGeodesic(true);
//        line.getOutlinePaint().setStrokeWidth(25);
//        line.getOutlinePaint().setColor(getResources().getColor(R.color.ML_PolyLine));
//        line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
//        line.getOutlinePaint().set
//        //Note, the info window will not show if you set the onclick listener
//        //line can also attach click listeners to the line
//        /*
//        line.setOnClickListener(new Polyline.OnClickListener() {
//            @Override
//            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
//                Toast.makeText(context, "Hello world!", Toast.LENGTH_LONG).show();
//                return false;
//            }
//        });*/
//        map.getOverlayManager().add(line);


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
                    LinearLayoutManeuver.setVisibility(View.GONE);
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

        mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                if (GetDirection)
                    return false;
                GifViewRouter.setVisibility(View.GONE);
                imageViewRouter.setVisibility(View.VISIBLE);
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
                TextViewAddress.setText(context.getResources().getString(R.string.WaitForYourLocation));
                RelativeLayoutDirection.setVisibility(View.VISIBLE);
                LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (RetrofitModule.isCancel)
//                            //vm_home.GetAddress(p.getLatitude(), p.getLongitude());
//                    }
//                }, 500);
                return false;
            }
        };
//        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
//        map.getOverlays().add(OverlayEvents);


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
                                    case "GetCurrentAddress":
                                        TextViewMessage.setVisibility(View.GONE);
                                        LinearLayoutDestination.setVisibility(View.VISIBLE);
                                        ErrorCount = 0;
                                        break;
                                    case "ReTryGetCurrentAddress":
                                        ErrorCount++;
                                        vm_home.GetAddress(
                                                CurrentLatLng.latitude,
                                                CurrentLatLng.longitude,
                                                true,
                                                ErrorCount
                                        );
                                        break;
                                    case "ReTrySuggestion":
                                        ErrorCount++;
                                        GifViewDestination.setVisibility(View.VISIBLE);
                                        ImageViewCloseSuggestion.setVisibility(View.GONE);
                                        vm_home.GetSuggestionAddress(
                                                EditTextDestination.getText().toString(),
                                                false,
                                                ErrorCount);
                                        break;
                                    case "onFailureSuggestion":
                                        GifViewDestination.setVisibility(View.GONE);
                                        ShowDialogMessage("");
                                        break;
                                    case "NotFoundSuggestion":
                                        GifViewDestination.setVisibility(View.GONE);
                                        ShowDialogMessage("");
                                        SetSuggestionAdabter(false);
                                        break;
                                    case "GetSuggestion":
                                        SetSuggestionAdabter(true);
                                        break;
                                    case "GetDirection":
                                        imageViewRouter.setVisibility(View.VISIBLE);
                                        GifViewRouter.setVisibility(View.GONE);
                                        LinearLayoutDestination.setVisibility(View.GONE);
                                        RetrofitModule.isCancel = true;
                                        RelativeLayoutDirection.setVisibility(View.GONE);
                                        AccessToGoneDirectionTrue();
                                        AccessToRemoveMarker = false;
                                        ConfigRoute();
                                        break;
                                    case "onFailureAddress":
                                        TextViewAddress.setText("");
                                        LinearLayoutRouter.setBackgroundResource(R.drawable.button_bg);
                                        AccessToGoneDirectionTrue();
                                        AccessToRemoveMarker = true;
                                        RetrofitModule.isCancel = true;
                                        imageViewRouter.setVisibility(View.VISIBLE);
                                        GifViewRouter.setVisibility(View.GONE);
                                        LinearLayoutDestination.setVisibility(View.VISIBLE);
                                        break;

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
                                    case "onFailureDirection":
                                        ErrorCount++;
                                        if (ErrorCount < 3) {
                                            RetrofitModule.isCancel = true;
                                            NewRouting();
                                        } else {
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
                                        }
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


    private void ShowDialogMessage(String Message) {//______________________________________________ ShowDialogMessage
        Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
    }//_____________________________________________________________________________________________ ShowDialogMessage


    private void SetDestinationAdabter() {//________________________________________________________ SetDestinationAdabter
        adabterDestination = new AdabterDestination(destinationAddresses, context, HomeOsm.this);
        RecyclerViewDestinations.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        RecyclerViewDestinations.setAdapter(adabterDestination);
    }//_____________________________________________________________________________________________ SetDestinationAdabter


    private void SetSuggestionAdabter(boolean LoadMore) {//_________________________________________ SetSuggestionAdabter

        List<ModelAdabterSuggestion> list = new ArrayList<>();
        for (ModelSuggestionAddress address : vm_home.getSuggestionAddresses()) {
            String ad = totalAddress(address);
            list.add(new ModelAdabterSuggestion(ad, false));
        }

        if (PositionChooseSuggestion == null)
            PositionChooseSuggestion = -1;

        if (LoadMore) {
            String loadmore = context.getResources().getString(R.string.LoadMore);
            list.add(new ModelAdabterSuggestion(loadmore, true));
        } else
            PositionChooseSuggestion--;

        adabterSuggestion = new AdabterSuggestion(list, context, HomeOsm.this);
        RecyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        RecyclerViewSuggestion.setAdapter(adabterSuggestion);
        RecyclerViewSuggestion.setVisibility(View.VISIBLE);
        GifViewDestination.setVisibility(View.GONE);
        ImageViewCloseSuggestion.setVisibility(View.VISIBLE);
        if (PositionChooseSuggestion < list.size())
            RecyclerViewSuggestion.scrollToPosition(PositionChooseSuggestion - 1);

    }//_____________________________________________________________________________________________ SetSuggestionAdabter


    private String totalAddress(ModelSuggestionAddress address) {//_________________________________ totalAddress
        String City = address.getAddress().getCity();
        String Neighbourhood = address.getAddress().getNeighbourhood();
        String Road = address.getAddress().getRoad();
        String district = address.getAddress().getDistrict();
        String suburb = address.getAddress().getSuburb();

        String ad = "";
        if (City != null && City.length() > 0 && !City.equalsIgnoreCase("null"))
            ad = City;
        else
            ad = district;

        if (suburb != null && suburb.length() > 0 && !suburb.equalsIgnoreCase("null"))
            ad = ad + " " + suburb;

        if (Neighbourhood != null && Neighbourhood.length() > 0 && !Neighbourhood.equalsIgnoreCase("null"))
            ad = ad + " " + Neighbourhood;

        if (Road != null && Road.length() > 0 && !Road.equalsIgnoreCase("null"))
            ad = ad + " " + Road;

        return ad;
    }//_____________________________________________________________________________________________ totalAddress


    public void ChooseAddressFromSuggestion(Integer position) {//___________________________________ ChooseAddressFromSuggestion

        PositionChooseSuggestion = position;
        vm_home.GetSuggestionAddress(
                EditTextDestination.getText().toString(),
                true,
                ErrorCount);

    }//_____________________________________________________________________________________________ ChooseAddressFromSuggestion


    public void ShowOnMap(Integer position) {//_____________________________________________________ ShowOnMap

        PositionChooseSuggestion = position;
        Double lat = vm_home.getSuggestionAddresses().get(position).getBoundingbox().get(0);
        Double lng = vm_home.getSuggestionAddresses().get(position).getBoundingbox().get(2);
//        map.getOverlays().clear();
//        Polyline line = new Polyline(map);
//        lat = vm_home.getSuggestionAddresses().get(position).getBoundingbox().get(0);
//        lng = vm_home.getSuggestionAddresses().get(position).getBoundingbox().get(3);
//        line.addPoint(new GeoPoint(lat, lng));
//        lat = vm_home.getSuggestionAddresses().get(position).getBoundingbox().get(1);
//        lng = vm_home.getSuggestionAddresses().get(position).getBoundingbox().get(2);
//        line.addPoint(new GeoPoint(lat, lng));
//        line.getOutlinePaint().setColor(getResources().getColor(R.color.ML_PolyLineEnd));
//        line.getOutlinePaint().setStrokeMiter(15);
//        line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
//        map.getOverlays().add(line);

        lat = vm_home.getSuggestionAddresses().get(position).getLat();
        lng = vm_home.getSuggestionAddresses().get(position).getLon();
        ChooseFromSuggestion = new GeoPoint(lat, lng);
        MoveCamera(ChooseFromSuggestion, 18.0, Long.valueOf(1000));

//        EditTextDestination.setText("");
//        RecyclerViewSuggestion.setVisibility(View.GONE);
        ImageViewCloseSuggestion.setVisibility(View.GONE);
        StaticFunctions.hideKeyboard(getActivity());
        LinearLayoutChoose.setVisibility(View.VISIBLE);
//        if (position == vm_home.getSuggestionAddresses().size())
//            vm_home.GetSuggestionAddress(
//                    EditTextDestination.getText().toString(),
//                    true,
//                    ErrorCount
//            );

    }//_____________________________________________________________________________________________ ShowOnMap


    public void DeleteDestination(Integer position) {//_____________________________________________ DeleteDestination
        if (destinationAddresses.size() > 0)
            destinationAddresses.remove(destinationAddresses.get(position));
        SetDestinationAdabter();
    }//_____________________________________________________________________________________________ DeleteDestination


    private void ConfigRoute() {//__________________________________________________________________ Start Void ConfigRoute

        ErrorCount = 0;
        if (currentMarker != null) {
            map.getOverlays().remove(currentMarker);
            currentMarker = null;
        }
        CarMarker.setVisibility(View.VISIBLE);
        BtnMove.setVisibility(View.INVISIBLE);
        MapMove = false;
        DrivingStep = 0;
        NewBearing = 0;
        PolyIndex = -1;
        routes = vm_home.getRoute();
        if (polylineList != null) {
            polylineList.clear();
            polylineList = null;
        }
//        if (RoutesLatLng != null) {
//            RoutesLatLng.clear();
//            RoutesLatLng = null;
//        }
//        RoutesLatLng = new ArrayList<>();
        polylineList = new ArrayList<>();
        ModelRouteStep startStep = routes.getRoutes().get(0).getLegs().get(0).getSteps().get(0);
        bearing = startStep.getManeuver().getBearing_after();
        totalDistance = 0;

        DrawRoutes();

    }//_____________________________________________________________________________________________ End ConfigRoute


    private void DrawRoutes() {//___________________________________________________________________ DrawRoutes
        GeoPoint StartPoint = null;
        GeoPoint EndPoint = null;

        //*** New Code
        if (drivingRoutes != null)
            drivingRoutes.clear();
        else
            drivingRoutes = new ArrayList<>();

        for (ModelRouteLeg leg : routes.getRoutes().get(0).getLegs()) {
            ArrayList<ModelRouteStep> steps = leg.getSteps();
            for (ModelRouteStep step : steps) {
                List<LatLng> latLng = ML_PolyUtil.decode(step.getGeometry());
                float duration = step.getDuration();
                float distance = 0;// = step.getDistance();
                String streetName = step.getName();
                ModelRouteManeuver routeManeuver = step.getManeuver();
                List<Polyline> polylines = new ArrayList<>();
                for (int i = 0; i < latLng.size() - 1; i++) {
                    StartPoint = new GeoPoint(latLng.get(i).latitude, latLng.get(i).longitude);
                    EndPoint = new GeoPoint(latLng.get(i + 1).latitude, latLng.get(i + 1).longitude);
                    distance = distance + MehrdadLatifiMap.MeasureDistance(latLng.get(i), latLng.get(i + 1));
                    polylines.add(DrawPolyLines(StartPoint, EndPoint));
                }

                totalDistance = totalDistance + distance;
                ModelDrivingRoute drivingRoute = new ModelDrivingRoute(latLng, polylines, duration, distance, streetName, routeManeuver);
                drivingRoutes.add(drivingRoute);

            }
        }


        if (CurrentLatLng != null) {
            GeoPoint current = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
            CurrentCenter = StaticFunctions.getMarkerProjectionOnSegment(current, drivingRoutes.get(0).getPolylines().get(0).getActualPoints(), map.getProjection());
            MoveCamera(CurrentCenter, 19.5, Long.valueOf(1000));
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //CalculateDistance(drivingRoutes.get(0).getDistance());
                CalculateDistance(0);
                String name = drivingRoutes.get(drivingRoutes.size() - 1).getStreetName();
                SetRoadName(context.getResources().getString(R.string.EndDestination), name, TextViewEndDestination);

                if (drivingRoutes.size() > 1) {
                    name = drivingRoutes.get(1).getStreetName();
                    name = SetImageManeuver(drivingRoutes.get(1).getRouteManeuver(), name);
                    SetRoadName(context.getResources().getString(R.string.NextManeuver), name, TextViewNextRoad);
                } else {
                    name = drivingRoutes.get(0).getStreetName();
                    name = SetImageManeuver(drivingRoutes.get(0).getRouteManeuver(), name);
                    SetRoadName(context.getResources().getString(R.string.NextManeuver), name, TextViewNextRoad);
                }


                LinearLayoutManeuver.setVisibility(View.VISIBLE);
                GetDirection = true;
            }
        }, 1000);

        //*** New Code


//        int stepCount = routes.getRoutes().get(0).getLegs().get(0).getSteps().size();
//
//
//        for (int st = 0; st < stepCount; st++) {
//            ModelRouteStep step = routes.getRoutes().get(0).getLegs().get(0).getSteps().get(st);
//            List<LatLng> latLngs = ML_PolyUtil.decode(step.getGeometry());
//            RoutesLatLng.add(new ModelRoutesSteps(latLngs, step.getManeuver().getBearing_before(), step.getManeuver().getBearing_after()));
//            for (int i = 0; i < latLngs.size() - 1; i = i + 1) {
//                StartPoint = new GeoPoint(latLngs.get(i).latitude, latLngs.get(i).longitude);
//                EndPoint = new GeoPoint(latLngs.get(i + 1).latitude, latLngs.get(i + 1).longitude);
//                DrawPolyLine(StartPoint, EndPoint);
//            }
//
//        }
//        StartPoint = EndPoint;
//        EndPoint = new GeoPoint(pointLatLng.latitude, pointLatLng.longitude);
//        DrawPolyLine(StartPoint, EndPoint);
//
//        if (CurrentLatLng != null) {
//            List<GeoPoint> point = polylineList.get(0).getActualPoints();
//            GeoPoint start = new GeoPoint(point.get(0).getLatitude(), point.get(0).getLongitude());
//            GeoPoint end = new GeoPoint(point.get(1).getLatitude(), point.get(1).getLongitude());
//            List<GeoPoint> Start = new ArrayList<>();
//            Start.add(start);
//            Start.add(end);
//            GeoPoint current = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
//            GeoPoint car = StaticFunctions.getMarkerProjectionOnSegment(current, Start, map.getProjection());
//            IMapController mapController = map.getController();
//            mapController.animateTo(car, 19.5, Long.valueOf(1000), getBearing());
//
//        }
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                GetDirection = true;
//            }
//        }, 1000);


    }//_____________________________________________________________________________________________ DrawRoutes


    private void MoveCamera(GeoPoint geoPoint, Double zoom, Long speed) {//_________________________ MoveCamera
        IMapController mapController = map.getController();
        mapController.animateTo(geoPoint, zoom, speed, getBearing());
    }//_____________________________________________________________________________________________ MoveCamera


    private void CalculateDistance(float Distance) {//______________________________________________ CalculateDistance
        String message;
        totalDistance = totalDistance - Distance;
        Integer DistanceMeter = Math.round(totalDistance);
        Integer DistanceKm = 0;
        if (totalDistance > 999.9) {
            DistanceKm = DistanceMeter / 1000;
            DistanceMeter = DistanceMeter % 1000;
        }
        if (DistanceKm > 0)
            message = context.getResources().getString(R.string.RemainingDistance) + DistanceKm + "." + DistanceMeter + " کیلومتر ";
        else
            message = context.getResources().getString(R.string.RemainingDistance) + DistanceMeter + " متر ";
        TextViewManeuverDistance.setText(message);
    }//_____________________________________________________________________________________________ CalculateDistance


    private void SetRoadName(String before, String Name, TextView textView) {//_____________________ SetRoadName
        if (Name == null)
            Name = "نامشخص";
        if (Name.equalsIgnoreCase("null"))
            Name = "نامشخص";
        if (Name.length() == 0)
            Name = "نامشخص";
        textView.setText(before + Name);
    }//_____________________________________________________________________________________________ SetRoadName


    private String SetImageManeuver(ModelRouteManeuver maneuver, String Name) {//___________________ SetImageManeuver

        String type = maneuver.getType();
        String m;
        switch (type) {
            case "on ramp":
            case "off ramp":
            case "merge":
            case "end of road":
            case "fork":
                m = maneuver.getType() + " ";
                if (maneuver.getModifier().contentEquals("left"))
                    m = m + "left";
                else
                    m = m + "right";
                break;
            case "depart":
            case "arrive":
            case "roundabout":
            case "rotary":
            case "exit roundabout":
            case "exit rotary":
                m = maneuver.getType();
                break;
            case "roundabout turn":
            case "turn":
            default:
                m = "turn " + maneuver.getModifier();
        }

        switch (m) {
            case "continue":
            case "turn straight":
                ImageViewManeuver.setImageResource(R.drawable.arrow0);
                break;
            case "turn slight right":
                ImageViewManeuver.setImageResource(R.drawable.arrow1);
                Name = "از راست حرکت کنید و وارد " + Name + " بشوید";
                break;
            case "turn right":
            case "on ramp right":
            case "continue right":
                ImageViewManeuver.setImageResource(R.drawable.arrow2);
                Name = "به راست بپیچید و وارد " + Name + " بشوید";
                break;
            case "turn sharp right":
                ImageViewManeuver.setImageResource(R.drawable.arrow3);
                Name = "به راست بپیچید و وارد " + Name + " بشوید";
                break;
            case "turn uturn":
                ImageViewManeuver.setImageResource(R.drawable.arrow4);
                Name = "دور بزنید و وارد " + Name + " بشوید";
                break;
            case "turn slight left":
                ImageViewManeuver.setImageResource(R.drawable.arrow5);
                Name = "از چپ حرکت کنید و وارد " + Name + " بشوید";
                break;
            case "turn left":
            case "on ramp left":
            case "continue left":
                ImageViewManeuver.setImageResource(R.drawable.arrow6);
                Name = "به چپ بپیچید و وارد " + Name + " بشوید";
                break;
            case "turn sharp left":
                ImageViewManeuver.setImageResource(R.drawable.arrow7);
                Name = "به چپ بپیچید و وارد " + Name + " بشوید";
                break;
            case "depart":
                ImageViewManeuver.setImageResource(R.drawable.arrow8);
                break;
            case "roundabout":
            case "exit roundabout":
            case "rotary":
            case "exit rotary":
                ImageViewManeuver.setImageResource(R.drawable.arrow10);
                Integer exit = maneuver.getExit();
                if (exit > 0)
                    Name = "در میدان از " + CountExit(exit) + " خرجی خارج شوید و وارد " + Name + " بشوید";
                else
                    Name = "از میدان خارج شده و وارد " + Name + " بشوید";
                break;
            case "arrive":
                ImageViewManeuver.setImageResource(R.drawable.arrow14);
                break;
            case "fork right":
                ImageViewManeuver.setImageResource(R.drawable.arrow18);
                Integer forkright = maneuver.getExit();
                if (forkright == null)
                    Name = "از سمت راست خارج شده و وارد " + Name + " بشوید";
                else {
                    if (forkright > 0)
                        Name = "از " + CountExit(forkright) + "خروجی سمت راست خارج شده و وارد " + Name + " بشوید";
                    else
                        Name = "از سمت راست خارج شده و وارد " + Name + " بشوید";
                }
                break;
            case "fork left":
                ImageViewManeuver.setImageResource(R.drawable.arrow19);
                Integer forkleft = maneuver.getExit();
                if (forkleft == null)
                    Name = "از سمت راست خارج شده و وارد " + Name + " بشوید";
                else {
                    if (forkleft > 0)
                        Name = "از " + CountExit(forkleft) + "خروجی سمت راست خارج شده و وارد " + Name + " بشوید";
                    else
                        Name = "از سمت راست خارج شده و وارد " + Name + " بشوید";
                }
                break;
            case "merge left":
                ImageViewManeuver.setImageResource(R.drawable.arrow20);
                Name = "از سمت چپ وارد " + Name + " بشوید";
                break;
            case "merge right":
                ImageViewManeuver.setImageResource(R.drawable.arrow21);
                Name = "از سمت راست وارد " + Name + " بشوید";
                break;
            case "end of road right":
                ImageViewManeuver.setImageResource(R.drawable.arrow22);
                Name = "به راست بپیچید و وارد " + Name + " بشوید";
                break;
            case "end of road left":
                ImageViewManeuver.setImageResource(R.drawable.arrow23);
                Name = "به چپ بپیچید و وارد " + Name + " بشوید";
                break;
            case "off ramp right":
                ImageViewManeuver.setImageResource(R.drawable.arrow24);
                Name = "از راست وارد " + Name + " بشوید";
                break;
            case "off ramp left":
                ImageViewManeuver.setImageResource(R.drawable.arrow25);
                Name = "از چپ وارد " + Name + " بشوید";
                break;

        }

        return Name;

    }//_____________________________________________________________________________________________ SetImageManeuver


    private String CountExit(Integer exit) {//______________________________________________________ CountExit
        switch (exit) {
            case 1:
                return "اولین";
            case 2:
                return "دومین";
            case 3:
                return "سومین";
            case 4:
                return "چهارمین";
            case 5:
                return "پنجمین";
            default:
                return "";
        }
    }//_____________________________________________________________________________________________ CountExit


    private void NewRouting() {//___________________________________________________________________ NewRouting
        GetDirection = false;
        map.getOverlays().clear();
        pointLatLng = LatLngDestination.get(0);
        GeoPoint point = new GeoPoint(pointLatLng.latitude, pointLatLng.longitude);
        drivingRoutes = null;
        pointMarket = null;
        pointMarket = new Marker(map);
        pointMarket.setPosition(point);
        pointMarket.setIcon(getResources().getDrawable(R.drawable.marker_point));
        pointMarket.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(pointMarket);
        TextViewAddress.setText("در حال یافتن مسیر جدید ...");
        RelativeLayoutDirection.setVisibility(View.VISIBLE);
        imageViewRouter.setVisibility(View.GONE);
        GifViewRouter.setVisibility(View.VISIBLE);
//        vm_home.DirectionS(
//                CurrentLatLng.latitude,
//                CurrentLatLng.longitude,
//                LatLngDestination
//        );
//        OldBearing = GetBearing(
//                new LatLng(OldLocation.getLatitude(),OldLocation.getLongitude()),
//                new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude())
//        );
        vm_home.Direction(CurrentLatLng.latitude, CurrentLatLng.longitude,
                pointLatLng.latitude, pointLatLng.longitude, OldLocation.getLatitude(), OldLocation.getLongitude());
    }//_____________________________________________________________________________________________ NewRouting


    private Polyline DrawPolyLines(GeoPoint start, GeoPoint end) {//________________________________ DrawPolyLines
        Polyline line = new Polyline(map);
        line.setGeodesic(true);
        line.addPoint(start);
        line.addPoint(end);
        line.setColor(getResources().getColor(R.color.ML_PolyLine));
        line.setWidth(40.0f);
        line.getOutlinePaint().setStrokeCap(Paint.Cap.ROUND);
        map.getOverlays().add(line);
        return line;
    }//_____________________________________________________________________________________________ DrawPolyLines


    private void EndDirection() {//_________________________________________________________________ EndDirection
        GetDirection = false;
        map.getOverlays().clear();
        MapEventsOverlay OverlayEvents = new MapEventsOverlay(mReceive);
        map.getOverlays().add(OverlayEvents);
        LinearLayoutManeuver.setVisibility(View.GONE);
        CarMarker.setVisibility(View.GONE);
        MoveCamera(CurrentCenter, 18.0, Long.valueOf(1000));
        destinationAddresses.clear();
        LatLngDestination.clear();
        adabterDestination = null;
        adabterSuggestion = null;
        LinearLayoutDestination.setVisibility(View.VISIBLE);
        SetDestinationAdabter();
    }//_____________________________________________________________________________________________ EndDirection


    private void Driving(Location loc) {//__________________________________________________________ Driving
        float speed = loc.getSpeed();
        speed = speed * 3.6f;
        TextViewKm.setText(Math.round(speed) + "");

        if (GetDirection) { //______________________________________________________ if GetDirection
            OldLocation = CurrentLocation;
            CurrentLocation = loc;
            CurrentLatLng = new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude());
            float distance = MehrdadLatifiMap.MeasureDistance(
                    new LatLng(OldLocation.getLatitude(), OldLocation.getLongitude())
                    , CurrentLatLng
            );
//                            Distance = MehrdadLatifiMap.MeasureDistance(CurrentLatLng, EndPolyLine);
//                            Distance = drivingRoutes.get(currentStep).getDistance() - Distance;
            CalculateDistance(distance);
            boolean isInsideManeuver = false;
            boolean WhileControl = true;
            int currentStep = -1;
            int currentPolyLine = -1;
            boolean WhilePolyLine = true;

            GeoPoint current = new GeoPoint(CurrentLocation.getLatitude(), CurrentLocation.getLongitude());
            if (Real == null) {
                Real = new Marker(map);
                Real.setPosition(current);
                Real.setIcon(getResources().getDrawable(R.drawable.marker_point_green));
                Real.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(Real);
            } else
                Real.setPosition(current);

            while (WhileControl) {//___________________________________________ while (WhileControl)
                currentStep++;
                WhilePolyLine = true;
                currentPolyLine = -1;

                while (WhilePolyLine) {
                    currentPolyLine++;
                    if (drivingRoutes.get(currentStep).getPolylines().size() == 0) {
                        currentStep = -1;
                        drivingRoutes.remove(0);
                        WhilePolyLine = false;
                        break;
                    }
                    Polyline polyline = drivingRoutes.get(currentStep).getPolylines().get(currentPolyLine);
                    List<LatLng> latLngInside = new ArrayList<>();
                    latLngInside.add(new LatLng(polyline.getActualPoints().get(0).getLatitude(), polyline.getActualPoints().get(0).getLongitude()));
                    latLngInside.add(new LatLng(polyline.getActualPoints().get(1).getLatitude(), polyline.getActualPoints().get(1).getLongitude()));
//
//                    GeoPoint test = StaticFunctions.getMarkerProjectionOnSegment(current, polyline.getActualPoints(), map.getProjection());
//                    LatLng curentTest = new LatLng(test.getLatitude(), test.getLongitude());
//                    isInsideManeuver = ML_PolyUtil.isLocationOnPath(curentTest, latLngInside, true, 7);

                    isInsideManeuver = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngInside, true, 17);

                    if (isInsideManeuver) {//__________________________________if (isInsideManeuver)
                        bearing = (float) GetBearing(latLngInside.get(0), latLngInside.get(1));
                        CurrentCenter = StaticFunctions.getMarkerProjectionOnSegment(current, polyline.getActualPoints(), map.getProjection());
                        CurrentLatLng = new LatLng(CurrentCenter.getLatitude(), CurrentCenter.getLongitude());
//                        if (speed < 5)
                        MoveCamera(CurrentCenter, 19.5, Long.valueOf(1050));
//                        else if (speed < 10)
//                            MoveCamera(CurrentCenter, 19.5, Long.valueOf(250));
//                        else if (speed < 20)
//                            MoveCamera(CurrentCenter, 19.5, Long.valueOf(100));
//                        else
//                            MoveCamera(CurrentCenter, 19.5, Long.valueOf(10));

                        LatLng EndPolyLine = latLngInside.get(1);
                        if (currentPolyLine + 1 < drivingRoutes.get(currentStep).getPolylines().size()) {
                            Polyline polylineNext = drivingRoutes.get(currentStep).getPolylines().get(currentPolyLine + 1);
                            EndPolyLine = new LatLng(polylineNext.getActualPoints().get(0).getLatitude(), polylineNext.getActualPoints().get(0).getLongitude());
                        } else {
                            if (currentStep + 1 < drivingRoutes.size()) {
                                Polyline polylineNext;
                                if (drivingRoutes.get(currentStep + 1).getPolylines() != null && drivingRoutes.get(currentStep + 1).getPolylines().size() > 0) {
                                    polylineNext = drivingRoutes.get(currentStep + 1).getPolylines().get(0);
                                    EndPolyLine = new LatLng(polylineNext.getActualPoints().get(0).getLatitude(), polylineNext.getActualPoints().get(0).getLongitude());
                                }
                            }
                        }

                        float Distance = MehrdadLatifiMap.MeasureDistance(CurrentLatLng, EndPolyLine);
                        if (Distance > 6) {

                            String name = drivingRoutes.get(drivingRoutes.size() - 1).getStreetName();
                            SetRoadName(context.getResources().getString(R.string.EndDestination), name, TextViewEndDestination);
                            if (drivingRoutes.size() > 1) {
                                name = drivingRoutes.get(1).getStreetName();
                                name = SetImageManeuver(drivingRoutes.get(1).getRouteManeuver(), name);
                            } else {
                                name = drivingRoutes.get(0).getStreetName();
                                name = SetImageManeuver(drivingRoutes.get(0).getRouteManeuver(), name);
                            }
                            SetRoadName("مسیر بعدی : ", name, TextViewNextRoad);

                            WhileControl = false;
                            WhilePolyLine = false;
                            break;
                        } else {
                            //map.getOverlays().remove(polyline);
                            polyline.setColor(getResources().getColor(R.color.ML_PolyLineEnd));
                            //map.getOverlays().add(polyline);
                            drivingRoutes.get(currentStep).getPolylines().remove(polyline);
                            if (drivingRoutes.get(currentStep).getPolylines().size() == 0) {
                                currentStep = -1;
                                drivingRoutes.remove(0);
                                WhilePolyLine = false;
                                break;
                            } else {
                                currentPolyLine = -1;
                                continue;
                            }
                        }

                    } else {//_________________________________________________if (isInsideManeuver)
                        //map.getOverlays().remove(polyline);
                        polyline.setColor(getResources().getColor(R.color.ML_PolyLineEnd));
                        //map.getOverlays().add(polyline);
                        drivingRoutes.get(currentStep).getPolylines().remove(polyline);
                        if (drivingRoutes.get(currentStep).getPolylines().size() == 0) {
                            currentStep = -1;
                            drivingRoutes.remove(0);
                            WhilePolyLine = false;
                            break;
                        } else {
                            currentPolyLine = -1;
                            continue;
                        }
                    }//___________________________________________________else if (isInsideManeuver)
                }

                if (drivingRoutes.size() < 2)
                    break;

            }//________________________________________________________________ while (WhileControl)

            if (drivingRoutes.size() < 2) {
                if (!isInsideManeuver) {
                    if (LatLngDestination.size() > 0) {
                        NewRouting();
                    } else {
                        EndDirection();
                    }
                }
            } else if (drivingRoutes.size() == 2) {
                Polyline polylineEnd = drivingRoutes.get(0).getPolylines().get(drivingRoutes.get(0).getPolylines().size() - 1);
                LatLng End = new LatLng(polylineEnd.getActualPoints().get(1).getLatitude(), polylineEnd.getActualPoints().get(1).getLongitude());
                float Distance = MehrdadLatifiMap.MeasureDistance(CurrentLatLng, End);
                if (Distance < 30) {
                    LatLngDestination.remove(0);
                    if (LatLngDestination.size() > 0) {
                        NewRouting();
                    } else {
                        EndDirection();
                    }
                }
            }
//            else {
//                if (!isInsideManeuver)
//                    NewRouting();
//            }


        } else {//__________________________________________________________________ if GetDirection
            if (CurrentLocation == null) {
                OldLocation = loc;
                CurrentLocation = loc;
                CurrentLatLng = new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude());
                GeoPoint current = new GeoPoint(CurrentLocation.getLatitude(), CurrentLocation.getLongitude());
                vm_home.GetAddress(CurrentLatLng.latitude, CurrentLatLng.longitude, true, ErrorCount);
//                if (currentMarker == null) {
//                    currentMarker = new Marker(map);
//                    currentMarker.setPosition(current);
//                    currentMarker.setIcon(getResources().getDrawable(R.drawable.navi_marker));
//                    currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                    map.getOverlays().add(currentMarker);
//                } else
//                    currentMarker.setPosition(current);
                MoveCamera(CurrentCenter, 18.0, Long.valueOf(1000));
            } else {
                OldLocation = CurrentLocation;
                CurrentLocation = loc;
                CurrentLatLng = new LatLng(CurrentLocation.getLatitude(), CurrentLocation.getLongitude());
            }
        }//____________________________________________________________________ else if GetDirection

    }//_____________________________________________________________________________________________ Driving


    @Override
    public void onCurrentLocationChange(Location loc) {//___________________________________________ onCurrentLocationChange

        //*** New Code

        float speed = loc.getSpeed();
        speed = speed * 3.6f;
        TextViewKm.setText(Math.round(speed) + "");

        if (MapMove) {
            CurrentLocation = loc;
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Driving(loc);
            }
        });


        //*** New Code

//
//        Integer LineStep = 0;
//        CurrentLocation = loc;
////        vm_home.getPublishSubject().onNext("CurrentLocation");
//        if (CurrentLatLng != null) {
//            OldLatLng = CurrentLatLng;
//        } else
//            OldLatLng = new LatLng(0, 0);
//
//        if (MapMove)
//            return;
//
//        Toast.makeText(context, "LocationChange", Toast.LENGTH_SHORT).show();
//
//
//        if (GetDirection) {
//            CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
//            List<GeoPoint> latLngsLine = null;
//            MehrdadLatifiMap latifiMap = new MehrdadLatifiMap();
//            boolean isInside = false;
//            boolean CheckNext = false;
//            for (int st = 0; st < RoutesLatLng.size(); st++) {
//                for (int line = 0; line < RoutesLatLng.get(st).getLatLngs().size() - 1; line++) {
//                    List<LatLng> latLngs = new ArrayList<>();
//                    List<GeoPoint> latLngsGeoPoint = new ArrayList<>();
//                    LatLng start = new LatLng(RoutesLatLng.get(st).getLatLngs().get(line).latitude, RoutesLatLng.get(st).getLatLngs().get(line).longitude);
//                    latLngs.add(start);
//                    latLngsGeoPoint.add(new GeoPoint(start.latitude, start.longitude));
//                    LatLng end = new LatLng(RoutesLatLng.get(st).getLatLngs().get(line + 1).latitude, RoutesLatLng.get(st).getLatLngs().get(line + 1).longitude);
//                    latLngs.add(end);
//                    latLngsGeoPoint.add(new GeoPoint(end.latitude, end.longitude));
//                    if (CheckNext)
//                        isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngs, true, 4);
//                    else
//                        isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, latLngs, true, 13);
//
//                    if (isInside) {
//                        bearing = (float) GetBearing(start, end);
//                        Toast.makeText(context, "isInside : " + isInside + " in Step : " + st + " in Line  : " + line, Toast.LENGTH_SHORT).show();
//                        CheckNext = true;
//                        DrivingStep = st;
//                        latLngsLine = latLngsGeoPoint;
//
//                    } else {
//                        if (CheckNext) {
//                            isInside = true;
//                            CheckNext = false;
//                            break;
//                        }
//                    }
//
//                }
//
//
//                if (st == RoutesLatLng.size() - 1) {
//                    CheckNext = false;
//                }
//
//                if (isInside && !CheckNext) {
//
//                    GeoPoint current = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
//                    GeoPoint car = StaticFunctions.getMarkerProjectionOnSegment(current, latLngsLine, map.getProjection());
//                    IMapController mapController = map.getController();
//                    mapController.animateTo(car, 19.5, Long.valueOf(1000), getBearing());
//                    break;
//                } else {
//                    GeoPoint StartPointCenter = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
//                    IMapController mapController = map.getController();
//                    mapController.animateTo(StartPointCenter, 19.5, Long.valueOf(1000), getBearing());
//                }
//            }
//
//            if (!isInside) {
//                GetDirection = false;
//                for (Polyline p : polylineList)
//                    map.getOverlays().remove(p);
//                TextViewAddress.setText("در حال یافتن مسیر جدید ...");
//                RelativeLayoutDirection.setVisibility(View.VISIBLE);
//                imageViewRouter.setVisibility(View.GONE);
//                GifViewRouter.setVisibility(View.VISIBLE);
//                vm_home.Direction(CurrentLatLng.latitude, CurrentLatLng.longitude,
//                        pointLatLng.latitude, pointLatLng.longitude);
//            }
//
//            for (int i = 0; i < DrivingStep; i++) {
//                for (int j = 0; j < RoutesLatLng.get(0).getLatLngs().size() - 1; j++) {
//                    Polyline p = polylineList.get(0);
//                    map.getOverlays().remove(p);
//                    polylineList.remove(0);
//                }
//                RoutesLatLng.remove(i);
//            }
//
//
//            float[] results = new float[1];
//            Location.distanceBetween(CurrentLatLng.latitude, CurrentLatLng.longitude,
//                    pointLatLng.latitude, pointLatLng.longitude, results);
//            if (results.length > 0)
//                if (results[0] < 20) {
//                    for (Polyline p : polylineList)
//                        map.getOverlays().remove(p);
//                    polylineList.clear();
//                    RoutesLatLng.clear();
//                    if (pointMarket != null)
//                        map.getOverlays().remove(pointMarket);
//                    GeoPoint StartPointCenter = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
//                    IMapController mapController = map.getController();
//                    mapController.animateTo(StartPointCenter, 18.0, Long.valueOf(1000), getBearing());
//                    RelativeLayoutDirection.setVisibility(View.GONE);
//                    imageViewRouter.setVisibility(View.VISIBLE);
//                    GifViewRouter.setVisibility(View.GONE);
//                    BtnMove.setVisibility(View.INVISIBLE);
//                    AccessToGoneDirection = true;
//                    AccessToRemoveMarker = true;
//                    GetDirection = false;
//                    LinearLayoutRouter.setBackgroundResource(R.drawable.dw_button_disable);
//
//                }
//
//
//        } else {
//
////            if (CurrentLatLng == null) {
//                CurrentLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
//                GeoPoint StartPointCenter = new GeoPoint(CurrentLatLng.latitude, CurrentLatLng.longitude);
//                if (currentMarker == null) {
//                    currentMarker = new Marker(map);
//                    currentMarker.setPosition(StartPointCenter);
//                    currentMarker.setIcon(getResources().getDrawable(R.drawable.navi_marker));
//                    currentMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                    map.getOverlays().add(currentMarker);
//                } else
//                    currentMarker.setPosition(StartPointCenter);
//
//                bearing = 211;
//
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (pointMarket == null) {
//                            Polyline line = new Polyline(map);
//                            line.setGeodesic(true);
//                            line.addPoint(new GeoPoint(35.830031, 50.962803));
//                            line.addPoint(new GeoPoint(35.829583, 50.962436));
//                            line.setColor(getResources().getColor(R.color.ML_PolyLine));
//                            line.setWidth(40.0f);
//                            map.getOverlays().add(line);
//                        }
//
//                        List<LatLng> lngs = new ArrayList<>();
//                        lngs.add(new LatLng(35.830031, 50.962803));
//                        lngs.add(new LatLng(35.829583, 50.962436));
//                        boolean isInside = ML_PolyUtil.isLocationOnPath(CurrentLatLng, lngs, true, 20);
//                        if (isInside) {
//                            List<GeoPoint> te = new ArrayList<>();
//                            te.add(new GeoPoint(35.830031, 50.962803));
//                            te.add(new GeoPoint(35.829583, 50.962436));
//                            GeoPoint car = StaticFunctions.getMarkerProjectionOnSegment(StartPointCenter, te, map.getProjection());
//                            if (pointMarket == null) {
//                                pointMarket = new Marker(map);
//                                pointMarket.setPosition(car);
//                                pointMarket.setIcon(getResources().getDrawable(R.drawable.marker_point));
//                                pointMarket.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                                map.getOverlays().add(pointMarket);
//                                map.invalidate();
//                            } else {
//                                pointMarket.setPosition(car);
//                            }
//                        } else
//                            if (pointMarket != null) {
//                                map.getOverlays().remove(pointMarket);
//                                pointMarket = null;
//                            }
//
//                    }
//                },1500);
//
//
//                IMapController mapController = map.getController();
//                mapController.animateTo(StartPointCenter, 19.0, Long.valueOf(1000), getBearing());
////            }
//
//        }

    }//_____________________________________________________________________________________________ onCurrentLocationChange


    @Override
    public void onBearingChanged(double bearing) {
        //OldBearing = bearing;
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

