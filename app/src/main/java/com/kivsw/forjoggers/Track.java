package com.kivsw.forjoggers;

import android.location.Location;
import android.os.SystemClock;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 *   This class hold a track as a set of geo points
 */
public class Track {
    //-----------------------------------------------------
    interface IOnChange
    {
        void onAddPoint();
        void onClear();

    }
    //-----------------------------------------------------
    protected ArrayList<Location> mGeoPoints=null;
    public long timeStart=0, timeStop=0;
    int activityType;
    IOnChange onChange=null;
    //-----------------------------------------------------
    public Track()
    {
        mGeoPoints=new ArrayList<Location>();
    };


    //-----------------------------------------------------
    /**  save points in a file
     * @param fileName file name
     * @return true if the saving was successful
     */
    public boolean saveGeoPoint(String fileName)
    {
        try {
            FileWriter writer = new FileWriter(fileName);
            String str=toJSON();
            writer.write(str);
            writer.close();

        }catch(Exception e)
        {
            return false;
        }


        return true;
    };
    //----------------------------------------------------
    /**
     * load points from file
     * @param fileName file name
     * @return true if the loading was successful
     */
    public boolean loadGeoPoint(String fileName)
    {
        try {
            File file = new File(fileName);

            StringBuilder data=new StringBuilder();
            long totalsize=file.length();
            int s;
            char buff[]= new char[8*1024];
            FileReader reader = new FileReader(fileName);
            while(data.length()<totalsize) {
                s = reader.read(buff);
                if(s<=0) break;
                data.append(buff,0,s);
            };
            reader.close();

            fromJSON(data.toString());

        }catch(Exception e)
        {
            return false;
        }


        return true;
    };
    //----------------------------------------------------

    String toJSON()
    {
        StringBuffer builder=new StringBuffer();
        final String separator=",\n";
        builder.append('[');

        builder.append(paramToJSON());

        if(mGeoPoints.size()>0) {
            for (Location loc : mGeoPoints)
                try {
                    builder.append(separator);
                    builder.append(locationToJSON(loc));
                } catch (org.json.JSONException e) {
                    e.toString();
                }
        }
        builder.append(']');

        return builder.toString();
    }

