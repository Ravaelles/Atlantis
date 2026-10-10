package atlantis.map;

import atlantis.Atlantis;
import atlantis.config.ActiveMap;
import atlantis.game.A;
import atlantis.game.ARandom;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
import atlantis.util.AConsole;
import atlantis.util.cache.Cache;
import bwapi.TilePosition;
import bwem.BWEM;
import bwem.BWMap;

import java.util.ArrayList;

/**
 * This class provides information about high-abstraction level map operations like returning place for the
 * next base or returning important choke point near the main base.
 */
public class AMap {
    protected static BWEM bwem = null;
//    private static final BWTA bwta = null;

    private static Cache<Object> cache = new Cache<>();
    //    private static List<ARegion> cached_regions;
//    private static List<AChoke> cached_chokes = null;
//    private static AChoke cached_mainBaseChoke = null;
//    private static Map<String, Positions> regionsToPolygonPoints = new HashMap<>();

    // =========================================================

    @SuppressWarnings("deprecation")
    public static void initMapAnalysis() {
        System.out.print("Analyzing map... ");

//        cached_basesToChokes = new HashMap<>();
//        cached_regions = new ArrayList<>();

        // Fake BWTA class using BWEM behind the scenes
//        BWTA.readMap(Atlantis.game());
//        BWTA.analyze();

//        game = bwClient.getGame();

        // Init BWEM - Terran analysis tool
        bwem = new BWEM(Atlantis.game());

        try {
            bwem.initialize();
            bwem.getMap().assignStartingLocationsToSuitableBases();
            verifyMapAnalysisIsUsable();

            // Init JBWEB - needed for calculating ground distance
            try {
                InitJBWEB.init();
    //            InitBWEB.init();
            } catch (Exception e) {
                AConsole.errPrintln(
                    "JBWEB exception: " + e.getMessage() + " "
                    + "but dont worry. We will continue."
                );
                if (!A.isUms()) e.printStackTrace();
            }
        } catch (Exception e) {
            handleBwemFailure(e);
        }
    }

    /**
     * What to do when BWEM cannot be built.
     *
     * <p>
     * On the classic backends this stays a warning: the bot has other sources for
     * bases and chokes and has always played through it.
     * </p>
     *
     * <p>
     * <b>On OpenBW it is fatal, by the owner's ruling (2026-10-10).</b> Everything
     * map-shaped in this bot - regions, areas, chokes, base locations, expansion
     * targets - is read from BWEM, and the OpenBW path has no second source for
     * them. Continuing with an uninitialised {@code bwem} does not produce a
     * degraded bot that does less; it produces a bot whose map knowledge is empty,
     * so every question about where to build or where to go answers "nowhere" and
     * the run fails in a way that looks like something else entirely - which is
     * exactly how a whole E2E session was spent on "Can't find place for Pylon"
     * (NEXT #47). A loud stop names the cause; a quiet continuation hides it.
     * </p>
     *
     * <p>
     * The exit is non-zero on purpose: {@code scripts/run-openbw-e2e.sh} propagates
     * the bot's code, so a BWEM failure is reported as a failed run rather than a
     * played one with zero buildings.
     * </p>
     *
     * <p>
     * Superseded advice: {@code _AI/LOCAL-STARCRAFT.md} said to continue with an
     * uninitialised BWEM and treat "fake natural / fake choke" as the only working
     * path. That is withdrawn - measured 2026-10-10, BWEM does initialise on
     * TauCross (a real {@code Choke{[67,114], width=2}} was produced), so a failure
     * here means something new is wrong and must not be papered over.
     * </p>
     */
    private static void handleBwemFailure(Exception e) {
        AConsole.errPrintln("BWEM exception: " + e.getMessage());
        if (!A.isUms()) e.printStackTrace();

        if (!atlantis.config.env.Env.isOpenBW()) {
            AConsole.errPrintln("Continuing: the classic backends have other map sources.");
            return;
        }

        AConsole.errPrintln(
            "\n"
                + "################################################################\n"
                + "# FATAL: BWEM failed on OpenBW - map analysis is unavailable.\n"
                + "# Regions, areas, chokes and base locations all come from BWEM,\n"
                + "# and the OpenBW path has no fallback for them. Continuing would\n"
                + "# leave the bot with empty map knowledge: it would place nothing,\n"
                + "# expand nowhere, and report it as \"can't build here\" instead of\n"
                + "# a map problem. Failing now, on purpose.\n"
                + "# Cause: " + e.getMessage() + "\n"
                + "# See _AI/CHALLENGES/OpenBW.md for the region/map traps.\n"
                + "################################################################\n"
        );

        System.exit(FAIL_EXIT_CODE_BWEM_MISSING);
    }

