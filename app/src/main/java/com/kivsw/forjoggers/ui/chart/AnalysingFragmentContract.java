package com.kivsw.forjoggers.ui.chart;

import com.kivsw.forjoggers.ui.IBasePresenter;

/**
 * Created by ivan on 6/2/16.
 */
public interface AnalysingFragmentContract {
    interface IPresenter extends IBasePresenter
    {
        void setUI(AnalysingFragment fragment);
    }

    interface IView {
    }

}
