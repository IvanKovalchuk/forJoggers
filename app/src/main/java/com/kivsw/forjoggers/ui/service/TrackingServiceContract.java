package com.kivsw.forjoggers.ui.service;

import com.kivsw.forjoggers.ui.IBasePresenter;


/**
 * this class determinates interfaces for interaction
 * between TrackingService and TrackingServicePresenter
 */
public class TrackingServiceContract {
    interface IPresenter
            extends IBasePresenter
    {
        void setService(TrackingService service);

        /** returns the time (milliseconds) to the next second,
         *  the moment to update the notification
         */
        long leftToNextSecond();

        /**
         * accomplishes the notification's action "exit"
         */
        void action_exit();

        /**
         *  accomplishes the notification's action "stop tracking"
         */
        void action_stopTracking();

        /**
         * inform service that tracking has been started
         */
        void startTracking();

        /**
         * inform service that tracking has been stopped
         */
        void endTracking();
        //-------------------------------------------------

        /**
         * inform service that the file saving process  has been started
         */
        void startSaving();
        /**
         * inform service that the file saving process has been stopped
         */
        void endSaving();

        //-------------------------------------------------

        /**
         * inform service that the background working has been started
         */
        void startBackground();
        /**
         * inform service that the background working  has been stopped
         */
        void endBackground();

        /**
         * inform service that the background working has been started
         */
        void startTTSspeaking();
        /**
         * inform service that the background working  has been stopped
         */
        void endTTSspeaking();


    }
    interface IView
    {

    }
}
