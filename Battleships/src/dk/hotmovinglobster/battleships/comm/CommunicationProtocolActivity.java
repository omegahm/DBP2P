package dk.hotmovinglobster.battleships.comm;

import java.util.List;

import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.BattleshipPosition;
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
	
	public void communicationShipsPlaced(List<BattleshipPosition> ships) {}
	
	public void communicationRulesReceived(int game_type) {}
	
	public void communicationShotFired(Point p) {}

}
