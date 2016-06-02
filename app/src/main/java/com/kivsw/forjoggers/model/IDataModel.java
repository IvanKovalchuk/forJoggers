package com.kivsw.forjoggers.model;

import com.kivsw.forjoggers.helper.UsingCounter;
import com.kivsw.forjoggers.model.track.CurrentTrack;
import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.model.track.TrackSmoother;

import rx.Observable;

/**
 * Created by ivan on 6/2/16.
 */
public interface IDataModel {

    /**
     *
     */
    UsingCounter getUsingCounter();
    TrackSmoother getTrackSmoother();
    CurrentTrack getCurrentTrack();
    boolean isTracking();
    boolean hasTrackData();
    long getTrackingTime();

    /**
     * starts recording a new track
     */
    void startTracking();

    /**
     * Stops recording the current track
     */
    void stopTracking();

    /**
     * Save track to a file
     */
    boolean saveTrack(String fileName);

    /**
     * Save track to file
     */
    boolean loadTrack(String fileName);

    /**
     *  return observable for the current track
     */
    Observable<Track> getCurrentTrackObservable();

    /**
     *  return observable for the smooth track
     */
    Observable<Track> getTrackSmootherObservable();

    /**
     * observable for an error message
     */
    Observable<String> getErrorMessageObservable();

    /**
     * observable for start/stop events
     */
    Observable<Boolean> getStartStopObservable();

    /**
     * this observable emits an event when the fileName has been changed
     */
    Observable<String> getFileNameObservable();

    /**
     * is invoked by a presenter when the user has changes the settings
     */
    void onSettingsChanged();

    /**
     * is invoked by a presenter when the activity has been started
     */
    void onActivityStarted();
}
