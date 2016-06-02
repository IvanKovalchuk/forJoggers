package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.model.IDataModel;
import com.kivsw.forjoggers.model.track.CurrentTrack;
import com.kivsw.forjoggers.model.DataModel;
import com.kivsw.forjoggers.model.track.TrackSmoother;

/**
 * Created by ivan on 4/27/16.
 */
public abstract class BasePresenter
implements IBasePresenter {
    protected Context context;
    public BasePresenter(Context context)
    {
        this.context = context.getApplicationContext();
    }

    @Override
    public boolean isTracking()
    {
        return getDataModel().isTracking();
    }
    @Override
    public long getTrackingTime()
    {
        return getDataModel().getTrackingTime();
    }
    @Override
    public boolean hasTrackData()
    {
        return getDataModel().hasTrackData();
    }
    @Override
    public boolean trackNeedToBeSaved()
    {
        return getDataModel().getCurrentTrack().needToBeSaved();
    }
    @Override
    public CurrentTrack getCurrentTrack()
    {
        return getDataModel().getCurrentTrack();
    }
    @Override
    public TrackSmoother getTrackSmoother()
    {
        return getDataModel().getTrackSmoother();
    }

    protected IDataModel getDataModel()
    {
        return DataModel.getInstance(context);
    };

    abstract public void onSettingsChanged();
}
