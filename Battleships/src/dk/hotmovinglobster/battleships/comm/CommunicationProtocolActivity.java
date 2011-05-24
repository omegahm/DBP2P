package dk.hotmovinglobster.battleships.comm;

import java.util.List;

import dk.hotmovinglobster.battleships.BattleGrid.Point;
import android.app.Activity;

/**
 * An activity that is to receive events from the CommunicationProtocol.
 * 
 *  The activity overrides the methods it needs 
 *
 * @author Jesper
 */
public class CommunicationProtocolActivity extends Activity {
	
	public void communicationDisconnected() {}
	
	public void communicationShipsPlaced(List<Point> ships) {}
	
	public void communicationRulesReceived(int columns, int rows, int single_tile_ships) {}
	
	public void communicationShotFired(Point p) {}

}
