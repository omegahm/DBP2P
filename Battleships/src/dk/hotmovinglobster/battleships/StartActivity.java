package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.os.Bundle;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new BattleGrid(this, Settings.GRID_WIDTH, Settings.GRID_HEIGHT));
    }
}