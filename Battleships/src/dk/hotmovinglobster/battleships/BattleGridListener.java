package dk.hotmovinglobster.battleships;

import dk.hotmovinglobster.battleships.BattleGrid.Point;

public interface BattleGridListener {

	/**
	 * Called when the user selects a single tile
	 * 
 	 * Only called if getAllowMultiSelection() == false.
	 *
	 * @param tile
	 */
	public void onSingleTileHit(Point tile);
	
	/**
	 * Called when the user selects multiple (>=1) tiles.
	 * 
	 * The tiles selected lie in a straight line between tile1 and tile 2,
	 * both included.
	 * 
	 * Only called if getAllowMultiSelection() == true.
	 * @param tile1
	 * @param tile2
	 */
	public void onMultiTileHit(Point tile1, Point tile2);
	
	/**
	 * Decide if the user can select all the tiles between tile1 and tile2.
	 * 
	 * Only called if getAllowMultiSelection() == true.
	 * @param tile1
	 * @param tile2
	 * @return
	 */
	public boolean allowMultiSelectionBetween(Point tile1, Point tile2);
	
	

}
