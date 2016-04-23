package com.kivsw.forjoggers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.kivsw.forjoggers.helper.GPSLocationListener;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.model.CurrentTrack;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

public class TrackingService extends Service {
    public static final String ACTION_START ="com.kivsw.forjoggers.ACTION_START",
                        ACTION_STOP ="com.kivsw.forjoggers.ACTION_STOP";

    //------------------------------------------------
    public static boolean isWorking=false;

    LocationListener mGPSLocationListener=null;
    SettingsKeeper settings=null;
    long startTime=0;
    MyHandler mHandler;

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
        mHandler = new MyHandler();
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
        mHandler.removeUpdateNotification();

        if(currentTrack!=null)
           CurrentTrack.saveTrack();
        currentTrack=null;

        TrackingServiceEventReceiver.sendServiceStatus(this, isWorking);
    };


    /**
     * sets the foreground mode for this service
     */
    final private int NOTIFICATION_ID=1;
    void turnIntoForeground()
    {

        startForeground(NOTIFICATION_ID, getNotification());
    }
    /**
     * This is the method that can be called to update the Notification
     */
    private void updateNotification()
    {
        Notification notification = getNotification();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    Notification getNotification()
    {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.tortoise_ltl);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(this.getText(R.string.app_name));
        long workingTime=SystemClock.elapsedRealtime()-startTime;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(new SimpleTimeZone(0, ""));
        sdf.format(new Date(workingTime));
        mBuilder.setContentText(sdf.format(new Date(workingTime)));

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
        return mBuilder.build();
    }

    //------------------------------------------------------
    class LocationListener extends GPSLocationListener
    {
        LocationListener(Context context)
        {
            super(context,false && BuildConfig.DEBUG);

        };

        @Override
        public void onLocationChanged(Location loc)
        {
            if(currentTrack!=null) {
                currentTrack.addPoint(loc);
            }
        }

    }
    //------------------------------------------------------
    class MyHandler extends Handler {
        final private int UPDATE_NOTOFICATION = 1;

        MyHandler() {
            super();
        }

        public void scheduleUpdateNotification() {
            removeMessages(UPDATE_NOTOFICATION);
            sendEmptyMessageDelayed(UPDATE_NOTOFICATION, 1000);
        }

        ;

        public void removeUpdateNotification() {
            removeMessages(UPDATE_NOTOFICATION);
        }

        ;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_NOTOFICATION:
                    updateNotification();
                    if (isWorking)
                        scheduleUpdateNotification();
                    break;
            }

        }
    }

}
