package atlantis;

import atlantis.production.orders.ABuildOrder;
import atlantis.production.orders.ProtossBuildOrder;
import atlantis.production.orders.TerranBuildOrder;
import atlantis.production.orders.ZergBuildOrder;
import atlantis.units.AUnitType;
import bwapi.Race;

/**
 * This class is used to set up your Atlantis framework by providing some basic informations about your bot.
 * It's race-agnostic by definition.
 * <br /><br />
 * Feel free to analyze and adjust variables in this class.
 * <br /><br />Example code:
 * <p>
 * <b> useConfigForTerran() </b>
 * </p>
 * is used to load config for Terran.
 */
public class AtlantisConfig {

    // =========================================================    
    // Customizable variables

    /**
     * Game speed. Lower is faster. 0 is fastest, 20 is about normal game speed.
     * In game you can use buttons -/+ to change the game speed.
     */
    public static int GAME_SPEED = 0;
    
    /**
     * Race used by the Atlantis.
     */
    public static final String OUR_RACE = "Terran";
//    public static final String OUR_RACE = "Protoss";
//    public static final String OUR_RACE = "Zerg";

    /**
     * Single player enemy race.
     */
//    public static final String ENEMY_RACE = "Terran";
    public static final String ENEMY_RACE = "Protoss";
//    public static final String ENEMY_RACE = "Zerg";
    
    /**
     * Will modify bwapi.ini to use this map.
     */
//    public static final String MAP = "maps/sscai/(?)*.sc?";
    public static final String MAP = "Maps/BroodWar/umt/vultureDrive.scx";
    
    // =========================================================
    // === These are default values that can be overriden in ===
    // === specific build order file. See `build_orders` dir ===
    // =========================================================
    
    /**
     * If value less than 201 is passed, then you don't need to specify when to build supply buildings. They
     * will be <b>automatically built only if your total supply exceeds this value</b>.
     */
    public static int USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 201;

    /**
     * Force production of a worker whenever you have 50 minerals and more than N workers, but less than
     * AUTO_PRODUCE_WORKERS_MAX_WORKERS.
     */
    public static int AUTO_PRODUCE_WORKERS_SINCE_N_WORKERS = 201;

    /**
     * Maximum number of workers. This variable ONLY MAKES SENSE WHEN USED TOGETHER WITH 
     * <b>AUTO_PRODUCE_WORKERS_MAX_WORKERS</b>.
     */
    public static int AUTO_PRODUCE_WORKERS_MAX_WORKERS = 50;

    /**
     * We must reach at least N workers (SCVs, Probes) to scout for the enemy location.
     */
    public static int SCOUT_IS_NTH_WORKER = 8;

    // =========================================================
    // =========================================================
    // =========================================================
    // =========================================================
    // =========================================================
    // Do not customize - see methods "useConfigFor{Race}"
    
    public static Race MY_RACE = null;
    public static AUnitType BASE = null;
    public static AUnitType WORKER = null;
    public static AUnitType BARRACKS = null;
    public static AUnitType SUPPLY = null;
    public static AUnitType GAS_BUILDING = null;
    public static AUnitType DEF_BUILDING_ANTI_LAND = null;
    public static AUnitType DEFENSIVE_BUILDING_ANTI_AIR = null;
    public static ABuildOrder DEFAULT_BUILD_ORDER = null;

    // =========================================================
    
    /**
     * Helper method for using Terran race.
     */
    public static void useConfigForTerran() {
        AtlantisConfig.DEFAULT_BUILD_ORDER = TerranBuildOrder.TERRAN_1_Base_Vultures;
        
        AtlantisConfig.MY_RACE = Race.Terran;
        AtlantisConfig.BASE = AUnitType.Terran_Command_Center;
        AtlantisConfig.WORKER = AUnitType.Terran_SCV;
        AtlantisConfig.BARRACKS = AUnitType.Terran_Barracks;
        AtlantisConfig.SUPPLY = AUnitType.Terran_Supply_Depot;
        AtlantisConfig.GAS_BUILDING = AUnitType.Terran_Refinery;
        
        AtlantisConfig.DEF_BUILDING_ANTI_LAND = AUnitType.Terran_Bunker;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Terran_Missile_Turret;
    }

