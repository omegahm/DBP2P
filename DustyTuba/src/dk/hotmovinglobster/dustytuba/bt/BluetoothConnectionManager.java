package dk.hotmovinglobster.dustytuba.bt;

import java.io.IOException;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;
import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.api.BtConnection;

/**
 * Class responsible for setup up a bluetooth connection
 * 
 * It assumes that bluetooth is already enabled on the device
 * 
 * @author Jesper
 *
 */
public class BluetoothConnectionManager extends BroadcastReceiver {
	
	private String mac;
	private UUID uuid;
	private String sdp_name;
	
	private final BluetoothAdapter mBTAdapter;
	
	private BtConnection mConnObject;
	
	private enum ConnectedAs { None, Client, Server };
	private ConnectedAs connectedAs;
	
	private AcceptThread acceptThread;
	private ConnectThread connectThread;
	
	private IntentFilter pairingIntentFilter;
	
	private boolean done = false;
	public boolean isDone() {
		return this.done;
	}

	private Context mContext;
	
	private AlertDialog pairingHelpDialog;
	
	public BluetoothConnectionManager(Context context, String mac, String uuid, String sdp_name) {
		this.mContext = context;
		this.mac = mac;
		this.uuid = UUID.fromString(uuid);
		this.sdp_name = sdp_name;
		this.mBTAdapter = BluetoothAdapter.getDefaultAdapter();
	}
	
	public synchronized boolean setupConnection() {
		log_v("setupConnection()");
		if (mBTAdapter == null) {
		    return false;
		}
		// We assume that bluetooth already has been enabled
		if (!mBTAdapter.isEnabled()) {
			return false;
		}
		
		pairingIntentFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		pairingIntentFilter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
		pairingIntentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		pairingIntentFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
		mContext.registerReceiver(this, pairingIntentFilter);
		
		this.mConnObject = null;
		this.connectedAs = ConnectedAs.None;
		this.done = false;
		
		initiateConnection();
		
		log_i("setupConnection() end");
		return true;
	}
	
	private synchronized void initiateConnection() {
		log_d("initiateConnection()");
		
		mBTAdapter.cancelDiscovery();
		
		acceptThread  = new AcceptThread(); 
		BluetoothDevice otherDevice = mBTAdapter.getRemoteDevice(mac);
		log_i("initiateConnection ("+otherDevice.getBondState()+")");
		connectThread = new ConnectThread(otherDevice); 

		connectToOtherDevice();
		startListeningForConnection();
		
		log_v("initiateConnection() end");
	}
	
	private synchronized void doneAndCleanUp() {
		log_v("doneAndCleanUp()");
		done = true;
		try {
			mContext.unregisterReceiver(this);
		} catch (IllegalArgumentException e) {
			// Hits if no receiver was registered - ignore
		}
		acceptThread.cancel();
		acceptThread.stop();
		connectThread.cancel();
		connectThread.stop();
		log_v("doneAndCleanUp() end");
	}
	
	private void log_v(String msg) {
		Log.v(BtAPI.LOG_TAG, "BluetoothConnectionManager: " + msg);
	}
	
	private void log_d(String msg) {
		Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: " + msg);
	}
	
	private void log_i(String msg) {
		Log.i(BtAPI.LOG_TAG, "BluetoothConnectionManager: " + msg);
	}
	
	private void log_w(String msg) {
		Log.w(BtAPI.LOG_TAG, "BluetoothConnectionManager: " + msg);
	}
	
	private void log_e(String msg) {
		Log.e(BtAPI.LOG_TAG, "BluetoothConnectionManager: " + msg);
	}
	
	private void startListeningForConnection() {
		log_v("startListeningForConnection()");
		acceptThread.start();
		log_v("startListeningForConnection() end");
	}
	
	private synchronized void manageConnectedServerSocket(BluetoothSocket socket) {
		log_v("manageConnectedServerSocket()");
		if (connectedAs == ConnectedAs.None) {
			Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: manageConnectedServerSocket() decided on server");
    		connectedAs = ConnectedAs.Server;
    		log_i("Connected as server to " + socket.getRemoteDevice().getAddress());
    		mConnObject = new BtConnection(socket, true);
    		doneAndCleanUp();
		}
		log_v("manageConnectedServerSocket() end");
	}
	
