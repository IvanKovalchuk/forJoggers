package com.kivsw.forjoggers.model;

import android.location.Location;
import android.os.SystemClock;

import com.kivsw.forjoggers.helper.SettingsKeeper;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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
        //void onError(Throwable msg);

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


    @Override
    public Track clone()
    {
        Track res=new Track();
        res.timeStart = timeStart;
        res.timeStop = timeStop;
        res.activityType = activityType;
        res.mGeoPoints = new ArrayList<Location>(mGeoPoints);

        return res;

    }

    public void assign(Track track)
    {
        timeStart = track.timeStart;
        timeStop = track.timeStop;
        activityType = track.activityType;
        mGeoPoints = (ArrayList<Location>)track.mGeoPoints.clone();
        if(onChange!=null)
            onChange.onAddPoint();
    }

    //-----------------------------------------------------
    /**  save points in a file
     * @param fileName file name
     * @return true if the saving was successful
     */
    public boolean saveGeoPoint(String fileName)
    {
       /* if(!fileName.matches(".*\\.gpx$"))
            fileName = fileName+".gpx";*/
        GpxConvertor gpx=new GpxConvertor(this);
        return gpx.saveToFile(fileName);

       /* try {
            FileWriter writer = new FileWriter(fileName);
            String str=toJSON();
            writer.write(str);
            writer.close();

        }catch(Exception e)
        {
            return false;
        }


        return true;*/
    };
    //----------------------------------------------------
    /**
     * load points from file
     * @param fileName file name
     * @return true if the loading was successful
     */
    public boolean loadGeoPoint(String fileName)
    {

        GpxConvertor gpx=new GpxConvertor(this);
        boolean r= gpx.loadFromFile(fileName);
        if(onChange!=null)
                onChange.onAddPoint();
        return r;

    };
    String toGPX()
    {
        GpxConvertor gpx=new GpxConvertor(this);
        Writer wr= new StringWriter();
        try {
            gpx.toGPX(wr);
            return wr.toString();
        }catch(Exception e)
        {
            return "";
        }

    }
    public boolean fromGPX(String str)
    {
        GpxConvertor gpx=new GpxConvertor(this);
        Reader rd= new StringReader(str);
        try {
            gpx.fromGPX(rd);
            if(onChange!=null)
                onChange.onAddPoint();
            return true;
        }catch(Exception e)
        {
            clear();
        }
        return false;
    }
    //----------------------------------------------------

   /* String toJSON()
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
                setActivityType (json.getInt("activityType"));
                return true;
            }
        }
        catch(Exception e)
        {e.toString();  }

        return false;
    }*/

    /**
     * get the track's duration out of its points
     * @return
     */
    public long getTrackPointsTime()
    {
        long t;
        int s = mGeoPoints.size(); // get track duration out of its points
        if (s < 2) return 0;
        Location firstLoc = mGeoPoints.get(0),
                lastLoc = mGeoPoints.get(s - 1);

        t = lastLoc.getTime() - firstLoc.getTime();

        return t;
    };
    public long getTrackTime()
    {
        long t;
        if( ((timeStart==0) && (timeStop==0))) {
            t = getTrackPointsTime();
        }
        else if((timeStart!=0) &&(timeStop==0))
           t= SystemClock.elapsedRealtime()-timeStart; // when the track is still being recorded
        else
           t=timeStop-timeStart;  // when the track is finished
        return t;

    };

    public String getTrackTimeStr()
    {
        long t=getTrackTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(new SimpleTimeZone(0, ""));
        return sdf.format(new Date(t));
    }

    /**
     *
     * @return the track length in meters
     */
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
        activityType= SettingsKeeper.JOGGING;
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

    public int getActivityType()
    {return activityType;}
    public void setActivityType(int v)
    {
        if(v<0) v=0;
        if(v>2) v=2;
        activityType=v;
    }

    public double getСalories(double weight)
    {
        Location prevLoc=null;
        double res=0;
        if(mGeoPoints==null) return 0;

        int activityType=getActivityType();
        for(Location loc:mGeoPoints)
        {
            double e=0;
            if(prevLoc!=null && loc.hasSpeed()) {
                double speed = loc.getSpeed();
                switch(activityType)
                {
                    case SettingsKeeper.HIKING:
                        e=hiking(speed);
                        break;
                    case SettingsKeeper.JOGGING:
                        e=jogging(speed);
                        break;
                    case SettingsKeeper.BICYCLING: e=bicycling(speed);
                        break;
                }
                e*=(loc.getTime()-prevLoc.getTime())/1000;
                if(e<0) e=0;
            }
            res = res + e;
            prevLoc=loc;
        }

        return res*weight;
    }

    //http://www.brianmac.co.uk/energyexp.htm
    // http://sportlib.su/Annuals/Bicycling/1986/p74-75.htm
    double workingSpeed[] ={4,  7,  10}, // km per hour
           workingEnegry[]={105./68/1800, 200./68/1800, 370./68/1800}; // calories per kg per second
    double joggingSpeed[]= {9,             10,            12,            16},
           jiggingEnergy[]={320./68/1800,  350./68/1800,  430./68/1800,  550./68/1800};
    double cyclingSpeed[]= {3.5,       9,            16,           21,           30,       35},
           cyclingEnergy[]={0.043/60,  120./68/1800, 220./68/1800, 320./68/1800, 0.250/60, 0.302/60};

    private double getEnergy(double currentSpeed, double speeds[], double energy[])
    {
        currentSpeed = currentSpeed*3.6; // translate meters per second to km to hour

        int i=0;
        while(i<speeds.length && currentSpeed>speeds[i])
            i++;
        if(i>=speeds.length){
            i=speeds.length-1;
            currentSpeed = speeds[i];
        }
        if(currentSpeed<speeds[0]) currentSpeed = speeds[0];
        if(currentSpeed>speeds[speeds.length-1]) currentSpeed=speeds[speeds.length-1];

        if(i==0) i=1;

        double k=(energy[i]-energy[i-1])/(speeds[i]-speeds[i-1]);
        double b= energy[i]-k*speeds[i];

        double r=k*currentSpeed+b;
        if(r<0) r=0;
        return r;
    }
    private double hiking(double speed)
    {return getEnergy(speed, workingSpeed, workingEnegry);};
    private double jogging(double speed)
    {return getEnergy(speed,joggingSpeed,jiggingEnergy);}
    private double bicycling(double speed)
    {return getEnergy(speed,cyclingSpeed,cyclingEnergy);}

 /*   // http://gotowalk.blogspot.ru/2014/06/Opredelenie-kolichestva-szhigaemyh-kalorij-pri-hodbe-bege.html
    // *  Определяем сколько калорий сжигается при ходьбе:
    // Е=0,007 x V^2 + 21,   где V -скорость ходьбы  в м/мин , Е—расход энергии (кал/кг/мин).
    // Определяем сколько сжигается калорий при беге:
    // Е =18,0 x V - 20,   где V—скорость бега (км/час), Е—расход энергии (кал/кг/мин).

    private double hiking(double speed)
    {
        speed = speed*60;
        double e=(0.007 * speed*speed+21)/60;
        return e;
    }
    private double jogging(double speed)
    {
        speed = speed*3.6;
        double e=((5.0 * speed)-20)/60;
        return e;
    }
    private double bicycling(double speed)
    {
        // http://sportlib.su/Annuals/Bicycling/1986/p74-75.htm
        // speed (km per hour)
        double speeds[]={0, 3.5,    8.5,    9,      10,    15,     20,      25,     30,      35};
        // enegry (cal) per a second
        double energy[]={0, 2.58,   3.3,    3.54,   4.2,   6.48, 8.52,  12,   15,    18.12 };

        speed = speed*3.6;

        int i=0;
        while(i<speeds.length && speed>speeds[i])
            i++;
        if(i>=speeds.length) i=speeds.length-1;
        if(i==0) i=1;

        double k=(energy[i]-energy[i-1])/(speeds[i]-speeds[i-1]);
        double b= energy[i]-k*speeds[i];

        double r=k*speed+b;
        if(r<k) r=0;
        return r;
    }*/

    protected void setOnChange(IOnChange onChange)
    {
        this.onChange = onChange;
    }
    public IOnChange getOnChange()
    {
        return this.onChange;
    }

    public ArrayList<Location> getGeoPoints()
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
