package com.kivsw.forjoggers.ui;

import android.content.Context;
import android.location.Location;

import com.kivsw.dialog.MessageDialog;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.GPSLocationListener;
import com.kivsw.forjoggers.model.CurrentTrack;
import com.kivsw.forjoggers.model.DataModel;
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

    final static int  WARNINGS_AND_START_SERVICE_MESSAGE_ID=0;

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
            rxGps= RxGps.getGprsUiObservable(context).subscribe(new Action1<Location>() {
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
    //----------------------------------------------------------
    /**
     * starts recording a new track
     */
    public void onStartClick()
    {
        StringBuilder problems=new StringBuilder();

        if(!getGPSstatus())
            problems.append(context.getText(R.string.GPS_has_not_found_location));

        if(DataModel.getInstance(context).getCurrentTrack().needToBeSaved())
            problems.append(context.getText(R.string.track_may_be_lost));

        if(problems.length()>0) {
            problems.append(context.getText(R.string.Continue));
            mapFragment.showMessageDialog(WARNINGS_AND_START_SERVICE_MESSAGE_ID,
                    context.getText(R.string.Warning).toString(),
                    problems.toString());
        }
        else
            doStart();

    }

    /**
     * Stops recording the current track
     */
    public void onStopClick()
    {
         doStop();
    };
    //--------------------------------------------------------------------
    public void onMessageBoxClose(int messageId, boolean OkButton)
    {
         if(OkButton && messageId==WARNINGS_AND_START_SERVICE_MESSAGE_ID)
         {
              doStart();
         }
    }

    protected void doStart()
    {
        mapFragment.showStopButton();
        DataModel.getInstance(context).startTracking();
    }
    protected void doStop()
    {
        DataModel.getInstance(context).stopTracking();
        mapFragment.showStartButton();
    }

    public boolean getGPSstatus()
    {
        return RxGps.isGPSavailable();
    }
}
