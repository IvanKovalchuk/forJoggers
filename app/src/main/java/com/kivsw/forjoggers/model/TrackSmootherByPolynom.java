package com.kivsw.forjoggers.model;

import android.location.Location;

import com.kivsw.forjoggers.model.PolinomApproximator;
import com.kivsw.forjoggers.model.Track;
import com.kivsw.forjoggers.model.TrackSmootherByLine;

import java.util.ArrayList;

/**
 * Created by ivan on 04.12.15.
 */
public class TrackSmootherByPolynom extends TrackSmootherByLine {

    public TrackSmootherByPolynom(Track track)
    {
        super(track);
        deltaT=20000; deltaDistance=600;
    }

    @Override
    protected void createApproximators()
    {
        if(latApproximator==null) {
            latApproximator = new PolinomApproximator(2);
        }
        if(lngApproximator==null) {
            lngApproximator = new PolinomApproximator(2);
        }

        //deltaT=1200000; deltaDistance=100;

    };//*/

    @Override
    void doSmooth()
    {
        super.doSmooth();
        improveGeoPoints();
    }

    void improveGeoPoints()
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
            double turn = Track.turn(lastLoc.getBearing(), loc.getBearing());
            double time=(loc.getTime()-lastLoc.getTime())/1000.0;

            if((lastLoc.distanceTo(loc)>maxDistance) || (loc.getTime()-lastLoc.getTime()>2) || (turn>20) || time>maxDeltaT)
            {
                lastLoc=loc;
                result.add(lastLoc);
            }
        }
        result.add(mGeoPoints.get(s-1));

        mGeoPoints = result;

    }
    /*void improveGeoPoints()
    {
        if(mGeoPoints.size()<5) return;
        ArrayList<Location> origin = track.getGeoPoints();
        int s=origin.size();

        ArrayList<Location> result=new ArrayList<Location>(s);

        result.add(mGeoPoints.get(0));
        for(int i=1; i<s; i++)
        {
            double turn = Track.turn(mGeoPoints.get(i-1).getBearing(), mGeoPoints.get(i).getBearing());
            if(turn >30)
            {

                int start = i;
                i++;
                while(i<s &&
                       (turn=Track.turn(mGeoPoints.get(i-1).getBearing(), mGeoPoints.get(i).getBearing()))<25)
                {
                    i++;
                }
                int stop=i;
                double maxTurn=Track.turn(mGeoPoints.get(start-1).getBearing(),mGeoPoints.get(stop-1).getBearing());
                int deltaDistance_;
                if(maxTurn>85) deltaDistance_=deltaDistance/3;
                else deltaDistance_=deltaDistance/2;

                result.addAll(
                        doSmooth(origin, 0, origin.size(), start, stop, deltaT, deltaDistance_));

            }
            else
                result.add(mGeoPoints.get(i));

        }

        mGeoPoints = result;

    }*/


}
