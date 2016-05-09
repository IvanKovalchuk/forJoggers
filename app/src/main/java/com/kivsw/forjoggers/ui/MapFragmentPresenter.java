package com.kivsw.forjoggers.ui;

import android.content.Context;
import android.location.Location;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.model.DataModel;
import com.kivsw.forjoggers.model.Track;
import com.kivsw.forjoggers.helper.RxGps;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by ivan on 4/22/16.
 */

public class MapFragmentPresenter  extends BasePresenter {

    static private MapFragmentPresenter singletone=null;

    static public MapFragmentPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new MapFragmentPresenter(context.getApplicationContext());
        return singletone;
    };

    MapFragment mapFragment=null;
    Subscription rxGps=null, rxGpsStateUpdate=null, rxTrackInfoUpdate=null;


    final static int  WARNINGS_AND_START_SERVICE_MESSAGE_ID=0;

    private MapFragmentPresenter(Context context) {
        super(context);
    }


    void setUI(MapFragment mapFragment)
    {
        if(mapFragment==null)
        {
            if(rxGps!=null)
               rxGps.unsubscribe();
            rxGps=null;

            if(rxGpsStateUpdate!=null) rxGpsStateUpdate.unsubscribe();
            rxGpsStateUpdate=null;

            if(rxTrackInfoUpdate!=null) rxTrackInfoUpdate.unsubscribe();
            rxTrackInfoUpdate=null;

            stopTrackingUpdating();

            this.mapFragment = null;
        }
        else
        {
            if(this.mapFragment!=null)
                setUI(null); // unsubscribes old rx-subscriptions
            this.mapFragment = mapFragment;

            final WeakReference<MapFragment> fragment=new WeakReference<MapFragment>(mapFragment);

            // emits GPS locations
            rxGps= RxGps.getGprsUiObservable(context).subscribe(new Action1<Location>() {
                @Override
                public void call(Location location) {
                    if(location==null) return;
                    long t=location.getTime();
                    long ct= System.currentTimeMillis();
                    Long.valueOf(t-ct);

                    if( fragment.get()!=null)
                         fragment.get().setCurrentLocation(location);
                }
            });

            // emits events to update GPS status
            rxGpsStateUpdate = Observable.interval(300,1000, TimeUnit.MILLISECONDS,AndroidSchedulers.mainThread())
                     .subscribe(new Action1<Long>(){
                         @Override
                         public void call(Long i) {
                             if( fragment.get()!=null)
                             fragment.get().setGPSstatus(getGPSstatus());
                         }
                     });

            // emits events to update track information
            if(isTracking())
                startTrackingUpdating();


            mapFragment.setGPSstatus(RxGps.isGPSavailable());
            updateFileName();
            onCurrentTrackUpdate(DataModel.getInstance(context).getCurrentTrack());
            onSmoothTrackUpdate(DataModel.getInstance(context).getTrackSmoother());

        }

    }

    private void startTrackingUpdating()// emits events to update track information
    {
        if(! isTracking()) return;

        final WeakReference<MapFragment> fragment=new WeakReference<MapFragment>(mapFragment);

        rxTrackInfoUpdate= Observable.interval(500, TimeUnit.MILLISECONDS,AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>(){
                    @Override
                    public void call(Long i) {
                        if(!isTracking())
                        {
                            stopTrackingUpdating();
                            return;
                        };
                        if( fragment.get()!=null)
                        {
                             fragment.get().updateTrackInfo(getTrackSmoother() ,getCurrentTrack());
                        }

                    }
                });
    }
    private void stopTrackingUpdating()
    {
        if(rxTrackInfoUpdate!=null)   rxTrackInfoUpdate.unsubscribe();
        rxTrackInfoUpdate=null;
    }
    public void actionShowCurrentTrack()
    {
        if(!hasTrackData()) return;
        if(mapFragment==null) return;
        Location loc= DataModel.getInstance(context).getCurrentTrack().getGeoPoints().get(0);
        mapFragment.stopFollowingMyLocation();
        mapFragment.showLocation(loc.getLatitude(), loc.getLongitude());
    }
    void actionAnimateTrack()
    {
        RxGps.setEmulationData(new ArrayList<Location>(getCurrentTrack().getGeoPoints()));
    }
    //----------------------------------------------------------

    /**
     *  shows the stop button or the start button
     */
    public void updateTrackingStatus()
    {
        if(mapFragment==null) return;

        if(DataModel.getInstance(context).isTracking())
            mapFragment.showStopButton();
        else
            mapFragment.showStartButton();

    }

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
        startTrackingUpdating();

        MainActivityPresenter.getInstance(context).menuUpdate();

    }
    protected void doStop()
    {
        DataModel.getInstance(context).stopTracking();
    }
    public void onAfterStopTracking()
    {
        mapFragment.showStartButton();
        MainActivityPresenter.getInstance(context).menuUpdate();
        stopTrackingUpdating();
    }


    public boolean getGPSstatus()
    {
        return RxGps.isGPSavailable();
    }

    /**
     * Method is invoked when currentTrack is changed
     * @param track
     */
    public void onCurrentTrackUpdate(Track track)
    {
        if(Thread.currentThread().getId()!=1)
        {
            int xxx=0;
        }
         if(mapFragment==null) return;
          mapFragment.putCurrentTrackOnMap(track);
          //mapFragment.updateTrackInfo(DataModel.getInstance(context).getCurrentTrack(), DataModel.getInstance(context).getTrackSmoother());
    }

    public void updateFileName()
    {
        if(mapFragment==null) return;
          mapFragment.updateFileName();
    };

    /**
     * Method is invoked when smoothTrack is ready
     * @param track
     */
    public void onSmoothTrackUpdate(Track track)
    {
        if(Thread.currentThread().getId()!=1)
        {
            int xxx=0;
        }
        if(mapFragment==null) return;
        mapFragment.putSmoothTrackOnMap(track);
        mapFragment.updateTrackInfo(DataModel.getInstance(context).getTrackSmoother(),DataModel.getInstance(context).getCurrentTrack());
    }

    @Override
    public void onSettingsChanged() {
        if(mapFragment==null) return;
        mapFragment.onSettingsChanged();
    }
}
