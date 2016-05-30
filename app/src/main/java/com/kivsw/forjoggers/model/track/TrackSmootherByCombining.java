package com.kivsw.forjoggers.model.track;

import android.location.Location;

import com.kivsw.forjoggers.model.math.LineApproximator;
import com.kivsw.forjoggers.model.math.PolinomApproximator;
import com.kivsw.forjoggers.model.math.iApproximator;

import java.util.ArrayList;

/**
 * This smoother use linear interpolation where is no turn and square interpolation where is a turn
 */
public class TrackSmootherByCombining extends TrackSmootherByLine {

    iApproximator latApproximator2=null, lngApproximator2=null;
    iApproximator latApproximator1=null, lngApproximator1=null;


    public TrackSmootherByCombining(Track track)
    {
        super(track);
       // deltaT=15000; deltaDistance=40;//600;

        latApproximator2 = new PolinomApproximator(2);
        lngApproximator2 = new PolinomApproximator(2);

        latApproximator1=new LineApproximator();
        lngApproximator1=new LineApproximator();
    }

    @Override
    protected void createApproximators()
    {

    };

    //-----------------------------------------------------
    @Override
    public void doSmooth()
    {
        if(track==null) return;
        int s=track.getGeoPoints().size();

        latApproximator=latApproximator1;
        lngApproximator=lngApproximator1;
        ArrayList<Location> linear = doSmooth(track.getGeoPoints(), 0,s,  0,s);

        latApproximator=latApproximator2;
        lngApproximator=lngApproximator2;
        ArrayList<Location> square = doSmooth(track.getGeoPoints(), 0,s,  0,s);

        mGeoPoints = combine(linear, square);

        thinOutGeoPoints();

    }

    /**
     *  combine linear and square interpolations
     * @param linear
     * @param square
     * @return
     */
    private ArrayList<Location> combine(ArrayList<Location>linear,ArrayList<Location>square)
    {
        double coefficient[]= calcShare(linear); // calculate factors for the square approximation

        for(int i=0, s=linear.size(); i<s; i++)
        {
            if(coefficient[i]<0.01) continue;

            double kln=1-coefficient[i], ksq=coefficient[i];

            Location ln=linear.get(i);
            Location sq=square.get(i);

            Location loc=new Location(ln);
            loc.setLatitude(ln.getLatitude()*kln + sq.getLatitude()*ksq);
            loc.setLongitude(ln.getLongitude()*kln + sq.getLongitude()*ksq);
            loc.setBearing((float)(ln.getBearing()*kln + sq.getBearing()*ksq));
            loc.setSpeed((float)(ln.getSpeed()*kln + sq.getSpeed()*ksq));

            /*if(i>0) {
                float distanceAndBearing[] = new float[3];
                Location prev=linear.get(i-1);
                Location.distanceBetween(prev.getLatitude(), prev.getLongitude(),
                                         loc.getLatitude(), loc.getLongitude(), distanceAndBearing);

                loc.setBearing(distanceAndBearing[1]);
                //loc.setSpeed((float) distanceAndBearing[0] / deltaT);
            }*/

            linear.set(i,loc);

        }

        return linear;

    }

    private double[] calcShare(ArrayList<Location> geoPoints)
    {
        double[] res = new double[geoPoints.size()];

        for(int i=1; i<res.length;i++) {
            double a=Track.turn(geoPoints.get(i-1).getBearing(), geoPoints.get(i).getBearing());

            if(a>12) // if we have a turn
            {
                setCoefficient(res, geoPoints,i);
            }
        }

        return res;
    }

    private void setCoefficient(double[] coef,ArrayList<Location> geoPoints, int ind)
    {
        long t0=geoPoints.get(ind).getTime();
        long minT=t0-deltaT,
             maxT=t0+deltaT;
        int i;
        long t;
        double c;


        t=t0; i=ind;
        while(t>minT && i>=0)
        {
            t=geoPoints.get(i).getTime();
            long dt= Math.abs(t-t0);
            c = ((double)deltaT-dt)/deltaT;
            if(coef[i]<c) coef[i]=c;
            i--;
        }

        t=t0; i=ind;
        while(t<maxT && i<coef.length)
        {
            t=geoPoints.get(i).getTime();
            long dt= Math.abs(t-t0);
            c = ((double)deltaT-dt)/deltaT;;
            if(coef[i]<c) coef[i]=c;
            i++;
        }
    }
}
