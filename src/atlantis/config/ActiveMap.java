package atlantis.config;

import atlantis.Atlantis;

import java.io.File;

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
        if (mapName != null) return mapName;
        if (Atlantis.game() != null && Atlantis.game().mapName() != null) return Atlantis.game().mapName();

        return null;
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

        // The map name already carries its own folder, e.g. "ums/rav/..." or
        // "sscai/(4)Python.scx". Prefixing it with "maps/BroodWar/" (the old
        // Windows layout) produced "maps/BroodWar/ums/..." - a path that does
        // not exist in the Wine install, where maps live under maps/ums and
        // maps/sscai directly. BWAPI then failed to load the map and the game
        // never started (measured 2026-10-05).
        String name = ActiveMap.mapName;
        if (name != null && name.startsWith("maps/")) {
            return _cachedMapPath = name;
        }

        // A short name (e.g. "M&M_v_Zealots.scx") and a full path (e.g.
        // "ums/rav/minimaps/M&M_v_Zealots.scx") can both name the same file.
        // BWAPI requires the exact path, and the same file name can exist in
        // several folders, so the resolver below turns a bare file name into
        // the one path that actually exists in the map root.
        String trimmed = name == null ? null : name.trim();
        if (trimmed != null && !trimmed.contains("/")) {
            String resolved = resolveByName(trimmed);
            if (resolved != null) {
                System.out.println("[Atlantis] Resolved map '" + trimmed + "' to '" + resolved + "'.");
                return _cachedMapPath = ("maps/" + resolved);
            }
        }

        return _cachedMapPath = ("maps/" + trimmed);
    }

    /**
     * Finds a map file by its bare name under the map root and returns its path
     * relative to {@code maps/}, or null when the root is not a directory or the
     * name is ambiguous/absent.
     *
     * <p>The roots are the two the game knows: {@code maps/ums/} (custom UMS
     * maps, where own test maps live) and {@code maps/sscai/} (the ladder map
     * set). If more than one folder holds a file of that name the answer is
     * ambiguous, so nothing is resolved and the caller falls back to
     * {@code maps/<name>} - a loud failure (BWAPI cannot load it) instead of a
     * silent mix-up between two real maps.</p>
     */
    private static String resolveByName(String fileName) {
        File mapsRoot = new File("maps");
        if (!mapsRoot.isDirectory()) return null;

        for (String area : new String[]{"ums", "sscai"}) {
            File areaRoot = new File(mapsRoot, area);
            if (!areaRoot.isDirectory()) continue;

            String found = findFile(areaRoot, fileName, 0);
            if (found != null) {
                return area + "/" + found;
            }
        }
        return null;
    }

    /** Recursive search for an exact file name; returns the path relative to {@code dir}, or null. */
    private static String findFile(File dir, String fileName, int depth) {
        if (depth > 6) return null;

        File[] children = dir.listFiles();
        if (children == null) return null;

        for (File child : children) {
            if (child.isDirectory()) {
                String nested = findFile(child, fileName, depth + 1);
                if (nested != null) return child.getName() + "/" + nested;
            } else if (child.getName().equalsIgnoreCase(fileName)) {
                return child.getName();
            }
        }
        return null;
    }

    public static boolean isMap(String ...mapPartialName) {
        if (ActiveMap.mapName == null) return false;

        for (String subname : mapPartialName){
            if (ActiveMap.mapName.contains(subname)) return true;
        }

        return false;
    }

    public static boolean isGosu() {
        return isMap("7th.scx") || isMap("/exp_") || isMap("vsGosu");
    }
}
