package dk.hotmovinglobster.battleships;

import java.util.Arrays;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;

/**
 * Lets the user configure the game and then launches PlaceShipsActivity.
 */
public class SetupGameActivity extends CommunicationProtocolActivity {

	private Spinner gridSizeSpinner;

	/** Dialog to keep the waiting user happy **/
	private ProgressDialog dialog_waiting;
	/** Warn the waiting user that BACK cancels game */
	private AlertDialog dialog_abort_warn;
	
	private Resources res;
	
	private int rules_game_type = 1;
	
	public static final int GAME_TYPE_SHORT = 0;
	public static final int GAME_TYPE_MEDIUM = 1;
	public static final int GAME_TYPE_LONG = 2;
	public static final int GAME_TYPE_VERY_SHORT = 4;
	
	private String[] game_types_array = { "Short", "Medium", "Long", "Very short (DEBUG)" };
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "SetupGameActivity: onCreate()");
		assert( BattleshipsApplication.context().Comm != null );
		BattleshipsApplication.context().Comm.setListeningActivity( this );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setup_game);

		res = getResources();
		setupComponents();

		if ( !BattleshipsApplication.context().Comm.isServer() ) {
			showWaitingDialog();
		}
	}
	
	private void setupComponents() {
		
		///////////////////////////////////
		////////// OK BUTTON //////////////
		///////////////////////////////////
		((Button)findViewById(R.id.setup_game_btn_ok)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BattleshipsApplication.context().Comm.sendRules(rules_game_type);
				acceptRulesAndStart(rules_game_type);
			}
		});
		
		///////////////////////////////////
		/////// GAME TYPE SPINNER /////////
		///////////////////////////////////
		gridSizeSpinner = (Spinner)findViewById(R.id.setup_game_game_type);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, game_types_array);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gridSizeSpinner.setAdapter( adapter );
		gridSizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					rules_game_type = GAME_TYPE_SHORT;
				} else if (position == 1) {
					rules_game_type = GAME_TYPE_MEDIUM;
				} else if (position == 2) {
					rules_game_type = GAME_TYPE_LONG;
				} else if (position == 3) {
					rules_game_type = GAME_TYPE_VERY_SHORT;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		// Choose medium game as default
		//gridSizeSpinner.setSelection( 1 );
		
		// TODO: Switch back!
		// Choose very short game as default during development
		gridSizeSpinner.setSelection( 3 );
	}
	
	private void showWaitingDialog() {
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
		        	BattleshipsApplication.context().Comm.disconnect();
					dialog.dismiss();
					SetupGameActivity.this.finish();
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            showWaitingDialog();
		            break;
		        }		
		    }
		};
		
		dialog_waiting = new ProgressDialog(this);
		dialog_waiting.setIndeterminate(true);
		dialog_waiting.setMessage(res.getString(R.string.setup_game_waiting_for_host));
		dialog_waiting.setCancelable(true);
		dialog_waiting.show();
		dialog_waiting.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog_waiting.dismiss();
				dialog_abort_warn = new AlertDialog.Builder(SetupGameActivity.this).setMessage(R.string.place_ships_warn_abort_wait).
				setPositiveButton(android.R.string.yes, dialog_click_listener).setNegativeButton(android.R.string.no, dialog_click_listener).show();
			}
		});
	}
	
	@Override
	public void onBackPressed() {
		BattleshipsApplication.context().Comm.disconnect();
		finish();
	}

	@Override
	public void communicationDisconnected() {
		Toast.makeText(this, getString(R.string.bt_disconnnected), Toast.LENGTH_LONG).show();
		finish();
	}
	
	@Override
	public void communicationRulesReceived(int game_type) {
		acceptRulesAndStart(game_type);
	}

	private void acceptRulesAndStart(int game_type) {
		if (dialog_abort_warn != null && dialog_abort_warn.isShowing()) {
			dialog_abort_warn.dismiss();
		}
		if (dialog_waiting != null && dialog_waiting.isShowing()) {
			dialog_waiting.dismiss();
		}
		
		Log.v(BattleshipsApplication.LOG_TAG, "SetupGameActivity: acceptRulesAndStart("+game_type+")");

		switch (game_type) {
			case GAME_TYPE_SHORT:
				BattleshipsApplication.context().GRID_COLUMNS = 6;
				BattleshipsApplication.context().GRID_ROWS  = 6;
				BattleshipsApplication.context().MAX_SHIPS = new int[] { 0, 0, 2, 1, 1, 1 };
				break;
			case GAME_TYPE_MEDIUM:
			default:
				BattleshipsApplication.context().GRID_COLUMNS = 8;
				BattleshipsApplication.context().GRID_ROWS  = 8;
				BattleshipsApplication.context().MAX_SHIPS = new int[] { 0, 0, 3, 2, 2, 1 };
				break;
			case GAME_TYPE_LONG:
				BattleshipsApplication.context().GRID_COLUMNS = 10;
				BattleshipsApplication.context().GRID_ROWS  = 10;
				BattleshipsApplication.context().MAX_SHIPS = new int[] { 0, 0, 3, 2, 2, 1 };
				break;
			case GAME_TYPE_VERY_SHORT:
				BattleshipsApplication.context().GRID_COLUMNS = 4;
				BattleshipsApplication.context().GRID_ROWS  = 4;
				BattleshipsApplication.context().MAX_SHIPS = new int[] { 0, 0, 1, 0, 0, 0 };
				break;
		}

		Log.v(BattleshipsApplication.LOG_TAG, "SetupGameActivity: acceptRulesAndStart("+BattleshipsApplication.context().GRID_COLUMNS+", " +BattleshipsApplication.context().GRID_ROWS+", " +Arrays.toString(BattleshipsApplication.context().MAX_SHIPS)+")");

		Intent i = new Intent(SetupGameActivity.this, PlaceShipsActivity.class);
		startActivity(i);
		finish();
	}


}
