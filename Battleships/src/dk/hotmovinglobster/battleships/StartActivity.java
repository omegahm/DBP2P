package dk.hotmovinglobster.battleships;

import dk.hotmovinglobster.battleships.BattleGrid.TileType;
import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends Activity implements BattleGridListener {
	
	private BattleGrid bg;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(new BattleGrid(this, Settings.GRID_WIDTH, Settings.GRID_HEIGHT));
        
        super.onCreate(savedInstanceState);
        LinearLayout l = new LinearLayout(this);
        l.setOrientation( LinearLayout.VERTICAL );
        TextView t = new TextView(this);
        t.setText("OP");
        l.addView(t);
        bg = new BattleGrid(this, Settings.GRID_WIDTH, Settings.GRID_HEIGHT);
        bg.setListener(this);
        l.addView(bg);
        t = new TextView(this);
        t.setText("OP");
        l.addView(t);
        
        setContentView(l);
        
        bg.setTileType( 1, 1, TileType.SHIP );
        bg.setTileType( 1, 2, TileType.SHIP );
        bg.setTileType( 1, 3, TileType.SHIP );
        
        bg.setTileType( 4, 2, TileType.SHIP );
        bg.setTileType( 5, 2, TileType.SHIP );

        bg.setTileType( 0, 0, TileType.SHIP );

        bg.setTileType( 4, 5, TileType.SHIP );
        bg.setTileType( 5, 5, TileType.SHIP );

    }

	@Override
	public void OnTileHit(int column, int row) {
		// TODO Auto-generated method stub
		//Toast.makeText(this, "Tile hit! (column "+column+", row " + row + ")", Toast.LENGTH_SHORT).show();
		
		TileType type = bg.getTileType(column, row);
		if ( type == TileType.SHIP ) {
			bg.setTileType(column, row, TileType.HIT);
		} else if ( type == TileType.EMPTY ) {
			bg.setTileType(column, row, TileType.MISS);
		}
	}
}