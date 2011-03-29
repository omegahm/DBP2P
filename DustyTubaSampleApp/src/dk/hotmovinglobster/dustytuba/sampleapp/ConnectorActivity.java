package dk.hotmovinglobster.dustytuba.sampleapp;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpAPIListener;
import com.bumptech.bumpapi.BumpConnectFailedReason;
import com.bumptech.bumpapi.BumpConnection;
import com.bumptech.bumpapi.BumpDisconnectReason;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class ConnectorActivity extends Activity implements BumpAPIListener, OnCancelListener {
	/* GENERAL */
	private Resources res;
	
	/* DEBUG */ 
	private static final String TAG = "DustyTubaSampleApp";
    private static final boolean D = true;
    
	/* BLUETOOTH */
    protected static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    protected static final int REQUEST_BT_ENABLE = 1;
	protected static final int REQUEST_BT_ESTABLISH = 2;
	protected static final int REQUEST_BUMP = 3;
	private BluetoothConnectionService mConnService = null;
	private String otherBluetoothMAC = "";
	
    // Message types sent from the BluetoothConnectionService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
	
	/* Application specifics */
	protected static final String BT_UUID = "fa87c0e0-afac-12de-8a39-a80f200c9a96";
	protected static final String BT_SDP_NAME = TAG;
	protected static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	private boolean isServer;
	private StringBuffer mOutStringBuffer;
	
	/* BUMP Protocol (current version) */
	private static final int VERSION = 1; // Incremented on API changes
	private enum ProtocolState { NONE, VERSION, SERVER_RANDOM_NUMBER, BLUETOOTH_MAC, BLUETOOTH_NAME };
	private ProtocolState protocolState = ProtocolState.NONE;
	private ByteArrayList protocolBuffer = new ByteArrayList(64);
	private static final byte PROTOCOL_VERSION = 0;
	private static final byte PROTOCOL_SERVER_RANDOM_NUMBER = 1;
	private static final byte PROTOCOL_BLUETOOTH_MAC = 2;
	private static final byte PROTOCOL_BLUETOOTH_NAME = 3;
	// TODO: Decide whether we're using the BYTES or ENUM. No need for both?
	
	/* BUMP */
	private int otherVersion = -1;
	private java.util.Random rnd = new java.util.Random();
	private float serverRandomNumber = rnd.nextFloat();
	private float otherServerRandomNumber;
	private String otherBluetoothName = "";
	private BumpConnection bumpConn = null;
	private ProgressDialog connectionSetupDialog;
	
	private byte[] checksum;
	private byte[] intentData;
	private boolean dataSent = false;
	private int state = -1;
	private int SENDER = 0;
	private int RECEIVER = 1;
	
	/**
	 * Launch the bump dialog and determine another bump user
	 * @param context a context, which implements OnCancelListener
	 * @return true if everything went well
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(D) Log.d(TAG, "++ ON CREATE ++");
		
		/* Cancel if no internet is found */
		if(!hasInternetConnection()) {
			this.finish();
		}
		
		/* Request bluetooth */
		if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
		}
		
		if(!isReadyForBluetooth()) {
			this.finish();
		}
		
		res = getResources();
		
		
		Intent intent = getIntent();
		if(intent.getBooleanExtra(ReceiveActivity.INTENT_RECEIVE_DATA, false)) {
			state = RECEIVER;
		} else if(intent.hasExtra(ShareActivity.INTENT_SEND_DATA)) {
			state = SENDER;
			intentData = intent.getByteArrayExtra(ShareActivity.INTENT_SEND_DATA);
			checksum = intent.getByteArrayExtra(ShareActivity.INTENT_SEND_CHECKSUM);
		}
		
		/* Create the bump intent*/
		Intent bump = new Intent(this, BumpAPI.class);
		/* Load it with API_KEY and USER_NAME*/
		bump.putExtra(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
		bump.putExtra(BumpAPI.EXTRA_USER_NAME, getBluetoothName());
		
		/* Wait for it to connect */
		/* This will return in our onActivityResult with REQEUST_BUMP */
		if(D) Log.v(TAG, "Requesting BUMP");
		startActivityForResult(bump, REQUEST_BUMP);
	}
	
	@Override
	public void onStart() {
		super.onStart();
    	if(D) Log.e(TAG, "++ ON START ++");
        if (mConnService == null) {
        	// Initialize the BluetoothConnectionService to perform bluetooth connections
            mConnService = new BluetoothConnectionService(this, mHandler, BT_UUID, BT_SDP_NAME);
            mOutStringBuffer = new StringBuffer("");
        }
    }
	
	@Override
	public void onResume() {
		super.onResume();
		// Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mConnService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mConnService.getState() == BluetoothConnectionService.STATE_NONE) {
              // Start the Bluetooth connection services
              mConnService.start();
            }
        }
        
        // HACK: Always connect on start
        if (!isServer){
        	if(D) Log.v(TAG, "Starting Bluetooth");
	        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(otherBluetoothMAC);
	        // HACK: Ugly hack for 2 sec delay, since client might establish connection before server listens
	        // TODO: Proper Delay w/ Retry and Error code is the way to go
	        try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
	        mConnService.connect(device);
        }
        
        if(!dataSent && state == SENDER) {
        	sendMessage(intentData);
        	sendMessage(checksum);
        }
	}
    
		
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_BT_ENABLE) {
    		if (resultCode == RESULT_OK) {
    			
    		} else {
    			// Toast.makeText(this, "Bluetooth was not enabled. Cannot fetch information", Toast.LENGTH_LONG).show();
    		}
    	} else if (requestCode == REQUEST_BT_ESTABLISH) {
    		if (resultCode == RESULT_OK) {
    			// Toast.makeText(this, "Connection established and finished OK", Toast.LENGTH_LONG).show();
    		} else if (resultCode == RESULT_CANCELED) {
    			// Toast.makeText(this, "Connection cancelled", Toast.LENGTH_LONG).show();
    		} else {
    			// TODO: btConnectFailedReason
    			// Toast.makeText(this, "TODO: btConnectFailedReason", Toast.LENGTH_LONG).show();
    		}
    	} else if (requestCode == REQUEST_BUMP) {
			if (resultCode == RESULT_OK) {
				bumpConn = data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
				bumpConn.setListener( this );
				Log.i(TAG, "Obtained connection through bump");
				sendBluetoothInfo();
			} else if (data != null) {
				// Failed to connect, obtain the reason
				BumpConnectFailedReason reason = (BumpConnectFailedReason) data.getSerializableExtra(BumpAPI.EXTRA_REASON);
				//Toast.makeText(this, "Failed to connect with Bump.\n" + reason.toString(), Toast.LENGTH_LONG).show();
			}
    	}
	}
	
	/** 
	 * Send BT setup info through Bump Connection 
	 */
    private void sendBluetoothInfo() {
    	connectionSetupDialog = ProgressDialog.show(this, "", res.getString(R.string.setting_up_connection), true, true, this);
    	ByteArrayList byl = new ByteArrayList();
    	connectionSetupDialog.setProgress(0);
    	byl.add( PROTOCOL_VERSION );
    	byl.addAll( ByteArrayTools.toByta( VERSION ) );
    	
    	connectionSetupDialog.setProgress(2500);
    	byl.add( PROTOCOL_SERVER_RANDOM_NUMBER );
    	byl.addAll( ByteArrayTools.toByta( serverRandomNumber ) );
    	
    	connectionSetupDialog.setProgress(5000);
    	byl.add( PROTOCOL_BLUETOOTH_MAC );
    	byl.addAll( ByteArrayTools.toByta( getBluetoothAddress() ) );
    	
    	connectionSetupDialog.setProgress(7500);
    	byl.add( PROTOCOL_BLUETOOTH_NAME );
    	byl.addAll( ByteArrayTools.toByta( getBluetoothName() ) );
    	
    	byl.add( (byte) 0 );
    	connectionSetupDialog.setProgress(10000);
    	bumpConn.send( byl.toArray() );
    	Log.i(TAG, "Sent bluetooth info" );
    }
	
	/** 
	 * Phone has (potential) Internet Connection (ignores invalid IP/gateway, capture portal etc) 
	 */
    private boolean hasInternetConnection() {
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = cm.getActiveNetworkInfo();
    	
    	if (networkInfo == null) {
    		return false;
    	} else {
    		return networkInfo.isConnectedOrConnecting();
    	}
    }
    
    /** 
     * Has identity provider provided us with enough info to do BT? 
     */
	private boolean isReadyForBluetooth() {
		return !otherBluetoothMAC.equals("") && BluetoothAdapter.checkBluetoothAddress(otherBluetoothMAC);
	}
	
    /** 
     * Easy access to Bluetooth MAC 
     */
    private String getBluetoothAddress() {
    	return mBluetoothAdapter.getAddress();
    }

    /** 
     * Easy access to Bluetooth name 
     */
    private String getBluetoothName() {
    	return mBluetoothAdapter.getName();
    }

	@Override
	public void bumpDataReceived(byte[] arg0) {
		if(D) Log.i(TAG, "Received: " + new String(arg0));
		if(D) Log.i(TAG, "State prior: " + protocolState.toString() );
		for (int i = 0; i < arg0.length; i++)
			byteReceived( arg0[i] );
		if(D) Log.i(TAG, "State posterior: " + protocolState.toString() );		
	}

	@Override
	public void bumpDisconnect(BumpDisconnectReason arg0) {
		// TODO Auto-generated method stub		
	}
	
	private void byteReceived(byte received) {
		Log.i(TAG, "Byte: " + received + " (" + (int) received + "), State: " + protocolState.toString() );
		if (protocolState == ProtocolState.NONE) {
			switch (received) {
				case PROTOCOL_VERSION:
					protocolState = ProtocolState.VERSION;
					break;
				case PROTOCOL_SERVER_RANDOM_NUMBER:
					protocolState = ProtocolState.SERVER_RANDOM_NUMBER;
					break;
				case PROTOCOL_BLUETOOTH_MAC:
					protocolState = ProtocolState.BLUETOOTH_MAC;
					break;
				case PROTOCOL_BLUETOOTH_NAME:
					protocolState = ProtocolState.BLUETOOTH_NAME;
					break;
			}
		} else {
			protocolBuffer.add( received );
			if (protocolState == ProtocolState.VERSION) {
				if ( protocolBuffer.size() == 4 ) {
					otherVersion = ByteArrayTools.toInt( protocolBuffer.toArray() );
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherVersionObtained();
				}
			} else if (protocolState == ProtocolState.SERVER_RANDOM_NUMBER) {
				if ( protocolBuffer.size() == 4 ) {
					otherServerRandomNumber = ByteArrayTools.toFloat( protocolBuffer.toArray() );
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherServerNumberObtained();
				}
			} else if (protocolState == ProtocolState.BLUETOOTH_MAC) {
				if ( protocolBuffer.size() == 17 ) {
					otherBluetoothMAC = new String( protocolBuffer.toArray() );
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
				}
			} else if (protocolState == ProtocolState.BLUETOOTH_NAME) {
				if ( (int)received == 0 ) {
					otherBluetoothName = new String( protocolBuffer.toArray() );
					otherBluetoothName = otherBluetoothName.substring(0, otherBluetoothName.length() - 1);
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					
					// HACK: We know Name is the last we send over BumpConn, so we're done!
					connectionSetupDialog.dismiss();
				} else {
					// Keep reading characters.
				}
			}
		}
	}
	
	private void otherVersionObtained() {
    	// Update main screen feedback
    	handleVersion(VERSION, otherVersion, res.getString(R.string.package_name));
	}
	
	/** 
	 * Decide Server + renegotiate if Server could not be decided 
	 */
	private void otherServerNumberObtained() {
		if ( otherServerRandomNumber == serverRandomNumber ) {
	    	bumpConn.send( new byte[]{PROTOCOL_SERVER_RANDOM_NUMBER} );
	    	bumpConn.send( ByteArrayTools.toByta( serverRandomNumber ) );
		} else {
			if ( otherServerRandomNumber < serverRandomNumber ) {
				isServer = true;
			} else {
				isServer = false;
			}
		}
	}
	
	/** 
	 * Reset Identity Provider info (e.g. on Aborts) 
	 */
	private void resetIdentityProviderInfo() {
		otherBluetoothMAC = "INVALID_MAC";
	}
	
	/** 
	 * Handle versions using Alert Dialog
	 * If same version:    Do nothing
	 * If this is newest:  Abort or Exit
	 * If other is newest: Update packageName or Exit
	 */
    public void handleVersion(int thisVersion, int otherVersion, String packageName) {
    	if ( thisVersion != otherVersion ) {
	    	resetIdentityProviderInfo();
    		Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("The recipient application is out of date.")
	    		.setCancelable(true)
	    		// TODO: Run through and change strings to @strings and use res.getXXX()
	    		.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int id) {
		                finish();
		            }
	    		}).setNeutralButton("Abort", new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int id) {
			            // Do nothing.
			        }
	        	});
	    	
			if ( thisVersion < otherVersion ) {
	    		builder = new AlertDialog.Builder(this);
				builder.setMessage("The application is out of date.")
		        	.setCancelable(false)	
					.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                finish();
			           }
					}).setPositiveButton("Update", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   Intent intent = new Intent(Intent.ACTION_VIEW);
			        	   intent.setData(Uri.parse("market://details?id=" + res.getString(R.string.package_name)));
			        	   startActivity(intent);
			        	   ConnectorActivity.this.finish();
			           }  
		        	});
			}    	
	        AlertDialog alert = builder.create();
	    	alert.show();		
		}
	}
    
    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    public void sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mConnService.getState() != BluetoothConnectionService.STATE_CONNECTED) {
            return;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Tell the BluetoothConnectionService to write
            mConnService.write(message);
            
            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }
    
    // The Handler that gets information back from the BluetoothConnectionService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case BluetoothConnectionService.STATE_CONNECTED:
                    //mConversationArrayAdapter.add("STATUS: " + "CONNECTED");
                    break;
                case BluetoothConnectionService.STATE_CONNECTING:
                	//mConversationArrayAdapter.add("STATUS: " + "CONNECTING");
                    break;
                case BluetoothConnectionService.STATE_LISTEN:
                case BluetoothConnectionService.STATE_NONE:
                	//mConversationArrayAdapter.add("STATUS: " + "NOT CONNECTED");
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("OUT:  " + writeMessage);
                Toast.makeText(ConnectorActivity.this, "Sending data: " + writeMessage, Toast.LENGTH_LONG).show();
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                //mConversationArrayAdapter.add("IN :  " + readMessage);
                Toast.makeText(ConnectorActivity.this, "Receiving data: " + readMessage, Toast.LENGTH_LONG).show();
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                //mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                /*Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();*/
                break;
            case MESSAGE_TOAST:
                /*Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();*/
                break;
            }
        }
    };
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth connection services
        if (mConnService != null) mConnService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

	@Override
	public void onCancel(DialogInterface dialog) {
		Toast.makeText(this, "Transfer canceled", Toast.LENGTH_LONG).show();
		
	}
}
