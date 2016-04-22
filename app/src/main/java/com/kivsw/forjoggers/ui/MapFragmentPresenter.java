package com.kivsw.forjoggers.ui;

import android.content.Context;
import android.location.Location;

import com.kivsw.forjoggers.rx.RxGps;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by ivan on 4/22/16.
 */

public class MapFragmentPresenter {

    static private MapFragmentPresenter singletone=null;
    static public MapFragmentPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new MapFragmentPresenter(context);
        return singletone;
    };

    Context context=null;
    MapFragment mapFragment=null;
    Subscription rxGps=null;

    private MapFragmentPresenter(Context context) {
        this.context=context;
    }

    void setUI(MapFragment mapFragment)
    {
        this.mapFragment = mapFragment;
        if(mapFragment==null)
        {
            if(rxGps!=null)
               rxGps.unsubscribe();
        }
        else
        {
            final MapFragment fragment=mapFragment;
            rxGps= RxGps.getGprsObservable(context).subscribe(new Action1<Location>() {
                @Override
                public void call(Location location) {
                    if(location==null) return;
                    long t=location.getTime();
                    long ct= System.currentTimeMillis();
                    Long.valueOf(t-ct);

                    fragment.setCurrentLocation(location);
                }
            });
        }

    }
}
