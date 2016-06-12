package com.kivsw.forjoggers.ui.gps_status;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.GpsSatellite;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.CompassHelper;
import com.kivsw.forjoggers.helper.SettingsKeeper;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.CombinedXYChart.XYCombinedChartDef;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.ScatterChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
*  This class shows GPS's satellites
 */
public class GpsStatusFragment extends DialogFragment
implements  GpsStatusContract.IView,
        View.OnClickListener
{

    TextView statusText, gpsText, glonassText, otherText;
    CheckBox compassCheckBox;
    FrameLayout chartLayout;
    LinearLayout rootLayout;
    MyHandler mHandler;

    SettingsKeeper settings;

    GpsStatusContract.IPresenter presenter;

    public GpsStatusFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment
     *
     * @return A new instance of fragment GpsStatusFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GpsStatusFragment newInstance() {
        GpsStatusFragment fragment = new GpsStatusFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        presenter = GpsStatusPresenter.getInstance(getContext());
        settings  = SettingsKeeper.getInstance(getContext());
        mHandler = new MyHandler(this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_gps_status, container, false);

        chartLayout = (FrameLayout) rootView.findViewById(R.id.chartLayout);
        rootLayout = (LinearLayout) rootView.findViewById(R.id.rootLayout);

        statusText = (TextView)rootView.findViewById(R.id.statusText);
        statusText.setText(" \n ");

        gpsText = (TextView)rootView.findViewById(R.id.gpsText);
        gpsText.setTextColor(gpsColor);
        gpsText.setVisibility(View.INVISIBLE);
        glonassText = (TextView)rootView.findViewById(R.id.glonassText);
        glonassText.setTextColor(glonassColor);
        glonassText.setVisibility(View.INVISIBLE);
        otherText = (TextView)rootView.findViewById(R.id.otherText);
        otherText.setTextColor(otherColor);
        otherText.setVisibility(View.INVISIBLE);

        compassCheckBox = (CheckBox)rootView.findViewById(R.id.compassCheckBox);
        compassCheckBox.setChecked(settings.getUseCompass());
        compassCheckBox.setVisibility(View.INVISIBLE);
        compassCheckBox.setOnClickListener(this);

        initPseudoRadarChart();


        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.setUI(this);

    }

    @Override
    public void onStop() {
        presenter.setUI(null);
        super.onStop();
        mHandler.deleteAllMessages();
    }

    XYSeries radialGrid1, radialGrid2, radialGridX, radialGridY;

    XYSeries gpsSeries,gpsInUseSeries,
            glonassSeries,glonassInUseSeries,
            otherSeries,otherInUseSeries,
            annotationSeries;

    XYMultipleSeriesRenderer multiRenderer;
    XYMultipleSeriesDataset dataset;

    GraphicalView mChart;

    double ampl=1.0;
    Location lastLoc=null;

    private void initPseudoRadarChart()
    {
        //mRenderer.setPointStrokeWidth(3);

        // creates the chart
        dataset=new XYMultipleSeriesDataset();

        multiRenderer=new XYMultipleSeriesRenderer();

        multiRenderer.setLegendTextSize(dpToPx(12));
        multiRenderer.setFitLegend(true);
        //multiRenderer.setLegendHeight(dpToPx(48));
        multiRenderer.setShowLegend(false);

        int ps=dpToPx(10);
        multiRenderer.setPointSize(ps);

        multiRenderer.setApplyBackgroundColor(true);
        multiRenderer.setMargins(new int[]{ps,ps,ps,ps});
        multiRenderer.setBackgroundColor(0x00FFFFFF);
        multiRenderer.setMarginsColor(0x00FFFFFF);

        multiRenderer.setShowGrid(false);
        multiRenderer.setShowLabels(false);
        multiRenderer.setXAxisColor(0x00FFFFFF);
        multiRenderer.setYAxisColor(0x00FFFFFF);
        multiRenderer.setGridColor(Color.GRAY);


        multiRenderer.setZoomButtonsVisible(false);
        //multiRenderer.setZoomEnabled(false);
        multiRenderer.setZoomEnabled(false,false);
        multiRenderer.setPanEnabled(false);
        multiRenderer.setShowGrid(false);


        // calculates chart view size
        Point p=getDislaySize();
        int s;
        if(p.x < p.y) {
            //rootLayout.setOrientation(LinearLayout.VERTICAL);
            s = p.x*9/10;
        }
        else {
            //rootLayout.setOrientation(LinearLayout.HORIZONTAL);
            s = p.y*9/10-dpToPx(80);//multiRenderer.getLegendHeight()*3;
        }
        ampl=s/2-ps; // make the scale approximitale 1:1

        // creates series
        int gridIndex[]=addRadialGrid();
        int seriesIndex[]=addGPSSeries();

        XYCombinedChartDef[] types = new XYCombinedChartDef[] {
                new XYCombinedChartDef(LineChart.TYPE, gridIndex),
                new XYCombinedChartDef(ScatterChart.TYPE, seriesIndex)};

        mChart = ChartFactory.getCombinedXYChartView(getActivity(),dataset , multiRenderer,types);
//         mChart = ChartFactory.getLineChartView(getActivity(),dataset , multiRenderer);

        mChart.setPivotX(s/2);
        mChart.setPivotY(s/2);

        // add the chart to the layout
        chartLayout.getLayoutParams().height=s;//+multiRenderer.getLegendHeight();
        chartLayout.getLayoutParams().width=s;
        chartLayout.setLayoutParams(chartLayout.getLayoutParams());

        chartLayout.addView(mChart, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


    };

    private int[] addRadialGrid()
    {
        XYSeriesRenderer mRenderer;
        int[] indexes=new int[4];
        int k=0;

        mRenderer =new XYSeriesRenderer();
        mRenderer.setLineWidth(dpToPx(2));
        mRenderer.setColor(0x60888888);
        mRenderer.setDisplayChartValues(false);
        mRenderer.setShowLegendItem(false);
        mRenderer.setAnnotationsColor(0x800000FF);
        int fntSz=dpToPx(20);
        mRenderer.setAnnotationsTextSize(fntSz);

        // Include low and max value
        mRenderer.setDisplayBoundingPoints(true);
        // we add point markers
        mRenderer.setPointStyle(PointStyle.POINT);

        radialGrid1=new XYSeries("");
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(radialGrid1);
        multiRenderer.addSeriesRenderer(mRenderer);

        radialGrid2=new XYSeries("");
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(radialGrid2);
        multiRenderer.addSeriesRenderer(mRenderer);


        radialGridX=new XYSeries("");
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(radialGridX);
        multiRenderer.addSeriesRenderer(mRenderer);

        radialGridY=new XYSeries("");
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(radialGridY);
        multiRenderer.addSeriesRenderer(mRenderer);


        // adds curcles
        //for(double a=-Math.PI/2;a<=Math.PI/2;a+= Math.PI/180)
        for(int i=-90;i<=90;i++)
        {
            double a=i*(Math.PI/180);
            double x=ampl*Math.sin(a),
                   y=ampl*Math.cos(a);
            radialGrid1.add(x,y);
            radialGrid2.add(x,-y);
        };

        // adds x y
        radialGridX.add(-ampl,0); radialGridX.add(ampl,0);
        radialGridY.add(0,-ampl); radialGridY.add(0,ampl);

        // direction's  labels
        radialGridX.addAnnotation(getString(R.string.west), -ampl+fntSz/3, 0-fntSz/3);
        radialGridX.addAnnotation(getString(R.string.east), ampl-fntSz/3 , 0-fntSz/3);
        radialGridX.addAnnotation(getString(R.string.north),  0,           ampl-fntSz*2/3);
        radialGridX.addAnnotation(getString(R.string.south),  0,           -ampl);

        return indexes;
    }

    int gpsColor=0xFF00BB00, glonassColor=Color.RED, otherColor=0xFFAAAA00;
    private int[] addGPSSeries()
    {
        XYSeriesRenderer mRenderer;
        int[] indexes=new int[7];
        int k=0;

        // GPS
        mRenderer =createRendered(gpsColor, false);
        gpsSeries=createXYSeries(getText(R.string.gps).toString());
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(gpsSeries);
        multiRenderer.addSeriesRenderer(mRenderer);

        mRenderer =createRendered(gpsColor, true);
        gpsInUseSeries=createXYSeries(getText(R.string.gps).toString());
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(gpsInUseSeries);
        multiRenderer.addSeriesRenderer(mRenderer);

        // glonass
        mRenderer =createRendered(glonassColor, false);
        glonassSeries=createXYSeries(getText(R.string.glonass).toString());
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(glonassSeries);
        multiRenderer.addSeriesRenderer(mRenderer);

        mRenderer =createRendered(glonassColor, true);
        glonassInUseSeries=createXYSeries(getText(R.string.glonass).toString());
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(glonassInUseSeries);
        multiRenderer.addSeriesRenderer(mRenderer);


        //other
        mRenderer =createRendered(otherColor, false);
        otherSeries=createXYSeries(getText(R.string.others).toString());
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(otherSeries);
        multiRenderer.addSeriesRenderer(mRenderer);

        mRenderer =createRendered(otherColor, true);
        otherInUseSeries=createXYSeries(getText(R.string.others).toString());
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(otherInUseSeries);
        multiRenderer.addSeriesRenderer(mRenderer);

        // for annotations
        mRenderer =createRendered(0xFFFFFFFF, true);
        mRenderer.setPointStyle(PointStyle.POINT);
        annotationSeries=createXYSeries("xxx");
        indexes[k++]=dataset.getSeriesCount();
        dataset.addSeries(annotationSeries);
        multiRenderer.addSeriesRenderer(mRenderer);

        return indexes;

    };
    @Override
    public void onClick(View v) {
      switch (v.getId())
      {
          case R.id.compassCheckBox:
              settings.setUseCompass(compassCheckBox.isChecked());
              if(compassCheckBox.isChecked()==false)
                  mHandler.setAngleTo(0);
                  //animateChartRotation(0);
              else
                  mHandler.setAngleTo(lastAzimuth);
                  //animateChartRotation(lastAzimuth);
              break;
      }
    };

    XYSeriesRenderer createRendered(int color, boolean inFix)
    {
        XYSeriesRenderer mRenderer =new XYSeriesRenderer();
        mRenderer.setLineWidth(0);
        mRenderer.setColor(color);
        mRenderer.setDisplayChartValues(false);
        mRenderer.setDisplayBoundingPoints(true);
        mRenderer.setPointStyle(PointStyle.CIRCLE);

        mRenderer.setFillPoints(inFix);
        mRenderer.setShowLegendItem(!inFix);

        mRenderer.setPointStrokeWidth(dpToPx(2));
        mRenderer.setAnnotationsColor(0xFF000000);
        mRenderer.setAnnotationsTextSize(dpToPx(10));
        return mRenderer;
    }
    XYSeries createXYSeries(String name)
    {
        return new XYSeries("    "+name+"  ");
    }
    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int px = (int)((dp * displayMetrics.density) + 0.5);
        return px;
    }

    Point getDislaySize() {
        // gets the picture position
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);
        return displaySize;

    }


    @Override
    public void clearSatellites()
    {
        gpsSeries.clear();
        gpsInUseSeries.clear();
        glonassSeries.clear();
        glonassInUseSeries.clear();
        otherSeries.clear();
        otherInUseSeries.clear();
        annotationSeries.clear();

        annotationSeries.add(0,0); // add a fake point to force this Series to draw annotations

        lastLoc=null;
    }
    @Override
    public void invalidateSatilletes() {
        mChart.invalidate();

        if(lastLoc!=null) {
            String f="%.6f";
            statusText.setText(getText(R.string.latitude) + String.format(f,lastLoc.getLatitude())+"\n"
                    + getText(R.string.longitude) + String.format(f,lastLoc.getLongitude()));
        }
        else
            statusText.setText(getText(R.string.latitude) + "\n"
                    + getText(R.string.longitude));
    }

    @Override
    public void addSatellites(ArrayList<GpsSatellite> satellites, int id) {
        XYSeries ser=null, serInUse=null;
        TextView legendText=null;

        switch(id)
        {
            case GpsStatusContract.GPS:
                ser=gpsSeries;
                serInUse=gpsInUseSeries;
                legendText=gpsText;
                break;
            case GpsStatusContract.GLONASS:
                ser=glonassSeries;
                serInUse=glonassInUseSeries;
                legendText=glonassText;
                break;
            case GpsStatusContract.OTHER:
                ser=otherSeries;
                serInUse=otherInUseSeries;
                legendText=otherText;
                break;
            default:
                return;
        }

        ser.clear();
        serInUse.clear();


        int fontH=dpToPx(10);

        for(GpsSatellite sat:satellites)
        {
            double x,y;
            double el=(90-sat.getElevation())/90.0 * ampl;
            double az=sat.getAzimuth()/180*Math.PI;
            x=el*Math.sin(az);
            y=el*Math.cos(az);

            ser.add(x,y);
            if(sat.usedInFix())
               serInUse.add(x,y);

            annotationSeries.addAnnotation(String.valueOf(sat.getPrn()),x,y-fontH*1/3);

        }
        if(satellites.size()>0)
            legendText.setVisibility(View.VISIBLE);
        else
            legendText.setVisibility(View.GONE);

    }
    ObjectAnimator animator=null;
    private void animateChartRotation(float angle)
    {
        float currentRotation=mChart.getRotation();
            if(currentRotation>angle)
            {
                while((currentRotation-angle)>180)
                    angle+=360;
            }
            else
            {
                while((angle-currentRotation)>180)
                    angle-=360;
            }

        if(animator==null)
        {
            animator = ObjectAnimator.ofFloat(mChart, "rotation", currentRotation, angle);// new ObjectAnimator();
            animator.setDuration(800);
           // animator.setTarget(mChart);

        }
        else
        if(animator.isStarted())
            animator.cancel();


       animator.setFloatValues(currentRotation, angle);

       animator.start();

    }

    @Override
    public void setLocation(Location loc)
    {
        lastLoc=loc;
    }

    float lastAzimuth=0;
    @Override
    public void setMagneticAzimuth(float angle) {
        lastAzimuth=angle;

        compassCheckBox.setVisibility(View.VISIBLE);
        if(!compassCheckBox.isChecked()) return;

        if(animator!=null && animator.isStarted()) return;

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int dspRotation = display.getRotation()*90;
        angle = angle - dspRotation;

        mHandler.setAngleTo(angle);
        //mChart.setRotation(angle);

    }
    //------------------------------------------------------
    static class MyHandler extends Handler {
        final private int UPDATE_AZIMUTH = 1;
        final private float speed=180.0f/2000; // 180 degree per second
        double maxStep=1000;
        WeakReference<GpsStatusFragment> fragment_ref;
        double targetAngel = 0;
        long lastTime=SystemClock.elapsedRealtime();

        MyHandler(GpsStatusFragment f) {
            super();
            this.fragment_ref=new WeakReference<>(f);
        }

        public void setAngleTo(double angle)
        {
            targetAngel=angle;
            if(!hasMessages(UPDATE_AZIMUTH)) {
                scheduleUpdateAzimith();
                lastTime=SystemClock.elapsedRealtime();
            }
        }
        protected void scheduleUpdateAzimith() {
            removeMessages(UPDATE_AZIMUTH);
            int dt=1000/50;
            sendEmptyMessageDelayed(UPDATE_AZIMUTH, dt );
            maxStep = speed*dt*3;
        }

        public void deleteAllMessages() {
            removeMessages(UPDATE_AZIMUTH);
        }


        void doRotateAnim(GpsStatusFragment fragment)
        {
            long currentTime=SystemClock.elapsedRealtime();
            double currentRot=fragment.mChart.getRotation();
            double target= CompassHelper.correctAngle(currentRot,targetAngel);
            long dt = currentTime-lastTime;
            lastTime=currentTime;
            double step = dt*speed;
            if(step > maxStep) step = maxStep;
            double resRot;
            if(currentRot<target)
            {
                resRot = currentRot+step;
                if(resRot<target)
                {
                    fragment.mChart.setRotation((float)resRot);
                    scheduleUpdateAzimith();
                }
                else
                    fragment.mChart.setRotation((float)target); // the target angle has been achieved
            }
            else
            {
                resRot = currentRot-step;
                if(resRot>target)
                {
                    fragment.mChart.setRotation((float)resRot);
                    scheduleUpdateAzimith();
                }
                else
                    fragment.mChart.setRotation((float)target);// the target angle has been achieved
            }
        };
        public void handleMessage(Message msg) {
            GpsStatusFragment fragment=fragment_ref.get();
            if(fragment==null) return;

            switch (msg.what) {
                case UPDATE_AZIMUTH:
                    doRotateAnim(fragment);
                    break;
            }

        }
    }



}
