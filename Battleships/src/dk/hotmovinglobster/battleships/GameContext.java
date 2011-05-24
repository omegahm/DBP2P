package dk.hotmovinglobster.battleships;

import java.util.ArrayList;
import java.util.List;

import dk.hotmovinglobster.battleships.BattleGrid.Point;
import dk.hotmovinglobster.battleships.comm.CommunicationProtocol;

public class GameContext {
	
	public int GRID_COLUMNS;
	public int GRID_ROWS;
	
	public int MAX_SHIPS;
	
	public CommunicationProtocol Comm;
	
	public List<Point> myShips = new ArrayList<Point>();
	public List<Point> opponentShips = new ArrayList<Point>();
	
	public GameContext() {
		GRID_COLUMNS = 6;
		GRID_ROWS = 6;
		MAX_SHIPS = 5;
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
