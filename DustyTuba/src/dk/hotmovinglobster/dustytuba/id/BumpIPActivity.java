package dk.hotmovinglobster.dustytuba.id;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpAPIListener;
import com.bumptech.bumpapi.BumpConnection;
import com.bumptech.bumpapi.BumpDisconnectReason;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.tools.ByteArrayList;
import dk.hotmovinglobster.dustytuba.tools.ByteArrayTools;

public class BumpIPActivity extends Activity implements BumpAPIListener, OnCancelListener {

	private static final int VERSION = 1; // Incremented on API changes
	private enum ProtocolState { NONE, VERSION, SERVER_RANDOM_NUMBER, BLUETOOTH_MAC, BLUETOOTH_NAME };
	private ProtocolState protocolState = ProtocolState.NONE;
	private ByteArrayList protocolBuffer = new ByteArrayList(64);
	private static final byte PROTOCOL_VERSION = 0;
	private static final byte PROTOCOL_BLUETOOTH_MAC = 1;
	//private static final byte PROTOCOL_SERVER_RANDOM_NUMBER = 2;
	//private static final byte PROTOCOL_BLUETOOTH_NAME = 3;
	// TODO: Decide whether we're using the BYTES or ENUM. No need for both?
	
	/* BUMP */
	private int otherVersion = -1;

	private BumpConnection bConn = null;
	private ProgressDialog connectionSetupDialog;
	
	//private Resources res;
	private String otherBluetoothMAC;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.d(BtAPI.LOG_TAG, "BumpIPActivity: Created");
    	
    	//res = getResources();
    	
    	Intent thisIntent = getIntent();
    	Log.v(BtAPI.LOG_TAG, "BumpIPActivity: with data (Size "+thisIntent.getExtras().size()+": "+thisIntent.getExtras().keySet()+")");

