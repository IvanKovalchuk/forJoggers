package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.model.DataModel;

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

    private MainActivityPresenter(Context context)
    {
        super(context);
    }


    public void onCreateActivity(MainActivity activity)
    {
        this.activity=activity;
        DataModel.getInstance(activity).getUsingCounter().startUsingBy(MainActivity.TAG);

    }
    public void onDestroyActivity()
    {
        DataModel.getInstance(activity).getUsingCounter().stopUsingBy(MainActivity.TAG);
        this.activity=null;
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
    void actionQuit()
    {
         if(this.activity!=null)
             this.activity.finish();
        TrackingServicePresenter.getInstance(context).endBackground();
    }
    ///---------------------------------------------------

    public void showError(String msg)
    {
        showMessage(-1, context.getText(R.string.Error).toString(), msg);
    }
    public void showMessage(int msgId, String title, String msg)
    {
         MainActivity.showMessage(context, msgId, title, msg);
    }
}
