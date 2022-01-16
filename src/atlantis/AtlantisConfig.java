package atlantis;

import atlantis.production.orders.ABuildOrder;
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
     * Disabling makes game so fast, you actually be like "Daaaaamn!".
     * Unfortunately it means nothing is rendered.
     */
    public static boolean DISABLE_GUI = false;

    /**`
     * Race used by the Atlantis.
     */
//    public static final String OUR_RACE = "Terran";
    public static final String OUR_RACE = "Protoss";
//    public static final String OUR_RACE = "Zerg";

    /**
     * Single player enemy race.
     */
//    public static final String ENEMY_RACE = "Terran";
//    public static final String ENEMY_RACE = "Protoss";
    public static final String ENEMY_RACE = "Zerg";
    
    /**
     * Will modify bwapi.ini to use this map.
     */
    public static final String MAP = UseMap.activeMapPath();

    // ==========================================================
    // === These are default values that can be overridden in ===
    // === specific build order file. See `build_orders` dir  ===
    // ==========================================================
    
    /**
     * If value less than 201 is passed, then you don't need to specify when to build supply buildings. They
     * will be <b>automatically built only if your total supply exceeds this value</b>.
     */
//    public static int AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 201;

    /**
     * Force production of a worker whenever you have minerals and more than N workers, but less than
     * AUTO_PRODUCE_WORKERS_MAX_WORKERS.
     */
//    public static int AUTO_PRODUCE_WORKERS_MIN_WORKERS = 1;

    /**
     * Maximum number of workers. This variable ONLY MAKES SENSE WHEN USED TOGETHER WITH 
     * <b>AUTO_PRODUCE_WORKERS_MAX_WORKERS</b>.
     */
//    public static int AUTO_PRODUCE_WORKERS_MAX_WORKERS = 50;

    /**
     * We must reach at least N workers (SCVs, Probes) to scout for the enemy location.
     */
//    public static int SCOUT_IS_NTH_WORKER = 8;

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
    public static AUnitType DEFENSIVE_BUILDING_ANTI_LAND = null;
    public static AUnitType DEFENSIVE_BUILDING_ANTI_AIR = null;
//    public static ABuildOrder DEFAULT_BUILD_ORDER = null;

    // =========================================================

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
//        validate("You have to define default build order\n-> AtlantisConfig.DEFAULT_BUILD_ORDER", DEFAULT_BUILD_ORDER);

        System.out.println("Atlantis config is valid.");
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
        System.err.println();
        System.err.println("#######################################");
        System.err.println("### ERROR IN ATLANTIS CONFIG ##########");
        System.err.println("#######################################");
        System.err.println("Please set variables for AtlantisConfig");
        System.err.println("before running your bot, read class doc");
        System.err.println();
        System.err.println("### What went wrong ###################");
        System.err.println(title);
        System.err.println();
        System.err.println("Program has stopped");
        System.exit(-1);
    }

}
