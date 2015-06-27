package jnibwapi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jnibwapi.types.*;
import jnibwapi.types.BulletType.BulletTypes;
import jnibwapi.types.DamageType.DamageTypes;
import jnibwapi.types.ExplosionType.ExplosionTypes;
import jnibwapi.types.OrderType.OrderTypes;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.TechType.TechTypes;
import jnibwapi.types.UnitCommandType.UnitCommandTypes;
import jnibwapi.types.UnitSizeType.UnitSizeTypes;
import jnibwapi.types.UnitType.UnitTypes;
import jnibwapi.types.UpgradeType.UpgradeTypes;
import jnibwapi.types.WeaponType.WeaponTypes;
import jnibwapi.util.BWColor;

/**
 * JNI interface for the Brood War API.<br>
 * 
 * This focus of this interface is to provide the callback and game state query functionality in
 * BWAPI.<br>
 * 
 * Note: for thread safety and game state sanity, all native calls should be invoked from the
 * callback methods.<br>
 * 
 * For BWAPI documentation see: {@link http://code.google.com/p/bwapi/}<br>
 * 
 * API Pages<br>
 * Game: {@link http://code.google.com/p/bwapi/wiki/Game}<br>
 * Unit: {@link http://code.google.com/p/bwapi/wiki/Unit}<br>
 */
public class JNIBWAPI {
	
	// load the BWAPI client library
	static {
		try {
			System.loadLibrary("client-bridge-" + System.getProperty("os.arch"));
			System.out.println("Loaded client bridge library.");
		} catch (UnsatisfiedLinkError e) {
			// Help beginners put the DLL in the correct place (although anywhere on the path will
			// work)
			File dll = new File("client-bridge-" + System.getProperty("os.arch") + ".dll");
			if (!dll.exists()) {
				System.err.println("Native code library not found: " + dll.getAbsolutePath());
			}
			System.err.println("Native code library failed to load." + e);
		}
	}
	
	private static JNIBWAPI instance = null;
	
	/**
	 * Get a reference to the JNIBWAPI object. Note it will be unusable until the
	 * {@link #connected()} callback, and all game-related fields may be undefined until the
	 * {@link #gameStarted()} callback.
	 */
	public static JNIBWAPI getInstance() {
		return instance;
	}
	
	/** callback listener for BWAPI events */
	private BWAPIEventListener listener;
	/** whether to use BWTA for map analysis */
	private boolean enableBWTA;
	private Charset charset;
	
	/**
	 * Instantiates a BWAPI instance, but does not connect to the bridge. To connect, the start
	 * method must be invoked.
	 * 
	 * @param listener - listener for BWAPI callback events.
	 * @param enableBWTA - whether to use BWTA for map analysis
	 */
	public JNIBWAPI(BWAPIEventListener listener, boolean enableBWTA) {
		instance = this;
		this.listener = listener;
		this.enableBWTA = enableBWTA;
		try {
			// Using the Korean character set for decoding byte[]s into Strings will allow Korean
			// characters to be parsed correctly.
			charset = Charset.forName("Cp949");
		} catch (UnsupportedCharsetException e) {
			System.out.println(
					"Korean character set not available. Some characters may not be read properly");
			charset = StandardCharsets.ISO_8859_1;
		}
	}
	
	/**
	 * Invokes the native library which will connect to the bridge and then invoke callback
	 * functions.
	 * 
	 * Note: this method never returns, it should be invoked from a separate thread if concurrent
	 * java processing is needed.
	 */
	public void start() {
		startClient(this);
	}
	
	// game state
	private int gameFrame = 0;
	private Map map;
	private HashMap<Integer, Unit> units = new HashMap<>();
	private ArrayList<Unit> playerUnits = new ArrayList<>();
	private ArrayList<Unit> alliedUnits = new ArrayList<>();
	private ArrayList<Unit> enemyUnits = new ArrayList<>();
	private ArrayList<Unit> neutralUnits = new ArrayList<>();
	private ArrayList<Unit> staticNeutralUnits = new ArrayList<>();
	
	// player lists
	private Player self;
	private Player neutralPlayer;
	private HashMap<Integer, Player> players = new HashMap<>();
	private HashSet<Player> allies = new HashSet<>();
	private HashSet<Player> enemies = new HashSet<>();
	
