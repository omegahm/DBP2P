package dk.hotmovinglobster.dustytuba;

import android.content.Context;

public interface IdentityProvider {
	public static final String EXTRA_BLUETOOTH_ADDRESS = "EXTRA_BLUETOOTH_ADDRESS";
	
	public void launch(Context context);
}
