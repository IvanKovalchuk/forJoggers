package com.kivsw.forjoggers;

import android.location.Location;

import org.json.JSONObject;

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
        return false;
    };
    //----------------------------------------------------
    /**
     * load points from file
     * @param fileName file name
     * @return true if the loading was successful
     */
    public boolean loadGeoPoint(String fileName)
    {
        return false;
    };
    //----------------------------------------------------

    String toJSON()
    {
        StringBuffer builder=new StringBuffer();
        final String separator=",\n";
        builder.append('[');
        for (Location loc:mGeoPoints)
        try{
            builder.append(locationToJSON(loc)) ;
            builder.append(separator);
        }catch(org.json.JSONException e)
        {e.toString();}

        builder.setLength(builder.length() - separator.length());
        builder.append(']');

        return builder.toString();
    }

    void fromJSON(String str)
    {
        int start=0, end=0;

        while(-1!=(start = str.indexOf('{',end)))
        {
            end= str.indexOf('}',start+1);
            if(end<0) break;

            end++;
            try {
                Location loc = JSONtoLocation(str.substring(start, end));
                mGeoPoints.add(loc);
            }catch(org.json.JSONException e)
            {
                e.toString();
            }
        }

    }
    //--------------------------------------------------------
    private String locationToJSON(Location loc) throws org.json.JSONException
    {
        JSONObject json=new JSONObject();

        json.put("time", loc.getTime());
        json.put("latitude", loc.getLatitude());
        json.put("longitude", loc.getLongitude());

        if(loc.hasAccuracy())
            json.put("accuracy", loc.getAccuracy());
        if(loc.hasAltitude())
            json.put("altitude", loc.getAltitude());
        if(loc.hasBearing())
            json.put("bearing", loc.getBearing());
        if(loc.hasSpeed())
            json.put("speed", loc.getSpeed());

        return json.toString();
    }
    private Location JSONtoLocation(String jsonStr) throws org.json.JSONException
    {

        JSONObject json=new JSONObject(jsonStr);
        Location loc= new Location("");

        loc.setTime(json.getLong("time"));
        loc.setLatitude(json.getDouble("latitude"));
        loc.setLongitude(json.getDouble("longitude"));

        if(json.has("accuracy"))
           loc.setAccuracy((float)json.getDouble("accuracy"));

        if(json.has("altitude"))
            loc.setAltitude(json.getDouble("altitude"));

        if(json.has("bearing"))
            loc.setBearing((float)json.getDouble("bearing"));

        if(json.has("speed"))
            loc.setSpeed((float)json.getDouble("speed"));

        return loc;
    }


}
