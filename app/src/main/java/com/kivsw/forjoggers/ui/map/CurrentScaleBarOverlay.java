package com.kivsw.forjoggers.ui.map;


import com.kivsw.forjoggers.helper.UnitUtils;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;

/**
 * This class draws a scale bar on the map
 */

public class CurrentScaleBarOverlay extends ScaleBarOverlay {

    UnitUtils unitUtils;

    public CurrentScaleBarOverlay(MapView mapView,UnitUtils unitUtils)
    {
        super(mapView);
        this.unitUtils = unitUtils;
    }
    /**
     * create text label for this scale bar
     * @param meters
     * @return
     */
    protected String scaleBarLengthText(int meters)
    {
        return unitUtils.distanceToStr(meters);
    }
}
