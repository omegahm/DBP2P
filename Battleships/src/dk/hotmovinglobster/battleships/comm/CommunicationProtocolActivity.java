package dk.hotmovinglobster.battleships.comm;

import java.util.List;

import dk.hotmovinglobster.battleships.BattleGrid.Point;
import android.app.Activity;

public class CommunicationProtocolActivity extends Activity {
	
	public void communicationDisconnected() {}
	
	public void communicationShipsPlaced(List<Point> ships) {}

}
