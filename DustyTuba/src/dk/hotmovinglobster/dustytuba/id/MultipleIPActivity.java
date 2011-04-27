package dk.hotmovinglobster.dustytuba.id;

import dk.hotmovinglobster.dustytuba.api.BtAPI;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class MultipleIPActivity extends Activity {
	
	private String[] providers = {};
	private String[] providerNames = {};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final Intent intent = getIntent();
		
		providers = intent.getStringArrayExtra( BtAPI.EXTRA_IP_PROVIDERS );
		providerNames = new String[ providers.length ];
		for (int i = 0; i < providers.length; i++ )
			providerNames[i] = BtAPI.stringToIdProviderName( this, providers[i] );
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle( BtAPI.res( this, "string", "dustytuba_select_identity_provider" ) );
		builder.setItems( providerNames, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	Intent i = BtAPI.getIntent( MultipleIPActivity.this, providers[item], intent.getExtras() );
		    	startActivityForResult( i, 0 );
		    }
		});
		builder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				setResult( RESULT_CANCELED );
				finish();
			}
		});
		AlertDialog alert = builder.create();
		
		alert.show();
	}
	
	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		setResult( resultCode, data );
		finish();
	}
	
}
