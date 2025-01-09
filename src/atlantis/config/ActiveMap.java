package atlantis.config;

public class ActiveMap {
    /**
     * Will modify bwapi.ini to use this map. To be set in Main.
     */
    private static String mapName = null;
    private static String _cachedMapPath = null;

    // =========================================================

    public static void specifyMap(String map) {
        assert map != null : "Map can't be null";

        ActiveMap.mapName = map;
    }

    public static String name() {
        return mapName;
    }

    // =========================================================

    public static String readMapFromCliArgument(String[] args) {
        String mapName = null;

        for (String arg : args) {
            if (arg.startsWith("--map=")) {
                mapName = arg.substring(6); // Remove "--map=" prefix
            }
        }

        return mapName;
    }

    // =========================================================

    public static String activeMapPath() {
        if (_cachedMapPath != null) return _cachedMapPath;

        return _cachedMapPath = ("maps/BroodWar/" + ActiveMap.mapName);
    }

    public static boolean isMap(String mapPartialName) {
        return ActiveMap.mapName != null && ActiveMap.mapName.contains(mapPartialName);
    }

    public static boolean isGosu() {
        return isMap("7th.scx") || isMap("/exp_") || isMap("vsGosu");
    }
}
