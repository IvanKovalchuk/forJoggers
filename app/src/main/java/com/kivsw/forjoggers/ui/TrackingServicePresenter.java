package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.model.DataModel;

/**
 * This is the presenter for the service
 */
public class TrackingServicePresenter extends BasePresenter {

    static private TrackingServicePresenter singletone=null;
    static public TrackingServicePresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new TrackingServicePresenter(context.getApplicationContext());
        return singletone;
    };

    TrackingService service=null;

    private TrackingServicePresenter(Context context)
    {
        super(context);

    }

    //-------------------------------------------------
    void setService(TrackingService service) {
        if (service == null)
        {
            DataModel.getInstance(context).getUsingCounter().stopUsingBy(TrackingService.TAG);
        }
        else
        {
            DataModel.getInstance(context).getUsingCounter().startUsingBy(TrackingService.TAG);
        }
        this.service=service;
    }

    //-------------------------------------------------
    @Override
    public void onSettingsChanged() {

    }
    //-------------------------------------------------
    // returns the time (milliseconds) to the next second,
    // the moment to update the notification
    public long leftToNextSecond()
    {
        long workingTime= DataModel.getInstance(context).getTrackingTime();
        long t=1000-workingTime%1000;
        return t;
    }
    //-------------------------------------------------
    public void startTracking()
    {
        TrackingService.start(context, TrackingService.TRACKING);
    }

    public void endTracking()
    {
        TrackingService.stop(context, TrackingService.TRACKING);
    }
    //-------------------------------------------------
    public void startSaving()
    {
        TrackingService.start(context,TrackingService.SAVING);
    }

    public void endSaving()
    {
        TrackingService.stop(context, TrackingService.SAVING);
    }

    //-------------------------------------------------
    public void startBackground()
    {
        TrackingService.start(context,TrackingService.BACKGROUND);
    }

    public void endBackground()
    {
        TrackingService.stop(context, TrackingService.BACKGROUND);
    }
}
