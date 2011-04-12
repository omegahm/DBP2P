package dk.hotmovinglobster.dustytuba.api;

/** Setups and hands off connection */
public class BtAPI {

	/**
	 * A btConnectFailedReason is returned by the API when the user exits before connection has been established.
	 */
	public enum BtConnectFailedReason {
		FAIL_NONE, /** No failure */
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
		END_OTHER_USER_LOST /** remote user was lost */
	}

}
