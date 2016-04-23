package com.kivsw.forjoggers.model;

import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.model.Track;

/**
 * Created by ivan on 03.12.15.
 */
public abstract class TrackSmoother extends Track
        implements Track.IOnChange {
    protected Track track;

    TrackSmoother(Track track)
    {
        super();
        mGeoPoints=null;
        this.track=track;
        track.setOnChange(this);
    };

    public void release()
    {
        track.setOnChange(null);
        this.track=null;
    }

    @Override
    public long getTrackTime(boolean totalTime)
    {
        if(track==null) return 0;
        return track.getTrackTime(totalTime);
    };

    @Override
    public int getActivityType(){
        if(track==null) return SettingsKeeper.JOGGING;
        return track.getActivityType();
    };



    @Override
    abstract public void onAddPoint();

    @Override
    abstract public void onClear();

}
