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
import dk.hotmovinglobster.battleships.comm.CommunicationProtocol;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;
import dk.hotmovinglobster.dustytuba.api.BtConnection;

public class EndActivity extends CommunicationProtocolActivity {

	protected static final int REQUEST_DUSTYTUBA = 20;
	
	private TextView opponent_info;
	private Button btn_play_again;
	private ImageView header;
	
	private boolean opponentWantsPlayAgain = false;
	private boolean userWantsPlayAgain = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "EndActivity.onCreate()");
		BattleshipsApplication.context().Comm.setListeningActivity( this );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.end_screen);

		// Get data
    	final Intent thisIntent = getIntent();
        final Bundle extras = thisIntent.getExtras();
        boolean winner = extras.getBoolean(BattleshipsApplication.EXTRA_END_WINNER);
		
		opponent_info = (TextView)findViewById(R.id.end_opponent_info);
		header = (ImageView)findViewById(R.id.end_image_header);

		toggleWinOrLoose(winner);
		
		initializeButtons();
		
	}
	
	/**
	 * Changes endscreen UI to reflect whether user won or lost
	 * @param won did the user win?
	 */
	private void toggleWinOrLoose(boolean won) {
		ImageView iv = ((ImageView)findViewById(R.id.end_ship));

		if (won){
			header.setImageResource(R.drawable.header_victory);
			iv.setImageResource(R.drawable.ship_trans);
		} else {
			header.setImageResource(R.drawable.header_defeat);
			iv.setImageResource(R.drawable.ship_hit_trans);
		}
		//@id/win_or_loose_text
		//@id/end_ship
		//@drawable/battleship_hit
	}

	private void initializeButtons() {
		btn_play_again = (Button)findViewById(R.id.end_btn_play_again);
		btn_play_again.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				synchronized( this ) {
					BattleshipsApplication.context().Comm.sendAuxilliaryMessage( CommunicationProtocol.PROTOCOL_PLAY_AGAIN );
					userWantsPlayAgain = true;
					if (opponentWantsPlayAgain) {
						playAgain();
					}
				}
			}
		});

	
		((Button)findViewById(R.id.end_btn_back_to_main)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				disconnectAndEndGame();
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
		opponent_info.setText( getString( R.string.end_opponent_disconnected ) );
		
		// 8 is GONE
		btn_play_again.setVisibility( 8 );
	}
	
	/**
	 * 'Quits' the program by finishing this activity and thus moving to start screeen.
	 * Subsequently application is moved to background, yielding the illusion of quitting.
	 */
	@Override
	public void onBackPressed() {
		disconnectAndEndGame();
		//moveTaskToBack(true);
	}
	
	private void disconnectAndEndGame() {
		BattleshipsApplication.context().Comm.disconnect();
		finish();
	}
	
	@Override
	public void communicationAuxilliaryMessage(byte message) {
		if ( message == CommunicationProtocol.PROTOCOL_PLAY_AGAIN ) {
			synchronized( this ) {
				if (userWantsPlayAgain) {
					playAgain();
				} else {
					opponentWantsPlayAgain = true;
					opponent_info.setText( R.string.end_opponent_wants_to_play_again );
				}
			}
		}
	}
	
	private void playAgain() {
		Intent i = new Intent(this, SetupGameActivity.class);
		startActivity(i);
		finish();
	}

}