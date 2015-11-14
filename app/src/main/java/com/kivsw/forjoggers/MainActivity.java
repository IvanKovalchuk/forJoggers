package com.kivsw.forjoggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity
implements  View.OnClickListener
{

    final static String ACTION_RECEIVE_TRACK="com.kivsw.forjoggers.ACTION_RECIEVE_TRACK";
    MapFragment mapFragment;
    SettingsKeeper settings;

    Button buttonStart, buttonStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(this);

        buttonStop = (Button)findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(this);
        if(TrackingService.isWorking)
        {
            buttonStop.setVisibility(View.VISIBLE);
            buttonStart.setVisibility(View.GONE);
        }
        else
        {
            buttonStop.setVisibility(View.GONE);
            buttonStart.setVisibility(View.VISIBLE);
        }

        settings=SettingsKeeper.getInstance(this);

        mapFragment.setTrack(settings.getCurrentTrack());

        processIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent i)
    {
        super.onNewIntent(i);
        processIntent(i);
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void processIntent(Intent i)
    {

        if(i==null) return;

        switch(i.getAction())
        {
            case ACTION_RECEIVE_TRACK:
                mapFragment.setTrack(settings.getCurrentTrack());
                break;
        }
    };
    static public void receiveNewTrack(Context context)
    {
        Intent i=new Intent(ACTION_RECEIVE_TRACK);
        i.setClass(context, MainActivity.class);
        //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);


    }


    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.buttonStart:
                TrackingService.start(this);
                buttonStart.setVisibility(View.GONE);
                buttonStop.setVisibility(View.VISIBLE);
                break;
            case R.id.buttonStop:
                TrackingService.stop(this);
                buttonStop.setVisibility(View.GONE);
                buttonStart.setVisibility(View.VISIBLE);
                break;
        }
    }
}
