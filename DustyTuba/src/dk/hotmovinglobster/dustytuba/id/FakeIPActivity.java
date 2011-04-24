package dk.hotmovinglobster.dustytuba.id;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Identity provider that simply returns the MAC address you supply to it.
 * This enables you to use a MAC address obtained through other means.
 * 
 * @author Thomas
 */
public class FakeIPActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.i(BtAPI.LOG_TAG, "FakeIPActivity: Created");

    	Intent thisIntent = getIntent();
    	Log.i(BtAPI.LOG_TAG, "FakeIPActivity: with data (Size "+thisIntent.getExtras().size()+": "+thisIntent.getExtras().keySet()+")");
    	// Get data
        Bundle extras = thisIntent.getExtras();
        String mac = extras.getString(BtAPI.EXTRA_IP_MAC);
        
    	Log.i(BtAPI.LOG_TAG, "FakeIPActivity: Received MAC Address: " + mac);
    	
        if (mac == null){
        	setResult(RESULT_CANCELED);
        	finish();
        } else {
        	mac = mac.toUpperCase();
        	if (!BluetoothAdapter.checkBluetoothAddress(mac)){
            	setResult(RESULT_CANCELED);
            	finish();
        	}
        }
        
    	// Return data
    	Intent data = new Intent();
        data.putExtra(BtAPI.EXTRA_IP_MAC,mac);
    	setResult(RESULT_OK, data);
    	
    	Log.i(BtAPI.LOG_TAG, "FakeIPActivity: Finishing");
    	finish();
    }
}
