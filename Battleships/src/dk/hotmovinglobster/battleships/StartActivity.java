package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        //setContentView(new BattleGrid(this, Settings.GRID_WIDTH, Settings.GRID_HEIGHT));
        
        super.onCreate(savedInstanceState);
        // Create a new ImageView
        ImageView imageView = new ImageView(this);
        // Set the background color to white
        imageView.setBackgroundColor(Color.WHITE);
        // Screw SVG, we just use PNG.
    	imageView.setImageResource(R.drawable.battleship_bullet);
        imageView.setImageResource(R.drawable.battleship_ship);
        imageView.setImageResource(R.drawable.battleship_hit);
        // Set the ImageView as the content view for the Activity
        setContentView(imageView);

    }
}