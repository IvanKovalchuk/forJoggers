package com.kivsw.forjoggers.helper;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;


/**
 * create an observable for GPS
 *
 */
public class RxGpsLocation {

    static Observable<Location> coreGpsObservable =null;
    static RxGPSLocationListener gpsListener=null;
    static BehaviorSubject gpsUiObservable=null;
    static PublishSubject gpsObservable=null;
    static Subscription gpsObservableSubscription, gpsUiObservableSubscription;

    static long lastLocationTime=0; // when the last location was received

    private static Observable<Location> getCoreObservable(Context aContext)
    {
        if(coreGpsObservable ==null)
        {
            final Context context = aContext.getApplicationContext();

            if(gpsListener==null)
                gpsListener = new RxGPSLocationListener(context);

            coreGpsObservable = Observable.create(new Observable.OnSubscribe<Location>() {
                @Override
                public void call(Subscriber<? super Location> subscriber) {
                    gpsListener.setSubscriber(subscriber);
                }
            })
            .filter(new Func1<Location, Boolean>() {  // remembers the time when the last location was received
                @Override
                public Boolean call(Location location) {
                    lastLocationTime = SystemClock.elapsedRealtime();
                    return true;// really I don't filter
                }
            });
        }
        return coreGpsObservable;

    }
    /**
     * return the observable that emits the current location
     * @param aContext
     * @return
     */

    public static Observable<Location> getGprsObservable(Context aContext)
    {
        if(gpsObservable!=null) return gpsObservable;

        gpsObservable= PublishSubject.create();

        gpsObservableSubscription=  getCoreObservable(aContext).subscribe(gpsObservable);

        return gpsObservable;
    }

    /**
     * returns observable (BehaviorSubject) for UI. At first It emits last known location
     * @param aContext
     * @return
     */
    public static Observable<Location> getGprsUiObservable(Context aContext)
    {
        if(gpsUiObservable!=null) return gpsUiObservable;

        Observable<Location> o=getGprsObservable(aContext);
        gpsUiObservable = BehaviorSubject.create(gpsListener.getLastknownLocation());
        gpsUiObservableSubscription= o.subscribe(gpsUiObservable);

        return gpsUiObservable;
    }

    /**
     * Substitute coreGpsObservable with a new one.
     * This new observable sends data from 'list'.
     * then it puts coreGpsObservable on its place
     * @param list
     */
    private static Subscription emuIntervalSubscription=null;
    public static void setEmulationData(final List<Location> list)
    {

        if(gpsObservableSubscription!=null) gpsObservableSubscription.unsubscribe();

        final PublishSubject<Long> ps= PublishSubject.create();
        emuIntervalSubscription=Observable.interval(250, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()).subscribe(ps);

        Observable o= ps.map(new Func1<Long,Location>() {
            @Override
            public Location call(Long i) {
                if(list.size()>i)
                    return list.get(i.intValue());

                gpsObservableSubscription.unsubscribe();
                emuIntervalSubscription.unsubscribe();
                gpsObservableSubscription=  getCoreObservable(null).subscribe(gpsObservable);// subscribe normal GPS again

                return null;
            }
        })
                .filter(new Func1<Location, Boolean>() {  // remembers the time when the last location was received
                    @Override
                    public Boolean call(Location location) {
                        if(location==null)
                            return false;
                        lastLocationTime = SystemClock.elapsedRealtime();
                        return true;// really I don't filter
                    }
                });
        gpsObservableSubscription = o.subscribe(gpsObservable);



/*        if(gpsObservableSubscription!=null) gpsObservableSubscription.unsubscribe();

        Observable o=Observable.interval(250, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .map(new Func1<Long,Location>() {
                    @Override
                    public Location call(Long i) {
                        if(list.size()>i)
                             return list.get(i.intValue());

                        gpsObservableSubscription.unsubscribe(); // subscribe normal GPS again
                        gpsObservableSubscription=  getCoreObservable(null).subscribe(gpsObservable);

                        return null;
                    }
                })
                .filter(new Func1<Location, Boolean>() {  // remembers the time when the last location was received
                    @Override
                    public Boolean call(Location location) {
                        if(location==null)
                               return false;
                        lastLocationTime = SystemClock.elapsedRealtime();
                        return true;// really I don't filter
                    }
                });
        gpsObservableSubscription = o.subscribe(gpsObservable);*/

    }

    /**
     * release current GPS listener
     */
    public static void release()
    {
        if(emuIntervalSubscription!=null) // track emulation
        {
            emuIntervalSubscription.unsubscribe();
            emuIntervalSubscription=null;
        }

        if(gpsListener!=null) { // release GPS
            gpsListener.releaseInstance();
            gpsListener.subscriber.onCompleted();
            gpsListener = null;
        }
        coreGpsObservable =null;
        gpsObservable=null;
        gpsUiObservable=null;
    }

    /**
     * @return true if GPS is giving the current location properly
     */
    public static boolean isGPSavailable()
    {
        long t=SystemClock.elapsedRealtime();
        return (lastLocationTime+GPSLocationListener.UPDATE_INTERVAL*5) > t;
    }
    /**
     *  a listener for Android GPS system
     */
     static protected class RxGPSLocationListener extends GPSLocationListener
    {
        Subscriber<? super Location> subscriber=null;
        public RxGPSLocationListener(Context context)
        {
            super(context,  false);//true && BuildConfig.DEBUG);

        }
        void setSubscriber(Subscriber<? super Location> subscriber)
        {
            this.subscriber = subscriber;
        }
        @Override
        public void onLocationChanged(Location loc)
        {
            if(subscriber!=null && !subscriber.isUnsubscribed())
                subscriber.onNext(loc);
        }
    }

}
