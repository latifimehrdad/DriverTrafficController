package ir.ngra.drivertrafficcontroller.viewmodels.fragments;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.subjects.PublishSubject;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitComponent;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;
import ir.ngra.drivertrafficcontroller.models.ModelAddress;
import ir.ngra.drivertrafficcontroller.models.ModelGetAddress;
import ir.ngra.drivertrafficcontroller.models.ModelLocation;
import ir.ngra.drivertrafficcontroller.models.ModelLocations;
import ir.ngra.drivertrafficcontroller.models.ModelResponcePrimery;
import ir.ngra.drivertrafficcontroller.models.ModelRoute;
import ir.ngra.drivertrafficcontroller.models.ModelSuggestionAddress;
import ir.ngra.drivertrafficcontroller.utility.DeviceTools;
import ir.ngra.drivertrafficcontroller.views.application.TrafficController;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ir.ngra.drivertrafficcontroller.utility.StaticFunctions.CheckResponse;
import static ir.ngra.drivertrafficcontroller.utility.StaticFunctions.GetAuthorization;

public class VM_Home {


    private Context context;
    private String MessageResponse;
    private PublishSubject<String> publishSubject;
    private ModelGetAddress address;
    private String TextAddress;
    private ModelRoute route;
    private ModelGetAddress currentAddress;
    private List<ModelSuggestionAddress> suggestionAddresses;

    public VM_Home(Context context) {//_____________________________________________________________ VM_Home
        this.context = context;
        publishSubject = PublishSubject.create();
    }//_____________________________________________________________________________________________ VM_Home


