package andriod.iot.com.mqttserialtethering;

import android.content.Context;
import android.os.Handler;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class ArduinoCallback implements MqttCallback {

	private final static int SUB_READ = 2;

	Context thisContext;
	private Handler mHandler;

	public ArduinoCallback(Context context,Handler handler){

		thisContext = context;
		mHandler = handler;
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		mHandler.obtainMessage(SUB_READ, 1, -1, "Connection Lost")
				.sendToTarget();
		BluetoothService.command =1;
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		BluetoothService.arrivedMessage.put(topic, message);
	}

}
