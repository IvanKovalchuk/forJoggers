package com.kivsw.forjoggers.ui.map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.osmdroid.views.MapView;

/**
 * This class is needed to distinguish when the user is touching the map.
 */
public class MapViewEnvelope extends MapView {


    public MapViewEnvelope(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }


    /**
     * returns true when the user is touching the map
     */
    int touchCount=0;
    public boolean isTouching()
    {
        return touchCount>0;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
            touchCount++;
        else if(event.getAction()==MotionEvent.ACTION_UP )
            touchCount--;
        else if(event.getAction()==MotionEvent.ACTION_CANCEL )
            touchCount=0;

        if(touchCount<0) touchCount=0;

        return super.dispatchTouchEvent(event);
    }
}