	// invokes the main native method
	private native void startClient(JNIBWAPI jniBWAPI);
	
	// query methods
	private native int getFrame();
	public native int getReplayFrameTotal();
	private native int[] getPlayersData();
	private native int[] getPlayerUpdate(int playerID);
	/** Returns string as a byte[] to properly handle ASCII-extended characters */
	private native byte[] getPlayerName(int playerID);
	private native int[] getResearchStatus(int playerID);
	private native int[] getUpgradeStatus(int playerID);
	private native int[] getAllUnitsData();
	private native int[] getStaticNeutralUnitsData();
	private native int[] getRaceTypes();
	private native String getRaceTypeName(int raceID);
	private native int[] getUnitTypes();
	private native String getUnitTypeName(int unitTypeID);
	private native int[] getRequiredUnits(int unitTypeID);
	private native int[] getTechTypes();
	private native String getTechTypeName(int techID);
	private native int[] getUpgradeTypes();
	private native String getUpgradeTypeName(int upgradeID);
	private native int[] getWeaponTypes();
	private native String getWeaponTypeName(int weaponID);
	private native int[] getUnitSizeTypes();
	private native String getUnitSizeTypeName(int sizeID);
	private native int[] getBulletTypes();
	private native String getBulletTypeName(int bulletID);
	private native int[] getDamageTypes();
	private native String getDamageTypeName(int damageID);
	private native int[] getExplosionTypes();
	private native String getExplosionTypeName(int explosionID);
	private native int[] getUnitCommandTypes();
	private native String getUnitCommandTypeName(int unitCommandID);
	private native int[] getOrderTypes();
	private native String getOrderTypeName(int unitCommandID);
	private native int[] getUnitIdsOnTile(int tx, int ty);
	
	// map data
	private native void analyzeTerrain();
	private native int getMapWidth();
	private native int getMapHeight();
	/** Returns string as a byte[] to properly handle ASCII-extended characters */
	private native byte[] getMapName();
	private native String getMapFileName();
	private native String getMapHash();
	private native int[] getHeightData();
	/** Returns the regionId for each map tile */
	private native int[] getRegionMap();
	private native int[] getWalkableData();
	private native int[] getBuildableData();
	private native int[] getChokePoints();
	private native int[] getRegions();
	private native int[] getPolygon(int regionID);
	private native int[] getBaseLocations();
	
