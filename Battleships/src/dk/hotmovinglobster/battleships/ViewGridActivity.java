package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;

public class ViewGridActivity extends Activity {

	private FrameLayout gridFrame;
	private BattleGrid grid;
	
	public static String EXTRA_OPPONENTS_SHIPS = "opponents_ships";

	private boolean opponents_ships;
	
	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "ViewGridActivity: onCreate()");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_grid);
		
		opponents_ships = getIntent().getBooleanExtra(EXTRA_OPPONENTS_SHIPS, false);

		gridFrame = (FrameLayout)findViewById(R.id.view_grid_grid_frame);

		grid       = new BattleGrid(this, BattleshipsApplication.context().GRID_COLUMNS, BattleshipsApplication.context().GRID_ROWS);
		grid.setAllowMultiSelection( false );
		grid.setAllowTouch(false);

		if (opponents_ships) {
			for (BattleshipPosition bsp: BattleshipsApplication.context().opponentShips) {
				grid.placeShipInTiles( bsp.getPosition(), bsp.getShip() );
			}
			grid.setTileTypes( BattleshipsApplication.context().opponentTileTypes );
		} else {
			for (BattleshipPosition bsp: BattleshipsApplication.context().myShips) {
				grid.placeShipInTiles( bsp.getPosition(), bsp.getShip() );
			}
			grid.setTileTypes( BattleshipsApplication.context().myTileTypes );
		}
		
		gridFrame.addView( grid );
	}
}