    public void GetSuggestionAddress(String address, boolean LoadMore, int ErrorCount) {//__________ GetSuggestionAddress

        if (suggestionAddresses == null)
            suggestionAddresses = new ArrayList<>();

        RetrofitComponent retrofitComponent = TrafficController
                .getApplication(context)
                .getRetrofitComponent();

        String q = getCityOfCurrentAddress();
        String[] separated = address.split(" ");
        for (String word : separated)
            q = q + "+" + word;

        String url = "https://nominatim.openstreetmap.org/search?format=json&addressdetails=1&limit=50&q=";
        url = url + q;

        if (LoadMore) {
            url = url + "&exclude_place_ids=";
            for (int i = 0; i<suggestionAddresses.size(); i++)
                if (i == suggestionAddresses.size()-1)
                    url = url+suggestionAddresses.get(i).getPlace_id();
                else
                    url = url+suggestionAddresses.get(i).getPlace_id()+",";
        } else
            suggestionAddresses.clear();

        retrofitComponent
                .getRetrofitApiInterface()
                .getSuggestionAddress(url)
                .enqueue(new Callback<List<ModelSuggestionAddress>>() {
                    @Override
                    public void onResponse(Call<List<ModelSuggestionAddress>> call, Response<List<ModelSuggestionAddress>> response) {
                        if (response.body() == null) {
                            if (ErrorCount > 3)
                                publishSubject.onNext("ReTrySuggestion");
                            else
                                publishSubject.onNext("onFailureSuggestion");

                        } else {
                            if (response.body().size() == 0)
                                publishSubject.onNext("NotFoundSuggestion");
                            else {
                                for (ModelSuggestionAddress address1 : response.body())
                                    suggestionAddresses.add(address1);
                                publishSubject.onNext("GetSuggestion");
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<List<ModelSuggestionAddress>> call, Throwable t) {
                        if (ErrorCount > 3)
                            publishSubject.onNext("ReTrySuggestion");
                        else
                            publishSubject.onNext("onFailureSuggestion");
                    }
                });

    }//_____________________________________________________________________________________________ GetSuggestionAddress


    public void GetAddress(double lat, double lon, boolean current, int ErrorCount) {//_____________ Start GetAddress

        RetrofitComponent retrofitComponent = TrafficController
                .getApplication(context)
                .getRetrofitComponent();

        String url = "http://nominatim.openstreetmap.org/reverse?format=json&lat=" + lat + "&lon=" + lon + "&zoom=22&addressdetails=5";

        retrofitComponent
                .getRetrofitApiInterface()
                .getAddress(url)
                .enqueue(new Callback<ModelGetAddress>() {
                    @Override
                    public void onResponse(Call<ModelGetAddress> call, Response<ModelGetAddress> response) {
                        if (response.body() == null) {

                            if (ErrorCount > 2) {
                                if (current) {
                                    currentAddress = new ModelGetAddress();
                                    currentAddress.setLat(String.valueOf(lat));
                                    currentAddress.setLon(String.valueOf(lon));
                                    currentAddress.setAddress(new ModelAddress());
                                    currentAddress.getAddress().setCity(context.getString(R.string.DefaultCity));
                                    publishSubject.onNext("GetCurrentAddress");
                                } else {
                                    address = new ModelGetAddress();
                                    address.setLat(String.valueOf(lat));
                                    address.setLon(String.valueOf(lon));
                                    publishSubject.onNext("onFailureAddress");
                                }
                            } else {
                                if (current)
                                    publishSubject.onNext("ReTryGetCurrentAddress");
                                else
                                    publishSubject.onNext("onFailureAddress");
                            }

                        } else {
                            if (current) {
                                if (response.body().getAddress() == null) {
                                    if (ErrorCount > 2) {
                                        currentAddress = response.body();
                                        currentAddress.getAddress().setCity(context.getString(R.string.DefaultCity));
                                        publishSubject.onNext("GetCurrentAddress");
                                    } else
                                        publishSubject.onNext("ReTryGetCurrentAddress");
                                } else {
                                    currentAddress = response.body();
                                    publishSubject.onNext("GetCurrentAddress");
                                }
                            } else {
                                address = response.body();
                                if (address.getAddress() == null) {
                                    address.setLat(String.valueOf(lat));
                                    address.setLon(String.valueOf(lon));
                                    publishSubject.onNext("onFailureAddress");
                                } else
                                    SetAddress();
                            }

                        }
                    }

                    @Override
                    public void onFailure(Call<ModelGetAddress> call, Throwable t) {
                        if (ErrorCount > 2) {
                            if (current) {
                                currentAddress = new ModelGetAddress();
                                currentAddress.setLat(String.valueOf(lat));
                                currentAddress.setLon(String.valueOf(lon));
                                currentAddress.setAddress(new ModelAddress());
                                currentAddress.getAddress().setCity(context.getString(R.string.DefaultCity));
                                publishSubject.onNext("GetCurrentAddress");
                            } else {
                                address = new ModelGetAddress();
                                address.setLat(String.valueOf(lat));
                                address.setLon(String.valueOf(lon));
                                publishSubject.onNext("onFailureAddress");
                            }
                        } else {
                            if (current)
                                publishSubject.onNext("ErrorGetCurrentAddress");
                            else
                                publishSubject.onNext("onFailureAddress");
                        }
                    }
                });

    }//_____________________________________________________________________________________________ End GetAddress


    private void SetAddress() {//___________________________________________________________________ Start SetAddress

        ModelGetAddress GetAddress = address;
        if (GetAddress != null && GetAddress.getAddress() != null) {
            StringBuilder address = new StringBuilder();

            String country = GetAddress.getAddress().getCountry();
            if (country != null &&
                    !country.equalsIgnoreCase("null") &&
                    !country.equalsIgnoreCase("")) {
                address.append(country);
                address.append(" ");
            }

            String state = GetAddress.getAddress().getState();
            if (state != null &&
                    !state.equalsIgnoreCase("null") &&
                    !state.equalsIgnoreCase("")) {
                address.append(state);
                address.append(" ");
            }

            String county = GetAddress.getAddress().getCounty();
            if (county != null &&
                    !county.equalsIgnoreCase("null") &&
                    !county.equalsIgnoreCase("")) {
                address.append(county);
                address.append(" ");
            }

            String city = GetAddress.getAddress().getCity();
            if (city != null &&
                    !city.equalsIgnoreCase("null") &&
                    !city.equalsIgnoreCase("")) {
                address.append("شهر");
                address.append(" ");
                address.append(city);
                address.append(" ");
            }

            String neighbourhood = GetAddress.getAddress().getNeighbourhood();
            if (neighbourhood != null &&
                    !neighbourhood.equalsIgnoreCase("null") &&
                    !neighbourhood.equalsIgnoreCase("")) {
                address.append(neighbourhood);
                address.append(" ");
            }

            String suburb = GetAddress.getAddress().getSuburb();
            if (suburb != null &&
                    !suburb.equalsIgnoreCase("null") &&
                    !suburb.equalsIgnoreCase("") &&
                    !suburb.equalsIgnoreCase(neighbourhood)) {
                address.append(suburb);
                address.append(" ");
            }

            String road = GetAddress.getAddress().getRoad();
            if (road != null &&
                    !road.equalsIgnoreCase("null") &&
                    !road.equalsIgnoreCase("")) {
                address.append("خیابان");
                address.append(" ");
                address.append(road);
                address.append(" ");
            }

            TextAddress = address.toString();
        } else {
            TextAddress = "";
        }

        publishSubject.onNext("GetAddress");

    }//_____________________________________________________________________________________________ End SetAddress


    public void DirectionS(double CurrentLat, double CurrentLon, List<LatLng> latLngs) {//__________ DirectionS

        RetrofitModule.isCancel = false;
        RetrofitComponent retrofitComponent =
                TrafficController.getApplication(context)
                        .getRetrofitComponent();

        String url = "https://routing.openstreetmap.de/routed-car/route/v1/driving/" + CurrentLon + "," + CurrentLat + ";";
        for (int i = 0; i < latLngs.size(); i++)
            if (i == latLngs.size() -1)
                url  = url + latLngs.get(i).longitude + "," + latLngs.get(i).latitude;
            else
                url  = url + latLngs.get(i).longitude + "," + latLngs.get(i).latitude + ";";

        url = url + "?overview=false&geometries=polyline&steps=true&alternatives=true";

        retrofitComponent.getRetrofitApiInterface()
                .getRoute(url)
                .enqueue(new Callback<ModelRoute>() {
                    @Override
                    public void onResponse(Call<ModelRoute> call, Response<ModelRoute> response) {
                        route = response.body();
                        if (route == null || route.getRoutes() == null || route.getRoutes().size() == 0)
                            publishSubject.onNext("onFailureDirection");
                        else
                            publishSubject.onNext("GetDirection");
                    }

                    @Override
                    public void onFailure(Call<ModelRoute> call, Throwable t) {
                        publishSubject.onNext("onFailureDirection");
                    }
                });
    }//_____________________________________________________________________________________________ DirectionS


    public void Direction(double CurrentLat, double CurrentLon, double lat, double lon) {//_________ Direction

        RetrofitModule.isCancel = false;
        RetrofitComponent retrofitComponent =
                TrafficController.getApplication(context)
                        .getRetrofitComponent();

        String url = "https://routing.openstreetmap.de/routed-car/route/v1/driving/" + CurrentLon + "," + CurrentLat + ";" + lon + "," + lat + "?overview=false&geometries=polyline&steps=true&alternatives=true";

        retrofitComponent.getRetrofitApiInterface()
                .getRoute(url)
                .enqueue(new Callback<ModelRoute>() {
                    @Override
                    public void onResponse(Call<ModelRoute> call, Response<ModelRoute> response) {
                        route = response.body();
                        if (route == null || route.getRoutes() == null || route.getRoutes().size() == 0)
                            publishSubject.onNext("onFailureDirection");
                        else
                            publishSubject.onNext("GetDirection");
                    }

                    @Override
                    public void onFailure(Call<ModelRoute> call, Throwable t) {
                        publishSubject.onNext("onFailureDirection");
                    }
                });
    }//_____________________________________________________________________________________________ Direction


    public void SendLocatoinToServer(Location location) {//_________________________________________ Start SendLocatoinToServer

        ArrayList<ModelLocation> list = new ArrayList<>();
        list.add(new ModelLocation(
                location.getLatitude(),
                location.getLongitude(),
                location.getAltitude(),
                location.getSpeed(),
                new Date(),
                location.getAccuracy(),
                true));

        ModelLocations lo = new ModelLocations(list);

        RetrofitComponent retrofitComponent =
                TrafficController
                        .getApplication(context)
                        .getRetrofitComponent();

        DeviceTools deviceTools = new DeviceTools(context);
        String imei = deviceTools.getIMEI();
        String Authorization = GetAuthorization(context);

        retrofitComponent
                .getRetrofitApiInterface()
                .DeviceLogs(
                        imei,
                        Authorization,
                        lo
                )
                .enqueue(new Callback<ModelResponcePrimery>() {
                    @Override
                    public void onResponse(Call<ModelResponcePrimery> call, Response<ModelResponcePrimery> response) {
                        String MessageResponse = CheckResponse(response, true);
                        if (MessageResponse == null) {
                            publishSubject.onNext("SendSuccess");
                        } else {
                            //RefreshToken();
                        }
                    }

                    @Override
                    public void onFailure(Call<ModelResponcePrimery> call, Throwable t) {
                        publishSubject.onNext("SendError");
                    }
                });

    }//_____________________________________________________________________________________________ End SendLocatoinToServer


    public String getCityOfCurrentAddress() {//_____________________________________________________ getCityOfCurrentAddress

        String city = currentAddress.getAddress().getCity();
        if (city != null &&
                !city.equalsIgnoreCase("null") &&
                !city.equalsIgnoreCase("")) {
        } else
            city = context.getString(R.string.DefaultCity);

        city = city.replace("شهرستان " , "");

        return city;

    }//_____________________________________________________________________________________________ getCityOfCurrentAddress


    public String getMessageResponse() {//__________________________________________________________ getMessageResponse
        return MessageResponse;
    }//_____________________________________________________________________________________________ getMessageResponse

    public PublishSubject<String> getPublishSubject() {//___________________________________________ getPublishSubject
        return publishSubject;
    }//_____________________________________________________________________________________________ getPublishSubject


    public List<ModelSuggestionAddress> getSuggestionAddresses() {
        return suggestionAddresses;
    }

    public ModelGetAddress getAddress() {
        return address;
    }

    public String getTextAddress() {
        return TextAddress;
    }

    public ModelRoute getRoute() {
        return route;
    }
}
