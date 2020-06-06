package ir.ngra.drivertrafficcontroller.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import ir.ngra.drivertrafficcontroller.utility.polyutil.AverageAngle;

/**
 * Utility class that provides bearing values to true north.
 */
public class BearingToNorthProvider implements SensorEventListener, LocationListener
{
    public static final String TAG = "BearingToNorthProvider";

    /**
     * Interface definition for a callback to be invoked when the bearing changes.
     */
    public static interface ChangeEventListener {
        void onCurrentLocationChange(Location loc);
        void onBearingChanged(double bearing);
    }

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagneticField;
    private Context Mcontext;
    private final int TWO_MINUTES = 1000 * 60 * 2;
    private Location previousBestLocation = null;

    // some arrays holding intermediate values read from the sensors, used to calculate our azimuth
    // value

    private float[] mValuesAccelerometer;
    private float[] mValuesMagneticField;
    private float[] mMatrixR;
    private float[] mMatrixI;
    private float[] mMatrixValues;

    /**
     * minimum change of bearing (degrees) to notify the change listener
     */
    private final double mMinDiffForEvent;

    /**
     * minimum delay (millis) between notifications for the change listener
     */
    private final double mThrottleTime;

    /**
     * the change event listener
     */
    private ChangeEventListener mChangeEventListener;

    /**
     * angle to magnetic north
     */
    private AverageAngle mAzimuthRadians;

    /**
     * smoothed angle to magnetic north
     */
    private double mAzimuth = Double.NaN;

    /**
     * angle to true north
     */
    private double mBearing = Double.NaN;

    /**
     * last notified angle to true north
     */
    private double mLastBearing = Double.NaN;

    /**
     * Current GPS/WiFi location
     */
    private Location mLocation;

    /**
     * when we last dispatched the change event
     */
    private long mLastChangeDispatchedAt = -1;

    /**
     * Default constructor.
     *
     * @param context Application Context
     */
    public BearingToNorthProvider(Context context) {
        this(context, 10, 0.5, 50);
        Mcontext = context;
    }

    /**
     * @param context Application Context
     * @param smoothing the number of measurements used to calculate a mean for the azimuth. Set
     *                      this to 1 for the smallest delay. Setting it to 5-10 to prevents the
     *                      needle from going crazy
     * @param minDiffForEvent minimum change of bearing (degrees) to notify the change listener
     * @param throttleTime minimum delay (millis) between notifications for the change listener
     */
    public BearingToNorthProvider(Context context, int smoothing, double minDiffForEvent, int throttleTime)
    {

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mSensorMagneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mValuesAccelerometer = new float[3];
        mValuesMagneticField = new float[3];

        mMatrixR = new float[9];
        mMatrixI = new float[9];
        mMatrixValues = new float[3];

        mMinDiffForEvent = minDiffForEvent;
        mThrottleTime = throttleTime;

        mAzimuthRadians = new AverageAngle(smoothing);
    }

    //==============================================================================================
    // Public API
    //==============================================================================================

    /**
     * Call this method to start bearing updates.
     */
    @SuppressLint("MissingPermission")
    public void start()
    {
        mSensorManager.registerListener(this, mSensorAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorMagneticField, SensorManager.SENSOR_DELAY_UI);
        mLocationManager = (LocationManager) Mcontext.getSystemService(Mcontext.LOCATION_SERVICE);
        //mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, this);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, this);

//        for (final String provider : mLocationManager.getProviders(true)) {
//            if (LocationManager.GPS_PROVIDER.equals(provider)
//                    || LocationManager.PASSIVE_PROVIDER.equals(provider)
//                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
//                if (mLocation == null) {
//                    mLocation = mLocationManager.getLastKnownLocation(provider);
//                }
//                mLocationManager.requestLocationUpdates(provider, 0, 100.0f, this);
//            }
//        }
    }

    /**
     * call this method to stop bearing updates.
     */
    public void stop()
    {
        mSensorManager.unregisterListener(this, mSensorAccelerometer);
        mSensorManager.unregisterListener(this, mSensorMagneticField);
        mLocationManager.removeUpdates(this);
    }

    /**
     * @return current bearing
     */
    public double getBearing()
    {
        return mBearing;
    }

    /**
     * Returns the bearing event listener to which bearing events must be sent.
     * @return the bearing event listener
     */
    public ChangeEventListener getChangeEventListener()
    {
        return mChangeEventListener;
    }

