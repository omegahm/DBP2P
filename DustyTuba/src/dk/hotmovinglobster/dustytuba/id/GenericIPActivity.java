package dk.hotmovinglobster.dustytuba.id;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.bt.BluetoothConnector;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Generic identity provider activity starter class.
 * 
 * Expects all identity provider acitivities to return a MAC address,
 * and returns a bluetooth connection to this MAC address.
 *
 * @author Jesper
 */
public class GenericIPActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Created");
    	Intent thisIntent = getIntent();
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: with data (Size "+thisIntent.getExtras().size()+": "+thisIntent.getExtras().keySet()+")");
    	
    	String ipClass = thisIntent.getStringExtra( BtAPI.EXTRA_IP_CLASS );
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Received '" + ipClass + "' as subclass");
/*    	
    	// Name of package of the identity provider activity
    	// Should be 'dk.hotmovinglobster.dustytuba.id' for local identity provider
    	String ipPackage = fullClass.substring(0, fullClass.lastIndexOf('.') );
    	// Name of class in package. Must start with leading . (e.g. '.FakeIPActivity')
    	String ipClass = fullClass.substring(fullClass.lastIndexOf('.'));
    	
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: subclass package: " + ipPackage);
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: subclass name: " + ipClass);
*/    	
    	Intent newIntent = new Intent();
    	newIntent.setClassName(this, ipClass);
    	//newIntent.setComponent( new ComponentName( ipPackage, ipClass ) );
    	
    	if (thisIntent.hasExtra( BtAPI.EXTRA_IP_BUNDLE )) {
    		Bundle b = thisIntent.getBundleExtra( BtAPI.EXTRA_IP_BUNDLE );
        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Received bundle (Size "+b.size()+": "+b.keySet()+")");
    		newIntent.replaceExtras( b );
    	}
    	
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Starting subactivity");
    	startActivityForResult(newIntent, 0);
    	
    }
    
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	// TODO: Move ID Provider + Setup BT to here from MainActivity
    	// TODO: For now implementing in MainActivity since that's easier
    	if (resultCode == RESULT_CANCELED) {
        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned (Result: cancel)");
        	setResult( RESULT_CANCELED );
    	} else if (resultCode == RESULT_OK) {
        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned (Result: OK)");
        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned MAC " + data.getStringExtra( BtAPI.EXTRA_IP_MAC ));
        	setResult( RESULT_OK, data );
    	} else {
        	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Subactivity returned (Result: "+resultCode+")");
    		setResult( resultCode );
    	}    	
    	Log.i(BtAPI.LOG_TAG, "GenericIPActivity: Finishing");
    	finish();
    }
}
