package dk.hotmovinglobster.dustytuba.apitest;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpConnectFailedReason;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.bt.BluetoothConnector;
import dk.hotmovinglobster.dustytuba.id.GenericIPActivity;
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
	private Button btnLaunchDustyTubaBump;
	
	private static final String LOG_TAG = "APITest";
	
	protected static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	private String other_mac;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initializeButtons();
    }

	private void initializeButtons() {
		btnLaunchDustyTubaFake = (Button)findViewById(R.id.btnLaunchDustyTubaFakeAlice);
        btnLaunchDustyTubaFake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle b = new Bundle();
				b.putString(BtAPI.EXTRA_IP_MAC, "90:21:55:a1:a5:67"); // HTC Desire (Thomas) (ALICE)
				Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_FAKE, b);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
				startActivityForResult(i, BtAPI.RESULTCODE_IDENTITY_PROVIDER);
			}
		});
        
		btnLaunchDustyTubaFake = (Button)findViewById(R.id.btnLaunchDustyTubaFakeBob);
        btnLaunchDustyTubaFake.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle b = new Bundle();
				b.putString(BtAPI.EXTRA_IP_MAC, "00:23:d4:34:45:d7"); // HTC Hero (Thomas) (BOB)
				Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_FAKE, b);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
				startActivityForResult(i, BtAPI.RESULTCODE_IDENTITY_PROVIDER);
			}
		});

		btnLaunchDustyTubaManual = (Button)findViewById(R.id.btnLaunchDustyTubaManual);
        btnLaunchDustyTubaManual.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_MANUAL);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Manual activity");
				startActivityForResult(i, BtAPI.RESULTCODE_IDENTITY_PROVIDER);
			}
		});

		btnLaunchDustyTubaBump = (Button)findViewById(R.id.btnLaunchDustyTubaBump);
        btnLaunchDustyTubaBump.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle b = new Bundle();
				b.putString(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_BUMP, b);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Bump! activity");
				startActivityForResult(i, BtAPI.RESULTCODE_IDENTITY_PROVIDER);
			}
		});
        
        ((Button)findViewById(R.id.btnSetupBT)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle b = new Bundle();
				b.putString(BtAPI.EXTRA_BT_MAC, other_mac);
				//	protected static final String BT_UUID = "fa87c0e0-afac-12de-8a39-a80f200c9a96";
				//	protected static final String BT_SDP_NAME = TAG;
				
				// FIXME: HACK: Not the proper way to go about this, but it will do for now...
				Intent i = new Intent(MainActivity.this, BluetoothConnector.class);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
				startActivityForResult(i, BtAPI.RESULTCODE_SETUP_BT);
			}
		});
	}
	
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	Log.i(LOG_TAG, "MainActivity: Returned from activity");
    	switch(requestCode){ // Use of switch rather than if/then/else ensures no duplicate result codes
    	case BtAPI.RESULTCODE_IDENTITY_PROVIDER:
        	Log.i(LOG_TAG, "MainActivity: Returned from BtAPI ID activity");
        	if (resultCode == RESULT_CANCELED ) {
            	Log.i(LOG_TAG, "MainActivity: Reason: Cancelled");
        	} else if (resultCode == RESULT_OK) {
            	Log.i(LOG_TAG, "MainActivity: Reason: OK");
        		Log.i(LOG_TAG, "MainActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
        	}
        	other_mac = data.getStringExtra(BtAPI.EXTRA_IP_MAC);
        	// TODO: Launch BT Setup automatically? For now just manual (TODO: perhaps enable button/label?)
    		break;
    	case BtAPI.RESULTCODE_SETUP_BT:
        	Log.i(LOG_TAG, "MainActivity: Returned from BtAPI BT SETUP activity");
        	if (resultCode == RESULT_CANCELED ) {
            	Log.i(LOG_TAG, "MainActivity: Reason: Cancelled");
        	} else if (resultCode == RESULT_OK) {
            	Log.i(LOG_TAG, "MainActivity: Reason: OK");
        		Log.i(LOG_TAG, "MainActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
        	}
        	// TODO handle connection object
    		break;
    	case BtAPI.RESULTCODE_ID_AND_SETUP:
        	Log.i(LOG_TAG, "MainActivity: Returned from BtAPI complete ID + SETUP activity");
        	if (resultCode == RESULT_CANCELED ) {
            	Log.i(LOG_TAG, "MainActivity: Reason: Cancelled");
        	} else if (resultCode == RESULT_OK) {
            	Log.i(LOG_TAG, "MainActivity: Reason: OK");
        		Log.i(LOG_TAG, "MainActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
        	}
        	// TODO after other two parts are done
    		break;
    	}
    }
}