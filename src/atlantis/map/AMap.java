package atlantis.map;

import atlantis.AGame;
import atlantis.Atlantis;
import atlantis.constructing.AConstructionRequests;
import atlantis.constructing.ConstructionOrder;
import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.position.HasPosition;
import atlantis.position.PositionHelper;
import atlantis.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.A;
import atlantis.position.PositionUtil;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.BWTA;
import bwta.Chokepoint;
import bwta.Region;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class provides information about high-abstraction level map operations like returning place for the
 * next base or returning important choke point near the main base.
 */
public class AMap {

    private static final BWTA bwta = null;
    private static final Set<AChokepoint> disabledChokepoints = new HashSet<>();
    private static List<ARegion> cached_regions;
    private static List<AChokepoint> cached_chokePoints = null;
    private static AChokepoint cached_mainBaseChokepoint = null;
    private static HashMap<APosition, AChokepoint> cached_basesToChokepoints;
//    private static AChokepoint cached_basesToChokepoints = null;
//    private static Map<String, Positions> regionsToPolygonPoints = new HashMap<>();

    // =========================================================

    @SuppressWarnings("deprecation")
    public static void initMapAnalysis() {
        System.out.print("Analyzing map... ");

        cached_basesToChokepoints = new HashMap<>();
        cached_regions = new ArrayList<>();

        BWTA.readMap(Atlantis.game());
        BWTA.analyze();
    }

    // =========================================================

