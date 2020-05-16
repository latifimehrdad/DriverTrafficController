package ir.ngra.drivertrafficcontroller.models;

import java.util.Date;

public class ModelLocation {

    private double Latitude;
    private double Longitude;
    private double Altitude;
    private float Speed;
    private Date ClientDate;
    private float Accuracy;
    private boolean IsGPS;

    public ModelLocation(
            double latitude,
            double longitude,
            double altitude,
            float speed,
            Date clientDate,
            float accuracy,
            boolean isGPS) {//_____________________________________________________________________ Start ModelLocation
        Latitude = latitude;
        Longitude = longitude;
        Altitude = altitude;
        Speed = speed;
        ClientDate = clientDate;
        Accuracy = accuracy;
        IsGPS = isGPS;
    }//_____________________________________________________________________________________________ End ModelLocation
}