    void fromJSON(String str)
    {
        int start=0, end=0, i=0;

        //mGeoPoints.clear();
        clear();
        while(-1!=(start = str.indexOf('{',end)))
        {
            end= str.indexOf('}',start+1);
            if(end<0) break;

            end++;
            try {
                boolean isLocation=true;
                if(i==0)
                    isLocation=!loadParamFromJSON(str.substring(start, end));

                if(isLocation) {
                    Location loc = JSONtoLocation(str.substring(start, end));
                    addPoint(loc);

                }
            }catch(org.json.JSONException e)
            {
                e.toString();
            }
            i++;
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
           loc.setAccuracy((float) json.getDouble("accuracy"));

        if(json.has("altitude"))
            loc.setAltitude(json.getDouble("altitude"));

        if(json.has("bearing"))
            loc.setBearing((float) json.getDouble("bearing"));

        if(json.has("speed"))
            loc.setSpeed((float) json.getDouble("speed"));

        return loc;
    }
    private String paramToJSON()
    {
        try {
            JSONObject json = new JSONObject();
            json.put("timeStart", timeStart);
            json.put("timeStop", timeStop);
            json.put("activityType", activityType);

            return json.toString();
        }catch(Exception e)
        {
            e.toString();
        };
        return "{}";
    };
    private boolean loadParamFromJSON(String jsonStr)
    {
        timeStart =  timeStop =0;
        activityType=SettingsKeeper.JOGGING;
        try{
            JSONObject json=new JSONObject(jsonStr);

            if(json.has("timeStart") && json.has("timeStop") ) {
                timeStart = json.getLong("timeStart");
                timeStop = json.getLong("timeStop");
                activityType = json.getInt("activityType");
                return true;
            }
        }
        catch(Exception e)
        {e.toString();  }

        return false;
    }

    public long getTrackTime()
    {

        long t;
        if((timeStart==0) && (timeStop==0)) {
            int s = mGeoPoints.size();
            if (s < 2) return 0;
            Location firstLoc = mGeoPoints.get(0),
                    lastLoc = mGeoPoints.get(s - 1);

            t = lastLoc.getTime() - firstLoc.getTime();

        }
        else if((timeStart!=0) &&(timeStop==0))
           t= SystemClock.elapsedRealtime()-timeStart;
        else
           t=timeStop-timeStart;
        return t;

    };

    public String getTrackTimeStr()
    {
        long t=getTrackTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(new SimpleTimeZone(0, ""));
        return sdf.format(new Date(t));
    }

    public double getTrackDistance()
    {
        Location prevLoc=null;
        double dist=0;
        if(mGeoPoints==null) return 0;

         for(Location loc:mGeoPoints)
         {
             if(prevLoc!=null) {
                 //double d = distance(loc.getLatitude(), loc.getLongitude(), prevLoc.getLatitude(), prevLoc.getLongitude());
                 double d = loc.distanceTo(prevLoc);
                 dist += d;
             }
             prevLoc=loc;
         }

        return dist;

    }

    public void clear()
    {
        activityType=SettingsKeeper.JOGGING;
        mGeoPoints.clear();
        timeStart=0; timeStop=0;
        if(onChange!=null)
            onChange.onClear();
    }
    public void addPoint(Location loc)
    {
        mGeoPoints.add(loc);
        if(onChange!=null) onChange.onAddPoint();
    }

    public void setActivityType(int v)
    {
        activityType=v;
    }

    public double getСalories(double weight)
    {
        /**
         *  Определяем сколько калорий сжигается при ходьбе:
         Е=0,007 x V^2 + 21,   где V -скорость ходьбы  в м/мин , Е—расход энергии (кал/кг/мин).
         E=0,0001167 * v^2 + 21, где V -скорость ходьбы  в м/сек

         Определяем сколько сжигается калорий при беге:
         Е =18,0 x V - 20,   где V—скорость бега (км/час), Е—расход энергии (кал/кг/мин).
         E=5.0 * v - 20, где V -скорость ходьбы  в м/сек
         */

        Location prevLoc=null;
        double res=0;
        if(mGeoPoints==null) return 0;

        for(Location loc:mGeoPoints)
        {
            double e=0;
            if(prevLoc!=null && loc.hasSpeed()) {
                switch(activityType)
                {
                    case SettingsKeeper.HIKING:
                        e=(0.0001167 * loc.getSpeed()*loc.getSpeed()+21);
                        break;
                    case SettingsKeeper.JOGGING:
                        e=(5.0 * loc.getSpeed()-20);
                        break;
                    case SettingsKeeper.BICYCLING: e=0;
                        break;
                }
                e*=(loc.getTime()-prevLoc.getTime())/60;
            }
            res = res + e;
            prevLoc=loc;
        }

        return res;
    }

    public void setOnChange(IOnChange onChange)
    {
        this.onChange = onChange;
    }
    public IOnChange getOnChange()
    {
        return this.onChange;
    }

    ArrayList<Location> getGeoPoints()
    {
        return mGeoPoints;
    }

//----------------------------------------------------------------------------------------------
    final static double earthRadius = 6371000; // the average earth radius
    /** Calculates the distance between two points
    *
     */
    public static double _distance (Location a,Location b)
    {return _distance(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());}
    public static double _distance (double Lat1,double Lng1,double Lat2,double Lng2)
    {
        double latRadius = earthRadius*Math.cos(Math.toRadians(Lat1));

        double dLat = Math.abs(Lat1-Lat2),
                dLon=Math.abs(Lng1-Lng2);

        if(dLat>180) dLat-=360;
        if(dLon>180) dLon-=360;

        double dy = earthRadius *Math.toRadians(dLat),
                dx = latRadius *Math.toRadians(dLon);

        double d= Math.sqrt(dy*dy+dx*dx);

        return d;
    }

    /** Calculates the bearing from the first points to the second one
     *
     */
    public static double _bearing (Location a,Location b)
    {
        return _bearing(a.getLatitude(), a.getLongitude(), b.getLatitude(), b.getLongitude());
    };
    public static double _bearing(double Lat1,double Lng1,double Lat2,double Lng2)
    {
      /*  double lat1 = Math.toRadians(Lat1),//φ1
            lat2 = Math.toRadians(Lat2),//φ2
            dLat = Math.toRadians((Lat2-Lat1)),//Δφ
            dLon = Math.toRadians((Lng2-Lng1)); // Δλ
        //θ = atan2( sin Δλ ⋅ cos φ2 , cos φ1 ⋅ sin φ2 − sin φ1 ⋅ cos φ2 ⋅ cos Δλ );
        double bearing=
        Math.atan2(
           Math.sin(dLon) + Math.cos(lat2),
           Math.cos(lat1)*Math.sin(lat2) - Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon));

        bearing = Math.toDegrees(bearing);
        return bearing;*/
        double latRadius = earthRadius*Math.cos(Math.toRadians(Lat1));

        double dLat = Lat2-Lat1,
               dLon=Lng2-Lng1;

        if(dLat>180) dLat-=360;
        if(dLon>180) dLon-=360;

        double dy = earthRadius *Math.toRadians(dLat),
                dx = latRadius *Math.toRadians(dLon);
        double bearing= Math.atan2(dx,dy);
        bearing = Math.toDegrees(bearing);

        return bearing;
    }

    public static double turn(double angle1, double angle2)
    {
        double r=angle2-angle1;

        if(r>180) r-=360;
        if(r<-180) r+=360;

        return r;
    }
}
