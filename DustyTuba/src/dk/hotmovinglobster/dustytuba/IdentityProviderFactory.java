package dk.hotmovinglobster.dustytuba;

import dk.hotmovinglobster.dustytuba.identityproviders.*;

public class IdentityProviderFactory {
	
	
	public static final String IDENTITY_PROVIDER_BUMP = "bump";

	public static IdentityProvider getIdentityProvider(String provider) throws UnknownIdentityProviderException {
		if ( provider.equals( IDENTITY_PROVIDER_BUMP ) ) {
			return new Bump();
		}
		throw new UnknownIdentityProviderException(provider);
	}
	
}
