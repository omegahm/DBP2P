package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	    if (requestCode == REQUEST_DUSTYTUBA) {
	        if (resultCode == RESULT_OK) {
	            GameContext.singleton.Comm = new CommunicationProtocol(BtConnection.getConnection());
				Intent i = new Intent(this, PlaceShipsActivity.class);
				startActivity(i);
	        } else {
	            Toast.makeText(this, "Failed to find opponent, please try again. (HC)",
	                           Toast.LENGTH_LONG).show();
	        }
	    }
	}
	
}