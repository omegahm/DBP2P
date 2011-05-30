package dk.hotmovinglobster.battleships;

import java.util.ArrayList;
import java.util.List;

import dk.hotmovinglobster.battleships.comm.CommunicationProtocol;

public class GameContext {
	
	public int GRID_COLUMNS;
	public int GRID_ROWS;
	
	public int[] MAX_SHIPS = new int[6];
	
	public CommunicationProtocol Comm;
	
	public List<BattleshipPosition> myShips = new ArrayList<BattleshipPosition>();
	public List<BattleshipPosition> opponentShips = new ArrayList<BattleshipPosition>();
	
	/**
	 * Used to save the shots fired for show battlefield after game
	 */
	public BattleGrid.TileType[][] myTileTypes;
	/**
	 * Used to save the shots fired for show battlefield after game
	 */
	public BattleGrid.TileType[][] opponentTileTypes;
	
	public GameContext() {
		GRID_COLUMNS = 8;
		GRID_ROWS = 8;
		
		MAX_SHIPS[0] = 0;
		MAX_SHIPS[1] = 0;
		
		MAX_SHIPS[2] = 3;
		MAX_SHIPS[3] = 2;
		MAX_SHIPS[4] = 2;
		MAX_SHIPS[5] = 1;
	}
	
	/*
	public GameContext(int columns, int rows, int ships) {
		GRID_COLUMNS = columns;
		GRID_ROWS = rows;
		MAX_SHIPS = ships;
	}
	
	public static void redefineRules(int columns, int rows, int ships) {
		GameContext newContext = new GameContext(columns, rows, ships);
		newContext.Comm = singleton.Comm;
		
		singleton = newContext;
	}
	*/
	
}
