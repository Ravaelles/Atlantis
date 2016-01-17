package atlantis;

import atlantis.production.strategies.AtlantisProductionStrategy;
import jnibwapi.types.RaceType;
import jnibwapi.types.RaceType.RaceTypes;
import jnibwapi.types.UnitType;
import jnibwapi.types.UnitType.UnitTypes;

/**
 * This class is used to set up your Atlantis framework by providing some basic informations about your bot.
 * Example code:
 * <p>
 * <b> useConfigForTerran() </b>
 * </p>
 * or for other race you are playing.
 */
public class AtlantisConfig {

    // =========================================================    
    // Customizable variables

    /**
     * If not empty, then before the game starts, player race in the bwapi.ini will be overriden to match 
     * currently played one.
     */
    public static String BWAPI_INI_PATH = "";
    
    /**
     * Game speed. Lower is faster. 0 is fastest, 20 is about normal game speed.
     */
    public static int GAME_SPEED = 0;
    
    /**
     * If true, game will slow down on fighting, but normally it will run quicker.
     */
    public static boolean USE_DYNAMIC_GAME_SPEED_SLOWDOWN = true;
    
    /** 
     * Amount of game speed units to be used for dynamic game speed slowdown.
     */
    public static int DYNAMIC_GAME_SPEED_SLOWDOWN = 3;

    /**
     * If value less than 201 is passed, then you don't need to specify when to build supply buildings. They
     * will be <b>automatically built only if your total supply exceeds this value</b>.
     */
    public static int USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 201;

    /**
     * Force production of a worker whenever you have 50 minerals and less than N workers.
     */
    public static int AUTO_PRODUCE_WORKERS_UNTIL_N_WORKERS = 0;

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
    public static int SCOUT_IS_NTH_WORKER = 7;

    // =========================================================
    // Do not customize
    public static RaceType MY_RACE = null;
    public static UnitType BASE = null;
    public static UnitType WORKER = null;
    public static UnitType BARRACKS = null;
    public static UnitType SUPPLY = null;
    public static UnitType GAS_BUILDING = null;
    
    private static AtlantisProductionStrategy productionStrategy;

    // =========================================================
    
    /**
     * Helper method for using Terran race.
     */
    public static void useConfigForTerran() {
        AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 11;

        AtlantisConfig.MY_RACE = RaceTypes.Terran;
        AtlantisConfig.BASE = UnitTypes.Terran_Command_Center;
        AtlantisConfig.WORKER = UnitTypes.Terran_SCV;
        AtlantisConfig.BARRACKS = UnitTypes.Terran_Barracks;
        AtlantisConfig.SUPPLY = UnitTypes.Terran_Supply_Depot;
        AtlantisConfig.GAS_BUILDING = UnitTypes.Terran_Refinery;
    }

    /**
     * Helper method for using Terran race.
     */
    public static void useConfigForZerg() {
        AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 8;

        AtlantisConfig.MY_RACE = RaceTypes.Zerg;
        AtlantisConfig.BASE = UnitTypes.Zerg_Hatchery;
        AtlantisConfig.WORKER = UnitTypes.Zerg_Drone;
        AtlantisConfig.BARRACKS = UnitTypes.Zerg_Spawning_Pool;
        AtlantisConfig.SUPPLY = UnitTypes.Zerg_Overlord;
        AtlantisConfig.GAS_BUILDING = UnitTypes.Zerg_Extractor;
    }

    /**
     * Helper method for using Protoss race.
     */
    public static void useConfigForProtoss() {
        AtlantisConfig.USE_AUTO_SUPPLY_MANAGER_WHEN_SUPPLY_EXCEEDS = 11;

        AtlantisConfig.MY_RACE = RaceTypes.Protoss;
        AtlantisConfig.BASE = UnitTypes.Protoss_Nexus;
        AtlantisConfig.WORKER = UnitTypes.Protoss_Probe;
        AtlantisConfig.BARRACKS = UnitTypes.Protoss_Gateway;
        AtlantisConfig.SUPPLY = UnitTypes.Protoss_Pylon;
        AtlantisConfig.GAS_BUILDING = UnitTypes.Protoss_Assimilator;
        
//        overrideBwapiIniRace("Protoss");
    }
    
    // =========================================================
    
    /**
     * 
     */
    private static void overrideBwapiIniRace(String raceString) {
        System.out.println("@NotImplemented overrideBwapiIniRace");
        System.exit(-1);
//        overrideBwapiIniRace(raceString);
    }
    
    /**
     * Makes sure all necessary variables are set (non-null).
     */
    protected static void validate() {
        validate("MY_RACE", MY_RACE);
        validate("BASE", BASE);
        validate("WORKER", WORKER);
        validate("BARRACKS", BARRACKS);
        validate("SUPPLY", SUPPLY);
        validate("GAS_BUILDING", GAS_BUILDING);
        validate("You have to specify production strategy\n-> AtlantisConfig.useProductionStrategy", productionStrategy);
    }

    // =========================================================
    private static void validate(String title, Object variable) {
        if (variable == null) {
            error(title);
        }
    }

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

    // =========================================================
    // Hi-level configs
    /**
     * Pass an object that will be responsible for the production queue. See e.g. class named
     * DefaultTerranProductionStrategy.
     */
    public static void useProductionStrategy(AtlantisProductionStrategy productionStrategy) {
        AtlantisConfig.productionStrategy = productionStrategy;
    }

    /**
     * Returns object that is responsible for the production queue.
     */
    public static AtlantisProductionStrategy getProductionStrategy() {
        return productionStrategy;
    }

}
