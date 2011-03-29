package dk.hotmovinglobster.dustytuba.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

/**
 * The purpose of this activity is to receive all intents from share dialog
 * and filter through them, passing them correctly
 */
public class ShareActivity extends Activity {

	private Intent intent;
	private int checksum;
	
	public void onCreate() {
		
	}
	
	public void onStart() {
		super.onStart();
		/* Check whether or not we are receiving from an intent */
    	intent = getIntent();
    	if(intent.getAction().equals(Intent.ACTION_SEND)) {
    		/* Get the type */
    		String intentType = intent.getType();
    		
    		/* Create a generic object to hold the data */
    		Intent i = new Intent(this, MainActivity.class);
    		
    		if(intentType.equals("text/plain")) {
    			/* The data is text */
    			String intentData = intent.getStringExtra(Intent.EXTRA_TEXT);
    			i.putExtra(MainActivity.INTENT_SEND_DATA, (String) intentData);
    			i.putExtra(MainActivity.INTENT_SEND_TEXT, (boolean) true);
    			
    			/* Calculate the hash code of the data */
        		/* For two objects to be equal, this must be equal */
    			checksum = intentData.hashCode();
    			i.putExtra(MainActivity.INTENT_SEND_CHECKSUM, checksum);
    		} else {
    			/* The data is something else (e.g. could be an image) */
    			Parcelable intentData = intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
    			i.putExtra(MainActivity.INTENT_SEND_DATA, (Parcelable) intentData);
    			i.putExtra(MainActivity.INTENT_SEND_TEXT, (boolean) false);
    			
    			/* Calculate the hash code of the data */
        		/* For two objects to be equal, this must be equal */
    			checksum = intentData.hashCode();
    			i.putExtra(MainActivity.INTENT_SEND_CHECKSUM, checksum);
    		}
 
    		//Log.v("INTENT", "IntentTest: " + intentType + " " + intentData);
    		/* Show the data as well as the type */
    		//Toast.makeText(this, "Got: " + intentData + " which is " + intentType + " ", Toast.LENGTH_LONG).show();
    		
    		/* Launch main activity to take care of sending the data */
    		startActivity(i);
    		
    		/* Stop this activity */
    		this.finish();
    	}
	}
	
}
