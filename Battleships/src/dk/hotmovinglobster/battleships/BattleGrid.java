package dk.hotmovinglobster.battleships;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class BattleGrid extends View {

	/**
	 * Amount of horizontal tiles
	 */
	private int mColumns;
	/**
	 * Amount of vertical tiles
	 */
	private int mRows;

	/**
	 * The width of each tile in pixels
	 */
	private int mTileWidth;
	/**
	 * The height of each tile in pixels
	 */
	private int mTileHeight;
	
	/**
	 * The height of each tile in pixels
	 */
	private final int mTileBorder = 1;
	
	public enum TileType { EMPTY, SHIP, HIT, MISS };
	private TileType[][] tiles;
	
	private Bitmap[][] tileBitmaps;
	private List<BattleshipPosition> battleshipPositions = new ArrayList<BattleshipPosition>();

	private Paint TileBackground;
	private Paint TileBorder;
	private Paint TileHover;
	
	private BattleGridListener mListener;
	
	/**
	 * The tile that the user pressed down last.
	 * 
	 * Used to keep track of dragging over multiple tiles
	 */
	private Point tileFirstPressed;
	
	/**
	 * The tile that the user touched last.
	 */
	private Point tileLastTouched;
	
	/**
	 * Did the mListener allow for multi selection between tileLastPressed
	 * and tileLastTouched?
	 */
	private boolean listenerAllowedMultiSelection = false;
	
	/**
	 * Allow for multiple tile selection
	 */
	private boolean allowMultiSelection = false;
	
	private boolean showNonDestroyedShips = true;

	public BattleGrid(Context context, int columns, int rows) {
		super(context);
		Log.v(BattleshipsApplication.LOG_TAG, "BattleGrid: constructor()");
		mColumns = columns;
		mRows = rows;
		
		TileBackground = new Paint();
		TileBackground.setColor( Color.WHITE );
		TileBackground.setAlpha(160);
		TileBorder = new Paint();
		TileBorder.setColor( Color.BLACK );
		TileBorder.setAlpha(160);
		TileHover = new Paint();
		TileHover.setColor( Color.YELLOW );
		TileHover.setAlpha(128);
		initializeTiles();
	}

	private void initializeTiles() {
		tiles = new TileType[mColumns][mRows];
		for (int column=0; column<mColumns; column++) {
			for (int row=0; row<mRows; row++) {
				tiles[column][row] = TileType.EMPTY;
			}
		}
		tileBitmaps = new Bitmap[mColumns][mRows];
	}

	public void setListener(BattleGridListener listener) {
		this.mListener = listener;
	}

	public BattleGridListener getListener() {
		return mListener;
	}
	
	public void setAllowMultiSelection(boolean allowMultiSelection) {
		this.allowMultiSelection = allowMultiSelection;
	}

	public boolean getAllowMultiSelection() {
		return allowMultiSelection;
	}

	public void setShowNonDestroyedShips(boolean showShips) {
		this.showNonDestroyedShips = showShips;
	}

	public boolean getShowNonDestroyedShips() {
		return showNonDestroyedShips;
	}

	public void setTileType(int column, int row, TileType type) {
		if (tiles[column][row] != type) {
			tiles[column][row] = type;
			invalidate();
		}
	}
	
	public void setTileType(Point p, TileType type) {
		if (tiles[p.column][p.row] != type) {
			tiles[p.column][p.row] = type;
			invalidate();
		}
	}

	public TileType getTileType(int column, int row) {
		return tiles[column][row];
	}
	
	public TileType getTileType(Point p) {
		return tiles[p.column][p.row];
	}
	
	public List<Point> getPointsWithType(TileType type) {
		List<Point> result = new ArrayList<Point>();
		for (int c = 0; c < mColumns; c++) {
			for (int r = 0; r < mRows; r++) {
				if (tiles[c][r] == type) {
					result.add( new Point(c, r) );
				}
			}
		}
		return result;
	}
	
	public boolean undo() {
		if (battleshipPositions.isEmpty()) {
			return false;
		}
		BattleshipPosition bsp = battleshipPositions.get( battleshipPositions.size() - 1 );
		battleshipPositions.remove( bsp );
		
		for (Point p: bsp.getPosition()) {
			tiles[p.column][p.row] = TileType.EMPTY;
			tileBitmaps[p.column][p.row] = null;
		}
		
		invalidate();
		return true;
	}
	
	public void placeShipInTiles(List<Point> position, Battleship ship) {
		BattleshipPosition bsp = new BattleshipPosition(ship, position);
		battleshipPositions.add( bsp );
		
		Log.d(BattleshipsApplication.LOG_TAG, "BattleGrid: Placing " + ship);
		
		List<Bitmap> bitmaps = bsp.getTileBitmaps();
		
		for (int i = 0; i < bsp.getPosition().size(); i++ ) {
			Point p = bsp.getPosition().get( i );
			Bitmap bmp = bitmaps.get( i );
			tileBitmaps[p.column][p.row] = bmp;
			tiles[p.column][p.row] = TileType.SHIP;
		}
		
		invalidate();
	}
	
	public List<BattleshipPosition> getBattleshipPositions() {
		return battleshipPositions;
	}
	
	private boolean isShipDestroyed(BattleshipPosition bsp) {
		for (Point p: bsp.getPosition()) {
			if (getTileType(p) != TileType.HIT) {
				return false;
			}
		}
		return true;
	}
	
	private BattleshipPosition getShipInTile(Point p) {
		for (BattleshipPosition bsp: battleshipPositions) {
			if (bsp.getPosition().contains( p ) ) {
				return bsp;
			}
		}
		return null;
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int maxSize = 0;
		if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
			maxSize = MeasureSpec.getSize(widthMeasureSpec);
		}
		if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED) {
			maxSize = java.lang.Math.min( MeasureSpec.getSize(heightMeasureSpec), maxSize );
		}
		if (maxSize == 0) {
			// TODO: Better size, find out what triggers UNSPECIFIED
			maxSize = 479;
		}
		setMeasuredDimension(maxSize, maxSize);
		recalculateSizes();
	}

	private void recalculateSizes() {
		mTileWidth  = (int) java.lang.Math.floor( getMeasuredWidth()  / mColumns  ) - mTileBorder;
		mTileHeight = (int) java.lang.Math.floor( getMeasuredHeight() / mRows ) - mTileBorder;
	}

	@Override
	public void onDraw(Canvas c) {
		int width  = getMeasuredWidth();
		int height = getMeasuredHeight();
		
		//c.drawPaint(TileBackground);
		
		///////////////////////////
		//// DRAW TILE BORDERS ////
		///////////////////////////
		for (int col = 1; col < mColumns; col++ ) {
			c.drawLine(0, col*(mTileWidth+mTileBorder), height, col*(mTileWidth+mTileBorder), TileBorder);
		}
		for (int row = 1; row < mRows; row++ ) {
			c.drawLine(row*(mTileHeight+mTileBorder), 0, row*(mTileHeight+mTileBorder), width, TileBorder);
		}
		
		///////////////////////////
		////// DRAW EACH TILE /////
		///////////////////////////
		for (int column=0; column<mColumns; column++) {
			for (int row=0; row<mRows; row++) {
				Point tile = new Point(column, row);
				Rect r = getTileRect(column, row);
				TileType type = getTileType(tile);
				//c.drawRect(r, TileBackground);
				
				boolean drawHover = false;
				
				if (tileFirstPressed != null) {
					
					if (allowMultiSelection) {

						/* If there is a straight line between the tile first pressed
						 * and the tile last touched, AND the current tile is in that
						 * straight line, draw hover here as well */
						
						if (listenerAllowedMultiSelection && 
							tile.isInStraightLineBetween(tileFirstPressed, tileLastTouched)) {
							drawHover = true;
						}
					}
					/* If multiselection is not allowed only draw hover
					 * if touching the first tile pressed */
					
					else {
//						if ( tile.equals( tileFirstPressed ) && tile.equals( tileLastTouched ) ) {
						if ( tile.equals( tileLastTouched ) ) {
							drawHover = true;
						}
					}
				}
				
				if (drawHover) {
					c.drawRect( r, TileHover );
				}
				
				Bitmap tileBitmap = tileBitmaps[column][row];
				if (tileBitmap != null) {
					if (showNonDestroyedShips || isShipDestroyed( getShipInTile(tile) ) ) {
						c.drawBitmap(tileBitmap, null, r, null);
					}
				}
				if (type == TileType.HIT) {
					c.drawBitmap(BattleshipsApplication.resources().Explosion, null, r, null);
				} else if (type == TileType.MISS) {
					c.drawBitmap(BattleshipsApplication.resources().Hit, null, r, null);
				}

			}
		}
		
	}

	/**
	 * Gets the rectangle corresponding to tile (x,y) (zero-indexed)
	 * @param column
	 * @param row
	 * @return 
	 */
	private Rect getTileRect(int column, int row) {
		return new Rect( column * (mTileWidth+mTileBorder),
				row * (mTileHeight+mTileBorder),
				(column+1) * (mTileWidth+mTileBorder),
				(row+1) * (mTileHeight+mTileBorder));
	}

	private Point getTileWithPoint(int x, int y) {
		for (int column=0; column<mRows; column++) {
			for (int row=0; row<mColumns; row++) {
				if (getTileRect(column, row).contains(x, y)) {
					return new Point( column, row );
				}
			}
		}
		return null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		int x = (int)e.getX();
		int y = (int)e.getY();
		Point tile = getTileWithPoint( x, y );
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
//			Log.v(BattleshipsApplication.LOG_TAG, "BattleGrid: ACTION_DOWN: " + tile);
			if ( tile != null ) {
				tileFirstPressed = tile;
				tileLastTouched = tile;
				if (allowMultiSelection && mListener != null) {
					listenerAllowedMultiSelection = mListener.allowMultiSelectionBetween(tile, tile);
				}
				postInvalidate();
				return true;
			}
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
//			Log.v(BattleshipsApplication.LOG_TAG, "BattleGrid: ACTION_UP: " + tile);

			if ( tile != null && mListener != null ) {
				if ( allowMultiSelection ) {
					if ( listenerAllowedMultiSelection ) {
						mListener.onMultiTileHit(tileFirstPressed, tileLastTouched);
					}
				} else {
					mListener.onSingleTileHit( tileLastTouched );
				}
			}
			
			tileFirstPressed = null;
			tileLastTouched = null;
			postInvalidate();
			return true;
//			}
		} else if (e.getAction() == MotionEvent.ACTION_CANCEL) {
//			Log.i(BattleshipsApplication.LOG_TAG, "BattleGrid: ACTION_CANCEL");
			tileFirstPressed = null;
			tileLastTouched = null;
			postInvalidate();
			return true;
		} else if (e.getAction() == MotionEvent.ACTION_OUTSIDE) {
//			Log.i(BattleshipsApplication.LOG_TAG, "BattleGrid: ACTION_OUTSIDE");
			tileFirstPressed = null;
			tileLastTouched = null;
			postInvalidate();
			return true;
		} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			if ( tile != null && tileLastTouched != null && !tile.equals(tileLastTouched) ) {
				tileLastTouched = tile;
				if (allowMultiSelection && mListener != null && tileFirstPressed != null && tile.sharesColumnOrRowWith( tileFirstPressed )) {
					Log.v(BattleshipsApplication.LOG_TAG, "BattleGrid: go ask listener for multi selection");
					listenerAllowedMultiSelection = mListener.allowMultiSelectionBetween(tileFirstPressed, tileLastTouched);
				} else {
					listenerAllowedMultiSelection = false;
				}
				postInvalidate();
			}
			return true;
		}
		return super.onTouchEvent(e);
	}
	
	public static class Point {
		
		public final int column;
		public final int row;
		
		public Point(int column, int row) {
			this.column = column;
			this.row = row;
		}
		
		@Override
		public String toString() {
			return String.format("(%d, %d)", column, row);
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Point) {
				Point p = (Point)o;
				if (p.column == this.column && p.row == this.row) {
					return true;
				}
			}
			return false;
		}
		
		/**
		 * Determines if the point is in a straight line between two other points
		 */
		public boolean isInStraightLineBetween(Point p1, Point p2) {
			// Vertical line
			if (p1.column == p2.column && p1.column == this.column) {
				return between(this.row, p1.row, p2.row);
			}
			// Horizontal line
			else if (p1.row == p2.row && p1.row == this.row) {
				return between(this.column, p1.column, p2.column);
			}
			
			else return false;
		}
		
		/**
		 * Returns the points in a straight line between the point and
		 * the given point p.
		 * 
		 * If the points are not in a straight line, an empty list is returned.
		 * 
		 * Both end points are included, i.e. if the point is equal to p, a
		 * list containing only the point is returned
		 */
		public List<Point> pointsInStraightLineTo(Point p) {
			List<Point> result = new ArrayList<Point>();
			// Vertical line
			if (this.column == p.column) {
				if (this.row < p.row) {
					for (int r=this.row; r<=p.row; r++) {
						result.add( new Point( this.column, r ) );
					}
				} else {
					for (int r=this.row; r>=p.row; r--) {
						result.add( new Point( this.column, r ) );
					}
				}
			}
			// Horizontal line
			else if (this.row == p.row) {
				if (this.column < p.column) {
					for (int c=this.column; c<=p.column; c++) {
						result.add( new Point( c, this.row ) );
					}
				} else {
					for (int c=this.column; c>=p.column; c--) {
						result.add( new Point( c, this.row ) );
					}
				}
			}
			
			return result;
		}
		
		/**
		 * Determines if the point is in the same column or row as
		 * the given point p
		 */
		public boolean sharesColumnOrRowWith(Point p) {
			Log.d(BattleshipsApplication.LOG_TAG, "BattleGrid::Point: sharedColumn " + this + ", " + p + ", " +(this.column==p.column || this.row==p.row));
			return (this.column==p.column || this.row==p.row);
		}
		
		/**
		 * Calculates distance to another point. Returns 1 for same point
		 * 
		 * Equals ( row difference + column difference + 1 )
		 */
		public int lengthTo(Point p) {
			return java.lang.Math.abs( this.column - p.column ) +
				   java.lang.Math.abs( this.row - p.row ) + 1;
		}
		
		/**
		 * Is i between a and b? (Both included)
		 * @param i
		 * @param a
		 * @param b
		 * @return
		 */
		private boolean between(int i, int a, int b) {
			if (a>b)
				return (i>=b && i<=a);
			else
				return (i>=a && i<=b);
		}
		
	}
}
