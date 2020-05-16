package ir.ngra.drivertrafficcontroller.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import java.util.List;


public class DeviceTools {

    private Context context;

    public DeviceTools(Context context) {
        this.context = context;
    }

    public String getDeviceId() {
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId != null ? deviceId : "";
    }

    public String getAndroidVersion() {
        String version = Build.VERSION.RELEASE;
        return version != null ? version : "";
    }

    public String getDeviceName() {
        String deviceName = Build.PRODUCT;
        return deviceName != null ? deviceName : "";
    }

    public String getDeviceBrand() {
        String deviceBrand = Build.BRAND;
        return deviceBrand != null ? deviceBrand : "";
    }

    public String getDeviceManufacturer() {
        String manufacturer = Build.MANUFACTURER;
        return manufacturer != null ? manufacturer : "";
    }

    public String getDeviceModel() {
        String model = Build.MODEL;
        return model != null ? model : "";
    }

    public String getMacAddress() {
        if (context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED) {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (manager != null) {
                WifiInfo info = manager.getConnectionInfo();
                String macAddress = info.getMacAddress();
                return macAddress != null ? macAddress : "";
            }
            else return "";
        }
        else return "";
    }

    public String getAndroidOSSerial() {
        String serial = Build.SERIAL;
        return serial != null ? serial : "";
    }

    public String getIMEI() {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

            String deviceId = "358662080107183";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                SubscriptionManager subsManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

                List<SubscriptionInfo> subsList = subsManager.getActiveSubscriptionInfoList();

                if (subsList!=null) {
                    for (SubscriptionInfo subsInfo : subsList) {
                        if (subsInfo != null) {
                            deviceId  = subsInfo.getIccId();
                        }
                    }
                }
            } else {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                deviceId = tm.getDeviceId();
            }
            return deviceId != null ? deviceId : "";
        }
        else return "";
    }

    public String getPhoneNumber() {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String line1Number = tm.getLine1Number();
            return line1Number != null ? line1Number : "";
        }
        else return "";
    }

    public String getOperator() {
        if (context.checkCallingOrSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = tm.getNetworkOperator();
            return operator != null ? operator : "";
        }
        else return "";
    }

    public int getWidthInPx() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public int getHeightInPx() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public float getHeightInInch() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        double y = dm.heightPixels / (dm.densityDpi);
        return (float) y;
    }

    public float getWidthInInch() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        double x = dm.widthPixels / (dm.densityDpi);
        return (float) x;
    }

    public float getDpi() {
        return context.getResources().getDisplayMetrics().densityDpi;
    }
}