package com.kivsw.forjoggers.rx;

import android.content.Context;
import android.location.Location;

import com.kivsw.forjoggers.BuildConfig;
import com.kivsw.forjoggers.helper.GPSLocationListener;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;


/**
 * create an observable for GPS
 *
 */
public class RxGps {

    static RxGPSLocationListener gpsListener=null;
    static BehaviorSubject gpsObservable=null;

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
        gpsObservable= BehaviorSubject.create(gpsListener.getLastknownLocation());

        Observable.create(new Observable.OnSubscribe<Location>() {
            @Override
            public void call(Subscriber<? super Location> subscriber) {
              gpsListener.setSubscriber(subscriber);
            }
        })
                .subscribe(gpsObservable);

        return gpsObservable;
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
    }

    /**
     *  a listener for Android GPS system
     */
    static class RxGPSLocationListener extends GPSLocationListener
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
