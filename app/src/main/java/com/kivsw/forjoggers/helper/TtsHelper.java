package com.kivsw.forjoggers.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * This class is meant for Text-to-speech
 */
public class TtsHelper {

    TextToSpeech tts=null;
    Context context;

    TextToSpeech.OnInitListener onInitListener=null; // listener for TTS

    boolean ready=false, needToRelease=false;

    public TtsHelper(Context context, String engineName, final TextToSpeech.OnInitListener onInitL)
    {
        this.context=context;
        ready=false;
        this.onInitListener=new TextToSpeech.OnInitListener(){
            @Override
            public void onInit(int status) {
                ready= (status==TextToSpeech.SUCCESS);
                if(onInitL!=null)
                     onInitL.onInit(status);
            }
        };
        init(engineName);
    }

    public boolean isReady() {
        return ready;
    }

    @TargetApi(14)
    void init(String engineName)
    {
        if(tts!=null) doRelease();
        needToRelease=false;
        speakingFinished=true;

        //initRx(); // creates all listeners
        if(engineName==null || (Build.VERSION.SDK_INT<14) )
             tts=new TextToSpeech(context, onInitListener);
        else
             tts=new TextToSpeech(context, onInitListener,engineName);

        tts.setOnUtteranceCompletedListener (new TextToSpeech.OnUtteranceCompletedListener(){
            @Override
            public void onUtteranceCompleted(String utteranceId) {
                if(lastUtteranceId.equals(utteranceId))
                    speakingFinished=true;

                if(speakingFinished && needToRelease)
                    doRelease();
            }
        });
    };

    public void release()
    {
        if(speakingFinished) // if tts is not speaking
            doRelease();
        else
            needToRelease=true; // release when tts stops speaking
    }
    private void doRelease()
    {
        if(tts!=null) {

            tts.shutdown();
        }
        tts=null;
        ready=false;
    }

    @TargetApi(14)
    public List<TextToSpeech.EngineInfo> getEngines()
    {
        if(Build.VERSION.SDK_INT<14) return null;

        return tts.getEngines();
    }

  /*  private void initRx()
    {
        // create observable for init
        onInitListener=null;
        Observable ttsInit=Observable.<Void>create(new Observable.OnSubscribe<Void>() {

            @Override
            public void call(final Subscriber<? super Void> subscriber) {
                onInitListener=new TextToSpeech.OnInitListener()
                {
                    @Override
                    public void onInit(int status) {
                        if(status==TextToSpeech.SUCCESS)
                            subscriber.onNext(null);
                        else if(status==TextToSpeech.ERROR)
                            if(onError!=null) onError.call();
                    }
                };
            }
        });

        // create observable for text to speak
        Observable ttsText=Observable.<String>create(new Observable.OnSubscribe<String>() {

            @Override
            public void call(final Subscriber<? super String> subscriber) {
                onNewTextListener=new Action1<String>()
                {
                    @Override
                    public void call(String text) {
                            subscriber.onNext(text);
                    }
                };
            }
        });

        // connect both of the observables
        subscription = Observable
                .<Void, String, String>combineLatest(ttsInit, ttsText,
                        new Func2<Void, String, String>() {
                        @Override
                        public String call(Void aVoid, String s) {
                            return s;
                        }
                   }).subscribe(new Action1<String> (){
                    @Override
                    public void call(String text) {
                        if(tts==null) return;

                        if(tts.isSpeaking()) speakingCount++;
                        else speakingCount=0;

                        if(speakingCount>2) return;

                        doSpeak(text);
                    }
                } );


    };*/
    public boolean isCurrentLanguageSupported()
    {
        Locale l=Locale.getDefault();
//        Locale 	ll=tts.getLanguage();

        int v=tts.isLanguageAvailable(l);
        switch(v)
        {
            case TextToSpeech.LANG_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                return true;
        }
        return false;
    };
    public boolean setLanguge(Locale loc)
    {
        int v=tts.setLanguage(loc);
        switch(v)
        {
            case TextToSpeech.LANG_AVAILABLE:
            case TextToSpeech.LANG_COUNTRY_AVAILABLE:
                return true;
        }
        return false;
    };

    public void speak(String text)
    {
        //onNewTextListener.call(text);
        doSpeak(text);
    };


    //@TargetApi(21)
    static int id=0;
    String lastUtteranceId="";
    boolean speakingFinished=true;

    private void doSpeak(String text)
    {
        lastUtteranceId=String.valueOf(id++);
        speakingFinished=false;
        if(Build.VERSION.SDK_INT<21) {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID ,lastUtteranceId);
            tts.speak(text, TextToSpeech.QUEUE_ADD, params);
        }
        else
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, lastUtteranceId);
    }
    public boolean isSpeaking(){return !speakingFinished;}


}
