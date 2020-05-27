package ir.ngra.drivertrafficcontroller.daggers.retrofit;

import ir.ngra.drivertrafficcontroller.models.ModelGetAddress;
import ir.ngra.drivertrafficcontroller.models.ModelLocations;
import ir.ngra.drivertrafficcontroller.models.ModelResponcePrimery;
import ir.ngra.drivertrafficcontroller.models.ModelRoute;
import ir.ngra.drivertrafficcontroller.models.ModelToken;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface RetrofitApiInterface {


    String Version = "/api/v1";

    //______________________________________________________________________________________________ getToken
    @FormUrlEncoded
    @POST("/token")
    Call<ModelToken> getToken
    (
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("grant_type") String grant_type
    );


    //______________________________________________________________________________________________ SendPhoneNumber
    @FormUrlEncoded
    @POST(Version + "/deviceaccount/demandverificationcode")
    Call<ModelResponcePrimery> SendPhoneNumber
    (
            @Field("DeviceNumber") String DeviceNumber,
            @Field("Type") Integer Type,
            @Field("DeviceSpecification") String DeviceSpecification,
            @Header("Authorization") String Authorization
    );


    //______________________________________________________________________________________________ SendVerifyCode
    @FormUrlEncoded
    @POST(Version + "/account/confirmmobile")
    Call<ModelResponcePrimery> SendVerifyCode
    (
            @Field("Mobile") String PhoneNumber,
            @Field("Code") String Password,
            @Header("Authorization") String Authorization
    );


    //______________________________________________________________________________________________ getLoginToken
    @FormUrlEncoded
    @POST("/token")
    Call<ModelToken> getLoginToken
    (
            @Field("client_id") String client_id,
            @Field("client_secret") String client_secret,
            @Field("grant_type") String grant_type,
            @Field("DeviceSpecification") String DeviceSpecification,
            @Field("DeviceNumber") String DeviceNumber,
            @Field("DeviceType") Integer DeviceType
    );



    //______________________________________________________________________________________________ DeviceLogs
    @POST(Version + "/deviceattendance/report")
    Call<ModelResponcePrimery> DeviceLogs
    (
            @Header("DeviceSpecification") String DeviceSpecification,
            @Header("Authorization") String Authorization,
            @Body ModelLocations locations
    );


    @GET()
    Call<ModelGetAddress> getAddress(
            @Url String url
    );


    @GET()
    Call<ModelRoute> getRoute(
            @Url String url
    );



//    @FormUrlEncoded
//    @POST("Api.aspx?action=register")
//    Call<Model_Result> SendPhoneNumber
//            (
//                    @Field("phone") String PhoneNumber
//            );


//    @POST("Api.aspx?action=validate")
//    @FormUrlEncoded
//    Call<Model_Result> VerifyPhoneNumber
//            (
//                    @Field("phone") String phone,
//                    @Field("code") String code,
//                    @Field("imei") String imei
//            );

//    @POST("Api.aspx?action=sync_location")
//    @FormUrlEncoded
//    Call<Model_Result> SendLocation(
//            @Field("imei") String imei,
//            @Field("locationsJson") String locationsJson
//    );

}