    /**
     * Helper method for using Protoss race.
     */
    public static void useConfigForProtoss() {
        AtlantisConfig.DEFAULT_BUILD_ORDER = ProtossBuildOrder.PROTOSS_2_GATEWAY_ZEALOT;
        
        AtlantisConfig.MY_RACE = Race.Protoss;
        AtlantisConfig.BASE = AUnitType.Protoss_Nexus;
        AtlantisConfig.WORKER = AUnitType.Protoss_Probe;
        AtlantisConfig.BARRACKS = AUnitType.Protoss_Gateway;
        AtlantisConfig.SUPPLY = AUnitType.Protoss_Pylon;
        AtlantisConfig.GAS_BUILDING = AUnitType.Protoss_Assimilator;
        
        AtlantisConfig.DEF_BUILDING_ANTI_LAND = AUnitType.Protoss_Photon_Cannon;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Protoss_Photon_Cannon;
    }
    
    /**
     * Helper method for using Zerg race.
     */
    public static void useConfigForZerg() {
        AtlantisConfig.DEFAULT_BUILD_ORDER = ZergBuildOrder.ZERG_13_POOL_MUTA;
        
        AtlantisConfig.MY_RACE = Race.Zerg;
        AtlantisConfig.BASE = AUnitType.Zerg_Hatchery;
        AtlantisConfig.WORKER = AUnitType.Zerg_Drone;
        AtlantisConfig.BARRACKS = AUnitType.Zerg_Spawning_Pool;
        AtlantisConfig.SUPPLY = AUnitType.Zerg_Overlord;
        AtlantisConfig.GAS_BUILDING = AUnitType.Zerg_Extractor;
        
        AtlantisConfig.DEF_BUILDING_ANTI_LAND = AUnitType.Zerg_Creep_Colony;
        AtlantisConfig.DEFENSIVE_BUILDING_ANTI_AIR = AUnitType.Zerg_Creep_Colony;
    }
    
    // =========================================================
    
    /**
     * This method could be used to overwrite user's race in bwapi.ini file.
     * <b>CURRENTLY NOT IMPLEMENTED</b>.
     */
    private static void overrideBwapiIniRace(String raceString) {
        System.out.println("@NotImplemented overrideBwapiIniRace");
        System.exit(-1);
//        overrideBwapiIniRace(raceString);
    }
    
    /**
     * Makes sure all necessary AtlantisConfig variables are set (non-null).
     */
    protected static void validate() {
        validate("MY_RACE", MY_RACE);
        validate("BASE", BASE);
        validate("WORKER", WORKER);
        validate("BARRACKS", BARRACKS);
        validate("SUPPLY", SUPPLY);
        validate("GAS_BUILDING", GAS_BUILDING);
        validate("You have to define default build order\n-> AtlantisConfig.DEFAULT_BUILD_ORDER", DEFAULT_BUILD_ORDER);
    }

    // =========================================================
    
    /**
     * Makes sure variable is non-null. If it's null, exits with a nice error.
     */
    private static void validate(String title, Object variable) {
        if (variable == null) {
            error(title);
        }
    }

    /**
     * Display error and exit Java.
     */
    private static void error(String title) {
        System.err.println("");
        System.err.println("#######################################");
        System.err.println("### ERROR IN ATLANTIS CONFIG ##########");
        System.err.println("#######################################");
        System.err.println("Please set variables for AtlantisConfig");
        System.err.println("before running your bot, read class doc");
        System.err.println("");
        System.err.println("### What went wrong ###################");
        System.err.println(title);
        System.err.println("");
        System.err.println("Program has stopped");
        System.exit(-1);
    }

}
