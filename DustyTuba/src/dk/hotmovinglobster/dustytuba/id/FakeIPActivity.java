package dk.hotmovinglobster.dustytuba.id;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class FakeIPActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	// Get data
        Bundle extras = getIntent().getExtras();
        String mac = extras.getString("MAC"); // TODO: grab from somewhere
    	
        if (mac == null){
        	setResult(RESULT_CANCELED);
        	finish();
        }
        
    	// Return data
    	Intent data = new Intent();
        data.putExtra("MAC",mac); // TODO: grab from somewhere
    	setResult(RESULT_OK, data);
    	finish();
    }
}
