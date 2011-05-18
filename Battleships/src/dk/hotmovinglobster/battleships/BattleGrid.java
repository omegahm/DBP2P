package dk.hotmovinglobster.battleships;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
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

	private Bitmap HitTile;
	private Bitmap MissTile;
	private Bitmap ShipTile;
	
	private Paint TileBackground;
	private Paint TileBorder;
	
	
	
	private BattleGridListener listener;

	public BattleGrid(Context context, int columns, int rows) {
		super(context);
		mColumns = columns;
		mRows = rows;

		loadGraphics();
		initializeTiles();
	}

	private void loadGraphics() {
		HitTile   = BitmapFactory.decodeResource(getResources(), R.drawable.battleship_hit);
		MissTile  = BitmapFactory.decodeResource(getResources(), R.drawable.battleship_bullet);
		ShipTile  = BitmapFactory.decodeResource(getResources(), R.drawable.battleship_ship);
		TileBackground = new Paint();
		TileBackground.setColor( Color.WHITE );
		TileBorder = new Paint();
		TileBorder.setColor( Color.BLACK );
	}

	private void initializeTiles() {
		tiles = new TileType[mColumns][mRows];
		for (int column=0; column<mColumns; column++) {
			for (int row=0; row<mRows; row++) {
				tiles[column][row] = TileType.EMPTY;
			}
		}
	}

	public void setListener(BattleGridListener listener) {
		this.listener = listener;
	}

	public BattleGridListener getListener() {
		return listener;
	}
	
	public void setTileType(int column, int row, TileType type) {
		if (tiles[column][row] != type) {
			tiles[column][row] = type;
			invalidate();
		}
	}

	public TileType getTileType(int column, int row) {
		return tiles[column][row];
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
		mTileWidth  = (int) java.lang.Math.floor( getMeasuredWidth()  / mColumns  );
		mTileHeight = (int) java.lang.Math.floor( getMeasuredHeight() / mRows );
	}

	@Override
	public void onDraw(Canvas c) {
		
		c.drawPaint(TileBorder);
		//		c.drawPaint()

		for (int column=0; column<mColumns; column++) {
			for (int row=0; row<mRows; row++) {
				Rect r = getTileRect(column, row);
				c.drawRect(r, TileBackground);
				Bitmap tile = tileTypeToBitmap( tiles[column][row] );
				if (tile != null) {
					c.drawBitmap(tile, null, r, null);
				}
			}
		}
	}
	
	private Bitmap tileTypeToBitmap(TileType tt) {
		switch(tt) {
			case HIT:
				return HitTile;
			case MISS:
				return MissTile;
			case SHIP:
				return ShipTile;
			case EMPTY:
			default:
				return null;
		}
	}

	/**
	 * Gets the rectangle corresponding to tile (x,y) (zero-indexed)
	 * @param column
	 * @param row
	 * @return 
	 */
	private Rect getTileRect(int column, int row) {
		return new Rect( column * mTileWidth,
				row * mTileHeight,
				column * mTileWidth + mTileWidth - mTileBorder,
				row * mTileHeight + mTileHeight - mTileBorder);
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
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			Point tile = getTileWithPoint( (int)e.getX(), (int)e.getY() );
			if ( tile != null ) {
				if ( listener != null )
					listener.onTileHit( tile.x, tile.y );
				return true;
			}
		}
		return super.onTouchEvent(e);
	}
}
