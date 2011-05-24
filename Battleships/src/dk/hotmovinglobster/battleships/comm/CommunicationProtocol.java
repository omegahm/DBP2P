package dk.hotmovinglobster.battleships.comm;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.BattleshipsApplication;
import dk.hotmovinglobster.dustytuba.api.BtAPI.BtDisconnectReason;
import dk.hotmovinglobster.dustytuba.api.BtAPIListener;
import dk.hotmovinglobster.dustytuba.api.BtConnection;
import dk.hotmovinglobster.dustytuba.tools.ByteArrayList;
import dk.hotmovinglobster.dustytuba.tools.ByteArrayTools;

/**
 * Class responsible for handling the communication between two players.
 * 
 * For an activity to receive events from the other player, it needs to extend the
 * CommunicationProtocolActivity class and call setListeningActivity() with itself
 * as argument.
 * @author Jesper
 *
 */
public class CommunicationProtocol implements BtAPIListener {

	private enum STATE {
		None, Rules, PlaceShips
	};

	private STATE mState;
	private ByteArrayList mProtocolBuffer = new ByteArrayList(128);

	private final BtConnection conn;

	/**
	 * Transmission of rules
	 * 
	 * Transmission starts with PROTOCOL_RULES,
	 * followed by three 4-byte integers: Columns in grid, rows in grid
	 * and amount of single tile ships
	 * 
	 * In total 13 bytes
	 */
	private static final byte PROTOCOL_RULES = 1;
	/**
	 * Transmission of ships placed.
	 * 
	 * Transmission starts with PROTOCOL_SHIPS_PLACED,
	 * followed by GameContext.MAX_SHIPS column/row pairs
	 * each pair consisting of two 4-byte integers.
	 * 
	 * In total (MAX_SHIPS * 8) + 1 bytes
	 */
	private static final byte PROTOCOL_SHIPS_PLACED = 2;
	/**
	 * Transmission of user quitting the game
	 */
	private static final byte PROTOCOL_QUIT = 120;
	
	private CommunicationProtocolActivity mActivity = null;

	public CommunicationProtocol(BtConnection conn) {
		this.conn = conn;
		this.conn.setListener(this);
		this.mState = STATE.None;
		
		Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: is server: " + conn.isServer() );
	}
	
	/**
	 * Is this player the server or the client?
	 * 
	 * @return true if server, false otherwise
	 */
	public boolean isServer() {
		return conn.isServer();
	}
	
	/**
	 * Sets the activity to receive events from the communication.
	 * 
	 * Only one activity can receive events at a time.
	 * @param l The activity to receive events
	 */
	public void setListeningActivity(CommunicationProtocolActivity l) {
		this.mActivity = l;
	}
	
	/**
	 * Send a list of ships placed to the opponent
	 * @param ships A list of Points (column/row pairs)
	 */
	public void sendShipsPlaced(List<Point> ships) {
		ByteArrayList byl = new ByteArrayList();
		byl.add( PROTOCOL_SHIPS_PLACED );
		for (Point p: ships) {
			byl.addAll( ByteArrayTools.toByta( p.column ) );
			byl.addAll( ByteArrayTools.toByta( p.row    ) );
		}
		conn.send( byl.toArray() );
		Log.v( BattleshipsApplication.LOG_TAG, "CommunicationProtocol: SendShipsPlaced("+ships.size()+" ships)");
	}

	/**
	 * Sends the rules to the opponent
	 * 
	 * @param columns Columns in grid
	 * @param rows Rows in grid
	 * @param single_tile_ships Amount of single tile ships
	 */
	public void sendRules(int columns, int rows, int single_tile_ships) {
		ByteArrayList byl = new ByteArrayList();
		byl.add( PROTOCOL_RULES );
		byl.addAll( ByteArrayTools.toByta( columns ) );
		byl.addAll( ByteArrayTools.toByta( rows ) );
		byl.addAll( ByteArrayTools.toByta( single_tile_ships ) );
		conn.send( byl.toArray() );
		Log.v( BattleshipsApplication.LOG_TAG, "CommunicationProtocol: SendRules("+columns+", "+rows+", " + single_tile_ships + ")");
	}
	
	@Override
	public void btDataReceived(byte[] arg0) {
		Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: Received chunk of data (size: " + arg0.length + "):");
		Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: " + arg0);
		for (int i = 0; i < arg0.length; i++)
			byteReceived(arg0[i]);
	}

	private void byteReceived(byte b) {
		//Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: Received byte ("+b+")");
		if (mState == STATE.None) {
			switch (b) {
				case PROTOCOL_RULES:
					mState = STATE.Rules;
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: New state: Rules");
					break;
				case PROTOCOL_SHIPS_PLACED:
					mState = STATE.PlaceShips;
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: New state: PlaceShips");
					break;
				case PROTOCOL_QUIT:
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: Opponent sent disconnect message");
					disconnect();
					break;
				default:
					break;
			}
		} else {
			mProtocolBuffer.add( b );
			//////////////////////////////////////
			////////////// RULES /////////////////
			//////////////////////////////////////
			if (mState == STATE.Rules) {
				if (mProtocolBuffer.size() == 12) {
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: All rules data received");

					int columns           = ByteArrayTools.toInt( mProtocolBuffer.subArray(0, 3) );
					int rows              = ByteArrayTools.toInt( mProtocolBuffer.subArray(4, 7) );
					int single_tile_ships = ByteArrayTools.toInt( mProtocolBuffer.subArray(8, 11) );
					
					if (mActivity != null) {
						Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: Sending rules to activity");
						mActivity.communicationRulesReceived(columns, rows, single_tile_ships);
					}
					mProtocolBuffer.clear();
					
					mState = STATE.None;
				}
				//////////////////////////////////////
				/////////// PLACE SHIPS //////////////
				//////////////////////////////////////
			} else if (mState == STATE.PlaceShips) {
				if (mProtocolBuffer.size() == BattleshipsApplication.context().MAX_SHIPS * 8) {
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: All ship placement data received");

					// Each ship is a 4-byte column integer followed by a 4-byte row integer
					int amount_ships = mProtocolBuffer.size() / 8;
					
					List<Point> ships = new ArrayList<Point>(amount_ships);

					for (int i = 0; i < amount_ships; i++ ) {
						int column = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 8    , i * 8 + 3) );
						int row    = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 8 + 4, i * 8 + 7) );
						ships.add( new Point(column, row ) );
					}
					
					if (mActivity != null) {
						Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: Sending ship placements to activity");
						mActivity.communicationShipsPlaced(ships);
					}
					
					mProtocolBuffer.clear();
					
					mState = STATE.None;
				}
			}

		
		}
	}
	

	@Override
	public void btDisconnect(BtDisconnectReason arg0) {
		if (mActivity != null) {
			mActivity.communicationDisconnected();
		}

	}
	
	private void sendDisconnectMessage() {
		Log.v( BattleshipsApplication.LOG_TAG, "CommunicationProtocol: SentDisconnectMessage()");
		conn.send( new byte[] {PROTOCOL_QUIT} );
	}

	/**
	 * Disconnect and notify the opponent.
	 */
	public void disconnect() {
		sendDisconnectMessage();
		conn.disconnect();
	}

}
