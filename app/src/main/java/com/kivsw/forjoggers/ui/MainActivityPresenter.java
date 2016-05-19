package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.model.DataModel;
import com.kivsw.forjoggers.ui.map.MapFragmentPresenter;
import com.kivsw.forjoggers.ui.settings.TrackingServicePresenter;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by ivan on 4/27/16.
 */
public class MainActivityPresenter extends BasePresenter {
    static private MainActivityPresenter singletone=null;
    static public MainActivityPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new MainActivityPresenter(context.getApplicationContext());
        return singletone;
    };

    MainActivity activity=null;
    boolean isActivityStarted=false;

    Subscription rxTrackingUpdate=null;

    private MainActivityPresenter(Context context)
    {
        super(context);

        DataModel.getInstance(context).getErrorMessageObservable()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        showError(s);
                    }
                });
    }


    public void onCreateActivity(MainActivity activity)
    {
        this.activity=activity;
    }
    public void onDestroyActivity()
    {
        this.activity=null;
    }
    public void onStartActivity()
    {
        isActivityStarted=true;
        rxTrackingUpdate=DataModel.getInstance(activity).getStartStopObservable()
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        menuUpdate();
                    }
                });

        DataModel.getInstance(activity).getUsingCounter().startUsingBy(MainActivity.TAG);
        DataModel.getInstance(activity).onActivityStarted();

    }
    public void onStopActivity()
    {
        isActivityStarted=false;
        DataModel.getInstance(activity).getUsingCounter().stopUsingBy(MainActivity.TAG);

        if(rxTrackingUpdate!=null) rxTrackingUpdate.unsubscribe();
        rxTrackingUpdate=null;
    }
    @Override
    public void onSettingsChanged() {

    }
    ///---------------------------------------------------
    boolean actionSaveTrack(String fileName)
    {
        return DataModel.getInstance(context).saveTrack(fileName);

    };
    boolean actionLoadTrack(String fileName)
    {
        return DataModel.getInstance(context).loadTrack(fileName);
    };

    void actionShowCurrentTrack()
    {
        if(!hasTrackData()) return;
        MapFragmentPresenter.getInstance(context).actionShowCurrentTrack();

    };
    void actionAnimateTrack()
    {
        if(!hasTrackData()) return;
        MapFragmentPresenter.getInstance(context).actionAnimateTrack();
    };
    public void actionExit()
    {
         if(this.activity!=null)
             this.activity.finish();
        TrackingServicePresenter.getInstance(context).endBackground();
    }
    ///---------------------------------------------------

    private void showError(String msg)
    {
        showMessage(-1, context.getText(R.string.Error).toString(), msg);
    }
    private void showMessage(int msgId, String title, String msg)
    {
         MainActivity.showMessage(context, msgId, title, msg);
    }

    private void menuUpdate()
    {
        if(this.activity!=null && isActivityStarted)
            activity.supportInvalidateOptionsMenu();
    }
}
