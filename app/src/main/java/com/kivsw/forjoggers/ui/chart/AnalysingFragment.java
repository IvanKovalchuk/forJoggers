package com.kivsw.forjoggers.ui.chart;


import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.kivsw.forjoggers.BuildConfig;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.UnitUtils;
import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.ui.CustomPagerView;
import com.kivsw.forjoggers.ui.MainActivity;

import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DecimalFormat;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnalysingFragment extends Fragment
        implements AnalysingFragmentContract.IView,
             AdapterView.OnItemSelectedListener,
             CustomPagerView.IonPageAppear
{

    LinearLayout layout;
    GraphicalView mChart;
    XYSeries lineData;
    XYSeriesRenderer mRenderer;
    XYMultipleSeriesRenderer multiRenderer;

    //MyLabelFormatter myFormatter;
    UnitUtils unitUtils;
    //LineGraphSeries<DataPoint> series;

    Spinner graphSpiner;
    ArrayAdapter<CharSequence> spinnerAdpter;
    AnalysingFragmentPresenter presenter;
    boolean isVisible=false;
    boolean needUpdate=true;


    public AnalysingFragment() {
        super();

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_analysing, container, false);

        layout =(LinearLayout)rootView.findViewById(R.id.layout);
        initChart();

        graphSpiner=(Spinner)rootView.findViewById(R.id.spinner);

        spinnerAdpter=ArrayAdapter.createFromResource(getActivity(),
                R.array.graphs, android.R.layout.simple_spinner_item);
        spinnerAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphSpiner.setAdapter(spinnerAdpter);
        graphSpiner.setOnItemSelectedListener(this);
        graphSpiner.setSelection(2);

        if(BuildConfig.DEBUG) graphSpiner.setVisibility(View.VISIBLE);
        else  graphSpiner.setVisibility(View.GONE);

        presenter = AnalysingFragmentPresenter.getInstance(getActivity());

        return rootView;
    }

    void initChart()
    {
        //http://stackoverflow.com/questions/9904457/how-to-set-value-in-x-axis-label-in-achartengine
        //http://stackoverflow.com/questions/12959969/achartengine-zoom-and-black-border
        lineData=new XYSeries("");
        mRenderer =new XYSeriesRenderer();
        mRenderer.setLineWidth(3);
        mRenderer.setColor(Color.BLUE);
// Include low and max value
        mRenderer.setDisplayBoundingPoints(true);
// we add point markers
        mRenderer.setPointStyle(PointStyle.POINT);
        //mRenderer.setPointStrokeWidth(3);


        // creates the chart
        XYMultipleSeriesDataset dataset=new XYMultipleSeriesDataset();
        dataset.addSeries(lineData);
        multiRenderer=new XYMultipleSeriesRenderer();

        multiRenderer.setAxisTitleTextSize(20);
        multiRenderer.setChartTitleTextSize(20);
        multiRenderer.setLabelsTextSize(20);
        multiRenderer.setLegendTextSize(24);
        multiRenderer.setPointSize(5f);

        multiRenderer.setApplyBackgroundColor(true);
        multiRenderer.setBackgroundColor(Color.parseColor("#F5F5F5"));
        multiRenderer.setMarginsColor(Color.parseColor("#F5F5F5"));

        //multiRenderer.setChartTitle("Weight / Temperature");
   //     multiRenderer.setXLabels(20);
       // multiRenderer.setXTitle(sdFormatter.format(currentDate));

        multiRenderer.setXLabelsAlign(Paint.Align.CENTER);

        //multiRenderer.setYLabels(10);


        multiRenderer.setAxesColor(Color.LTGRAY);
        multiRenderer.setLabelsColor(Color.DKGRAY);
        multiRenderer.setYLabelsColor(0,Color.DKGRAY);
        multiRenderer.setXLabelsColor(Color.DKGRAY);
        multiRenderer.setShowGrid(true);
        multiRenderer.setGridColor(Color.GRAY);

        multiRenderer.setShowLegend(false);
        multiRenderer.setZoomButtonsVisible(true);

        multiRenderer.addSeriesRenderer(mRenderer);
        //mChart = ChartFactory.getLineChartView(getActivity(),dataset , multiRenderer);
       // mChart = ChartFactory.getTimeChartView(getActivity(),dataset , multiRenderer,"HH:mm:ss");//"HH:mm:ss"
        mChart =  new GraphicalView(getActivity(), new TimeChart(dataset , multiRenderer));

        // add the chart to the layout
        layout.addView(mChart,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

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
    }

    //-----------------------------------------------------------------------
    /**
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        showGraph(position);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //--------------------------------------------------------------------------
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            unitUtils = new UnitUtils(getActivity());
            ((MainActivity)getActivity()).analysingFragment=this;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    //--------------------------------------------------------------------------

    void showGraph(int num)
    {
        needUpdate=false;
        Track curentTrack= presenter.getTrackSmoother();//CurrentTrack.getInstance(getActivity());
        if(curentTrack==null)
            return;
        if(curentTrack.getOnChange() instanceof Track)
        {
            curentTrack = (Track)curentTrack.getOnChange();
        };

        lineData.clear();

        if(curentTrack.getGeoPoints().size()>0) {
            long t0 = curentTrack.getGeoPoints().get(0).getTime();

            Location prevLoc=null;
            double prev=0, add=0, t_i=1;
            for(Location loc:curentTrack.getGeoPoints())
            {

                double y=0;
                switch(num)
                {
                    case 0: y=loc.getLatitude();
                        break;
                    case 1: y=loc.getLongitude();
                        break;
                    case 2: y = unitUtils.convertSpeed(loc.getSpeed());
                        break;
                    case 3: y=loc.getBearing()+add;
                            if(prevLoc!=null)
                            {
                                double d=y-prev;
                                if(d<-180) add+=360;
                                if(d>180) add-=360;
                                y=loc.getBearing()+add;
                            }
                            prev=y;

                        break;
                    case 4:
                            if(prevLoc!=null && prevLoc.hasBearing() && loc.hasBearing())
                                y=Track.turn(prevLoc.getBearing(),loc.getBearing());
                        break;
                }
                t_i=(loc.getTime()-t0);
               //data[i++]=new DataPoint(lastX, y);
                lineData.add(t_i, y);
                prevLoc = loc;
            };


            DecimalFormat nf=new DecimalFormat();

            switch(num)
            {
                case 0:
                case 1://myFormatter.yFormat="%.4f";
                       //graph.getGridLabelRenderer().setVerticalAxisTitle(getText(R.string.degree).toString());
                        nf.setMaximumFractionDigits(4);
                       multiRenderer.setYTitle(getText(R.string.degree).toString());
                       break;
                case 3:
                case 4://myFormatter.yFormat="%.0f";
                       //graph.getGridLabelRenderer().setVerticalAxisTitle(getText(R.string.degree).toString());
                       multiRenderer.setYTitle(getText(R.string.degree).toString());
                       nf.setMaximumFractionDigits(0);
                       break;
                case 2:
                      // myFormatter.yFormat="%.1f";
                       //graph.getGridLabelRenderer().setVerticalAxisTitle(unitUtils.speedUnit(false));
                       multiRenderer.setYTitle(unitUtils.speedUnit(false));
                       nf.setMaximumFractionDigits(1);
                       break;
            }
            multiRenderer.setYLabelFormat(nf,0);

            double minX=lineData.getMinX()-1000, maxX=lineData.getMaxX()+1000,
                   minY=lineData.getMinY()-0.0001, maxY=lineData.getMaxY()+0.0001;

            multiRenderer.setInitialRange(new double[] {minX, maxX, minY, maxY});
            mChart.zoomReset(); // it also do repaint

        /*    multiRenderer.setXAxisMin(0,0);
            multiRenderer.setXAxisMax(lastX*1.05,0);

            multiRenderer.setYAxisMin(,0);
            multiRenderer.setYAxisMax(,0);*/

         /*   graph.getGridLabelRenderer().setLabelVerticalWidth(null);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(lastX*1.1+1);*/

            //mChart.invalidate();
        }


    }
    //----------------------------------------------

    public void updateChart() {
        needUpdate=true;
        if(isVisible)
            showGraph(graphSpiner.getSelectedItemPosition());
    }

    //----------------------------------------------
    // CustomPagerView.IonPageAppear
    @Override
    public void onPageAppear() {
        isVisible=true;
      if(/*needUpdate && */graphSpiner!=null)
          showGraph(graphSpiner.getSelectedItemPosition());
    }

    @Override
    public void onPageDisappear() {
        isVisible=false;
    }

    //------------------------------------------------
 /*   class MyLabelFormatter implements LabelFormatter {
        Viewport viewport=null;
        String xFormat="%.0f", yFormat="%.3f";

    public String formatLabel(double value, boolean isValueX)
    {
        if(isValueX) return String.format(Locale.US,xFormat,value);
        else return String.format(Locale.US, yFormat,value);
    };


    public void setViewport(Viewport viewport)
    {
        this.viewport = viewport;
    };
}*/
}
