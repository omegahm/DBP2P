package dk.hotmovinglobster.dustytuba.sampleapp;

import com.bumptech.bumpapi.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;

public class MainActivity extends Activity implements BumpAPIListener, OnCancelListener {
	
	/* General */
	private Resources res;
	private static final String TAG = "DustyTubaSampleApp";
    private static final boolean D = true; //TODO: use more of these: if(D) ...
	protected static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	/* This Intent: Extras */
	protected static final String INTENT_SEND_DATA = "sending_data";
	protected static final String INTENT_SEND_CHECKSUM = "sending_checksum";
	protected static final String INTENT_SEND_TEXT = "sending_text";

    /* Other Intents: Request codes */
	protected static final int REQUEST_BT_ENABLE = 1;
	protected static final int REQUEST_BT_ESTABLISH = 2;
	protected static final int REQUEST_BUMP = 3;
	
	/* Application specifics */
	protected static final String BT_UUID = "fa87c0e0-afac-12de-8a39-a80f200c9a96";
	protected static final String BT_SDP_NAME = TAG;
	protected static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	
	/* Identity Provider Details*/
	private String otherBluetoothMAC = "";
	private boolean isServer;
	// TODO: Consider whether we should have also have a BT protocol version (in addition to BUMP protocol version)
	// * Bump version is only if we want to exchange other info / additional ID info... e.g. PASSKEY some day
	// * BT conn version (so deprecate older, on on major application changes):
	// *	* exchange stuff before handing over control? unlikely
	// *	* as a service for developer, easier to keep track of versions of software?
	// * 	*	nice to have, but far from vital. devs might want other behavior. Need devs to specify App package name.
	
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
	private BumpConnection bConn = null;
	private ProgressDialog connectionSetupDialog;
	
	/* DISPLAY */
	private TextView lblMyBTMac;
	private TextView lblMyBTName;
	private TextView lblMyBTConnType;
	private TextView lblMyProtocolVersion;
	private TextView lblOtherBTMac;
	private TextView lblOtherBTName;
	private TextView lblOtherBTConnType;
	private TextView lblOtherProtocolVersion;
	// Buttons: only needed for those we want to enable/disable clickability for
	private Button btnConnectBump;
	private Button btnConnectBluetooth;
	
	/*
	 * LOGIC + SETUP
	 */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initializeViews();
        res = getResources();
        
