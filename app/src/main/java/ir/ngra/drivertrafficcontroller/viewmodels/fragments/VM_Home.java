package ir.ngra.drivertrafficcontroller.viewmodels.fragments;

import android.content.Context;

import io.reactivex.subjects.PublishSubject;

public class VM_Home {


    private Context context;
    private String MessageResponse;
    private PublishSubject<String> publishSubject;

    public VM_Home(Context context) {//_____________________________________________________________ VM_Home
        this.context = context;
    }//_____________________________________________________________________________________________ VM_Home


    public String getMessageResponse() {//__________________________________________________________ getMessageResponse
        return MessageResponse;
    }//_____________________________________________________________________________________________ getMessageResponse

    public PublishSubject<String> getPublishSubject() {//___________________________________________ getPublishSubject
        return publishSubject;
    }//_____________________________________________________________________________________________ getPublishSubject
}
