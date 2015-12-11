package com.kivsw.forjoggers;


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
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.kivsw.dialog.MessageDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class AnalysingFragment extends Fragment
        implements AdapterView.OnItemSelectedListener, SettingsFragment.onSettingsCloseListener
{

    GraphView graph;
    LineGraphSeries<DataPoint> series;
    Spinner graphSpiner;
    ArrayAdapter<CharSequence> spinnerAdpter;


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
        //graph.getViewport().setScalable(true);
        //graph.getViewport().setScrollable(true);


        graphSpiner=(Spinner)rootView.findViewById(R.id.spinner);

        spinnerAdpter=ArrayAdapter.createFromResource(getActivity(),
                R.array.graphs, android.R.layout.simple_spinner_item);
        spinnerAdpter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        graphSpiner.setAdapter(spinnerAdpter);
        graphSpiner.setOnItemSelectedListener(this);
        graphSpiner.setSelection(0);


        return rootView;
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
            ((MainActivity)getActivity()).analysingFragment=this;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
    //--------------------------------------------------------------------------

    void showGraph(int num)
    {
        Track curentTrack=CurrentTrack.getInstance(getActivity());
        if(curentTrack.getOnChange() instanceof Track)
        {
            curentTrack = (Track)curentTrack.getOnChange();
        };

        if(curentTrack.getGeoPoints().size()>0) {
            DataPoint data[]=new DataPoint[curentTrack.getGeoPoints().size()];
            long t0 = curentTrack.getGeoPoints().get(0).getTime();
            int i=0;
            Location prevLoc=null;
            double prev=0, add=0;
            for(Location loc:curentTrack.getGeoPoints())
            {

                double y=0;
                switch(num)
                {
                    case 0: y=loc.getLatitude();
                        break;
                    case 1: y=loc.getLongitude();
                        break;
                    case 2: y = loc.getSpeed();
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
                    case 5: y=loc.getAccuracy();
                        break;
                }
                data[i++]=new DataPoint((loc.getTime()-t0)/1000, y);
                prevLoc = loc;
            }
            //graph.getViewport().setXAxisBoundsManual(false);
            //graph.getViewport().setYAxisBoundsManual(false);
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

    @Override
    public void onSettingsChanged() {
        showGraph(graphSpiner.getSelectedItemPosition());
    }
}
