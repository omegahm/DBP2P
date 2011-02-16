package dk.diku.bumptest;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpAPIListener;
import com.bumptech.bumpapi.BumpConnectFailedReason;
import com.bumptech.bumpapi.BumpConnection;
import com.bumptech.bumpapi.BumpDisconnectReason;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements BumpAPIListener {

	private static final String BUMP_API_DEV_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	// Used to distinguish Bump activity results from other activity results
	private static final int BUMP_API_REQUEST_CODE = 1025;

	private Button btnStartBumpAPI;
	
	private BumpConnection bConn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		initializeButtons();
	}

	private void initializeButtons() {
		btnStartBumpAPI = (Button)findViewById(R.id.btnStartBumpAPI);
		btnStartBumpAPI.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent bump = new Intent(MainActivity.this, BumpAPI.class);
				bump.putExtra(BumpAPI.EXTRA_API_KEY, BUMP_API_DEV_KEY);
				startActivityForResult(bump, BUMP_API_REQUEST_CODE);            }
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BUMP_API_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Bump connected successfully, set this activity as its listener
				bConn = (BumpConnection) data.getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
				bConn.setListener(this);
			} else if (data != null) {
				// Failed to connect, obtain the reason
				BumpConnectFailedReason reason =
					(BumpConnectFailedReason) data.getSerializableExtra(BumpAPI.EXTRA_REASON);
			}
		}
	}	
	@Override
	public void bumpDataReceived(byte[] arg0) {
		Toast.makeText(this, "Bump data received:\n" + arg0.toString(), Toast.LENGTH_LONG);
		// TODO Auto-generated method stub

	}

	@Override
	public void bumpDisconnect(BumpDisconnectReason arg0) {
		Toast.makeText(this, "Bump disconnected:\n" + arg0.toString(), Toast.LENGTH_LONG);
		// TODO Auto-generated method stub

	}
}