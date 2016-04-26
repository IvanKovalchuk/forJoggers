package com.kivsw.forjoggers.ui;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kivsw.dialog.MessageDialog;
import com.kivsw.forjoggers.BuildConfig;
import com.kivsw.forjoggers.CustomPagerView;
import com.kivsw.forjoggers.MainActivity;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.SettingsFragment;
import com.kivsw.forjoggers.TrackingService;
import com.kivsw.forjoggers.UnitUtils;
import com.kivsw.forjoggers.helper.GPSLocationListener;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.model.Track;
import com.kivsw.forjoggers.model.TrackSmoother;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.Polyline;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.compass.CompassOverlay;

import java.util.ArrayList;
import java.util.Locale;

//import org.osmdroid.bonuspack.overlays.Polyline;


public class MapFragment extends Fragment
implements SettingsFragment.onSettingsCloseListener,
        CustomPagerView.IonPageAppear, View.OnClickListener,
        MessageDialog.OnCloseListener {

    private OnFragmentInteractionListener mListener;
    private static long savingTime = 0, savedPointIndex = 0;

    private MapView mapView = null;

    private TextView textTrackInfo,textCurrentSpeedInfo;
    ImageView satelliteImageView;
    TextView fileNameTextView;
    Button buttonStart, buttonStop;

    Polyline originalPath = null, smoothyPath = null;

    private View rootView;

    private SettingsKeeper settings = null;
    UnitUtils unitUtils = null;

    private CurrentLocationOverlay myLocationoverlay;
    private MyHandler mHandler;


    public MapFragment() {
        super();

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = SettingsKeeper.getInstance(getActivity());
        unitUtils = new UnitUtils(getActivity());

        // mGPSLocationListener = new MyGPSLocationListener(getActivity());
        mHandler = new MyHandler();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapView) rootView.findViewById(R.id.map);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);
        //mapView.setTileSource(TileSourceFactory.CYCLEMAP);
        OnlineTileSourceBase tileSource = null;
        tileSource = TileSourceFactory.MAPNIK;//CYCLEMAP;//CLOUDMADESMALLTILES;//MAPQUESTAERIAL;//MAPQUESTOSM;//PUBLIC_TRANSPORT;
        mapView.setTileSource(tileSource);
        //mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // My Location Overlay
        myLocationoverlay = new CurrentLocationOverlay(getActivity(), mapView);
        mapView.getOverlays().add(myLocationoverlay);

        // path overlay
        originalPath = new Polyline(getActivity());
        originalPath.setColor(0xFFFF0000);
        originalPath.setWidth(3f);
        mapView.getOverlays().add(originalPath);

        smoothyPath = new Polyline(getActivity());
        smoothyPath.setColor(0x7F7F3030);
        smoothyPath.setWidth(2f);
        mapView.getOverlays().add(smoothyPath);

        CompassOverlay compass = new CompassOverlay(getActivity(), mapView);
        mapView.getOverlays().add(compass);

        IMapController mapController = mapView.getController();
        mapController.setZoom(settings.getZoomLevel());
        GeoPoint c = new GeoPoint(settings.getLastLatitude(), settings.getLastLongitude());
        mapController.setCenter(c);
        mHandler.scheduleRestoringPosition();
        /*if(mGPSLocationListener.getLastKnownLocation()!=null)
           mapController.animateTo(new GeoPoint(mGPSLocationListener.getLastKnownLocation()));*/
        mapView.setMapListener(new MapListener());

        textTrackInfo = (TextView) rootView.findViewById(R.id.textTrackInfo);
        textTrackInfo.setText("");
        textCurrentSpeedInfo = (TextView) rootView.findViewById(R.id.textCurrentSpeedInfo);
        textCurrentSpeedInfo.setText("");

        satelliteImageView = (ImageView) rootView.findViewById(R.id.satelliteImageView);

        fileNameTextView = (TextView) rootView.findViewById(R.id.fileNameTextView);
        updateFileName();

        buttonStart = (Button) rootView.findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(this);

        buttonStop = (Button) rootView.findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(this);
        if (TrackingService.isWorking) {
            buttonStop.setVisibility(View.VISIBLE);
            buttonStart.setVisibility(View.GONE);
        } else {
            buttonStop.setVisibility(View.GONE);
            buttonStart.setVisibility(View.VISIBLE);
        }


        if (savedInstanceState != null) {

        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mGPSLocationListener.releaseInstance();
        // trackSmoother.release();
    }

    @Override
    public void onResume() {
        super.onResume();

        MapFragmentPresenter.getInstance(getActivity()).setTrackingStatus();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        MapFragmentPresenter.getInstance(getActivity()).setUI(this);

    }

    @Override
    public void onStop() {
        MapFragmentPresenter.getInstance(getActivity()).setUI(null);
        super.onStop();
        mHandler.deleteAllMessages();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            ((MainActivity) getActivity()).mapFragment = this;
            if (activity instanceof OnFragmentInteractionListener)
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


    /**
     * check if mylocation is on the screen and correct visible bounds
     */
    void checkMyLocationVisibility() {
        if (!settings.getReturnToMyLocation()) return;
        showMyLocation();
    }


    /**
     * relocates the visible map zone to the current position
     */
    public void showMyLocation() {
        if (myLocationoverlay.getLastFix() == null) return;
        GeoPoint topLeftGpt = (GeoPoint) mapView.getProjection().fromPixels(0, 0);
        GeoPoint bottomRightGpt = (GeoPoint) mapView.getProjection().fromPixels(
                mapView.getWidth(), mapView.getHeight());

        BoundingBoxE6 bounding = mapView.getBoundingBox();

        GeoPoint cl = new GeoPoint(myLocationoverlay.getLastFix());
        boolean r = bounding.contains(cl);
        if (!r) {
            showLocation(cl.getLatitude(), cl.getLongitude());
        }
    }

    /**
     * relocates the visible map zone to (lat,lon) position
     * @param lat
     * @param lon
     */
    public void showLocation(double lat, double lon) {
        IMapController mapController = mapView.getController();
        GeoPoint cl = new GeoPoint(lat, lon);
        mapController.animateTo(cl);
        settings.setZoomLevel(settings.getZoomLevel(), cl.getLatitude(), cl.getLongitude());
    }
    /*public void animateTrack()
    {
        ArrayList<Location> points=currentTrack.getGeoPoints();
        if(points!=null && points.size()>0) {
            showLocation(points.get(0).getLatitude(), points.get(0).getLongitude());
            mHandler.startPointAnimation();
        }
    }*/

    /**
     *
     *
     void setTrack(String gpx)
     {
     currentTrack.fromGPX(gpx);
     updateTrack(true);
     }*/


    /**
     * update the CurrentTrack if it's necessary
     */
    /*void updateTrack(boolean always)
    {
        if(always || currentTrack.getGeoPoints().size() != originalPath.getNumberOfPoints()) {
            putCurrentTrackOnMap();
        }
        updateTrackInfo();
    }*/
    public void updateTrackInfo(TrackSmoother trackSmoother, Track currentTrack ) {
        if(trackSmoother==null)
        {
            textTrackInfo.setText("");
            return;
        }
        StringBuilder str = new StringBuilder();
        ArrayList<Location> points = trackSmoother.getGeoPoints();

        double distance = trackSmoother.getTrackDistance();
        double time = trackSmoother.getTrackTime(false) / 1000.0;

        str.append(getText(R.string.distance));
        str.append(unitUtils.distanceToStr(distance));
        if (BuildConfig.DEBUG) {
            str.append(" (");
            str.append(unitUtils.distanceToStr(currentTrack.getTrackDistance()));
            str.append(")");
        }
        str.append("\n");

        str.append(getText(R.string.time));
        str.append(trackSmoother.getTrackTimeStr(true));
        str.append("\n");

        String energy = getCalloriesStr(trackSmoother);
        if (energy != null) {
            str.append(getText(R.string.energy));
            str.append(energy);
            str.append("\n");
        }

        if (points.size() > 1) {
            str.append(getText(R.string.average_speed));
            str.append(unitUtils.speedToStr(distance / time));
            str.append("\n");

        }

        textTrackInfo.setText(str);
    }

    ;


    String getCalloriesStr(Track trackSmoother) {
        double e = trackSmoother.getСalories(settings.getMyWeightKg());
        String res = null;

        if (e > 1) {
            res = String.format(Locale.US, "%.0f", e) + getText(R.string.Cal);
        }
        return res;
    }

    /**
     * @param available
     */
    public void setGPSstatus(boolean available) {
        if (available) {
            satelliteImageView.setImageResource(R.drawable.satellite_en);
            mHandler.scheduleLastPositionFix(GPSLocationListener.UPDATE_INTERVAL * 5);
        } else satelliteImageView.setImageResource(R.drawable.satellite_dis);
    }

    public void putCurrentTrackOnMap(Track currentTrack) {

        //OverlayManager om=mapView.getOverlayManager();
        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>(currentTrack.getGeoPoints().size());

        for (Location l : currentTrack.getGeoPoints()) {
            points.add(new GeoPoint(l));
        }
        originalPath.setPoints(points);

        mapView.postInvalidate();
    }

    public void putSmoothTrackOnMap(Track trackSmoother)
    {
        int size = trackSmoother!=null?trackSmoother.getGeoPoints().size():0;
        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>(size);
        if(trackSmoother!=null)
        for(Location l:trackSmoother.getGeoPoints()) {
            points.add(new GeoPoint(l));
        }
        smoothyPath.setPoints(points);

        mapView.postInvalidate();
    }

    public boolean loadTrackFromFile(String fileName)
    {
      /*  boolean r=currentTrack.loadGeoPoint(fileName);
        updateTrack(true);
        updateFileName();

        ArrayList<Location> points=currentTrack.getGeoPoints();
        if(points!=null && points.size()>0)
            showLocation(points.get(0).getLatitude(), points.get(0).getLongitude());

        return r;*/
        return false;
    }

    public boolean saveTrackToFile(String fileName)
    {
       /* boolean r= currentTrack.saveGeoPoint(fileName);
        if(r)
            updateFileName();
        return r;*/
        return false;
    }

    /** is invoked when it's necessary to update the tracking service status
     *
     */
   /* public void onStartStopTrackingService(boolean isRunning) {
        if (isRunning) {
            updateTrack(true);
            mHandler.startTrackUpdate(true);
            buttonStart.setVisibility(View.GONE);
            buttonStop.setVisibility(View.VISIBLE);
            updateFileName();
        }
        else
        {
            buttonStop.setVisibility(View.GONE);
            buttonStart.setVisibility(View.VISIBLE);
        }
        getActivity().supportInvalidateOptionsMenu();
    }*/
    private void updateFileName()
    {

       /* String fn= currentTrack.getFileName();
        if(fn!=null && !fn.isEmpty()) {
            File file = new File(fn);
            fn=file.getName();
        }
        if(fn==null || fn.isEmpty())
            fn="*";
        fileNameTextView.setText(fn);*/
    };
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void setCurrentLocation(Location location)
    {
        myLocationoverlay.setLocation(location);//onLocationChanged(location,null);

        if(location.hasSpeed()) {
            StringBuilder str=new StringBuilder();
                str.append(getText(R.string.current_speed));

              /*  Location lastLoc = null;
                if (!currentTrack.getGeoPoints().isEmpty()) {
                    lastLoc = currentTrack.getGeoPoints().get(currentTrack.getGeoPoints().size() - 1);
                    if (!lastLoc.hasSpeed()) lastLoc = null;
                }
                ;
                if (lastLoc == null) {
                    lastLoc = points.get(points.size() - 1);
                }

                str.append(unitUtils.speedToStr(lastLoc.getSpeed()));*/
            str.append(unitUtils.speedToStr(location.getSpeed()));
            textCurrentSpeedInfo.setText(str);
        }
        else
            textCurrentSpeedInfo.setText("");
    };
    //----------------------------------------------
    // SettingsFragment.onSettingsCloseListener
    @Override
    public void onSettingsChanged() {
        //updateTrackInfo();
    }
    //----------------------------------------------
    // CustomPagerView.IonPageAppear
    @Override
    public void onPageAppear() {

    }

    @Override
    public void onPageDisappear() {

    }
    //--------------------------------------------
    // View.OnClickListener
    //----------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.buttonStart: {
                  MapFragmentPresenter.getInstance(getActivity()).onStartClick();
            }
            break;
            case R.id.buttonStop:
                //stopTrackService();
                MapFragmentPresenter.getInstance(getActivity()).onStopClick();
                break;
        }
    }

