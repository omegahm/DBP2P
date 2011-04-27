package dk.hotmovinglobster.dustytuba.apitest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.bumpapi.BumpAPI;

import dk.hotmovinglobster.dustytuba.api.*;
import dk.hotmovinglobster.dustytuba.api.BtAPI.BtDisconnectReason;
import dk.hotmovinglobster.dustytuba.bt.BluetoothConnector;
import dk.hotmovinglobster.dustytuba.id.*;

public class MainActivity extends Activity implements BtAPIListener {
	
	private static final String LOG_TAG = "APITest";
	
	protected static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	private String other_mac;

	protected String chosenIDProvider = "NONE";
	
	protected static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initializeButtons();
    }

	private void initializeButtons() {
        ((Button)findViewById(R.id.btnLaunchDustyTubaFakeAlice)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, FakeIPActivity.class);
				i.putExtra(BtAPI.EXTRA_IP_MAC, "90:21:55:a1:a5:67"); // HTC Desire (Thomas) (ALICE)
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
				startActivityForResult(i, BtAPI.REQUESTCODE_IDENTITY_PROVIDER);
				chooseIDProvider("FAKE_ALICE"); // Choose for FULL
			}
		});
        
        ((Button)findViewById(R.id.btnLaunchDustyTubaFakeBob)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, FakeIPActivity.class);
				i.putExtra(BtAPI.EXTRA_IP_MAC, "00:23:d4:34:45:d7"); // HTC Hero (Thomas) (BOB)
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
				startActivityForResult(i, BtAPI.REQUESTCODE_IDENTITY_PROVIDER);
				chooseIDProvider("FAKE_BOB"); // Choose for FULL
			}
		});

        ((Button)findViewById(R.id.btnLaunchDustyTubaManual)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, ManualIPActivity.class);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Manual activity");
				startActivityForResult(i, BtAPI.REQUESTCODE_IDENTITY_PROVIDER);
				chooseIDProvider("MANUAL"); // Choose for FULL
			}
		});

        ((Button)findViewById(R.id.btnLaunchDustyTubaBump)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, BumpIPActivity.class);
				if (mBluetoothAdapter != null && mBluetoothAdapter.getName() != null)
					i.putExtra(BumpAPI.EXTRA_USER_NAME, mBluetoothAdapter.getName());
				i.putExtra(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Bump! activity");
				startActivityForResult(i, BtAPI.REQUESTCODE_IDENTITY_PROVIDER);
				chooseIDProvider("BUMP");
			}
		});
        
        ((Button)findViewById(R.id.btnLaunchDustyTubaMultiple)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] providers = {BtAPI.IDENTITY_PROVIDER_MANUAL, BtAPI.IDENTITY_PROVIDER_BUMP};
				Bundle b = new Bundle();
				b.putStringArray(BtAPI.EXTRA_IP_PROVIDERS, providers);
				b.putString(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_MULTIPLE, b);

				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Multiple activity");
				startActivityForResult(i, BtAPI.REQUESTCODE_IDENTITY_PROVIDER);
				chooseIDProvider("MULTIPLE");
			}
		});
        
        ((Button)findViewById(R.id.btnSetupBT)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// FIXME: HACK: Not the proper way to go about this, but it will do for now...
				Intent i = new Intent(MainActivity.this, BluetoothConnector.class);
				boolean isServer = false; // try to connect as client in addition to being a server
				String BT_UUID = "fa87c0e0-afac-12de-8a39-a80f200c9a96";
				String BT_SDP_NAME = "DustyTubaAPI_SDP_NAME";
				// Change to use BTAPI CONSTANTS
				//b.putExtra(BtAPI.EXTRA_BT_MAC, other_mac);
				i.putExtra(BluetoothConnector.BT_CONN_DATA.SERVER.name(), isServer);
				i.putExtra(BluetoothConnector.BT_CONN_DATA.MAC.name(), other_mac);
				i.putExtra(BluetoothConnector.BT_CONN_DATA.UUID.name(), BT_UUID);
				i.putExtra(BluetoothConnector.BT_CONN_DATA.SDP_NAME.name(), BT_SDP_NAME);
				Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
				startActivityForResult(i, BtAPI.REQUESTCODE_SETUP_BT);
			}
		});
		
		
        ((Button)findViewById(R.id.btnDustyTuba)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (chosenIDProvider == "FAKE_ALICE"){
					Bundle b = new Bundle();
					b.putString(BtAPI.EXTRA_IP_MAC, "90:21:55:a1:a5:67"); // HTC Desire (Thomas) (ALICE)
					Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_FAKE, b);
					Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
					startActivityForResult(i, BtAPI.REQUESTCODE_DUSTYTUBA);
				} else if (chosenIDProvider == "FAKE_BOB"){
					Bundle b = new Bundle();
					b.putString(BtAPI.EXTRA_IP_MAC, "00:23:d4:34:45:d7"); // HTC Hero (Thomas) (BOB)
					Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_FAKE, b);
					Log.i(LOG_TAG, "MainActivity: Launching BtAPI Fake activity");
					startActivityForResult(i, BtAPI.REQUESTCODE_DUSTYTUBA);
				} else if (chosenIDProvider == "MANUAL"){
					Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_MANUAL);
					Log.i(LOG_TAG, "MainActivity: Launching BtAPI Manual activity");
					startActivityForResult(i, BtAPI.REQUESTCODE_DUSTYTUBA);
				} else if (chosenIDProvider == "BUMP"){
					Bundle b = new Bundle();
					b.putString(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
					if (mBluetoothAdapter != null && mBluetoothAdapter.getName() != null)
						b.putString(BumpAPI.EXTRA_USER_NAME, mBluetoothAdapter.getName());
					Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_BUMP, b);
					Log.i(LOG_TAG, "MainActivity: Launching BtAPI Bump! activity");
					startActivityForResult(i, BtAPI.REQUESTCODE_DUSTYTUBA);
				} else if (chosenIDProvider == "MULTIPLE" ) {
					String[] providers = {BtAPI.IDENTITY_PROVIDER_MANUAL, BtAPI.IDENTITY_PROVIDER_BUMP};
					Bundle b = new Bundle();
					b.putStringArray(BtAPI.EXTRA_IP_PROVIDERS, providers);
					b.putString(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
					Intent i = BtAPI.getIntent(MainActivity.this, BtAPI.IDENTITY_PROVIDER_MULTIPLE, b);
				} else {
					// TODO: Toast no ID chosen
				}
			}
		});
	}
	
    protected void chooseIDProvider(String str) {
		chosenIDProvider = str; // Choose for FULL
		((TextView) findViewById(R.id.lblchosenID)).setText( str );
	}

	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	// TODO: Move ID Provider + Setup BT to GenericIPActivity (remove from here)
    	Log.i(LOG_TAG, "MainActivity: Returned from activity");
    	switch(requestCode){ // Use of switch rather than if/then/else ensures no duplicate result codes
    	case BtAPI.REQUESTCODE_IDENTITY_PROVIDER:
        	Log.i(LOG_TAG, "MainActivity: Returned from BtAPI ID activity");
        	switch(resultCode){
        	case RESULT_CANCELED:
        		Log.i(LOG_TAG, "MainActivity: Reason: Cancelled");
        		break;
    		case RESULT_OK:
            	Log.i(LOG_TAG, "MainActivity: Reason: OK");
        		Log.i(LOG_TAG, "MainActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
        		other_mac = data.getStringExtra(BtAPI.EXTRA_IP_MAC);
	        	((TextView) findViewById(R.id.lblMAC)).setText( other_mac );
	        	break;
        	}
    		break;
    	case BtAPI.REQUESTCODE_SETUP_BT:
        	Log.i(LOG_TAG, "MainActivity: Returned from BtAPI BT SETUP activity");
        	switch(resultCode){
        	case RESULT_CANCELED:
        		Log.i(LOG_TAG, "MainActivity: Reason: Cancelled");
        		break;
    		case RESULT_OK:
    			BtConnection conn = (BtConnection)data.getParcelableExtra(BtAPI.EXTRA_BT_CONNECTION);
    			conn.setListener(this);
            	Log.i(LOG_TAG, "MainActivity: Reason: OK");
        		Log.i(LOG_TAG, "MainActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
	        	break;
        	}
    		break;
    	case BtAPI.REQUESTCODE_DUSTYTUBA:
        	Log.i(LOG_TAG, "MainActivity: Returned from BtAPI complete ID + SETUP activity");
        	switch(resultCode){
        	case RESULT_CANCELED:
        		Log.i(LOG_TAG, "MainActivity: Reason: Cancelled");
        		break;
    		case RESULT_OK:
            	Log.i(LOG_TAG, "MainActivity: Reason: OK");
        		Log.i(LOG_TAG, "MainActivity: with data (Size "+data.getExtras().size()+": "+data.getExtras().keySet()+")");
        		other_mac = data.getStringExtra(BtAPI.EXTRA_IP_MAC);
	        	((TextView) findViewById(R.id.lblMAC)).setText( other_mac );
	        	break;
        	}
        	// TODO Final usage of API (blocked: till other two parts are done)
    		break;
    	}
    }



	@Override
	public void btDataReceived(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void btDisconnect(BtDisconnectReason arg0) {
		// TODO Auto-generated method stub
		
	}
}