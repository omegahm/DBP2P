package dk.hotmovinglobster.dustytuba;

public class UnknownIdentityProviderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnknownIdentityProviderException(String provider) {
		super( "Unknown IdentityProvider \"" + provider + "\"" );
	}

}
