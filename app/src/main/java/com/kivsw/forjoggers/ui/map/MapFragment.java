package com.kivsw.forjoggers.ui.map;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.kivsw.dialog.MessageDialog;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.helper.UnitUtils;
import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.model.track.TrackSmoother;
import com.kivsw.forjoggers.ui.CustomPagerView;
import com.kivsw.forjoggers.ui.gps_status.GpsStatusFragment;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

//import org.osmdroid.bonuspack.overlays.Polyline;



public class MapFragment
        extends Fragment
        implements MapFragmentContract.IView,
            CustomPagerView.IonPageAppear, View.OnClickListener,
            MessageDialog.OnCloseListener
{
    private MapViewEnvelope mapView = null;

    private TextView textTrackInfo,textCurrentSpeedInfo;
    ImageView satelliteImageView, activityImageView;
    Boolean isGpsAvailable=null;

    TextView fileNameTextView;
    Button buttonStart, buttonStop;

    FloatingActionButton myLocationButton;

    Polyline originalPath = null, smoothyPath = null;

    private View rootView;

    private SettingsKeeper settings = null;

    UnitUtils unitUtils = null;

    private CurrentLocationOverlay myLocationoverlay;
    private CompassOverlay mCompassOverlay;
    private ScaleBarOverlay mScaleBarOverlay;
    MapFragmentContract.IPresenter presenter;


    public MapFragment() {
        super();

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = SettingsKeeper.getInstance(getActivity());
        unitUtils = new UnitUtils(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = (MapViewEnvelope) rootView.findViewById(R.id.map);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);
        //mapView.setTileSource(TileSourceFactory.CYCLEMAP);
        OnlineTileSourceBase tileSource = null;
        tileSource = TileSourceFactory.MAPNIK;//CYCLEMAP;//CLOUDMADESMALLTILES;//MAPQUESTAERIAL;//MAPQUESTOSM;//PUBLIC_TRANSPORT;

        Configuration.getInstance().setUserAgentValue(getContext().getPackageName());

        /*float scale = getContext().getResources().getDisplayMetrics().density/1.5f;
        if(scale <1) scale=1;

        final int newScale = (int) (256 * scale + 0.5);
        tileSource=new XYTileSource("Mapnik",
                0, 19, newScale, ".png", new String[] {
                "https://a.tile.openstreetmap.org/",
                "https://b.tile.openstreetmap.org/",
                "https://c.tile.openstreetmap.org/" },"© OpenStreetMap contributors",
                new TileSourcePolicy(2,
                        TileSourcePolicy.FLAG_NO_BULK
                                | TileSourcePolicy.FLAG_NO_PREVENTIVE
                                | TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL
                                | TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
                ));//*/
        mapView.setTileSource(tileSource);
        //mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);


        // My Location Overlay
        myLocationoverlay = new CurrentLocationOverlay(getActivity(), mapView);
        mapView.getOverlays().add(myLocationoverlay);

        // Compass overlay
        mCompassOverlay = new CompassOverlay(getActivity(), new InternalCompassOrientationProvider(getActivity()), mapView);
        mCompassOverlay.enableCompass();
        mapView.getOverlays().add(mCompassOverlay);

        // map scale bar
        mScaleBarOverlay = new CurrentScaleBarOverlay(mapView,unitUtils);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setAlignBottom(true);
        mapView.getOverlays().add(this.mScaleBarOverlay);

        // path overlay
        originalPath = new Polyline(mapView);
        originalPath.setColor(0xFFFF0000);
        originalPath.setWidth(3f);
        mapView.getOverlays().add(originalPath);

        smoothyPath = new Polyline(mapView);
        smoothyPath.setColor(0x7F7F3030);
        smoothyPath.setWidth(2f);
        mapView.getOverlays().add(smoothyPath);

        CompassOverlay compass = new CompassOverlay(getActivity(), mapView);
        mapView.getOverlays().add(compass);

        IMapController mapController = mapView.getController();
        mapController.setZoom(settings.getZoomLevel());
        GeoPoint c = new GeoPoint(settings.getLastLatitude(), settings.getLastLongitude());
        mapController.setCenter(c);

        mapView.setMapListener(new MapListener());

        textTrackInfo = (TextView) rootView.findViewById(R.id.textTrackInfo);
        textTrackInfo.setText("");
        textCurrentSpeedInfo = (TextView) rootView.findViewById(R.id.textCurrentSpeedInfo);
        textCurrentSpeedInfo.setText("");

        satelliteImageView = (ImageView) rootView.findViewById(R.id.satelliteImageView);
        satelliteImageView.setVisibility(View.INVISIBLE);
        satelliteImageView.setOnClickListener(this);

        activityImageView = (ImageView) rootView.findViewById(R.id.activityImageView);

        fileNameTextView = (TextView) rootView.findViewById(R.id.fileNameTextView);

        buttonStart = (Button) rootView.findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(this);
        buttonStart.setVisibility(View.GONE);

        buttonStop = (Button) rootView.findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(this);
        buttonStop.setVisibility(View.GONE);

        myLocationButton = (FloatingActionButton) rootView.findViewById(R.id.myLocationButton);
        myLocationButton.setOnClickListener(this);
        //myLocationButton.setRippleColor(0x90FFFFFF);
        myLocationButton.setBackgroundTintList(ColorStateList.valueOf(0x90FFFFFF));

        if (savedInstanceState != null) {

        }

        presenter = MapFragmentPresenter.getInstance(getActivity());

        if(settings.getReturnToMyLocation())
            startFollowingMyLocation();
        else
            stopFollowingMyLocation();

        onSettingsChanged();

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

        presenter.updateTrackingStatus();
        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.setUI(this);

        if(rootView.getWidth()>0)
            initLayoutPositions();
        else {
            ViewTreeObserver vto = rootView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    // remove the listener so it won't get called again if the view layout changes
                    if (Build.VERSION.SDK_INT >= 16)
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    else
                        rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                    initLayoutPositions();

                }
            });

        }

    }
    private void initLayoutPositions()
    {
        final float compassRadius=35;
        final float Xcenter = unitUtils.pxToDp(satelliteImageView.getLeft()) - compassRadius;
        final float Ycenter = unitUtils.pxToDp(satelliteImageView.getHeight()/2+satelliteImageView.getTop());

        mCompassOverlay.setCompassCenter(Xcenter, Ycenter);
    };


    @Override
    public void onStop() {
        presenter.setUI(null);
        super.onStop();
        //mHandler.deleteAllMessages();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    /**
     * check if mylocation is on the screen and correct visible bounds
     */
    void checkMyLocationVisibility() {
        if (settings.getReturnToMyLocation() || presenter.isTracking())
            showMyLocation();
    }


    /**
     * start mode following
     */
    @Override
    public void startFollowingMyLocation()
    {
        //timeOfStartFollowingMyLocation = SystemClock.elapsedRealtime();
        settings.setReturnToMyLocation(true);
        myLocationButton.setImageResource(R.drawable.center_direction_colour);
        showMyLocation();

    }
    @Override
    public void stopFollowingMyLocation()
    {
        settings.setReturnToMyLocation(false);
        myLocationButton.setImageResource(R.drawable.center_direction_black);
    }
    /**
     * relocates the visible map zone to the current position
     */
    public void showMyLocation() {
        if (myLocationoverlay.getLastFix() == null) return;

        boolean r = isMyLocationVisible(); // whether the current location is visible
        if (!r) {
            GeoPoint cl = new GeoPoint(myLocationoverlay.getLastFix());
            showLocation(cl.getLatitude(), cl.getLongitude());
        }
    }

    boolean isMyLocationVisible()
    {
        if (myLocationoverlay.getLastFix() == null) return false;

        GeoPoint topLeftGpt = (GeoPoint) mapView.getProjection().fromPixels(0, 0);
        GeoPoint bottomRightGpt = (GeoPoint) mapView.getProjection().fromPixels(
                mapView.getWidth(), mapView.getHeight());

        BoundingBox bounding = mapView.getBoundingBox();

        GeoPoint cl = new GeoPoint(myLocationoverlay.getLastFix());
        boolean r = bounding.contains(cl); // whether the current location is visible
        return r;
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

    /**
     * shows the current track's information
     * @param trackSmoother
     * @param currentTrack
     */
    @Override
    public void updateTrackInfo(TrackSmoother trackSmoother, Track currentTrack ) {

        switch(currentTrack.getActivityType()) {
            case SettingsKeeper.HIKING:
                activityImageView.setImageResource(R.drawable.walking_c);
                break;
            case SettingsKeeper.JOGGING:
                activityImageView.setImageResource(R.drawable.jogging_c);
                break;
            case SettingsKeeper.BICYCLING:
                activityImageView.setImageResource(R.drawable.bycicling_c);
                break;
        }

        if((trackSmoother==null) ||
           (!presenter.isTracking() && trackSmoother.getGeoPoints().size()<2)    )
        {
            textTrackInfo.setText("");
            return;
        }

        StringBuilder str = new StringBuilder();
        ArrayList<Location> points = trackSmoother.getGeoPoints();

        double distance = trackSmoother.getTrackDistance();
        double time = trackSmoother.getTrackPointsTime() / 1000.0;

        str.append(getText(R.string.distance));
        str.append(unitUtils.distanceToStr(distance));
        /*if (BuildConfig.DEBUG) {
            str.append(" (");
            str.append(unitUtils.distanceToStr(currentTrack.getTrackDistance()));
            str.append(")");
        }*/
        str.append("\n");

        str.append(getText(R.string.time));
        str.append(currentTrack.getTrackTimeStr());
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
        double e = trackSmoother.getСalories(settings.getMyWeight().getWeightKg());
        String res = null;

        if (e > 1) {
            res = String.format(Locale.US, "%.0f", e) + getText(R.string.Cal);
        }
        return res;
    }

    /**
     * @param available
     */
    @Override
    public void setGPSstatus(boolean available) {
        if((isGpsAvailable!=null) && (available == isGpsAvailable.booleanValue()))
            return;

        satelliteImageView.setVisibility(View.VISIBLE);

        if (available) {
            satelliteImageView.setImageResource(R.drawable.gps_receiving);
            isGpsAvailable = Boolean.TRUE;
        } else{
            satelliteImageView.setImageResource(R.drawable.gps_disconnected);
            textCurrentSpeedInfo.setText("");
            isGpsAvailable = Boolean.FALSE;
        }


    }

    public void putCurrentTrackOnMap(Track currentTrack) {
        int size = currentTrack!=null?currentTrack.getGeoPoints().size():0;

        ArrayList<GeoPoint> points = new ArrayList<GeoPoint>(size);

        if(currentTrack!=null)
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


    /** is invoked when it's necessary to update the tracking service status
     *
     */

    public void updateFileName()
    {

        String fn= presenter.getCurrentTrack().getFileName();
        if(fn!=null && !fn.isEmpty()) {
            File file = new File(fn);
            fn=file.getName();
        }
        if(fn==null || fn.isEmpty())
            fn="*";
        fileNameTextView.setText(fn);
    };
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    public void setCurrentLocation(Location location)
    {
        myLocationoverlay.setLocation(location);//onLocationChanged(location,null);

        if(settings.getReturnToMyLocation())
            checkMyLocationVisibility();

        String resStr="";
        if(isGpsAvailable!=null && isGpsAvailable.booleanValue()) {
            if (location.hasSpeed()) {
                resStr = unitUtils.speedToStr(location.getSpeed());
            } else if ( presenter.isTracking() &&
                        presenter.getTrackSmoother() != null &&
                        presenter.getTrackSmoother().getGeoPoints() != null &&
                        presenter.getTrackSmoother().getGeoPoints().size() > 1)
            {
                ArrayList<Location> points = presenter.getTrackSmoother().getGeoPoints();
                Location l = points.get(points.size() - 1);
                if (l.hasSpeed())
                    resStr = "~" + unitUtils.speedToStr(l.getSpeed());
            }
        }

        textCurrentSpeedInfo.setText(resStr);

    };
    void showGpsStatus()
    {
        FragmentManager fm=getFragmentManager();
        GpsStatusFragment fragment=(GpsStatusFragment)fm.findFragmentByTag("gpsStatus");
        if(fragment==null)
             fragment=GpsStatusFragment.newInstance();
        if(fragment.isAdded()) return;

        fragment.show(fm,"gpsStatus");

    };
    //----------------------------------------------

    public void onSettingsChanged() {
        updateTrackInfo(presenter.getTrackSmoother(), presenter.getCurrentTrack());

        switch(settings.getDistanceUnit())
        {
            case SettingsKeeper.METERS:
            case SettingsKeeper.KILOMETERS:
                mScaleBarOverlay.setUnitsOfMeasure(ScaleBarOverlay.UnitsOfMeasure.metric);
                break;
            case SettingsKeeper.MILES:
                mScaleBarOverlay.setUnitsOfMeasure(ScaleBarOverlay.UnitsOfMeasure.imperial);
                break;
        }
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
                presenter.onStartClick();
            }
            break;
            case R.id.buttonStop:
                presenter.onStopClick();
                break;

            case R.id.myLocationButton:
                startFollowingMyLocation();
                break;
            case R.id.satelliteImageView:
                showGpsStatus();
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
        presenter.onMessageBoxClose(msg.getDlgId(), true);
    }

    @Override
    public void onClickCancel(MessageDialog msg)
    { presenter.onMessageBoxClose(msg.getDlgId(), false); }

    @Override
    public void onClickExtra(MessageDialog msg)
    { presenter.onMessageBoxClose(msg.getDlgId(), false); }
    //-----------------------------------------------------------



    //------------------------------------------
    class MapListener implements org.osmdroid.events.MapListener
    {

        @Override
        public boolean onScroll(ScrollEvent event) {

            long T=SystemClock.elapsedRealtime();
            if(mapView.isTouching() &&  !isMyLocationVisible())// if startFollowingMyLocation() has been invoked recently
                 stopFollowingMyLocation();
           // mHandler.scheduleRestoringPosition();
            GeoPoint c=mapView.getBoundingBox().getCenter();
            settings.setZoomLevel(settings.getZoomLevel(), c.getLatitude(), c.getLongitude());
            return false;
        }

        @Override
        public boolean onZoom(ZoomEvent event) {
            int zl=(int)Math.round(event.getZoomLevel());
            GeoPoint c=mapView.getBoundingBox().getCenter();
            settings.setZoomLevel(zl, c.getLatitude(), c.getLongitude());
            return false;
        }
    }

}
