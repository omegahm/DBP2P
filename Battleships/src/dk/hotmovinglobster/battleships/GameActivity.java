package dk.hotmovinglobster.battleships;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.BattleGrid.TileType;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;

public class GameActivity extends CommunicationProtocolActivity implements BattleGridListener {
	
	private BattleGrid myGrid;
	private BattleGrid opponentGrid;
	
	private FrameLayout gridFrame;
	private ImageView header;
	
	private boolean attacking = true;

	private AlertDialog dialog_abort_warn;

	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "GameActivity: onCreate()");
		assert( BattleshipsApplication.context().Comm != null );
		BattleshipsApplication.context().Comm.setListeningActivity( this );
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.game);
		
		setupGrids();
		
		if (BattleshipsApplication.context().Comm.isServer()) {
			// This means server starts by attacking.
			// The call to switchGrids sets attacking=true
			attacking = false;
		}
		
		gridFrame = (FrameLayout)findViewById(R.id.game_grid_frame);
		header = (ImageView)findViewById(R.id.game_image_header);

		switchGrids();
		
	}

	private void setupGrids() {
		myGrid       = new BattleGrid(this, BattleshipsApplication.context().GRID_COLUMNS, BattleshipsApplication.context().GRID_ROWS);
		opponentGrid = new BattleGrid(this, BattleshipsApplication.context().GRID_COLUMNS, BattleshipsApplication.context().GRID_ROWS);
		
		myGrid.setListener( this );
		opponentGrid.setListener( this );
		
		for (Point p: BattleshipsApplication.context().myShips) {
			myGrid.setTileType(p, TileType.SHIP);
		}

		for (Point p: BattleshipsApplication.context().opponentShips) {
			opponentGrid.setTileType(p, TileType.SHIP);
		}

		
	}
	
	private void switchGrids() {
		BattleGrid newGrid;
		int headerImage;
		attacking = !attacking;
		if (attacking) {
			newGrid = opponentGrid;
			headerImage = R.drawable.attack;
		} else {
			newGrid = myGrid;
			headerImage = R.drawable.defend;
		}
		
		gridFrame.removeAllViews();
		gridFrame.addView( newGrid );
		header.setImageResource( headerImage );
		
	}

	@Override
	public void onTileHit(int column, int row) {
		switchGrids();
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
	public void onBackPressed() {
		showAbortDialog();
	}
	


}
