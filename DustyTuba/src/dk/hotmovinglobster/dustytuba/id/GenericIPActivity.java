package dk.hotmovinglobster.dustytuba.id;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.api.BtConnection;
import dk.hotmovinglobster.dustytuba.bt.BluetoothConnector;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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

	private static final String LOG_TAG = "APITest:GenericIPActivity: ";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Created");
    	thisIntent = getIntent();
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: with data (Size "+thisIntent.getExtras().size()+": "+thisIntent.getExtras().keySet()+")");

    	// Get the class of the identity provider to use
    	ipClass = thisIntent.getStringExtra( BtAPI.EXTRA_IP_CLASS );
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Received '" + ipClass + "' as subclass");

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
    
    private void bluetoothEnabled() {
    	final Intent newIntent = new Intent();
    	newIntent.setClassName(this, ipClass);
    	//newIntent.setComponent( new ComponentName( ipPackage, ipClass ) );
    	
    	if (thisIntent.hasExtra( BtAPI.EXTRA_IP_BUNDLE )) {
    		final Bundle b = thisIntent.getBundleExtra( BtAPI.EXTRA_IP_BUNDLE );
        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Received bundle (Size "+b.size()+": "+b.keySet()+")");
    		newIntent.replaceExtras( b );
    	}
    	
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Starting subactivity");
    	startActivityForResult(newIntent, REQUEST_IDENTITY_PROVIDER);
    	
	}

	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	switch(requestCode){
		case BtAPI.REQUEST_IDENTITY_PROVIDER:
	    	Log.i(LOG_TAG, "Returned from BtAPI ID activity");
	    	switch(resultCode){
	    	case RESULT_CANCELED:
	    		Log.i(LOG_TAG, "Reason: Cancelled");
	        	setResult(RESULT_CANCELED);
	        	finish();
	    		break;
			case RESULT_OK:
	        	Log.i(LOG_TAG, "Reason: OK");
	    		Log.i(LOG_TAG, "with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
	    		String other_mac = data.getStringExtra(BtAPI.EXTRA_IP_MAC);
	    		// Identity received, start BT connection
	    		startBT(other_mac);
	        	break;
	    	}
			break;
		case BtAPI.REQUEST_SETUP_BT:
	    	Log.i(LOG_TAG, "Returned from BtAPI BT SETUP activity");
	    	switch(resultCode){
			case RESULT_OK:
				Log.i(LOG_TAG, "Reason: OK");
	    		Log.i(LOG_TAG, "with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
				// Don't unparcel, just forward?
				// BtConnection conn = (BtConnection)data.getParcelableExtra(BtAPI.EXTRA_BT_CONNECTION);
	    		setResult(RESULT_OK,data);
	        	break;
	    	default:
	    		Log.i(LOG_TAG, "Reason: " + resultCode);
	        	setResult(resultCode);
	    	}
        	finish();
    	}
    }

	private void startBT(String other_mac) {
		// FIXME: HACK: Not the proper way to go about this, but it will do for now...
		Intent i = new Intent(GenericIPActivity.this, BluetoothConnector.class);
		boolean isServer = false; // try to connect as client in addition to being a server
		String BT_UUID = "fa87c0e0-afac-12de-8a39-a80f200c9a96";
		String BT_SDP_NAME = "DustyTubaAPI_SDP_NAME";
		// Change to use BTAPI CONSTANTS
		//b.putExtra(BtAPI.EXTRA_BT_MAC, other_mac);
		i.putExtra(BluetoothConnector.BT_CONN_DATA.SERVER.name(), isServer);
		i.putExtra(BluetoothConnector.BT_CONN_DATA.MAC.name(), other_mac);
		i.putExtra(BluetoothConnector.BT_CONN_DATA.UUID.name(), BT_UUID);
		i.putExtra(BluetoothConnector.BT_CONN_DATA.SDP_NAME.name(), BT_SDP_NAME);
		Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
		startActivityForResult(i, BtAPI.REQUEST_SETUP_BT);
	}
}
