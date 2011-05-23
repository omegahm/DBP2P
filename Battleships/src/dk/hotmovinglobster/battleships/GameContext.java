package dk.hotmovinglobster.battleships;

import dk.hotmovinglobster.battleships.comm.CommunicationProtocol;

public class GameContext {
	
	public static GameContext singleton = new GameContext();
	
	public final int GRID_COLUMNS;
	public final int GRID_ROWS;
	
	public final int MAX_SHIPS;
	
	public CommunicationProtocol Comm;
	
	public static String LOG_TAG = "Battleships";
	
	private GameContext() {
		GRID_COLUMNS = 6;
		GRID_ROWS = 6;
		MAX_SHIPS = 5;
	}
	
}
