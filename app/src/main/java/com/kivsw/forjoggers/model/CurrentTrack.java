package com.kivsw.forjoggers.model;

import android.content.Context;


import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by ivan on 20.11.15.
 */
public class CurrentTrack extends Track {
    static private CurrentTrack track=null;
    SettingsKeeper settings=null;
    Context context;
    String fileName;

    static synchronized  CurrentTrack getInstance(Context context, Observer observer)
    {
        if(track==null)
        {
            track = new CurrentTrack(context.getApplicationContext());
            //track.settings = SettingsKeeper.getInstance(context);
            if(observer!=null)
                track.getObservable().subscribe(observer);

            String fn=track.settings.getCurrentFileName();
            if(fn!=null && !fn.isEmpty())
               track.loadGeoPoint(fn);
            else
                track.fromGPX(track.settings.getCurrentTrack());
        }
        return track;
    }

    private CurrentTrack(Context context)
    {
        super();
        this.context = context;
        fileName="";
        settings = SettingsKeeper.getInstance(context);
    }

    /**
     * gets observable for this class
     */
    private Observable<Track> observable=null;
    public Observable<Track>  getObservable()
    {
        if(observable!=null) return observable;

        PublishSubject res = PublishSubject.<Track>create();

        observable.create(new Observable.OnSubscribe<Track>() {

            @Override
            public void call(final Subscriber<? super Track> subscriber) {

                setOnChange(new IOnChange(){
                    @Override
                    public void onAddPoint() {
                        subscriber.onNext(CurrentTrack.this);
                    }

                    @Override
                    public void onClear() {
                        subscriber.onNext(CurrentTrack.this);
                    }

                    @Override public void onError(Throwable e)
                    {
                        subscriber.onError(e);
                    }
                });
            }
        }).subscribe(res);


        observable = res;
        return observable;
    }

    /**
     * Save the track into sharedPreferences
     */
    static synchronized public void saveTrack()
    {
        if(track==null) return;
        track.settings.setCurrentTrack( track.toGPX());
    }


    public String getFileName() {
        return fileName;
    }

    @Override
    public void clear()
    {
        fileName="";
        super.clear();

        settings.setCurrentFileName("");
    }

    //----------------------------------------------------
    /**  save points in a file asynchronously
     * @param fileName file name
     * @return true if the saving was successful
     */
    public boolean saveGeoPoint(String fileName)
    {
      /*  if(!fileName.matches(".*\\.gpx$"))
            fileName = fileName+".gpx";*/

            /*if(super.saveGeoPoint(fileName)) {
            this.fileName = fileName;
            settings.setCurrentFileName(fileName);

            return true;
        }
        return false;*/
        this.fileName = fileName;
        final String fn=fileName;
        Observable.just(this)
                .map(new Func1 < CurrentTrack, Track > () {
                    @Override
                    public Track call(CurrentTrack currentTrack) {
                        return currentTrack.clone();
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Func1 < Track, Boolean > (){
                    @Override
                    public Boolean call(Track track) {
                        if(!track.saveGeoPoint(fn))
                            throw new RuntimeException(String.format(context.getText(R.string.cannot_save_file).toString(),fn));
                        return Boolean.TRUE;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        onChange.onError(e);
                    }

                    @Override
                    public void onNext(Boolean r) {
                        CurrentTrack.this.fileName = fn;
                        settings.setCurrentFileName(CurrentTrack.this.fileName);
                        onChange.onAddPoint();
                    }
                });

        return true;

    };

    /**
     * load points from file asynchronously
     * @param fileName file name
     * @return true if the loading was successful
     */
    public boolean loadGeoPoint(String fileName) {
        boolean r=false;

        super.clear();
        this.fileName = fileName;

        Observable.just(fileName)
                .observeOn(Schedulers.io())
                .map(new Func1<String, Track>() {
                    @Override
                    public Track call(String fileName) { // load values
                        Track r=new Track();
                        if(r.loadGeoPoint(fileName))
                            return r;
                        else {
                            throw new RuntimeException(String.format(context.getText(R.string.cannot_load_file).toString(),fileName));
                        }
                    };
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Track>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        CurrentTrack.this.fileName = "";
                        clear();
                        onChange.onError(e);
                    }

                    @Override
                    public void onNext(Track track) {

                        assign(track);
                        settings.setCurrentFileName(CurrentTrack.this.fileName);
                        onChange.onAddPoint();
                    }
                });


        return true;
    }

    public boolean needToBeSaved()
    {
        return (fileName==null || fileName.isEmpty()) && (mGeoPoints.size()>1);
    }
}
