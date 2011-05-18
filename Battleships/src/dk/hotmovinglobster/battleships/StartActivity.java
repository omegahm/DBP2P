package dk.hotmovinglobster.battleships;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class StartActivity extends Activity {

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(Settings.LOG_TAG, "StartActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		
		initializeButtons();
	}
	
	private void initializeButtons() {
		((Button)findViewById(R.id.start_place_ships_activity)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(v.getContext(), PlaceShipsActivity.class);
				startActivityForResult(i, 0);
			}
		});
	}

}