package com.kivsw.forjoggers.helper;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides magnet azimuth
 */
public class CompassHelper
implements SensorEventListener {

    private Context context;
    private Sensor magneticSensor, accelerimeterSensor;
    private SensorManager mSensorManager;
    private CompassDirectionListener compassDirectionListener;

    LinkedList<DirectionItem> angles;
    final int MAX_AGE=1500;

    public interface CompassDirectionListener
    {
        void onCompassDirection(float azimuth); // azimuth in degree
    };

    public CompassHelper(Context context) throws Exception
    {
        this.context = context.getApplicationContext();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        magneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if(magneticSensor==null)
            throw new Exception("Magnetic sensor is unavailable");
        accelerimeterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerimeterSensor==null)
            throw new Exception("Accelerimeter sensor is unavailable");

        angles = new LinkedList<DirectionItem>();
       // List<Sensor> sensors=mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
    };

    public void start(CompassDirectionListener compassDirectionListener)
    {
        this.compassDirectionListener = compassDirectionListener;
        mSensorManager.registerListener(this,accelerimeterSensor,SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this,magneticSensor,SensorManager.SENSOR_DELAY_UI);
    }
    public void stop()
    {
        mSensorManager.unregisterListener(this);
    };


    static public boolean isAvaliable(Context context)
    {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!=null &&
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!=null;
    }

    void addNewAngle(double a)
    {
        // adds the angle to the list
        if(angles.size()>0)
        {
            double lst=angles.peekLast().val;

            a= correctAngle(lst,a);

        }
        angles.add(DirectionItem.valueOf(a));

        // remove ald values
        while(angles.peekFirst().age()>MAX_AGE)
            angles.remove(0);

        // sends an average value
        if(compassDirectionListener!=null)
            compassDirectionListener.onCompassDirection((float) calcAvAngle());
    }

    static public double correctAngle(double prevAngle, double nextAngle)
    {
        if(prevAngle>nextAngle)
        {
            while((prevAngle-nextAngle)>180)
                nextAngle+=360;
        }
        else
        {
            while((nextAngle-prevAngle)>180)
                nextAngle-=360;
        }
        return nextAngle;
    }

    double calcAvAngle()
    {
        if(angles.size()==0) return 0;

        double sum=0;
        Iterator<DirectionItem> i=angles.iterator();
        while(i.hasNext())
            sum += i.next().val;
        return sum/angles.size();
    }
    //------------------------------------------------
    // SensorEventListener
    float accel[]=null;

    //http://stackoverflow.com/questions/13293687/compass-in-android
    @Override
    public void onSensorChanged(SensorEvent event) {
        /*float x=event.values[0];
        float y=event.values[1];

        // gets angle
        double a=(Math.atan2(x,y)/Math.PI*180);

        addNewAngle(a);*/
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {
            accel = event.values.clone();
        }
        else
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            if (accel == null) return;

            float magnetic[] = event.values;

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, accel, magnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                double azimuth = orientation[0] / Math.PI * 180;
                if(accel[2]>0) azimuth=-azimuth; // correct sign if the phone has a screen-down orientation
                addNewAngle(azimuth);
            }
            /*        float x=event.values[0];
                    float y=event.values[1];

                    // gets angle
                    double a=(Math.atan2(x,y)/Math.PI*180);

                    addNewAngle(a);*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    ///--------------------------------------
    static class DirectionItem
    {
        double val;
        long t;

        static public DirectionItem valueOf(double v)
        {
            return new DirectionItem(v);
        }
        private DirectionItem(double v)
        {
            t= SystemClock.elapsedRealtime();
            val=v;
        }

        public long age()
        {
            return SystemClock.elapsedRealtime()-t;
        };

    }
}
