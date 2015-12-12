package com.kivsw.forjoggers;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

/**
 * Created by ivan on 12.12.2015.
 */
public class TrackingServiceEventReceiver extends android.content.BroadcastReceiver{
    public interface OnChangingListener
    {
        void onServiceStatusChanged(boolean isRunning);
    };

    public  OnChangingListener onChangingListener = null;
    private Context context;
    public static final String ACTION_SERVICE_STATUS_CHANGED = "com.kivsw.forjoggers.TrackingService.ACTION_SERVICE_STATUS_CHANGED";

    //-----------------------------------------
    private TrackingServiceEventReceiver(Context context, OnChangingListener l)
    {
        onChangingListener = l;
        this.context = context;
    }
    //-----------------------------------------

    /**
     * Creates and registers a new reciever
     * @param context
     * @param l  a listener of OnChangingListener
     * @return return a new WebSockServiceReceiver instance
     */
    public static TrackingServiceEventReceiver createAndRegister(Context context,OnChangingListener l)
    {
        if(l==null || context==null) return null;

        TrackingServiceEventReceiver receiver = new TrackingServiceEventReceiver(context,l);

        IntentFilter filter;

        filter=new IntentFilter(ACTION_SERVICE_STATUS_CHANGED);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(receiver, filter);

        return receiver;
    }
    //-----------------------------------------
    /**
     *  unregisters a receiver

     */
    public void unregister()
    {
        if(context==null) return;
        context.unregisterReceiver(this);
    }

    //-----------------------------------------
    @Override
    public void onReceive(Context context, Intent intent) {
        if(onChangingListener==null) return;

        if (intent.getAction().equals(ACTION_SERVICE_STATUS_CHANGED))
        {
            boolean st=intent.getBooleanExtra("ServiceStatus", false);
            onChangingListener.onServiceStatusChanged(st);
        }

    }

    //-----------------------------------------

    public static void sendServiceStatus(Context context, boolean isRunning)
    {
        Intent i = new Intent(ACTION_SERVICE_STATUS_CHANGED);
        //i.setClass(context, ReceiverLogData.class);
        i.putExtra("ServiceStatus", isRunning);
        context.sendBroadcast(i);
    }

}
