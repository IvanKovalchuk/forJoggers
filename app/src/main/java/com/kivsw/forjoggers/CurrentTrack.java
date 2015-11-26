package com.kivsw.forjoggers;

import android.content.Context;

/**
 * Created by ivan on 20.11.15.
 */
public class CurrentTrack extends Track {
    static private CurrentTrack track=null;
    SettingsKeeper settings=null;

    static synchronized  public CurrentTrack getInstance(Context context)
    {
        if(track==null)
        {
            track = new CurrentTrack();
            track.settings = SettingsKeeper.getInstance(context);
            track.fromJSON(track.settings.getCurrentTrack());
        }
        return track;
    }

    static synchronized public void saveTrack()
    {
        if(track==null) return;
        track.settings.setCurrentTrack( track.toJSON());
    }

    private CurrentTrack()
    {super();}
}
