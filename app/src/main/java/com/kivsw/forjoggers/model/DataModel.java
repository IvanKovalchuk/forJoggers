package com.kivsw.forjoggers.model;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.RxGpsLocation;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.helper.UsingCounter;
import com.kivsw.forjoggers.model.track.CurrentTrack;
import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.model.track.TrackSmoother;
import com.kivsw.forjoggers.model.track.TrackSmootherFactory;
import com.kivsw.forjoggers.ui.map.MapFragmentPresenter;
import com.kivsw.forjoggers.ui.service.TrackingServicePresenter;

import java.io.File;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

/**
 * This is the main module of the data model.
 * it controls all the data
 */
public class DataModel
        implements IDataModel, UsingCounter.IUsingChanged
{

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
    UsingCounter<String> usingCounter;
    Speaker speaker=null;

    boolean isTracking=false;

    private CurrentTrack currentTrack=null;
    private volatile boolean isTrackSmoothing=false;
    private TrackSmoother trackSmoother=null;
    Subscriber trackingSubscriber=null;
    Subscription autostopSubscription=null;


    private DataModel(Context context)
    {
        this.context = context;
        usingCounter = new UsingCounter(this);

        settings = SettingsKeeper.getInstance(context);
        currentTrack = new CurrentTrack();
        currentTrack.setActivityType(settings.getActivityType());

        initSmoothCalculation();
      //  initCurrentTrackUpdating();

        // load the last track
        loadLastData();

    };
    private void release()
    {
        RxGpsLocation.release();
        if(speaker!=null)
            speaker.release();
        speaker=null;
    }
    //----------------------------------------------------------

    /**
     * Watches whether other entities use this class.
     * Release all the resources if no one uses this class.
     * @param usingCount
     */
    @Override
    public void onUsingCountChanged(int usingCount) {
          if(usingCount==0)
              release();
    }
    //------------------------------------------------------------------
    @Override
    public UsingCounter getUsingCounter()
    {
        return usingCounter;
    }
    @Override
    public TrackSmoother getTrackSmoother() {
        return trackSmoother;
    };
    @Override
    public CurrentTrack getCurrentTrack() {
        return currentTrack;
    };

    /**
     * loads the last track
     */
    private void loadLastData()
    {
        String fn=settings.getCurrentFileName();
        if(fn==null || fn.isEmpty())
            fn = getTempFileName();
        File file = new File(fn);
        if(file.exists())
             loadTrack(fn);

    }
    @Override
    public long getTrackingTime()
    {
        if(!isTracking) return 0;

        return  SystemClock.elapsedRealtime() - currentTrack.timeStart;
    }
    public boolean isTracking()
    {
        return isTracking;
    }
    private void setTracking(boolean v)
    {
        isTracking=v;
        if(startStopObservable!=null)
            startStopObservable.onNext(isTracking);
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
                TrackSmoother r =TrackSmootherFactory
                        .getSmoother(TrackSmootherFactory.SQUARE_TURNS, currentTrack);
                //.getSmoother(TrackSmootherFactory.SQUARE, currentTrack);
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
                if(trackSmootherObservable!=null)
                    trackSmootherObservable.onNext(trackSmoother);
                //doUpdateCurrentSmoothTrackView();
                return null;
            }
        })
        .subscribe(subject);

    };

    //------------------------------------------------------------------
    private String getTempFileName()
    {
        String s=context.getCacheDir().getAbsolutePath() + "/lasttrack.gpx";
        return s;
    }
    /**
     * starts recording a new track
     */
    public void startTracking()
    {
        if(isTracking) return;

        TrackingServicePresenter.getInstance(context).startTracking();

        trackSmoother=null;
        currentTrack.clear();
        currentTrack.setActivityType(settings.getActivityType());
        currentTrack.timeStart= SystemClock.elapsedRealtime();

        setTracking(true);

        nextTimeTospeak=settings.getTimeSpeaking().getTimeSeconds();
        nextDistanseToSpeak=(long)settings.getDistanceSpeaking().getDistanceMeters();

        trackingSubscriber = new Subscriber<Location>() { // this entity adds new data from GPS to the track
            @Override
            public void onCompleted() {  }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(Location location) {

                currentTrack.addPoint(location);

            }
        };
        RxGpsLocation.getGprsObservable(context) // starts forming a new current track
                .filter(new Func1<Location, Boolean>() {
                    @Override
                    public Boolean call(Location location) {
                        if(location==null) return false;
                        return true;
                    }
                })
                //.observeOn(Schedulers.computation())
                .subscribe(trackingSubscriber);


        // for autostop option
        autostopSubscription= Observable.interval(1000, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        checkAutoStop();
                        checkSpeak();
                    }
                });


        // updates all the views
        //if(fileNameObservable!=null) fileNameObservable.onNext(currentTrack.getFileName());
        if(trackSmootherObservable !=null) trackSmootherObservable.onNext(trackSmoother);

       // doUpdateCurrentTrackView();
        //doUpdateCurrentSmoothTrackView();
        //doUpdateFileNameView();

        speaker=new Speaker(context, new Action1<String>() {
                    @Override
                    public void call(String s) {
                        errorMessageObservable.onNext(s);
                    }
                });
        if(settings.getIsStartStopSpeaking())
            speaker.speakStart();
    }

    /**
     * Stops recording the current track
     */
    public void stopTracking()
    {
        if(!isTracking) return;

        trackingSubscriber.unsubscribe();
        trackingSubscriber=null;

        autostopSubscription.unsubscribe();
        autostopSubscription=null;

        currentTrack.timeStop= SystemClock.elapsedRealtime();

        saveTrack(getTempFileName());

        setTracking(false);

        //MapFragmentPresenter.getInstance(context).onAfterStopTracking();

        if(settings.getIsStartStopSpeaking()) {
            speaker.speakStop();
            //speaker.speakTrack();
        }
        TrackingServicePresenter.getInstance(context).endTracking();
        speaker.release();
        speaker=null;

    };

    /**
     * Save track to a file
     */
    public boolean saveTrack(String fileName)
    {
        TrackingServicePresenter.getInstance(context).startSaving(); // starts service

        boolean r=getCurrentTrack().saveGeoPoint(fileName,getTempFileName().equals(fileName),
                new Action2<Boolean,String>() {
            @Override
            public void call(Boolean aBoolean, String aFileName) {
                TrackingServicePresenter.getInstance(context).endSaving(); // stops service
                if(aBoolean.booleanValue())
                {
                    if(currentTrack.getFileName().equals(getTempFileName())) // don't keep the temporary file's name
                        currentTrack.setFileName("");

                    //doUpdateFileNameView();
                    settings.setCurrentFileName(currentTrack.getFileName());
                }
                else
                {
                    /*MainActivityPresenter.getInstance(context)
                            .showError(String.format(context.getText(R.string.cannot_save_file).toString(),aFileName));*/
                    if(errorMessageObservable!=null)
                        errorMessageObservable.onNext(String.format(context.getText(R.string.cannot_save_file).toString(),aFileName));
                }

            }
        });

        return r;
    }

    /**
     * Save track to file
     */
    public boolean loadTrack(String fileName)
    {
        trackSmoother=null;
        if(trackSmootherObservable!=null) trackSmootherObservable.onNext(trackSmoother);

        boolean r= getCurrentTrack().loadGeoPoint(fileName, getTempFileName().equals(fileName),
                new Action2<Boolean,String>() {
            @Override
            public void call(Boolean aBoolean, String aFileName) {
                if(aBoolean.booleanValue())
                {
                    if(aFileName.equals(getTempFileName()))
                        currentTrack.setFileName("");

                    MapFragmentPresenter.getInstance(context).actionShowCurrentTrack();
                    settings.setCurrentFileName(currentTrack.getFileName());
                }
                else
                {
                    if(errorMessageObservable!=null)
                       errorMessageObservable.onNext(String.format(context.getText(R.string.cannot_load_file).toString(),aFileName));
                   /* MapFragmentPresenter.getInstance(context).doUpdateFileName();
                    MainActivityPresenter.getInstance(context)
                            .showError(String.format(context.getText(R.string.cannot_load_file).toString(),aFileName));
                    doUpdateFileNameView();*/
                }
            }
        });


        return r;
    }

    /**
     * check whether we should stop tracking right now
     *  and stop it if it's necessary
     */
    private void checkAutoStop()
    {
        Track track=trackSmoother;

        if(track!=null && settings.getIsDistanceAutoStop())
        {
            if(track.getTrackDistance() > settings.getAutoStopDistance().getDistanceMeters())
                stopTracking();
        }

        if(settings.getIsAutoStopTime())
        {
            if(currentTrack.getTrackTime()>(settings.getAutoStopTime().getTimeSeconds()*1000))
                stopTracking();
        }
    }

    /**
     * check if it should prounce track information
     */
    private long nextTimeTospeak, nextDistanseToSpeak;
    private void checkSpeak()
    {
        if(trackSmoother==null || speaker==null) return;

        if(settings.getIsDistanceSpeaking() && (nextDistanseToSpeak<=trackSmoother.getTrackDistance()))
        {
            speaker.speakTrack();
            nextDistanseToSpeak+=settings.getDistanceSpeaking().getDistanceMeters();
        };

        if(settings.getIsTimeSpeaking() && (nextTimeTospeak<=(getTrackingTime()/1000) ))
        {
            speaker.speakTrack();
            nextTimeTospeak += settings.getTimeSpeaking().getTimeSeconds();
        };
    };

    ///---------------------------------------------------------------------------
    ///--------------------------------------------------------------------------
    // MODEL'S OBSERVABLES
    /**
     *  return observable for the current track
     */
    public Observable<Track> getCurrentTrackObservable()
    {
        return currentTrack.getObservable();
    }
    /**
     *  return observable for the smooth track
     */
    PublishSubject<Track> trackSmootherObservable=null;
    public Observable<Track> getTrackSmootherObservable()
    {
        if(trackSmootherObservable==null)
           trackSmootherObservable = PublishSubject.create();
        return trackSmootherObservable;
    }

    /**
     * observable for an error message
     */
    PublishSubject<String> errorMessageObservable=null;
    public Observable<String> getErrorMessageObservable()
    {
        if(errorMessageObservable==null)
            errorMessageObservable = PublishSubject.create();
        return errorMessageObservable;
    }
    /**
     * observable for start/stop events
     */
    PublishSubject<Boolean> startStopObservable=null;
    public Observable<Boolean> getStartStopObservable()
    {
        if(startStopObservable==null)
            startStopObservable = PublishSubject.create();
        return startStopObservable;
    }
    /**
     * this observable emits an event when the fileName has been changed
     */
    public Observable<String> getFileNameObservable()
    {
        return currentTrack.getFileNameObservable();
    }

    //---------------------------------------------------------------------
    //---------------------------------------------------------------------
    // MODEL OUT INTERFACE
    /**
     * is invoked by a presenter when the user has changes the settings
     */
    public void onSettingsChanged() {
        if(trackSmoother!=null)
            trackSmoother.setActivityType(currentTrack.getActivityType());

        // starts or stop background working
        if(settings.getKeepBackGround())
            TrackingServicePresenter.getInstance(context).startBackground();
        else
            TrackingServicePresenter.getInstance(context).endBackground();

        if(speaker!=null) {
            if(!settings.getTTS_engine().equals(speaker.currentEngine)) // if the user has changed TTS-engine
                speaker.release();
        }


    }
    /**
     * is invoked by a presenter when the activity has been started
     */
    public void onActivityStarted()
    {
        // starts background working
        if(settings.getKeepBackGround())
            TrackingServicePresenter.getInstance(context).startBackground();
    };

}
