package dk.hotmovinglobster.dustytuba.id;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import dk.hotmovinglobster.dustytuba.api.BtAPI;

// FIXME: Does not return properly... might be the way we call it.

/**
 * An activity allowing an end user to select from multiple identity providers
 * 
 * Expects a string array extra with key BtAPI.EXTRA_IP_PROVIDERS with string
 * values of identity providers available for selection
 */
public class MultipleIPActivity extends Activity {
	
	/**
	 * List of string identifiers of identity providers
	 */
	private String[] providers = {};
	/**
	 * List of nice names of identity providers
	 */
	private String[] providerNames = {};
	private Intent intent;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		intent = getIntent();
		
		// Populate string arrays
		providers = intent.getStringArrayExtra( BtAPI.EXTRA_IP_PROVIDERS );
		providerNames = new String[ providers.length ];
		for (int i = 0; i < providers.length; i++ ) {
			providerNames[i] = BtAPI.stringToIdProviderName( this, providers[i] );
		}
		
		// Build selection dialog
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle( BtAPI.res( this, "string", "dustytuba_select_identity_provider" ) );
		builder.setItems( providerNames, new DialogInterface.OnClickListener() {
			// When an identity provider is selected, the providers activity
			// should be launched
		    public void onClick(DialogInterface dialog, int item) {
		    	Log.i(BtAPI.LOG_TAG, "MultipleIPActivity: Starting identity provider \""+providerNames[item]+"\"");
		    	startIdentityProvider(providers[item]);
		    }
		});
		
		// When user presses back button, activity should cancel
		builder.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				setResult( RESULT_CANCELED );
				finish();
			}
		});
		final AlertDialog alert = builder.create();
		
		alert.show();
	}
	
	private void startIdentityProvider(String provider) {
		Intent i = new Intent( this, BtAPI.stringToIdProviderClass(provider) );
		i.replaceExtras( intent );
    	startActivityForResult( i, 0 );
	}
	
	@Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		// Upon return from the launched identity provider activity,
		// the data should just be forwarded to GenericIPActivity
		if (resultCode == RESULT_OK) {
			Log.v(BtAPI.LOG_TAG, "MultipleIPActivity: onActivityResult("+requestCode+", RESULT_OK, "+data.getExtras().keySet().toString()+")");
		} else {
			if (data == null) {
				Log.v(BtAPI.LOG_TAG, "MultipleIPActivity: onActivityResult("+requestCode+", RESULT_OK, null)");
			} else {
				if (data.getExtras() == null) {
					Log.v(BtAPI.LOG_TAG, "MultipleIPActivity: onActivityResult("+requestCode+", RESULT_OK, <Empty intent>)");
				} else {
					Log.v(BtAPI.LOG_TAG, "MultipleIPActivity: onActivityResult("+requestCode+", RESULT_OK, "+data.getExtras().keySet().toString()+")");
				}
			}
		}
		setResult( resultCode, data );
		finish();
	}
	
}
