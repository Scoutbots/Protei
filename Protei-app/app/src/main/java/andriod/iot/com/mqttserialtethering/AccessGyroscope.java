package andriod.iot.com.mqttserialtethering;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Message;
import android.widget.TextView;
import android.os.Handler;

public class AccessGyroscope  implements SensorEventListener
{


    //the Sensor Manager
    private SensorManager sManager;

    Context thisContext;
    private Handler mHandler;

    private final static int TV_STAT = 4; // for gyro

    public AccessGyroscope(Context context, Handler handler, SensorManager smanager ) {

        mHandler = handler;
        sManager = smanager;
        thisContext = context;

    }



    //when this Activity starts

    protected void onResume()
    {

		/*register the sensor listener to listen to the gyroscope sensor, use the
		 * callbacks defined in this class, and gather the sensor information as
		 * quick as possible*/
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_FASTEST);

    }

    //When this Activity isn't visible anymore


    protected void onPause() {


        // to stop the listener and save battery
        sManager.unregisterListener(this);
    }


    protected void onStop()
    {
        //unregister the sensor listener
        sManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //if sensor is unreliable, return void
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }

        String tv_Text= ("Orientation X (Roll) :"+ Float.toString(event.values[2]) +"\n"+
                "Orientation Y (Pitch) :"+ Float.toString(event.values[1]) +"\n"+
                "Orientation Z (Yaw) :"+ Float.toString(event.values[0]));

        mHandler.obtainMessage(TV_STAT, 1, -1, tv_Text)
                .sendToTarget();

        //else it will output the Roll, Pitch and Yawn values


    }
}