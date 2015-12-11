package com.kivsw.forjoggers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by ivan on 11.11.15.
 */
public class SettingsKeeper {
    private static SettingsKeeper settingKeeper=null;
    public synchronized static SettingsKeeper getInstance(Context context)
    {
        if(settingKeeper==null)
        {
            settingKeeper = new SettingsKeeper(context);
        }
        return settingKeeper;
    }


    private SharedPreferences sharedPreferences;
    private SettingsKeeper(Context context)
    {
        sharedPreferences =  context.getSharedPreferences("settings",Context.MODE_PRIVATE);

    }

    //-----------------------------------------------------

    /**
     * Set and get for the last zoom and the last position
     * on the map
     * @return
     */
    public double getLastLatitude()
    {
        try {
            long s = sharedPreferences.getLong("latitude", 0);
            return Double.longBitsToDouble(s);
        }catch(Exception e)
        {}
        return 0;
    }
    public double getLastLongitude()
    {
        try {
            long s=sharedPreferences.getLong("longitude", 0);
            return Double.longBitsToDouble(s);
        }catch(Exception e)
        {}
        return 0;
    }
    public  int getZoomLevel()
    {
        return sharedPreferences.getInt("zoom", 2);
    };
    public void setZoomLevel(int zoom, double lat, double lng)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("zoom", zoom);
        e.putLong("latitude", Double.doubleToLongBits(lat));
        e.putLong("longitude", Double.doubleToLongBits(lng));
        e.commit();
    }

    //-----------------------------------------------------
    public  String getCurrentTrack()
    {
        return sharedPreferences.getString("currentTrack", "");
    };
    public void setCurrentTrack(String t)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putString("currentTrack", t);
        e.commit();
    }
    //-----------------------------------------------------
    public  String getCurrentFileName()
    {
        return sharedPreferences.getString("FileName", null);
    };
    public void setCurrentFileName(String fn)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putString("FileName", fn);
        e.commit();
    }
    //-----------------------------------------------------
    //-----------------------------------------------------
    public  boolean getReturnToMyLocation()
    {
        return sharedPreferences.getBoolean("ReturnToMyLocation", true);
    };
    public void setReturnToMyLocation(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("ReturnToMyLocation", v);
        e.commit();
    }
    //-----------------------------------------------------
    public  String getLastPath()
    {
        return sharedPreferences.getString("LastPath", null);
    };
    public void setLastPath(String t)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putString("LastPath", t);
        e.commit();
    }
    //-----------------------------------------------------
    public int getMyWeight()
    {
        int v= sharedPreferences.getInt("myWeight", 70);
        return v;
    }
    public double getMyWeightKg()
    {
        double w=getMyWeight();
        if(getMyWeightUnit()==LB)
            w=(w*0.45359237f);
        return w;
    }
    /**
     *
     * @param weight is my weight
     * @param weightUnits the weight's unit. 0-kg, 1-lb
     */
    static final int KG = 0, LB = 1;
    public int getMyWeightUnit()
    {
        return sharedPreferences.getInt("weightUnits", 0);
    }

    public void setMyWeight(int weight, int weightUnits)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("myWeight", weight);
        e.putInt("weightUnits", weightUnits);
        e.commit();
    }
    //-------------------------------------------------------------
    /**
     *
     * @return
     */
    static final int HIKING=0, JOGGING=1, BICYCLING=2;
    public int getActivityType()
    {
        return sharedPreferences.getInt("activityType", 1);
    }
    public  void setActivityType(int activityType)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("activityType", activityType);
        e.commit();
    }
    //-------------------------------------------------------------
    static final int METERS=0, KILOMETERS=1, MILES=2;
    public int getDistanceUnit()
    {
        return sharedPreferences.getInt("distanceUnit", 1);
    }
    public  void setDistanceUnit(int distanceUnit)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("distanceUnit", distanceUnit);
        e.commit();
    }
    //-------------------------------------------------------------
    static final int SECOND=0, MINUTE=1, HOUR=2;
    public int getSpeedUnitTime()
    {
        return sharedPreferences.getInt("timeUnit", 1);
    }
    public int getSpeedUnitDistance()
    {
        return sharedPreferences.getInt("speedDistanceUnit", 1);
    }
    public  void setSpeedUnit(int distanceUnit, int timeUnit)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("timeUnit", timeUnit);
        e.putInt("speedDistanceUnit", distanceUnit);
        e.commit();
    }
    //-------------------------------------------------------------

}
