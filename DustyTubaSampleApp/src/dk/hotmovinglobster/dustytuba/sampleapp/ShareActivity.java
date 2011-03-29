package dk.hotmovinglobster.dustytuba.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * The purpose of this activity is to receive all intents from share dialog
 * and filter through them, passing them correctly
 */
public class ShareActivity extends Activity {

	private Intent intent;
	
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
    		Object intentData;
    		if(intentType.equals("text/plain")) {
    			/* The data is text */
    			intentData = intent.getStringExtra(Intent.EXTRA_TEXT);
    		} else {
    			/* The data is something else (e.g. could be an image) */
    			intentData = intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
    		}
 
    		Log.v("INTENT", "IntentTest: " + intentType + " " + intentData);
    		/* Show the data as well as the type*/
    		Toast.makeText(this, "Got: " + intentData + " which is " + intentType + " ", Toast.LENGTH_LONG).show();
    		
    		/* Launch main activity to take care of sending the data */
    		Intent i = new Intent(this, MainActivity.class);
    		i.putExtra(MainActivity.INTENT_SEND_DATA, true);
    		startActivity(i);
    		
    		/* Stop this activity */
    		this.finish();
    	}
	}
	
}
