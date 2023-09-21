package atlantis.config;

import main.Main;

public class MapAndRace {

    /**
     * Race used by the Atlantis.
     */
//    public static final String OUR_RACE = "Protoss";
//    public static final String OUR_RACE = "Terran";
    public static final String OUR_RACE = "Zerg";

    /**
     * Single player enemy race.
     */
    public static final String ENEMY_RACE = "Protoss";
//    public static final String ENEMY_RACE = "Terran";
//    public static final String ENEMY_RACE = "Zerg";

    /**
     * Will modify bwapi.ini to use this map.
     */
    public static final String MAP = activeMapPath();

    // =========================================================

    public static String activeMap() {
        return Main.activeMap();
    }

    // =========================================================

    public static String activeMapPath() {
        return "maps/BroodWar/" + activeMap();
    }

    public static boolean isMap(String mapPartialName) {
        return activeMap().contains(mapPartialName);
    }

    public static boolean isMapGosu() {
        return isMap("7th.scx") || isMap("/exp_") || isMap("vsGosu");
    }
}