        // BUMP Volume buttons control media volume (which dictates bump sounds)
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC); 
        
        restoreState( savedInstanceState );
             
        //if (mBluetoothAdapter == null) {
        //	Toast.makeText(this, "Bluetooth not available on this device!", Toast.LENGTH_LONG).show();
        //}
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
    	outState.putString( "lblMyBTMacText",              lblMyBTMac.getText().toString() );
    	outState.putString( "lblMyBTNameText",             lblMyBTName.getText().toString() );
    	outState.putString( "lblMyBTConnTypeText",         lblMyBTConnType.getText().toString() );
    	outState.putString( "lblMyProtocolVersionText",    lblMyProtocolVersion.getText().toString() );

    	outState.putString( "lblOtherBTMacText",           lblOtherBTMac.getText().toString() );
    	outState.putString( "lblOtherBTNameText",          lblOtherBTName.getText().toString() );
    	outState.putString( "lblOtherBTConnTypeText",      lblOtherBTConnType.getText().toString() );
    	outState.putString( "lblOtherProtocolVersionText", lblOtherProtocolVersion.getText().toString() );
    }
    
    private void restoreState(Bundle inState) {
    	if (inState == null) {
    		return;
    	}
    	
    	lblMyBTMac.setText( inState.getString( "lblMyBTMacText" ) );
    	lblMyBTName.setText( inState.getString( "lblMyBTNameText" ) );
    	lblMyBTConnType.setText( inState.getString( "lblMyBTConnTypeText" ) );
    	lblMyProtocolVersion.setText( inState.getString( "lblMyProtocolVersionText" ) );
    	
    	lblOtherBTMac.setText( inState.getString( "lblOtherBTMacText" ) );
    	lblOtherBTName.setText( inState.getString( "lblOtherBTNameText" ) );
    	lblOtherBTConnType.setText( inState.getString( "lblOtherBTConnTypeText" ) );
    	lblOtherProtocolVersion.setText( inState.getString( "lblOtherProtocolVersionText" ) );
    }
    
	private void initializeViews() {
		lblMyBTMac = (TextView) findViewById(R.id.lblMyBTMac);
        lblMyBTName = (TextView) findViewById(R.id.lblMyBTName);
        lblMyBTConnType = (TextView) findViewById(R.id.lblMyBTConnType);
        lblMyProtocolVersion = (TextView) findViewById(R.id.lblMyProtocolVersion);
        
        lblOtherBTMac = (TextView) findViewById(R.id.lblOtherBTMac);
        lblOtherBTName = (TextView) findViewById(R.id.lblOtherBTName);
        lblOtherBTConnType = (TextView) findViewById(R.id.lblOtherBTConnType);
        lblOtherProtocolVersion = (TextView) findViewById(R.id.lblOtherProtocolVersion);

        btnConnectBump = (Button) findViewById(R.id.btnConnectBump);
        btnConnectBump.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!hasInternetConnection()) {
					Toast.makeText(MainActivity.this, res.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
					return;
				}
				Intent bump = new Intent(MainActivity.this, BumpAPI.class);
				bump.putExtra(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				bump.putExtra(BumpAPI.EXTRA_USER_NAME, getBluetoothName());
				startActivityForResult(bump, REQUEST_BUMP);
			}
		});
        
        btnConnectBluetooth = (Button) findViewById(R.id.btnConnectBluetooth);
        btnConnectBluetooth.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Assert TODO: Remove
				if(!isReadyForBluetooth()) {
					String errorText = "ERROR: bluetooth button can be pressed, but we're not ready";
					Log.e(TAG, errorText);
					Toast.makeText(MainActivity.this, errorText, Toast.LENGTH_SHORT).show();
        			finish();
				}
				// End Assert
				
				// Start bluetooth
				startBluetooth();				
			}
		});
        
        ((Button) findViewById(R.id.btnConnectAliceBob)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				identityProviderAliceBob();
			}
		});
        
        ((Button) findViewById(R.id.btnReceive)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent receive = new Intent(MainActivity.this, ReceiveActivity.class);
				// TODO: Receive : Add extra parameters needed for bluetooth setup inside Receive Activity? Or pass Conn object.
				//receive.putExtra(BumpAPI.EXTRA_USER_NAME, mBluetoothAdapter.getName());
				startActivity(receive);
			}
		});
	}
	
	@Override
    public void onStart() {
    	super.onStart();
    	lblMyProtocolVersion.setText( String.format( res.getString(R.string.protocol_version_f), PROTOCOL_VERSION ) );
        if(!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
        } else {
        	BluetoothActivated();
        }
        
        // FIXME: State not restored on orientation shift
        btnConnectBluetooth.setEnabled( isReadyForBluetooth() );
        
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_BT_ENABLE) {
    		if (resultCode == RESULT_OK) {
    			BluetoothActivated();
    		} else {
    			Toast.makeText(this, "Bluetooth was not enabled. Cannot fetch information", Toast.LENGTH_LONG).show();
    		}
    	} else if (requestCode == REQUEST_BT_ESTABLISH) {
    		if (resultCode == RESULT_OK) {
    			Toast.makeText(this, "Connection established and finished OK", Toast.LENGTH_LONG).show();
    		} else if (resultCode == RESULT_CANCELED) {
    			Toast.makeText(this, "Connection cancelled", Toast.LENGTH_LONG).show();
    		} else {
    			// TODO: btConnectFailedReason
    			Toast.makeText(this, "TODO: btConnectFailedReason", Toast.LENGTH_LONG).show();
    		}
    	} else if (requestCode == REQUEST_BUMP) {
			if (resultCode == RESULT_OK) {
				bConn = data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
				bConn.setListener( this );
				Log.i(TAG, "Obtained connection through bump");
				sendBluetoothInfo();
			} else if (data != null) {
				// Failed to connect, obtain the reason
				BumpConnectFailedReason reason =
					(BumpConnectFailedReason) data.getSerializableExtra(BumpAPI.EXTRA_REASON);
				Toast.makeText(this, "Failed to connect with Bump.\n" + reason.toString(), Toast.LENGTH_LONG).show();
			}
    	}
    }
	      
	/*
	 * BUMP
	 */

	/** REQ Called when the API connection terminates */
    @Override
	public void bumpDisconnect(BumpDisconnectReason arg0) {
		Toast.makeText(this, "Bump disconnected", Toast.LENGTH_LONG).show();
		connectionSetupDialog.dismiss();
	}
    
	/** REQ Called when a chunk of data is received from the remote client */
	@Override
	public void bumpDataReceived(byte[] arg0) {
		if(D) Log.i(TAG, "Received: " + new String(arg0));
		if(D) Log.i(TAG, "State prior: " + protocolState.toString() );
		for (int i = 0; i < arg0.length; i++)
			byteReceived( arg0[i] );
		if(D) Log.i(TAG, "State posterior: " + protocolState.toString() );
		Toast.makeText(this, "Bump received: " + new String(arg0), Toast.LENGTH_SHORT).show();
	}
	
	/** Handles received data */
	private void byteReceived(byte arg0) {
		Log.i(TAG, "Byte: " + arg0 + " (" + (int)arg0 + "), State: " + protocolState.toString() );
		if (protocolState == ProtocolState.NONE) {
			switch (arg0) {
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
			protocolBuffer.add( arg0 );
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
					otherBluetoothMACObtained();
				}
			} else if (protocolState == ProtocolState.BLUETOOTH_NAME) {
				if ( (int)arg0 == 0 ) {
					otherBluetoothName = new String( protocolBuffer.toArray() );
					otherBluetoothName = otherBluetoothName.substring(0, otherBluetoothName.length() - 1);
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherBluetoothNameObtained();
					// HACK: We know Name is the last we send over BumpConn, so we're done!
					connectionSetupDialog.dismiss();
					btnConnectBluetooth.setEnabled( isReadyForBluetooth() );
				} else {
					// Keep reading characters.
				}
			}
		}
	}

	/** Send stuff (BT setup info) through Bump Connection */
    private void sendBluetoothInfo() {
    	connectionSetupDialog = ProgressDialog.show(this, "", res.getString(R.string.setting_up_connection), true, true, this);
    	ByteArrayList byl = new ByteArrayList();
    	byl.add( PROTOCOL_VERSION );
    	byl.addAll( ByteArrayTools.toByta( VERSION ) );
    	byl.add( PROTOCOL_SERVER_RANDOM_NUMBER );
    	byl.addAll( ByteArrayTools.toByta( serverRandomNumber ) );
    	byl.add( PROTOCOL_BLUETOOTH_MAC );
    	byl.addAll( ByteArrayTools.toByta( getBluetoothAddress() ) );
    	byl.add( PROTOCOL_BLUETOOTH_NAME );
    	byl.addAll( ByteArrayTools.toByta( getBluetoothName() ) );
    	byl.add( (byte)0 );
    	bConn.send( byl.toArray() );
    	Log.i(TAG, "Sent bluetooth info" );
    }
    
	/*
	 * BUMP VIEW + LOGIC intermingled
	 */
    
    /** Used for when a user cancels during progress dialog */
	@Override
	public void onCancel(DialogInterface dialog) {
		if ( dialog == connectionSetupDialog ) {
			// This happens on BACK key in progressdialog, so no need to dismiss.
			Toast.makeText(this, "Bump Transfer cancelled", Toast.LENGTH_LONG).show();
			resetIdentityProviderInfo();
		}
	}
	
	/** Reset Identity Provider info (e.g. on Aborts) */
	private void resetIdentityProviderInfo() {
		otherBluetoothMAC = "INVALID_MAC";
		btnConnectBluetooth.setEnabled( isReadyForBluetooth() );
	}
	
	/** Decide Server + renegotiate if Server could not be decided */
	private void otherServerNumberObtained() {
		if ( otherServerRandomNumber == serverRandomNumber ) {
	    	bConn.send( new byte[]{PROTOCOL_SERVER_RANDOM_NUMBER} );
	    	bConn.send( ByteArrayTools.toByta( serverRandomNumber ) );
		} else {
			if ( otherServerRandomNumber < serverRandomNumber ) {
				isServer = true;
				lblMyBTConnType.setText( res.getString( R.string.connection_type_server ) );
				lblOtherBTConnType.setText( res.getString( R.string.connection_type_client ) );
			} else {
				isServer = false;
				lblMyBTConnType.setText( res.getString( R.string.connection_type_client ) );
				lblOtherBTConnType.setText( res.getString( R.string.connection_type_server ) );
			}
		}
	}

	private void otherVersionObtained() {
    	// Update main screen feedback
    	lblOtherProtocolVersion.setText( String.format( res.getString(R.string.protocol_version_f), otherVersion ) );
    	handleVersion(VERSION, otherVersion, res.getString(R.string.package_name));
	}
    
	
    /* 
     * VIEW 
     */
    
    private void otherBluetoothMACObtained() {
		lblOtherBTMac.setText( String.format( res.getString(R.string.mac_address_f), otherBluetoothMAC ) );
	}

    private void otherBluetoothNameObtained() {
		lblOtherBTName.setText( String.format( res.getString(R.string.name_f), otherBluetoothName ));
	}
    
    private void BluetoothActivated() {
        lblMyBTMac.setText( String.format( res.getString(R.string.mac_address_f), getBluetoothAddress() ) );
        lblMyBTName.setText( String.format( res.getString(R.string.name_f), getBluetoothName() ) );
        btnConnectBump.setEnabled( true );
    }

    /*
     * IDENTITY PROVIDER
     */
	
    /** ID Sets fake identity provider values */
	private void identityProviderAliceBob() {
		Log.i(TAG, "Utilizing Alice&Bob Identity Provider");
		
		//Version
		otherVersion = VERSION;
		otherVersionObtained();
	
		// Server + MAC + NAME
		serverRandomNumber = (float) 0.5;
		if (getBluetoothName().equals("Bob")){
			otherServerRandomNumber = (float) 0.6;
			//otherBluetoothMAC = "90:21:55:a1:a5:8d".toUpperCase(); // HTC Desire (Jesper)
			//otherBluetoothMAC = "00:23:d4:36:da:4a".toUpperCase(); // HTC Hero (KMD)
			otherBluetoothMAC = "00:23:d4:34:45:d7".toUpperCase(); // HTC Hero (Thomas)
			otherBluetoothName = "Hero";
		} else {
			otherServerRandomNumber = (float) 0.4;
			//otherBluetoothMAC = "90:21:55:a1:a5:67".toUpperCase(); // HTC Desire (Thomas)
			otherBluetoothMAC = "90:21:55:a1:a5:8d".toUpperCase(); // HTC Desire (Jesper)
			otherBluetoothName = "Bob";
		}
		
		// VIEW + HANDLE
		otherBluetoothMACObtained();
		otherBluetoothNameObtained();
		otherServerNumberObtained();
		btnConnectBluetooth.setEnabled( isReadyForBluetooth() );
	}
	
	/*
	 * BLUETOOTH
	 */
	
	/** Start bluetooth connection */
    private void startBluetooth() {
		Intent bt = new Intent(MainActivity.this, BluetoothConnectionDialog.class);
		bt.putExtra(BluetoothConnectionDialog.BT_CONN_DATA.SERVER.name(), isServer);
		bt.putExtra(BluetoothConnectionDialog.BT_CONN_DATA.MAC.name(), otherBluetoothMAC);
		bt.putExtra(BluetoothConnectionDialog.BT_CONN_DATA.UUID.name(), BT_UUID);
		bt.putExtra(BluetoothConnectionDialog.BT_CONN_DATA.SDP_NAME.name(), BT_SDP_NAME);
		startActivityForResult(bt, REQUEST_BT_ESTABLISH);
	}
	
	/*
	 * HELPERS
	 */
	
	/** Has identity provider provided us with enough info to do BT? */
	private boolean isReadyForBluetooth() {
		return !otherBluetoothMAC.equals("")
			&& BluetoothAdapter.checkBluetoothAddress(otherBluetoothMAC);
	}
	
	/** Phone has (potential) Internet Connection (ignores invalid IP/gateway, capture portal etc) */
    private boolean hasInternetConnection() {
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
    	if (ni==null)
    		return false;
    	return ni.isConnectedOrConnecting();
    }
    
    /** Easy access to Bluetooth MAC */
    protected String getBluetoothAddress() {
    	return mBluetoothAdapter.getAddress();
    }

    /** Easy access to Bluetooth name */
    protected String getBluetoothName() {
    	return mBluetoothAdapter.getName();
    }
    
	/** Handle versions using Alert Dialog
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
			        	   MainActivity.this.finish();
			           }  
		        	});
			}    	
	        AlertDialog alert = builder.create();
	    	alert.show();		
		}
	}
}