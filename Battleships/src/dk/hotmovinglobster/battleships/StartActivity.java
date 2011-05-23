package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.bumpapi.BumpAPI;

import dk.hotmovinglobster.battleships.comm.CommunicationProtocol;
import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.api.BtConnection;

public class StartActivity extends Activity {

	protected static final int REQUEST_DUSTYTUBA = 20;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(GameContext.LOG_TAG, "StartActivity.onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.start);
		
		
		initializeButtons();
	}
	
	private void initializeButtons() {
		((Button)findViewById(R.id.start_place_ships_activity)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), LocalPlaceShipsActivity.class);
				startActivityForResult(i, 0);
			}
		});

	
		((Button)findViewById(R.id.start_btn_find_opponent)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] providers = {BtAPI.IDENTITY_PROVIDER_MANUAL, BtAPI.IDENTITY_PROVIDER_BUMP,
                        BtAPI.IDENTITY_PROVIDER_PAIRED};
		        Bundle b = new Bundle();
		        b.putStringArray(BtAPI.EXTRA_IP_PROVIDERS, providers);
		        b.putString(BumpAPI.EXTRA_API_KEY, "273a39bb29d342c2a9fcc2e61158cbba");
		        Intent i = BtAPI.getIntent(StartActivity.this, BtAPI.IDENTITY_PROVIDER_MULTIPLE, b);
		        startActivityForResult(i, REQUEST_DUSTYTUBA);

		    }
		});

		((Button)findViewById(R.id.start_btn_find_opponent_debug)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		        Bundle b = new Bundle();
		        String other_mac;
		        String my_mac = BtAPI.getBluetoothAddress();
		        if (my_mac.equals( "00:23:D4:34:45:D7" )) { // Thomas' Hero
		        	other_mac = "90:21:55:A1:A5:8D";
		        } else { // Jesper's Desire
		        	other_mac = "00:23:D4:34:45:D7";
		        }
		        b.putString(BtAPI.EXTRA_IP_MAC, other_mac);
		        Intent i = BtAPI.getIntent(StartActivity.this, BtAPI.IDENTITY_PROVIDER_FAKE, b);
		        startActivityForResult(i, REQUEST_DUSTYTUBA);
		    }
		});
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_DUSTYTUBA) {
	        if (resultCode == RESULT_OK) {
	            GameContext.singleton.Comm = new CommunicationProtocol(BtConnection.getConnection());
	            Toast.makeText(this, "Connected to opponent, please place your ships (HC)", Toast.LENGTH_LONG).show();
				Intent i = new Intent(this, PlaceShipsActivity.class);
				startActivity(i);
	        } else {
	            Toast.makeText(this, "Failed to find opponent, please try again. (HC)",
	                           Toast.LENGTH_LONG).show();
	        }
	    }
	}
	
}