package dk.hotmovinglobster.battleships;

public class Battleship {
	
	private final RotatableBitmap startTile;
	private final RotatableBitmap middleTile1;
	private final RotatableBitmap middleTile2;
	private final RotatableBitmap middleTile3;
	private final RotatableBitmap endTile;
	
	private int length;
	
	private final String name;
	
	public Battleship( String name, RotatableBitmap startTile, RotatableBitmap middleTile1, RotatableBitmap middleTile2, RotatableBitmap middleTile3, RotatableBitmap endTile ) {
		assert( startTile != null );
		this.startTile = startTile;
		this.middleTile1 = middleTile1;
		this.middleTile2 = middleTile2;
		this.middleTile3 = middleTile3;
		this.endTile = endTile;
		this.name = name;
		
		if (this.middleTile1 == null) {
			length = 1;
		} else if (this.middleTile2 == null) {
			length = 2;
		} else if (this.middleTile3 == null) {
			length = 3;
		} else if (this.endTile == null) {
			length = 4;
		} else {
			length = 5;
		}
		
	}
	
	public RotatableBitmap getStartTile() {
		return startTile;
	}

	public RotatableBitmap getMiddleTile1() {
		return middleTile1;
	}

	public RotatableBitmap getMiddleTile2() {
		return middleTile2;
	}

	public RotatableBitmap getMiddleTile3() {
		return middleTile3;
	}

	public RotatableBitmap getEndTile() {
		return endTile;
	}

	public int getLength() {
		return length;
	}
	
	@Override
	public String toString() {
		return "Battleship " + name + " ("+length+" tiles)";
	}
}
