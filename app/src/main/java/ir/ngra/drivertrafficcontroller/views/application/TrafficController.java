package ir.ngra.drivertrafficcontroller.views.application;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.DaggerRetrofitComponent;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitComponent;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;

public class TrafficController extends MultiDexApplication {

    private Context context;
    private RetrofitComponent retrofitComponent;

    @Override
    public void onCreate() {//______________________________________________________________________ onCreate
        super.onCreate();
        this.context = getApplicationContext();
        ConfigurationCalligraphy();
        ConfigrationRetrofitComponent();
    }//_____________________________________________________________________________________________ onCreate


    private void ConfigurationCalligraphy() {//_____________________________________________________ ConfigurationCalligraphy
        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("font/iransanslight.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

    }//_____________________________________________________________________________________________ ConfigurationCalligraphy


    private void ConfigrationRetrofitComponent() {//________________________________________________ Start ConfigrationRetrofitComponent
        retrofitComponent = DaggerRetrofitComponent
                .builder()
                .retrofitModule(new RetrofitModule(context))
                .build();
    }//_____________________________________________________________________________________________ End ConfigrationRetrofitComponent


    public static TrafficController getApplication(Context context) {//_____________________________ Start getApplication
        return (TrafficController) context.getApplicationContext();
    }//_____________________________________________________________________________________________ End getApplication


    public RetrofitComponent getRetrofitComponent() {//_____________________________________________ Start getRetrofitComponent
        return retrofitComponent;
    }//_____________________________________________________________________________________________ End getRetrofitComponent


}
