package dk.hotmovinglobster.battleships;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.bumpapi.BumpAPI;

import dk.hotmovinglobster.battleships.comm.CommunicationProtocol;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;
import dk.hotmovinglobster.dustytuba.api.BtAPI;
import dk.hotmovinglobster.dustytuba.api.BtConnection;

public class EndActivity extends CommunicationProtocolActivity {

	protected static final int REQUEST_DUSTYTUBA = 20;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "EndActivity.onCreate()");
		// Commented out, since while debugging we haven't got any Comm setup
		//BattleshipsApplication.context().Comm.setListeningActivity( this );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.end_screen);
		
    	// Get data
    	final Intent thisIntent = getIntent();
        final Bundle extras = thisIntent.getExtras();
        boolean winner = extras.getBoolean(BattleshipsApplication.EXTRA_END_WINNER);
		
		toggleWinOrLoose(winner);
		
		initializeButtons();
	}
	
	/**
	 * Changes endscreen UI to reflect whether user won or lost
	 * @param won did the user win?
	 */
	private void toggleWinOrLoose(boolean won) {
		TextView tv = ((TextView)findViewById(R.id.end_win_or_loose_text));
		ImageView iv = ((ImageView)findViewById(R.id.end_ship));

		if (won){
			tv.setText(getString(R.string.win));
			iv.setImageResource(R.drawable.ship_trans);
		} else {
			tv.setText(getString(R.string.loose));
			iv.setImageResource(R.drawable.battleship_hit_large);
		}
		//@id/win_or_loose_text
		//@id/end_ship
		//@drawable/battleship_hit
	}

	private void initializeButtons() {
		((Button)findViewById(R.id.btn_playagain)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), LocalPlaceShipsActivity.class);
				startActivity(i);
			}
		});

	
		((Button)findViewById(R.id.btn_newopponent)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] providers = {BtAPI.IDENTITY_PROVIDER_MANUAL, BtAPI.IDENTITY_PROVIDER_BUMP,
                        BtAPI.IDENTITY_PROVIDER_PAIRED};
		        Bundle b = new Bundle();
		        b.putStringArray(BtAPI.EXTRA_IP_PROVIDERS, providers);
		        b.putString(BumpAPI.EXTRA_API_KEY, "273a39bb29d342c2a9fcc2e61158cbba");
		        Intent i = BtAPI.getIntent(EndActivity.this, BtAPI.IDENTITY_PROVIDER_MULTIPLE, b);
		        startActivityForResult(i, REQUEST_DUSTYTUBA);
		    }
		});

		/*
		((Button)findViewById(R.id.btn_exit)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
		    }
		});
		*/
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		Log.v(BattleshipsApplication.LOG_TAG, "EndActivity: onActivityResult(): requestCode="+requestCode+", resultCode=" + resultCode);
	    if (requestCode == REQUEST_DUSTYTUBA) {
	        if (resultCode == RESULT_OK) {
	            BattleshipsApplication.context().Comm = new CommunicationProtocol(BtConnection.getConnection());
				Intent i = new Intent(this, SetupGameActivity.class);
				startActivity(i);
	        } else {
	            Toast.makeText(this, "Failed to find opponent, please try again. (HC)",
	                           Toast.LENGTH_LONG).show();
	        }
	    }
	}
	
	/*
	 * Don't offer the opportunity to connect to the same opponent if BT connection is lost.
	 * 
	 * (non-Javadoc)
	 * @see dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity#communicationDisconnected()
	 */
	@Override
	public void communicationDisconnected() {
		Toast.makeText(this, getString(R.string.bt_disconnnected), Toast.LENGTH_LONG).show();
		finish();
	}
	
	/**
	 * 'Quits' the program by finishing this activity and thus moving to start screeen.
	 * Subsequently application is moved to background, yielding the illusion of quitting.
	 */
	@Override
	public void onBackPressed() {
		finish();
		moveTaskToBack(true);
	}
}