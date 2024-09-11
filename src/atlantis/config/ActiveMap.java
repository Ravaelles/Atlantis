package atlantis.config;

import main.Main;

public class ActiveMap {
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

    public static boolean isGosu() {
        return isMap("7th.scx") || isMap("/exp_") || isMap("vsGosu");
    }
}
