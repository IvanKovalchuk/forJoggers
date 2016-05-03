package com.kivsw.forjoggers.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.helper.UsingCounter;
import com.kivsw.forjoggers.model.DataModel;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * This service works when the application is tracking.
 * it does not have presenter because it's too simple
 */
public class TrackingService extends Service {
    public static final String TAG="TrackingService";
    public static final String ACTION_START ="com.kivsw.forjoggers.ACTION_START",
                        ACTION_STOP ="com.kivsw.forjoggers.ACTION_STOP";

    //------------------------------------------------
    boolean isWorking=false;
    SettingsKeeper settings=null;
    TrackingServicePresenter presenter =null;
    MyHandler mHandler;


    public final static String TRACKING="tracking", BACKGROUND="background", SAVING="saving"; // processes that may keep this service working
    UsingCounter<String> usingCounter=null;

    //------------------------------------------------------
    /**
     * Starts tracking
     * @param context
     * @param reason
     */
    static void start(Context context, String reason)
    {
        if(!reason.equals(TRACKING) &&  !reason.equals(BACKGROUND) &&  !reason.equals(SAVING))
            return;

        Intent i=new Intent(ACTION_START, null,context, TrackingService.class);
        i.putExtra("reason", reason);
        context.startService(i);
    }

    /**
     * Stops tracking and sends the track to Activity
     * @param context
     */
    static void stop(Context context,String reason)
    {
        Intent i=new Intent(ACTION_STOP, null,context, TrackingService.class);
        i.putExtra("reason", reason);
        context.startService(i);
    }


    //-------------------------------------------------------
    public TrackingService() {
        super();
        settings = SettingsKeeper.getInstance(this);
        mHandler = new MyHandler(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        presenter = TrackingServicePresenter.getInstance(this);
        presenter.setService(this);
        usingCounter  = new UsingCounter<String>(null);
    }
    @Override
    public void onDestroy() {
        doStopAll();
        presenter.setService(null);
        mHandler.removeUpdateNotification();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action="", reason;
        if(intent!=null)
            action=intent.getAction();
        reason = intent.getStringExtra("reason");

        switch(action)
        {
            case ACTION_START: doStart(reason);
                break;
            case ACTION_STOP:  doStop(reason);
                break;

        }

        if(usingCounter.count()==0) {
            isWorking=false;
            stopSelf();
        }
        return START_NOT_STICKY;
    }
    //-----------------------------------------
    private void doStart(String reason)
    {
        if(!usingCounter.startUsingBy(reason)) // if we have already had this reason
            return;
        if(!isWorking) {
            turnIntoForeground();
            isWorking=true;
        }
        else
            updateNotification();

    }

    private void doStop(String reason)
    {
        if(!usingCounter.stopUsingBy(reason))// if we have already had this reason
            return;
        updateNotification();
        //mHandler.removeUpdateNotification();
    }

    private void doStopAll()
    {
        Object reasons[]=usingCounter.array();
        for(Object reason:reasons)
        {
            doStop((String)reason);
        }
    }

    /**
     * sets the foreground mode for this service
     */
    final private int NOTIFICATION_ID=1;
    void turnIntoForeground()
    {
        startForeground(NOTIFICATION_ID, getNotification());

        if(usingCounter.contains(TRACKING))
            mHandler.scheduleUpdateNotification();
    }
    /**
     * This is the method that can be called to update the Notification
     */
    private void updateNotification()
    {
        Notification notification = getNotification();

        if(notification!=null) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
        else
        {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }

        if(usingCounter.contains(TRACKING))
            mHandler.scheduleUpdateNotification();
    }

    /**  chooses a Notification for the current application state
    *
     */
    private Notification getNotification()
    {
        if(usingCounter.contains(TRACKING)) {
            return getTrackingNotification();
        }
        else  if(usingCounter.contains(SAVING))
        {
            return getSavingNotification();
        }
        else  if(usingCounter.contains(BACKGROUND))
        {
            return getBackgroundgNotification();
        }

        return null;
    }
    private Notification getTrackingNotification()
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.tortoise_ltl);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(this.getText(R.string.app_name));
        long workingTime= presenter.getTrackingTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(new SimpleTimeZone(0, ""));
        sdf.format(new Date(workingTime));
        mBuilder.setContentText(sdf.format(new Date(workingTime)));

        mBuilder.setContentIntent(createNotificationIntent());

        return mBuilder.build();
    }
    private Notification getSavingNotification()
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.tortoise_ltl);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(this.getText(R.string.app_name));

        mBuilder.setContentText(getText(R.string.file_saving));
        mBuilder.setContentIntent(createNotificationIntent());

        return mBuilder.build();
    }
    private Notification getBackgroundgNotification()
    {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.drawable.tortoise_ltl);
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        mBuilder.setContentTitle(this.getText(R.string.app_name));

        mBuilder.setContentText(getText(R.string.background));
        mBuilder.setContentIntent(createNotificationIntent());

        return mBuilder.build();
    }

    PendingIntent createNotificationIntent()
    {
        Intent intent=new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, MainActivity.class);
        //intent.setClassName(this, settings.getActivityName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return PendingIntent.getActivity(this, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //------------------------------------------------------
    static class MyHandler extends Handler {
        final private int UPDATE_NOTOFICATION = 1;
        WeakReference<TrackingService> service;

        MyHandler(TrackingService service) {
            super();
            this.service=new WeakReference<>(service);
        }

        public void scheduleUpdateNotification() {
            removeMessages(UPDATE_NOTOFICATION);

            TrackingService s=service.get();
            if(s==null) return;

            long t=service.get().presenter.leftToNextSecond();
            sendEmptyMessageDelayed(UPDATE_NOTOFICATION, t);
        }

        public void removeUpdateNotification() {
            removeMessages(UPDATE_NOTOFICATION);
        }


        public void handleMessage(Message msg) {
            TrackingService s=service.get();
            if(s==null) return;

            switch (msg.what) {
                case UPDATE_NOTOFICATION:
                    s.updateNotification();
                    if (s.isWorking)
                        scheduleUpdateNotification();
                    break;
            }

        }
    }

}
