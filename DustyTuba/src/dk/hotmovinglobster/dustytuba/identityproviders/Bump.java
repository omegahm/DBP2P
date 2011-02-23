package dk.hotmovinglobster.dustytuba.identityproviders;

import android.content.Context;
import android.content.Intent;

import com.bumptech.bumpapi.BumpAPI;

import dk.hotmovinglobster.dustytuba.IdentityProvider;

public class Bump implements IdentityProvider {
	
	private static final String API_KEY = "273a39bb29d342c2a9fcc2e61158cbba";
	private static final int BUMP_API_REQUEST_CODE = 1;

	@Override
	public void launch(Context context) {
		Intent bump = new Intent(context, BumpAPI.class);
		bump.putExtra(BumpAPI.EXTRA_API_KEY, API_KEY);
		//context.startActivityForResult(bump, BUMP_API_REQUEST_CODE);
	}

}
