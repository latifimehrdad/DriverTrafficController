package ir.ngra.drivertrafficcontroller.viewmodels.fragments;

import android.content.Context;
import android.content.SharedPreferences;;
import io.reactivex.subjects.PublishSubject;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitApis;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitComponent;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;
import ir.ngra.drivertrafficcontroller.models.ModelResponcePrimery;
import ir.ngra.drivertrafficcontroller.models.ModelToken;
import ir.ngra.drivertrafficcontroller.utility.DeviceTools;
import ir.ngra.drivertrafficcontroller.views.application.TrafficController;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static ir.ngra.drivertrafficcontroller.utility.StaticFunctions.CheckResponse;
import static ir.ngra.drivertrafficcontroller.utility.StaticFunctions.GetAuthorization;


public class VM_Verify {

    private Context context;
    private PublishSubject<String> Observables;
    private String MessageResponcse;
    private ModelToken modelToken;

    public VM_Verify(Context context) {//___________________________________________________ Start VM_FragmentVerify
        this.context = context;
        Observables = PublishSubject.create();
    }//_____________________________________________________________________________________________ End VM_FragmentVerify


    public void SendNumber(String PhoneNumber) {//__________________________________________________ Start SendNumber

        RetrofitModule.isCancel = false;
        RetrofitComponent retrofitComponent =
                TrafficController
                        .getApplication(context)
                        .getRetrofitComponent();

        DeviceTools deviceTools = new DeviceTools(context);
        String Authorization = GetAuthorization(context);
        String imei = deviceTools.getIMEI();

        retrofitComponent
                .getRetrofitApiInterface()
                .SendPhoneNumber(
                        PhoneNumber,
                        1,
                        imei,
                        Authorization
                )
                .enqueue(new Callback<ModelResponcePrimery>() {
                    @Override
                    public void onResponse(Call<ModelResponcePrimery> call, Response<ModelResponcePrimery> response) {
                        if (RetrofitModule.isCancel)
                            return;
                        MessageResponcse = CheckResponse(response, true);
                        if (MessageResponcse == null) {
                            Observables.onNext("SuccessfulToken");
                        } else
                            Observables.onNext("Error");
                    }

                    @Override
                    public void onFailure(Call<ModelResponcePrimery> call, Throwable t) {
                        Observables.onNext("Failure");
                    }
                });


    }//_____________________________________________________________________________________________ End SendNumber


    public void VerifyNumber(String PhoneNumber, String verify) {//_________________________________ Start VerifyNumber

        RetrofitModule.isCancel = false;
        RetrofitComponent retrofitComponent =
                TrafficController
                        .getApplication(context)
                        .getRetrofitComponent();

        String Authorization = GetAuthorization(context);

        retrofitComponent
                .getRetrofitApiInterface()
                .SendVerifyCode(
                        PhoneNumber, verify, Authorization
                )
                .enqueue(new Callback<ModelResponcePrimery>() {
                    @Override
                    public void onResponse(Call<ModelResponcePrimery> call, Response<ModelResponcePrimery> response) {
                        if (!RetrofitModule.isCancel) {
                            MessageResponcse = CheckResponse(response, true);
                            if (MessageResponcse == null)
                                Observables.onNext("VerifyDone");
                            else
                                Observables.onNext("Error");

                        }
                    }

                    @Override
                    public void onFailure(Call<ModelResponcePrimery> call, Throwable t) {
                        if (!RetrofitModule.isCancel)
                            Observables.onNext("Failure");
                    }
                });


    }//_____________________________________________________________________________________________ End VerifyNumber


    public void GetLoginToken(String PhoneNumber) {//_______________________________________________ Start GetLoginToken

        RetrofitModule.isCancel = false;
        RetrofitComponent retrofitComponent =
                TrafficController
                        .getApplication(context)
                        .getRetrofitComponent();

        DeviceTools deviceTools = new DeviceTools(context);
        String imei = deviceTools.getIMEI();

        retrofitComponent
                .getRetrofitApiInterface()
                .getLoginToken(
                        RetrofitApis.client_id_value,
                        RetrofitApis.client_secret_value,
                        RetrofitApis.grant_type_device,
                        imei,
                        PhoneNumber,
                        1)
                .enqueue(new Callback<ModelToken>() {
                    @Override
                    public void onResponse(Call<ModelToken> call, Response<ModelToken> response) {
                        if (RetrofitModule.isCancel)
                            return;
                        MessageResponcse = CheckResponse(response, true);
                        if (MessageResponcse == null) {
                            modelToken = response.body();
                            SaveToken();
                        } else
                            Observables.onNext("Error");
                    }

                    @Override
                    public void onFailure(Call<ModelToken> call, Throwable t) {
                        Observables.onNext("Failure");
                    }
                });


    }//_____________________________________________________________________________________________ End GetLoginToken


    private void SaveToken() {//____________________________________________________________________ Start SaveToken

        SharedPreferences.Editor token =
                context.getSharedPreferences("dtrafficcontrollertoken", 0).edit();
        token.putString("accesstoken", modelToken.getAccess_token());
        token.putString("tokentype", modelToken.getToken_type());
        token.putInt("expiresin", modelToken.getExpires_in());
        token.putString("clientid", modelToken.getClient_id());
        token.putString("issued", modelToken.getIssued());
        token.putString("expires", modelToken.getExpires());
        token.putBoolean("login", true);
        token.apply();
        Observables.onNext("LoginDone");

    }//_____________________________________________________________________________________________ End SaveToken



    //______________________________________________________________________________________________  Start Getter & Setter


    public String getMessageResponcse() {
        return MessageResponcse;
    }

    public PublishSubject<String> getObservables() {
        return Observables;
    }

    //______________________________________________________________________________________________  End Getter & Sette

}
