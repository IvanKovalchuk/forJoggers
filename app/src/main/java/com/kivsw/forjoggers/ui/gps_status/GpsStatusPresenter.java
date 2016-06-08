package com.kivsw.forjoggers.ui.gps_status;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationManager;

import com.kivsw.forjoggers.helper.GPSStatusHelper;
import com.kivsw.forjoggers.helper.RxGpsLocation;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by ivan on 6/7/16.
 */
public class GpsStatusPresenter
        implements GpsStatusContract.IPresenter, GPSStatusHelper.OnStatusChange
{

    static private GpsStatusPresenter singletone=null;

    static public GpsStatusPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new GpsStatusPresenter(context.getApplicationContext());
        return singletone;
    };


    Context context;
    GpsStatusContract.IView view;
    GPSStatusHelper mGPSStatusHelper;

    GpsStatusPresenter(Context context)
    {
        this.context=context;
        mGPSStatusHelper = null;
    };



    @Override
    public void setUI(GpsStatusContract.IView aGpsStatusFragment) {
        view=aGpsStatusFragment;

        if(view==null)
        {
            if(mGPSStatusHelper!=null) mGPSStatusHelper.release();
            mGPSStatusHelper=null;
        }
        else
        {
            mGPSStatusHelper=new GPSStatusHelper(context,this);
        }
    }

    @Override
    public void onStatusChange(int event, GpsStatus status) {

        if(event==GpsStatus.GPS_EVENT_STOPPED)
        {
            view.clearSatellites();
            view.invalidateSatilletes();
            return;
        };

        Iterator<GpsSatellite> satellites= status.getSatellites().iterator();

        ArrayList<GpsSatellite> gps=new ArrayList<>(),
                glonass=new ArrayList<>(),
                other=new ArrayList<>();


        while(satellites.hasNext())
        {
            GpsSatellite sat=satellites.next();

            int prn =sat.getPrn();
            if(prn<=32) gps.add(sat);
            else
            if(prn>=64 && prn<=88)
                glonass.add(sat);
            else
                other.add(sat);
        }

        if(view!=null)
        {
            view.clearSatellites();

            if(RxGpsLocation.isGpsLocationAvailable()) {
                try {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    view.setLocation(locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
                }catch(Exception e)
                {
                    e.toString();
                }
            }
            else
                view.setLocation(null);

            view.addSatellites(gps,GpsStatusContract.GPS);
            view.addSatellites(glonass,GpsStatusContract.GLONASS);
            view.addSatellites(other,GpsStatusContract.OTHER);
            view.invalidateSatilletes();
        }
    }
}
