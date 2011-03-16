package dk.hotmovinglobster.dustytuba.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReceiveActivity extends Activity {

	private Intent intent;
	public void onCreate() {
		
	}
	
	public void onStart() {
		super.onStart();
		/* Check whether or not we are receiving from an intent */
    	intent = getIntent();
    	if(intent.getAction().equals(Intent.ACTION_SEND)) {
    		String intentType = intent.getType();
    		
    		Object intentData;
    		if(intentType.equals("text/plain")) {
    			intentData = intent.getStringExtra(Intent.EXTRA_TEXT);
    		} else {
    			intentData = intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
    		}
 
    		Log.v("INTENT", "IntentTest: " + intentType + " " + intentData);
    		Toast.makeText(this, "Got: " + intentData + " which is " + intentType + " ", Toast.LENGTH_LONG).show();
    	}
	}
	
}
