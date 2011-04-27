package dk.hotmovinglobster.dustytuba.id;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
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
 * @author Jesper
 */
public class GenericIPActivity extends Activity {
	
	private static final int REQUEST_ENABLE_BT = 100;
	private static final int REQUEST_IDENTITY_PROVIDER = 101;
	
	private Intent thisIntent;
	private String ipClass;

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
    		setResult( BtAPI.BtConnectFailedReason.FAIL_BT_UNAVAILABLE );
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
//    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: ");
    	// TODO: Move ID Provider + Setup BT to here from MainActivity
    	// TODO: For now implementing in MainActivity since that's easier
    	if (requestCode == REQUEST_ENABLE_BT) {
    		if (resultCode == RESULT_OK) {
    			bluetoothEnabled();
    		} else if (resultCode == RESULT_CANCELED) {
    			setResult( BtAPI.BtConnectFailedReason.FAIL_USER_CANCELED );
    			finish();
    			return;
    		}
       	} else if (requestCode == REQUEST_IDENTITY_PROVIDER) {
	    	if (resultCode == RESULT_CANCELED) {
	        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned (Result: cancel)");
	        	setResult( RESULT_CANCELED );
	    	} else if (resultCode == RESULT_OK) {
	        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned (Result: OK)");
	        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
	        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned MAC " + data.getStringExtra( BtAPI.EXTRA_BT_MAC ));
	        	setResult( RESULT_OK, data );
	    	} else {
	        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned (Result: "+resultCode+")");
	    		setResult( resultCode );
	    	}    	
	    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Finishing");
	    	finish();
       	}
    	
    }
}
