package com.kivsw.forjoggers.helper;

import android.content.Context;
import android.location.GpsStatus;
import android.location.LocationManager;

/**
 *  This class retrieve GPS status (to get all visible satellites)
 */
public class GPSStatusHelper
        implements GpsStatus.Listener
{

    public interface  OnStatusChange
    {
        void onStatusChange(int event, GpsStatus status);
    }

    private LocationManager locationManager;
    OnStatusChange  onStatusChangeListener=null;
    public GPSStatusHelper(Context context, OnStatusChange onStatusChangeListener)
    {
        super();

        locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        this.onStatusChangeListener=onStatusChangeListener;
        locationManager.addGpsStatusListener(this);
    }

    public void release()
    {
        locationManager.removeGpsStatusListener(this);
    }

    public boolean hasGPS()
    {
        return locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER);
    }

    public boolean isEnabled()
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    @Override
    public void onGpsStatusChanged(int event) {

        GpsStatus status= locationManager.getGpsStatus (null);

        if(onStatusChangeListener!=null)
            onStatusChangeListener.onStatusChange(event, status);
        switch (event)
        {
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;
        };

    }
}
