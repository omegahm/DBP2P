package dk.hotmovinglobster.battleships;

import dk.hotmovinglobster.battleships.BattleGrid.Point;

public interface BattleGridListener {
	
	public void onSingleTileHit(Point tile);
	public void onMultiTileHit(Point tile1, Point tile2);
	
	public boolean allowMultiSelectionBetween(Point tile1, Point tile2);
	
	

}
