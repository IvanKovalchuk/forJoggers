package com.kivsw.forjoggers.ui.map;

import android.content.Context;
import android.location.Location;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.GPSLocationListener;
import com.kivsw.forjoggers.helper.RxGpsLocation;
import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.ui.BasePresenter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by ivan on 4/22/16.
 */

public class MapFragmentPresenter
        extends BasePresenter
    implements MapFragmentContract.IPresenter
{

    static private MapFragmentPresenter singletone=null;

    static public MapFragmentPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new MapFragmentPresenter(context.getApplicationContext());
        return singletone;
    };

    MapFragmentContract.IView mapFragment=null;
    Subscription rxGps=null, rxGpsStateUpdate=null, rxTrackInfoUpdate=null,
                 rxFileNameUpdate=null, rxCurrentTrackUpdate=null, rxTrackSmotherUpdate=null,
                 rxStartStopUpdate =null;


    final static int  WARNINGS_AND_START_SERVICE_MESSAGE_ID=0;

    private MapFragmentPresenter(Context context) {
        super(context);
    }


    @Override
    public void setUI(MapFragmentContract.IView aMapFragment)
    {
        if(aMapFragment==null)
        {
            if(rxGps!=null)
               rxGps.unsubscribe();
            rxGps=null;

            if(rxGpsStateUpdate!=null) rxGpsStateUpdate.unsubscribe();
            rxGpsStateUpdate=null;

            if(rxTrackInfoUpdate!=null) rxTrackInfoUpdate.unsubscribe();
            rxTrackInfoUpdate=null;

            stopTrackingUpdating();

            if( rxFileNameUpdate!=null) rxFileNameUpdate.unsubscribe();
            rxFileNameUpdate=null;

            if(rxCurrentTrackUpdate!=null) rxCurrentTrackUpdate.unsubscribe();
            rxCurrentTrackUpdate=null;

            if(rxTrackSmotherUpdate!=null) rxTrackSmotherUpdate.unsubscribe();
            rxTrackSmotherUpdate=null;

            if(rxStartStopUpdate !=null)
                rxStartStopUpdate.unsubscribe();
            rxStartStopUpdate =null;

            mapFragment = null;
        }
        else
        {
            if(mapFragment!=null)
                setUI(null); // unsubscribes old rx-subscriptions
            mapFragment = aMapFragment;

            mapFragment.setGPSstatus(RxGpsLocation.isGpsLocationAvailable());

            // receives GPS locations
            rxGps= RxGpsLocation.getGprsUiObservable(context).subscribe(new Action1<Location>() {
                @Override
                public void call(Location location) {
                    if(location==null) return;

                    if( mapFragment!=null) {
                        if(!RxGpsLocation.isGpsLocationAvailable()) { // if we received an old (last known)location
                            location.removeSpeed();
                            location.removeBearing();
                        }
                        mapFragment.setCurrentLocation(location);
                    }
                }
            });

            // receives events to update GPS status (in a case GPS won't give data)
            rxGpsStateUpdate = Observable.interval(300,1000, TimeUnit.MILLISECONDS,AndroidSchedulers.mainThread())
                     .subscribe(new Action1<Long>(){
                         @Override
                         public void call(Long i) {
                             if( mapFragment!=null)
                                 mapFragment.setGPSstatus(RxGpsLocation.isGpsLocationAvailable());
                         }
                     });

            // emits events to update track information
            if(isTracking())
                startTrackingUpdating();

            rxFileNameUpdate=getDataModel().getFileNameObservable()
                         .subscribe(new Action1<String>() {
                             @Override
                             public void call(String s) {
                                 doUpdateFileName();
                             }
                         });

            rxCurrentTrackUpdate=getDataModel().getCurrentTrackObservable()
                        .subscribe(new Action1<Track>() {
                                @Override
                                public void call(Track track) {
                                    doCurrentTrackUpdate(track);
                                }
                            });

            rxTrackSmotherUpdate=getDataModel().getTrackSmootherObservable()
                        .subscribe(new Action1<Track>() {
                            @Override
                            public void call(Track track) {
                                doSmoothTrackUpdate(track);
                            }
                        });

            rxStartStopUpdate =getDataModel().getStartStopObservable()
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean isTracking) {
                            doAfterStartStopTracking(isTracking);
                        }
                    });


            doUpdateFileName();
            doCurrentTrackUpdate(getDataModel().getCurrentTrack());
            doSmoothTrackUpdate(getDataModel().getTrackSmoother());

        }

    }

    private void startTrackingUpdating()// emits events to update track information
    {
        if(! isTracking()) return;

        rxTrackInfoUpdate= Observable.interval(500, TimeUnit.MILLISECONDS,AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>(){
                    @Override
                    public void call(Long i) {

                        if(!isTracking())
                        {
                            stopTrackingUpdating();
                            return;
                        };
                        if( mapFragment!=null)
                        {
                            mapFragment.updateTrackInfo(getTrackSmoother() ,getCurrentTrack());
                        }

                    }
                });
    }
    private void stopTrackingUpdating()
    {
        if(rxTrackInfoUpdate!=null)   rxTrackInfoUpdate.unsubscribe();
        rxTrackInfoUpdate=null;
    }
    @Override
    public void actionShowCurrentTrack()
    {
        if(!hasTrackData()) return;
        if(mapFragment==null) return;
        Location loc= getDataModel().getCurrentTrack().getGeoPoints().get(0);
        mapFragment.stopFollowingMyLocation();
        mapFragment.showLocation(loc.getLatitude(), loc.getLongitude());
    }
    @Override
    public void actionAnimateTrack()
    {
        //RxGpsLocation.setEmulationData(new ArrayList<Location>(getCurrentTrack().getGeoPoints()));
        RxGpsLocation.setEmulationData(new ArrayList<Location>(getTrackSmoother().getGeoPoints()));
    }
    //----------------------------------------------------------

    /**
     *  shows the stop button or the start button
     */

    public void updateTrackingStatus()
    {
        if(mapFragment==null) return;

        if(getDataModel().isTracking())
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

        switch(GPSLocationListener.estimateServiceStatus(context)) {
            case 0:  if (!RxGpsLocation.isGpsLocationAvailable()) //
                       problems.append(context.getText(R.string.GPS_has_not_found_location));
                break;
            case 1:
                problems.append(context.getText(R.string.gps_disable));
                break;
            case 2:
                problems.append(context.getText(R.string.gps_unreachable));
                break;
        }

        if(getDataModel().getCurrentTrack().needToBeSaved())
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
    @Override
    public void onStopClick()
    {
         doStop();
    };
    @Override
    public void onSettingsChanged() {
        if(mapFragment==null) return;
        mapFragment.onSettingsChanged();
    }

    @Override
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
        getDataModel().startTracking();
        startTrackingUpdating();

    }
    protected void doStop()
    {
        getDataModel().stopTracking();
    }
    private void doAfterStartStopTracking(boolean isTracking)
    {
        if(mapFragment!=null) {
            if(isTracking) mapFragment.showStopButton();
            else           mapFragment.showStartButton();
        }

        stopTrackingUpdating();
    }



    /**
     * Method is invoked when currentTrack is changed
     * @param track
     */
    private void doCurrentTrackUpdate(Track track)
    {
        if(Thread.currentThread().getId()!=1)
        {
            int xxx=0;
        }
         if(mapFragment==null) return;
          mapFragment.putCurrentTrackOnMap(track);
          //mapFragment.updateTrackInfo(getDataModel().getCurrentTrack(), DataModel.getInstance(context).getTrackSmoother());
    }

    private void doUpdateFileName()
    {
        if(Thread.currentThread().getId()!=1)
        {
            int xxx=0;
        }
        if(mapFragment==null) return;
          mapFragment.updateFileName();
    };

    /**
     * Method is invoked when smoothTrack is ready
     * @param track
     */
    private void doSmoothTrackUpdate(Track track)
    {
        if(Thread.currentThread().getId()!=1)
        {
            int xxx=0;
        }
        if(mapFragment==null) return;
        mapFragment.putSmoothTrackOnMap(track);
        mapFragment.updateTrackInfo(getDataModel().getTrackSmoother(),getDataModel().getCurrentTrack());
    }


}
