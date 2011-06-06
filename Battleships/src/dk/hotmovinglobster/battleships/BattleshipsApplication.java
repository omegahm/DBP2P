package dk.hotmovinglobster.battleships;

import java.util.UUID;

import android.app.Application;
import android.util.Log;

/**
 * Maintains global application state
 */
public class BattleshipsApplication extends Application {

	public static final String LOG_TAG = "Battleships";
	public static final String EXTRA_END_WINNER = "EXTRA_END_WINNER";
	
	public static final UUID BLUETOOTH_UUID = UUID.fromString( "fa87c0e0-afac-12de-8a39-a80f210c9a96" );
	
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
