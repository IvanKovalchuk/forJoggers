package com.kivsw.forjoggers;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by ivan on 02.12.15.
 */
public class TrackSmootherByLine extends TrackSmoother{

    //protected ArrayList<Location> mGeoPoints=null;
    LineApproximator latApproximator, lngApproximator;

    TrackSmootherByLine(Track track)
    {
        super(track);
        latApproximator=new LineApproximator();
        lngApproximator=new LineApproximator();
    };

    ArrayList<Location> getGeoPoints()
    {
        if(mGeoPoints==null)
            doSmooth();
         return mGeoPoints;
    }

    /**
     *  when a new points was added
     */
    @Override
    public void onAddPoint() {
        mGeoPoints=null;
    }

    /**
     *  when points were cleared
     */
    @Override
    public void onClear() {
        mGeoPoints=null;
    }

    //-----------------------------------------------------
    void doSmooth()
    {
        ArrayList<Location> original=track.getGeoPoints();
        int s= original.size();

        mGeoPoints=new ArrayList<Location>(s);

        int avE=0, avB=0;
        for(int i=0;i<s;i++)
        {
            Location loc=original.get(i);
            long Tmin= loc.getTime()-deltaT, Tmax=loc.getTime()+deltaT;
            // looks for the first point of the interval
            while(avB<i){
                Location l=original.get(avB);
                if((l.getTime()>Tmin) && (deltaDistance>Track.distance(l,loc)))
                  break;
                avB++;
            };

            // looks for the last point of the interval
            while(avE<s){
                Location l=original.get(avE);
                if((l.getTime()>Tmax) || (deltaDistance<Track.distance(l,loc))  )
                    break;
                avE++;
            };

            Location avLoc=getAvarageFor(i, avB, avE);
            mGeoPoints.add(avLoc);
        }
    };


    protected int deltaT=60000, deltaDistance=60;
    protected Location getAvarageFor(int ind, int b, int e)
    {
        ArrayList<Location> original=track.getGeoPoints();
        if(b<0) b=0;
        if(e>original.size()) e=original.size();
        if(b>e)
            b=e;

        double t[]=new double[e-b], lng[]=new double[e-b], lat[]=new double[e-b];
        long t0=original.get(b).getTime();

        for(int j=0,i=b;  i<e;  i++,j++)
        {
            Location loc=original.get(i);
            t[j] = ((loc.getTime()-t0)/1000.0);
            lat[j]=loc.getLatitude();
            lng[j]=loc.getLongitude();
        }

        if(latApproximator.approximate(t,lat) && lngApproximator.approximate(t,lng))
        {
            Location loc=new Location(original.get(ind));
            double time=(original.get(ind).getTime()-t0)/1000.0;
            double latitude=latApproximator.function(time),
                    longitude=lngApproximator.function(time);
            loc.setLatitude(latitude);
            loc.setLongitude(longitude);
            return loc;
        }


        return original.get(ind);

    }



}
