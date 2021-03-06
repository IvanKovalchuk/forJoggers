package com.kivsw.forjoggers.ui.service;

import android.content.Context;

import com.kivsw.forjoggers.ui.BasePresenter;
import com.kivsw.forjoggers.ui.MainActivityIPresenter;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * This is the presenter for the service's UI
 */
public class TrackingServicePresenter
        extends BasePresenter
implements TrackingServiceContract.IPresenter
{

    static private TrackingServicePresenter singletone=null;
    static public TrackingServicePresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new TrackingServicePresenter(context.getApplicationContext());
        return singletone;
    };

    TrackingServiceContract.IView service=null;

    private TrackingServicePresenter(Context context)
    {
        super(context);

    }

    //-------------------------------------------------
    @Override
    public void setService(TrackingServiceContract.IView service) {
        if (service == null)
        {
            getDataModel().getUsingCounter().stopUsingBy(TrackingService.TAG);
        }
        else
        {
            getDataModel().getUsingCounter().startUsingBy(TrackingService.TAG);
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
    @Override
    public long leftToNextSecond()
    {
        long workingTime= getDataModel().getTrackingTime();
        long t=1000-workingTime%1000;
        return t;
    }
    //-------------------------------------------------

    /**
     * accomplishes the notification's action "exit"
     */
    @Override
    public void action_exit()
    {
        MainActivityIPresenter.getInstance(context).actionExit();
    };

    /**
     *  accomplishes the notification's action "stop tracking"
     */
    @Override
    public void action_stopTracking()
    {
        getDataModel().stopTracking();
    };
    //-------------------------------------------------
    /**
     * inform service that tracking has been started
     */
    @Override
    public void startTracking()
    {
        TrackingService.start(context, TrackingService.TRACKING);
    }

    /**
     * inform service that tracking has been stopped
     */
    @Override
    public void endTracking()
    {
        TrackingService.stop(context, TrackingService.TRACKING);
    }
    //-------------------------------------------------

    /**
     * inform service that the file saving process  has been started
     */
    @Override
    public void startSaving()
    {
        TrackingService.start(context,TrackingService.SAVING);
    }
    /**
     * inform service that the file saving process has been stopped
     */
    @Override
    public void endSaving()
    {
        TrackingService.stop(context, TrackingService.SAVING);
    }

    //-------------------------------------------------

    /**
     * inform service that the background working has been started
     */
    @Override
    public void startBackground()
    {
        TrackingService.start(context,TrackingService.BACKGROUND);
    }
    /**
     * inform service that the background working  has been stopped
     */
    @Override
    public void endBackground()
    {
        TrackingService.stop(context, TrackingService.BACKGROUND);
    }
    //-------------------------------------------------

    /**
     * inform service that the background working has been started
     */
    Subscription speakingWachdogTimer=null;
    @Override
    public void startTTSspeaking()
    {
        if(speakingWachdogTimer!=null)
            speakingWachdogTimer.unsubscribe();

        TrackingService.start(context,TrackingService.TTS_SPEAKS);

        speakingWachdogTimer = rx.Observable.timer(30, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        endTTSspeaking();
                    }
                });
    }
    /**
     * inform service that the background working  has been stopped
     */
    @Override
    public void endTTSspeaking()
    {
        if(speakingWachdogTimer!=null)
           speakingWachdogTimer.unsubscribe();
        speakingWachdogTimer=null;

        TrackingService.stop(context, TrackingService.TTS_SPEAKS);
    }

}
