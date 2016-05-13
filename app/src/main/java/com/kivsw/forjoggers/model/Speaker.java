package com.kivsw.forjoggers.model;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.helper.TtsHelper;
import com.kivsw.forjoggers.ui.MainActivityPresenter;

import java.util.LinkedList;
import java.util.Locale;

/**
 * Created by ivan on 5/12/16.
 */
public class Speaker {
    TtsHelper ttsHelper = null;
    Context context;
    SettingsKeeper settings;
    boolean useEngMessages=false;
    LinkedList<UtteranceType> utteranceQueue = new LinkedList();
    enum UtteranceType {START , STOP , TRACKSTATE};


    Speaker(Context cnt) {
        context = cnt;
        settings = SettingsKeeper.getInstance(context);
    }

    void release() {
        if (ttsHelper != null)
            ttsHelper.release();
        ttsHelper = null;
    }

    void init() {
        if (ttsHelper != null) return;
        useEngMessages=false;

        ttsHelper = new TtsHelper(context, settings.getTTS_engine(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (!ttsHelper.isReady()) {
                    MainActivityPresenter.getInstance(context)
                            .showError(context.getText(R.string.tts_init_error).toString());
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

        });

    }

    public void speakStart() {
        init();
        utteranceQueue.add(UtteranceType.START);
        processUtterances();
    }

    public void speakStop()
    {
        init();
        utteranceQueue.add(UtteranceType.STOP);
        processUtterances();
    }
    public void speakTrack()
    {
        init();
        while(utteranceQueue.contains(UtteranceType.TRACKSTATE))
            utteranceQueue.remove(UtteranceType.TRACKSTATE);
        utteranceQueue.add(UtteranceType.TRACKSTATE);
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
                case STOP:
                    doSpeakStop();
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
    private void doSpeakStop()
    {
        String str;
        if(useEngMessages) str=context.getText(R.string.tts_stop_en).toString();
        else  str=context.getText(R.string.tts_stop).toString();
        ttsHelper.speak(str);
    }
    private void doSpeakTrack(double d, double t)
    {

    }

}
