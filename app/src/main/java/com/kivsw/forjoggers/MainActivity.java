package com.kivsw.forjoggers;

import android.content.Intent;
import android.location.Location;
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
import android.widget.TextView;

import com.kivsw.dialog.FileDialog;
import com.kivsw.dialog.MessageDialog;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
implements  View.OnClickListener, FileDialog.OnCloseListener, MessageDialog.OnCloseListener
{

    final static String ACTION_RECEIVE_TRACK="com.kivsw.forjoggers.ACTION_RECIEVE_TRACK";

    private ViewPager pager;
    MapFragment mapFragment=null;
    AnalysingFragment analysingFragment=null;
    TextView fileNameTextView;
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
        fileNameTextView = (TextView)findViewById(R.id.fileNameTextView);
        updateFileName();

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
//----------------------------------------------------------
    @Override
    protected void onNewIntent(Intent i)
    {
        super.onNewIntent(i);
        processIntent(i);
    }
    //----------------------------------------------------------
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

    }
    //----------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //----------------------------------------------------------
     @Override
    public boolean onPrepareOptionsMenu(Menu menu)
     {
         MenuItem item=menu.findItem(R.id.action_show_my_location);

         item=menu.findItem(R.id.action_load_track);
         item.setEnabled(!TrackingService.isWorking);

         item=menu.findItem(R.id.action_save_track);
         item.setEnabled(!TrackingService.isWorking);

         boolean isTrack = CurrentTrack.getInstance(this).getGeoPoints().size()>1;
         item=menu.findItem(R.id.action_show_my_track);
         item.setEnabled(isTrack);

         item=menu.findItem(R.id.action_animate_my_track);
         item.setEnabled(isTrack && !TrackingService.isWorking);

         return super.onPrepareOptionsMenu(menu);
     }
    //----------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                SettingsFragment.newInstance(new SettingsFragment.onSettingsCloseListener(){
                    @Override
                    public void onSettingsChanged() {
                        analysingFragment.onSettingsChanged();
                        mapFragment.onSettingsChanged();
                    }
                }).show(getSupportFragmentManager(),"");
                return true;

            case R.id.action_save_track:
                saveCurrentTrack();
                return true;

            case R.id.action_load_track:
                if(CurrentTrack.getInstance(this).needToBeSaved())
                {
                    MessageDialog.newInstance(TRACK_MAY_BE_LOST_LOAD_FILE,
                            getText(R.string.Warning).toString(),
                            getText(R.string.track_may_be_lost).toString(),
                            this)
                            .show(getSupportFragmentManager(),"");
                }
                else
                {
                    loadCurrentTrack();
                }
                return true;

            case R.id.action_show_my_location:
                mapFragment.showMyLocation();
                return true;

            case R.id.action_show_my_track:
                ArrayList<Location> points=CurrentTrack.getInstance(this).getGeoPoints();
                if(points!=null && points.size()>0)
                   mapFragment.showLocation(points.get(0).getLatitude(), points.get(0).getLongitude());
                return true;

            case R.id.action_animate_my_track:
                mapFragment.animateTrack();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------
    private void processIntent(Intent i)
    {

    };

    //----------------------------------------------------------
    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.buttonStart: {
                StringBuilder problems=new StringBuilder();

                if (mapFragment != null && !mapFragment.getGPSstatus())
                     problems.append(getText(R.string.GPRS_has_not_found_location));

                if (CurrentTrack.getInstance(this).needToBeSaved())
                    problems.append(getText(R.string.track_may_be_lost));

                if(problems.length()>0) {
                    problems.append(getText(R.string.Continue));
                    MessageDialog.newInstance(WARNINGS_AND_START_SERVICE,
                            getText(R.string.Warning).toString(), problems.toString(),
                            this)
                            .show(getSupportFragmentManager(), "");
                }
                    else
                        startTrackService();
                }
                break;
            case R.id.buttonStop:
                stopTrackService();
                break;
        }
    }
//----------------------------------------------------------
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
//----------------------------------------------------------
    /**
     * stops tracking
     */
    private void stopTrackService()
    {
        TrackingService.stop(this);
        buttonStop.setVisibility(View.GONE);
        buttonStart.setVisibility(View.VISIBLE);
    };

    private void updateFileName()
    {
        String fn= CurrentTrack.getInstance(this).fileName;
        if(fn!=null && !fn.isEmpty()) {
            File file = new File(fn);
            fn=file.getName();
        }
        if(fn==null || fn.isEmpty())
            fn="*";
        fileNameTextView.setText(fn);
    };

    private void saveCurrentTrack()
    {
        String dir=settings.getLastPath();
        if(dir==null)
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();
        FileDialog fd;
        fd=FileDialog.newInstance(1,FileDialog.TypeDialog.SAVE , dir, "*", this);
        fd.show(getSupportFragmentManager(), "");
    }

    private void loadCurrentTrack()
    {
        String dir=settings.getLastPath();
        if(dir==null)
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();
        FileDialog fd;
        fd = FileDialog.newInstance(2, FileDialog.TypeDialog.OPEN, dir, "*", this);
        fd.show(getSupportFragmentManager(), "");
    }

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
                else
                {
                    updateFileName();
                }
                break;
            case 2:
                if(!mapFragment.loadTrackFromFile(fileName))
                {
                    String msg=String.format(getText(R.string.cannot_load_file).toString(),fileName);
                    MessageDialog.newInstance(getText(R.string.Error).toString(),msg)
                            .show(getSupportFragmentManager(),"");
                }
                else
                {
                    updateFileName();
                };
                break;
        }
    }

    @Override
    public void onClickCancel(FileDialog dlg) {

    }
    //-------------------------------

    final int WARNINGS_AND_START_SERVICE =0, TRACK_MAY_BE_LOST_LOAD_FILE=1;
    //  MessageDialog.OnCloseListener
    @Override
    public void onClickOk(MessageDialog msg) {
        switch (msg.getDlgId())
        {
            case WARNINGS_AND_START_SERVICE://R.string.track_may_be_lost:
                     startTrackService();
                break;
            case TRACK_MAY_BE_LOST_LOAD_FILE://R.string.track_may_be_lost:
                    loadCurrentTrack();
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
