package com.kivsw.forjoggers.model.track;

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
        this.track=track.clone();
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
    public boolean needRecalculate(Track newTrack)
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
        throw  new RuntimeException("TrackSmoother cannot return correct value of getTrackTime()");
    };

    @Override
    public int getActivityType(){
        if(track==null) return SettingsKeeper.JOGGING;
        return track.getActivityType();
    };

    public abstract void doSmooth();
  /*  @Override
    abstract public void onAddPoint();

    @Override
    abstract public void onClear();*/

}
