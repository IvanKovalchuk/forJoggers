package com.kivsw.forjoggers.model.track;

import android.location.Location;

import com.kivsw.forjoggers.model.math.LineApproximator;
import com.kivsw.forjoggers.model.math.iApproximator;

import java.util.ArrayList;

/**
 * Created by ivan on 11/3/16.
 */

public class TrackSmootherByTurnAndPolynom extends TrackSmootherByPolynom {

    ArrayList <Double> turnsArray=null;
    iApproximator latBearingApproximator, lngBearingApproximator;
    TrackSmootherByTurnAndPolynom(Track track)
    {
        super(track);
        latBearingApproximator=new LineApproximator();
        lngBearingApproximator=new LineApproximator();

        //deltaT=15000; deltaDistance=40;
    }

    public double getTurn(int index)
    {
        return turnsArray.get(index);
    }
    public void doSmooth()
    {
        calcTurns();
        super.doSmooth();
    }

    protected void calcTurns()
    {
        if(track==null) return;
        int s=track.getGeoPoints().size();
        turnsArray=calcTurns(track.getGeoPoints(), 0,s,  0,s);
    };
    protected ArrayList <Double> calcTurns(ArrayList< Location > original, int begin, int end, int start, int stop)
    {
        ArrayList <Double> turns=new ArrayList <Double>(stop-start);

        if(begin<0) begin=0;
        if(end>original.size()) end=original.size();
        if(start<begin) start=begin;
        if(stop>end) stop=end;

        int avE=begin, avB=begin;

        for(int i=start;i<stop;i++) {
            Location loc = original.get(i);
            long Tmin = loc.getTime() - deltaT, Tmax = loc.getTime() + deltaT;
            // looks for the first point of the interval
            while (avB < i) {
                Location l = original.get(avB);

                if(super.nearEnough(i,loc,l))
                    break;
                avB++;
            };

            // looks for the last point of the interval
            while (avE < end) {
                Location l = original.get(avE);
                // if ((l.getTime() > Tmax) || (deltaDistance < loc.distanceTo(l)))
                if(!super.nearEnough(i,loc,l))
                    break;
                avE++;
            };


            double turn = getTurnFor( i, avB, avE); // interpolates interval [avB, avE) with a line

            turns.add(turn);

        }
        return turns;
    };

    protected double getTurnFor(int ind, int b, int e)
    {
        final int minPoints=10;
        if( ((ind-b)<minPoints) || ((e-ind)<minPoints) ) return 0;


        double b0=getBearing(b, ind+1 );
        double b1=getBearing(ind, e);

        double turn=b1-b0;
        while (turn>180) turn-=360;
        while (turn<-180) turn+=360;

        return turn;
    }
    protected double getBearing(int begin, int end )
    {

        ArrayList<Location> original=track.getGeoPoints();
        double bearing=0;
        if(begin<0) begin=0;
        if(end>original.size()) end=original.size();
        if(begin>end)
            begin=end;

        double t[]=new double[end-begin], lng[]=new double[end-begin], lat[]=new double[end-begin];
        long t0=original.get(begin).getTime();

        for(int j=0,i=begin;  i<end;  i++,j++)
        {
            Location loc=original.get(i);
            t[j] = ((loc.getTime()-t0)/1000.0);
            lat[j]=loc.getLatitude();
            lng[j]=loc.getLongitude();
        }

        if(latBearingApproximator.approximate(t,lat) && lngBearingApproximator.approximate(t,lng))
        {
            int ind = end-1;
            double time=(original.get(ind).getTime()-t0)/1000.0;

            long deltaTime=2;
            double latitude0, longitude0, latitude, longitude;

            latitude=latBearingApproximator.function(time);
            longitude=lngBearingApproximator.function(time);

            latitude0 = latBearingApproximator.function(time - deltaTime);
            longitude0 = lngBearingApproximator.function(time - deltaTime);


            float distanceAndBearing[]=new float[3];
            Location.distanceBetween(latitude0, longitude0, latitude, longitude,distanceAndBearing);

            bearing = distanceAndBearing[1];

        }

        return bearing;
    }

    boolean nearEnough(int originIndex, Location origin,  Location neighbour)
    {
        long dT=Math.abs(origin.getTime()-neighbour.getTime());
        double dd=origin.distanceTo(neighbour);
        double scale = Math.abs(getTurn(originIndex)/60);
        if(scale<1) scale=1;

        // return (dT<=deltaT && dd<=deltaDistance);
        return ( (dT/(double)deltaT + scale*dd/deltaDistance) < 1.5);

    }
}
