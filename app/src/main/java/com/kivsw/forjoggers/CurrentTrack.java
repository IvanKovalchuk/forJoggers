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

            String fn=track.settings.getCurrentFileName();
            if(fn!=null && !fn.isEmpty())
               track.loadGeoPoint(fn);
            else
                track.fromGPX(track.settings.getCurrentTrack());
        }
        return track;
    }

    static synchronized public void saveTrack()
    {
        if(track==null) return;
        track.settings.setCurrentTrack( track.toGPX());
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
        settings.setCurrentFileName("");
    }

    //----------------------------------------------------
    /**  save points in a file
     * @param fileName file name
     * @return true if the saving was successful
     */
    public boolean saveGeoPoint(String fileName)
    {
      /*  if(!fileName.matches(".*\\.gpx$"))
            fileName = fileName+".gpx";*/

            if(super.saveGeoPoint(fileName)) {
            this.fileName = fileName;
            settings.setCurrentFileName(fileName);

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
        boolean r=false;
        r=super.loadGeoPoint(fileName);

        if(r)
        {
            this.fileName = fileName;
            settings.setCurrentFileName(fileName);
            return true;
        }
        return false;
    }

    public boolean needToBeSaved()
    {
        return (fileName==null || fileName.isEmpty()) && (mGeoPoints.size()>1);
    }
}
