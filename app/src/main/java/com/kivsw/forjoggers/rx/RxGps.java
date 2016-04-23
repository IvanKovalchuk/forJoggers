package com.kivsw.forjoggers.rx;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import com.kivsw.forjoggers.BuildConfig;
import com.kivsw.forjoggers.helper.GPSLocationListener;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;


/**
 * create an observable for GPS
 *
 */
public class RxGps {

    static RxGPSLocationListener gpsListener=null;
    static BehaviorSubject gpsUiObservable=null;
    static PublishSubject gpsObservable=null;
    static long lastLocationTime=0; // when the last location was received

    /**
     * return the observable that emits the current location
     * @param aContext
     * @return
     */
    public static Observable<Location> getGprsObservable(Context aContext)
    {
        if(gpsObservable!=null) return gpsObservable;

        final Context context = aContext.getApplicationContext();
        gpsListener = new RxGPSLocationListener(context);
        gpsObservable= PublishSubject.create();

        Observable.create(new Observable.OnSubscribe<Location>() {
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
                })
                .subscribe(gpsObservable);

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

        gpsUiObservable = BehaviorSubject.create(gpsListener.getLastknownLocation());
        getGprsObservable(aContext).subscribe(gpsUiObservable);

        return gpsUiObservable;
    }

    /**
     * release current GPS listener
     */
    public static void release()
    {
        if(gpsListener!=null) {
            gpsListener.releaseInstance();
            gpsListener.subscriber.onCompleted();
            gpsListener = null;
        }
        gpsObservable=null;
        gpsUiObservable=null;
    }

    /**
     * @return true if GPS is giving the current location properly
     */
    public static boolean isGPSavailable()
    {
        return (lastLocationTime+GPSLocationListener.UPDATE_INTERVAL*5) < SystemClock.elapsedRealtime();
    }
    /**
     *  a listener for Android GPS system
     */
     static protected class RxGPSLocationListener extends GPSLocationListener
    {
        Subscriber<? super Location> subscriber=null;
        public RxGPSLocationListener(Context context)
        {
            super(context,  true && BuildConfig.DEBUG);

        }
        void setSubscriber(Subscriber<? super Location> subscriber)
        {
            this.subscriber = subscriber;
        }
        @Override
        public void onLocationChanged(Location loc)
        {
            subscriber.onNext(loc);
        }
    }

}
