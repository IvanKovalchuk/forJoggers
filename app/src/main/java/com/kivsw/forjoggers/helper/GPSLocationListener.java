package com.kivsw.forjoggers.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import java.util.List;

/**
 * This class  is a wrapper around LocationManager.
 * It provides the current location
 */
public class GPSLocationListener implements android.location.LocationListener{
    //Context context=null;
    LocationManager locationManager=null;
    private boolean useNetWorkProvider=false;//BuildConfig.DEBUG;
    Location emulateLocation=null;
    final static public int UPDATE_INTERVAL=1000;


    public GPSLocationListener(Context context, boolean useNetWorkProvider )
    {
        super();
        this.useNetWorkProvider=useNetWorkProvider;
        registerInstance(context, this );
    }
    //-------------------------------------------------

    private synchronized  GPSLocationListener registerInstance(Context context, GPSLocationListener listener )
    {
           LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);

           // context = context.getApplicationContext();
            listener.locationManager = locationManager;

            try {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 2f, listener);
            }catch(SecurityException e)
            {
                e.getMessage();
            }

            if(useNetWorkProvider)
            try {
                if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, 2f, listener);
            }catch(SecurityException e)
            {
                e.getMessage();
            }

        return listener;
    };
    //-------------------------------------------------
    public void releaseInstance()
    {

        if(locationManager!=null) {
            try {
                locationManager.removeUpdates(this);
                locationManager=null;
            }catch(SecurityException e)
            {
                e.getMessage();
            }
        }

    }
    @Override
    public void finalize() throws java.lang.Throwable
    {
        super.finalize();
        releaseInstance();
    }
    //-------------------------------------------------
    // return 0 if we may use GPRS or/and NETWORK_PROVIDER
    //    1 - if gps-provider is turned off
    //    2 - if the device has no GPS service
    public static int estimateServiceStatus(Context context)
    {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        List<String> providers =locationManager.getAllProviders();
        boolean isGPS=false;
        if(providers!=null)
            isGPS= providers.indexOf(LocationManager.GPS_PROVIDER)>=0;

        if(isGPS==false)
            return 2;

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return 1;

        return 0;

    }

    //----------------------------------------
    public Location getLastknownLocation()
    {
        if(locationManager==null) return null;
        Location loc=null;

        try {
            loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null && useNetWorkProvider)
                loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }catch (SecurityException e)
        {e.toString();}

       /* if(loc!=null) {
            loc.removeSpeed();
            loc.removeBearing();
        }*/

        return loc;
    }

    //----------------------------------------
    @Override
    public void onLocationChanged(Location loc)
    {


    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
        provider.toString();
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
        provider.toString();
    }


    @Override
    public void onStatusChanged(String provider,int status, Bundle extras) {


        provider.toString();
        switch(status) {
            case     LocationProvider.OUT_OF_SERVICE:
                break;
            case   LocationProvider.AVAILABLE:
                break;
            case     LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
        }

        //mGpsStatus = locationManager.getGpsStatus(mGpsStatus);
    }

}
