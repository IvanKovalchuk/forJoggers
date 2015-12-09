package com.kivsw.forjoggers;

import android.content.Context;

/**
 * Created by ivan on 20.11.15.
 */
public class CurrentTrack extends Track {
    static private CurrentTrack track=null;
    SettingsKeeper settings=null;
    String fileName;

    static synchronized  public CurrentTrack getInstance(Context context)
    {
        if(track==null)
        {
            track = new CurrentTrack();
            track.settings = SettingsKeeper.getInstance(context);

            track.fromJSON(track.settings.getCurrentTrack());
        }
        return track;
    }

    static synchronized public void saveTrack()
    {
        if(track==null) return;
        track.settings.setCurrentTrack( track.toJSON());
    }

    private CurrentTrack()
    {
        super();
        fileName="";
    }

    @Override
    public void clear()
    {
        super.clear();
        fileName="";
    }

    //----------------------------------------------------
    /**  save points in a file
     * @param fileName file name
     * @return true if the saving was successful
     */
    public boolean saveGeoPoint(String fileName)
    {
        if(super.saveGeoPoint(fileName)) {
            this.fileName = fileName;
            return true;
        }
        return false;
    }

    /**
     * load points from file
     * @param fileName file name
     * @return true if the loading was successful
     */
    public boolean loadGeoPoint(String fileName) {
        if( super.loadGeoPoint(fileName)) {
            this.fileName = fileName;
            return true;
        }
        return false;
    }

    public boolean needToBeSaved()
    {
        return (fileName==null || fileName.isEmpty()) && (mGeoPoints.size()>0);
    }
}
