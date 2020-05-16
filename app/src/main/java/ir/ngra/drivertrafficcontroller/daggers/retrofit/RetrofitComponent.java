package ir.ngra.drivertrafficcontroller.daggers.retrofit;


import dagger.Component;
import ir.ngra.drivertrafficcontroller.daggers.DaggerScope;

@DaggerScope
@Component(modules = RetrofitModule.class)
public interface RetrofitComponent {
    RetrofitApiInterface getRetrofitApiInterface();
}
