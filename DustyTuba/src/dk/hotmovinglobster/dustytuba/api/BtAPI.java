package dk.hotmovinglobster.dustytuba.api;

import dk.hotmovinglobster.dustytuba.id.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/** Setups and hands off connection */
public class BtAPI {

	/** FIXME
	 * This is only necessary provided we leave calling identity provider and subsequently bt provider to the implementing activity.
	 * Hopefully we can get around this - and I think it's what Jesper has been trying to do with GenericIPActivity, but I'm not entirely sure.
	 * If/when this succeeds, then this can be removed and relevant onActivityResult handling can be moved to API.
	 */
	public static final int RESULTCODE_IDENTITY_PROVIDER = 0;
	public static final int RESULTCODE_SETUP_BT = 1;
	public static final int RESULTCODE_ID_AND_SETUP = 2; // TODO: Idealy this will be the only one we use
	
	/**
	 * Gets an intent to setup a bluetooth connection.
	 * 
	 * Use this intent with StartActivityForResult() to attempt to setup the connection
	 * 
	 * @param context A Context of the application package using this class.
	 * @param idProvider A string deciding which identity provider to use
	 * @return The Intent to pass on to startActivityForResult
	 */
	public static Intent getIntent(Context context, String idProvider) {
		return getIntent( context, idProvider, null );
	}
	
	/**
	 * Gets an intent to setup a bluetooth connection.
	 * 
	 * Use this intent with StartActivityForResult() to attempt to setup the connection
	 * 
	 * @param context A Context of the application package using this class.
	 * @param idProvider A string deciding which identity provider to use
	 * @param extras Extra data to bundle along with the intent
	 * @return The Intent to pass on to startActivityForResult
	 */
	public static Intent getIntent(Context context, String idProvider, Bundle extras) {
		Class<?> cls = stringToIdProviderClass(idProvider);
		
		// TODO: More sensible, maybe throw an exception
		if ( cls == null )
			return null;
		
		Intent intent = new Intent(context, GenericIPActivity.class);
		
		if ( extras != null )
			intent.putExtra(EXTRA_IP_BUNDLE, extras);
		intent.putExtra(EXTRA_IP_CLASS, cls.getCanonicalName());
		
		return intent;
	}
	
	public static final String IDENTITY_PROVIDER_BUMP = "bump";
	public static final String IDENTITY_PROVIDER_FAKE = "fake";
	public static final String IDENTITY_PROVIDER_MANUAL = "manual";
	
	/**
	 * Find the identity providor given a string name
	 * 
	 * @param idProvider A string name of an identity providor
	 * @return The identity providor class
	 */
	private static Class<?> stringToIdProviderClass(String idProvider) {
		if (idProvider.equals(IDENTITY_PROVIDER_BUMP)) 
			return BumpIPActivity.class;
		else if (idProvider.equals(IDENTITY_PROVIDER_FAKE)) 
			return FakeIPActivity.class;
		else if (idProvider.equals(IDENTITY_PROVIDER_MANUAL)) 
			return ManualIPActivity.class;
		else 
			return null;
	}
	
	/**
	 * A btConnectFailedReason is returned by the API when the user exits before connection has been established.
	 */
	public enum BtConnectFailedReason {
		FAIL_NONE, /** No failure */ // TODO: Do we need this?
		FAIL_USER_CANCELED, /** Local user quit the API */
		FAIL_BT_UNAVAILABLE, /** Local user quit before network became available (e.g. cancelled enable BT dialog) */
		FAIL_OTHER /** Something wierd happened TODO: Remove? */
	}
	
	/**
	 * A BtDisconnectReason is returned by the API when the user exits after connection has been established.
	 */
	public enum BtDisconnectReason {
		END_USER_QUIT, 	/** local user quit cleanly */
		END_LOST_NET, /** connection to the server was lost */
		END_OTHER_USER_QUIT, /** remote user quit cleanly */
		END_OTHER_USER_LOST /** remote user was lost */ // TODO: Remove (Bump has two connections to central server. We don't, so doesn't apply)
	}
	
	public static final String EXTRA_IP_CLASS = "ip_class";
	public static final String EXTRA_IP_BUNDLE = "ip_bundle";
	public static final String EXTRA_IP_MAC = "ip_mac";
	public static final String EXTRA_BT_MAC = "bt_mac";
	
	public static final String EXTRA_BT_CONNECTION = "bt_connection";
	
	public static final String LOG_TAG = "DustyTuba";
	
	/**
	 * Get res object from the application
	 * 
	 * @param context A Context of the application package using this class.
	 * @param type Ressource type to find
	 * @param name Name of ressource to find
	 * @return The ressource
	 */
	public static int res(Context context, String type, String name) {
		String pkg = context.getApplicationInfo().packageName;
		return context.getResources().getIdentifier(name, type, pkg);
	}
	
	/**
	 * Fetch the address of this Bluetooth adapter.
	 * @return this device's Bluetooth MAC address
	 */
	public static String getBluetoothAddress() {
		// TODO: Make good
		return "00:00:00:00:00:00";
		//return mBluetoothAdapter.getAddress(); // TODO: Requires BT activated...
	}

}
