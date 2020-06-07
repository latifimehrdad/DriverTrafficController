package ir.ngra.drivertrafficcontroller.views.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.databinding.FragmentSplashBinding;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Splash;
import ir.ngra.drivertrafficcontroller.views.dialogs.DialogMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class Splash extends Fragment {


    private View view;
    private Context context;
    private NavController navController;
    private VM_Splash vm_Splash;
    private DisposableObserver<String> observer;


    public Splash() {//_____________________________________________________________________ Start FragmentSplash

    }//_____________________________________________________________________________________________ End FragmentSplash


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {//__________________________________________________________ Start onCreateView
        this.context = getContext();
        FragmentSplashBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_splash, container, false
        );
        vm_Splash = new VM_Splash(context);
        binding.setSplash(vm_Splash);
        view = binding.getRoot();
        ButterKnife.bind(this, view);
        return view;
    }//_____________________________________________________________________________________________ Start onCreateView


    @Override
    public void onStart() {//_______________________________________________________________________ Start onStart
        super.onStart();
        navController = Navigation.findNavController(view);
        if (observer != null)
            observer.dispose();
        observer = null;
        ObserverObservables();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                vm_Splash.CheckToken();
            }
        }, 2000);

    }//_____________________________________________________________________________________________ End onStart


    private void ObserverObservables() {//__________________________________________________________ Start ObserverObservables

        observer = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (s) {
                            case "ConfigHandlerForHome":
                                ConfigHandlerForHome();
                                break;
                            case "ConfigHandlerForLogin":
                                ConfigHandlerForHome();
                                //ConfigHandlerForLogin();
                                break;
                            case "CancelByUser":
                                break;
                            case "Failure":
                                ShowMessage(
                                        getResources().getString(R.string.onFailure),
                                        getResources().getColor(R.color.ML_White),
                                        getResources().getDrawable(R.drawable.ic_warning_red));
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

        vm_Splash
                .getObservables()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }//_____________________________________________________________________________________________ End ObserverObservables


    private void ConfigHandlerForHome() {//_________________________________________________________ Start ConfigHandlerForHome

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (observer != null)
                    observer.dispose();
                observer = null;
//                navController.navigate(R.id.action_splash_to_home2);// Google Map
                navController.navigate(R.id.action_splash_to_homeOsm);// OSM
            }
        }, 2000);

    }//_____________________________________________________________________________________________ End ConfigHandlerForHome


    private void ConfigHandlerForLogin() {//________________________________________________________ Start ConfigHandlerForLogin

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (observer != null)
                    observer.dispose();
                observer = null;
                navController.navigate(R.id.action_splash_to_login);
            }
        }, 2000);

    }//_____________________________________________________________________________________________ End ConfigHandlerForLogin


    private void ShowMessage(String message, int color, Drawable icon) {//__________________________ Start ShowMessage

        DialogMessage dialogMessage = new DialogMessage(context, message, color, icon);
        dialogMessage.setCancelable(false);
        dialogMessage.show(getFragmentManager(), NotificationCompat.CATEGORY_PROGRESS);

    }//_____________________________________________________________________________________________ End ShowMessage


    @Override
    public void onDestroy() {//_____________________________________________________________________ Start onDestroy
        super.onDestroy();
        if (observer != null)
            observer.dispose();
        observer = null;
    }//_____________________________________________________________________________________________ End onDestroy


}
