package com.kivsw.forjoggers.ui.settings;

import android.content.Context;

import com.kivsw.forjoggers.model.DataModel;
import com.kivsw.forjoggers.ui.chart.AnalysingFragmentPresenter;
import com.kivsw.forjoggers.ui.BasePresenter;
import com.kivsw.forjoggers.ui.map.MapFragmentPresenter;

/**
 * Created by ivan on 03.05.2016.
 */
public class SettingsFragmentPresenter extends BasePresenter {
    static private SettingsFragmentPresenter singletone=null;

    static public SettingsFragmentPresenter getInstance(Context context)
    {
        if(singletone==null)
            singletone = new SettingsFragmentPresenter(context.getApplicationContext());
        return singletone;
    };

    SettingsFragment settingsFragment=null;

    private SettingsFragmentPresenter(Context context) {
        super(context);
    }

    @Override
    public void onSettingsChanged() {
        // informs the rest components about new settings
        AnalysingFragmentPresenter.getInstance(context).onSettingsChanged();
        MapFragmentPresenter.getInstance(context).onSettingsChanged();
        DataModel.getInstance(context).onSettingsChanged();
    }
}
