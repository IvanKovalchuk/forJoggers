package com.kivsw.forjoggers.model.track;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action2;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 *  This class holds the current track data.
 *  This class adds rx-features.
 *  The class may load and save its data asynchronously
 */
public class CurrentTrack extends Track {



    private String fileName;

    public CurrentTrack()
    {
        super();
        fileName="";
    }

    /**
     * gets observable for this class
     * this observable emits an event when the track data has been changed
     */
    private Observable<Track> observable=null;
    public Observable<Track>  getObservable()
    {
        if(observable!=null) return observable;

        PublishSubject res = PublishSubject.<Track>create();

        Observable.create(new Observable.OnSubscribe<Track>() {

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

                });
            }
        }).subscribe(res);


        observable = res;
        return observable;
    }

    /**
     * Observable for fileName
     */
    private PublishSubject<String> fileNameObservable=null;
    public Observable<String> getFileNameObservable()
    {
        if(fileNameObservable==null)
            fileNameObservable=PublishSubject.create();
        return fileNameObservable;
    }

    public String getFileName() {
        return fileName;

    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        if(fileNameObservable!=null)
            fileNameObservable.onNext(fileName);
    }

    @Override
    public void clear()
    {
        setFileName("");
        super.clear();

    }

    //----------------------------------------------------
    /**  save points in a file asynchronously
     * @param aFileName file name
     * @return true if the saving was successful
     */
    public boolean saveGeoPoint(final String aFileName, final boolean cachFile, final Action2<Boolean,String> onCompletedListener)
    {
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
                        if(track.saveGeoPoint(aFileName))
                             return Boolean.TRUE;
                        else return Boolean.FALSE;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        if(onCompletedListener!=null)
                            onCompletedListener.call(Boolean.FALSE, aFileName);
                    }

                    @Override
                    public void onNext(Boolean r) {
                        if(r.booleanValue() && !cachFile) {
                            CurrentTrack.this.setFileName(aFileName);
                        }
                        if(onCompletedListener!=null)
                            onCompletedListener.call(r,aFileName);
                    }
                });

        return true;

    };

    /**
     * load points from file asynchronously
     * @param aFileName file name
     * @return true if the loading was successful
     */
    public boolean loadGeoPoint(final String aFileName, final boolean cachFile,final Action2<Boolean, String> onCompletedListener) {
        boolean r=false;

        super.clear();
        setFileName( "");

        Observable.just(aFileName)
                .observeOn(Schedulers.io())
                .map(new Func1<String, Track>() {
                    @Override
                    public Track call(String fileName) { // load values
                        Track r=new Track();
                        if(r.loadGeoPoint(fileName))
                            return r;
                        else {
                            return null;
                            //throw new RuntimeException(String.format(context.getText(R.string.cannot_load_file).toString(),fileName));
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

                        clear();
                        if(onCompletedListener!=null)
                             onCompletedListener.call(Boolean.FALSE,aFileName);

                    }

                    @Override
                    public void onNext(Track track) {

                        if(track!=null) {
                            if(!cachFile)
                                CurrentTrack.this.setFileName(aFileName);
                            assign(track);

                            if(onCompletedListener!=null)
                                onCompletedListener.call(Boolean.TRUE,aFileName);
                        }
                        else
                        {
                            if(onCompletedListener!=null)
                                onCompletedListener.call(Boolean.FALSE,aFileName);

                        }
                    }
                });


        return true;
    }

    public boolean needToBeSaved()
    {
        return (fileName==null || fileName.isEmpty()) && (mGeoPoints.size()>1);
    }
}