    /**
     * Exit code for "BWEM could not be built on OpenBW". Non-zero so the runner
     * reports a failed run; kept distinct from 1 so a log or a CI step can tell this
     * apart from a generic crash.
     */
    public static final int FAIL_EXIT_CODE_BWEM_MISSING = 42;

    /**
     * Exit code for "BWEM built, but its contents are empty/unusable". Separate from
     * {@link #FAIL_EXIT_CODE_BWEM_MISSING} because the two need different fixes: a
     * missing BWEM means the library failed, an empty one means it ran and produced
     * nothing (wrong map data, wrong start locations).
     */
    public static final int FAIL_EXIT_CODE_BWEM_EMPTY = 43;

    /**
     * Asserts that the map model is actually usable before the game relies on it.
     *
     * <p>
     * Every map-shaped decision in this bot reads BWEM, so "it did not throw" is not
     * enough - the model must have real content. Measured 2026-10-10 on TauCross:
     * BWEM produces one main choke and a non-empty area list, so those are the bars.
     * An empty model is the state that made a whole session chase "Can't find place
     * for Pylon" through placement code when the real answer was "there is no map".
     * </p>
     *
     * <p>
     * On OpenBW a failure here exits non-zero (the runner propagates the code and
     * reports a failed run). On the classic backends it only prints, because those
     * have other map sources and have always played through a partial analysis.
     * </p>
     */
    private static void verifyMapAnalysisIsUsable() {
        int areas = -1;
        int chokes = -1;
        try {
            areas = bwem.getMap().getAreas().size();

            // Chokes hang off each Area in this BWEM (there is no map-level
            // getChokePoints - our src/bwem/BWMap shadows the jar's copy), so they
            // are counted the way the bot reads them.
            int total = 0;
            for (bwem.Area area : bwem.getMap().getAreas()) {
                total += area.getChokePoints().size();
            }
            chokes = total;
        } catch (Exception e) {
            areas = -1;
        }

        System.out.println("MAP_ANALYSIS areas=" + areas + " chokes=" + chokes);

        boolean usable = areas > 0 && chokes > 0;
        if (usable) return;

        AConsole.errPrintln(
            "MAP_ANALYSIS: BWEM produced no usable map model (areas=" + areas
                + ", chokes=" + chokes + "). Regions, base locations and every"
                + " build/expand decision come from it; continuing would leave the bot"
                + " with empty map knowledge."
        );

        if (atlantis.config.env.Env.isOpenBW()) {
            AConsole.errPrintln("FATAL on OpenBW: exiting " + FAIL_EXIT_CODE_BWEM_EMPTY
                + " so the run is reported as failed rather than as a played game"
                + " that builds nothing. See _AI/CHALLENGES/OpenBW.md.");
            System.exit(FAIL_EXIT_CODE_BWEM_EMPTY);
        }
    }

    // =========================================================

    /**
     * Returns map object.
     */
    public static BWMap getMap() {
        return bwem.getMap();
    }

    public static BWEM bwem() {
        return bwem;
    }

//    public static BWTA getMap() {
//        return bwta;
//    }

    /**
     * Returns map width in tiles.
     */
    public static int getMapWidthInTiles() {
        return Atlantis.game().mapWidth();
    }

    /**
     * Returns map height in tiles.
     */
    public static int getMapHeightInTiles() {
        return Atlantis.game().mapHeight();
    }

    // === Choke points ========================================

    /**
     * Returns random point on map with fog of war, preferably unexplored one.
     */
    public static APosition randomInvisiblePosition(AUnit unit) {
        if (unit == null) return null;

        APosition position = null;
        for (int attempts = 0; attempts < 50; attempts++) {
            int maxRadius = 30 * TilePosition.SIZE_IN_PIXELS;
            int dx = -maxRadius + ARandom.randWithSeed(0, 2 * maxRadius, unit.id());
            int dy = -maxRadius + ARandom.randWithSeed(0, 2 * maxRadius, unit.id());
            position = unit.translateByPixels(dx, dy).makeBuildableGroundPositionFarFromBounds();
            if (
                position != null
                    && position.isWalkable()
                    && position.isBuildableNotIncludingBuildings()
                    && !position.isPositionVisible()
                    && unit.hasPathTo(position)
                    && unit.position().groundDistanceTo(position) <= 100
            ) {
                return getMostWalkablePositionNear(position, 4);
            }
        }
        return null;
    }

