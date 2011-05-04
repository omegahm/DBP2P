package dk.hotmovinglobster.dustytuba.api;

import dk.hotmovinglobster.dustytuba.id.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/** 
 * Setups and hands off connection 
 */
public class BtAPI {

	/* 
	 * FIXME
	 * This is only necessary provided we leave calling identity provider and subsequently bt provider to the implementing activity.
	 * Hopefully we can get around this - and I think it's what Jesper has been trying to do with GenericIPActivity, but I'm not entirely sure.
	 * If/when this succeeds, then this can be removed and relevant onActivityResult handling can be moved to API.
	 */
	public static final int REQUEST_IDENTITY_PROVIDER = 0;
	public static final int REQUEST_SETUP_BT = 1;
	public static final int REQUEST_DUSTYTUBA = 2; // TODO: Idealy this will be the only one we use
	public static final int RESULT_BT_UNAVAILABLE = 99131; /** Cannot proceed, BT not enabled */
	public static final int RESULT_FAILURE_CONNECT = 99132; /** Cannot establish connection to device */

	
	/**
	 * Gets an intent to setup a bluetooth connection.
	 * 
	 * Use this intent with StartActivityForResult() to attempt to setup the connection
	 * 
	 * @param context A Context of the application package using this class.
	 * @param idProvider A string deciding which identity provider to use
	 * @return The Intent to pass on to startActivityForResult
	 */
	public static Intent getIntent(final Context context, final String idProvider) {
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
	public static Intent getIntent(final Context context, final String idProvider, final Bundle extras) {
		final Class<?> cls = stringToIdProviderClass(idProvider);
		
		if ( cls == null ) {
			// TODO: More sensible, maybe throw an exception
			return null;
		} else {
			Intent intent = new Intent(context, GenericIPActivity.class);
			if ( extras != null ) {
				intent.putExtra(EXTRA_IP_BUNDLE, extras);
			}
			intent.putExtra(EXTRA_IP_CLASS, cls.getCanonicalName());

			return intent;
		}
		
	}
	
	// Please note that the string values here also refer to resource strings in
	// dustytubastrings.xml with the prefix 'dustytuba_identity_provider_',
	// e.g. 'dustytuba_identity_provider_bump' 
	public static final String IDENTITY_PROVIDER_BUMP   = "bump";
	public static final String IDENTITY_PROVIDER_FAKE   = "fake";
	public static final String IDENTITY_PROVIDER_MANUAL = "manual";
	public static final String IDENTITY_PROVIDER_PAIRED = "paired";
	public static final String IDENTITY_PROVIDER_MULTIPLE = "multiple";
	
	/**
	 * Find the identity provider given a string name
	 * 
	 * @param idProvider A string name of an identity provider
	 * @return The identity provider class or null if none is found
	 */
	private static Class<?> stringToIdProviderClass(final String idProvider) {
		Class<?> result;
		
		if (idProvider.equals(IDENTITY_PROVIDER_BUMP)) {
			result = BumpIPActivity.class;
		} else if (idProvider.equals(IDENTITY_PROVIDER_FAKE)) { 
			result = FakeIPActivity.class;
		} else if (idProvider.equals(IDENTITY_PROVIDER_MANUAL)) { 
			result = ManualIPActivity.class;
		} else if (idProvider.equals(IDENTITY_PROVIDER_PAIRED)) { 
			result = PairedIPActivity.class;
		} else if (idProvider.equals(IDENTITY_PROVIDER_MULTIPLE)) { 
			result = MultipleIPActivity.class;
		} else {
			result = null;
		}
			
		return result;
	}
	
	/**
	 * Get the nice localized name of an identity provider
	 * 
	 * @param context The context used to retrieve string resources
	 * @param idProvider The identity provider
	 * @return A string containing the localized name
	 */
	public static String stringToIdProviderName(final Context context, final String idProvider) {
		int resource = res( context, "string", "dustytuba_identity_provider_" + idProvider );
		if (resource == 0) {
			resource = res( context, "string", "dustytuba_identity_provider_unknown" );
		}
		
		return context.getResources().getString( resource );
	}

	/**
	 * A BtDisconnectReason is returned by the API when the user exits after connection has been established.
	 */
	public static enum BtDisconnectReason {
		END_USER_QUIT, 	     /** local user quit cleanly */
		END_LOST_NET,        /** connection to the server was lost */
		END_OTHER_USER_QUIT, /** remote user quit cleanly */
		END_OTHER_USER_LOST  /** remote user was lost */ // TODO: Remove (Bump has two connections to central server. We don't, so doesn't apply)
	}
	
	/**
	 * Used internally for bundling the class of an identity provider
	 */
	public static final String EXTRA_IP_CLASS     = "ip_class";
	/**
	 * Used internally for sending a Bundle to the actual identity provider
	 */
	public static final String EXTRA_IP_BUNDLE    = "ip_bundle";
	/**
	 * Used internally for sending a MAC address to and from identity providers
	 */
	public static final String EXTRA_IP_MAC       = "ip_mac";
	/**
	 * Used with the Multiple Identity Provider to limit the available providers
	 * 
	 * Value must be a string array
	 */
	public static final String EXTRA_IP_PROVIDERS = "ip_providers";
	
	/**
	 * Used for returning the bluetooth MAC address of the other device to the application
	 */
	public static final String EXTRA_BT_MAC       = "bt_mac";
	
	/**
	 * Used for returning the bluetooth connection object to the application
	 * 
	 * Value is of type dk.hotmovinglobster.dustytyba.api.BluetoothConnector
	 */
	public static final String EXTRA_BT_CONNECTION = "bt_connection";
	
	/**
	 * The tag used for logging messages to Logcat.
	 */
	public static final String LOG_TAG = "DustyTuba";
	
	/**
	 * Get a resource identifier from the application
	 * 
	 * @param context A Context of the application package using this class.
	 * @param type Resource type to find
	 * @param name Name of resource to find
	 * @return The resource identifier
	 */
	public static int res(final Context context, final String type, final String name) {
		final String pkg = context.getApplicationInfo().packageName;
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
