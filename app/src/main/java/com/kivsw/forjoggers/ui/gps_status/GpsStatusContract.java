package com.kivsw.forjoggers.ui.gps_status;

import android.location.GpsSatellite;
import android.location.Location;

import java.util.ArrayList;

/**
 * Created by ivan on 6/7/16.
 */
public interface GpsStatusContract {
    interface IPresenter
    {
        void setUI(IView aGpsStatusFragment);

    };


    static final int  GPS=0,GLONASS=1,OTHER=2;
    interface IView
    {
        void clearSatellites();
        void invalidateSatilletes();
        void addSatellites(ArrayList<GpsSatellite> satellite, int sysId);

        void setLocation(Location loc);
    }
}
