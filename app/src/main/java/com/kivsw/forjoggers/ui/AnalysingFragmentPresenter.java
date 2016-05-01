package com.kivsw.forjoggers.ui;

import android.content.Context;

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

    void setUI(AnalysingFragment fragment) {
        if (fragment == null)
        {

        }
        else
        {

        }
        analysingFragment=fragment;
    }
}
