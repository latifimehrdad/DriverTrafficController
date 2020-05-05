package ir.ngra.drivertrafficcontroller.views.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;


import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import ir.ngra.drivertrafficcontroller.R;
import ir.ngra.drivertrafficcontroller.databinding.ActivityMainBinding;
import ir.ngra.drivertrafficcontroller.viewmodels.activity.VM_Main;

public class MainActivity extends AppCompatActivity {

    private VM_Main vm_main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {//__________________________________________ onCreate
        super.onCreate(savedInstanceState);
        BindView();
        SetPermission();
    }//_____________________________________________________________________________________________ onCreate



    private void BindView() {//_____________________________________________________________________ BindView
        vm_main = new VM_Main(this);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMain(vm_main);
        ButterKnife.bind(this);
    }//_____________________________________________________________________________________________ BindView



    public void SetPermission() {//_________________________________________________________________ Start SetPermission

        int permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (permissionLocation != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
            listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);



        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    0);
        }

    }//_____________________________________________________________________________________________ End SetPermission



    public void attachBaseContext(Context newBase) {//______________________________________________ attachBaseContext
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }//_____________________________________________________________________________________________ attachBaseContext

}
