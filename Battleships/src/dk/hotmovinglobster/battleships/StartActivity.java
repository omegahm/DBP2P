package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StartActivity extends Activity {
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
        l.addView(new BattleGrid(this, Settings.GRID_WIDTH, Settings.GRID_HEIGHT));
        t = new TextView(this);
        t.setText("OP");
        l.addView(t);
        
        setContentView(l);
    }
}