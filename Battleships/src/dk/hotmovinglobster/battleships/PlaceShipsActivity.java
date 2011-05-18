package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import dk.hotmovinglobster.battleships.BattleGrid.TileType;

public class PlaceShipsActivity extends Activity implements BattleGridListener {

	private BattleGrid grid;
	private TextView txt_ships_remaining;
	
	private ProgressDialog dialog_waiting;
	private AlertDialog dialog_abort_warn;

	private Resources res;

	private int ships_remaining;
	
	private Handler mHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(Settings.LOG_TAG, "PlaceShipsActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_ships);
		mHandler = new Handler();
		res = getResources();

		ships_remaining = Settings.MAX_SHIPS;
		txt_ships_remaining = (TextView) findViewById(R.id.place_ships_txt_ships_remaining);
		updateShipsRemainingLabel();

		grid = new BattleGrid(this, Settings.GRID_COLUMNS, Settings.GRID_ROWS);
		grid.setListener(this);
		((FrameLayout) findViewById(R.id.place_ships_grid_frame)).addView(grid);
	}

	@Override
	public void onTileHit(int column, int row) {
		Log.v(Settings.LOG_TAG, "PlaceShipsActivity.onTileHit(" + column + ", " + row + ")");
		if (ships_remaining > 0) {
			grid.setTileType(column, row, TileType.SHIP);
			updateShipsRemaining();
		}
	}

	private void updateShipsRemaining() {
		int ships_on_grid = 0;
		for (int column = 0; column<Settings.GRID_COLUMNS; column++) {
			for (int row = 0; row<Settings.GRID_ROWS; row++) {
				if (grid.getTileType(column, row) == TileType.SHIP) {
					ships_on_grid++;
				}
			}
		}

		ships_remaining = Settings.MAX_SHIPS - ships_on_grid;
		
		updateShipsRemainingLabel();
		
		if (ships_remaining == 0) {
			allShipsPlaced();
		}
	}

	private void allShipsPlaced() {
		Log.i(Settings.LOG_TAG, "PlaceShipsActivity: All ships placed");
		showWaitingDialog();
		
		// Simulate that opponent is ready after 5 seconds
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				opponentReady();
			}
		}, 5000);
	}
	
	private void opponentReady() {
		Log.i(Settings.LOG_TAG, "PlaceShipsActivity: Opponent ready");
		if (dialog_abort_warn != null && dialog_abort_warn.isShowing()) {
			dialog_abort_warn.dismiss();
		}
		if (dialog_waiting != null && dialog_waiting.isShowing()) {
			dialog_waiting.dismiss();
		}
		Toast.makeText(this, "Opponent ready!", Toast.LENGTH_SHORT).show();
		finish();
	}

	private void showWaitingDialog() {
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which){
		        case DialogInterface.BUTTON_POSITIVE:
					dialog.dismiss();
					PlaceShipsActivity.this.finish();
		            break;

		        case DialogInterface.BUTTON_NEGATIVE:
		            showWaitingDialog();
		            break;
		        }		
		    }
		};
		
		dialog_waiting = new ProgressDialog(this);
		dialog_waiting.setIndeterminate(true);
		dialog_waiting.setMessage(res.getString(R.string.place_ships_all_ships_placed));
		dialog_waiting.setCancelable(true);
		dialog_waiting.show();
		dialog_waiting.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog_waiting.dismiss();
				dialog_abort_warn = new AlertDialog.Builder(PlaceShipsActivity.this).setMessage(R.string.place_ships_warn_abort_wait).
				setPositiveButton(android.R.string.yes, dialog_click_listener).setNegativeButton(android.R.string.no, dialog_click_listener).show();
			}
		});
	}

	private void updateShipsRemainingLabel() {
		txt_ships_remaining.setText( res.getString( R.string.place_ships_remaining_ships_formatted, ships_remaining ) );
	}
}
