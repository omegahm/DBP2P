package dk.hotmovinglobster.battleships;

import java.util.Arrays;
import java.util.List;

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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.BattleGrid.TileType;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;

public class PlaceShipsActivity extends CommunicationProtocolActivity implements BattleGridListener {

	private BattleGrid grid;
	/** Simple view for listing remaining ships to be placed */
	private TextView txt_ships_remaining;

	/** Dialog to keep the waiting user happy **/
	private ProgressDialog dialog_waiting;
	/** Warn the waiting user that BACK cancels game */
	private AlertDialog dialog_abort_warn;

	private Resources res;

	/** Number of remaining ships to be placed for different tile-sizes. */
	private int[] shipsRemaining;
	/** Number of remaining ships to be placed in total. */
	private int totalShipsRemaining;

	private boolean isReady = false;
	private boolean opponentReady = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: onCreate()");
		assert( BattleshipsApplication.context().Comm != null );
		BattleshipsApplication.context().Comm.setListeningActivity( this );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.place_ships);

		res = getResources();

		txt_ships_remaining = (TextView) findViewById(R.id.place_ships_txt_ships_remaining);

		grid = new BattleGrid(this, BattleshipsApplication.context().GRID_COLUMNS, BattleshipsApplication.context().GRID_ROWS);
		grid.setAllowMultiSelection( true );
		grid.setListener(this);
		((FrameLayout) findViewById(R.id.place_ships_grid_frame)).addView(grid);

		((Button)findViewById(R.id.place_ships_btn_undo)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				grid.undo();
				updateShipsRemaining();
			}
		});

		updateShipsRemaining();

	}
	/*
	@Override
	protected void onStart() {
		super.onStart();
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: onStart()");
	}
	@Override
    protected void onRestart() {
		super.onRestart();
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: onRestart()");
	}
	@Override
    protected void onResume() {
		super.onResume();
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: onResume()");
	}
	@Override
    protected void onPause() {
		super.onPause();
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: onPause()");
	}
	@Override
    protected void onStop() {
		super.onStop();
		BattleshipsApplication.context().Comm.setListeningActivity(null);
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: onStop()");
	}
	@Override
    protected void onDestroy() {
		super.onDestroy();
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: onDestroy()");
	}
	 */
	@Override
	public void onSingleTileHit(Point p) {
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity.onTileHit(" + p.column + ", " + p.row + ")");
		/*		
		if (totalShipsRemaining() > 0) {
			grid.setTileType(p, TileType.SHIP);
			updateShipsRemaining();
		}
		 */
	}

	private int[] calculateShipsRemaining() {
		int[] result = BattleshipsApplication.context().MAX_SHIPS.clone();
		int length = 0;
//		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: calculateShipsRemaining(): " + Arrays.toString(result));

		for (BattleshipPosition bsp: grid.getBattleshipPositions()) {
			length = bsp.getShip().getLength();
//			Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: calculateShipsRemaining(): loop " + length + ", " + result[length]);
			result[length]--;
			assert(result[length] >= 0); // Requires: adb shell setprop debug.assert 1
//			Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: calculateShipsRemaining(): l00p " + length + ", " + result[length]);
		}

//		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: calculateShipsRemaining(): end " + Arrays.toString(result));

		return result;
	}

	private void updateShipsRemaining() {
		shipsRemaining = calculateShipsRemaining();
		totalShipsRemaining = 0;
		for (int i=0; i<shipsRemaining.length; i++) {
			totalShipsRemaining += shipsRemaining[i];
		}

		updateShipsRemainingLabel();

		Log.d(BattleshipsApplication.LOG_TAG, "Ships remaining ("+totalShipsRemaining+"): " + Arrays.toString(shipsRemaining));

		if (totalShipsRemaining == 0) {
			allShipsPlaced();
		}
	}

	private void allShipsPlaced() {
		Log.i(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: All ships placed");
		isReady = true;
		List<BattleshipPosition> ships = grid.getBattleshipPositions();
		BattleshipsApplication.context().myShips = ships;
		BattleshipsApplication.context().Comm.sendShipsPlaced( ships );
		if (opponentReady) {
			bothReady();
		} else {
			showWaitingDialog();
		}
	}

	private void opponentReady() {
		opponentReady = true;
		Log.i(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: Opponent ready");
		if (dialog_abort_warn != null && dialog_abort_warn.isShowing()) {
			dialog_abort_warn.dismiss();
		}
		if (dialog_waiting != null && dialog_waiting.isShowing()) {
			dialog_waiting.dismiss();
		}
		if (isReady) {
			bothReady();
		}
	}

	private void bothReady() {
		Intent i = new Intent(this, GameActivity.class);
		startActivity(i);
		finish();
	}

	private void showAbortDialog() {
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					BattleshipsApplication.context().Comm.disconnect();
					dialog.dismiss();
					PlaceShipsActivity.this.finish();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					showWaitingDialog();
					break;
				}		
			}
		};

		dialog_abort_warn = new AlertDialog.Builder(PlaceShipsActivity.this).setMessage(R.string.place_ships_warn_abort_wait).
		setPositiveButton(android.R.string.yes, dialog_click_listener).setNegativeButton(android.R.string.no, dialog_click_listener).show();
	}

	private void showWaitingDialog() {
		dialog_waiting = new ProgressDialog(this);
		dialog_waiting.setIndeterminate(true);
		dialog_waiting.setMessage(res.getString(R.string.place_ships_all_ships_placed));
		dialog_waiting.setCancelable(true);
		dialog_waiting.show();
		dialog_waiting.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog_waiting.dismiss();
				showAbortDialog();
			}
		});
	}

	private void updateShipsRemainingLabel() {
		String text = "2: " + shipsRemaining[2] + ", 3: " + shipsRemaining[3] + 
		              ", 4: " + shipsRemaining[4] + ", 5: " + shipsRemaining[5];
//		txt_ships_remaining.setText( res.getString( R.string.place_ships_remaining_ships_formatted, text ) );
		txt_ships_remaining.setText( text );

	}

	@Override
	public void communicationDisconnected() {
		Toast.makeText(this, "Disconnected from opponent (HC)", Toast.LENGTH_LONG).show();
		finish();
	}

	@Override
	public void communicationShipsPlaced(List<BattleshipPosition> ships) {
		Log.v(BattleshipsApplication.LOG_TAG, "PlaceShipsActivity: Ships placed (amount: "+ships.size()+")");
		BattleshipsApplication.context().opponentShips = ships;
		opponentReady();
	}

	@Override
	public void onBackPressed() {
		showAbortDialog();
	}
	@Override
	public boolean allowMultiSelectionBetween(Point tile1, Point tile2) {
		int length = tile1.lengthTo( tile2 );
		if ( length > 5 )
			return false;
		for (Point p: tile1.pointsInStraightLineTo(tile2) ) {
			if ( grid.getTileType(p) == TileType.SHIP) {
				return false;
			}
		}
		// Check if we the user needs to place any more ships of the given length
		return shipsRemaining[ length ] >= 1;
	}

	@Override
	public void onMultiTileHit(Point tile1, Point tile2) {
		assert( allowMultiSelectionBetween(tile1, tile2) );

		grid.placeShipInTiles( tile1.pointsInStraightLineTo( tile2 ), BattleshipsApplication.resources().getBattleship( tile1.lengthTo( tile2 ) ) );
		updateShipsRemaining();
	}
}
