package andriod.iot.com.mqttserialtethering;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTService {
	String broker = "tcp://52.39.209.110:1883";
	String clientId = "arduino_lib";
	MqttConnectOptions conOps;
	MqttClient client;
    MemoryPersistence persistence = new MemoryPersistence();
    
    public MQTTService(String clientId) {
    	this.clientId = clientId;
    	
    }
	public void begin(String server, int port, String username, String password) throws Exception{
		broker = "tcp://" + server + ":" + port;
    	client = new MqttClient(broker, clientId, persistence);
			
		conOps = new MqttConnectOptions();
		conOps.setCleanSession(true);
		if (username != null && username.length() > 1) {
			conOps.setUserName(username);
			conOps.setPassword(password.toCharArray());
		}

	}

	public void connect() throws Exception{
        if (client != null) {
            client.connect(conOps);
        }

	}
	
	public void publish(String topic, String message)  throws Exception{
		MqttMessage mqttMessage = new MqttMessage();
		mqttMessage.setPayload(message.getBytes());
		mqttMessage.setQos(2);
		mqttMessage.setRetained(false);
		client.publish(topic, mqttMessage);

	}
	
	public void subscribe(String topicFilter, MqttCallback callback) throws Exception{
		client.subscribe(topicFilter);
		client.setCallback(callback);
	}
	public void disconnect() throws Exception{
		client.disconnect();
	}
	
	public boolean isConnected() {
        if (client == null) return false;
		return client.isConnected();
	}

	public void restart ()throws Exception{
		if (client.isConnected()==false){
			begin("52.39.209.110",1883,"krohak","");
			connect();
		}
	}
}
