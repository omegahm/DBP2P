package dk.hotmovinglobster.battleships.comm;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.GameContext;
import dk.hotmovinglobster.dustytuba.api.BtAPIListener;
import dk.hotmovinglobster.dustytuba.tools.*;
import dk.hotmovinglobster.dustytuba.api.BtAPI.BtDisconnectReason;
import dk.hotmovinglobster.dustytuba.api.BtConnection;

public class CommunicationProtocol implements BtAPIListener {

	private enum STATE {
		None, PlaceShips
	};

	private STATE mState;
	private ByteArrayList mProtocolBuffer = new ByteArrayList(128);

	private final BtConnection conn;

	private static final byte PROTOCOL_SHIPS_PLACED = 2;
	private static final byte PROTOCOL_QUIT = 120;
	
	private CommunicationProtocolActivity mActivity = null;

	public CommunicationProtocol(BtConnection conn) {
		this.conn = conn;
		this.conn.setListener(this);
		this.mState = STATE.None;
		
		Log.v(GameContext.LOG_TAG, "CommunicationProtocol: is server: " + conn.isServer() );
	}
	
	public void setListener(CommunicationProtocolActivity l) {
		this.mActivity = l;
	}
	
	public void sendShipsPlaced(List<Point> ships) {
		ByteArrayList byl = new ByteArrayList();
		byl.add( PROTOCOL_SHIPS_PLACED );
		for (Point p: ships) {
			byl.addAll( ByteArrayTools.toByta( p.column ) );
			byl.addAll( ByteArrayTools.toByta( p.row    ) );
		}
		conn.send( byl.toArray() );
		Log.v( GameContext.LOG_TAG, "CommunicationProtocol: SenShipsPlaced("+ships.size()+" ships)");
	}

	private void sendDisconnectMessage() {
		Log.v( GameContext.LOG_TAG, "CommunicationProtocol: SentDisconnectMessage()");
		conn.send( new byte[] {PROTOCOL_QUIT} );
	}

	@Override
	public void btDataReceived(byte[] arg0) {
		Log.v(GameContext.LOG_TAG, "CommunicationProtocol: Received chunk of data (size: " + arg0.length + "):");
		Log.v(GameContext.LOG_TAG, "CommunicationProtocol: " + arg0);
		for (int i = 0; i < arg0.length; i++)
			byteReceived(arg0[i]);
	}

	private void byteReceived(byte b) {
		//Log.v(GameContext.LOG_TAG, "CommunicationProtocol: Received byte ("+b+")");
		if (mState == STATE.None) {
			switch (b) {
				case PROTOCOL_SHIPS_PLACED:
					mState = STATE.PlaceShips;
					Log.v(GameContext.LOG_TAG, "CommunicationProtocol: New state: PlaceShips");
					break;
				case PROTOCOL_QUIT:
					Log.v(GameContext.LOG_TAG, "CommunicationProtocol: Opponent sent disconnect message");
					disconnect();
					break;
				default:
					break;
			}
		} else {
			mProtocolBuffer.add( b );
			if (mState == STATE.PlaceShips) {
				if (mProtocolBuffer.size() == GameContext.singleton.MAX_SHIPS * 8) {

					// Each ships is a 4-byte column integer and a 4-byte row integer
					int amount_ships = mProtocolBuffer.size() / 8;
					
					List<Point> ships = new ArrayList<Point>(amount_ships);

					for (int i = 0; i < amount_ships; i++ ) {
						int column = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 8    , i * 8 + 3) );
						int row    = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 8 + 4, i * 8 + 7) );
						ships.add( new Point(column, row ) );
						Log.v(GameContext.LOG_TAG, "CommunicationProtocol: ship no. " + ships.size() + " added ("+column+","+row+")");
						Log.v(GameContext.LOG_TAG, "CommunicationProtocol: "+ (i*8) + ", " + (i*8+3) + ", "+ (i*8+4) + ", " + (i*8+7));
						Log.v(GameContext.LOG_TAG, "CommunicationProtocol: "+ ByteArrayTools.toString( mProtocolBuffer.subArray(i * 8    , i * 8 + 3) ) + ", " + ByteArrayTools.toString( mProtocolBuffer.subArray(i * 8 + 4, i * 8 + 7) ) );
						Log.v(GameContext.LOG_TAG, "CommunicationProtocol: "+ mProtocolBuffer.subArray(i * 8    , i * 8 + 3).length + ", " + mProtocolBuffer.subArray(i * 8 + 4, i * 8 + 7).length );
						Log.v(GameContext.LOG_TAG, "CommunicationProtocol: --------------------------------------------------" );
					}

					if (mActivity != null) {
						mActivity.communicationShipsPlaced(ships);
					}
					
					mProtocolBuffer.clear();
					
					mState = STATE.None;
				}
			}
		
		}
	}
	
	/*
	private void otherRandomHostNumberObtained() {
		Log.v( GameContext.LOG_TAG, "CommunicationProtocol: otherRandomHostNumberObtained("+otherRandomHostNumber+")");
		if ( myRandomHostNumber > otherRandomHostNumber ) {
			Log.v( GameContext.LOG_TAG, "CommunicationProtocol: I am host");
		} else {
			Log.v( GameContext.LOG_TAG, "CommunicationProtocol: Opponent is host");
		}
		
	}
*/
	@Override
	public void btDisconnect(BtDisconnectReason arg0) {
		if (mActivity != null) {
			mActivity.communicationDisconnected();
		}

	}
	
	public void disconnect() {
		sendDisconnectMessage();
		conn.disconnect();
		
	}

}
