package ir.ngra.drivertrafficcontroller.views.application;

import androidx.multidex.MultiDexApplication;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import ir.ngra.drivertrafficcontroller.R;

public class TrafficController extends MultiDexApplication {

    @Override
    public void onCreate() {//______________________________________________________________________ onCreate
        super.onCreate();
        ConfigurationCalligraphy();
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



}
