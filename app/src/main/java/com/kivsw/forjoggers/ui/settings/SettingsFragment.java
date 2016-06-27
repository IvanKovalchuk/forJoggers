package com.kivsw.forjoggers.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.kivsw.dialog.IconSpinnerAdapter;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.helper.TtsHelper;
import com.kivsw.forjoggers.ui.CustomPagerView;
import com.kivsw.forjoggers.ui.MainActivity;

import java.util.List;
import java.util.Locale;

/**
 *  This class is the UI for settings
 */
public class SettingsFragment extends Fragment
implements CustomPagerView.IonPageAppear
{

    SettingsKeeper settings;

    /*public interface onSettingsCloseListener
    {
        void updateChart();
    }*/
    CheckBox keepInBackgroundCheckBox,autoStopDistanceCheckBox,autoStopTimeCheckBox,
            startStopSpeakCheckBox,timeSpeakCheckBox,distanceSpeakCheckBox;
    EditText weightEditText, autoStopDistanceValueEditText,autoStopTimeValueEditText,
            timeSpeakValueEditText,distanceSpeakValueEditText;
    Spinner weightUnitsSpinner,
            currentActivitySpinner,
            defaultActivitySpinner,

            distanceUnitsSpinner,
            speedDUnitsSpinner,
            speedTUnitsSpinner,
            autoStopDistanceUnitSpinner,autoStopTimeUnitSpinner,
            ttsSpinner,timeSpeakUnitSpinner,distanceSpeakUnitSpinner;


    SettingsFragmentPresenter presenter;

    public SettingsFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings=SettingsKeeper.getInstance(getActivity());
        if (getArguments() != null) {

        }

        presenter = SettingsFragmentPresenter.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ArrayAdapter<CharSequence> adapter;
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_settings, container, false);

        keepInBackgroundCheckBox = (CheckBox)rootView.findViewById(R.id.keepInBackgroundCheckBox);
        weightEditText = (EditText)rootView.findViewById(R.id.weightEditText);
        weightEditText.addTextChangedListener(new WeightWatcher());

        weightUnitsSpinner = (Spinner)rootView.findViewById(R.id.weightUnitsSpinner);
        adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.weight_unit, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightUnitsSpinner.setAdapter(adapter);

        currentActivitySpinner = (Spinner)rootView.findViewById(R.id.currentActivitySpinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currentActivitySpinner.setAdapter(getActivityAdapter());

        defaultActivitySpinner= (Spinner)rootView.findViewById(R.id.defaultActivitySpinner);
        defaultActivitySpinner.setAdapter(getActivityAdapter());

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

        // auto stop options
        autoStopDistanceCheckBox = (CheckBox)rootView.findViewById(R.id.autoStopDistanceCheckBox);
        autoStopDistanceValueEditText = (EditText)rootView.findViewById(R.id.autoStopDistanceValueEditText);
        autoStopDistanceValueEditText.addTextChangedListener(new DistanceValueWatcher(autoStopDistanceValueEditText));

        autoStopDistanceUnitSpinner=(Spinner)rootView.findViewById(R.id.autoStopDistanceUnitSpinner);
        adapter =  ArrayAdapter.createFromResource(getContext(), R.array.distance_unit,android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoStopDistanceUnitSpinner.setAdapter(adapter);

        autoStopTimeCheckBox = (CheckBox)rootView.findViewById(R.id.autoStopTimeCheckBox);
        autoStopTimeValueEditText = (EditText)rootView.findViewById(R.id.autoStopTimeValueEditText);
        autoStopTimeValueEditText.addTextChangedListener(new TimeValueWatcher(autoStopTimeValueEditText));

        autoStopTimeUnitSpinner=(Spinner)rootView.findViewById(R.id.autoStopTimeUnitSpinner);
        adapter =  ArrayAdapter.createFromResource(getContext(), R.array.time_unit_plural,android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        autoStopTimeUnitSpinner.setAdapter(adapter);

// speaking with tts
        ttsSpinner=(Spinner)rootView.findViewById(R.id.ttsSpinner);
        ttsSpinner.setAdapter(getTTS_Adapter());

        startStopSpeakCheckBox= (CheckBox)rootView.findViewById(R.id.startStopSpeakCheckBox);

        timeSpeakCheckBox= (CheckBox)rootView.findViewById(R.id.timeSpeakCheckBox);
        timeSpeakValueEditText=(EditText)rootView.findViewById(R.id.timeSpeakValueEditText);
        timeSpeakValueEditText.addTextChangedListener(new TimeValueWatcher(timeSpeakValueEditText));
        timeSpeakUnitSpinner=(Spinner)rootView.findViewById(R.id.timeSpeakUnitSpinner);
        adapter =  ArrayAdapter.createFromResource(getContext(), R.array.time_unit_plural,android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpeakUnitSpinner.setAdapter(adapter);

        distanceSpeakCheckBox= (CheckBox)rootView.findViewById(R.id.distanceSpeakCheckBox);
        distanceSpeakValueEditText=(EditText)rootView.findViewById(R.id.distanceSpeakValueEditText);
        distanceSpeakValueEditText.addTextChangedListener(new DistanceValueWatcher(distanceSpeakValueEditText));
        distanceSpeakUnitSpinner=(Spinner)rootView.findViewById(R.id.distanceSpeakUnitSpinner);
        adapter =  ArrayAdapter.createFromResource(getContext(), R.array.distance_unit,android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        distanceSpeakUnitSpinner.setAdapter(adapter);

        loadData();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    //-------------------------------------------------------

    @Override
    public void onPause()
    {
        if(isVisible) // if the page is visible and the user could change smth
            saveData();
        super.onPause();

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
        keepInBackgroundCheckBox.setChecked(settings.getKeepBackGround());

        SettingsKeeper.FlexibleWeight weight=settings.getMyWeight();
        weightEditText.setText(String.valueOf(weight.getWeight()));
        weightUnitsSpinner.setSelection(weight.getUnit());

        currentActivitySpinner.setSelection(presenter.getCurrentTrack().getActivityType());
        defaultActivitySpinner.setSelection(settings.getActivityType());

        distanceUnitsSpinner.setSelection(settings.getDistanceUnit());
        speedDUnitsSpinner.setSelection(settings.getSpeedUnitDistance());
        speedTUnitsSpinner.setSelection(settings.getSpeedUnitTime());


        // auto stop
        autoStopDistanceCheckBox.setChecked(settings.getIsDistanceAutoStop());

        SettingsKeeper.FlexibleDistance d=settings.getAutoStopDistance();
        autoStopDistanceValueEditText.setText(  String.format(Locale.US,"%.1f",d.getDistance()));
        autoStopDistanceUnitSpinner.setSelection(d.getUnit());

        // auto stop
        autoStopTimeCheckBox.setChecked(settings.getIsAutoStopTime());

        SettingsKeeper.FlexibleTime t=settings.getAutoStopTime();
        autoStopTimeValueEditText.setText(String.valueOf(t.getTime()));
        autoStopTimeUnitSpinner.setSelection(t.getUnit());


        // tts
        ttsSpinner.setSelection(getTTSengineIndex(settings.getTTS_engine()));
        startStopSpeakCheckBox.setChecked(settings.getIsStartStopSpeaking());

        timeSpeakCheckBox.setChecked(settings.getIsTimeSpeaking());
        t=settings.getTimeSpeaking();
        timeSpeakValueEditText.setText(String.valueOf(t.getTime()));
        timeSpeakUnitSpinner.setSelection(t.getUnit());

        distanceSpeakCheckBox.setChecked(settings.getIsDistanceSpeaking());
        d=settings.getDistanceSpeaking();
        distanceSpeakValueEditText.setText(String.format(Locale.US,"%.1f",d.getDistance()));
        distanceSpeakUnitSpinner.setSelection(d.getUnit());



    }
    private void saveData()
    {
        int i;
        double d;
        settings.setKeepBackGround(keepInBackgroundCheckBox.isChecked());

        try{i=Integer.parseInt(weightEditText.getText().toString());}
        catch(Exception e){i=0;};
        settings.setMyWeight(new SettingsKeeper.FlexibleWeight(i, weightUnitsSpinner.getSelectedItemPosition()));
        presenter.getCurrentTrack().setActivityType(currentActivitySpinner.getSelectedItemPosition());

        settings.setActivityType(defaultActivitySpinner.getSelectedItemPosition());

        settings.setDistanceUnit(distanceUnitsSpinner.getSelectedItemPosition());

        settings.setSpeedUnit(speedDUnitsSpinner.getSelectedItemPosition(), speedTUnitsSpinner.getSelectedItemPosition());

        settings.setIsDistanceAutoStop(autoStopDistanceCheckBox.isChecked());

        settings.setAutoStopDistance(new SettingsKeeper.FlexibleDistance(Double.parseDouble(autoStopDistanceValueEditText.getText().toString()),autoStopDistanceUnitSpinner.getSelectedItemPosition()) );

        settings.setIsAutoStopTime(autoStopTimeCheckBox.isChecked());

        settings.setAutoStopTime(new SettingsKeeper.FlexibleTime(Long.parseLong(autoStopTimeValueEditText.getText().toString()), autoStopTimeUnitSpinner.getSelectedItemPosition()));

        // tts
        settings.setTTS_engine(getTTSengineName(ttsSpinner.getSelectedItemPosition()));
        settings.setIsStartStopSpeaking(startStopSpeakCheckBox.isChecked());

        settings.setIsTimeSpeaking(timeSpeakCheckBox.isChecked());
        try{i=Integer.parseInt(timeSpeakValueEditText.getText().toString());}
        catch(Exception e){i=0;};
        settings.setTimeSpeaking(new SettingsKeeper.FlexibleTime(i,timeSpeakUnitSpinner.getSelectedItemPosition()));

        settings.setIsDistanceSpeaking( distanceSpeakCheckBox.isChecked());
        try{d=Double.parseDouble(distanceSpeakValueEditText.getText().toString());}
        catch(Exception e){d=0;};
        settings.setDistanceSpeaking(new SettingsKeeper.FlexibleDistance(d,distanceSpeakUnitSpinner.getSelectedItemPosition()));


        // informs the others that we have new settings
        presenter.onSettingsChanged();

    }
    //----------------------------------------------
    // CustomPagerView.IonPageAppear
    boolean isVisible=false;
    @Override
    public void onPageAppear() {
        isVisible=true;
        loadData();
    }

    @Override
    public void onPageDisappear() {
        View focusView= getView().findFocus();
        if(focusView!=null) {
            InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            focusView.clearFocus();
        }
        if(isVisible)
           saveData();
        isVisible=false;
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
    class DistanceValueWatcher implements TextWatcher {
        EditText editText;
        DistanceValueWatcher(EditText et)
        {
            editText=et;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            double v = -1;
            try {
                v = Double.parseDouble(s.toString());
            } catch (Exception e) {
            } ;
            if ((v < 0 || v > 99999) ) {
                editText.setError(getText(R.string.Incorrect_value));
            } else
                editText.setError(null);

        }
    }
    //----------------------------------------------
    class TimeValueWatcher implements TextWatcher {
        EditText editText;
        TimeValueWatcher(EditText et)
        {
            editText=et;
        }
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
            } ;
            if ((v < 0 || v > 99999)) {
                editText.setError(getText(R.string.Incorrect_value));
            } else
                editText.setError(null);
        }
    }
    //----------------------------------------------

    List<TextToSpeech.EngineInfo> ttsEngineList=null;
    int getTTSengineIndex(String packageName)
    {
        int i=0;
        for(;i<ttsEngineList.size();i++)
        {
            if(ttsEngineList.get(i).name.equals(packageName))
            {
                return i+1; // add one because the first item is "default" in getTTS_Adapter()
            };
        }
        return 0; // default
    }
    String getTTSengineName(int index)
    {
        index--; // the first index is "default" in getTTS_Adapter(), so substract 1
        if(index<0 || index >= ttsEngineList.size()) return "";

        return ttsEngineList.get(index).name;
    }
    IconSpinnerAdapter getTTS_Adapter()
    {
        TtsHelper speaker=new TtsHelper(getContext(),null,null);
        ttsEngineList=speaker.getEngines();
        speaker.release();

        Drawable icons[];
        String names[];

        if(ttsEngineList==null)
        {
            names=new String[1];
            icons = null;
        }
        else
        {
            names=new String[1+ttsEngineList.size()];
            icons = new Drawable[1+ttsEngineList.size()];

            int i=1;
            Drawable appIcon;
            for(TextToSpeech.EngineInfo engineInfo:ttsEngineList)
            {
                appIcon=null;

                try {
                    ApplicationInfo appInfo = getContext().getPackageManager().getApplicationInfo(engineInfo.name, 0);
                    appIcon = getContext().getPackageManager().getDrawable(engineInfo.name, engineInfo.icon, appInfo);
                }catch(Exception e) {}

                if(appIcon==null)
                    try {
                        appIcon = getContext().getPackageManager().getApplicationIcon(engineInfo.name);
                    }catch (Exception ee) { }

                names[i]=engineInfo.label;
                icons[i]=appIcon;
                i++;
            }
        }

        names[0]=getText(R.string.defaultTTS).toString();
        icons[0]=null;

        return new IconSpinnerAdapter(getContext(),names,icons);

    }
    IconSpinnerAdapter getActivityAdapter()
    {
        String activities[]=getResources().getStringArray(R.array.activities);
        Drawable icons[]=new Drawable[3];
        icons[0]=  new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(),R.drawable.walking_c) );//getResources().getDrawable(R.drawable.walking,null);
        icons[1]=  new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(),R.drawable.jogging_c) );
        icons[2]=  new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(),R.drawable.bycicling_c) );

        return new IconSpinnerAdapter(getContext(),activities,icons);
    }

}
