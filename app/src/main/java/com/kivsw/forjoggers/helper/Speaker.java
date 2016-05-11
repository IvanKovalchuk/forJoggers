package com.kivsw.forjoggers.helper;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func2;


/**
 * This class is meant for Text-to-speech
 */
public class Speaker {
    interface OnError{
        void call();
    }
    TextToSpeech tts=null;
    Context context;

    TextToSpeech.OnInitListener onInitListener=null; // listener for TTS
    Action1<String> onNewTextListener=null;          // listener for new Text to pronounce
    Subscription subscription=null;

    OnError onError=null;
    int speakingCount=0;

    public Speaker(Context context, OnError onError)
    {
        this.context=context;
        this.onError=onError;
        init(null);
    }

    @TargetApi(14)
    void init(String engineName)
    {
        if(tts!=null) release();

        initRx(); // creates all listeners
        if(engineName==null || (Build.VERSION.SDK_INT<14) )
             tts=new TextToSpeech(context, onInitListener);
        else
             tts=new TextToSpeech(context, onInitListener,engineName);
    };

    public void release()
    {
        if(tts!=null) {
            tts.stop();
            tts.shutdown();
        }
        tts=null;
    }

    @TargetApi(14)
    public List<TextToSpeech.EngineInfo> getEngines()
    {
        if(Build.VERSION.SDK_INT<14) return null;

        return tts.getEngines();
    }

    private void initRx()
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
                        if(tts!=null) return;

                        if(tts.isSpeaking()) speakingCount++;
                        else speakingCount=0;

                        if(speakingCount>2) return;

                        doSpeak(text);
                    }
                } );


    };

    public void speak(String text)
    {
        onNewTextListener.call(text);
    };

    //@TargetApi(21)
    private void doSpeak(String text)
    {
        if(Build.VERSION.SDK_INT<21)
           tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null,"");
    }



}
