package com.kivsw.forjoggers.helper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class keeps all the setting with SharedPreferences
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
        e.apply();
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
        e.apply();
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
        e.apply();
    }
    //-----------------------------------------------------
    public  String getLastDirPath()
    {
        return sharedPreferences.getString("LastPath", null);
    };
    public void setLastDirPath(String t)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putString("LastPath", t);
        e.apply();
    }
    //-----------------------------------------------------
    public FlexibleWeight getMyWeight()
    {
        FlexibleWeight r=new FlexibleWeight();
        r.load(sharedPreferences,"myWeight");
        return r;

    }

    public void setMyWeight(FlexibleWeight weight)
    {
        weight.save(sharedPreferences,"myWeight");
    }
    //-------------------------------------------------------------
    /**
     *
     * @return
     */
    public static final int HIKING=0, JOGGING=1, BICYCLING=2;
    public int getActivityType()
    {
        return sharedPreferences.getInt("activityType", JOGGING);
    }
    public  void setActivityType(int activityType)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("activityType", activityType);
        e.apply();
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
        e.apply();
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
        e.apply();
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
        e.apply();
    }
    //-----------------------------------------------------
    public  boolean getUseCompass()
    {
        return sharedPreferences.getBoolean("UseCompass", true);
    };
    public void setUseCompass(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("UseCompass", v);
        e.apply();
    }
    //-----------------------------------------------------
    // sets autostop for distance
    public  boolean getIsDistanceAutoStop()
    {
        return sharedPreferences.getBoolean("IsDistanceAutoStop", false);
    };
    public void setIsDistanceAutoStop(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("IsDistanceAutoStop", v);
        e.apply();
    }

    public FlexibleDistance getAutoStopDistance()
    {
        FlexibleDistance r=new FlexibleDistance();
        r.setDefault(5000,METERS);
        r.load(sharedPreferences,"autoStopDistance");
        return r;
    }
    public void setAutoStopDistance(FlexibleDistance d)
    {
        d.save(sharedPreferences,"autoStopDistance");
    }
    //-----------------------------------------------------
    // sets autostop for time
    public  boolean getIsAutoStopTime()
    {
        return sharedPreferences.getBoolean("autoStopTime", false);
    };
    public void setIsAutoStopTime(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("autoStopTime", v);
        e.apply();
    }
    public FlexibleTime getAutoStopTime()
    {
        FlexibleTime r=new FlexibleTime();
        r.setDefault(1,HOUR);
        r.load(sharedPreferences,"autoStopTime");
        return r;
    };
    public void setAutoStopTime(FlexibleTime t)
    {
        t.save(sharedPreferences,"autoStopTime");
    };

    //-----------------------------------------
    public void setTTS_engine(String engine)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putString("TTSengine", engine);
        e.apply();
    }
    public  String getTTS_engine()
    {
        String v=sharedPreferences.getString("TTSengine", "");
        return v;
    };

    /**
     * pronounce all the start stop events
     * @return
     */
    public  boolean getIsStartStopSpeaking()
    {
        return sharedPreferences.getBoolean("StartStopSpeaking", false);
    };
    public void setIsStartStopSpeaking(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("StartStopSpeaking", v);
        e.apply();
    }
    // sets distance speaking
    public  boolean getIsDistanceSpeaking()
    {
        return sharedPreferences.getBoolean("IsDistanceSpeaking", false);
    };
    public void setIsDistanceSpeaking(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("IsDistanceSpeaking", v);
        e.apply();
    }

    public FlexibleDistance getDistanceSpeaking()
    {
        FlexibleDistance r=new FlexibleDistance();
        r.setDefault(500,METERS);
        r.load(sharedPreferences, "DistanceSpeaking");
        return r;
    }
    public void setDistanceSpeaking(FlexibleDistance v)
    {
        v.save(sharedPreferences, "DistanceSpeaking");
    }
    // sets time speaking
    public  boolean getIsTimeSpeaking()
    {
        return sharedPreferences.getBoolean("IsTimeSpeaking", false);
    };
    public void setIsTimeSpeaking(boolean v)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putBoolean("IsTimeSpeaking", v);
        e.apply();
    }
    public FlexibleTime getTimeSpeaking()
    {
        FlexibleTime r=new FlexibleTime();
        r.setDefault(2,MINUTE);
        r.load(sharedPreferences, "TimeSpeaking");
        return r;
    }
    public void setTimeSpeaking(FlexibleTime v)
    {
        v.save(sharedPreferences, "TimeSpeaking");
    }

    /**
     *  hold data value and its units
     */
    public static class FlexibleData
    {
        protected int unit, defaultUnit;
        protected long value,defaultValue;

        FlexibleData()
        {
            unit=0; value=0; defaultValue=0; defaultUnit=0;
        }

        void save(SharedPreferences sharedPreferences, String name)
        {
            SharedPreferences.Editor e= sharedPreferences.edit();

            e.putLong(name+"Value", value);
            e.putInt(name+"Unit", unit);
            e.apply();
        }

        void load(SharedPreferences sharedPreferences,String name)
        {
            value=sharedPreferences.getLong(name+"Value",defaultValue);
            unit = sharedPreferences.getInt(name+"Unit",defaultUnit);
        }


        protected double getDoubleValue() {
            return Double.longBitsToDouble(value);
        }

        protected void setDoubleValue(double value) {
            this.value = Double.doubleToLongBits(value);
        }
        public int getUnit() {
            return unit;
        }

    }

    public static class FlexibleTime extends FlexibleData
    {
        public FlexibleTime(){super();}
        public FlexibleTime(long t, int u)
          {value=t; unit=u;}

        public void setDefault(long dt, int u) { defaultValue=dt; defaultUnit=u;};
        public long getTime() {return value;};
        public long getTimeSeconds()
        {
            long v=value;
            switch(unit)
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
        }
    }


    public static class FlexibleDistance extends FlexibleData
    {
        public FlexibleDistance(){super();}
        public FlexibleDistance(double d, int u)
          {setDoubleValue(d); unit=u;};

        public void setDefault(double dd, int u) { defaultValue=Double.doubleToLongBits(dd); defaultUnit=u;};
        public double getDistance() {return getDoubleValue();};
        public double getDistanceMeters()
        {
            double v=getDoubleValue();
            switch(unit)
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
    }

    static final int KG = 0, LB = 1;
    public static class FlexibleWeight extends FlexibleData
    {
        public FlexibleWeight()
          {super(); defaultValue=70;}
        public FlexibleWeight(long w, int u)
          {value=w; unit=u; defaultUnit=70;};

        public long getWeight() {return value;};
        public double getWeightKg()
        {
            double w=value;
                if(unit==LB)
                    w=(w*0.45359237f);
            return w;
        }
    }

}
