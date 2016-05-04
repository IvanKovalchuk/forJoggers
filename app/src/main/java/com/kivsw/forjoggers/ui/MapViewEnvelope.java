package com.kivsw.forjoggers.ui;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.views.MapView;

/**
 * Created by ivan on 5/4/16.
 */
public class MapViewEnvelope extends MapView {

    protected MapViewEnvelope(final Context context, final int tileSizePixels,
                      final ResourceProxy resourceProxy, MapTileProviderBase tileProvider,
                      final Handler tileRequestCompleteHandler, final AttributeSet attrs) {
        super (context, tileSizePixels,
            resourceProxy, tileProvider,
            tileRequestCompleteHandler, attrs);
    }

    public MapViewEnvelope(final Context context, final AttributeSet attrs)
    {
        super (context, attrs);
    }
    public MapViewEnvelope(final Context context, final int tileSizePixels)
    {super (context, tileSizePixels);}

    public MapViewEnvelope(final Context context, final int tileSizePixels,
            final ResourceProxy resourceProxy)
    {
        super( context,  tileSizePixels, resourceProxy);
    }
    public MapViewEnvelope(final Context context, final int tileSizePixels,
            final ResourceProxy resourceProxy, final MapTileProviderBase aTileProvider)
    {
        super(context, tileSizePixels,  resourceProxy,  aTileProvider);
    }

    public MapViewEnvelope(final Context context, final int tileSizePixels,
                   final ResourceProxy resourceProxy, final MapTileProviderBase aTileProvider,
                   final Handler tileRequestCompleteHandler)
    {
        super(context,tileSizePixels,resourceProxy, aTileProvider, tileRequestCompleteHandler);
    };


    int touchCount=0;
    public boolean isTouching()
    {
        return touchCount>0;
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {
        if(event.getAction()==MotionEvent.ACTION_DOWN) touchCount++;
        else if(event.getAction()==MotionEvent.ACTION_UP ) touchCount--;

        return super.dispatchTouchEvent(event);
    }
}
