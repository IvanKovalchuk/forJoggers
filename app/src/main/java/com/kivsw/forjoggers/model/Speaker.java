package com.kivsw.forjoggers.model;

import android.content.Context;
import android.content.res.Resources;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.helper.TtsHelper;
import com.kivsw.forjoggers.model.track.Track;
import com.kivsw.forjoggers.ui.settings.TrackingServicePresenter;

import java.util.LinkedList;
import java.util.Locale;

import rx.functions.Action1;

/**
 * this class
 * - works with TTS via TtsHelper,
 * - forms the messages for "start", "stop" and the current track information,
 * - synchronize events of speaking and TTS-realising/initializing
 *
 */
public class Speaker {
    TtsHelper ttsHelper = null;
    Context context;
    SettingsKeeper settings;
    boolean useEngMessages=false,
            speakingFinished = true,
            needToRelease=false;
    String currentEngine=null;
    LinkedList<UtteranceType> utteranceQueue = new LinkedList();
    enum UtteranceType {START , STOP , TRACKSTATE};

    Action1<String> onErrorListener=null;


    Speaker(Context cnt, Action1<String> anOnErrorListener) {
        context = cnt;
        settings = SettingsKeeper.getInstance(context);
        onErrorListener=anOnErrorListener;
    }

    void release() {

        if(speakingFinished)
            doRelease();
        else
            needToRelease=true; // it will invoke doRelease() when speaking is finished

    }
    private  void doRelease()
    {
        if (ttsHelper != null)
            ttsHelper.release();
        ttsHelper = null;
    }

    void init() {
        needToRelease=false;

        if (ttsHelper != null) return;
        useEngMessages=false;

        currentEngine = settings.getTTS_engine();
        ttsHelper = new TtsHelper(context,currentEngine , new TtsHelper.TTS_Helperlistener() {
            @Override
            public void onInit(int status) {
                if(ttsHelper==null) return;
                if (!ttsHelper.isReady()) {
                    if(onErrorListener!=null)
                        onErrorListener.call(context.getText(R.string.tts_init_error).toString());
                    return;
                }
                if(!ttsHelper.isCurrentLanguageSupported()) {
                    ttsHelper.setLanguge(Locale.ENGLISH);
                    useEngMessages=true;
                }
                else
                    ttsHelper.setLanguge(Locale.getDefault());
                processUtterances();
            }

            @Override
            public void onStopSpeaking() {
                TrackingServicePresenter.getInstance(context).endTTSspeaking();
                speakingFinished=true;
                if(needToRelease)
                    doRelease();
            }

        });

    }

    public void speakStart() {
        init();

        TrackingServicePresenter.getInstance(context).startTTSspeaking();
        utteranceQueue.add(UtteranceType.START);
        speakingFinished=false;

        processUtterances();
    }

    public void speakStop()
    {
        init();

        TrackingServicePresenter.getInstance(context).startTTSspeaking();
        utteranceQueue.add(UtteranceType.STOP);
        speakingFinished=false;

        processUtterances();
    }
    public void speakTrack()
    {
        init();

        if(utteranceQueue.size()>2 || ttsHelper.isSpeaking())
            return;

        while(utteranceQueue.contains(UtteranceType.TRACKSTATE))
            utteranceQueue.remove(UtteranceType.TRACKSTATE);

        TrackingServicePresenter.getInstance(context).startTTSspeaking();
        utteranceQueue.add(UtteranceType.TRACKSTATE);
        speakingFinished=false;

        processUtterances();
    }

    private void processUtterances()
    {
        UtteranceType u;
        if(!ttsHelper.isReady()) return;

        while(null!=(u= utteranceQueue.poll()))
        {
            switch(u)
            {
                case START:
                    doSpeakStart();
                    break;
                case STOP: {
                    Track t = DataModel.getInstance(context).getTrackSmoother();
                    if (t != null)
                        doSpeakStop(t.getTrackDistance(), t.getTrackTime());
                    else
                        doSpeakStop(0, 0);
                    }
                    break;
                case TRACKSTATE: {
                    Track t=DataModel.getInstance(context).getTrackSmoother();
                    if(t!=null)
                       doSpeakTrack(t.getTrackDistance(), t.getTrackTime());
                    }
                    break;
            }
        }
    };
    private void doSpeakStart()
    {
        String str;
        if(useEngMessages) str=context.getText(R.string.tts_start_en).toString();
        else  str=context.getText(R.string.tts_start).toString();
        ttsHelper.speak(str);

    }
    private void doSpeakStop(double d, long t)
    {
        String str;
        if(useEngMessages) str=context.getText(R.string.tts_stop_en).toString();
        else  str=context.getText(R.string.tts_stop).toString();

        String str2=createTrackInfo( d, t);
        ttsHelper.speak(str+" \n "+str2);
    }
    private void doSpeakTrack(double d, long t)
    {
        String str=createTrackInfo( d,  t);
        ttsHelper.speak(str.toString());
    }
    String createTrackInfo(double d, long t)
    {
        StringBuilder str=new StringBuilder();
        int id_hour, id_min, id_sec, id_meter, id_kilimeter, id_mile, id_miles;
        // choose ids for the text
        if(useEngMessages) {
            id_hour=R.plurals.numberOfhours_en;
            id_min=R.plurals.numberOfminutes_en;
            id_sec=R.plurals.numberOfseconds_en;
            id_meter=R.plurals.numberOfmeters_en;
            id_kilimeter=R.plurals.numberOfkilometers_en;
            id_mile=R.plurals.numberOfmiles_en;
            id_miles = R.string.tts_miles_en;
        }
        else {
            id_hour=R.plurals.numberOfhours;
            id_min=R.plurals.numberOfminutes;
            id_sec=R.plurals.numberOfseconds;
            id_meter=R.plurals.numberOfmeters;
            id_kilimeter=R.plurals.numberOfkilometers;
            id_mile=R.plurals.numberOfmiles;
            id_miles = R.string.tts_miles;
        }

        // forms time
        long h,m,s;
        Resources res=context.getResources();

        t=t/1000;
        h=t/3600; t=t%3600;
        m=t/60; t=t%60;
        s=t;


        if(h>0)
            str.append(res.getQuantityString(id_hour, (int)h,(int)h));
        str.append(" ");
        if(m>0)
            str.append(res.getQuantityString(id_min, (int)m,(int)m));
        str.append(" ");

        if(s>0)
            str.append(res.getQuantityString(id_sec, (int)s,(int)s));
        str.append(" \n ");

        // forms distance
        if(settings.getDistanceUnit()==SettingsKeeper.MILES)
        { // miles
            d=d/1609.0;
            str.append(String.format(Locale.US, "%.1f",d));
            str.append(res.getText(id_miles));

        }
        else
        { // kilometers
            long meters=(long)(d+0.5);
            long km;

            km = (meters/1000);
            meters=meters%1000;

            if(km>0)
                str.append(res.getQuantityString(id_kilimeter, (int)km,(int)km));
            str.append(" ");
            if(meters>0)
                str.append(res.getQuantityString(id_meter, (int)meters,(int)meters));
            str.append(" ");
        }

        return str.toString();
    }

}
