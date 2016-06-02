package com.kivsw.forjoggers.ui.map;

import com.kivsw.forjoggers.ui.IBasePresenter;

/**
 * this class determinates interfaces for interaction
 * between MapFragment and MapFragmentPresenter
 */
public interface MapFragmentContract {

    interface IPresenter
            extends IBasePresenter
    {
        void setUI(MapFragment aMapFragment);
        void actionShowCurrentTrack();
        void actionAnimateTrack();

        /**
         *  shows the stop button or the start button
         */
        void updateTrackingStatus();

        /**
         * starts recording a new track
         */
        void onStartClick();
        /**
         * Stops recording the current track
         */
        void onStopClick();

        void onMessageBoxClose(int messageId, boolean OkButton);

    }


    interface IView
    {
        void showMessageDialog(int id, String caption, String message);
    }
}
