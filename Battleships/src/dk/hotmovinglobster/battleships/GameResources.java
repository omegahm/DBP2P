package dk.hotmovinglobster.battleships;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class GameResources {
	
	public final Bitmap Explosion;
	public final Bitmap Hit;
	
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

		Explosion = getBitmap(R.drawable.explosion);
		Hit = getBitmap(R.drawable.explosion_blue);

		GenericSingleTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_single) ); 
		GenericBackTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_back) );
		GenericMiddleTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_middle) );
		GenericFrontTile = new RotatableBitmap( getBitmap(R.drawable.ship_generic_front) );
		
		ShipOne   = new Battleship("Single tile ship", GenericSingleTile, null, null, null, null);
		ShipTwo   = new Battleship("Double tile ship", GenericBackTile, GenericFrontTile, null, null, null);
		ShipThree = new Battleship("Triple tile ship", GenericBackTile, GenericMiddleTile, GenericFrontTile, null, null);
		ShipFour  = new Battleship("Quadruple tile ship", GenericBackTile, GenericMiddleTile, GenericMiddleTile, GenericFrontTile, null);
		ShipFive  = new Battleship("Quintuple tile ship", GenericBackTile, GenericMiddleTile, GenericMiddleTile, GenericMiddleTile, GenericFrontTile);
		
		Log.d(BattleshipsApplication.LOG_TAG, "GameResources: Shiplengths: " + ShipOne.getLength() + ", " + ShipTwo.getLength() + ", " + ShipThree.getLength() + ", " + ShipFour.getLength() + ", " + ShipFive.getLength());
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
