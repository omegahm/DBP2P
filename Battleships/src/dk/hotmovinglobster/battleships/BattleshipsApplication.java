package dk.hotmovinglobster.battleships;

import android.app.Application;
import android.util.Log;

public class BattleshipsApplication extends Application {

	public static final String LOG_TAG = "Battleships";
	public static final String EXTRA_END_WINNER = "EXTRA_END_WINNER";
	
    private static BattleshipsApplication singleton;
    public static BattleshipsApplication get(){return singleton;}
    
    private GameResources resources;
    public static GameResources resources() {return singleton.resources;}
    
    private GameContext gameContext;
    public static GameContext context() { return singleton.gameContext;}
    
    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
        resources = new GameResources( this );
        gameContext = new GameContext();
        Log.d(LOG_TAG, "BattleshipsApplication created");
        
    }
}
