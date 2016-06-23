package com.kivsw.forjoggers.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kivsw.dialog.FileDialog;
import com.kivsw.dialog.MessageDialog;
import com.kivsw.forjoggers.BuildConfig;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;
import com.kivsw.forjoggers.ui.chart.AnalysingFragment;
import com.kivsw.forjoggers.ui.map.MapFragment;
import com.kivsw.forjoggers.ui.settings.SettingsFragment;

import java.io.File;


public class MainActivity extends ActionBarActivity
implements  FileDialog.OnCloseListener,
            MessageDialog.OnCloseListener,
            View.OnClickListener,
        MainActivityContract.IView

{

    final public static String TAG="MainActivity";
    final static String ACTION_SHOW_MSG="com.kivsw.forjoggers.ACTION_SHOW_MSG";

    private ViewPager pager;
    public SettingsFragment settingsFragment=null;
    public MapFragment mapFragment=null;
    public AnalysingFragment analysingFragment=null;
    MainActivityContract.IPresenter IPresenter = null;
    android.support.design.widget.TabLayout tabLayout;

    SettingsKeeper settings;

    //TrackingServiceEventReceiver trackingServiceEventReceiver=null;



    static public void showMessage(Context cnt, int msgId, String title, String msg)
    {
        Intent i=new Intent(ACTION_SHOW_MSG);
        i.setClass(cnt, MainActivity.class);
        i.putExtra("msgId",msgId);
        i.putExtra("title",title);
        i.putExtra("message",msg);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        cnt.startActivity(i);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initStrictMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager =(ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new MyOnPageChange());

        tabLayout=(android.support.design.widget.TabLayout)findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);
        tabLayout.getTabAt(0).setIcon(R.drawable.settings);
        tabLayout.getTabAt(1).setIcon(R.drawable.world_map_b);
        tabLayout.getTabAt(2).setIcon(R.drawable.line_chart);
       /* tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition(),true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition(),true);
            }
        });//*/

        settings=SettingsKeeper.getInstance(this);

       if(savedInstanceState!=null)
       {
           pager.setCurrentItem(savedInstanceState.getInt("pageIndex"));
       }
        else
       {
           pager.setCurrentItem(1);
       }

        IPresenter = MainActivityIPresenter.getInstance(this);
        IPresenter.onCreateActivity(this);

        onNewIntent(getIntent());

        checkDozeMode();
    }
