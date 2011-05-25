package dk.hotmovinglobster.battleships;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GameResources {
	
	public final Bitmap HitTile;
	public final Bitmap MissTile;
	public final Bitmap ShipTile;
	
	public final Bitmap Explosion;
	
	public final RotatableBitmap GenericSingleTile;
	public final RotatableBitmap GenericBackTile;
	public final RotatableBitmap GenericMiddleTile;
	public final RotatableBitmap GenericFrontTile;
	
	public final Bitmap GridBackground = null;
	
	private final Context mContext;
	private final Resources res;
	
	public final Battleship ShipOne;
	public final Battleship ShipTwo;
	public final Battleship ShipThree;
	public final Battleship ShipFour;
	public final Battleship ShipFive;
	
	public GameResources(Context context) {
		Log.v(BattleshipsApplication.LOG_TAG, "GameResources: constructor");
		mContext = context;
		res = mContext.getResources();

		HitTile   = getBitmap(R.drawable.battleship_hit);
		MissTile  = getBitmap(R.drawable.battleship_bullet);
		ShipTile  = getBitmap(R.drawable.battleship_ship);
		
		Explosion = getBitmap(R.drawable.explosion);

		GenericSingleTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_single) ); 
		GenericBackTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_back) );
		GenericMiddleTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_middle) );
		GenericFrontTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_front) );
		
		ShipOne   = new Battleship(GenericSingleTile, null, null, null, null);
		ShipTwo   = new Battleship(GenericBackTile, GenericFrontTile, null, null, null);
		ShipThree = new Battleship(GenericBackTile, GenericMiddleTile, GenericFrontTile, null, null);
		ShipFour  = new Battleship(GenericBackTile, GenericMiddleTile, GenericMiddleTile, GenericFrontTile, null);
		ShipFive  = new Battleship(GenericBackTile, GenericMiddleTile, GenericMiddleTile, GenericMiddleTile, GenericFrontTile);
	}
	
	public Battleship getBattleship(int length) {
		assert(length>0);
		assert(length<=5);
		switch(length) {
			case 1:
				return ShipOne;
			case 2:
				return ShipTwo;
			case 3:
				return ShipThree;
			case 4:
				return ShipFour;
			case 5:
				return ShipFive;
			default:
				return null;
		}
	}
	
	private Bitmap getBitmap(int resId) {
		return BitmapFactory.decodeResource(res, resId); //.copy(Bitmap.Config.ARGB_8888, true);
	}

}
