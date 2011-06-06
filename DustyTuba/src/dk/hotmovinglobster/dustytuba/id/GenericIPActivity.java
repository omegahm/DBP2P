package dk.hotmovinglobster.dustytuba.id;

import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.api.BtConnection;
import dk.hotmovinglobster.dustytuba.bt.BluetoothConnectionManager;

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
	
	private Handler mHandler;

//	private static final String LOG_TAG = "APITest:GenericIPActivity: ";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mHandler = new Handler();
    	
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
			    		final String other_mac = data.getStringExtra(BtAPI.EXTRA_IP_MAC);
			    		Log.v(BtAPI.LOG_TAG, "Other MAC address: " + other_mac);
			    		// Identity received, start BT connection
			    		mHandler.post(new Runnable() {
							@Override
							public void run() {
					    		startBT(other_mac);
							}
						});
			        	break;
		    	}
				break;
	    	case REQUEST_ENABLE_BT:
	    		Log.d(BtAPI.LOG_TAG, "GenericIPActivity: Returned from bluetooth activation");
		    	switch(resultCode){
			    	case RESULT_CANCELED:
			    		Log.v(BtAPI.LOG_TAG, "Result: Cancelled");
			        	setResult(RESULT_CANCELED);
			        	finish();
			    		break;
					case RESULT_OK:
			        	Log.v(BtAPI.LOG_TAG, "Result: OK");
			    		bluetoothEnabled();
			        	break;
		    	}
				break;
    	}
    		
    }

	private void startBT(final String other_mac) {
		final ProgressDialog dialog = ProgressDialog.show(this, "", 
                getResources().getString( BtAPI.res(this, "string", "dustytuba_setting_up_connection") ), true);
		dialog.setCancelable( true );
		dialog.setOnCancelListener( new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				setResult(RESULT_CANCELED);
				finish();				
			}
		});
		dialog.show();

		final UUID uuid = (UUID) thisIntent.getSerializableExtra( BtAPI.EXTRA_BT_UUID );
		final String BT_SDP_NAME = "DustyTubaAPI_SDP_NAME";

		// Start initialization of bluetooth connection in a separate thread
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				BluetoothConnectionManager bcm = new BluetoothConnectionManager(GenericIPActivity.this, other_mac, uuid, BT_SDP_NAME);
				bcm.setupConnection();
				
				BtConnection btConn = null;
				
				final int pollInterval = 200;
//				final int pollTimeout = 5000;
				int pollCount = 0;
				
				Log.v(BtAPI.LOG_TAG, "GenericIPActivity: Thread: Entering poll loop");
				
				// Repeat until connection acquired or timeout time reached
//				while ( !bcm.isDone() || pollCount * pollInterval < pollTimeout ) {
				while ( !bcm.isDone() ) {
										try {
//						Log.v(BtAPI.LOG_TAG, "GenericIPActivity: Thread: In loop, about to sleep... (pollCount="+pollCount+")");
						Thread.sleep(pollInterval);
//						Log.v(BtAPI.LOG_TAG, "GenericIPActivity: Thread: In loop, sleep finished...");
					} catch (InterruptedException e) {
						Log.w(BtAPI.LOG_TAG, "GenericIPActivity: Thread: In loop, interrupted...");
						break;
					}
					pollCount++;

				}

				Log.v(BtAPI.LOG_TAG, "GenericIPActivity: Thread: Out of loop");

				btConn = bcm.getConnectionObject();

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
		}).start();
		
	}
}
