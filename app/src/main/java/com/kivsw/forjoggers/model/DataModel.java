package com.kivsw.forjoggers.model;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import com.kivsw.dialog.MessageDialog;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.TrackingService;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.rx.RxGps;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ivan on 4/22/16.
 */
public class DataModel {

    static DataModel dataModel=null;
    static public  DataModel getInstance(Context context)
    {
        if(dataModel==null)
            dataModel = new DataModel(context.getApplicationContext());
        return dataModel;
    }


    //-------------------------------------------------------------------
    Context context;
    SettingsKeeper settings;

    boolean isTracking=false;

    private CurrentTrack currentTrack=null;
    private TrackSmoother trackSmoother=null;
    Subscriber trackingSubscriber=null;


    private DataModel(Context context)
    {
        this.context = context;
        settings = SettingsKeeper.getInstance(context);
        currentTrack = CurrentTrack.getInstance(context);
        trackSmoother = //new TrackSmootherByLine(currentTrack);
                new TrackSmootherByPolynom(currentTrack);
    };
    public void release()
    {
        RxGps.release();
        trackSmoother.release();
        context = null;

    }
    //------------------------------------------------------------------
    public TrackSmoother getTrackSmoother() {
        return trackSmoother;
    };
    public CurrentTrack getCurrentTrack() {
        return currentTrack;
    };
    public long getTrackingTime()
    {
        if(!isTracking) return 0;

        return  SystemClock.elapsedRealtime() - currentTrack.timeStart;
    }
    //------------------------------------------------------------------
    /**
     * starts recording a new track
     */
    public void startTracking()
    {
        TrackingService.start(context);

        currentTrack.clear();
        currentTrack.setActivityType(settings.getActivityType());
        currentTrack.timeStart= SystemClock.elapsedRealtime();
        isTracking=true;


        trackingSubscriber = new Subscriber<Location>() {
            @Override
            public void onCompleted() {  }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(Location location) {

            }
        };
        RxGps.getGprsObservable(context)
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        if(location==null) return false;
                        currentTrack.addPoint(location);
                        return true;
                    }
                })
                .observeOn(Schedulers.computation())
                .subscribe(trackingSubscriber);



    }

    /**
     * Stops recording the current track
     */
    public void stopTracking()
    {
        TrackingService.stop(context);
        trackingSubscriber.unsubscribe();
        trackingSubscriber=null;
        currentTrack.timeStop= SystemClock.elapsedRealtime();
        currentTrack.saveTrack();
        isTracking = false;
    };
    //--------------------------------------------------------------------

}
