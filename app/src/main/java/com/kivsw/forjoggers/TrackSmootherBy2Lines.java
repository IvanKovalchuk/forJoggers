package com.kivsw.forjoggers;

import android.location.Location;

/**
 * Created by ivan on 04.12.15.
 */
public class TrackSmootherBy2Lines extends TrackSmootherByLine{

    TrackSmootherBy2Lines(Track track)
    {
        super(track);
        deltaT=120000;
        deltaDistance=60;
    }

    @Override
    protected Location getAvarageFor(int ind, int b, int e)
    {
        int leftSize=ind-b, rightSize=e-ind;

        if((leftSize<3 ) || (rightSize<3))
            return super.getAvarageFor(ind, b, e);

        float ratio=(leftSize<rightSize)? (float)rightSize/leftSize : (float)leftSize/rightSize;
        if(ratio>=2)
            return super.getAvarageFor(ind, b, e);

        Location loc1=super.getAvarageFor(ind, b, ind+1),
                 loc2=super.getAvarageFor(ind, ind, e);

        loc1.setLongitude((loc1.getLongitude() + loc2.getLongitude()) / 2);
        loc1.setLatitude((loc1.getLatitude()+loc2.getLatitude())/2);
        //return super.getAvarageFor(ind, b, e);
        return loc1;
    }
}
