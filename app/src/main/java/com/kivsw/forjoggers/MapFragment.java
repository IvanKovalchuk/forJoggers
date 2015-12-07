package com.kivsw.forjoggers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

//import org.osmdroid.bonuspack.overlays.Polyline;


public class MapFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private MapView mapView=null;

    private TextView textTrackInfo;
    Polyline originalPath =null, smoothyPath=null;

    private View rootView;

    private Track currentTrack;
    TrackSmoother trackSmoother;
    private SettingsKeeper settings=null;
    private MyGPSLocationListener mGPSLocationListener;
    private MyLocationNewOverlay myLocationoverlay;
    private MyHandler mHandler;



    public MapFragment() {
        super();

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = SettingsKeeper.getInstance(getActivity());
        currentTrack = CurrentTrack.getInstance(getActivity());
        trackSmoother = new TrackSmootherByLine(currentTrack);
                        //new TrackSmootherBy2Lines(currentTrack);

        mGPSLocationListener = new MyGPSLocationListener(getActivity());
        mHandler=new MyHandler();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView=inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView) rootView.findViewById(R.id.map);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);
        //mapView.setTileSource(TileSourceFactory.CYCLEMAP);
        OnlineTileSourceBase tileSource=null;
        tileSource = TileSourceFactory.MAPNIK;//CYCLEMAP;//CLOUDMADESMALLTILES;//MAPQUESTAERIAL;//MAPQUESTOSM;//PUBLIC_TRANSPORT;
        mapView.setTileSource(tileSource);
        //mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // My Location Overlay
        myLocationoverlay = new MyLocationNewOverlay(getActivity(), mapView);
        myLocationoverlay.enableMyLocation(mGPSLocationListener); // not on by default
        myLocationoverlay.disableFollowLocation();
        myLocationoverlay.setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(myLocationoverlay);

        // path overlay
        originalPath = new Polyline(getActivity());
        originalPath.setColor(0xFFFF0000);
        originalPath.setWidth(3f);
        mapView.getOverlays().add(originalPath);

        smoothyPath =  new Polyline(getActivity());
        smoothyPath.setColor(0xFF7F3030);
        smoothyPath.setWidth(2f);
        mapView.getOverlays().add(smoothyPath);

        IMapController mapController = mapView.getController();
        mapController.setZoom(settings.getZoomLevel());
        GeoPoint c=new GeoPoint(settings.getLastLatitude(), settings.getLastLongitude());
        mapController.setCenter(c);
        mHandler.scheduleRestoringPosition();
        /*if(mGPSLocationListener.getLastKnownLocation()!=null)
           mapController.animateTo(new GeoPoint(mGPSLocationListener.getLastKnownLocation()));*/
        mapView.setMapListener(new MapListener());

        textTrackInfo = (TextView)rootView.findViewById(R.id.textTrackInfo);
        textTrackInfo.setText("");

        updateTrack(true);

        return rootView;
    }
     @Override
     public void onDestroy()
     {
         super.onDestroy();
         mGPSLocationListener.releaseInstance();
         trackSmoother.release();
     }

    @Override
    public void onResume()
    {
        super.onResume();

        if(TrackingService.isWorking)
            onStartTrackingService();

    }
    @Override
    public void onPause()
    {
        super.onPause();

        mHandler.deleteAllMessages();

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            ((MainActivity)getActivity()).mapFragment=this;
            if(activity instanceof OnFragmentInteractionListener)
               mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //-------------------------------------

    /** is invoked when the tracking service has been started
     *
     */
    public void onStartTrackingService()
    {
        updateTrack(true);
        mHandler.startTrackUpdate(true);
    }

    /**
     * check if mylocation is on the screen and correct visible bounds
     */
    void checkMyLocationVisibility()
    {
        if(!settings.getReturnToMyLocation()) return;
        if(mGPSLocationListener.getLastKnownLocation()==null) return;
        GeoPoint topLeftGpt     = (GeoPoint) mapView.getProjection().fromPixels(0, 0);
        GeoPoint bottomRightGpt = (GeoPoint) mapView.getProjection().fromPixels(
                mapView.getWidth(), mapView.getHeight());

        BoundingBoxE6 bounding=mapView.getBoundingBox();

        GeoPoint cl=new GeoPoint(mGPSLocationListener.getLastKnownLocation());
       boolean r= bounding.contains(cl);
       if(!r)
       {
           IMapController mapController = mapView.getController();
           mapController.animateTo(cl);
           settings.setZoomLevel(settings.getZoomLevel(), cl.getLatitude(), cl.getLongitude());
       }

    };

    /**
     *
     */
    void setTrack(String json)
    {
        currentTrack.fromJSON(json);
        updateTrack(true);
    }


    /**
     * update the CurrentTrack if it's necessary
     */
    void updateTrack(boolean always)
    {
        if(always || currentTrack.getGeoPoints().size() != originalPath.getNumberOfPoints()) {
            putCurrentTrackOnMap();
        }
        updateTrackInfo();
    }

    private void updateTrackInfo()
    {
        StringBuilder str=new StringBuilder();

        str.append(getText(R.string.distance));
        str.append((long)currentTrack.getTrackDistance());
        str.append(" (");
        str.append((long)trackSmoother.getTrackDistance());
        str.append(")\n");
        str.append(getText(R.string.time));

        str.append(currentTrack.getTrackTimeStr());
        //str.append(Double.toString(currentTrack.getTrackTime()/1000));

        textTrackInfo.setText(str);
    };

    private void putCurrentTrackOnMap()
    {

        //OverlayManager om=mapView.getOverlayManager();
        ArrayList<GeoPoint> points=new ArrayList<GeoPoint>(currentTrack.getGeoPoints().size());;

        for(Location l:currentTrack.getGeoPoints()) {
            points.add(new GeoPoint(l));
        }
        originalPath.setPoints(points);

        points.clear();
        for(Location l:trackSmoother.getGeoPoints()) {
            points.add(new GeoPoint(l));
        }
        smoothyPath.setPoints(points);
        //om.add(originalPath);

        mapView.invalidate();
    }

    boolean loadTrackFromFile(String fileName)
    {
        boolean r=currentTrack.loadGeoPoint(fileName);
        updateTrack(true);
        mHandler.startPointAnimation();
        return r;
    }

    boolean saveTrackToFile(String fileName)
    {
        return currentTrack.saveGeoPoint(fileName);
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    //------------------------------------------
    class MapListener implements org.osmdroid.events.MapListener
    {

        @Override
        public boolean onScroll(ScrollEvent event) {
            mHandler.scheduleRestoringPosition();
            GeoPoint c=mapView.getBoundingBox().getCenter();
            settings.setZoomLevel(settings.getZoomLevel(), c.getLatitude(), c.getLongitude());
            return false;
        }

        @Override
        public boolean onZoom(ZoomEvent event) {
            int zl=event.getZoomLevel();
            GeoPoint c=mapView.getBoundingBox().getCenter();
            settings.setZoomLevel(zl, c.getLatitude(), c.getLongitude());
            return false;
        }
    }
    //----------------------------------------
    class MyGPSLocationListener extends GPSLocationListener
            implements IMyLocationProvider
    {

        MyGPSLocationListener(Context context)
        {super(context,BuildConfig.DEBUG);}

        //----------------------------------------
        @Override
        public void onLocationChanged(Location loc)
        {
            super.onLocationChanged(loc);
            if(consumer!=null) {
                consumer.onLocationChanged(loc, this);

                mHandler.scheduleIfNecessaryRestoringPosition();
            }
        }

        //------------------------------
        IMyLocationConsumer consumer=null;
        @Override
        public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {
            consumer = myLocationConsumer;
            return true;
        }

        @Override
        public void stopLocationProvider() {
            consumer=null;
        }

        @Override
        public Location getLastKnownLocation() {
            return super.getLastknownLocation();
        }
    }
    //----------------------------------------

    class MyHandler extends Handler
    {
        final private int RESTORE_POSITION=1, POINT_ANIMATION=2, UPDATE_TRACK=3;
        MyHandler()
        {
            super();
        }

        public void deleteAllMessages()
        {
            removeMessages(RESTORE_POSITION);
            removeMessages(POINT_ANIMATION);
            removeMessages(UPDATE_TRACK);

            if(marker!=null)
               mapView.getOverlays().remove(marker);
            marker=null;

        }

        public void scheduleIfNecessaryRestoringPosition()
        {
            if(!hasMessages(RESTORE_POSITION))
                sendEmptyMessageDelayed(RESTORE_POSITION, 7000);
        }
        public void scheduleRestoringPosition()
        {
            removeMessages(RESTORE_POSITION);
            sendEmptyMessageDelayed(RESTORE_POSITION, 7000);
        }

        private int pointIndex=0;
        MyLocationNewOverlay marker=null;
        IMyLocationProvider fakeProvider=new IMyLocationProvider(){
            Location loc=new Location("fake");
            public boolean startLocationProvider(IMyLocationConsumer myLocationConsumer) {return true;};
            public void stopLocationProvider() {}
            public Location getLastKnownLocation() { return loc; }
        };
        public void startPointAnimation()
        {
            pointIndex=0;
            if(marker==null) {
                marker = new MyLocationNewOverlay(getActivity(), mapView);//new Marker(mapView);
                marker.disableFollowLocation();
                marker.setDrawAccuracyEnabled(true);
                marker.enableMyLocation(fakeProvider);
                mapView.getOverlayManager().add(marker);
            }
            removeMessages(POINT_ANIMATION);
            sendEmptyMessageDelayed(POINT_ANIMATION, 500);
        }
        private void nextPointAnimation()
        {
            Track track = trackSmoother; // currenttrack
            if(pointIndex<track.getGeoPoints().size()) {
                Location loc=track.getGeoPoints().get(pointIndex);
                pointIndex++;

                marker.onLocationChanged(loc,null);
                sendEmptyMessageDelayed(POINT_ANIMATION, 300);

            }
            else
            {
                //marker.remove(mapView);
                mapView.getOverlays().remove(marker);
                marker=null;
                mapView.invalidate();
            }
        }
        public Bitmap rotateBitmap(Bitmap source, float angle)
        {
            Matrix matrix = new Matrix();
            matrix.postScale(0.2f,0.2f);
            matrix.postRotate(angle);

            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }

        long stopTrackUpdatingTime=0;
        public void startTrackUpdate(boolean doProlongWorking)
        {
            long t= SystemClock.elapsedRealtime();
            if(doProlongWorking)
            {
                stopTrackUpdatingTime = t+1000;
            };

            if(t<=stopTrackUpdatingTime) {
                removeMessages(UPDATE_TRACK);
                sendEmptyMessageDelayed(UPDATE_TRACK, 300);
            }
        };

        public void handleMessage (Message msg)
        {
            switch(msg.what)
            {
                case RESTORE_POSITION:
                    checkMyLocationVisibility();
                    break;
                case POINT_ANIMATION:
                     nextPointAnimation();
                     break;
                case UPDATE_TRACK:
                    updateTrack(false);
                    startTrackUpdate(TrackingService.isWorking);
                    break;
            }

        }

    }

}
