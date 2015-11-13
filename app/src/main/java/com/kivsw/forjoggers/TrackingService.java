package com.kivsw.forjoggers;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class TrackingService extends Service {
    public static final String ACTION_START ="com.kivsw.forjoggers.ACTION_START",
                        ACTION_STOP ="com.kivsw.forjoggers.ACTION_STOP",
                        ACTION_SEND_TRACK ="com.kivsw.forjoggers.ACTION_SEND_TRACK";

    LocationListener mGPSLocationListener=null;
    Track track=null;
    //------------------------------------------------------

    /**
     * Starts tracking
     * @param context
     */
    public static void start(Context context)
    {
        Intent i=new Intent(ACTION_START, null,context, TrackingService.class);
        //i.setClass(context,TrackingService.class);
        context.startService(i);
    }

    /**
     * Stops tracking and sends the track to Activity
     * @param context
     */
    public static void stop(Context context)
    {
        Intent i=new Intent(ACTION_STOP, null,context, TrackingService.class);
        context.startService(i);
    }

    public static void sendTrack(Context context)
    {
        Intent i=new Intent(ACTION_SEND_TRACK, null,context, TrackingService.class);
        context.startService(i);
    }
    //-------------------------------------------------------
    public TrackingService() {
        super();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action="";
        if(intent!=null)
            action=intent.getAction();

        switch(action)
        {
            case ACTION_START: doStart();
                break;
            case ACTION_STOP:  doStop();
                break;
            case ACTION_SEND_TRACK: doSendTrack();
                break;


        };

        if(mGPSLocationListener==null)
            stopSelf();
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        doStop();
    }



    //-----------------------------------------
    private void doStart()
    {
        track=new Track();
        mGPSLocationListener = new LocationListener(this);
    };

    private void doStop()
    {
        if(mGPSLocationListener!=null)
            mGPSLocationListener.releaseInstance();
        mGPSLocationListener=null;

        if(track!=null)
          track=null;
    };

    private void doSendTrack()
    {

    };

    class LocationListener extends GPSLocationListener
    {
        LocationListener(Context context)
        {
            super(context);

        };

        @Override
        public void onLocationChanged(Location loc)
        {
            if(track!=null)
                track.mGeoPoints.add(loc);
        }

    }

}
