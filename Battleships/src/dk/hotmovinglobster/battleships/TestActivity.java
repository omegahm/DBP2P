package dk.hotmovinglobster.battleships;

import android.os.Bundle;
import android.util.Log;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocolActivity;

public class TestActivity extends CommunicationProtocolActivity {

	BattleGrid grid;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(BattleshipsApplication.LOG_TAG, "TestActivity: onCreate()");
		super.onCreate(savedInstanceState);
		grid = new BattleGrid(this, 6, 6);
		setContentView(grid);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.v(BattleshipsApplication.LOG_TAG, "TestActivity: onStop()");
//		grid.
	}
}
