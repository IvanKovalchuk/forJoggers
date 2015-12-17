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

import com.kivsw.dialog.FileDialog;
import com.kivsw.dialog.MessageDialog;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
implements  FileDialog.OnCloseListener, TrackingServiceEventReceiver.OnChangingListener,
            MessageDialog.OnCloseListener,SettingsFragment.onSettingsCloseListener
{

    final static String ACTION_RECEIVE_TRACK="com.kivsw.forjoggers.ACTION_RECIEVE_TRACK";

    private ViewPager pager;
    SettingsFragment settingsFragment=null;
    MapFragment mapFragment=null;
    AnalysingFragment analysingFragment=null;

    SettingsKeeper settings;

    TrackingServiceEventReceiver trackingServiceEventReceiver=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager =(ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new MyOnPageChange());
       // mapFragment = pager.insta

        /*pager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });*/

        //mapFragment = (MapFragment)getSupportFragmentManager().findFragmentById(R.id.mapFragment);


        settings=SettingsKeeper.getInstance(this);

       if(savedInstanceState!=null)
       {
           pager.setCurrentItem(savedInstanceState.getInt("pageIndex"));
       }
        else
       {
           pager.setCurrentItem(1);
       }

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("pageIndex", pager.getCurrentItem());

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
           /* case R.id.action_settings:
                SettingsFragment.newInstance(new SettingsFragment.onSettingsCloseListener(){
                    @Override
                    public void onSettingsChanged() {
                        analysingFragment.onSettingsChanged();
                        mapFragment.onSettingsChanged();
                    }
                }).show(getSupportFragmentManager(),"");
                return true;*/

            case R.id.action_save_track:
                if(!TrackingService.isWorking)
                   saveCurrentTrack();
                return true;

            case R.id.action_load_track:
                if(!TrackingService.isWorking) {
                    if (CurrentTrack.getInstance(this).needToBeSaved()) {
                        MessageDialog.newInstance(TRACK_MAY_BE_LOST_LOAD_FILE,
                                getText(R.string.Warning).toString(),
                                getText(R.string.track_may_be_lost).toString(),
                                this)
                                .show(getSupportFragmentManager(), "");
                    } else {
                        loadCurrentTrack();
                    }
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
    @Override
    protected void onStart()
    {
        super.onStart();
        trackingServiceEventReceiver = TrackingServiceEventReceiver.createAndRegister(this, this);

    }
    //----------------------------------------------------------
    @Override
    protected void onStop()
    {
        trackingServiceEventReceiver.unregister();
        super.onStop();

    }
    //----------------------------------------------------------
    @Override
    public void onBackPressed()
    {
        if(pager.getCurrentItem()==1)
            super.onBackPressed();
        else
            pager.setCurrentItem(1,true);
    }
    //----------------------------------------------------------
    private void processIntent(Intent i)
    {

    };







    private void saveCurrentTrack()
    {
        String dir=settings.getLastPath();
        if(dir==null)
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();
        FileDialog fd;
        fd=FileDialog.newInstance(1, FileDialog.TypeDialog.SAVE, dir, "*.gpx", this);
        fd.show(getSupportFragmentManager(), "");
    }

    private void loadCurrentTrack()
    {
        String dir=settings.getLastPath();
        if(dir==null)
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();
        FileDialog fd;
        fd = FileDialog.newInstance(2, FileDialog.TypeDialog.OPEN, dir, "*.gpx", this);
        fd.show(getSupportFragmentManager(), "");
    }

    //-------------------------------
    // FileDialog.OnCloseListener
    @Override
    public void onClickOk(FileDialog dlg, String fileName) {

        boolean success=false;
        switch(dlg.getDlgId())
        {
            case 1:
                if(!fileName.matches(".*\\.gpx$"))
                    fileName=fileName+".gpx";
                success=mapFragment.saveTrackToFile(fileName);
                if(!success)
                {
                    String msg=String.format(getText(R.string.cannot_save_file).toString(),fileName);
                    MessageDialog.newInstance(getText(R.string.Error).toString(),msg)
                            .show(getSupportFragmentManager(), "");
                }
                break;
            case 2:
                success=mapFragment.loadTrackFromFile(fileName);
                if(!success)
                {
                    String msg=String.format(getText(R.string.cannot_load_file).toString(),fileName);
                    MessageDialog.newInstance(getText(R.string.Error).toString(),msg)
                            .show(getSupportFragmentManager(), "");
                }
                break;
        }
        if(success) {
            File f = new File(fileName);
            if (f.exists())
                settings.setLastPath(f.getParent());
        }
    }

    @Override
    public void onClickCancel(FileDialog dlg) {

    }
    //-------------------------------
    //  MessageDialog.OnCloseListener
    final static int TRACK_MAY_BE_LOST_LOAD_FILE=1;

    @Override
    public void onClickOk(MessageDialog msg) {
        switch (msg.getDlgId())
        {

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
    // SettingsFragment.onSettingsCloseListener
    @Override
    public void onSettingsChanged() {
        analysingFragment.onSettingsChanged();
        mapFragment.onSettingsChanged();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Fragment getItem(int position) {
            //return ArrayListFragment.newInstance(position);
            Fragment res=null;
            switch(position)
            {
                case 0:
                    res = new SettingsFragment();
                    break;
                case 1:
                    res=new MapFragment();
                    break;
                case 2:
                    res=new AnalysingFragment();
                    break;
            }
            return res;
        }
    }
    //--------------------------------------------------------------------------
//TrackingServiceEventReceiver.OnChangingListener
    public void onServiceStatusChanged(boolean isRunning)
    {
        supportInvalidateOptionsMenu();
        mapFragment.onStartStopTrackingService(isRunning);
    }
    class MyOnPageChange extends ViewPager.SimpleOnPageChangeListener
    {
        Fragment currentPage =null;

        @Override
        public void onPageSelected(int position)
        {
            if(currentPage !=null && (currentPage instanceof CustomPagerView.IonPageAppear))
                ((CustomPagerView.IonPageAppear) currentPage).onPageDisappear();

            switch(position)
            {
                case 0:currentPage=settingsFragment;
                    break;
                case 1:currentPage=mapFragment;
                    break;
                case 2:currentPage=analysingFragment;
                    break;
                default: currentPage=null;
            };

            if(currentPage !=null && (currentPage instanceof CustomPagerView.IonPageAppear))
                ((CustomPagerView.IonPageAppear) currentPage).onPageAppear();
        }

    }


}
