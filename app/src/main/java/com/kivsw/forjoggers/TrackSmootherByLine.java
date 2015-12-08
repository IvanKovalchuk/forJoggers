package com.kivsw.forjoggers;

import android.location.Location;

import java.util.ArrayList;

/**
 * Created by ivan on 02.12.15.
 */
public class TrackSmootherByLine extends TrackSmoother{

    //protected ArrayList<Location> mGeoPoints=null;
    iApproximator latApproximator=null, lngApproximator=null;
    protected int deltaT=1200000, deltaDistance=100;

    TrackSmootherByLine(Track track)
    {
        super(track);
        deltaT=1200000; deltaDistance=100;
    };

    protected void createApproximators()
    {
        if(latApproximator==null)
           latApproximator=new LineApproximator();
        if(lngApproximator==null)
           lngApproximator=new LineApproximator();
    }

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

        int s=track.getGeoPoints().size();
        mGeoPoints = doSmooth(track.getGeoPoints(), 0,s,  0,s, deltaT,deltaDistance);
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
    ArrayList<Location> doSmooth(ArrayList<Location> original, int begin, int end, int start, int stop, int deltaT, int deltaDistance)
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
                if ((l.getTime() > Tmin) && (deltaDistance > loc.distanceTo(l))) //Track.distance(l,loc)
                    break;
                avB++;
            };

            // looks for the last point of the interval
            while (avE < end) {
                Location l = original.get(avE);
                if ((l.getTime() > Tmax) || (deltaDistance < loc.distanceTo(l))) //Track.distance(l,loc)
                    break;
                avE++;
            };


            Location avLoc = getAvarageFor(i, avB, avE); // interpolates interval [avB, avE) with a line
           /* if(prevLoc!=null)
            {
                float distanceAndBearing[]=new float[3];
                Location.distanceBetween(prevLoc.getLatitude(), prevLoc.getLongitude(),
                                    avLoc.getLatitude(), avLoc.getLongitude(),distanceAndBearing);
                avLoc.setBearing(distanceAndBearing[1]);
                avLoc.setSpeed((float) distanceAndBearing[0]*1000f/(avLoc.getTime()-prevLoc.getTime()));
            }*/

            mResGeoPoints.add(avLoc);
            prevLoc = avLoc;
        }
        return mResGeoPoints;
       /* ArrayList<Location> original=track.getGeoPoints();
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
                if((l.getTime()>Tmin) && (deltaDistance>loc.distanceTo(l))) //Track.distance(l,loc)
                  break;
                avB++;
            };

            // looks for the last point of the interval
            while(avE<s){
                Location l=original.get(avE);
                if((l.getTime()>Tmax) || (deltaDistance<loc.distanceTo(l))  ) //Track.distance(l,loc)
                    break;
                avE++;
            };

            Location avLoc=getAvarageFor(i, avB, avE);
            mGeoPoints.add(avLoc);
        }*/
    };


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

            long deltaT=10;
            double latitude2=latApproximator.function(time+deltaT),
                   longitude2=lngApproximator.function(time+deltaT);

            loc.setLatitude(latitude);
            loc.setLongitude(longitude);

            float distanceAndBearing[]=new float[3];
            Location.distanceBetween(latitude, longitude, latitude2, longitude2,distanceAndBearing);

            loc.setBearing(distanceAndBearing[1]);
            loc.setSpeed((float) distanceAndBearing[0]/deltaT);
            /*loc.setBearing((float) Track.bearing(latitude, longitude, latitude2, longitude2));
            loc.setSpeed((float)Track.distance(latitude, longitude, latitude2, longitude2)/deltaT);*/
            return loc;
        }


        return original.get(ind);

    }



}
