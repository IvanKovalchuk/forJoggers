package com.kivsw.forjoggers.ui;

import android.content.Context;
import android.location.Location;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * Created by ivan on 22.04.2016.
 */
public class CurrentLocationOverlay extends MyLocationNewOverlay {

    public CurrentLocationOverlay(Context context, MapView mapView) {
        super(context, new GpsMyLocationProvider(context), mapView);
        disableFollowLocation();
        setDrawAccuracyEnabled(true);
    };
    public boolean isMyLocationEnabled() {
        return true;
    }
    public void setLocation(Location location)
    {super.setLocation(location);}
}
