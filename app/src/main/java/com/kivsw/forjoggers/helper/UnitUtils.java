package com.kivsw.forjoggers.helper;

import android.content.Context;

import com.kivsw.forjoggers.R;

import java.util.Locale;

/**
 * Created by ivan on 12.12.2015.
 * Translate units of values
 */
public class UnitUtils {

    SettingsKeeper settings;
    Context context;

    public UnitUtils(Context context)
    {
        this.context = context;
        this.settings = SettingsKeeper.getInstance(context);
    }

    /**
     * Distance
     * @param distanceMeters
     * @return
     */
    public String distanceToStr(double distanceMeters)
    {
        return String.format(Locale.US, distanceUnit(true), convertDistance(distanceMeters));
    }
    public double convertDistance(double distanceMeters)
    {
        double k=1;

        int v=settings.getDistanceUnit();
        switch(v)
        {
            case SettingsKeeper.METERS:
                k=1;
                break;
            case SettingsKeeper.KILOMETERS:
                k=0.001;
                break;
            case SettingsKeeper.MILES:
                k=1/1609.0;
                break;
        }
        return distanceMeters*k;

    }

    public String distanceUnit(boolean withFormat)
    {
        String unit="";
        String units[]=context.getResources().getStringArray(R.array.distance_short_unit);
        String format="%f";
        try {
            int v=settings.getDistanceUnit();
            unit=units[v];
            switch(v)
            {
                case SettingsKeeper.METERS:
                    format="%.0f ";
                    break;
                case SettingsKeeper.KILOMETERS:
                    format="%.3f ";
                    break;
                case SettingsKeeper.MILES:
                    format="%.3f " ;
                    break;
            }
        }catch(Exception e)
        {
            return e.toString();
        }

        if(withFormat)
            return format+unit;

        return unit;
    }

    //--------------------------------------------

    /**
     * Speed
     * @param speedMetersPerSec
     * @return
     */
    public  String speedToStr(double speedMetersPerSec)
    {
        return String.format(Locale.US, speedUnit(true), convertSpeed(speedMetersPerSec));
    }

    public double convertSpeed(double speedMetersPerSec)
    {
        double k=1;

        int speedUnit = settings.getSpeedUnitDistance();
        switch (speedUnit) {
            case SettingsKeeper.METERS:
                k = 1;
                break;
            case SettingsKeeper.KILOMETERS:
                k = 0.001;
                break;
            case SettingsKeeper.MILES:
                k = 1 / 1609.0;
                break;
        }

        speedUnit = settings.getSpeedUnitTime();
        switch (speedUnit) {
            case SettingsKeeper.SECOND:
                k = k * 1;
                break;
            case SettingsKeeper.MINUTE:
                k = k * 60;
                break;
            case SettingsKeeper.HOUR:
                k = k * 3600;
                break;
        }

        return  speedMetersPerSec*k;
    }

    public String speedUnit(boolean withFormat)
    {
        String unit="";
        String distanceUnits[]=context.getResources().getStringArray(R.array.distance_short_unit);
        String timeUnits[]=context.getResources().getStringArray(R.array.time_short_unit);
        String format="%.1f ";

        try {
            int speedUnit = settings.getSpeedUnitDistance();
            unit = distanceUnits[speedUnit];

            speedUnit = settings.getSpeedUnitTime();
            unit = unit + "/" + timeUnits[speedUnit];
        }catch(Exception e)
        {
            return e.toString();
        }

        if(withFormat)
            return format+unit;

        return unit;

    }
}
