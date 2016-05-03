package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.model.CurrentTrack;
import com.kivsw.forjoggers.model.DataModel;
import com.kivsw.forjoggers.model.TrackSmoother;

/**
 * Created by ivan on 4/27/16.
 */
public abstract class BasePresenter {
    protected Context context;
    BasePresenter(Context context)
    {
        this.context = context.getApplicationContext();
    }

    public boolean isTracking()
    {
        return DataModel.getInstance(context).isTracking();
    }
    public long getTrackingTime()
    {
        return DataModel.getInstance(context).getTrackingTime();
    }
    public boolean hasTrackData()
    {
        return DataModel.getInstance(context).hasTrackData();
    }
    public boolean trackNeedToBeSaved()
    {
        return DataModel.getInstance(context).getCurrentTrack().needToBeSaved();
    }
    public CurrentTrack getCurrentTrack()
    {
        return DataModel.getInstance(context).getCurrentTrack();
    }
    public TrackSmoother getTrackSmoother()
    {
        return DataModel.getInstance(context).getTrackSmoother();
    }

    abstract public void onSettingsChanged();
}
