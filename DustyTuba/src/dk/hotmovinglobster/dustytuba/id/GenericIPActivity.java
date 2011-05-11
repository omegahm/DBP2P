package dk.hotmovinglobster.dustytuba.id;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.api.BtConnection;
import dk.hotmovinglobster.dustytuba.bt.BluetoothConnectionManager;
import dk.hotmovinglobster.dustytuba.bt.BluetoothConnector;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

/**
 * Generic identity provider activity starter class.
 * 
 * Expects all identity provider activities to return a MAC address
 * with the key BtAPI.EXTRA_BT_MAC
 * and returns a bluetooth connection to this MAC address with the key
 * BtAPI.EXTRA_BT_CONNECTION
 * 
 * Can return following result codes:
 *
 * RESULT_CANCELED Error API not used correctly (e.g. wrong class, invalid MAC) or ID provider failed
 * RESULT_OK ID & BT ran successfully and returned active connection in data parcel
 * RESULTCODE_BT_UNAVAILABLE Bluetooth not enabled
 * RESULTCODE_FAILURE_CONNECT Cannot connect to specified device
 * @author Jesper & Thomas
 */
public class GenericIPActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 100;
	private static final int REQUEST_IDENTITY_PROVIDER = 101;
	
	private Intent thisIntent;
	private String ipClass;

//	private static final String LOG_TAG = "APITest:GenericIPActivity: ";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	Log.d(BtAPI.LOG_TAG, "GenericIPActivity: Created");
    	thisIntent = getIntent();
    	Log.v(BtAPI.LOG_TAG, "GenericIPActivity: with data (Size "+thisIntent.getExtras().size()+": "+thisIntent.getExtras().keySet()+")");

    	// Get the class of the identity provider to use
    	ipClass = thisIntent.getStringExtra( BtAPI.EXTRA_IP_CLASS );
    	Log.v(BtAPI.LOG_TAG, "GenericIPActivity: Received '" + ipClass + "' as subclass");

    	// Check for Bluetooth availability
    	BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
    	if (ba == null) {
    		setResult( BtAPI.RESULT_BT_UNAVAILABLE );
    	    finish();
    	    return;
    	}
    	if (ba.isEnabled()) {
    		bluetoothEnabled();
    	} else {
    	    final Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    	    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    	}
    	
    }
    
    /**
     * To be called when Bluetooth is enabled, either upon launch of the activity
     * or when it has been manually enabled.
     */
    private void bluetoothEnabled() {
    	final Intent newIntent = new Intent();
    	newIntent.setClassName(this, ipClass);
    	//newIntent.setComponent( new ComponentName( ipPackage, ipClass ) );
    	
    	if (thisIntent.hasExtra( BtAPI.EXTRA_IP_BUNDLE )) {
    		final Bundle b = thisIntent.getBundleExtra( BtAPI.EXTRA_IP_BUNDLE );
        	Log.d(BtAPI.LOG_TAG, "GenericIPActivity: Received bundle (Size "+b.size()+": "+b.keySet()+")");
    		newIntent.replaceExtras( b );
    	}
    	
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Starting subactivity \""+ipClass+"\"");
    	startActivityForResult(newIntent, REQUEST_IDENTITY_PROVIDER);
    	
	}

	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		Log.d(BtAPI.LOG_TAG, "GenericIPActivity: onActivityResult");
    	switch(requestCode){
			case REQUEST_IDENTITY_PROVIDER:
		    	Log.d(BtAPI.LOG_TAG, "GenericIPActivity: Returned from BtAPI ID activity");
		    	switch(resultCode){
			    	case RESULT_CANCELED:
			    		Log.v(BtAPI.LOG_TAG, "Reason: Cancelled");
			        	setResult(RESULT_CANCELED);
			        	finish();
			    		break;
					case RESULT_OK:
			        	Log.v(BtAPI.LOG_TAG, "Reason: OK");
			    		Log.v(BtAPI.LOG_TAG, "with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
			    		String other_mac = data.getStringExtra(BtAPI.EXTRA_IP_MAC);
			    		Log.v(BtAPI.LOG_TAG, "Other MAC address: " + other_mac);
			    		// Identity received, start BT connection
			    		startBT(other_mac);
			        	break;
		    	}
				break;
    	}
    }

	private void startBT(String other_mac) {
		final ProgressDialog dialog = ProgressDialog.show(this, "", 
                getResources().getString( BtAPI.res(this, "string", "dustytuba_setting_up_connection") ), true);
		dialog.show();
		// FIXME: HACK: Not the proper way to go about this, but it will do for now...
		String BT_UUID = "fa87c0e0-afac-12de-8a39-a80f200c9a96";
		String BT_SDP_NAME = "DustyTubaAPI_SDP_NAME";
		// Change to use BTAPI CONSTANTS
		//b.putExtra(BtAPI.EXTRA_BT_MAC, other_mac);
		/*
		Intent i = new Intent(GenericIPActivity.this, BluetoothConnector.class);
		boolean isServer = false; // try to connect as client in addition to being a server
		i.putExtra(BluetoothConnector.BT_CONN_DATA.SERVER.name(), isServer);
		i.putExtra(BluetoothConnector.BT_CONN_DATA.MAC.name(), other_mac);
		i.putExtra(BluetoothConnector.BT_CONN_DATA.UUID.name(), BT_UUID);
		i.putExtra(BluetoothConnector.BT_CONN_DATA.SDP_NAME.name(), BT_SDP_NAME);
		startActivityForResult(i, BtAPI.REQUEST_SETUP_BT);
		*/
		
		BluetoothConnectionManager bcm = new BluetoothConnectionManager(other_mac, BT_UUID, BT_SDP_NAME);
		bcm.setupConnection();
		
		BtConnection btConn = null;
		
		final int pollInterval = 200;
		final int pollTimeout = 5000;
		int pollCount = 0;
		
		// Repeat until connection acquired or timeout time reached
		while (btConn == null && pollCount * pollInterval < pollTimeout) {
			try {
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				break;
			}
			pollCount++;

			btConn = bcm.getConnectionObject();
		}
		
		BtConnection.setConnection(btConn);

		if (btConn != null) {
			btConn.startListening();
			final Intent i = new Intent();
			i.putExtra(BtAPI.EXTRA_BT_MAC, other_mac);
			setResult(RESULT_OK, i);
		} else {
			setResult(RESULT_CANCELED);
		}
		
		dialog.dismiss();
		
		finish();
		
	}
}
