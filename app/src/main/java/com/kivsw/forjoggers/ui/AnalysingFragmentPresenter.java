package com.kivsw.forjoggers.ui;

import android.content.Context;

import com.kivsw.forjoggers.model.DataModel;
import com.kivsw.forjoggers.model.Track;

import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by ivan on 01.05.2016.
 */
public class AnalysingFragmentPresenter extends BasePresenter  {
    static private AnalysingFragmentPresenter singletone=null;

    static public AnalysingFragmentPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new AnalysingFragmentPresenter(context.getApplicationContext());
        return singletone;
    };

    AnalysingFragment analysingFragment=null;

    private AnalysingFragmentPresenter(Context context)
    {
        super(context);

    }

    Subscription subscription=null;
    void setUI(AnalysingFragment fragment) {
        if (fragment == null)
        {
            if(subscription!=null)
               subscription.unsubscribe();
        }
        else
        {
            // subscribe when currentTrack is changed
            subscription=
                 DataModel.getInstance(context).getTrackSmootherObservable()
                    .subscribe(new Action1<Track>() {
                        @Override
                        public void call(Track track) {
                            if(analysingFragment!=null)
                                 analysingFragment.updateChart();
                        }
                    });
        }
        analysingFragment=fragment;
    }


    @Override
    public void onSettingsChanged() {
        if(analysingFragment==null) return;
        analysingFragment.updateChart();
    }
}
