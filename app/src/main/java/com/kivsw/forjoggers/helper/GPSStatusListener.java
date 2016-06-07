package com.kivsw.forjoggers.helper;

import android.content.Context;
import android.location.GpsStatus;
import android.location.LocationManager;

/**
 * Created by ivan on 6/6/16.
 */
public class GPSStatusListener
        implements GpsStatus.Listener
{

    private LocationManager locationManager;
    public GPSStatusListener(Context context)
    {
        super();

        locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

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
