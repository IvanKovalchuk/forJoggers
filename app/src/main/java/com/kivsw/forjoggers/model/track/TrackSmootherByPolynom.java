package com.kivsw.forjoggers.model.track;

import android.location.Location;

import com.kivsw.forjoggers.model.math.PolinomApproximator;

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


    };//*/

    @Override
    public void doSmooth()
    {
        super.doSmooth();
        thinOutGeoPoints();
    }




}
