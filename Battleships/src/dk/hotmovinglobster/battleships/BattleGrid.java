package dk.hotmovinglobster.battleships;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
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

	private final Bitmap HitTile;
	private final Bitmap MissTile;
	private final Bitmap ShipTile;
	private final Bitmap EmptyTile;
	
	private final Paint TileBackground;
	private final Paint TileBorder;
	
	
	
	private BattleGridListener listener;

	public BattleGrid(Context context, int columns, int rows) {
		super(context);
		mColumns = columns;
		mRows = rows;

		HitTile   = BitmapFactory.decodeResource(getResources(), R.drawable.battleship_hit);
		MissTile  = BitmapFactory.decodeResource(getResources(), R.drawable.battleship_bullet);
		ShipTile  = BitmapFactory.decodeResource(getResources(), R.drawable.battleship_ship);
		EmptyTile = BitmapFactory.decodeResource(getResources(), R.drawable.tile_empty);
		TileBackground = new Paint();
		TileBackground.setColor( Color.WHITE );
		TileBorder = new Paint();
		TileBorder.setColor( Color.BLACK );
		
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
		tiles[column][row] = type;
		invalidate();
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
		Log.d(Settings.LOG_TAG, "onMeasure: wms: " + MeasureSpec.toString(widthMeasureSpec) + ", hms: " + MeasureSpec.toString(heightMeasureSpec) );
		Log.d(Settings.LOG_TAG, "onMeasure: maxSize: " + maxSize);
		Log.d(Settings.LOG_TAG, "onMeasure: w: " + getWidth() + ", h: " + getHeight());
	}

	private void recalculateSizes() {
		mTileWidth  = (int) java.lang.Math.floor( getMeasuredWidth()  / mColumns  );
		mTileHeight = (int) java.lang.Math.floor( getMeasuredHeight() / mRows );
	}

	@Override
	public void onDraw(Canvas c) {
		Log.d(Settings.LOG_TAG, "onDraw: c.width: "+c.getWidth()+", c.height: " + c.getHeight() );
		Log.d(Settings.LOG_TAG, "onDraw: mw: "+getMeasuredWidth()+", mh: " + getMeasuredHeight() );
		
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
			case EMPTY:
				return null;
			case HIT:
				return HitTile;
			case MISS:
				return MissTile;
			case SHIP:
				return ShipTile;
		}
		return null;
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
					listener.OnTileHit( tile.x, tile.y );
				return true;
			}
		}
		return super.onTouchEvent(e);
		// TODO Auto-generated method stub

	}
}