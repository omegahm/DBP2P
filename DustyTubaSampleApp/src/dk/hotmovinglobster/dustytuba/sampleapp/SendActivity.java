package dk.hotmovinglobster.dustytuba.sampleapp;

import android.app.Activity;
import android.content.Intent;

public class SendActivity extends Activity {

	public void onStart() {
		super.onStart();
		
		/* If sending through a "send to..." dialog, we want to grab the data */
        Intent intent = getIntent();
        if(intent.hasExtra(MainActivity.INTENT_SEND_DATA)) {
        	Object intentData = null;
	        if(intent.getBooleanExtra(MainActivity.INTENT_SEND_TEXT, false)) {
	        	intentData = (String) intent.getStringExtra(MainActivity.INTENT_SEND_DATA);
	        } else if(!intent.getBooleanExtra(MainActivity.INTENT_SEND_TEXT, true)) {
	        	intentData = intent.getParcelableExtra(MainActivity.INTENT_SEND_DATA);
	        } 
	        
	        if(intentData != null) {
	        	/* We have data to send */
	        	return;
	        }
         
	        /* Here is a way to check the checksums after we have done bluetooth transfer */
	        /*try {
	            int checksum = intent.getIntExtra(MainActivity.INTENT_SEND_CHECKSUM, 0);
	        	if(checksum != intentData.hashCode()) {
	        		Toast.makeText(this, "Something went wrong while transfering data", Toast.LENGTH_LONG).show();
	        	} else {
	        		Toast.makeText(this, "Checksums match", Toast.LENGTH_SHORT).show();
	        	}
	        } catch(Exception e) {
	        	Toast.makeText(this, "Something went wrong while transfering data", Toast.LENGTH_LONG).show();
	        }*/
	        
        	/* TODO: Now we need to send the data via bluetooth */
	        
        }
	}	
}
