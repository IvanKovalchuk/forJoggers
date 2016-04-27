package com.kivsw.forjoggers;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.ui.MainActivity;


public class SettingsFragment extends Fragment
implements CustomPagerView.IonPageAppear
{

    SettingsKeeper settings;


    public interface onSettingsCloseListener
    {
        void onSettingsChanged();
    }
    CheckBox returnToMyLocationCheckBox;
    EditText weightEditText;
    Spinner weightUnitsSpinner,
            currentActivitySpinner,
            defaultActivitySpinner,

            distanceUnitsSpinner,
            speedDUnitsSpinner,
            speedTUnitsSpinner;


    public SettingsFragment() {
        super();
    }


    /*public static SettingsFragment newInstance(onSettingsCloseListener listener) {
        SettingsFragment fragment = new SettingsFragment();
        fragment.setOnCloseLister(listener);
        return fragment;
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings=SettingsKeeper.getInstance(getActivity());
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> adapter;
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_settings, container, false);

        returnToMyLocationCheckBox = (CheckBox)rootView.findViewById(R.id.returnToMyLocationCheckBox);
        weightEditText = (EditText)rootView.findViewById(R.id.weightEditText);
        weightEditText.addTextChangedListener(new WeightWatcher());

        weightUnitsSpinner = (Spinner)rootView.findViewById(R.id.weightUnitsSpinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.weight_unit, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightUnitsSpinner.setAdapter(adapter);

        currentActivitySpinner = (Spinner)rootView.findViewById(R.id.currentActivitySpinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.activities, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currentActivitySpinner.setAdapter(adapter);

        defaultActivitySpinner= (Spinner)rootView.findViewById(R.id.defaultActivitySpinner);
        defaultActivitySpinner.setAdapter(adapter);

        distanceUnitsSpinner = (Spinner)rootView.findViewById(R.id.distanceUnitsSpinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.distance_unit, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceUnitsSpinner.setAdapter(adapter);

        speedDUnitsSpinner = (Spinner)rootView.findViewById(R.id.speedDUnitsSpinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.distance_unit, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedDUnitsSpinner.setAdapter(adapter);

        speedTUnitsSpinner = (Spinner)rootView.findViewById(R.id.speedTUnitsSpinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.time_unit, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedTUnitsSpinner.setAdapter(adapter);

       // getDialog().setTitle(getText(R.string.Settings));

        loadData();
        return rootView;
    }

    //-------------------------------------------------------

    @Override
    public void onPause()
    {
        saveData();
        super.onPause();

    }
    @Override
    public void onStop()
    {
        super.onStop();

    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            ((MainActivity)getActivity()).settingsFragment=this;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void loadData()
    {
        returnToMyLocationCheckBox.setChecked(settings.getReturnToMyLocation());
        weightEditText.setText(String.valueOf(settings.getMyWeight()));
        weightUnitsSpinner.setSelection(settings.getMyWeightUnit());

       // currentActivitySpinner.setSelection(CurrentTrack.getInstance(getActivity()).activityType);
        defaultActivitySpinner.setSelection(settings.getActivityType());

        distanceUnitsSpinner.setSelection(settings.getDistanceUnit());
        speedDUnitsSpinner.setSelection(settings.getSpeedUnitDistance());
        speedTUnitsSpinner.setSelection(settings.getSpeedUnitTime());
    }
    private void saveData()
    {
        int i;
        settings.setReturnToMyLocation(returnToMyLocationCheckBox.isChecked());

        try{i=Integer.parseInt(weightEditText.getText().toString());}
        catch(Exception e){i=0;};
        settings.setMyWeight(i, weightUnitsSpinner.getSelectedItemPosition());

      //  CurrentTrack.getInstance(getActivity()).setActivityType(
        //        currentActivitySpinner.getSelectedItemPosition());

        settings.setActivityType(defaultActivitySpinner.getSelectedItemPosition());

        settings.setDistanceUnit(distanceUnitsSpinner.getSelectedItemPosition());

        settings.setSpeedUnit(speedDUnitsSpinner.getSelectedItemPosition(), speedTUnitsSpinner.getSelectedItemPosition());

        if(getActivity()!=null && (getActivity() instanceof onSettingsCloseListener) ) {
            onSettingsCloseListener l = (onSettingsCloseListener) getActivity();
            l.onSettingsChanged();
        }

    }
    //----------------------------------------------
    // CustomPagerView.IonPageAppear
    @Override
    public void onPageAppear() {
        loadData();
    }

    @Override
    public void onPageDisappear() {
        saveData();
    }
    ////--------------------------------------------
    class WeightWatcher implements TextWatcher {
        //TextWatcher
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            long v = -1;
            try {
                v = Long.parseLong(s.toString());
            } catch (Exception e) {
            }
            ;
            if (v < 0 || v > 999) {
                weightEditText.setError(getText(R.string.Incorrect_value));
            } else
                weightEditText.setError(null);

        }
    }
    //----------------------------------------------

}
