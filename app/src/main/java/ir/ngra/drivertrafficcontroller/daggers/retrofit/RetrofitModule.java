package ir.ngra.drivertrafficcontroller.daggers.retrofit;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import ir.ngra.drivertrafficcontroller.daggers.DaggerScope;
import okhttp3.Cache;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.converter.gson.GsonConverterFactory;

import static ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitApis.Host;

@Module
public class RetrofitModule {

    private Context context;
    public static boolean isCancel = false;

    public RetrofitModule(Context context) {//______________________________________________________ Start RetrofitModule
        this.context = context;
    }//_____________________________________________________________________________________________ End RetrofitModule

    @Provides
    @DaggerScope
    public RetrofitApiInterface getRetrofitApiInterface(retrofit2.Retrofit retrofit) {//____________ Start RetrofitModule
        return retrofit.create(RetrofitApiInterface.class);
    }//_____________________________________________________________________________________________ End RetrofitModule


    @Provides
    @DaggerScope
    public retrofit2.Retrofit getRetrofit(OkHttpClient okHttpClient) {//____________________________ Start getRetrofit

        Gson gson = new GsonBuilder()
                .setLenient().create();

        return new retrofit2.Retrofit.Builder()
                .baseUrl(Host)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build();
    }//_____________________________________________________________________________________________ End getRetrofit


    @Provides
    @DaggerScope
    public OkHttpClient getOkHttpClient(
            Cache cache,
            HttpLoggingInterceptor interceptor,
            JavaNetCookieJar javaNetCookieJar) {//__________________________________________________ Start getOkHttpClient
        return new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor)
                .cookieJar(javaNetCookieJar)
                .cache(cache)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build();
    }//_____________________________________________________________________________________________ End getOkHttpClient


    @Provides
    @DaggerScope
    public JavaNetCookieJar getJavaNetCookieJar(CookieHandler cookieHandler) {//____________________ Start getJavaNetCookieJar
        return new JavaNetCookieJar(cookieHandler);
    }//_____________________________________________________________________________________________ End getJavaNetCookieJar


    @Provides
    @DaggerScope
    public CookieHandler getCookieHandler() {//_____________________________________________________ Start getCookieHandler
        return new CookieManager();
    }//_____________________________________________________________________________________________ End getCookieHandler


    @Provides
    @DaggerScope
    public HttpLoggingInterceptor getHttpLoggingInterceptor() {//___________________________________ Start getHttpLoggingInterceptor
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }//_____________________________________________________________________________________________ End getHttpLoggingInterceptor


    @Provides
    @DaggerScope
    public Cache getCache(File file) {//____________________________________________________________ Start getCache
        return new Cache(file, 5 * 1000 * 1000);
    }//_____________________________________________________________________________________________ End getCache


    @Provides
    @DaggerScope
    public File getFile() {//_______________________________________________________________________ Start getFile
        return new File(context.getCacheDir(), "Okhttp_cache");
    }//_____________________________________________________________________________________________ End getFile

}
