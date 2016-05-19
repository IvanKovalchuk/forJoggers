package com.kivsw.forjoggers.ui;


import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LabelFormatter;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kivsw.dialog.MessageDialog;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.UnitUtils;
import com.kivsw.forjoggers.model.Track;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnalysingFragment extends Fragment
        implements AdapterView.OnItemSelectedListener,
        CustomPagerView.IonPageAppear
{

    GraphView graph;
    MyLabelFormatter myFormatter;
    UnitUtils unitUtils;
    LineGraphSeries<DataPoint> series;
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

        graph =(GraphView)rootView.findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        String t[]=getActivity().getResources().getStringArray(R.array.time_short_unit);
        graph.getGridLabelRenderer().setHorizontalAxisTitle(t[0]);
        myFormatter = new MyLabelFormatter();
        graph.getGridLabelRenderer().setLabelFormatter(myFormatter);
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setScrollable(true);

        graphSpiner=(Spinner)rootView.findViewById(R.id.spinner);

        spinnerAdpter=ArrayAdapter.createFromResource(getActivity(),
                R.array.graphs, android.R.layout.simple_spinner_item);
        spinnerAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphSpiner.setAdapter(spinnerAdpter);
        graphSpiner.setOnItemSelectedListener(this);
        graphSpiner.setSelection(2);

       /* if(BuildConfig.DEBUG) graphSpiner.setVisibility(View.VISIBLE);
        else*/  graphSpiner.setVisibility(View.GONE);

        presenter = AnalysingFragmentPresenter.getInstance(getActivity());

        return rootView;
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

        if(curentTrack.getGeoPoints().size()>0) {
            DataPoint data[]=new DataPoint[curentTrack.getGeoPoints().size()];
            long t0 = curentTrack.getGeoPoints().get(0).getTime();
            int i=0;
            Location prevLoc=null;
            double prev=0, add=0, lastX=1;
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
                lastX=(loc.getTime()-t0)/1000;
                data[i++]=new DataPoint(lastX, y);
                prevLoc = loc;
            };
            switch(num)
            {
                case 0:
                case 1:myFormatter.yFormat="%.4f";
                       graph.getGridLabelRenderer().setVerticalAxisTitle(getText(R.string.degree).toString());
                       break;
                case 3:
                case 4:myFormatter.yFormat="%.0f";
                       graph.getGridLabelRenderer().setVerticalAxisTitle(getText(R.string.degree).toString());
                       break;
                case 2:
                       myFormatter.yFormat="%.1f";
                       graph.getGridLabelRenderer().setVerticalAxisTitle(unitUtils.speedUnit(false));
                       break;
            }

            //graph.getViewport().setXAxisBoundsManual(false);
            //graph.getViewport().setYAxisBoundsManual(false);
            graph.getGridLabelRenderer().setLabelVerticalWidth(null);
            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(lastX*1.1+1);
           // graph.getGridLabelRenderer().invalidate(false,false);

            try {
                series.resetData(data);
            }catch(Exception e)
            {
                MessageDialog.newInstance(getText(R.string.Error).toString(), e.toString())
                        .show(getFragmentManager(),"");
            }
            //graph.getViewport().setScalable(true);
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
    class MyLabelFormatter implements LabelFormatter {
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
}
}
