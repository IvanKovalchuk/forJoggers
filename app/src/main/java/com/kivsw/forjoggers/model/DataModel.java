package com.kivsw.forjoggers.model;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import com.kivsw.forjoggers.TrackingService;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.rx.RxGps;
import com.kivsw.forjoggers.ui.MapFragmentPresenter;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

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
    private volatile boolean isTrackSmoothing=false;
    private TrackSmoother trackSmoother=null;
    Subscriber trackingSubscriber=null;


    private DataModel(Context context)
    {
        this.context = context;
        settings = SettingsKeeper.getInstance(context);
        currentTrack = CurrentTrack.getInstance(context);
        /*trackSmoother = //new TrackSmootherByLine(currentTrack);
                new TrackSmootherByPolynom(currentTrack);*/

        initSmoothCalculation();


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
    public boolean isTracking()
    {
        return isTracking;
    }
    public boolean hasTrackData()
    {
        return getCurrentTrack().getGeoPoints().size()>1;
    }

    //------------------------------------------------------------------
    // initializes smoothen calculation
    protected void initSmoothCalculation()
    {
        Subject<Track,Track> subject=PublishSubject.create();
        currentTrack.getObservable()
        .mergeWith(subject) // creates a loop
        .observeOn(AndroidSchedulers.mainThread())
        .filter(new Func1<Track, Boolean>() { //
            @Override
            public Boolean call(Track track) {

                if(isTrackSmoothing) // if the calculation is doing
                    return Boolean.FALSE;

                if(trackSmoother==null || trackSmoother.needRecalculate(currentTrack))
                   return Boolean.TRUE;

                return Boolean.FALSE;
            }
        })
        .map(new Func1<Track, TrackSmoother>(){ // creates a smoother
            @Override
            public TrackSmoother call(Track track) {
                TrackSmoother r = new TrackSmootherByPolynom(currentTrack.clone());//new TrackSmootherByLine(currentTrack);
                isTrackSmoothing=true;
                return r;
            }
        })
        .observeOn(Schedulers.computation()) // change to another thread
        .map(new Func1<TrackSmoother, TrackSmoother>(){ // calculates a smooth track
            @Override
            public TrackSmoother call(TrackSmoother track) {
                track.doSmooth();
                return track;
            }
        })
        .observeOn(AndroidSchedulers.mainThread())// change to the main thread
        .map(new Func1<TrackSmoother, TrackSmoother>(){ // calculates a smooth track
            @Override
            public TrackSmoother call(TrackSmoother track) {
                trackSmoother = track;
                isTrackSmoothing=false;
                doUpdateCurrentSmoothTrackView();
                return null;
            }
        })
        .subscribe(subject);

    };
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
                currentTrack.addPoint(location);
                doUpdateCurrentTrackView();

            }
        };
        RxGps.getGprsObservable(context)
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        if(location==null) return false;
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
    protected void doUpdateCurrentTrackView()
    {
        MapFragmentPresenter.getInstance(context).onCurrentTrackUpdate(currentTrack);
    }
    protected void doUpdateCurrentSmoothTrackView()
    {
         MapFragmentPresenter.getInstance(context).onSmoothTrackUpdate(trackSmoother);
    }

}
