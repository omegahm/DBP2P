package dk.hotmovinglobster.dustytuba.id;

import java.util.regex.*;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * An identity provider allowing a user to manually enter a MAC address
 * 
 * Mainly for testing purposes
 * 
 * Include a bundled string with key <BtAPI.EXTRA_IP_MAC> to set default MAC
 * 
 * @author Jesper
 */
public class ManualIPActivity extends Activity {
	
	private Button btnOK;
	private EditText txtMAC;
	private Resources res;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	res = getResources();
    	Log.i(BtAPI.LOG_TAG, "ManualIPActivity: Created");
    	setContentView( BtAPI.res(this, "layout", "dustytuba_manual_ip_dialog" ) );

    	txtMAC = (EditText)findViewById( BtAPI.res(this, "id", "dustytuba_manual_txt_mac" ) );

    	Intent thisIntent = getIntent();
    	if (thisIntent.getExtras() != null) {
    		Log.i(BtAPI.LOG_TAG, "ManualIPActivity: with data (Size "+thisIntent.getExtras().size()+": "+thisIntent.getExtras().keySet()+")");

    		Bundle extras = thisIntent.getExtras();
    		String mac = extras.getString(BtAPI.EXTRA_IP_MAC); // TODO: grab from somewhere
    	
    		if (mac != null){
    			Log.i(BtAPI.LOG_TAG, "ManualIPActivity: Received MAC Address: " + mac);
    			txtMAC.setText(mac);
    		}
    	}
        
    	initializeButtons();
        
    }
    
	private static Pattern MacPattern = Pattern.compile("^([0-9a-fA-F][0-9a-fA-F])[:\\-]?([0-9a-fA-F][0-9a-fA-F])[:\\-]?([0-9a-fA-F][0-9a-fA-F])[:\\-]?([0-9a-fA-F][0-9a-fA-F])[:\\-]?([0-9a-fA-F][0-9a-fA-F])[:\\-]?([0-9a-fA-F][0-9a-fA-F])$");
	
	/**
	 * Checks if the provided string is a valid MAC Address and if it is,
	 * converts it to an Android BT compatible MAC address (uppercase and with colon separators)
	 * @param mac MAC Address to test
	 * @return null if invalid, otherwise a correct MAC address string
	 */
    private static String validateMacAddress(String mac) {
    	Matcher m = MacPattern.matcher( mac );
    	if (m.find()) {
    		return m.group(1).toUpperCase() + ":" + 
				   m.group(2).toUpperCase() + ":" +
				   m.group(3).toUpperCase() + ":" +
				   m.group(4).toUpperCase() + ":" +
				   m.group(5).toUpperCase() + ":" +
				   m.group(6).toUpperCase();
    		
    	} else {
    		return null;
    	}

    }

    private void initializeButtons() {
		btnOK = (Button)findViewById(BtAPI.res(this, "id", "dustytuba_manual_btn_ok" ) );
    	btnOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String mac = validateMacAddress( txtMAC.getText().toString() );
				if ( mac == null ) {
					Toast.makeText(ManualIPActivity.this, res.getString( BtAPI.res(ManualIPActivity.this, "string", "dustytuba_invalid_mac" ) ), Toast.LENGTH_SHORT).show();
					return;
				}
				
		    	// Return data
		    	Intent data = new Intent();
		        data.putExtra(BtAPI.EXTRA_IP_MAC,mac);
		    	setResult(RESULT_OK, data);
		    	
		    	Log.i(BtAPI.LOG_TAG, "ManualIPActivity: Finishing");
		    	finish();
			}
		});
	}

}
