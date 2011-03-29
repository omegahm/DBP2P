package dk.hotmovinglobster.dustytuba.sampleapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

/**
 * The purpose of this activity is to receive all intents from share dialog
 * and filter through them, passing them correctly
 */
public class ShareActivity extends Activity {

	/* DEBUG */ 
	private static final String TAG = "DustyTubaSampleApp";
    private static final boolean D = true;
	
    private Intent intent;
	private int checksum;
	
	private static final int BT_CONNECT = 1;
	
	protected static final String INTENT_SEND_DATA = "intent_data";
	protected static final String INTENT_SEND_CHECKSUM = "intent_checksum";
	
	public void onStart() {
		super.onStart();
		/* Check whether or not we are receiving from an intent */
    	intent = getIntent();
    	if(intent.getAction().equals(Intent.ACTION_SEND)) {
    		/* Get the type */
    		String intentType = intent.getType();
    		
    		byte[] intentData;
    		if(intentType.equals("text/plain")) {
    			/* The data is text */
    			intentData = intent.getStringExtra(Intent.EXTRA_TEXT).getBytes();
    		} else {
    			/* The data is something else (e.g. could be an image) */
    			intentData = intent.getByteArrayExtra(Intent.EXTRA_STREAM);
    		}
 
    		/* Calculate the hash code of the data */
    		/* For two objects to be equal, this must be equal */
        	byte[] checksum = new Integer(intentData.hashCode()).toString().getBytes();
    		
    		/* Connect to other phone */
    		intent = new Intent(this, Connector.class);
    		
    		/* Send data */
    		intent.putExtra(INTENT_SEND_DATA, intentData);
    		
    		/* Send checksum */
    		intent.putExtra(INTENT_SEND_CHECKSUM, checksum);
    		startActivity(intent);
    		
    		/* Stop this activity */
    		this.finish();
    	}
	}	
}
