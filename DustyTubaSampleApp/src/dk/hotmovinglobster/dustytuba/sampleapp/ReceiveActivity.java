package dk.hotmovinglobster.dustytuba.sampleapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ReceiveActivity extends Activity {

	protected static final String INTENT_RECEIVE_DATA = "intent_receive_data";
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, ConnectorActivity.class);
		
		/* Prepare to receive data */
		intent.putExtra(INTENT_RECEIVE_DATA, true);
		
		startActivity(intent);
	}
	
}
