package dk.hotmovinglobster.dustytuba.bt;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import dk.hotmovinglobster.dustytuba.api.BtConnection;

/**
 * Class responsible for setup up a bluetooth connection
 * 
 * It assumes that bluetooth is already enabled on the device
 * 
 * @author Jesper
 *
 */
public class BluetoothConnectionManager {
	
	private String mac;
	private UUID uuid;
	private String sdp_name;
	
	private BluetoothAdapter mBTAdapter;
	
	private static final String LOG_TAG = "DustyTuba";
	
	private BtConnection mConnObject = null;
/*	
	public static BtConnection setupConnection(String mac, String uuid, String sdp_name) {
		BluetoothConnectionManager bcm = new BluetoothConnectionManager(mac, uuid, sdp_name);
		if (bcm.setupConnection()) {
			return bcm.getConnectionObject();
		} else {
			return null;
		}
	}
	*/
	public BluetoothConnectionManager(String mac, String uuid, String sdp_name) {
		this.mac = mac;
		this.uuid = UUID.fromString(uuid);
		this.sdp_name = sdp_name;
	}
	
	public boolean setupConnection() {
		mBTAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBTAdapter == null) {
		    return false;
		}
		// We assume that bluetooth already has been enabled
		if (!mBTAdapter.isEnabled()) {
			return false;
		}
		
		startListeningForConnection();
		connectToOtherDevice();
		
		
		return true;
	}
	
	private void startListeningForConnection() {
		AcceptThread a = new AcceptThread();
		a.start();
		
	}
	
	private void manageConnectedServerSocket(BluetoothSocket socket) {
		log_i("Server socket complete!");
		mConnObject = new BtConnection(socket);
	}
	
	private void connectToOtherDevice() {
		BluetoothDevice otherDevice = mBTAdapter.getRemoteDevice(mac);
		
		ConnectThread c = new ConnectThread(otherDevice);
		c.start();
	}

	public void manageConnectedClientSocket(BluetoothSocket socket) {
		log_i("Client socket complete!");
		mConnObject = new BtConnection(socket);
	}
	
	public BtConnection getConnectionObject() {
		return mConnObject;
	}

	private void log_e(String msg) {
		Log.e(LOG_TAG, "BCM ("+mBTAdapter.getAddress()+"): " + msg);
	}
	
	private void log_i(String msg) {
		Log.i(LOG_TAG, "BCM ("+mBTAdapter.getAddress()+"): " + msg);
	}
	
	private class AcceptThread extends Thread {
		private BluetoothServerSocket mBTServerSocket;
		
		public AcceptThread() {
			try {
				mBTServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord(sdp_name, uuid);
			} catch (IOException e) {
				log_e("BCM: AcceptThread(): IOException");
				e.printStackTrace();
				return;
			}
		}
		
		public void run() {
			BluetoothSocket socket = null;

	        while (true) {
	            try {
	                socket = mBTServerSocket.accept();
	            } catch (IOException e) {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	                manageConnectedServerSocket(socket);
	                try {
						mBTServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;
	            }
	        }
	    }
		
		public void cancel() {
	        try {
	            mBTServerSocket.close();
	        } catch (IOException e) { }
	    }
	 }

	private class ConnectThread extends Thread {
		private final BluetoothDevice mBTOtherDevice;
		private final BluetoothSocket mBTClientSocket;
		
		public ConnectThread(BluetoothDevice otherDevice) {
			mBTOtherDevice = otherDevice;
			
			BluetoothSocket tmp = null;
			
			try {
				tmp = mBTOtherDevice.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			mBTClientSocket = tmp;
		}
		
		public void run() {
			try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mBTClientSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mBTClientSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }

	        // Do work to manage the connection (in a separate thread)
	        manageConnectedClientSocket(mBTClientSocket);			
		}

		public void cancel() {
	        try {
	            mBTClientSocket.close();
	        } catch (IOException e) { }
	    }
	}

}
