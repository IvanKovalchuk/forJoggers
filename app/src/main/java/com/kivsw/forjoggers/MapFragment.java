package com.kivsw.forjoggers;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.Polyline;
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


public class MapFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    MapView mapView;

    View rootView;

    SettingsKeeper settings=null;
    MyGPSLocationListener mGPSLocationListener;
    MyLocationNewOverlay myLocationoverlay;
    MyHandler mHandler;

    public MapFragment() {
        super();

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = SettingsKeeper.getInstance(getActivity());

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
        tileSource = TileSourceFactory.CYCLEMAP;//CLOUDMADESMALLTILES;//MAPQUESTAERIAL;//MAPQUESTOSM;//PUBLIC_TRANSPORT;
        mapView.setTileSource(tileSource);
        //mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // My Location Overlay
        myLocationoverlay = new MyLocationNewOverlay(getActivity(), mapView);
        myLocationoverlay.enableMyLocation(mGPSLocationListener); // not on by default
        myLocationoverlay.disableFollowLocation();
        myLocationoverlay.setDrawAccuracyEnabled(true);
        mapView.getOverlays().add(myLocationoverlay);

        IMapController mapController = mapView.getController();
        mapController.setZoom(settings.getZoomLevel());
        mapController.animateTo(new GeoPoint(mGPSLocationListener.getLastKnownLocation()));
        mapView.setMapListener(new MapListener());

        Polyline pl;
        return rootView;
    }
     @Override
     public void onDestroy()
     {
         super.onDestroy();
         mGPSLocationListener.releaseInstance();
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

    /**
     * check if mylocation is on the screen and correct visible bounds
     */
    void checkMyLocationVisibility()
    {
        GeoPoint topLeftGpt     = (GeoPoint) mapView.getProjection().fromPixels(0, 0);
        GeoPoint bottomRightGpt = (GeoPoint) mapView.getProjection().fromPixels(
                mapView.getWidth(), mapView.getHeight());

        BoundingBoxE6 bounding=mapView.getBoundingBox();

        GeoPoint cl=new GeoPoint(mGPSLocationListener.getLastKnownLocation());
       boolean r= bounding.contains(cl);
       if(!r)
       {
           IMapController mapController = mapView.getController();
           mapController.animateTo(new GeoPoint(mGPSLocationListener.getLastKnownLocation()));
       }

    };

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
            return false;
        }

        @Override
        public boolean onZoom(ZoomEvent event) {
            int zl=event.getZoomLevel();
            settings.setZoomLevel(zl);
            return false;
        }
    }
    //----------------------------------------
    class MyGPSLocationListener extends GPSLocationListener
            implements IMyLocationProvider
    {

        MyGPSLocationListener(Context context)
        {super(context);}

        //----------------------------------------
        @Override
        public void onLocationChanged(Location loc)
        {
            super.onLocationChanged(loc);
            if(consumer!=null) {
                consumer.onLocationChanged(loc, this);
                mHandler.scheduleRestoringPosition();
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
        final private int RESTORE_POSITION=1;
        MyHandler()
        {
            super();
        }


        public void scheduleRestoringPosition()
        {
            removeMessages(RESTORE_POSITION);
            sendEmptyMessageDelayed(RESTORE_POSITION, 2000);
        }
        public void handleMessage (Message msg)
        {
            switch(msg.what)
            {
                case RESTORE_POSITION:
                    checkMyLocationVisibility();
                    break;
            }

        }

    }

}