	private void connectToOtherDevice() {
		log_v("connectToOtherDevice()");
		
		connectThread.start();
		log_v("connectToOtherDevice() end");
	}

	public synchronized void manageConnectedClientSocket(BluetoothSocket socket) {
		log_v("manageConnectedClientSocket()");
		if (connectedAs == ConnectedAs.None) {
			Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: manageConnectedClientSocket() decided on client");
			connectedAs = ConnectedAs.Client;
			log_i("Connected as client to " + socket.getRemoteDevice().getAddress());
			mConnObject = new BtConnection(socket, false);
			doneAndCleanUp();
		}
		log_v("manageConnectedClientSocket() end");
	}
	
	public synchronized BtConnection getConnectionObject() {
		log_v("getConnectionObject()");
		return mConnObject;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
			final BluetoothDevice otherDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			
			Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: BOND_STATE_CHANGED: "+previousBondState+"=>"+bondState+" ("+otherDevice.getBondState()+")");
			
			// Pairing initiated by Android
			if (previousBondState == BluetoothDevice.BOND_NONE &&
				bondState == BluetoothDevice.BOND_BONDING) {
				Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: Pairing begun");
				acceptThread.cancel();
				acceptThread.stop();
				connectThread.cancel();
				connectThread.stop();
				showPairingHelpDialog();
			}
			
			// Pairing attempt failed
			if (previousBondState == BluetoothDevice.BOND_BONDING &&
					bondState == BluetoothDevice.BOND_NONE) {
				Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: Pairing failed!");
				showRetryPairingDialog();
			}
			
			// Pairing done!
			if (bondState == BluetoothDevice.BOND_BONDED) {
				Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: Pairing complete!");
				if (pairingHelpDialog != null && pairingHelpDialog.isShowing()) {
					pairingHelpDialog.dismiss();
				}
				showPairingCompleteDialog();
				Toast.makeText(mContext, "PAIRED!!!", Toast.LENGTH_LONG).show();
/*
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
					}
				}, 2000);
				*/
			}

		} else if (intent.getAction().equals(BluetoothDevice.ACTION_CLASS_CHANGED)) {
			final BluetoothDevice otherDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: CLASS_CHANGED: ("+otherDevice.getBondState()+")");
			
		} else if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
			final BluetoothDevice otherDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: FOUND: ("+otherDevice.getBondState()+")");
			
		} else if (intent.getAction().equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
			final BluetoothDevice otherDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(BtAPI.LOG_TAG, "BluetoothConnectionManager: NAME_CHANGED: ("+otherDevice.getBondState()+")");
			
		}
	}
	
	private void showPairingHelpDialog() {
		log_v("showPairingHelpDialog()");
		if (pairingHelpDialog != null && pairingHelpDialog.isShowing()) {
			pairingHelpDialog.dismiss();
		}
		
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_NEGATIVE:
		            doneAndCleanUp();
		            break;
		        }		
		    }
		};

		pairingHelpDialog = new AlertDialog.Builder(mContext).setMessage(BtAPI.res_string(mContext, "dustytuba_pairing_help")).
		setNegativeButton(android.R.string.cancel, dialog_click_listener).show();
		log_v("showPairingHelpDialog() end");
	}
	
	private void showRetryPairingDialog() {
		log_v("showRetryPairingDialog()");
		if (pairingHelpDialog != null && pairingHelpDialog.isShowing()) {
			pairingHelpDialog.dismiss();
		}
		
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	initiateConnection();
		        	break;
		        case DialogInterface.BUTTON_NEGATIVE:
		        	doneAndCleanUp();
		        	break;
		        }		
		    }
		};
		
		final OnCancelListener dialog_cancel_listener = new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
	            doneAndCleanUp();
			}
		};
		
		pairingHelpDialog = new AlertDialog.Builder(mContext).setMessage(BtAPI.res_string(mContext, "dustytuba_pairing_failed_try_again")).
		setPositiveButton(android.R.string.yes, dialog_click_listener).
		setCancelable(true).setOnCancelListener(dialog_cancel_listener).
		setNegativeButton(android.R.string.no, dialog_click_listener).show();
		log_v("showRetryPairingDialog() end");
	}
	
	private void showPairingCompleteDialog() {
		if (pairingHelpDialog != null && pairingHelpDialog.isShowing()) {
			pairingHelpDialog.dismiss();
		}
		
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
	            doneAndCleanUp();
		    }
		};

		final OnCancelListener dialog_cancel_listener = new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
	            doneAndCleanUp();
			}
		};
		
		pairingHelpDialog = new AlertDialog.Builder(mContext).setMessage(BtAPI.res_string(mContext, "dustytuba_pairing_complete")).
		setCancelable(true).setOnCancelListener(dialog_cancel_listener).
		setPositiveButton(android.R.string.ok, dialog_click_listener).show();
	}
	
	
	private class AcceptThread extends Thread {
		private BluetoothServerSocket mBTServerSocket;
		
		public AcceptThread() {
			try {
				mBTServerSocket = mBTAdapter.listenUsingRfcommWithServiceRecord(sdp_name, uuid);
			} catch (IOException e) {
				log_e("AcceptThread(): IOException");
				e.printStackTrace();
				return;
			}
		}
		
		public void run() {
			log_v("AcceptThread: run()");
			BluetoothSocket socket = null;

	        while (true) {
				log_v("AcceptThread: run() loop");
	            try {
	                socket = mBTServerSocket.accept();
					log_v("AcceptThread: run() accepted");
	            } catch (IOException e) {
					log_w("AcceptThread: run() accept() caused IOException");
	                break;
	            }
				log_v("AcceptThread: run() outside of try");
	            
	            // If a connection was accepted
	            if (socket != null) {
	                manageConnectedServerSocket(socket);
	            }
				log_v("AcceptThread: run() after socket check");

                try {
    				log_v("AcceptThread: run() closing server socket");
					mBTServerSocket.close();
    				log_v("AcceptThread: run() closed server socket");
				} catch (IOException e) {
					log_w( "IOException on closing Bluetooth server socket" );
				}
                break;
            }
        }
	    
		
		public void cancel() {
			log_v("AcceptThread: cancel()");
	        try {
	        	mBTServerSocket.close();
	        } catch (IOException e) {
				log_e( "IOException on closing Bluetooth server socket" );
	        }
			log_v("AcceptThread: cancel() end");
	    }
	 }

	private class ConnectThread extends Thread {
		private final BluetoothDevice mBTOtherDevice;
		private final BluetoothSocket mBTClientSocket;
		
		public ConnectThread(BluetoothDevice otherDevice) {
			mBTOtherDevice = otherDevice;
			
			BluetoothSocket tmp = null;
			
			log_v("ConnectThread: constructor()");
			try {
				tmp = mBTOtherDevice.createRfcommSocketToServiceRecord(uuid);
			} catch (IOException e) {
				log_w("ConnectThread: IOException on creating socket");
				e.printStackTrace();
			}
			
			mBTClientSocket = tmp;
		}
		
		public void run() {
			log_v("ConnectThread: run()");
			try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
				mBTAdapter.cancelDiscovery();
	            mBTClientSocket.connect();
				log_v("ConnectThread: run() connected");
	        } catch (IOException connectException) {
				log_w("ConnectThread: connect() caused IOException");

	            // Unable to connect; close the socket and get out
	            try {
	                mBTClientSocket.close();
	            } catch (IOException closeException) {
					log_e( "ConnectThread: IOException on closing Bluetooth client socket" );
	            }
	            
	            log_v("ConnectThread: connect() caused IOException end");
	            return;
	        }

			log_v("ConnectThread: run() going to manageConnectedClientSocket()");

			manageConnectedClientSocket(mBTClientSocket);			
	        // Do work to manage the connection (in a separate thread)
        	
		}

		public void cancel() {
        	log_v("ConnectThread: cancel()");
	    }
	}

	

}
