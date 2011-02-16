package dk.diku.bumptest;

import com.bumptech.bumpapi.BumpAPI;
import com.bumptech.bumpapi.BumpAPIListener;
import com.bumptech.bumpapi.BumpConnection;
import com.bumptech.bumpapi.BumpDisconnectReason;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConnectedActivity extends Activity implements BumpAPIListener {

	private BumpConnection bConn;
	
	private Button btnSendMessage;
	private Button btnDisconnectBump;
	private TextView lblConnectedTo;
	private TextView lblLogArea;
	private EditText txtSendText;
	
	private String log = "";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.connected);
		initializeButtons();
		initializeOtherviews();
		// Bump connected successfully, set this activity as its listener
		bConn = (BumpConnection)getIntent().getParcelableExtra(BumpAPI.EXTRA_CONNECTION);
		bConn.setListener(this);
		lblConnectedTo.setText("Connected to " + bConn.getOtherUserName());
	}
	
	private void initializeOtherviews() {
		lblConnectedTo = (TextView)findViewById(R.id.lblConnectedTo);
		lblLogArea = (TextView)findViewById(R.id.lblLogArea);
		txtSendText = (EditText)findViewById(R.id.txtSendText);
	}

	private void initializeButtons() {
		btnSendMessage = (Button)findViewById(R.id.btnSendMessage);
		btnSendMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				bConn.send(txtSendText.getText().toString().getBytes());
				addToLog( "Sent message: " + txtSendText.getText() );
				txtSendText.setText("");
			}
		});
		btnDisconnectBump = (Button)findViewById(R.id.btnDisconnectBump);
		btnDisconnectBump.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				bConn.disconnect();
				finish();
			}
		});
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		log = "";
		lblLogArea.setText("");
	}
	
	private void addToLog(String str)
	{
		log = str +"\n" + log;
		lblLogArea.setText( log );
	}

	@Override
	public void bumpDataReceived(byte[] arg0) {
		String in = new String(arg0);
		Toast.makeText(this, "Bump data received:\n" + in, Toast.LENGTH_LONG).show();
		addToLog("Received message: " + in);
		// TODO Auto-generated method stub

	}

	@Override
	public void bumpDisconnect(BumpDisconnectReason arg0) {
		Toast.makeText(this, "Bump disconnected:\n" + arg0.toString(), Toast.LENGTH_LONG).show();
		// TODO Auto-generated method stub

	}
}
