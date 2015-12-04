package com.kivsw.forjoggers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.kivsw.dialog.FileDialog;
import com.kivsw.dialog.MessageDialog;

import java.io.File;


public class MainActivity extends ActionBarActivity
implements  View.OnClickListener, FileDialog.OnCloseListener, MessageDialog.OnCloseListener
{

    final static String ACTION_RECEIVE_TRACK="com.kivsw.forjoggers.ACTION_RECIEVE_TRACK";

    private ViewPager pager;
    MapFragment mapFragment=null;
    AnalysingFragment analysingFragment=null;
    SettingsKeeper settings;

    Button buttonStart, buttonStop;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager =(ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
       // mapFragment = pager.insta

        /*pager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/

        //mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);

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

        //mapFragment.setTrack(settings.getCurrentTrack());

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
    public boolean onPrepareOptionsMenu(Menu menu)
     {
         MenuItem item=menu.findItem(R.id.return_to_mylocation);
         item.setChecked(settings.getReturnToMyLocation());
         return super.onPrepareOptionsMenu(menu);
     }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        FileDialog fd=null;
        String dir=settings.getLastPath();
        if(dir==null)
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();


        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_save_track:
                fd=FileDialog.newInstance(1,FileDialog.TypeDialog.SAVE , dir, "*", this);
                fd.show(getSupportFragmentManager(), "");
                return true;
            case R.id.action_load_track:
                fd=FileDialog.newInstance(2,FileDialog.TypeDialog.OPEN , dir, "*", this);
                fd.show(getSupportFragmentManager(),"");
                return true;
            case R.id.return_to_mylocation:
                settings.setReturnToMyLocation(!item.isChecked());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void processIntent(Intent i)
    {

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
                if(CurrentTrack.getInstance(this).needToBeSaved())
                    MessageDialog.newInstance(R.string.track_may_be_lost,
                                  getText(R.string.Warning).toString(),
                                  getText(R.string.track_may_be_lost).toString(),
                                  this)
                    .show(getSupportFragmentManager(),"");
                else
                    startTrackService();
                break;
            case R.id.buttonStop:
                stopTrackService();
                break;
        }
    }

    /**
     * start tracking
     */
    private void startTrackService()
    {
        TrackingService.start(this);
        mapFragment.onStartTrackingService();
        buttonStart.setVisibility(View.GONE);
        buttonStop.setVisibility(View.VISIBLE);
    };

    /**
     * stops tracking
     */
    private void stopTrackService()
    {
        TrackingService.stop(this);
        buttonStop.setVisibility(View.GONE);
        buttonStart.setVisibility(View.VISIBLE);
    };

    //-------------------------------
    // FileDialog.OnCloseListener
    @Override
    public void onClickOk(FileDialog dlg, String fileName) {

        File f=new File(fileName);
        if(f.exists())
          settings.setLastPath(f.getParent());

        switch(dlg.getDlgId())
        {
            case 1:
                if(!mapFragment.saveTrackToFile(fileName))
                {
                    String msg=String.format(getText(R.string.cannot_save_file).toString(),fileName);
                    MessageDialog.newInstance(getText(R.string.Error).toString(),msg)
                            .show(getSupportFragmentManager(),"");
                }
                break;
            case 2:
                if(!mapFragment.loadTrackFromFile(fileName))
                {
                    String msg=String.format(getText(R.string.cannot_load_file).toString(),fileName);
                    MessageDialog.newInstance(getText(R.string.Error).toString(),msg)
                            .show(getSupportFragmentManager(),"");
                };
                break;
        }
    }

    @Override
    public void onClickCancel(FileDialog dlg) {

    }
    //-------------------------------
    //  MessageDialog.OnCloseListener
    @Override
    public void onClickOk(MessageDialog msg) {
        switch (msg.getDlgId())
        {
            case R.string.track_may_be_lost:
                startTrackService();
                break;
        }
    }

    @Override
    public void onClickCancel(MessageDialog msg) { }

    @Override
    public void onClickExtra(MessageDialog msg) { }

    //--------------------------------------------------------------------------

    public class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            //return ArrayListFragment.newInstance(position);
            Fragment res=null;
            switch(position)
            {
                case 0:
                      res=new MapFragment();
                    break;
                case 1:
                      res=new AnalysingFragment();
                    break;
            }
            return res;
        }
    }


}
