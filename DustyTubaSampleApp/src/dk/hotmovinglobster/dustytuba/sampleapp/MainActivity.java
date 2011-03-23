package dk.hotmovinglobster.dustytuba.sampleapp;

import com.bumptech.bumpapi.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
//import android.bluetooth.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;

public class MainActivity extends Activity implements BumpAPIListener, OnCancelListener {
	
	private static final String TAG = "DustyTubaSampleApp";
	
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int REQUEST_BUMP = 3;
	private static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	
	private enum ProtocolState { NONE, VERSION, SERVER_RANDOM_NUMBER, BLUETOOTH_MAC, BLUETOOTH_NAME };
	private ProtocolState protocolState = ProtocolState.NONE;
	private ByteArrayList protocolBuffer = new ByteArrayList(64);
	
	/* Implementation details (of our current version) of the protocol */
	private static final int VERSION = 1; // Incremented on API changes
	
	/* Protocol States */
	private static final byte PROTOCOL_VERSION = 0;
	private static final byte PROTOCOL_SERVER_RANDOM_NUMBER = 1;
	private static final byte PROTOCOL_BLUETOOTH_MAC = 2;
	private static final byte PROTOCOL_BLUETOOTH_NAME = 3;
	
	private TextView lblMyBTMac;
	private TextView lblMyBTName;
	private TextView lblMyBTConnType;
	private TextView lblMyProtocolVersion;
	private TextView lblOtherBTMac;
	private TextView lblOtherBTName;
	private TextView lblOtherBTConnType;
	private TextView lblOtherProtocolVersion;
	
	private Button btnConnectBump;
	
	//private String MyMAC = null;
	private int otherVersion;
	private java.util.Random rnd = new java.util.Random();
	private float serverRandomNumber = rnd.nextFloat();
	private float otherServerRandomNumber;
	private String otherBluetoothMAC;
	private String otherBluetoothName;
	private boolean isServer;
	
	private ProgressDialog connectionSetupDialog;
	
	private Resources res;
	
	private BumpConnection bConn = null;
	
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initializeViews();
        res = getResources();
        
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
    	if (inState == null)
    		return;
    	
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
		lblMyBTMac = (TextView)findViewById(R.id.lblMyBTMac);
        lblMyBTName = (TextView)findViewById(R.id.lblMyBTName);
        lblMyBTConnType = (TextView)findViewById(R.id.lblMyBTConnType);
        lblMyProtocolVersion = (TextView)findViewById(R.id.lblMyProtocolVersion);
        
        lblOtherBTMac = (TextView)findViewById(R.id.lblOtherBTMac);
        lblOtherBTName = (TextView)findViewById(R.id.lblOtherBTName);
        lblOtherBTConnType = (TextView)findViewById(R.id.lblOtherBTConnType);
        lblOtherProtocolVersion = (TextView)findViewById(R.id.lblOtherProtocolVersion);

        btnConnectBump = (Button)findViewById(R.id.btnConnectBump);
        btnConnectBump.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!hasInternetConnection()) {
					Toast.makeText(MainActivity.this, res.getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
					return;
				}
				Intent bump = new Intent(MainActivity.this, BumpAPI.class);
				bump.putExtra(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				bump.putExtra(BumpAPI.EXTRA_USER_NAME, mBluetoothAdapter.getName());
				startActivityForResult(bump, REQUEST_BUMP);
			}
		});
	}
    
    @Override
    public void onStart() {
    	super.onStart();
        lblMyProtocolVersion.setText( String.format( res.getString(R.string.protocol_version_f), PROTOCOL_VERSION ) );
    	btnConnectBump.setEnabled( false );
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
        	BluetoothActivated();
        }
    }
    
    private boolean hasInternetConnection() {
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
    	if (ni==null)
    		return false;
    	
    	return ni.isConnectedOrConnecting();
    }
    
    private String getBluetoothAddress() {
    	return mBluetoothAdapter.getAddress();
    	/*
    	if (MyMAC == null) {
        	MyMAC = "00:00:00:00:00:" + rnd.nextInt(10) + rnd.nextInt(10);
    	}
    	return MyMAC;
    	*/
    }
    
    private String getBluetoothName() {
    	return mBluetoothAdapter.getName();
    	//return MyMAC.substring(15, 17);
    }
    
    private void BluetoothActivated() {
        lblMyBTMac.setText( String.format( res.getString(R.string.mac_address_f), getBluetoothAddress() ) );
        lblMyBTName.setText( String.format( res.getString(R.string.name_f), getBluetoothName() ) );
        btnConnectBump.setEnabled( true );
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (requestCode == REQUEST_ENABLE_BT) {
    		if (resultCode == RESULT_OK) {
    			BluetoothActivated();
    		} else {
    			Toast.makeText(this, "Bluetooth was not enabled. Cannot fetch information", Toast.LENGTH_LONG).show();
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

	@Override
	public void bumpDataReceived(byte[] arg0) {
		Log.i(TAG, "Received: " + new String(arg0));
		Log.i(TAG, "State prior: " + protocolState.toString() );
		for (int i = 0; i < arg0.length; i++)
			byteReceived( arg0[i] );
		Log.i(TAG, "State posterior: " + protocolState.toString() );
		//Toast.makeText(this, "Bump received: " + new String(arg0), Toast.LENGTH_SHORT).show();
		
	}
	
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
					connectionSetupDialog.dismiss();
				}
			}
		}
	}

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
    
    /** Used for when a user cancels a progress dialog **/
	@Override
	public void onCancel(DialogInterface dialog) {
		if ( dialog == connectionSetupDialog ) {
			// TODO: Define behaviour for when user cancels the setup dialog
			Toast.makeText(this, "Connection setup canceled...", Toast.LENGTH_LONG).show();
		}
	}
	
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
    	lblOtherProtocolVersion.setText( String.format( res.getString(R.string.protocol_version_f), otherVersion ) );
		if ( VERSION != otherVersion ) {
			String errorText = "The application is out of date.";
			if ( VERSION > otherVersion )
				errorText = "Recipient application is out of date.";
			Toast.makeText(this, errorText, Toast.LENGTH_LONG).show();
		}    	
		
		// TODO: Do something sensible, e.g.:
		// * proper error respective to version number
		// * abort
		// * launch market link
		// * offer to send apk (root only?) to other phone
		// * extend with major / minor version
	}
	
    private void otherBluetoothMACObtained() {
		lblOtherBTMac.setText( String.format( res.getString(R.string.mac_address_f), otherBluetoothMAC ) );
	}

    private void otherBluetoothNameObtained() {
		lblOtherBTName.setText( String.format( res.getString(R.string.name_f), otherBluetoothName ));
	}

	@Override
	public void bumpDisconnect(BumpDisconnectReason arg0) {
		Toast.makeText(this, "Bump disconnected", Toast.LENGTH_LONG).show();
		// TODO Auto-generated method stub
		
	}
}