    public static APosition randomUnexploredPosition(HasPosition startPoint) {
        if (startPoint == null) return null;

        APosition position = null;
        for (int attempts = 0; attempts < 50; attempts++) {
            int mapDimension = Math.max(Atlantis.game().mapWidth(), Atlantis.game().mapHeight());
            int maxRadius = mapDimension * TilePosition.SIZE_IN_PIXELS;
            int dx = -maxRadius + ARandom.rand(0, 2 * maxRadius);
            int dy = -maxRadius + ARandom.rand(0, 2 * maxRadius);
            position = startPoint.translateByPixels(dx, dy).makeBuildableGroundPositionFarFromBounds();
            if (
                position != null
                    && position.isWalkable()
                    && position.isBuildableNotIncludingBuildings()
                    && !position.isExplored()
//                            && position.translateByTiles(-1, 0).isWalkable()
//                            && position.translateByTiles(1, 0).isWalkable()
//                            && position.translateByTiles(0, 1).isWalkable()
//                            && position.translateByTiles(0, -1).isWalkable()
                    && startPoint.position().hasPathTo(position)
                    && startPoint.position().groundDistanceTo(position) <= 100
            ) {
                return getMostWalkablePositionNear(position, 4);
            }
        }
        return null;
    }


    /**
     * If unit moves near the edges, its running options are limited and could be stuck.
     * Instead of going there, prefer a Near position which has more space around.
     */
    public static APosition getMostWalkablePositionNear(APosition position, int tileSearchRadius) {
        int bestScore = -1;
        APosition bestTile = null;

        for (int dtx = -tileSearchRadius; dtx <= 2 * tileSearchRadius; dtx += 2) {
            for (int dty = -tileSearchRadius; dty <= 2 * tileSearchRadius; dty += 2) {
                if (dtx != 0 && dty != 0) {
                    APosition tile = position.translateByTiles(dtx, dty).makeBuildableGroundPositionFarFromBounds();

                    if (tile == null) continue;

                    int score = tileWalkabilityScore(tile);
                    if (score > bestScore) {
                        bestScore = score;
                        bestTile = tile;
                    }
                }
            }
        }

        return bestTile;
    }

    private static int tileWalkabilityScore(APosition position) {
        int score = 0;
        int tileSearchRadius = 8;

        for (int dtx = -tileSearchRadius; dtx <= 2 * tileSearchRadius; dtx += 3) {
            for (int dty = -tileSearchRadius; dty <= 2 * tileSearchRadius; dty += 3) {
                if (tileSearchRadius <= dtx + dty && dtx + dty <= tileSearchRadius + 1) {
                    APosition tile = position.translateByTiles(dtx, dty).makeValidGroundPosition();

                    if (tile == null) continue;

                    score += tile.isWalkable() ? 1 : 0;
                }
            }
        }

        return score;
    }

    public static String getMapName() {
        return Atlantis.game().mapName();
    }

    public static ArrayList<APosition> allChokeCenters() {
        return (ArrayList<APosition>) cache.get(
            "allChokeCenters",
            -1,
            () -> {
                ArrayList<APosition> centers = new ArrayList<>();
                for (AChoke choke : Chokes.chokes()) {
                    centers.add(choke.center());
                }
                return centers;
            }
        );
    }

    // =========================================================
    // Special methods

    /**
     * Analyzing map and terrain is far from perfect. For many maps it happens that there are some choke
     * points near the main base which are completely invalid e.g. they lead to a dead-end or in the best case
     * are pointing to a place where the enemy won't come from. This method "disables" those points so they're
     * never returned, but they don't actually get removed. It only sets disabled=true flag for them.
     *
     * @return true if everything went okay
     */
//    public static boolean disableSomeOfTheChokes() {
//        AUnit mainBase = Select.mainBase();
//        if (mainBase == null) {
//            return false;
//        }
//
//        ARegion baseRegion = getRegion(mainBase.position());
//        if (baseRegion == null) {
//            System.err.println("Error #821493b");
//            System.err.println("Main base = " + mainBase);
//            System.err.println("Base region = " + baseRegion);
//            return false;
//        }
//
//        Collection<AChoke> chokes = baseRegion.chokes();
//        for (AChoke choke : chokes) {
//            if (baseRegion.chokes().contains(choke)) {
//                System.err.println("Disabling choke point: " + APosition.create(choke.getCenter()));
//                Chokes.disabledChokes.add(choke);    //choke.setDisabled(true);
//            }
//        }
//
//        return true;
//    }
    public static void setBWEM(BWEM bwem) {
        AMap.bwem = bwem;
    }

    public static String mapFileNameWithoutPath() {
        // Remove everything before the last slash
        String name = ActiveMap.name();

        if (name == null) return "# Invalid map name #";

        int lastSlashIndex = name.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            name = name.substring(lastSlashIndex + 1);
        }

        return name;
    }
}
