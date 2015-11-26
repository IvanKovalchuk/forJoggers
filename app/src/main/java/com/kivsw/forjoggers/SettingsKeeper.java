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
    public  int getZoomLevel()
    {
        return sharedPreferences.getInt("zoom",2);
    };
    public void setZoomLevel(int zoom)
    {
        SharedPreferences.Editor e= sharedPreferences.edit();
        e.putInt("zoom",zoom);
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
}
