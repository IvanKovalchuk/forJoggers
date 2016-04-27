package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.model.DataModel;

/**
 * Created by ivan on 4/27/16.
 */
public class BasePresenter {
    protected Context context;
    BasePresenter(Context context)
    {
        this.context = context.getApplicationContext();
    }

    boolean isTracking()
    {
        return DataModel.getInstance(context).isTracking();
    }
    boolean hasTrackData()
    {
        return DataModel.getInstance(context).hasTrackData();
    }
    boolean trackNeedToBeSaved()
    {
        return DataModel.getInstance(context).getCurrentTrack().needToBeSaved();
    }
}
