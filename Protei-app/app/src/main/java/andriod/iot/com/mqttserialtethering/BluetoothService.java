/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package andriod.iot.com.mqttserialtethering;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import android.os.Handler;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class BluetoothService {
	// Debugging
	private static final String TAG = "RobotBluetoothService";
	private static final boolean D = true;
	private static boolean SEND_DATA = false;

	static String SubscribeStatus;
	static String PublishStatus;
	
	public static ConcurrentHashMap<String, MqttMessage> arrivedMessage = new ConcurrentHashMap<String, MqttMessage>();
	private List<String> subscribedQueues = new ArrayList<String>();
	public static int command=0;

	MQTTService mqttSerivce;
	// Name for the SDP record when creating server socket
	private static final String NAME_INSECURE = "BluetoothChatInsecure";

	// Unique UUID for this application
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Member fields
	private final BluetoothAdapter mAdapter;
	private AcceptThread mInsecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	Context thisContext;
	private Handler mHandler;
	private final static int SUB_READ = 2; // used in bluetooth handler to identify message update
	private final static int PUB_READ = 3; // used in bluetooth handler to identify message status
	private final static int BEND_LEVEL = 5;
	private final static int SAIL_LEVEL = 6;



	public BluetoothService(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		thisContext = context;
		mHandler = handler;

	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		// mHandler.obtainMessage(BluetoothChat.MESSAGE_STATE_CHANGE, state,
		// -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {
		if (D)
			Log.d(TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_LISTEN);

		if (mInsecureAcceptThread == null) {
			mInsecureAcceptThread = new AcceptThread(false);
			mInsecureAcceptThread.start();
		}
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(BluetoothDevice device, boolean secure) {
		if (D)
			Log.d(TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device, secure);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device, final String socketType) {
		if (D)
			Log.d(TAG, "connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		// Message msg =
		// mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
		// Bundle bundle = new Bundle();
		// bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
		// msg.setData(bundle);
		// mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see andriod.iot.com.mqttserialtethering.BluetoothService.ConnectedThread#write(byte[])
	 */
	public synchronized void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed() {
		// Send a failure message back to the Activity
		// Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
		// Bundle bundle = new Bundle();
		// bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
		// msg.setData(bundle);
		// mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		BluetoothService.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		// Send a failure message back to the Activity
		// Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
		// Bundle bundle = new Bundle();
		// bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
		// msg.setData(bundle);
		// mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		BluetoothService.this.start();
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread {
		// The local server socket
		private final BluetoothServerSocket mmServerSocket;
		private String mSocketType;

		public AcceptThread(boolean secure) {
			BluetoothServerSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Create a new listening server socket
			try {
				tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
						NAME_INSECURE, MY_UUID_INSECURE);
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
			}
			mmServerSocket = tmp;
		}

		public void run() {
			if (D)
				Log.d(TAG, "Socket Type: " + mSocketType
						+ "BEGIN mAcceptThread" + this);
			setName("AcceptThread" + mSocketType);

			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED) {
				try {
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e) {
					Log.e(TAG, "Socket Type: " + mSocketType
							+ "accept() failed", e);
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					synchronized (BluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							// Situation normal. Start the connected thread.
							connected(socket, socket.getRemoteDevice(),
									mSocketType);
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							// Either not ready or already connected. Terminate
							// new socket.
							try {
								socket.close();
							} catch (IOException e) {
								Log.e(TAG, "Could not close unwanted socket", e);
							}
							break;
						}
					}
				}
			}
			if (D)
				Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

		}

		public void cancel() {
			if (D)
				Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
			try {
				mmServerSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "Socket Type" + mSocketType
						+ "close() of server failed", e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;

		public ConnectThread(BluetoothDevice device, boolean secure) {
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {

				tmp = device
						.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
			} catch (IOException e) {
				Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
			}
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e) {
				// Close the socket
				try {
					mmSocket.close();
				} catch (IOException e2) {
					Log.e(TAG, "unable to close() " + mSocketType
							+ " socket during connection failure", e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothService.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect " + mSocketType
						+ " socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;




		public ConnectedThread(BluetoothSocket socket, String socketType) {
			Log.d(TAG, "create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "temp sockets not created", e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			//turn=0;
		}

		private String readBuffer(InputStream mmInStream, int data) throws IOException {
			List<Byte> byteArray = new ArrayList<Byte>();

			while (true) {
				data = mmInStream.read();
				if (data == 245) {
					break;
				}
				byteArray.add((byte) data);
			}
			Byte[] buffer = new Byte[byteArray.size()];
			byteArray.toArray(buffer);
			byte[] pBuffer = ArrayUtils.toPrimitive(buffer);
			return new String(pBuffer);
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectedThread");
			String clientId = "arduino_lib";
			String hostname = "52.39.209.110";
			String portnumber = "1883";
			String publishQueue = null;
			String publishPayload = null;
			String subscribeQueue = null;
			String username = "krohak";
			String password = null;
			int turn=0;
			int sail=0;




			// Keep listening to the InputStream while connected
			while (true) {
				try {

					// Read from the InputStream
					// using a fixed width

					if (mmInStream.available() > 0) {
						int data = mmInStream.read();
						if (data == 1) {
							clientId = readBuffer(mmInStream, data);
							mqttSerivce = new MQTTService(clientId);
						} else if (data == 2) {
							hostname = readBuffer(mmInStream, data);
						} else if (data == 3) {
							portnumber = readBuffer(mmInStream, data);
						} else if (data == 4) {
							username = readBuffer(mmInStream, data);
						} else if (data == 5) {
							password = readBuffer(mmInStream, data);
						}else if (data == 6) {
							mqttSerivce.begin(hostname, Integer.valueOf(portnumber), username, password);
							mqttSerivce.connect();
						} else if (data == 7) {
							mqttSerivce.disconnect();
						} else if (data == 8) {
							publishQueue = readBuffer(mmInStream, data);
						} else if (data == 9) {
							publishPayload = readBuffer(mmInStream, data);
                            if (mqttSerivce != null) {
                                mqttSerivce.publish(publishQueue, publishPayload);
								PublishStatus = publishQueue + " : " + publishPayload;

								mHandler.obtainMessage(PUB_READ, 1, -1, PublishStatus)
										.sendToTarget();
								turn=0;
								sail=0;

								String turn_str = "Rudder: " + Integer.toString(turn);
								String sail_str = "Sail: " + Integer.toString(sail);

								mHandler.obtainMessage(BEND_LEVEL, 1, -1, turn_str)
										.sendToTarget();

								mHandler.obtainMessage(SAIL_LEVEL, 1, -1, sail_str)
										.sendToTarget();
                            }
						} else if (data == 10) {
							subscribeQueue = readBuffer(mmInStream, data);
							subscribedQueues.add(subscribeQueue);
							ArduinoCallback acb;
							acb = new ArduinoCallback(thisContext,mHandler);
							mqttSerivce.subscribe(subscribeQueue, acb);

						}

					} else {
						SystemClock.sleep(100);
						if (mqttSerivce != null) {
							if (!mqttSerivce.isConnected()) {
								SystemClock.sleep(200);
								mqttSerivce.connect();
							}
						}
					}
					if (arrivedMessage.size() > 0) {
						 
						for (String key : arrivedMessage.keySet()) {
							String val=arrivedMessage.get(key).toString();
							String json  = "{\"topic\":\""+key+"\",\"payload\":\""+val+"\"}";
							write(json.getBytes());
							SubscribeStatus = json;

							mHandler.obtainMessage(SUB_READ, 1, -1, SubscribeStatus)
									.sendToTarget();
							if (val.equals("r")){
								turn+=4;
							}
							else if (val.equals("l")){
								turn-=4;
							}
							else if (val.equals("u")){
								sail+=4;
							}
							else if (val.equals("dow")){
								sail-=4;
							}

							//turn+=8;

							String turn_str = "Rudder: " + Integer.toString(turn);
							String sail_str = "Sail: " + Integer.toString(sail);

							mHandler.obtainMessage(BEND_LEVEL, 1, -1, turn_str)
									.sendToTarget();

							mHandler.obtainMessage(SAIL_LEVEL, 1, -1, sail_str)
									.sendToTarget();

						}
						write(new byte[]{(byte)254});
						arrivedMessage.clear();
						
					}

					if(command==1){
						mqttSerivce = new MQTTService("Javss");
						mqttSerivce.begin(hostname, Integer.valueOf(portnumber), username, password);
						mqttSerivce.connect();
						subscribeQueue = "helloout";
						ArduinoCallback acb;
						acb = new ArduinoCallback(thisContext,mHandler);
						mqttSerivce.subscribe(subscribeQueue, acb);
						mqttSerivce.subscribe("mar", acb);

						//mqttSerivce.restart();
						command=0;
						//BluetoothService.this.start();
					}

					// Send the obtained bytes to the UI Activity
					// mHandler.obtainMessage(BluetoothChat.MESSAGE_READ, bytes,
					// -1, buffer) .sendToTarget();
				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					// Start the service over to restart listening mode
					BluetoothService.this.start();
					break;
				}catch(Exception exception) {

                }

			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer) {
			try {

				mmOutStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}
}
