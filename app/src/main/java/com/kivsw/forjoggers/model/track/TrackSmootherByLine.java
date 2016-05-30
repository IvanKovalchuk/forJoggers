package com.kivsw.forjoggers.model.track;

import android.location.Location;

import com.kivsw.forjoggers.model.math.LineApproximator;
import com.kivsw.forjoggers.model.math.iApproximator;

import java.util.ArrayList;

/**
 * Created by ivan on 02.12.15.
 */
public class TrackSmootherByLine extends TrackSmoother {

    //protected ArrayList<Location> mGeoPoints=null;
    iApproximator latApproximator=null, lngApproximator=null;
    protected int deltaT,   // milliseconds
                 deltaDistance; // meters

    public TrackSmootherByLine(Track track)
    {
        super(track);
        //deltaT=20000; deltaDistance=600;
        deltaT=15000; deltaDistance=40;
    };

    protected void createApproximators()
    {
        if(latApproximator==null)
           latApproximator=new LineApproximator();
        if(lngApproximator==null)
           lngApproximator=new LineApproximator();
    }

    public ArrayList<Location> getGeoPoints()
    {
        if(mGeoPoints==null)
            doSmooth();
         return mGeoPoints;
    }

    /**
     *  when a new points was added
     */
 /*   @Override
    public void onAddPoint() {
        mGeoPoints=null;
    }

    /**
     *  when points were cleared
     */
  /*  @Override
    public void onClear() {
        mGeoPoints=null;
    }*/

    //-----------------------------------------------------
    @Override
    public void doSmooth()
    {
        if(track==null) return;
        int s=track.getGeoPoints().size();
        mGeoPoints = doSmooth(track.getGeoPoints(), 0,s,  0,s);
    }

    /**
     *
     * @param original real data to be smoothed
     * @param begin the first available item of real data
     * @param end   the end of real data
     * @param start the first item to bew smoothed
     * @param stop  the last+1 item to be smoothed
     * @return an array of smoothed locations from (start) to (stop-1)
     */
    ArrayList<Location> doSmooth(ArrayList<Location> original, int begin, int end, int start, int stop)
    {
        createApproximators();
        if(begin<0) begin=0;
        if(end>original.size()) end=original.size();
        if(start<begin) start=begin;
        if(stop>end) stop=end;

        ArrayList<Location> mResGeoPoints=new ArrayList<Location>(stop-start);
        int avE=begin, avB=begin;
        Location prevLoc=null;

        for(int i=start;i<stop;i++) {
            Location loc = original.get(i);
            long Tmin = loc.getTime() - deltaT, Tmax = loc.getTime() + deltaT;
            // looks for the first point of the interval
            while (avB < i) {
                Location l = original.get(avB);
                //if ((l.getTime() > Tmin) && (deltaDistance > loc.distanceTo(l)))
               if(nearEnough(loc,l))
                    break;
                avB++;
            };

            // looks for the last point of the interval
            while (avE < end) {
                Location l = original.get(avE);
                // if ((l.getTime() > Tmax) || (deltaDistance < loc.distanceTo(l)))
                if(!nearEnough(loc,l))
                    break;
                avE++;
            };


            Location avLoc = getAvarageFor(i, avB, avE); // interpolates interval [avB, avE) with a line

            mResGeoPoints.add(avLoc);
            prevLoc = avLoc;
        }
        return mResGeoPoints;

    };

    boolean nearEnough(Location origin, Location neighbour)
    {
        long dT=Math.abs(origin.getTime()-neighbour.getTime());
        double dd=origin.distanceTo(neighbour);

        // return (dT<=deltaT && dd<=deltaDistance);
        return ( (0.2*dT/(double)deltaT + dd/deltaDistance) < 1.5);

    }

    /** interpolates interval [avB, avE) with a line
     *  and return the approximated value for (ind)
     */

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

            long deltaT=2;
            double latitude0=latApproximator.function(time-deltaT),
                   longitude0=lngApproximator.function(time-deltaT);

            loc.setLatitude(latitude);
            loc.setLongitude(longitude);

            float distanceAndBearing[]=new float[3];
            Location.distanceBetween(latitude0, longitude0, latitude, longitude,distanceAndBearing);

            loc.setBearing(distanceAndBearing[1]);
            loc.setSpeed((float) distanceAndBearing[0]/deltaT);
            /*loc.setBearing((float) Track.bearing(latitude, longitude, latitude2, longitude2));
            loc.setSpeed((float)Track.distance(latitude, longitude, latitude2, longitude2)/deltaT);*/
            return loc;
        }


        return original.get(ind);

    }

    //--------------------------------------------------------------------------
    void thinOutGeoPoints()
    { // thin out points
        if(mGeoPoints.size()<5) return;
        ArrayList<Location> origin = track.getGeoPoints();
        int s=origin.size();

        ArrayList<Location> result=new ArrayList<Location>(s);

        result.add(mGeoPoints.get(0));
        Location lastLoc=mGeoPoints.get(0);
        final int maxDistance=0, maxDeltaT=deltaT/4;
        for(int i=1; i<s-1; i++)
        {
            Location loc=mGeoPoints.get(i);
            double turn = turn(lastLoc.getBearing(), loc.getBearing());
            long time=(loc.getTime()-lastLoc.getTime());

            if((lastLoc.distanceTo(loc)>maxDistance) || (loc.getTime()-lastLoc.getTime()>2) || (turn>20) || time>maxDeltaT)
            {
                lastLoc=loc;
                result.add(lastLoc);
            }
        }
        result.add(mGeoPoints.get(s-1));

        mGeoPoints = result;

    }



}
