package com.kivsw.forjoggers.ui.map;

import android.location.Location;

import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.model.track.TrackSmoother;
import com.kivsw.forjoggers.ui.IBasePresenter;

/**
 * this class determinates interfaces for interaction
 * between MapFragment and MapFragmentPresenter
 */
public interface MapFragmentContract {

    interface IPresenter
            extends IBasePresenter
    {
        void setUI(IView aMapFragment);
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
        void setGPSstatus(boolean isAvailable);
        void setCurrentLocation(Location location);
        void updateTrackInfo(TrackSmoother trackSmoother, Track currentTrack );
        void startFollowingMyLocation();
        void stopFollowingMyLocation();
        void showLocation(double lat, double lon);
        void showStopButton();
        void showStartButton();
        void onSettingsChanged();
        void putCurrentTrackOnMap(Track track);
        void putSmoothTrackOnMap(Track track);
        void updateFileName();
    }
}
