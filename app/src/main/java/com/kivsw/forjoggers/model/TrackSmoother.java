package com.kivsw.forjoggers.model;

import com.kivsw.forjoggers.helper.SettingsKeeper;

/**
 * Created by ivan on 03.12.15.
 */
public abstract class TrackSmoother extends Track
{
    protected Track track;

    TrackSmoother(Track track)
    {
        super();
        mGeoPoints=null;
        this.track=track;
        //track.setOnChange(this);
    };

    public void release()
    {
        track.setOnChange(null);
        this.track=null;
    }

    /**
     * checks if newTrack has new data compared with track
     * @param newTrack
     * @return
     */
    boolean needRecalculate(Track newTrack)
    {
        return newTrack.getGeoPoints().size()!=track.getGeoPoints().size();
    }
    @Override
    public long getTrackPointsTime()
    {
        if(track==null) return 0;
        return track.getTrackPointsTime();
    };
    @Override
    public long getTrackTime()
    {
        if(track==null) return 0;
        return track.getTrackTime();
    };

    @Override
    public int getActivityType(){
        if(track==null) return SettingsKeeper.JOGGING;
        return track.getActivityType();
    };

    abstract void doSmooth();
  /*  @Override
    abstract public void onAddPoint();

    @Override
    abstract public void onClear();*/

}