    /**
     * Specifies the bearing event listener to which bearing events must be sent.
     * @param changeEventListener the bearing event listener
     */
    public void setChangeEventListener(ChangeEventListener changeEventListener)
    {
        this.mChangeEventListener = changeEventListener;
    }

    //==============================================================================================
    // SensorEventListener implementation
    //==============================================================================================

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mValuesAccelerometer, 0, 3);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mValuesMagneticField, 0, 3);
                break;
        }

        boolean success = SensorManager.getRotationMatrix(mMatrixR, mMatrixI,
                mValuesAccelerometer,
                mValuesMagneticField);

        // calculate a new smoothed azimuth value and store to mAzimuth
        if (success) {
            SensorManager.getOrientation(mMatrixR, mMatrixValues);
            mAzimuthRadians.putValue(mMatrixValues[0]);
            mAzimuth = Math.toDegrees(mAzimuthRadians.getAverage());
        }

//        if (event.sensor == this.accelerometer) {
//            System.arraycopy(event.values, 0, this.lastAccelerometer, 0, event.values.length);
//            this.lastAccelerometerSet = true;
//        } else if (event.sensor == this.magnetometer) {
//            System.arraycopy(event.values, 0, this.lastMagnetometer, 0, event.values.length);
//            this.lastMagnetometerSet = true;
//        }
//
//        if (this.lastAccelerometerSet && this.lastAccelerometerSet) {
//            SensorManager.getRotationMatrix(this.rotationMatrix,null, this.lastAccelerometer, this.lastMagnetometer);
//            SensorManager.getOrientation(this.rotationMatrix, this.orientation);
//
//
//            float azimuthInRadiands = this.orientation[0];
//
//            // this is now the heading of the phone. If you want
//            // to rotate a view to north don´t forget that you have
//            // to rotate by the negative value.
//            float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadiands);
//            mChangeEventListener.onBearingChanged(azimuthInDegrees);
//        }

        // update mBearing
        updateBearing();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {   }

    //==============================================================================================
    // LocationListener implementation
    //==============================================================================================

    @Override
    public void onLocationChanged(Location location)
    {
        // set the new location
        if (isBetterLocation(location, previousBestLocation)) {
            previousBestLocation = location;
//            if (location.getProvider().equalsIgnoreCase("network"))
//                return;
            mChangeEventListener.onCurrentLocationChange(location);
            this.mLocation = location;
            // update mBearing
//            updateBearing();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {   }

    @Override
    public void onProviderEnabled(String s) {   }

    @Override
    public void onProviderDisabled(String s) {   }

    //==============================================================================================
    // Private Utilities
    //==============================================================================================

    private void updateBearing()
    {
        if (!Double.isNaN(this.mAzimuth)) {
            if(this.mLocation == null) {
                Log.w(TAG, "Location is NULL bearing is not true north!");
                mBearing = mAzimuth;
            } else {
                mBearing = getBearingForLocation(this.mLocation);
            }

            // Throttle dispatching based on mThrottleTime and minDiffForEvent
            if( System.currentTimeMillis() - mLastChangeDispatchedAt > mThrottleTime &&
                    (Double.isNaN(mLastBearing) || Math.abs(mLastBearing - mBearing) >= mMinDiffForEvent)) {
                mLastBearing = mBearing;
                if(mChangeEventListener != null) {
                    mChangeEventListener.onBearingChanged(mBearing);
                }
                mLastChangeDispatchedAt = System.currentTimeMillis();
            }
        }
    }

    private double getBearingForLocation(Location location)
    {
        return mAzimuth + getGeomagneticField(location).getDeclination();
    }

    private GeomagneticField getGeomagneticField(Location location)
    {
        GeomagneticField geomagneticField = new GeomagneticField(
                (float)location.getLatitude(),
                (float)location.getLongitude(),
                (float)location.getAltitude(),
                System.currentTimeMillis());
        return geomagneticField;
    }




    protected boolean isBetterLocation(Location location, Location currentBestLocation) {//_________ isBetterLocation
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }//_____________________________________________________________________________________________ isBetterLocation


    private boolean isSameProvider(String provider1, String provider2) {//__________________________ isSameProvider
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }//_____________________________________________________________________________________________ isSameProvider

}