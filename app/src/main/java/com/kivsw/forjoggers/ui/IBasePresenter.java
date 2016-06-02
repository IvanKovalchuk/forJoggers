package com.kivsw.forjoggers.ui;

import com.kivsw.forjoggers.model.IDataModel;
import com.kivsw.forjoggers.model.track.CurrentTrack;
import com.kivsw.forjoggers.model.track.TrackSmoother;

/**
 * Created by ivan on 6/2/16.
 */
public interface IBasePresenter {
    boolean isTracking();
    long getTrackingTime();
    boolean hasTrackData();
    boolean trackNeedToBeSaved();
    CurrentTrack getCurrentTrack();
    TrackSmoother getTrackSmoother();
    void onSettingsChanged();

}
