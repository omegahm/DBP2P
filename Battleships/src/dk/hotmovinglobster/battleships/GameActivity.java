package dk.hotmovinglobster.battleships;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.BattleGrid.TileType;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;

public class GameActivity extends CommunicationProtocolActivity implements BattleGridListener {

	private static final int HIT_VIBRATE_LENGTH = 200;
	private static final int SWITCH_GRID_DELAY = 1500;
	private BattleGrid myGrid;
	private BattleGrid opponentGrid;

	private FrameLayout gridFrame;
	private ImageView header;
	
	private Handler mHandler;

	private boolean attacking = true;

	@SuppressWarnings("unused")
	private AlertDialog dialog_abort_warn;
	
	private int myShipsLeft;
	private int opponentShipsLeft;
	
	private Vibrator mVibrator;

	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "GameActivity: onCreate()");
		assert( BattleshipsApplication.context().Comm != null );
		BattleshipsApplication.context().Comm.setListeningActivity( this );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.game);
		
		// TODO: FIX
		for (int i = 0; i < BattleshipsApplication.context().MAX_SHIPS.length; i++ ) {
			myShipsLeft += i * BattleshipsApplication.context().MAX_SHIPS[i];
		}
		
		opponentShipsLeft = myShipsLeft;

		mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		setupGrids();

		if (BattleshipsApplication.context().Comm.isServer()) {
			// This means server starts by attacking.
			// The call to switchGrids sets attacking=true
			attacking = false;
		}

		gridFrame = (FrameLayout)findViewById(R.id.game_grid_frame);
		header = (ImageView)findViewById(R.id.game_image_header);
		
		mHandler = new Handler();

		switchGrids();

	}
	
	private void vibrate() {
		mVibrator.vibrate(HIT_VIBRATE_LENGTH);
	}

	private void setupGrids() {
		myGrid       = new BattleGrid(this, BattleshipsApplication.context().GRID_COLUMNS, BattleshipsApplication.context().GRID_ROWS);
		opponentGrid = new BattleGrid(this, BattleshipsApplication.context().GRID_COLUMNS, BattleshipsApplication.context().GRID_ROWS);

		myGrid.setAllowMultiSelection( false );
		opponentGrid.setAllowMultiSelection( false );
		
		opponentGrid.setShowNonDestroyedShips( true );
		opponentGrid.setShowNonDestroyedShips( false );
		
		myGrid.setListener( this );
		opponentGrid.setListener( this );

		for (BattleshipPosition bsp: BattleshipsApplication.context().myShips) {
			myGrid.placeShipInTiles( bsp.getPosition(), bsp.getShip() );
		}
		for (BattleshipPosition bsp: BattleshipsApplication.context().opponentShips) {
			opponentGrid.placeShipInTiles( bsp.getPosition(), bsp.getShip() );
		}

	}
	
	private void switchGrids() {
		switchGrids(0);
	}

	private void switchGrids(int delay) {
		mHandler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// if checkIfGameComplete() evaluates to true,
				// it automatically exits and goes to end screen
				if (checkIfGameComplete()) {
					return;
				}
				BattleGrid newGrid;
				int headerImage;
				attacking = !attacking;
				if (attacking) {
					newGrid = opponentGrid;
					headerImage = R.drawable.header_attack;
				} else {
					newGrid = myGrid;
					headerImage = R.drawable.header_defend;
				}

				gridFrame.removeAllViews();
				gridFrame.addView( newGrid );
				header.setImageResource( headerImage );

			}
		}, delay);
		

	}

	@Override
	public void onSingleTileHit(Point point) {
		if (attacking) {
			TileType tt = opponentGrid.getTileType(point);
			Log.v(BattleshipsApplication.LOG_TAG, "Tiletype:  " + tt);
			if ( tt == TileType.EMPTY || tt == TileType.SHIP ) {
				boolean hit = false;
				for (BattleshipPosition bsp: BattleshipsApplication.context().opponentShips) {
					if (bsp.getPosition().contains( point ) ) {
						hit = true;
					}
				}
				
				if (hit) {
					Log.v(BattleshipsApplication.LOG_TAG, "HIT!");
					opponentGrid.setTileType(point, TileType.HIT);
					opponentShipsLeft--;
					vibrate();
				} else {
					Log.v(BattleshipsApplication.LOG_TAG, "MISS!");
					opponentGrid.setTileType(point, TileType.MISS);
				}
				
				BattleshipsApplication.context().Comm.sendShotFired(point);
				switchGrids(SWITCH_GRID_DELAY);
			}
		}
	}

	private void showAbortDialog() {
		final DialogInterface.OnClickListener dialog_click_listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					BattleshipsApplication.context().Comm.disconnect();
					dialog.dismiss();
					GameActivity.this.finish();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					break;
				}		
			}
		};

		dialog_abort_warn = new AlertDialog.Builder(GameActivity.this).setMessage(R.string.place_ships_warn_abort_wait).
		setPositiveButton(android.R.string.yes, dialog_click_listener).setNegativeButton(android.R.string.no, dialog_click_listener).show();
	}

	@Override
	public void communicationDisconnected() {
		Toast.makeText(this, "Disconnected from opponent (HC)", Toast.LENGTH_LONG).show();
		finish();
	}
	
	@Override
	public void communicationShotFired(Point p) {
		// If it is not opponents turn, regard as warning shot
		if (attacking) {
			return;
		}
		
		TileType tt = myGrid.getTileType(p);
		if (tt == TileType.EMPTY) {
			myGrid.setTileType(p, TileType.MISS);
		} else if (tt == TileType.SHIP) {
			myGrid.setTileType(p, TileType.HIT);
			myShipsLeft--;
			vibrate();
		}
		switchGrids(SWITCH_GRID_DELAY);
		
	}
	
	private boolean checkIfGameComplete() {
		if (myShipsLeft == 0 || opponentShipsLeft == 0) {
			boolean won;
			if (myShipsLeft == 0) {
				won = false;
			} else {
				won = true;
			}
			
			Intent i = new Intent(this, EndActivity.class);
			i.putExtra(BattleshipsApplication.EXTRA_END_WINNER, won);
			startActivity(i);
			finish();
			
			return true;
		}
		return false;
	}


	@Override
	public void onBackPressed() {
		showAbortDialog();
	}

	@Override
	public boolean allowMultiSelectionBetween(Point tile1, Point tile2) {
		return false;
	}

	@Override
	public void onMultiTileHit(Point tile1, Point tile2) {}
	
	

}
