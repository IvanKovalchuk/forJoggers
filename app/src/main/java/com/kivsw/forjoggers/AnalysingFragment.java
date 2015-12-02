package com.kivsw.forjoggers;


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

/**
 * A simple {@link Fragment} subclass.
 */
public class AnalysingFragment extends Fragment
        implements AdapterView.OnItemSelectedListener
{

    GraphView graph;
    LineGraphSeries<DataPoint> series;
    Spinner graphSpiner;
    ArrayAdapter<CharSequence> spinnerAdpter;


    public AnalysingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_analysing, container, false);

        graph =(GraphView)rootView.findViewById(R.id.graph);
        series = new LineGraphSeries<DataPoint>();
        graph.addSeries(series);
        /*graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);*/


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

    void showGraph(int num)
    {
        CurrentTrack curentTrack=CurrentTrack.getInstance(getActivity());

        if(curentTrack.mGeoPoints.size()>0) {
            DataPoint data[]=new DataPoint[curentTrack.mGeoPoints.size()];
            long t0 = curentTrack.mGeoPoints.get(0).getTime();
            int i=0;
            for(Location loc:curentTrack.mGeoPoints)
            {
                double y=0;
                switch(num)
                {
                    case 0: y=loc.getLatitude();
                        break;
                    case 1: y=loc.getLongitude();
                        break;
                    case 3: y=loc.getSpeed();
                        break;
                    case 4: y=loc.getAccuracy();
                        break;
                }
                data[i++]=new DataPoint((loc.getTime()-t0)/1000, y);
            }
            //graph.getViewport().setXAxisBoundsManual(false);
            //graph.getViewport().setYAxisBoundsManual(false);
            series.resetData(data);
            //graph.getViewport().setScalable(true);
        }


    }
}
