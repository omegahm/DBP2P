package dk.hotmovinglobster.battleships.comm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;
import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.Battleship;
import dk.hotmovinglobster.battleships.BattleshipPosition;
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
		None, Rules, PlaceShips, Shoot
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
	 * Transmission starts with PROTOCOL_SHIPS_PLACED, followed by a
	 * 4-byte integer representing how many ship positions are to be transferred,
	 * followed by, for each ship, two column/row pairs, each 2*4-byte integers
	 * with start and end positions
	 * 
	 * Total transmission size dependent on the naval fleet size
	 */
	private static final byte PROTOCOL_SHIPS_PLACED = 2;
	/**
	 * Transmission of a shot fired
	 * 
	 * Transmission starts with PROTOCOL_SHOOT
	 * followed by two 4-byte integers: Column no and row no
	 * 
	 * In total 9 bytes
	 */
	private static final byte PROTOCOL_SHOOT = 3;
	
	/**
	 * Transmission of user quitting the game
	 */
	private static final byte PROTOCOL_QUIT = 120;
	
	/**
	 * Used to save how many ships are expected in PLACE_SHIPS, 
	 * or actually how many bytes (=ships*16)
	 */
	private int mBufferIntArg1 = -1;
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
	public void sendShipsPlaced(List<BattleshipPosition> ships) {
		ByteArrayList byl = new ByteArrayList();
		byl.add( PROTOCOL_SHIPS_PLACED );
		byl.addAll( ByteArrayTools.toByta( ships.size() ) );
		
		for (BattleshipPosition bsp: ships) {
			Point pStart = bsp.getPosition().get( 0 );
			Point pEnd = bsp.getPosition().get( bsp.getPosition().size() - 1 );
			byl.addAll( ByteArrayTools.toByta( pStart.column ) );
			byl.addAll( ByteArrayTools.toByta( pStart.row    ) );
			byl.addAll( ByteArrayTools.toByta( pEnd.column ) );
			byl.addAll( ByteArrayTools.toByta( pEnd.row    ) );
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

	/**
	 * Send shot fired to the opponent
	 * @param p A point to shoot at
	 */
	public void sendShotFired(Point p) {
		ByteArrayList byl = new ByteArrayList();
		byl.add( PROTOCOL_SHOOT );
		byl.addAll( ByteArrayTools.toByta( p.column ) );
		byl.addAll( ByteArrayTools.toByta( p.row    ) );
		conn.send( byl.toArray() );
		Log.v( BattleshipsApplication.LOG_TAG, "CommunicationProtocol: SendShotFired("+p.column+", "+p.row+")");
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
				case PROTOCOL_SHOOT:
					mState = STATE.Shoot;
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: New state: Shoot");
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
				if (mBufferIntArg1 == -1) {
					if (mProtocolBuffer.size() == 4) {
						mBufferIntArg1 = 16 * ByteArrayTools.toInt( mProtocolBuffer.toArray() );
						mProtocolBuffer.clear();
					}
				} else if ( mProtocolBuffer.size() == mBufferIntArg1 ) {
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: All ship placement data received");

					// Each ship is two 8-byte column/row pairs
					int amount_ships = mProtocolBuffer.size() / 16;
					
					List<BattleshipPosition> ships = new ArrayList<BattleshipPosition>(amount_ships);
					

					for (int i = 0; i < amount_ships; i++ ) {
						int columnStart = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 16    ,  i * 16 + 3 ) );
						int rowStart    = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 16 + 4,  i * 16 + 7 ) );
						int columnEnd   = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 16 + 8,  i * 16 + 11) );
						int rowEnd      = ByteArrayTools.toInt( mProtocolBuffer.subArray(i * 16 + 12, i * 16 + 15) );
						Point pStart = new Point( columnStart, rowStart );
						Point pEnd = new Point( columnEnd, rowEnd );
						Battleship ship = BattleshipsApplication.resources().getBattleship( pStart.lengthTo( pEnd ) );
						ships.add( new BattleshipPosition( ship, pStart.pointsInStraightLineTo( pEnd )  ) );
					}
					
					if (mActivity != null) {
						Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: Sending ship placements to activity");
						mActivity.communicationShipsPlaced(ships);
					}
					
					mProtocolBuffer.clear();
					
					mState = STATE.None;
					mBufferIntArg1 = -1;
				}
				//////////////////////////////////////
				////////////// SHOOT /////////////////
				//////////////////////////////////////
			} else if (mState == STATE.Shoot) {
				if (mProtocolBuffer.size() == 8) {
					Log.v(BattleshipsApplication.LOG_TAG, "CommunicationProtocol: Shot fired!");

					int column = ByteArrayTools.toInt( mProtocolBuffer.subArray(0, 3) );
					int row    = ByteArrayTools.toInt( mProtocolBuffer.subArray(4, 7) );
					
					Point p = new Point(column, row);
					
					if (mActivity != null) {
						mActivity.communicationShotFired(p);
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
