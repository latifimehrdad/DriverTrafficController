package ir.ngra.drivertrafficcontroller.viewmodels.fragments;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.subjects.PublishSubject;


public class VM_Splash {

    private Context context;
    private PublishSubject<String> Observables = null;

    public VM_Splash(Context context) {//___________________________________________________ Start VM_FragmentSplash
        this.context = context;
        Observables = PublishSubject.create();
    }//_____________________________________________________________________________________________ End VM_FragmentSplash


    public void CheckToken() {//_______________________________ ____________________________________ Start CheckToken

        SharedPreferences prefs = context.getSharedPreferences("dtrafficcontrollertoken", 0);
        if (prefs == null) {
            Observables.onNext("GetTokenFromServer");
        } else {
            String access_token = prefs.getString("accesstoken", null);
            String expires = prefs.getString("expires", null);
            if ((access_token != null) || (expires != null)) {
                boolean login = prefs.getBoolean("login", false);
                if (login)
                    Observables.onNext("ConfigHandlerForHome");
                else
                    Observables.onNext("ConfigHandlerForLogin");
            } else
                Observables.onNext("ConfigHandlerForLogin");
        }

    }//_____________________________________________________________________________________________ End CheckToken


    //______________________________________________________________________________________________ Start Getter
    public PublishSubject<String> getObservables() {
        return Observables;
    }
    //______________________________________________________________________________________________ End Getter

}
