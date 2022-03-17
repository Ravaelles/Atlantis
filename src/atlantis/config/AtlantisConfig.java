package atlantis.config;

import atlantis.units.AUnitType;
import bwapi.Race;

/**
 * This class is used to set up your Atlantis framework by providing basic information about your bot and bwapi.ini.
 *
 * Notice that setting OUR_RACE, ENEMY_RACE and MAP will automatically override respective content in bwapi.ini file,
 * to save you from manually having to update these.
 */
public class AtlantisConfig {

    // =========================================================    
    // Customizable variables

    /**
     * Disabling makes game so fast, you actually be like "Daaaaamn!".
     * Unfortunately it means nothing gets rendered, so game appears to be frozen.
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
    public static final String ENEMY_RACE = "Protoss";
//    public static final String ENEMY_RACE = "Zerg";
    
    /**
     * Will modify bwapi.ini to use this map.
     */
    public static final String MAP = UseMap.activeMapPath();

    // =========================================================
    // =========================================================
    // =========================================================
    // =========================================================
    // =========================================================
    // Do not change manually - see AtlantisConfigChanger::useConfigFor
    
    public static Race MY_RACE = null;
    public static AUnitType BASE = null;
    public static AUnitType WORKER = null;
    public static AUnitType BARRACKS = null;
    public static AUnitType SUPPLY = null;
    public static AUnitType GAS_BUILDING = null;
    public static AUnitType DEFENSIVE_BUILDING_ANTI_LAND = null;
    public static AUnitType DEFENSIVE_BUILDING_ANTI_AIR = null;

    // =========================================================

    /**
     * Makes sure all necessary AtlantisConfig variables are set (non-null).
     */
    public static void validate() {
        validate("MY_RACE", MY_RACE);
        validate("BASE", BASE);
        validate("WORKER", WORKER);
        validate("BARRACKS", BARRACKS);
        validate("SUPPLY", SUPPLY);
        validate("GAS_BUILDING", GAS_BUILDING);

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