//----------------------------------------------------------
    @Override
    protected void onNewIntent(Intent i)
    {
        super.onNewIntent(i);

        if(i.getAction().equals(ACTION_SHOW_MSG))
        {
            String title = i.getExtras().getString("title");
            String message = i.getExtras().getString("message");
            int msgId=i.getExtras().getInt("msgId");
            MessageDialog.newInstance(msgId,title, message, this)
                    .show(getSupportFragmentManager(), "");
        }
    }
    //----------------------------------------------------------
    @Override
    protected void onDestroy()
    {
        IPresenter.onDestroyActivity();
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
        //super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //----------------------------------------------------------
     @Override
    public boolean onPrepareOptionsMenu(Menu menu)
     {

         super.onPrepareOptionsMenu(menu);
         boolean isTracking = IPresenter.isTracking();
         MenuItem item;//=menu.findItem(R.id.action_show_my_location);

         item=menu.findItem(R.id.action_load_track);
         item.setEnabled(!isTracking);

         item=menu.findItem(R.id.action_save_track);
         item.setEnabled(!isTracking);

         boolean hasTrackData = IPresenter.hasTrackData();
         item=menu.findItem(R.id.action_show_my_track);
         item.setEnabled(hasTrackData);

         item=menu.findItem(R.id.action_emulate_my_track);
         item.setVisible(/*hasTrackData &&*/ !isTracking && BuildConfig.DEBUG);

         item=menu.findItem(R.id.action_quit);
         item.setVisible(settings.getKeepBackGround() && !IPresenter.isTracking());

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

            case R.id.action_save_track:
                if(!IPresenter.isTracking())
                   saveCurrentTrack();
                return true;

            case R.id.action_load_track:
                if(!IPresenter.isTracking() ) {
                    if (IPresenter.trackNeedToBeSaved()) {
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


            case R.id.action_show_my_track:
                 IPresenter.actionShowCurrentTrack();
                return true;
                /*ArrayList<Location> points=CurrentTrack.getInstance(this).getGeoPoints();
                if(points!=null && points.size()>0)
                   mapFragment.showLocation(points.get(0).getLatitude(), points.get(0).getLongitude());
                return true;*/

            case R.id.action_emulate_my_track:
                IPresenter.actionAnimateTrack();
                //mapFragment.animateTrack();
                break;

            case R.id.action_quit:
                IPresenter.actionExit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------
    @Override
    protected void onStart()
    {
        super.onStart();
        IPresenter.onStartActivity();
        setVolumeControlStream (AudioManager.STREAM_MUSIC );


    }
    //----------------------------------------------------------
    @Override
    protected void onStop()
    {
        IPresenter.onStopActivity();
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

    /**
     * Asks the user to turn off Doze mode for this application
     */
    @TargetApi(23)
    private void checkDozeMode()
    {
        if(Build.VERSION.SDK_INT<23) return;

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        String packageName = this.getPackageName();
        if(pm.isIgnoringBatteryOptimizations(packageName) )
             return;

        //Intent i=new Intent( Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS );
        Intent i=new Intent( Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + packageName) );

        startActivity(i);
    }

    private void initStrictMode()
    {
      /*  if (!BuildConfig.DEBUG) return;

            // Tell Android what thread issues you want to detect and what to do when found.
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()    // or use .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            // Tell Android what VM issues you want to detect and what to do when found.
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()       // Log the problem
                    .penaltyDeath()     // Then kill the app
                    .build());*/

    };

    private void saveCurrentTrack()
    {
        Fragment fr=getSupportFragmentManager().findFragmentByTag("FileDialog");
        if(fr!=null && fr.isAdded())
            return;

        String dir=settings.getLastDirPath();
        if(dir==null)
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();
        FileDialog fd;
        fd=FileDialog.newInstance(1, FileDialog.TypeDialog.SAVE, dir, "*.gpx", this);
        fd.show(getSupportFragmentManager(), "FileDialog");
    }

    private void loadCurrentTrack()
    {
        Fragment fr=getSupportFragmentManager().findFragmentByTag("FileDialog");
        if(fr!=null && fr.isAdded())
            return;

        String dir=settings.getLastDirPath();
        if(dir==null)
            dir=Environment.getExternalStorageDirectory().getAbsolutePath();
        FileDialog fd;
        fd = FileDialog.newInstance(2, FileDialog.TypeDialog.OPEN, dir, "*.gpx", this);
        fd.show(getSupportFragmentManager(), "FileDialog");
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
                success= IPresenter.actionSaveTrack(fileName);//mapFragment.saveTrackToFile(fileName);
                if(!success)
                {
                    String msg=String.format(getText(R.string.cannot_save_file).toString(),fileName);
                    MessageDialog.newInstance(getText(R.string.Error).toString(),msg)
                            .show(getSupportFragmentManager(), "");
                }
                break;
            case 2:
                success= IPresenter.actionLoadTrack(fileName);//mapFragment.loadTrackFromFile(fileName);
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
                settings.setLastDirPath(f.getParent());
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

    //------------------------------------
    // View.OnClickListener
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

        }
    }

    //--------------------------------------------------------------------------

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


    class MyOnPageChange extends ViewPager.SimpleOnPageChangeListener
    {
        Fragment currentPage =null;

        @Override
        public void onPageSelected(int position)
        {
            if(currentPage !=null && (currentPage instanceof CustomPagerView.IonPageAppear))
                ((CustomPagerView.IonPageAppear) currentPage).onPageDisappear();

            tabLayout.setScrollPosition(position,0f,true);
            switch(position)
            {
                case 0:
                    currentPage=settingsFragment;
                    break;
                case 1:
                    currentPage=mapFragment;
                    break;
                case 2:
                    currentPage=analysingFragment;
                    break;
                default: currentPage=null;
            };



            if(currentPage !=null && (currentPage instanceof CustomPagerView.IonPageAppear))
                ((CustomPagerView.IonPageAppear) currentPage).onPageAppear();
        }

    }


}