    /**
     * Returns map object.
     */
    public static BWTA getMap() {
        return bwta;
    }

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
     * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This
     * method returns this choke point. It's perfect position to defend (because it's *choke* point).
     */
    public static AChokepoint getChokepointForMainBase() {
        if (cached_mainBaseChokepoint == null) {
            AUnit mainBase = Select.mainBase();
            if (mainBase != null) {

                // Define region where our main base is
                ARegion mainRegion = getRegion(mainBase.getPosition());
                // System.out.println("mainRegion = " + mainRegion);
                if (mainRegion != null) {

                    // Define localization of the second base to expand
//                    ABaseLocation naturalBase = getNaturalBaseLocation();
                    APosition naturalBase = getNaturalBaseLocation();
                    // System.out.println("secondBase = " + secondBase);
                    if (naturalBase == null) {
                        return null;
                    }

                    // Define region of the second base
                    ARegion naturalBaseRegion = naturalBase.getRegion();
                    // System.out.println("secondRegion = " + secondRegion);
                    if (naturalBaseRegion == null) {
                        return null;
                    }

                    // Try to match choke points between the two regions
                    for (AChokepoint mainRegionChoke : mainRegion.getChokepoints()) {
                        // System.out.println("mainRegionChoke = " + mainRegionChoke + " / "
                        // + (mainRegionChoke.getFirstRegion()) + " / " + (mainRegionChoke.getSecondRegion()));
                        if (naturalBaseRegion.equals(mainRegionChoke.getFirstRegion())
                                || naturalBaseRegion.equals(mainRegionChoke.getSecondRegion())) {
                            cached_mainBaseChokepoint = mainRegionChoke;
                            // System.out.println("MAIN CHOKE FOUND! " + cached_mainBaseChokepoint);
                            break;
                        }
                    }

                    if (cached_mainBaseChokepoint == null) {
                        cached_mainBaseChokepoint = mainRegion.getChokepoints().iterator().next();
                    }
                }
            }
        }

        return cached_mainBaseChokepoint;
    }

    /**
     * Returns chokepoint to defend for the natural (second) base.
     */
    public static AChokepoint getChokepointForNaturalBase(APosition relativeTo) {
        if (relativeTo == null) {
            return null;
        }

        if (cached_basesToChokepoints.containsKey(relativeTo)) {
            AChokepoint choke = cached_basesToChokepoints.get(relativeTo);
//            APainter.paintCircle(APosition.create(choke.getCenter()), 5, Color.White);
//            APainter.paintCircle(APosition.create(choke.getCenter()), 7, Color.White);
//            APainter.paintTextCentered(
//                    choke.getCenter().translateByTiles(0, 3),
//                    choke.getWidth() + " wide",
//                    Color.White
//            );
            return choke;
        }

        // =========================================================

        ARegion naturalRegion = getRegion(getNaturalBaseLocation(relativeTo.getPosition()));
        if (naturalRegion == null) {
            System.err.println("Can't find region for natural base");
            AGame.setUmsMode(true);
            return null;
        }

        AChokepoint chokepointForMainBase = getChokepointForMainBase();
        if (chokepointForMainBase == null) {
            return null;
        }

        for (AChokepoint chokepoint : naturalRegion.getChokepoints()) {
//            naturalRegion.
//            APosition center = APosition.create(chokepoint.getCenter());
//            if (chokepoint.getCenter() > 1) {
            if (chokepoint.getCenter().distTo(chokepointForMainBase) > 1) {
                cached_basesToChokepoints.put(relativeTo, chokepoint);
                return chokepoint;
            }
        }

        return null;
    }

    /**
     * Returns starting location that's nearest to given position and is not yet explored (black space, not
     * fog of war).
     */
    public static APosition getNearestUnexploredStartingLocation(APosition nearestTo) {
        if (nearestTo == null) {
            return null;
        }

        // Get list of all starting locations
        Positions<ABaseLocation> startingLocations = new Positions<>();
        startingLocations.addPositions(getStartingLocations(true));

        // Sort them all by closest to given nearestTo position
        startingLocations.sortByDistanceTo(nearestTo, true);

        // For every location...
        for (ABaseLocation baseLocationPosition : startingLocations.list()) {
            if (!isExplored(baseLocationPosition.getPosition())) {
                return APosition.create(baseLocationPosition);
            }
        }
        return null;
    }

    public static ABaseLocation getStartingLocationBasedOnIndex(int index) {
        ArrayList<ABaseLocation> baseLocations = new ArrayList<>();
        baseLocations.addAll(getStartingLocations(true));

        if (baseLocations.isEmpty()) {
            return null;
        }

        return baseLocations.get(index % baseLocations.size());
    }

    /**
     * Returns nearest free base location where we don't have base built yet.
     */
    public static ABaseLocation getExpansionFreeBaseLocationNearestTo(Position nearestTo) {

        // Get list of all base locations
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(getBaseLocations());

        // Sort them all by closest to given nearestTo position
        if (nearestTo != null) {
            baseLocations.sortByDistanceTo(nearestTo, true);
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation)) {
                return baseLocation;
            }
        }
        return null;
    }

    /**
     * Returns free base location which is as far from enemy starting location as possible.
     */
    public static ABaseLocation getExpansionBaseLocationMostDistantToEnemy() {
        APosition farthestTo = AEnemyUnits.getEnemyBase();
        if (farthestTo == null) {
            return getExpansionFreeBaseLocationNearestTo(Select.ourBases().first().getPosition());
        }

        // =========================================================

        // Get list of all base locations
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(getBaseLocations());

        // Sort them all by closest to given nearestTo position
        if (farthestTo != null) {
            baseLocations.sortByDistanceTo(farthestTo, false);
        }

        // For every location...
        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation)) {
                return baseLocation;
            }
        }
        return null;
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static APosition getNaturalBaseLocation() {
        if (Select.mainBase() == null) {
            return null;
        }

        ABaseLocation naturalBaseLocation = getNaturalBaseLocation(Select.mainBase().getPosition());
        if (naturalBaseLocation != null) {
            return naturalBaseLocation.getPosition();
        }

        return null;
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static ABaseLocation getNaturalBaseLocation(APosition nearestTo) {
        // Get all base locations, sort by being closest to given nearestTo position
        Positions<ABaseLocation> baseLocations = new Positions<>();
        baseLocations.addPositions(getBaseLocations());
        baseLocations.sortByGroundDistanceTo(nearestTo, true);

        for (ABaseLocation baseLocation : baseLocations.list()) {
            if (baseLocation.isStartLocation() || !nearestTo.hasPathTo(baseLocation.getPosition())) {
                continue;
            }
            return baseLocation;
        }

        return null;
    }

    /**
     * Returns random point on map with fog of war, preferably unexplored one.
     */
    public static APosition getRandomInvisiblePosition(APosition startPoint) {
        APosition position = null;
        for (int attempts = 0; attempts < 50; attempts++) {
            int maxRadius = 30 * TilePosition.SIZE_IN_PIXELS;
            int dx = -maxRadius + A.rand(0, 2 * maxRadius);
            int dy = -maxRadius + A.rand(0, 2 * maxRadius);
            position = PositionHelper.translateByPixels(startPoint, dx, dy).makeValidFarFromBounds();
            if (isWalkable(position) && !isVisible(position) && startPoint.hasPathTo(position)) {
                return position;
            }
        }
        return null;
    }

    public static APosition getRandomUnexploredPosition(APosition startPoint) {
        APosition position = null;
        for (int attempts = 0; attempts < 50; attempts++) {
            int mapDimension = Math.max(Atlantis.game().mapWidth(), Atlantis.game().mapHeight());
            int maxRadius = mapDimension * TilePosition.SIZE_IN_PIXELS;
            int dx = -maxRadius + A.rand(0, 2 * maxRadius);
            int dy = -maxRadius + A.rand(0, 2 * maxRadius);
            position = PositionHelper.translateByPixels(startPoint, dx, dy).makeValidFarFromBounds();
            if (isWalkable(position) && !isExplored(position) && startPoint.hasPathTo(position)) {
                return getMostWalkablePositionNear(position, 2);
            }
        }
        return null;
    }

    /**
     * Returns nearest (preferably directly connected) region which has center of it still unexplored.
     */
    public static ARegion getNearestUnexploredRegion(APosition position) {
        ARegion region = AMap.getRegion(position);
        if (region == null) {
            return null;
        }

        ARegion regionToVisit = null;

        for (ARegion reachableRegion : region.getReachableRegions()) {
            if (!AMap.isExplored(reachableRegion.getCenter())) {
                regionToVisit = reachableRegion;
//                return APosition.createFrom(regionToVisit.getCenter());
                return regionToVisit;
            }
        }

        return null;
    }


    /**
     * If unit moves near the edges, its running options are limited and could be stuck.
     * Instead of going there, prefer a nearby position which has more space around.
     */
    public static APosition getMostWalkablePositionNear(APosition position, int tileSearchRadius) {
        int bestScore = -1;
        APosition bestTile = null;

        for (int dtx = -tileSearchRadius; dtx <= 2 * tileSearchRadius; dtx += 2) {
            for (int dty = -tileSearchRadius; dty <= 2 * tileSearchRadius; dty += 2) {
                if (dtx != 0 && dty != 0) {
                    APosition tile = position.translateByTiles(dtx, dty).makeValidFarFromBounds();
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
                    score += isWalkable(position.translateByTiles(dtx, dty).makeValid()) ? 1 : 0;
                }
            }
        }

        return score;
    }

    /**
     * Returns nearest land-connected position on map which hasn't been explored.
     */
//    public static APosition getNearestUnexploredAccessiblePosition(APosition position) {
//        int maxRadius = Math.max(getMapWidthInTiles(), getMapHeightInTiles());
//        int currentRadius = 6;
//        int step = 3;
//
//        while (currentRadius < maxRadius) {
//            double doubleCurrentRadius = currentRadius * 2;
//            for (int dx = -currentRadius; dx <= currentRadius; dx += doubleCurrentRadius) {
//                for (int dy = -currentRadius; dy <= currentRadius; dy += doubleCurrentRadius) {
//                    APosition potentialPosition = position.translateByTiles(dx, dy).makeValid();
//                    if (!isExplored(potentialPosition) && position.hasPathTo(potentialPosition)) {
//                        return potentialPosition;
//                    }
//                }
//            }
//
//            currentRadius += 3;
//        }
//
////        System.err.println("Can't find getNearestUnexploredAccessiblePosition");
//        return null;
//    }

    public static AChokepoint getNearestChokepoint(APosition position) {
        double nearestDist = 99999;
        AChokepoint nearest = null;

        for (AChokepoint chokePoint : getChokePoints()) {
            double dist = position.groundDistanceTo(chokePoint.getCenter()) - chokePoint.getWidth() / 32.0 / 2;
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = chokePoint;
            }
        }

        return nearest;
    }

    /**
     * @fix
     * @broken due to BWTA Polygon gone from the bridge :- ( It was working in BWMirror.
     *
     * Can be used to avoid getting to close to the region edges, which may cause unit to get stuck.
     */
