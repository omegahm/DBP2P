package dk.hotmovinglobster.dustytuba.api;

/**
 * A BT API Listener, that handles events from the BT connection 
 * once it has been established.
 * 
 * @author thomas
 */
public abstract interface BtAPIListener {
  
	/**
	 * Called when the API connection terminates
	 * @param arg0
	 */
	public abstract void btDisconnect(BtAPI.BtDisconnectReason arg0);
  
	/**
	 * Called when a chunk of data is received through BT connection
	 * @param arg0 data
	 */
	public abstract void btDataReceived(byte[] arg0);
}