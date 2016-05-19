package com.kivsw.forjoggers.ui;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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
import com.kivsw.forjoggers.BuildConfig;
import com.kivsw.forjoggers.R;
import com.kivsw.forjoggers.helper.SettingsKeeper;

import java.io.File;


public class MainActivity extends ActionBarActivity
implements  FileDialog.OnCloseListener,
            MessageDialog.OnCloseListener
{

    final public static String TAG="MainActivity";
    final static String ACTION_SHOW_MSG="com.kivsw.forjoggers.ACTION_SHOW_MSG";

    private ViewPager pager;
    public SettingsFragment settingsFragment=null;
    public MapFragment mapFragment=null;
    public AnalysingFragment analysingFragment=null;
    MainActivityPresenter presenter = null;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager =(ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        pager.addOnPageChangeListener(new MyOnPageChange());

        settings=SettingsKeeper.getInstance(this);

       if(savedInstanceState!=null)
       {
           pager.setCurrentItem(savedInstanceState.getInt("pageIndex"));
       }
        else
       {
           pager.setCurrentItem(1);
       }

        presenter=MainActivityPresenter.getInstance(this);
        presenter.onCreateActivity(this);

        onNewIntent(getIntent());
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
        presenter.onDestroyActivity();
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

         boolean isTracking = presenter.isTracking();
         MenuItem item;//=menu.findItem(R.id.action_show_my_location);

         item=menu.findItem(R.id.action_load_track);
         item.setEnabled(!isTracking);

         item=menu.findItem(R.id.action_save_track);
         item.setEnabled(!isTracking);

         boolean hasTrackData = presenter.hasTrackData();
         item=menu.findItem(R.id.action_show_my_track);
         item.setEnabled(hasTrackData);

         item=menu.findItem(R.id.action_emulate_my_track);
         item.setVisible(/*hasTrackData &&*/ !isTracking && BuildConfig.DEBUG);

         item=menu.findItem(R.id.action_quit);
         item.setVisible(settings.getKeepBackGround() && !presenter.isTracking());

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
                if(!presenter.isTracking())
                   saveCurrentTrack();
                return true;

            case R.id.action_load_track:
                if(!presenter.isTracking() ) {
                    if (presenter.trackNeedToBeSaved()) {
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
                 presenter.actionShowCurrentTrack();
                return true;
                /*ArrayList<Location> points=CurrentTrack.getInstance(this).getGeoPoints();
                if(points!=null && points.size()>0)
                   mapFragment.showLocation(points.get(0).getLatitude(), points.get(0).getLongitude());
                return true;*/

            case R.id.action_emulate_my_track:
                presenter.actionAnimateTrack();
                //mapFragment.animateTrack();
                break;

            case R.id.action_quit:
                presenter.actionExit();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //----------------------------------------------------------
    @Override
    protected void onStart()
    {
        super.onStart();
        presenter.onStartActivity();
        setVolumeControlStream (AudioManager.STREAM_MUSIC );

    }
    //----------------------------------------------------------
    @Override
    protected void onStop()
    {
        presenter.onStopActivity();
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
                success=presenter.actionSaveTrack(fileName);//mapFragment.saveTrackToFile(fileName);
                if(!success)
                {
                    String msg=String.format(getText(R.string.cannot_save_file).toString(),fileName);
                    MessageDialog.newInstance(getText(R.string.Error).toString(),msg)
                            .show(getSupportFragmentManager(), "");
                }
                break;
            case 2:
                success=presenter.actionLoadTrack(fileName);//mapFragment.loadTrackFromFile(fileName);
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
//TrackingServiceEventReceiver.OnChangingListener
    public void onServiceStatusChanged(boolean isRunning)
    {
        supportInvalidateOptionsMenu();
        //mapFragment.onStartStopTrackingService(isRunning);
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
