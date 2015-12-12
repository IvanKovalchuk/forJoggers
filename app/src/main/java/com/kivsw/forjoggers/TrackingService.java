package com.kivsw.forjoggers;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class TrackingService extends Service {
    public static final String ACTION_START ="com.kivsw.forjoggers.ACTION_START",
                        ACTION_STOP ="com.kivsw.forjoggers.ACTION_STOP";

    //------------------------------------------------
    public static boolean isWorking=false;

    LocationListener mGPSLocationListener=null;
    CurrentTrack currentTrack=null;
    SettingsKeeper settings=null;

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


    //-------------------------------------------------------
    public TrackingService() {
        super();
        settings = SettingsKeeper.getInstance(this);
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
        currentTrack=CurrentTrack.getInstance(this);

        currentTrack.clear();
        currentTrack.setActivityType(settings.getActivityType());
        currentTrack.timeStart=SystemClock.elapsedRealtime();
        mGPSLocationListener = new LocationListener(this);
        Location loc = mGPSLocationListener.getLastknownLocation();

        isWorking=true;
        turnIntoForeground();
    };

    private void doStop()
    {
        isWorking=false;
        if(currentTrack!=null)
            currentTrack.timeStop= SystemClock.elapsedRealtime();
        if(mGPSLocationListener!=null)
            mGPSLocationListener.releaseInstance();
        mGPSLocationListener=null;

        if(currentTrack!=null)
           CurrentTrack.saveTrack();
        currentTrack=null;
    };


    /**
     * sets the foreground mode for this service
     */
    void turnIntoForeground()
    {
        final int id=1;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(this.getText(R.string.app_name));
        //mBuilder.setContentText(this.getText(R.string.app_name));


        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, MainActivity.class);
        //intent.setClassName(this, settings.getActivityName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mBuilder.setContentIntent(PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT));

		/*NotificationManager mNotificationManager =
			    (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
		// notificationId allows you to update the notification later on.
		mNotificationManager.notify(id, mBuilder.build());*/
        startForeground(id, mBuilder.build());
    }
    //------------------------------------------------------
    class LocationListener extends GPSLocationListener
    {
        LocationListener(Context context)
        {
            super(context,!false && BuildConfig.DEBUG);

        };

        @Override
        public void onLocationChanged(Location loc)
        {
            if(currentTrack!=null) {
                currentTrack.addPoint(loc);
            }
        }

    }

}
