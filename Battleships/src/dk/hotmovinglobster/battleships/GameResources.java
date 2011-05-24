package dk.hotmovinglobster.battleships;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class GameResources {
	
	public final Bitmap HitTile;
	public final Bitmap MissTile;
	public final Bitmap ShipTile;

	
	private final Context mContext;
	private final Resources res;
	
	private BitmapFactory.Options bfOptions;
	
	public GameResources(Context context) {
		Log.v(BattleshipsApplication.LOG_TAG, "GameResources: constructor");
		mContext = context;
		res = mContext.getResources();

		HitTile   = getBitmap(R.drawable.battleship_hit);
		MissTile  = getBitmap(R.drawable.battleship_bullet);
		ShipTile  = getBitmap(R.drawable.battleship_ship);

	}
	
	private Bitmap getBitmap(int resId) {
		return BitmapFactory.decodeResource(res, resId); //.copy(Bitmap.Config.ARGB_8888, true);
	}

}
