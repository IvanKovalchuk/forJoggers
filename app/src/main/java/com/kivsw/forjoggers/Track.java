package com.kivsw.forjoggers;

import android.location.Location;

import java.util.ArrayList;

/**
 *   This class hold a track as a set of geo points
 */
public class Track {
    public ArrayList<Location> mGeoPoints=null;

    //----------------------------------------------------
    public Track()
    {
        mGeoPoints=new ArrayList<Location>();
    };

    //----------------------------------------------------
    /**  save points in a file
     * @param fileName file name
     * @return true if the saving was successful
     */
    public boolean saveGeoPoint(String fileName)
    {

    };
    //----------------------------------------------------
    /**
     * load points from file
     * @param fileName file name
     * @return true if the loading was successful
     */
    public boolean loadGeoPoint(String fileName)
    {

    };
    //----------------------------------------------------

    String toJSON()
    {

    }
    void fromJSON(String str)
    {

    }


}
