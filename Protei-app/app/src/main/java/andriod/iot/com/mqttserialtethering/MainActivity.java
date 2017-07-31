package andriod.iot.com.mqttserialtethering;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity implements OnClickListener {
    private static final String TAG = "BluetoothTethering";
    private static final boolean D = true;

    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothService mChatService = null;
    private AccessGyroscope accessGyroscope = null;

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;


    TextView mSubscribeStatus;
    TextView mPublishStatus;
    Handler mHandler; // Our main handler that will receive callback notifications
    private final static int SUB_READ = 2; // used in bluetooth handler to identify message update
    private final static int PUB_READ = 3; // used in bluetooth handler to identify message status
    private final static int TV_STAT = 4; // for gyro
    private final static int BEND_LEVEL = 5;
    private final static int SAIL_LEVEL = 6;




    // Insert your server's MAC address
    private String address = Constant.ADDRESS;
    Button connect;
    TextView macAddress;

    //a TextView
    private TextView tv;

    private SensorManager sManager;

    private TextView bend;

    private TextView sail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        setContentView(R.layout.activity_main);
        connect = (Button) findViewById(R.id.button1);
        macAddress = (EditText) findViewById(R.id.editText1);
        connect.setOnClickListener(this);

        mSubscribeStatus = (TextView) findViewById(R.id.subscribeStatus);
        mPublishStatus = (TextView)findViewById(R.id.publishStatus);

        //get the TextView from the layout file
        tv = (TextView) findViewById(R.id.tv);
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        bend = (TextView) findViewById(R.id.bend);
        sail = (TextView) findViewById(R.id.sail);


        mHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == SUB_READ){
                    if(msg.arg1 == 1)
                        mSubscribeStatus.setText((String)(msg.obj));
                    else
                        mSubscribeStatus.setText("Failed");
                }

                if(msg.what == PUB_READ){
                    if(msg.arg1 == 1)
                        mPublishStatus.setText((String)(msg.obj));
                    else
                        mPublishStatus.setText("Failed");
                }

                if(msg.what == TV_STAT){
                    if(msg.arg1 == 1)
                        tv.setText((String)(msg.obj));
                    else
                        tv.setText("Failed");
                }

                if(msg.what == BEND_LEVEL){
                    if(msg.arg1 == 1)
                        bend.setText((String)(msg.obj));
                    else
                        bend.setText("Failed");
                }

                if(msg.what == SAIL_LEVEL){
                    if(msg.arg1 == 1)
                        sail.setText((String)(msg.obj));
                    else
                        sail.setText("Failed");
                }
            }
        };


    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "BT not enabled Finishing",
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (D)
            Log.e(TAG, "++ ON START ++");

        accessGyroscope = new AccessGyroscope(this, mHandler, sManager);

    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
        mChatService = new BluetoothService(this, mHandler);
        connectDevice(null, false);
        // Initialize the buffer for outgoing messages
    }

    private void connectDevice(Intent data, boolean secure) {

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public synchronized void onResume() {
        super.onResume();



        if (D)
            Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity
        // returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
                connectDevice(null, false);

            }
        }

        accessGyroscope.onResume();


    }

    @Override
    protected void onPause() {
        super.onPause();

        accessGyroscope.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();

        accessGyroscope.onStop();
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View arg0) {
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        address = macAddress.getText().toString();
        if (address != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                // Otherwise, setup the chat session
            } else {
                if (mChatService == null)
                    setupChat();
            }
        } else {
            Toast.makeText(this, "Please provide your Module MAC Address", Toast.LENGTH_SHORT)
                    .show();
        }
    }




}