	// Unit commands. These should generally be accessed via the Unit class now.
	private native boolean canIssueCommand(int unitID, int unitCommandTypeID, int targetUnitID, int x, int y, int extra);
	public boolean canIssueCommand(UnitCommand cmd) {
		return canIssueCommand(cmd.getUnit().getID(), cmd.getType().getID(), cmd.getTargetUnitID(),
				cmd.getX(), cmd.getY(), cmd.getExtra());
	}
	private native boolean issueCommand(int unitID, int unitCommandTypeID, int targetUnitID, int x, int y, int extra);
	public boolean issueCommand(UnitCommand cmd) {
		return issueCommand(cmd.getUnit().getID(), cmd.getType().getID(), cmd.getTargetUnitID(),
				cmd.getX(), cmd.getY(), cmd.getExtra());
	}
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean attack(int unitID, int x, int y);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean attack(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean build(int unitID, int tx, int ty, int typeID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean buildAddon(int unitID, int typeID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean train(int unitID, int typeID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean morph(int unitID, int typeID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean research(int unitID, int techID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean upgrade(int unitID, int updateID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean setRallyPoint(int unitID, int x, int y);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean setRallyPoint(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean move(int unitID, int x, int y);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean patrol(int unitID, int x, int y);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean holdPosition(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean stop(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean follow(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean gather(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean returnCargo(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean repair(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean burrow(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean unburrow(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean cloak(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean decloak(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean siege(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean unsiege(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean lift(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean land(int unitID, int tx, int ty);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean load(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean unload(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean unloadAll(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean unloadAll(int unitID, int x, int y);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean rightClick(int unitID, int x, int y);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean rightClick(int unitID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean haltConstruction(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean cancelConstruction(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean cancelAddon(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean cancelTrain(int unitID, int slot);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean cancelMorph(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean cancelResearch(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean cancelUpgrade(int unitID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean useTech(int unitID, int typeID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean useTech(int unitID, int typeID, int x, int y);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean useTech(int unitID, int typeID, int targetID);
	/** @deprecated Use the one in {@link Unit} instead */
	public native boolean placeCOP(int unitID, int tx, int ty);

	// utility commands
	public native void drawHealth(boolean enable);
	public native void drawTargets(boolean enable);
	public native void drawIDs(boolean enable);
	public native void enableUserInput();
	public native void enablePerfectInformation();
	public native void setGameSpeed(int speed);
	public native void setFrameSkip(int frameSkip);
	public native void leaveGame();
	
	// draw commands (if screenCoords is false, draws at map pixel coordinates)
	private native void drawBox(int left, int top, int right, int bottom, int color, boolean fill, boolean screenCoords);
	public void drawBox(Position topLeft, Position bottomRight, BWColor bWColor, boolean fill, boolean screenCoords) {
		drawBox(topLeft.getPX(), topLeft.getPY(), bottomRight.getPX(), bottomRight.getPY(), bWColor.getID(), fill, screenCoords);
	}
	private native void drawCircle(int x, int y, int radius, int color, boolean fill, boolean screenCoords);
	public void drawCircle(Position p, int radius, BWColor bWColor, boolean fill, boolean screenCoords) {
		drawCircle(p.getPX(), p.getPY(), radius, bWColor.getID(), fill, screenCoords);
	}
	private native void drawLine(int x1, int y1, int x2, int y2, int color, boolean screenCoords);
	public void drawLine(Position start, Position end, BWColor bWColor, boolean screenCoords) {
		drawLine(start.getPX(), start.getPY(), end.getPX(), end.getPY(), bWColor.getID(), screenCoords);
	}
	private native void drawDot(int x, int y, int color, boolean screenCoords);
	public void drawDot(Position p, BWColor bWColor, boolean screenCoords) {
		drawDot(p.getPX(), p.getPY(), bWColor.getID(), screenCoords);
	}
	private native void drawText(int x, int y, String msg, boolean screenCoords);
	public void drawText(Position a, String msg, boolean screenCoords) {
		drawText(a.getPX(), a.getPY(), msg, screenCoords);
	}
	
	// Extended Commands
	private native boolean isVisible(int tileX, int tileY);
	public boolean isVisible(Position p) {
		return isVisible(p.getBX(), p.getBY());
	}
	private native boolean isExplored(int tileX, int tileY);
	public boolean isExplored(Position p) {
		return isExplored(p.getBX(), p.getBY());
	}
	private native boolean isBuildable(int tx, int ty, boolean includeBuildings);
	public boolean isBuildable(Position p, boolean includeBuildings) {
		return isBuildable(p.getBX(), p.getBY(), includeBuildings);
	}
	private native boolean hasCreep(int tileX, int tileY);
	public boolean hasCreep(Position p) {
		return hasCreep(p.getBX(), p.getBY());
	}
	private native boolean hasPower(int tileX, int tileY, int unitTypeID);
	public boolean hasPower(Position p) {
		return hasPower(p, UnitTypes.None);
	}
	public boolean hasPower(Position p, UnitType ut) {
		return hasPower(p.getBX(), p.getBY(), ut.getID());
	}
	private native boolean hasPower(int tileX, int tileY, int tileWidth, int tileHeight, int unitTypeID);
	public boolean hasPower(Position p, int tileWidth, int tileHeight){
		return hasPower(p, tileWidth, tileHeight, UnitTypes.None);
	}
	public boolean hasPower(Position p, int tileWidth, int tileHeight, UnitType ut) {
		return hasPower(p.getBX(), p.getBY(), tileWidth, tileHeight, ut.getID());
	}
	private native boolean hasPowerPrecise(int x, int y, int unitTypeID);
	public boolean hasPowerPrecise(Position p) {
		return hasPowerPrecise(p, UnitTypes.None);
	}
	public boolean hasPowerPrecise(Position p, UnitType ut) {
		return hasPowerPrecise(p.getPX(), p.getPY(), ut.getID());
	}
	private native boolean hasPath(int fromX, int fromY, int toX, int toY);
	public boolean hasPath(Position from, Position to) {
		return hasPath(from.getPX(), from.getPY(), to.getPX(), to.getPY());
	}
	private native boolean hasPath(int unitID, int targetID);
	public boolean hasPath(Unit u, Unit target) {
		return hasPath(u.getID(), target.getID());
	}
	private native boolean hasPath(int unitID, int toX, int toY);
	public boolean hasPath(Unit u, Position to) {
		return hasPath(u.getID(), to.getPX(), to.getPY());
	}
	protected native int[] getLoadedUnits(int unitID);
	protected native int[] getInterceptors(int unitID);
	protected native int[] getLarva(int unitID);
	private native boolean canBuildHere(int tileX, int tileY, int unitTypeID, boolean checkExplored);
	public boolean canBuildHere(Position p, UnitType ut, boolean checkExplored) {
		return canBuildHere(p.getBX(), p.getBY(), ut.getID(), checkExplored);
	}
	private native boolean canBuildHere(int unitID, int tileX, int tileY, int unitTypeID, boolean checkExplored);
	public boolean canBuildHere(Unit u, Position p, UnitType ut, boolean checkExplored) {
		return canBuildHere(u == null ? -1 : u.getID(), p.getBX(), p.getBY(), ut.getID(), checkExplored);
	}
	private native boolean canMake(int unitTypeID);
	public boolean canMake(UnitType ut) {
		return canMake(ut.getID());
	}
	private native boolean canMake(int unitID, int unitTypeID);
	public boolean canMake(Unit u, UnitType ut) {
		return canMake(u.getID(), ut.getID());
	}
	private native boolean canResearch(int techTypeID);
	public boolean canResearch(TechType tt) {
		return canResearch(tt.getID());
	}
	private native boolean canResearch(int unitID, int techTypeID);
	public boolean canResearch(Unit u, TechType tt) {
		return canResearch(u.getID(), tt.getID());
	}
	private native boolean canUpgrade(int upgradeTypeID);
	public boolean canUpgrade(UpgradeType ut) {
		return canUpgrade(ut.getID());
	}
	private native boolean canUpgrade(int unitID, int upgradeTypeID);
	public boolean canUpgrade(Unit u, UpgradeType ut) {
		return canUpgrade(u.getID(), ut.getID());
	}
	
	
	public native void printText(String message);
	public native void sendText(String message);
	public native void setLatCom(boolean enabled);
	public native void setCommandOptimizationLevel(int level);
	public native boolean isReplay();
	private native boolean isVisibleToPlayer(int unitID, int playerID);
	public boolean isVisibleToPlayer(Unit u, Player p) {
		return isVisibleToPlayer(u.getID(), p.getID());
	}
	public native int getLastError();
	public native int getRemainingLatencyFrames();

	// Old get___ methods for ID dereferencing no longer needed (call method body directly).
	@Deprecated
	public UnitType getUnitType(int typeID) { return UnitTypes.getUnitType(typeID); }
	@Deprecated
	public RaceType getRaceType(int typeID) { return RaceTypes.getRaceType(typeID); }
	@Deprecated
	public TechType getTechType(int typeID) { return TechTypes.getTechType(typeID); }
	@Deprecated
	public UpgradeType getUpgradeType(int upgradeID) { return UpgradeTypes.getUpgradeType(upgradeID); }
	@Deprecated
	public WeaponType getWeaponType(int weaponID) { return WeaponTypes.getWeaponType(weaponID); }
	@Deprecated
	public UnitSizeType getUnitSizeType(int sizeID) { return UnitSizeTypes.getUnitSizeType(sizeID); }
	@Deprecated
	public BulletType getBulletType(int bulletID) { return BulletTypes.getBulletType(bulletID); }
	@Deprecated
	public DamageType getDamageType(int damageID) { return DamageTypes.getDamageType(damageID); }
	@Deprecated
	public ExplosionType getExplosionType(int explosionID) { return ExplosionTypes.getExplosionType(explosionID); }
	@Deprecated
	public UnitCommandType getUnitCommandType(int unitCommandID) { return UnitCommandTypes.getUnitCommandType(unitCommandID); }
	@Deprecated
	public OrderType getOrderType(int orderID) { return OrderTypes.getOrderType(orderID); }
	
	// Old ___Types() methods no longer needed (call method body directly).
	@Deprecated
	public Collection<UnitType> unitTypes() { return UnitTypes.getAllUnitTypes(); }
	@Deprecated
	public Collection<RaceType> raceTypes() { return RaceTypes.getAllRaceTypes(); }
	@Deprecated
	public Collection<TechType> techTypes() { return TechTypes.getAllTechTypes(); }
	@Deprecated
	public Collection<UpgradeType> upgradeTypes() { return UpgradeTypes.getAllUpgradeTypes(); }
	@Deprecated
	public Collection<WeaponType> weaponTypes() { return WeaponTypes.getAllWeaponTypes(); }
	@Deprecated
	public Collection<UnitSizeType> unitSizeTypes() { return UnitSizeTypes.getAllUnitSizeTypes(); }
	@Deprecated
	public Collection<BulletType> bulletTypes() { return BulletTypes.getAllBulletTypes(); }
	@Deprecated
	public Collection<DamageType> damageTypes() { return DamageTypes.getAllDamageTypes(); }
	@Deprecated
	public Collection<ExplosionType> explosionTypes() { return ExplosionTypes.getAllExplosionTypes(); }
	@Deprecated
	public Collection<UnitCommandType> unitCommandTypes() { return UnitCommandTypes.getAllUnitCommandTypes(); }
	@Deprecated
	public Collection<OrderType> orderTypes() { return OrderTypes.getAllOrderTypes(); }
	
	// ID Lookup Methods (should not usually be needed)
	public Player getPlayer(int playerID) { return players.get(playerID); }
	public Unit getUnit(int unitID) { return units.get(unitID); }

	// game state accessors
	public int getFrameCount() { return gameFrame; }
	public Player getSelf() { return self; }
	public Player getNeutralPlayer() { return neutralPlayer; }
	public Collection<Player> getPlayers() { return Collections.unmodifiableCollection(players.values()); }
	public Set<Player> getAllies() { return Collections.unmodifiableSet(allies); }
	public Set<Player> getEnemies() { return Collections.unmodifiableSet(enemies); }
	public Collection<Unit> getAllUnits() { return Collections.unmodifiableCollection(units.values()); }
	public List<Unit> getMyUnits() { return Collections.unmodifiableList(playerUnits); }
	public List<Unit> getAlliedUnits() { return Collections.unmodifiableList(alliedUnits); }
	public List<Unit> getEnemyUnits() { return Collections.unmodifiableList(enemyUnits); }
	public List<Unit> getNeutralUnits() { return Collections.unmodifiableList(neutralUnits); }
	public List<Unit> getStaticNeutralUnits() { return Collections.unmodifiableList(staticNeutralUnits); }
	
	public List<Unit> getUnits(Player p) {
		List<Unit> pUnits = new ArrayList<Unit>();
		for (Unit u : units.values()) {
			if (u.getPlayer() == p) {
				pUnits.add(u);
			}
		}
		return pUnits;
	}
	
	public List<Unit> getUnitsOnTile(Position p) {
		// Often will have 0 or few units on tile
		List<Unit> units = new ArrayList<Unit>(0);
		for (int id : getUnitIdsOnTile(p.getBX(), p.getBY())) {
			units.add(getUnit(id));
		}
		return units;
	}
	
	/**
	 * Returns the map.
	 */
	public Map getMap() {
		return map;
	}
	
	/**
	 * Loads type data from BWAPI.
	 */
	private void loadTypeData() {
		// race types
		int[] raceTypeData = getRaceTypes();
		for (int index = 0; index < raceTypeData.length; index += RaceType.numAttributes) {
			int id = raceTypeData[index];
			RaceTypes.getRaceType(id).initialize(raceTypeData, index, getRaceTypeName(id));
		}
		
		// unit types
		int[] unitTypeData = getUnitTypes();
		for (int index = 0; index < unitTypeData.length; index += UnitType.numAttributes) {
			int id = unitTypeData[index];
			UnitTypes.getUnitType(id).initialize(unitTypeData, index, getUnitTypeName(id),
					getRequiredUnits(id));
		}
		
		// tech types
		int[] techTypeData = getTechTypes();
		for (int index = 0; index < techTypeData.length; index += TechType.numAttributes) {
			int id = techTypeData[index];
			TechTypes.getTechType(id).initialize(techTypeData, index, getTechTypeName(id));
		}
		
		// upgrade types
		int[] upgradeTypeData = getUpgradeTypes();
		for (int index = 0; index < upgradeTypeData.length; index += UpgradeType.numAttributes) {
			int id = upgradeTypeData[index];
			UpgradeTypes.getUpgradeType(id).initialize(upgradeTypeData, index, getUpgradeTypeName(id));
		}
		
		// weapon types
		int[] weaponTypeData = getWeaponTypes();
		for (int index = 0; index < weaponTypeData.length; index += WeaponType.numAttributes) {
			int id = weaponTypeData[index];
			WeaponTypes.getWeaponType(id).initialize(weaponTypeData, index, getWeaponTypeName(id));
		}
		
		// unit size types
		int[] unitSizeTypeData = getUnitSizeTypes();
		for (int index = 0; index < unitSizeTypeData.length; index += UnitSizeType.numAttributes) {
			int id = unitSizeTypeData[index];
			UnitSizeTypes.getUnitSizeType(id).initialize(unitSizeTypeData, index, getUnitSizeTypeName(id));
		}
		
		// bullet types
		int[] bulletTypeData = getBulletTypes();
		for (int index = 0; index < bulletTypeData.length; index += BulletType.numAttributes) {
			int id = bulletTypeData[index];
			BulletTypes.getBulletType(id).initialize(bulletTypeData, index, getBulletTypeName(id));
		}
		
		// damage types
		int[] damageTypeData = getDamageTypes();
		for (int index = 0; index < damageTypeData.length; index += DamageType.numAttributes) {
			int id = damageTypeData[index];
			DamageTypes.getDamageType(id).initialize(damageTypeData, index, getDamageTypeName(id));
		}
		
		// explosion types
		int[] explosionTypeData = getExplosionTypes();
		for (int index = 0; index < explosionTypeData.length; index += ExplosionType.numAttributes) {
			int id = explosionTypeData[index];
			ExplosionTypes.getExplosionType(id).initialize(explosionTypeData, index, getExplosionTypeName(id));
		}
		
		// unitCommand types
		int[] unitCommandTypeData = getUnitCommandTypes();
		for (int index = 0; index < unitCommandTypeData.length; index += UnitCommandType.numAttributes) {
			int id = unitCommandTypeData[index];
			UnitCommandTypes.getUnitCommandType(id).initialize(unitCommandTypeData, index, getUnitCommandTypeName(id));
		}
		
		// order types
		int[] orderTypeData = getOrderTypes();
		for (int index = 0; index < orderTypeData.length; index += OrderType.numAttributes) {
			int id = orderTypeData[index];
			OrderTypes.getOrderType(id).initialize(orderTypeData, index, getOrderTypeName(id));
		}
		
		// event types - no extra data to load
	}
	
	/**
	 * Loads map data and (if enableBWTA is true) BWTA data.
	 * 
	 * TODO: figure out how to use BWTA's internal map storage
	 */
	private void loadMapData() {
		String mapName = new String(getMapName(), charset);
		map = new Map(getMapWidth(), getMapHeight(), mapName, getMapFileName(), getMapHash(),
				getHeightData(), getBuildableData(), getWalkableData());
		if (!enableBWTA) {
			return;
		}
		
		// get region and choke point data
		File bwtaFile = new File("mapData" + File.separator + map.getHash() + ".jbwta");
		String mapHash = map.getHash();
		File mapDir = bwtaFile.getParentFile();
		if (mapDir != null) {
			mapDir.mkdirs();
		}
		boolean analyzed = bwtaFile.exists();
		int[] regionMapData = null;
		int[] regionData = null;
		int[] chokePointData = null;
		int[] baseLocationData = null;
		HashMap<Integer, int[]> polygons = new HashMap<>();
		
		// run BWTA
		if (!analyzed) {
			analyzeTerrain();
			regionMapData = getRegionMap();
			regionData = getRegions();
			chokePointData = getChokePoints();
			baseLocationData = getBaseLocations();
			for (int index = 0; index < regionData.length; index += Region.numAttributes) {
				int id = regionData[index];
				polygons.put(id, getPolygon(id));
			}
			
			// sometimes BWTA seems to crash on analyse. Make sure we are definitely in the same map
			if (!mapHash.equals(map.getHash())) {
				System.err.println("Error: Map changed during analysis! BWTA file not saved.");
				System.exit(1);
			}
			
			// store the results to a local file (bwta directory)
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(bwtaFile));
				
				writeMapData(writer, regionMapData);
				writeMapData(writer, regionData);
				writeMapData(writer, chokePointData);
				writeMapData(writer, baseLocationData);
				for (int id : polygons.keySet()) {
					writer.write("" + id + ",");
					writeMapData(writer, polygons.get(id));
				}
				
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// load from file
		else {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(bwtaFile));
				
				regionMapData = readMapData(reader);
				regionData = readMapData(reader);
				chokePointData = readMapData(reader);
				baseLocationData = readMapData(reader);
				// polygons (first integer is ID)
				int[] polygonData;
				while ((polygonData = readMapData(reader)) != null) {
					int[] coordinateData = Arrays.copyOfRange(polygonData, 1, polygonData.length);
					
					polygons.put(polygonData[0], coordinateData);
				}
				
				reader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		map.initialize(regionMapData, regionData, polygons, chokePointData, baseLocationData);
	}
	
	/** Convenience method to write out each part of BWTA map data to a stream */
	private static void writeMapData(BufferedWriter writer, int[] data) throws IOException {
		boolean first = true;
		for (int val : data) {
			if (first) {
				first = false;
				writer.write("" + val);
			}
			else {
				writer.write("," + val);
			}
		}
		writer.write("\n");
	}
	
	/**
	 * Convenience method to read each part of BWTA map data from a stream
	 * 
	 * @return null when end of stream is reached, otherwise an int array (possibly empty)
	 */
	private static int[] readMapData(BufferedReader reader) throws IOException {
		int[] data = new int[0];
		String line = reader.readLine();
		if (line == null)
			return null;
		String[] stringData = line.split(",");
		if (stringData.length > 0 && !stringData[0].equals("")) {
			data = new int[stringData.length];
			for (int i = 0; i < stringData.length; i++) {
				data[i] = Integer.parseInt(stringData[i]);
			}
		}
		return data;
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Utility function for printing to the java console from C++.
	 */
	private void javaPrint(String msg) {
		try {
			System.out.println("Bridge: " + msg);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the client and event listener that a connection has been formed to the bridge.
	 */
	private void connected() {
		try {
			loadTypeData();
			listener.connected();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the client that a game has started. Not passed on to the event listener.<br>
	 * 
	 * Note: this is always called before the matchStarted event, and is meant as a way of notifying
	 * the AI client to clear up state.
	 */
	private void gameStarted() {
		try {
			// get the players
			self = null;
			allies.clear();
			enemies.clear();
			players.clear();
			
			int[] playerData = getPlayersData();
			for (int index = 0; index < playerData.length; index += Player.numAttributes) {
				String name = new String(getPlayerName(playerData[index]), charset);
				Player player = new Player(playerData, index, name);
				
				players.put(player.getID(), player);
				
				if (player.isSelf()) {
					self = player;
				}
				else if (player.isAlly()) {
					allies.add(player);
				}
				else if (player.isEnemy()) {
					enemies.add(player);
				}
				else if (player.isNeutral()) {
					neutralPlayer = player;
				}
			}
			
			// get unit data
			units.clear();
			playerUnits.clear();
			alliedUnits.clear();
			enemyUnits.clear();
			neutralUnits.clear();
			int[] unitData = getAllUnitsData();
			
			for (int index = 0; index < unitData.length; index += Unit.numAttributes) {
				int id = unitData[index];
				Unit unit = new Unit(id, this);
				unit.update(unitData, index);
				
				units.put(id, unit);
				if (self != null && unit.getPlayer() == self) {
					playerUnits.add(unit);
				}
				else if (allies.contains(unit.getPlayer())) {
					alliedUnits.add(unit);
				}
				else if (enemies.contains(unit.getPlayer())) {
					enemyUnits.add(unit);
				}
				else {
					neutralUnits.add(unit);
				}
			}
			staticNeutralUnits.clear();
			unitData = getStaticNeutralUnitsData();
			for (int index = 0; index < unitData.length; index += Unit.numAttributes) {
				int id = unitData[index];
				
				// Ensure we don't have duplicate units
				Unit unit = units.get(id);
				if (unit == null) {
					unit = new Unit(id, this);
					unit.update(unitData, index);
				}
				
				staticNeutralUnits.add(unit);
			}
			
			gameFrame = getFrame();
			loadMapData();
			
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the client that game data has been updated. Not passed on to the event listener.<br>
	 * 
	 * Note: this is always called before the events each frame, and is meant as a way of notifying
	 * the AI client to update state.
	 */
	private void gameUpdate() {
		try {
			// update game state
			gameFrame = getFrame();
			if (!isReplay()) {
				self.update(getPlayerUpdate(self.getID()));
				self.updateResearch(getResearchStatus(self.getID()), getUpgradeStatus(self.getID()));
			} else {
				for (Integer playerID : players.keySet()) {
					players.get(playerID).update(getPlayerUpdate(playerID));
					players.get(playerID).updateResearch(getResearchStatus(playerID),
							getUpgradeStatus(playerID));
				}
			}
			// update units
			int[] unitData = getAllUnitsData();
			HashSet<Integer> deadUnits = new HashSet<>(units.keySet());
			ArrayList<Unit> playerList = new ArrayList<>();
			ArrayList<Unit> alliedList = new ArrayList<>();
			ArrayList<Unit> enemyList = new ArrayList<>();
			ArrayList<Unit> neutralList = new ArrayList<>();
			
			for (int index = 0; index < unitData.length; index += Unit.numAttributes) {
				int id = unitData[index];
				
				deadUnits.remove(id);
				
				Unit unit = units.get(id);
				if (unit == null) {
					unit = new Unit(id, this);
					units.put(id, unit);
				}
				
				unit.update(unitData, index);
				
				if (self != null)
				{
					if (unit.getPlayer() == self) {
						playerList.add(unit);
					}
					else if (allies.contains(unit.getPlayer())) {
						alliedList.add(unit);
					}
					else if (enemies.contains(unit.getPlayer())) {
						enemyList.add(unit);
					}
					else {
						neutralList.add(unit);
					}
				}
				else if (allies.contains(unit.getPlayer())) {
					alliedList.add(unit);
				}
				else if (enemies.contains(unit.getPlayer())) {
					enemyList.add(unit);
				}
				else {
					neutralList.add(unit);
				}
			}
			
			// update the unit lists
			playerUnits = playerList;
			alliedUnits = alliedList;
			enemyUnits = enemyList;
			neutralUnits = neutralList;
			for (Integer unitID : deadUnits) {
				units.get(unitID).setDestroyed();
				units.remove(unitID);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the event listener that the game has terminated.<br>
	 * 
	 * Note: this is always called after the matchEnded event, and is meant as a way of notifying
	 * the AI client to clear up state.
	 */
	private void gameEnded() {}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Sends BWAPI callback events to the event listener.
	 */
	private void eventOccurred(int eventTypeID, int param1, int param2, String param3) {
		try {
			EventType event = EventType.getEventType(eventTypeID);
			switch (event) {
				case MatchStart:
					listener.matchStart();
					break;
				case MatchEnd:
					listener.matchEnd(param1 == 1);
					break;
				case MatchFrame:
					listener.matchFrame();
					break;
				case MenuFrame:
					// Unused?
					break;
				case SendText:
					listener.sendText(param3);
					break;
				case ReceiveText:
					listener.receiveText(param3);
					break;
				case PlayerLeft:
					listener.playerLeft(param1);
					break;
				case NukeDetect:
					if (param1 == -1)
						listener.nukeDetect();
					else
						listener.nukeDetect(new Position(param1, param2));
					break;
				case UnitDiscover:
					listener.unitDiscover(param1);
					break;
				case UnitEvade:
					listener.unitEvade(param1);
					break;
				case UnitShow:
					listener.unitShow(param1);
					break;
				case UnitHide:
					listener.unitHide(param1);
					break;
				case UnitCreate:
					listener.unitCreate(param1);
					break;
				case UnitDestroy:
					listener.unitDestroy(param1);
					break;
				case UnitMorph:
					listener.unitMorph(param1);
					break;
				case UnitRenegade:
					listener.unitRenegade(param1);
					break;
				case SaveGame:
					listener.saveGame(param3);
					break;
				case UnitComplete:
					listener.unitComplete(param1);
					break;
				case PlayerDropped:
					listener.playerDropped(param1);
					break;
				case None:
					// Unused?
					break;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * C++ callback function.<br>
	 * 
	 * Notifies the event listener that a key was pressed.
	 */
	public void keyPressed(int keyCode) {
		try {
			listener.keyPressed(keyCode);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
