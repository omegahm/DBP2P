package dk.hotmovinglobster.battleships;

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

public class SetupGameActivity extends CommunicationProtocolActivity {

	private Spinner gridSizeSpinner;
	private Spinner singleTileShipsSpinner;

	private ProgressDialog dialog_waiting;
	private AlertDialog dialog_abort_warn;
	
	private Resources res;
	
	private int rules_columns = 4;
	private int rules_rows = 4;
	private int rules_single_tile_ships = 3;
	
	private String[] grid_sizes_array = { "6 x 6", "8 x 8", "10 x 10" };
	private String[] single_tile_ships_array = { "3", "4", "5", "6", "7", "8", "9", "10" };
	
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
				BattleshipsApplication.context().Comm.sendRules(rules_columns, rules_rows, rules_single_tile_ships);
				acceptRulesAndStart(rules_columns, rules_rows, rules_single_tile_ships);
			}
		});
		
		///////////////////////////////////
		/////// GRID SIZE SPINNER /////////
		///////////////////////////////////
		gridSizeSpinner = (Spinner)findViewById(R.id.setup_game_grid_size);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, grid_sizes_array);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		gridSizeSpinner.setAdapter( adapter );
		gridSizeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					rules_columns = rules_rows = 6;
				} else if (position == 1) {
					rules_columns = rules_rows = 8;
				} else if (position == 2) {
					rules_columns = rules_rows = 10;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		// Choose 6x6 grid as default
		gridSizeSpinner.setSelection( 1 );
		
		///////////////////////////////////
		//// SINGLE TILE SHIPS SPINNER ////
		///////////////////////////////////
		singleTileShipsSpinner = (Spinner)findViewById(R.id.setup_game_single_tile_ships);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, single_tile_ships_array);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		singleTileShipsSpinner.setAdapter( adapter );
		singleTileShipsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					rules_single_tile_ships = 3;
				} else if (position == 1) {
					rules_single_tile_ships = 4;
				} else if (position == 2) {
					rules_single_tile_ships = 5;
				} else if (position == 3) {
					rules_single_tile_ships = 6;
				} else if (position == 4) {
					rules_single_tile_ships = 7;
				} else if (position == 5) {
					rules_single_tile_ships = 8;
				} else if (position == 6) {
					rules_single_tile_ships = 9;
				} else if (position == 7) {
					rules_single_tile_ships = 10;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
		});
		
		// Choose 5 ships as default
		singleTileShipsSpinner.setSelection( 2 );

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
	public void communicationRulesReceived(int columns, int rows, int single_tile_ships) {
		acceptRulesAndStart(columns, rows, single_tile_ships);
	}

	private void acceptRulesAndStart(int columns, int rows, int single_tile_ships) {
		if (dialog_abort_warn != null && dialog_abort_warn.isShowing()) {
			dialog_abort_warn.dismiss();
		}
		if (dialog_waiting != null && dialog_waiting.isShowing()) {
			dialog_waiting.dismiss();
		}

		BattleshipsApplication.context().GRID_COLUMNS = columns;
		BattleshipsApplication.context().GRID_ROWS  = rows;
		//BattleshipsApplication.context().MAX_SHIPS = single_tile_ships;
		Intent i = new Intent(SetupGameActivity.this, PlaceShipsActivity.class);
		startActivity(i);
		finish();
	}


}
