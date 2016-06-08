package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.ui.map.MapFragmentPresenter;
import com.kivsw.forjoggers.ui.service.TrackingServicePresenter;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by ivan on 4/27/16.
 */
public class MainActivityIPresenter
        extends BasePresenter
        implements MainActivityContract.IPresenter
{
    static private MainActivityIPresenter singletone=null;
    static public MainActivityIPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new MainActivityIPresenter(context.getApplicationContext());
        return singletone;
    };

    MainActivity activity=null;
    boolean isActivityStarted=false;

    Subscription rxTrackingUpdate=null, rxCurrentTrackUpdate=null;

    private MainActivityIPresenter(Context context)
    {
        super(context);

        getDataModel().getErrorMessageObservable()
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
        rxTrackingUpdate=getDataModel().getStartStopObservable()
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        menuUpdate();
                    }
                });

        rxCurrentTrackUpdate=getDataModel().getCurrentTrackObservable()
                .subscribe(new Action1<Track>() {
                    @Override
                    public void call(Track track) {
                        menuUpdate();
                    }
                });

        getDataModel().getUsingCounter().startUsingBy(MainActivity.TAG);
        getDataModel().onActivityStarted();
        menuUpdate();

    }
    public void onStopActivity()
    {
        isActivityStarted=false;
        getDataModel().getUsingCounter().stopUsingBy(MainActivity.TAG);

        if(rxTrackingUpdate!=null) rxTrackingUpdate.unsubscribe();
        rxTrackingUpdate=null;

        if(rxCurrentTrackUpdate!=null) rxCurrentTrackUpdate.unsubscribe();
        rxCurrentTrackUpdate=null;
    }
    @Override
    public void onSettingsChanged() {

    }
    ///---------------------------------------------------
    @Override
    public boolean actionSaveTrack(String fileName)
    {
        return getDataModel().saveTrack(fileName);

    };
    @Override
    public boolean actionLoadTrack(String fileName)
    {
        return getDataModel().loadTrack(fileName);
    };

    @Override
    public  void actionShowCurrentTrack()
    {
        if(!hasTrackData()) return;
        MapFragmentPresenter.getInstance(context).actionShowCurrentTrack();

    };
    @Override
    public void actionAnimateTrack()
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
