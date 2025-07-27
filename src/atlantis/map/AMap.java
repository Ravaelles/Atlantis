package atlantis.map;

import atlantis.Atlantis;
import atlantis.config.ActiveMap;
import atlantis.game.A;
import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;
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

            // Init JBWEB - needed for calculating ground distance
            try {
                InitJBWEB.init();
    //            InitBWEB.init();
            } catch (Exception e) {
                A.errPrintln(
                    "JBWEB exception: " + e.getMessage() + "\n"
                    + "but dont worry. We will continue."
                );
                if (!A.isUms()) e.printStackTrace();
            }
        } catch (Exception e) {
            A.errPrintln(
                "BWEM exception: " + e.getMessage() + "\n"
                + "but dont worry. We will continue."
            );
            if (!A.isUms()) e.printStackTrace();
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
        APosition position = null;
        for (int attempts = 0; attempts < 50; attempts++) {
            int maxRadius = 30 * TilePosition.SIZE_IN_PIXELS;
            int dx = -maxRadius + A.randWithSeed(0, 2 * maxRadius, unit.id());
            int dy = -maxRadius + A.randWithSeed(0, 2 * maxRadius, unit.id());
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
            int dx = -maxRadius + A.rand(0, 2 * maxRadius);
            int dy = -maxRadius + A.rand(0, 2 * maxRadius);
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
