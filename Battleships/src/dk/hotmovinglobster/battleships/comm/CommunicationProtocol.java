package dk.hotmovinglobster.battleships.comm;

import android.util.Log;
import dk.hotmovinglobster.battleships.GameContext;
import dk.hotmovinglobster.dustytuba.api.BtAPIListener;
import dk.hotmovinglobster.dustytuba.tools.*;
import dk.hotmovinglobster.dustytuba.api.BtAPI.BtDisconnectReason;
import dk.hotmovinglobster.dustytuba.api.BtConnection;

public class CommunicationProtocol implements BtAPIListener {

	private enum STATE {
		None, DecideHost, PlaceShips
	};

	private STATE mState;
	private ByteArrayList mProtocolBuffer = new ByteArrayList(128);

	private final BtConnection conn;
	
	private double myRandomHostNumber;
	private double otherRandomHostNumber;

	private static final byte PROTOCOL_DECIDE_HOST = 1;

	public CommunicationProtocol(BtConnection conn) {
		this.conn = conn;
		this.conn.setListener(this);
		this.mState = STATE.None;

		initProtocol();
	}

	private void initProtocol() {
		myRandomHostNumber = java.lang.Math.random();
		sendDecideHostMessage();
		// TODO Auto-generated method stub

	}

	private void sendDecideHostMessage() {
		ByteArrayList byl = new ByteArrayList();
		byl.add( PROTOCOL_DECIDE_HOST );
		byl.addAll( ByteArrayTools.toByta( myRandomHostNumber ) );
		conn.send( byl.toArray() );
		Log.v( GameContext.LOG_TAG, "CommunicationProtocol: SentDecideHostMessage("+myRandomHostNumber+")");
	}

	@Override
	public void btDataReceived(byte[] arg0) {
		for (int i = 0; i < arg0.length; i++)
			byteReceived(arg0[i]);
	}

	private void byteReceived(byte b) {
		if (mState == STATE.None) {
			switch (b) {
				case PROTOCOL_DECIDE_HOST:
					mState = STATE.DecideHost;
					break;
				default:
					break;
			}
		} else {
			mProtocolBuffer.add( b );
			if (mState == STATE.DecideHost) {
				if (mProtocolBuffer.size() == 8) {
					otherRandomHostNumber = ByteArrayTools.toDouble( mProtocolBuffer.toArray() );
					mProtocolBuffer.clear();
					otherRandomHostNumberObtained();
				}
			}
			
		}
	}

	private void otherRandomHostNumberObtained() {
		Log.v( GameContext.LOG_TAG, "CommunicationProtocol: otherRandomHostNumberObtained("+otherRandomHostNumber+")");
		if ( myRandomHostNumber > otherRandomHostNumber ) {
			Log.v( GameContext.LOG_TAG, "CommunicationProtocol: I am host");
		} else {
			Log.v( GameContext.LOG_TAG, "CommunicationProtocol: Opponent is host");
		}
		
	}

	@Override
	public void btDisconnect(BtDisconnectReason arg0) {
		// TODO Auto-generated method stub

	}

}
