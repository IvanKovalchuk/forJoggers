package com.kivsw.forjoggers;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;

import java.util.List;

/**
 * Created by ivan on 12.11.15.
 */
public class GPSLocationListener implements android.location.LocationListener{
    Context context=null;
    LocationManager locationManager=null;
    GpsStatus mGpsStatus=null;

    private static boolean useNetWorkProvider=BuildConfig.DEBUG;
    boolean isGPS;

    Location emulateLocation=null;


    GPSLocationListener(Context context)
    {
        super();
        registerInstance(context, this );
    }
    //-------------------------------------------------

    private static synchronized  GPSLocationListener registerInstance(Context context, GPSLocationListener listener )
    {
           LocationManager locationManager = (LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);

            context = context.getApplicationContext();
            listener.locationManager = locationManager;

            try {
                if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, 1000, 10f, listener);
            }catch(Exception e)
            {
                e.getMessage();
            }

            if(useNetWorkProvider)
            try {
                if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 1000, 10f, listener);
            }catch(Exception e)
            {
                e.getMessage();
            }

        return listener;
    };
    //-------------------------------------------------
    public void releaseInstance()
    {

        if(locationManager!=null) {
            locationManager.removeUpdates(this);
            locationManager=null;
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
    public static int estimateServiceStatus(Context context, StringBuilder msg)
    {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        List<String> providers =locationManager.getAllProviders();
        boolean isGPS=false, isNetwork=false;
        if(providers!=null)
        {
            isGPS= providers.indexOf(LocationManager.GPS_PROVIDER)>=0;
            isNetwork =  providers.indexOf(LocationManager.NETWORK_PROVIDER)>=0;
        };

        if(isGPS==false &&  isNetwork==false)
        {
            msg.append(context.getText(R.string.gps_unreachable));
            return 2;
        };

        if(!getGpsStatus( context, locationManager ))
        {
            msg.append(context.getText(R.string.gps_disable));
            return 1;
        };

        return 0;

    }


    //-------------------------------------------------
    /**
     *  Method to Check GPS is enable or disable
     * */
    private static boolean getGpsStatus(Context context,LocationManager locationManager )
    {
        boolean e1=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER),
                e2=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return e1 || e2;
    }
    //----------------------------------------
    public Location getLastknownLocation()
    {
        Location loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        return loc;
    }

    //----------------------------------------
    @Override
    public void onLocationChanged(Location loc)
    {

       /* if(loc!=null) {
            String provider = loc.getProvider();
            synchronized (mutex) {
                switch (provider) {
                    case LocationManager.GPS_PROVIDER:
                        locGPS = loc;
                        break;
                    case LocationManager.NETWORK_PROVIDER:
                        locNETWORK_PROVIDER = loc;
                        break;
                } ;
            }
        }

        Location bestLocation=getBestLocation();
        float speed=-1;
        if(bestLocation.hasSpeed())
            speed=bestLocation.getSpeed()*(3.6f);

        GPSDataReceiver.sendMyPosition(context, bestLocation.getLatitude(),
                bestLocation.getLongitude(), speed);*/



    /*//----------to get City-Name from coordinates -------------
      String cityName=null;
      Geocoder gcd = new Geocoder(getBaseContext(),
   Locale.getDefault());
      List<Address>  addresses;
      try {
      addresses = gcd.getFromLocation(loc.getLatitude(), loc
   .getLongitude(), 1);
      if (addresses.size() > 0)
         System.out.println(addresses.get(0).getLocality());
         cityName=addresses.get(0).getLocality();
        } catch (IOException e) {
        e.printStackTrace();
      }

      String s = longitude+"\n"+latitude +
   "\n\nMy Currrent City is: "+cityName;
           editLocation.setText(s);*/
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
        // TODO Auto-generated method stub
        provider.toString();
        switch(status) {
            case     LocationProvider.OUT_OF_SERVICE:
                break;
            case   LocationProvider.AVAILABLE:
                break;
            case     LocationProvider.TEMPORARILY_UNAVAILABLE:
                break;
        }

        mGpsStatus = locationManager.getGpsStatus(mGpsStatus);
    }

}
