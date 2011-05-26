package dk.hotmovinglobster.battleships;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.util.Log;
import dk.hotmovinglobster.battleships.BattleGrid.Point;

public class BattleshipPosition {
	
	private final Battleship ship;
	private final List<Point> position;
	
	private List<Bitmap> rotatedBitmaps;
	
	public static final int RIGHT = 0;
	public static final int UP = 1;
	public static final int LEFT = 2;
	public static final int DOWN = 3;
	private int orientation;
	
	public BattleshipPosition(Battleship ship, List<Point> position) {
		this.ship = ship;
		this.position = position;
		assert(ship.getLength() == position.size());
		decideOrientation();
	}
	
	public Battleship getShip() {
		return ship;
	}
	
	public List<Point> getPosition() {
		return position;
	}
	
	public int getOrientation() {
		return orientation;
	}
	
	public List<Bitmap> getTileBitmaps() {
		if (rotatedBitmaps == null) {
			int length = ship.getLength();
			rotatedBitmaps = new ArrayList<Bitmap>();
			rotatedBitmaps.add( getRotatedBitmap(ship.getStartTile()) );
			if (length == 1)
				return rotatedBitmaps;
			rotatedBitmaps.add( getRotatedBitmap(ship.getMiddleTile1()) );
			if (length == 2)
				return rotatedBitmaps;
			rotatedBitmaps.add( getRotatedBitmap(ship.getMiddleTile2()) );
			if (length == 3)
				return rotatedBitmaps;
			rotatedBitmaps.add( getRotatedBitmap(ship.getMiddleTile3()) );
			if (length == 4)
				return rotatedBitmaps;
			rotatedBitmaps.add( getRotatedBitmap(ship.getEndTile()) );
		}
		return rotatedBitmaps;
	}
	
	private Bitmap getRotatedBitmap(RotatableBitmap rbmp) {
		switch(orientation) {
			case UP:
				return rbmp.getRotated90();
			case LEFT:
				return rbmp.getRotated180();
			case DOWN:
				return rbmp.getRotated270();
			case RIGHT:
				default:
				return rbmp.getOriginal();
		}
	}
	
	private void decideOrientation() {
		Point first = position.get( 0 );
		Point last = position.get( position.size() - 1 );
		
		// horizontal
		if (first.row == last.row) {
			if (first.column < last.column) {
				Log.v(BattleshipsApplication.LOG_TAG, "Orientation: Right");
				orientation = RIGHT;
			} else {
				Log.v(BattleshipsApplication.LOG_TAG, "Orientation: Left");
				orientation = LEFT;
			}
		}
		// Else, assume vertital
		else {
			if (first.row <= last.row) {
				Log.v(BattleshipsApplication.LOG_TAG, "Orientation: Up");
				orientation = UP;
			} else {
				Log.v(BattleshipsApplication.LOG_TAG, "Orientation: Down");
				orientation = DOWN;
			}
		}
	}
}