//    public static double getDistanceToAnyRegionPolygonPoint(APosition unitPosition) {
//        return 99;
//        Region region = unitPosition.getRegion();
//
//        if (region == null) {
//            System.err.println("isPositionFarFromAnyRegionPolygonPoint -> Region is null");
//            return 999;
//        }
//        if (region.getPolygon() == null) {
//            System.err.println("isPositionFarFromAnyRegionPolygonPoint -> region.getPolygon() is null");
//            return 999;
//        }
//
//        // === Define polygon points for given region ==============
//
//        Positions polygonPoints = new Positions();
//        if (regionsToPolygonPoints.containsKey(region.toString())) {
//            polygonPoints = regionsToPolygonPoints.get(region.toString());
//        } else {
//            polygonPoints = new Positions();
//            polygonPoints.addPositions(region.getPolygon().getPoints());
//            regionsToPolygonPoints.put(region.toString(), polygonPoints);
//        }
//
////        for (Positions positions : regionsToPolygonPoints.values()) {
////            for (Iterator it = positions.arrayList().iterator(); it.hasNext();) {
////                Position position = (Position) it.next();
////                APainter.paintCircle(position, 13, Color.Yellow);
////                APainter.paintCircle(position, 16, Color.Yellow);
////            }
////        }
//
//        APosition nearestPolygon = polygonPoints.nearestTo(unitPosition);
//
//        // =========================================================
//
//        if (nearestPolygon != null) {
//            double distanceTo = nearestPolygon.distanceTo(unitPosition);
//            return nearestPolygon.distanceTo(unitPosition);
//        } else {
//            return 99;
//        }
//    }

    // =========================================================

    /**
     * Returns list of places that have geyser and mineral fields so they are the places where you could build
     * a base. Starting locations are also included here.
     */
    public static List<ABaseLocation> getBaseLocations() {
//        return BWTA.getBaseLocations();
        return BWTA.getBaseLocations()
                .stream()
                .map(base -> ABaseLocation.create(base))
                .collect(Collectors.toList());
    }

    /**
     * Returns list of all places where players can start a game. Note, that if you play map for two players
     * and you know location of your own base. So you also know the location of enemy base (enemy *must* be
     * there), but still obviously you don't see him.
     */
    public static List<ABaseLocation> getStartingLocations(boolean excludeOurStartLocation) {
        ArrayList<ABaseLocation> startingLocations = new ArrayList<>();
        for (ABaseLocation baseLocation : AMap.getBaseLocations()) {
            if (baseLocation.isStartLocation()) {

                // Exclude our base location if needed.
                if (excludeOurStartLocation) {
                    AUnit mainBase = Select.mainBase();
                    if (mainBase != null && PositionUtil.distanceTo(mainBase, baseLocation.getPosition()) <= 10) {
                        continue;
                    }
                }

                startingLocations.add(baseLocation);
            }
        }
        return startingLocations;
    }

    /**
     * Returns list of all choke points i.e. places where suddenly it gets extra tight and fighting there
     * usually prefers ranged units. They are perfect places for terran bunkers.
     */
    public static List<AChokepoint> getChokePoints() {
        if (cached_chokePoints == null) {
            cached_chokePoints = new ArrayList<>();
            for (Chokepoint choke : BWTA.getChokepoints()) {
                if (!disabledChokepoints.contains(choke)) { // choke.isDisabled()
                    cached_chokePoints.add(AChokepoint.create(choke));
                }
            }
        }
        return cached_chokePoints;
    }

    public static List<ARegion> getRegions() {
        if (cached_regions == null) {
            cached_regions = new ArrayList<>();
            for (Region region : BWTA.getRegions()) {
                cached_regions.add(ARegion.create(region));
            }
        }
        return cached_regions;
    }

    /**
     * Returns region object for given <b>position</b>. This object provides some very helpful informations
     * like you can access list of choke points that belong to it etc.
     *
     * @see ARegion
     */
    public static ARegion getRegion(Object param) {
        Position position = null;

        if (param instanceof Position) {
            position = (Position) param;
        } else if (param instanceof Region) {
            position = ((Region) param).getCenter();
        } else if (param instanceof HasPosition) {
            position = ((HasPosition) param).getPosition();
        } else {
            System.err.println("getRegion failed for " + param);
            return null;
        }

        return ARegion.create(BWTA.getRegion(position));
    }

    public static String getMapName() {
        return Atlantis.game().mapName();
    }

    /**
     * Returns true if given position is explored i.e. if it's not black screen (but could be fog of war).
     */
    public static boolean isExplored(APosition position) {
        return Atlantis.game().isExplored(position.toTilePosition());
    }

    /**
     * Returns true if given position visible.
     */
    public static boolean isVisible(APosition position) {
        return Atlantis.game().isVisible(position.toTilePosition());
    }

    /**
     * Returns true if given position can be traversed by land units.
     */
    public static boolean isWalkable(APosition position) {
        return Atlantis.game().isWalkable(position.getX() / 8, position.getY() / 8);
    }

    /**
     * Returns true if it's possible to build on the given position.
     */
    public static boolean isBuildable(APosition position) {
        return Atlantis.game().isBuildable(position.getX() / 8, position.getY() / 8);
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
    public static boolean disableSomeOfTheChokePoints() {
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return false;
        }

        ARegion baseRegion = getRegion(mainBase.getPosition());
        if (baseRegion == null) {
            System.err.println("Error #821493b");
            System.err.println("Main base = " + mainBase);
            System.err.println("Base region = " + baseRegion);
            return false;
        }

        Collection<AChokepoint> chokes = baseRegion.getChokepoints();
        for (AChokepoint choke : chokes) {
            if (baseRegion.getChokepoints().contains(choke)) {
                System.out.println("Disabling choke point: " + APosition.create(choke.getCenter()));
                disabledChokepoints.add(choke);    //choke.setDisabled(true);
            }
        }

        return true;
    }

    /**
     * Returns true if given base location is free from units, meaning it's a good place for expansion.
     * <p>
     * Trying to avoid:
     * - existing buildings
     * - any enemy units
     * - planned constructions
     */
    public static boolean isBaseLocationFreeOfBuildingsAndEnemyUnits(ABaseLocation baseLocation) {

        // If we have any base, FALSE.
        if (Select.ourBases().inRadius(7, baseLocation.getPosition()).count() > 0) {
            return false;
        }

        // If any enemy unit is nearby
        if (Select.enemy().inRadius(11, baseLocation.getPosition()).count() > 0) {
            return false;
        }

        // Check for planned constructions
        for (ConstructionOrder constructionOrder : AConstructionRequests.getAllConstructionOrders()) {
            APosition constructionPlace = constructionOrder.getPositionToBuildCenter();
            if (constructionPlace != null && constructionPlace.distTo(baseLocation.getPosition()) < 8) {
                return false;
            }
        }

        // All conditions have been fulfilled.
        return true;
    }

    public static APosition getEnemyNatural() {
        APosition enemyBase = AEnemyUnits.getEnemyBase();
        if (enemyBase == null) {
            return null;
        }

        ABaseLocation baseLocation = AMap.getNaturalBaseLocation(enemyBase);
        if (baseLocation != null) {
            return baseLocation.getPosition();
        }

        return null;
    }

    public static AChokepoint getEnemyMainChokepoint() {
        APosition enemyMain = AEnemyUnits.getEnemyBase();
        if (enemyMain == null) {
            return null;
        }

        return getNearestChokepoint(enemyMain);
    }

    public static AChokepoint getEnemyNaturalChokepoint() {
        APosition enemyNatural = getEnemyNatural();
        if (enemyNatural == null) {
            return null;
        }

        return getChokepointForNaturalBase(enemyNatural);
    }

    /**
     * Warning - takes very long time.
     * <p>
     * Returns land distance (in tiles) between unit and given position.
     */
    public static double groundDist(AUnit unit, APosition runTo) {
        return BWTA.getGroundDistance(unit.getPosition().toTilePosition(), runTo.toTilePosition()) / 32;
    }

    public static boolean distanceToNearestChokeLessThan(AUnit unit, double dist) {
        for (AChokepoint choke : getChokePoints()) {
            if ((choke.getCenter().distTo(unit) - choke.getWidth()) <= dist) {
                return true;
            }
        }

        return false;
    }

    /**
     * Warning - takes very long time.
     *
     * Returns shortest land distance (in tiles) between unit and given position.
     */
//    public static double getShortestPath(AUnit unit, APosition runTo) {
//        return BWTA.get(unit.getPosition().toTilePosition(), runTo.toTilePosition()) / 32;
//    }

}
