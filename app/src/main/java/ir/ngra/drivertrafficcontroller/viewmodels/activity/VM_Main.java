package ir.ngra.drivertrafficcontroller.viewmodels.activity;

import android.content.Context;

import io.reactivex.subjects.PublishSubject;

public class VM_Main {

    private Context context;
    private String MessageResponse;
    private PublishSubject<String> publishSubject;

    public VM_Main(Context context) {//_____________________________________________________________ VM_Main
        this.context = context;
    }//_____________________________________________________________________________________________ VM_Main


    public String getMessageResponse() {//__________________________________________________________ getMessageResponse
        return MessageResponse;
    }//_____________________________________________________________________________________________ getMessageResponse

    public PublishSubject<String> getPublishSubject() {//___________________________________________ getPublishSubject
        return publishSubject;
    }//_____________________________________________________________________________________________ getPublishSubject
}
