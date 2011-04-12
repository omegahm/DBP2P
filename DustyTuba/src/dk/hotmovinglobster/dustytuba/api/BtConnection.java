package dk.hotmovinglobster.dustytuba.api;

import android.os.Handler;

public class BtConnection {
	
	// TODO: protect?
	public BtConnection (){
		// TODO:
	}
	
	/** Sets the btAPIListener for this connection */
	public void setListener(BtAPIListener l){}
	
	/** Sets the btAPIListener for this connection defining a Handler on which the calls will be made */
	public void setListener(BtAPIListener l, Handler handler){}
	
	/** Send a chunk to the other user */
	public void send(byte[] chunk){}
	
	/** Disconnect from the API service */
	public void disconnect(){}
}