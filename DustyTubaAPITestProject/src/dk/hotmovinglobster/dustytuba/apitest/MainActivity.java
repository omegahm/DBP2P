package dk.hotmovinglobster.dustytuba.apitest;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Button btnLaunchDustyTubaFake;
	private Button btnLaunchDustyTubaManual;
	
	private static final String LOG_TAG = "APITest";
	
	private Resources res;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        res = getResources();
        
        initializeButtons();
    }

	private void initializeButtons() {
		btnLaunchDustyTubaFake = (Button)findViewById(R.id.btnLaunchDustyTubaFake);
        btnLaunchDustyTubaFake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle b = new Bundle();
				b.putString(BtAPI.EXTRA_IP_MAC, "00:00:00:00:00:00");
				Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_FAKE, b);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI activity");
				startActivityForResult(i, 0);
			}
		});

		btnLaunchDustyTubaManual = (Button)findViewById(R.id.btnLaunchDustyTubaManual);
        btnLaunchDustyTubaManual.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_MANUAL);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI activity");
				startActivityForResult(i, 0);
			}
		});
}
	
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	Log.i(LOG_TAG, "MainActivity: Returned from BtAPI activity");
    	if (resultCode == RESULT_CANCELED ) {
        	Log.i(LOG_TAG, "MainActivity: Reason: Cancelled");
    	} else if (resultCode == RESULT_OK) {
        	Log.i(LOG_TAG, "MainActivity: Reason: OK");
    		Log.i(LOG_TAG, "MainActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
    	}
    }
}