//----------------------------------------------------------
//-------------------------------
//-------------------------------
public void showStartButton()
{
    buttonStart.setVisibility(View.VISIBLE);
    buttonStop.setVisibility(View.GONE);
}
public void showStopButton()
{
    buttonStop.setVisibility(View.VISIBLE);
    buttonStart.setVisibility(View.GONE);
}
/** shows a message Dialog
 *
 * @param id
 * @param caption
 * @param message
 */
public void showMessageDialog(int id, String caption, String message)
{
    MessageDialog.newInstance(id, caption, message, this)
            .show(getFragmentManager(), "");
}
//---------------------------------
//  MessageDialog.OnCloseListener

    @Override
    public void onClickOk(MessageDialog msg) {
        MapFragmentPresenter.getInstance(getActivity()).onMessageBoxClose(msg.getDlgId(), true);
    }

    @Override
    public void onClickCancel(MessageDialog msg)
    { MapFragmentPresenter.getInstance(getActivity()).onMessageBoxClose(msg.getDlgId(), false); }

    @Override
    public void onClickExtra(MessageDialog msg)
    { MapFragmentPresenter.getInstance(getActivity()).onMessageBoxClose(msg.getDlgId(), false); }
    //-----------------------------------------------------------




    //----------------------------------------------------------
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
/*    //----------------------------------------
    class MyGPSLocationListener extends GPSLocationListener
            implements IMyLocationProvider
    {

        MyGPSLocationListener(Context context)
        {super(context,false&&BuildConfig.DEBUG);}

        //----------------------------------------
        @Override
        public void onLocationChanged(Location loc)
        {
            super.onLocationChanged(loc);
            if(consumer!=null) {
                consumer.onLocationChanged(loc, this);

                mHandler.scheduleIfNecessaryRestoringPosition();
                if(loc.getProvider().equals(LocationManager.GPS_PROVIDER)) {
                    setGPSstatus(true);

                }
            }
        }
        @Override
        public void onStatusChanged(String provider,int status, Bundle extras) {
            super.onStatusChanged(provider, status, extras);

            if(!provider.equals(LocationManager.GPS_PROVIDER)) return;

            switch(status) {
                case     LocationProvider.TEMPORARILY_UNAVAILABLE:
                case     LocationProvider.OUT_OF_SERVICE:
                    setGPSstatus(false);
                    break;
                case   LocationProvider.AVAILABLE:
                    //setGPSstatus(1);
                    break;
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
    //----------------------------------------*/

    class MyHandler extends Handler
    {
        final private int RESTORE_POSITION=1, POINT_ANIMATION=2, UPDATE_TRACK=3, LAST_LOCATION_FIX_TIMEOUT =4;
        MyHandler()
        {
            super();
        }

        public void deleteAllMessages()
        {
            removeMessages(RESTORE_POSITION);
            removeMessages(POINT_ANIMATION);
            //removeAnimation();
            removeMessages(UPDATE_TRACK);
            removeMessages(LAST_LOCATION_FIX_TIMEOUT);

          /*  if(marker!=null)
               mapView.getOverlays().remove(marker);
            marker=null;*/

        }

        public void scheduleIfNecessaryRestoringPosition()
        {
            if(!hasMessages(RESTORE_POSITION))
                sendEmptyMessageDelayed(RESTORE_POSITION, 5000);
        }
        public void scheduleRestoringPosition()
        {
            removeMessages(RESTORE_POSITION);
            sendEmptyMessageDelayed(RESTORE_POSITION, 5000);
        }
        public void scheduleLastPositionFix(int timeout)
        {
            removeMessages(LAST_LOCATION_FIX_TIMEOUT);
            sendEmptyMessageDelayed(LAST_LOCATION_FIX_TIMEOUT, timeout);
        }

      /*  private int pointIndex=0;
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
               removeAnimation();
            }
        }
        void removeAnimation()
        {
            if(marker==null) return;
            mapView.getOverlays().remove(marker);
            marker=null;
            mapView.invalidate();
            pointIndex=0;
        }*/

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
               /* case POINT_ANIMATION:
                     //nextPointAnimation();
                     break;*/
               /* case UPDATE_TRACK:
                    updateTrack(false);
                    startTrackUpdate(TrackingService.isWorking);
                    break;*/
                case LAST_LOCATION_FIX_TIMEOUT:
                    setGPSstatus(false);
                    break;
            }

        }

    }


}
