package com.kivsw.forjoggers.helper;

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
  /*  public  String getCurrentTrack()
    {
        return sharedPreferences.getString("currentTrack", "");
    };
    public void setCurrentTrack(String t)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putString("currentTrack", t);
        e.commit();
    }*/
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
    public static final int HIKING=0, JOGGING=1, BICYCLING=2;
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
    public static final int METERS=0, KILOMETERS=1, MILES=2;
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
    public static final int SECOND=0, MINUTE=1, HOUR=2;
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

    public  boolean getKeepBackGround()
    {
        return sharedPreferences.getBoolean("keepBackground", false);
    };
    public void setKeepBackGround(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("keepBackground", v);
        e.commit();
    }
    //-----------------------------------------------------
    // sets autostop for distance
    public  boolean getAutoStopDistance()
    {
        return sharedPreferences.getBoolean("autoStopDistance", false);
    };
    public void setAutoStopDistance(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("autoStopDistance", v);
        e.commit();
    }

    public  int getAutoStopDistanceUnit()
    {
        return sharedPreferences.getInt("autoStopDistanceUnit", 0);
    };
    public void setAutoStopDistanceUnit(int v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("autoStopDistanceUnit", v);
        e.commit();
    }

    public  double getAutoStopDistanceValue()
    {
        long v=sharedPreferences.getLong("autoStopDistanceValue", 0);
        return Double.longBitsToDouble(v);
    };
    double getAutoStopDistanceMeters()
    {
        double v=getAutoStopDistanceValue();
        switch(getAutoStopDistanceUnit())
        {
            case METERS:
                break;
            case KILOMETERS:
                v*=1000;
                break;
            case MILES:
                v*=1609;
                break;
        }
        return v;
    }
    public void setAutoStopDistanceValue(double v, int type)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        long vl= Double.doubleToLongBits(v);
        e.putLong("autoStopDistanceValue", vl);
        e.commit();
    }
    //-----------------------------------------------------
    // sets autostop for time
    public  boolean getAutoStopTime()
    {
        return sharedPreferences.getBoolean("autoStopTime", false);
    };
    public void setAutoStopTime(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("autoStopTime", v);
        e.commit();
    }

    public  int getAutoStopTimeUnit()
    {
        return sharedPreferences.getInt("autoStopTimeUnit", 0);
    };
    public void setAutoStopTimeUnit(int v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("autoStopTimeUnit", v);
        e.commit();
    }

    public void setAutoStopTimeValue(long v, int type)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putLong("autoStopDistanceValue", v);
        e.commit();
    }
    public  long getAutoStopTimeValue()
    {
        long v=sharedPreferences.getLong("autoStopTimeValue", 0);
        return v;
    };
    public  long getAutoStopTimeSeconds()
    {
        long v=getAutoStopTimeValue();
        switch(getAutoStopTimeUnit())
        {
            case SECOND:
                break;
            case MINUTE:
                v*=60;
                break;
            case HOUR:
                v*=60*60;
                break;
        }
        return v;
    };
}
