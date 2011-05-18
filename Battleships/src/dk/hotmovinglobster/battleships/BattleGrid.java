package dk.hotmovinglobster.battleships;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class BattleGrid extends View {

	/**
	 * Amount of horizontal tiles
	 */
	private int mWidth;
	/**
	 * Amount of vertical tiles
	 */
	private int mHeight;
	
	/**
	 * The width of each tile in pixels
	 */
	private int mTileWidth;
	/**
	 * The height of each tile in pixels
	 */
	private int mTileHeight;
	
	private final Bitmap EmptyTile;
	
	public BattleGrid(Context context, int width, int height) {
		super(context);
		mWidth = width;
		mHeight = height;
		
		EmptyTile = BitmapFactory.decodeResource(getResources(), R.drawable.tile_empty);
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
		mTileWidth  = (int) java.lang.Math.floor( getMeasuredWidth()  / mWidth  );
		mTileHeight = (int) java.lang.Math.floor( getMeasuredHeight() / mHeight );
	}
	
	@Override
	public void onDraw(Canvas c) {
		Log.d(Settings.LOG_TAG, "onDraw: c.width: "+c.getWidth()+", c.height: " + c.getHeight() );
		Log.d(Settings.LOG_TAG, "onDraw: mw: "+getMeasuredWidth()+", mh: " + getMeasuredHeight() );
		
//		c.drawPaint()
		
		for (int column=0; column<mHeight; column++) {
			for (int row=0; row<mWidth; row++) {
				c.drawBitmap(EmptyTile, null, getTileRect(column, row), null);
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
		return new Rect( column * mTileWidth,
				         row * mTileHeight,
				         column * mTileWidth + mTileWidth,
				         row * mTileHeight + mTileHeight);
	}
	
	private Point getTileWithPoint(int x, int y) {
		for (int column=0; column<mHeight; column++) {
			for (int row=0; row<mWidth; row++) {
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
			Toast.makeText(getContext(), "Hit tile ("+tile.x+","+tile.y+")", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onTouchEvent(e);
		// TODO Auto-generated method stub
		
	}
}
