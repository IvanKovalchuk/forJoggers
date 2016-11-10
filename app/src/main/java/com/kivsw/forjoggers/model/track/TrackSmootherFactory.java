package com.kivsw.forjoggers.model.track;

/**
 * Created by ivan on 5/26/16.
 */
public class TrackSmootherFactory {

    final static public int LINEAR=0, SQUARE=1, SQUARE_TURNS=2;
    static public TrackSmoother getSmoother(int id, Track track)
    {
        switch(id)
        {
            case LINEAR: return new TrackSmootherByLine(track);

            case SQUARE:
                 return new TrackSmootherByPolynom(track);

            case SQUARE_TURNS: return  new TrackSmootherByTurnAndPolynom(track);
        }

        return null;
    }
}
