package ir.ngra.drivertrafficcontroller.views.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.cunoraz.gifview.library.GifView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.daggers.retrofit.RetrofitModule;
import ir.ngra.drivertrafficcontroller.databinding.FragmentLoginBinding;
import ir.ngra.drivertrafficcontroller.viewmodels.fragments.VM_Login;
import ir.ngra.drivertrafficcontroller.views.dialogs.DialogMessage;

import static ir.ngra.drivertrafficcontroller.utility.StaticFunctions.TextChangeForChangeBack;

public class Login extends Fragment {

    private Context context;
    private VM_Login viewModel;
    private static View LoginView;
    private String PhoneNumber;
    private NavController navController;
    private DisposableObserver<String> observer;

    @BindView(R.id.editPhoneNumber)
    EditText editPhoneNumber;

    @BindView(R.id.editPassword)
    EditText editPassword;

    @BindView(R.id.imgShowPassword)
    ImageView imgShowPassword;

    @BindView(R.id.BtnLogin)
    RelativeLayout BtnLogin;

    @BindView(R.id.ProgressGif)
    GifView ProgressGif;

    @BindView(R.id.BtnLoginText)
    TextView BtnLoginText;

    @BindView(R.id.imgProgress)
    ImageView imgProgress;


    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {//__________________________________________________________ Start onCreateView
        this.context = getContext();
        viewModel = new VM_Login(context);
        FragmentLoginBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_login, container, false
        );
        binding.setLogin(viewModel);
        LoginView = binding.getRoot();
        ButterKnife.bind(this, LoginView);
        return LoginView;
    }//_____________________________________________________________________________________________ Start onCreateView


    public Login() {//______________________________________________________________________ Start FragmentLogin

    }//_____________________________________________________________________________________________ End FragmentLogin


    @Override
    public void onStart() {//_______________________________________________________________________ Start onStart
        super.onStart();
        navController = Navigation.findNavController(LoginView);
        SetClick();
        SetTextWatcher();
        if (observer != null)
            observer.dispose();
        observer = null;
        ObserverObservable();
        DismissLoading();
        //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }//_____________________________________________________________________________________________ End onStart


    private void SetClick() {//_____________________________________________________________________ Start SetClick

        BtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (RetrofitModule.isCancel) {
                    if (CheckEmpty()) {
                        ShowLoading();
                        viewModel.GetLoginToken(PhoneNumber);
                    }
                } else {
                    RetrofitModule.isCancel = true;
                    DismissLoading();
                }
            }
        });


    }//_____________________________________________________________________________________________ End SetClick


    private void ObserverObservable() {//___________________________________________________________ Start ObserverObservable

        observer = new DisposableObserver<String>() {
            @Override
            public void onNext(String s) {
                getActivity()
                        .runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DismissLoading();
                                switch (s) {
                                    case "Successful":
                                        if (observer != null)
                                            observer.dispose();
                                        observer = null;
                                        Bundle bundle = new Bundle();
                                        bundle.putString("PhoneNumber", PhoneNumber);
                                        navController.navigate(
                                                R.id.action_login_to_verify,
                                                bundle
                                        );
                                        break;
                                    case "ConfigHandlerForHome":
                                        if (observer != null)
                                            observer.dispose();
                                        observer = null;
                                        navController.navigate(R.id.action_login_to_home2);
                                        break;
                                    case "Error":
                                        ShowMessage(
                                                viewModel.getMessageResponcse(),
                                                getResources().getColor(R.color.ML_White),
                                                getResources().getDrawable(R.drawable.ic_warning_red)
                                        );
                                        break;
                                    case "Failure":
                                        ShowMessage(
                                                getResources().getString(R.string.onFailure),
                                                getResources().getColor(R.color.ML_White),
                                                getResources().getDrawable(R.drawable.ic_warning_red)
                                        );
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


        viewModel
                .getObservables()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

    }//_____________________________________________________________________________________________ End ObserverObservable


    private Boolean CheckEmpty() {//________________________________________________________________ Start CheckEmpty

        boolean phone = false;
        PhoneNumber = editPhoneNumber.getText().toString();


        if (editPhoneNumber.getText().length() != 11) {
            editPhoneNumber.setBackgroundResource(R.drawable.edit_empty_background);
            editPhoneNumber.setError(getResources().getString(R.string.EmptyPhoneNumber));
            editPhoneNumber.requestFocus();
            phone = false;
        } else {
            String ZeroNine = editPhoneNumber.getText().subSequence(0, 2).toString();
            if (ZeroNine.equalsIgnoreCase("09"))
                phone = true;
            else {
                editPhoneNumber.setBackgroundResource(R.drawable.edit_empty_background);
                editPhoneNumber.setError(getResources().getString(R.string.EmptyPhoneNumber));
                editPhoneNumber.requestFocus();
                phone = false;
            }
        }

        if (phone)
            return true;
        else
            return false;

    }//_____________________________________________________________________________________________ End CheckEmpty


    private void SetTextWatcher() {//_______________________________________________________________ Start SetTextWatcher
        editPhoneNumber.addTextChangedListener(TextChangeForChangeBack(editPhoneNumber));
    }//_____________________________________________________________________________________________ End SetTextWatcher


    private void ShowMessage(String message, int color, Drawable icon) {//__________________________ Start ShowMessage

        DialogMessage dialogMessage = new DialogMessage(context, message, color, icon);
        dialogMessage.setCancelable(false);
        dialogMessage.show(getFragmentManager(), NotificationCompat.CATEGORY_PROGRESS);

    }//_____________________________________________________________________________________________ End ShowMessage


    private void DismissLoading() {//_______________________________________________________________ Start DismissLoading
        RetrofitModule.isCancel = true;
        BtnLoginText.setText(getResources().getString(R.string.Login));
        BtnLogin.setBackground(getResources().getDrawable(R.drawable.button_bg));
        ProgressGif.setVisibility(View.GONE);
        imgProgress.setVisibility(View.VISIBLE);

    }//_____________________________________________________________________________________________ End DismissLoading


    private void ShowLoading() {//__________________________________________________________________ Start ShowLoading
        RetrofitModule.isCancel = false;
        BtnLoginText.setText(getResources().getString(R.string.Cancel));
        BtnLogin.setBackground(getResources().getDrawable(R.drawable.button_red));
        ProgressGif.setVisibility(View.VISIBLE);
        imgProgress.setVisibility(View.INVISIBLE);
    }//_____________________________________________________________________________________________ End ShowLoading


    @Override
    public void onDestroy() {//_____________________________________________________________________ Start onDestroy
        super.onDestroy();
        if (observer != null)
            observer.dispose();
        observer = null;
    }//_____________________________________________________________________________________________ End onDestroy


}