    	Intent i = new Intent( this, com.bumptech.bumpapi.BumpAPI.class );
    	i.replaceExtras( thisIntent.getExtras() );
    	i.putExtra(BumpAPI.EXTRA_USER_NAME, BtAPI.getBluetoothName());
    	startActivityForResult(i, 0);
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_CANCELED) {
        	Log.d(BtAPI.LOG_TAG, "BumpIPActivity: Bump activity returned (Result: cancel)");
        	setResult( RESULT_CANCELED );
        	finish();
    	} else if (resultCode == RESULT_OK) {
        	Log.d(BtAPI.LOG_TAG, "BumpIPActivity: Bump activity returned (Result: OK)");
        	Log.v(BtAPI.LOG_TAG, "BumpIPActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
			bConn = data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
			bConn.setListener( this );
			sendBluetoothInfo();
    	} else {
        	Log.d(BtAPI.LOG_TAG, "BumpIPActivity: Bump activity returned (Result: "+resultCode+")");
    		setResult( resultCode );
    		finish();
    	}
    	
//    	Log.d(BtAPI.LOG_TAG, "BumpIPActivity: Finishing");
//    	finish();
    	
    }
    
	/** REQ Called when the API connection terminates */
    @Override
	public void bumpDisconnect(BumpDisconnectReason arg0) {
    	Log.v(BtAPI.LOG_TAG, "BumpIPActivity: bumpDisconnect("+arg0.toString()+")");
		// Toast.makeText(this, "Bump disconnected", Toast.LENGTH_LONG).show();
		stopProgressDialog();
	}
    
	/** REQ Called when a chunk of data is received from the remote client */
	@Override
	public void bumpDataReceived(byte[] arg0) {
		for (int i = 0; i < arg0.length; i++)
			byteReceived( arg0[i] );
	}
	
	/** Handles received data */
	private void byteReceived(byte arg0) {
//		Log.i(TAG, "Byte: " + arg0 + " (" + (int)arg0 + "), State: " + protocolState.toString() );
		if (protocolState == ProtocolState.NONE) {
			switch (arg0) {
				case PROTOCOL_VERSION:
					protocolState = ProtocolState.VERSION;
					break;
				case PROTOCOL_BLUETOOTH_MAC:
					protocolState = ProtocolState.BLUETOOTH_MAC;
					break;
					/*
				case PROTOCOL_SERVER_RANDOM_NUMBER:
					protocolState = ProtocolState.SERVER_RANDOM_NUMBER;
					break;
				case PROTOCOL_BLUETOOTH_NAME:
					protocolState = ProtocolState.BLUETOOTH_NAME;
					break;
					*/
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
			} else if (protocolState == ProtocolState.BLUETOOTH_MAC) {
				if ( protocolBuffer.size() == 17 ) {
					otherBluetoothMAC = new String( protocolBuffer.toArray() );
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherBluetoothMACObtained();
				}/*
			} else if (protocolState == ProtocolState.SERVER_RANDOM_NUMBER) {
				if ( protocolBuffer.size() == 4 ) {
					otherServerRandomNumber = ByteArrayTools.toFloat( protocolBuffer.toArray() );
					protocolBuffer.clear();
					protocolState = ProtocolState.NONE;
					otherServerNumberObtained();
				}*/
				/*
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
				*/
			}
		}
	}

	/** Send stuff (BT setup info) through Bump Connection */
    private void sendBluetoothInfo() {
		Log.v(BtAPI.LOG_TAG, "BumpIPActivity: sendBluetoothInfo(): Start");
    	//connectionSetupDialog = ProgressDialog.show(this, "", res.getString( BtAPI.res( this, "string", "dustytuba_setting_up_connection" ) ), true, true, this);
    	ByteArrayList byl = new ByteArrayList();
    	byl.add( PROTOCOL_VERSION );
    	byl.addAll( ByteArrayTools.toByta( VERSION ) );
    	byl.add( PROTOCOL_BLUETOOTH_MAC );
    	byl.addAll( ByteArrayTools.toByta( BtAPI.getBluetoothAddress() ) );
//    	byl.add( PROTOCOL_SERVER_RANDOM_NUMBER );
//    	byl.addAll( ByteArrayTools.toByta( serverRandomNumber ) );
//    	byl.add( PROTOCOL_BLUETOOTH_NAME );
//    	byl.addAll( ByteArrayTools.toByta( getBluetoothName() ) );
//    	byl.add( (byte)0 );
    	bConn.send( byl.toArray() );
		Log.v(BtAPI.LOG_TAG, "BumpIPActivity: sendBluetoothInfo(): Sent ("+new String(byl.toArray())+")");
//    	Log.i(TAG, "Sent bluetooth info" );
    }
    
	/*
	 * BUMP VIEW + LOGIC intermingled
	 */
    
    /** Used for when a user cancels during progress dialog */
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.v(BtAPI.LOG_TAG, "BumpIPActivity: OnCancelListener.OnCancel()");
		if ( dialog == connectionSetupDialog ) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}

	private void otherVersionObtained() {
		Log.v(BtAPI.LOG_TAG, "BumpIPActivity: otherVersionObtained(): " + otherVersion);
    	// Update main screen feedback
		// TODO: Handle VERSION
    	// handleVersion(VERSION, otherVersion, res.getString( BtAPI.res( this, "string", "package_name" ) ) );
	}
	
	private void otherBluetoothMACObtained() {
		Log.v(BtAPI.LOG_TAG, "BumpIPActivity: otherBluetoothMACObtained(): " + otherBluetoothMAC);
    	Intent data = new Intent();
        data.putExtra( BtAPI.EXTRA_IP_MAC, otherBluetoothMAC );
		setResult( RESULT_OK, data );
		stopProgressDialog();
		finish();
	}
	
	private void stopProgressDialog() {
		if (connectionSetupDialog != null && connectionSetupDialog.isShowing()) {
			connectionSetupDialog.hide();
		}

	}

}
