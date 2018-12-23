package atlantis.information;

import atlantis.Atlantis;
import atlantis.constructing.AConstructionManager;
import atlantis.constructing.ConstructionOrder;
import atlantis.debug.APainter;
import atlantis.enemy.AEnemyUnits;
import atlantis.position.APosition;
import atlantis.position.PositionOperationsWrapper;
import atlantis.position.Positions;
import atlantis.units.AUnit;
import atlantis.units.Select;
import atlantis.util.AtlantisUtilities;
import atlantis.util.PositionUtil;
import bwem.BWEM;
import bwem.Base;
import bwem.ChokePoint;
import bwem.area.Area;
import bwem.map.Map;
import bwem.typedef.CPPath;
import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.WalkPosition;
import org.openbw.bwapi4j.type.Color;

import java.util.*;

/**
 * This class provides information about high-abstraction level map operations like returning place for the
 * next base or returning important choke point near the main base.
 */
public class AMap {

    private static BWEM bwem = null;
    private static Set<ChokePoint> disabledChokePoints = new HashSet<>();
    private static List<ChokePoint> cached_chokePoints = null;
    private static ChokePoint cached_mainBaseChokePoint = null;
    private static ChokePoint cached_naturalBaseChokePoint = null;
//    private static Map<String, Positions> areasToPolygonPoints = new HashMap<>();

    // =========================================================

    /**
     * Initialize BWEM map object.
     */
    public static void initializeMap() {
        bwem = new BWEM(Atlantis.getBW());
        bwem.initialize();
    }

    /**
     * Returns BWEM (BroodWar Easy Map) map object. Advanced terrain processing.
     */
    public static Map getMap() {
        return bwem.getMap();
    }

    /**
     * Returns BWMap map object, class returned by OpenBW. It's native StarCraft mostly.
     */
    public static BWMap getBWMap() {
        return Atlantis.getBW().getBWMap();
    }

    /**
     * Returns map width in tiles.
     */
    public static int getMapWidthInTiles() {
        return Atlantis.getBW().getBWMap().mapWidth();
    }

    /**
     * Returns map height in tiles.
     */
    public static int getMapHeightInTiles() {
        return Atlantis.getBW().getBWMap().mapHeight();
    }

    // === Choke points ========================================    

    /**
     * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This
     * method returns this choke point. It's perfect position to defend (because it's *choke* point).
     */
    public static ChokePoint getChokePointForMainBase() {
        if (cached_mainBaseChokePoint == null) {
            AUnit mainBase = Select.mainBase();
            if (mainBase != null) {

                // Define area where our main base is
                Area mainArea = getArea(mainBase.getPosition());
                // System.out.println("mainArea = " + mainArea);
                if (mainArea != null) {

                    // Define localization of the second base to expand
                    Base naturalBase = getNaturalBase(Atlantis.getInteraction().self()
                            .getStartLocation().toPosition());
                    // System.out.println("secondBase = " + secondBase);
                    if (naturalBase == null) {
                        return null;
                    }

                    // Define area of the second base
                    Area naturalBaseArea = naturalBase.getArea();
                    // System.out.println("secondArea = " + secondArea);
                    if (naturalBaseArea == null) {
                        return null;
                    }

                    // Try to match choke points between the two areas
                    for (ChokePoint mainAreaChoke : mainArea.getChokePoints()) {
                        // System.out.println("mainAreaChoke = " + mainAreaChoke + " / "
                        // + (mainAreaChoke.getFirstArea()) + " / " + (mainAreaChoke.getSecondArea()));
                        if (naturalBaseArea.equals(mainAreaChoke.getAreas().getFirst())    // getFirstArea()
                                || naturalBaseArea.equals(mainAreaChoke.getAreas().getSecond())) {    // getSecondArea()
                            cached_mainBaseChokePoint = mainAreaChoke;
                            // System.out.println("MAIN CHOKE FOUND! " + cached_mainBaseChokePoint);
                            break;
                        }
                    }

                    if (cached_mainBaseChokePoint == null) {
                        cached_mainBaseChokePoint = mainArea.getChokePoints().iterator().next();
                    }
                }
            }
        }

        return cached_mainBaseChokePoint;
    }

    /**
     * Returns chokePoint to defend for the natural (second) base.
     */
    public static ChokePoint getChokePointForNaturalBase() {
        if (cached_naturalBaseChokePoint != null) {
            APainter.paintCircle(APosition.create(cached_naturalBaseChokePoint.getCenter().toPosition()), 5, Color.WHITE);
            return cached_naturalBaseChokePoint;
        }

        // =========================================================

        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            System.err.println("Can't find natural base chokePoint");
            return null;
        }

        Area naturalArea = getArea(getNaturalBase(mainBase.getPoint()));
        if (naturalArea == null) {
            System.err.println("Can't find area for natural base");
            return null;
        }

        for (ChokePoint chokePoint : naturalArea.getChokePoints()) {
            APosition center = APosition.create(chokePoint.getCenter().toPosition());
            if (center.distanceTo(getChokePointForMainBase().getCenter().toPosition()) > 1) {
                cached_naturalBaseChokePoint = chokePoint;
                return cached_naturalBaseChokePoint;
            }
        }

        return null;
    }

    /**
     * Returns starting location that's nearest to given position and is not yet explored (black space, not
     * fog of war).
     */
    public static Base getNearestUnexploredStartingLocation(APosition nearestTo) {
        if (nearestTo == null) {
            return null;
        }

        // Get list of all starting locations
        Positions<Base> startingLocations = new Positions<>();
        startingLocations.addPositions(getStartingLocations(true));

        // Sort them all by closest to given nearestTo position
        startingLocations.sortByDistanceTo(nearestTo, true);

        // For every location...
        for (Base base : startingLocations.list()) {
            if (!isExplored(base.getCenter())) {
                return base;
            }
        }
        return null;
    }

    public static Base getStartingLocationBasedOnIndex(int index) {
        ArrayList<Base> bases = new ArrayList<>();
        bases.addAll(getStartingLocations(true));

        return bases.get(index % bases.size());
    }

    /**
     * Returns nearest free base location where we don't have base built yet.
     */
    public static Base getExpansionFreeBaseNearestTo(APosition nearestTo) {

        // Get list of all base locations
        Positions<Base> bases = new Positions<Base>();
        bases.addPositions(getBases());

        // Sort them all by closest to given nearestTo position
        if (nearestTo != null) {
            bases.sortByDistanceTo(nearestTo, true);
        }

        // For every location...
        for (Base base : bases.list()) {
            if (isBaseFreeOfBuildingsAndEnemyUnits(base)) {
                return base;
            }
        }
        return null;
    }

    /**
     * Returns free base location which is as far from enemy starting location as possible.
     */
    public static Base getExpansionBaseMostDistantToEnemy() {
        APosition farthestTo = AEnemyUnits.getEnemyBase();
        if (farthestTo == null) {
            return getExpansionFreeBaseNearestTo(Select.ourBases().first().getPosition());
        }

        // =========================================================

        // Get list of all base locations
        Positions<Base> bases = new Positions<Base>();
        bases.addPositions(getBases());

        // Sort them all by closest to given nearestTo position
        if (farthestTo != null) {
            bases.sortByDistanceTo(farthestTo, false);
        }

        // For every location...
        for (Base base : bases.list()) {
            if (isBaseFreeOfBuildingsAndEnemyUnits(base)) {
                return base;
            }
        }
        return null;
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static Base getNaturalBase() {
        return getNaturalBase(Select.mainBase().getPosition());
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static Base getNaturalBase(Object mainBasePosition) {
        Position nearestTo = mainBasePosition instanceof Position
                ? (Position) mainBasePosition
                : ((APosition) mainBasePosition).getPoint();

        // =========================================================

        // Get list of all base locations
        Positions<Base> bases = new Positions<Base>();
        bases.addPositions(getBases());

        // Sort them all by closest to given nearestTo position
        bases.sortByGroundDistanceTo(nearestTo, true);

        // Return second nearest location.
        int counter = 0;
        for (Base base : bases.list()) {
            if (counter > 0) {
                return base;
            }
            counter++;
        }
        return null;
    }

    /**
     * Returns random point on map with fog of war, preferably unexplored one.
     */
    public static APosition getRandomInvisiblePosition(APosition startPoint) {
        APosition position = null;
        for (int attempts = 0; attempts < 10; attempts++) {
            int maxRadius = 30 * TilePosition.SIZE_IN_PIXELS;
            int dx = -maxRadius + AtlantisUtilities.rand(0, 2 * maxRadius);
            int dy = -maxRadius + AtlantisUtilities.rand(0, 2 * maxRadius);
            position = PositionOperationsWrapper.translateByPixels(startPoint, dx, dy).makeValid();
            if (!isVisible(position)) {
                return position;
            }
        }
        return position;
    }

    /**
     * Returns nearest (preferably directly connected) area which has center of it still unexplored.
     */
    public static Area getNearestUnexploredArea(APosition position) {
        Area area = AMap.getArea(position);
        if (area == null) {
            return null;
        }

        Area areaToVisit = null;

        for (Area reachableArea : area.getAccessibleNeighbors()) {
            if (!AMap.isExplored(reachableArea.getWalkPositionWithHighestAltitude().toPosition())) {
                areaToVisit = reachableArea;
//                return APosition.createFrom(areaToVisit.getCenter());
                return areaToVisit;
            }
        }

        return null;
    }

    /**
     * Returns nearest land-connected position on map which hasn't been explored.
     */
    public static APosition getNearestUnexploredAccessiblePosition(APosition position) {
        int maxRadius = Math.max(getMapWidthInTiles(), getMapHeightInTiles());
        int currentRadius = 6;
        int step = 3;

        while (currentRadius < maxRadius) {
            double doubleCurrentRadius = currentRadius * 2;
            for (int dx = -currentRadius; dx <= currentRadius; dx += doubleCurrentRadius) {
                for (int dy = -currentRadius; dy <= currentRadius; dy += doubleCurrentRadius) {
                    APosition potentialPosition = position.translateByTiles(dx, dy).makeValid();
                    if (!isExplored(potentialPosition) && position.hasPathTo(potentialPosition)) {
                        return potentialPosition;
                    }
                }
            }

            currentRadius += 3;
        }

//        System.err.println("Can't find getNearestUnexploredAccessiblePosition");
        return null;
    }

    public static ChokePoint getNearestChokePoint(APosition position) {
        double bestDistance = 99999;
        ChokePoint bestChoke = null;

        for (ChokePoint chokePoint : getChokePoints()) {
//            double dist = position.distanceTo(chokePoint.getCenter().toPosition()) - chokePoint.getWidth() / 32 / 2;
            double dist = position.distanceTo(chokePoint.getCenter().toPosition());
            if (dist < bestDistance) {
                bestDistance = dist;
                bestChoke = chokePoint;
            }
        }

        return bestChoke;
    }

    /**
     * Can be used to avoid getting to close to the area edges, which may cause unit to get stuck.
     */
    public static double getDistanceToAnyAreaPolygonPoint(APosition unitPosition) {
        // @TODO
        return 100;
//        Area area = unitPosition.getArea();
//
//
//        if (area == null) {
//            System.err.println("isPositionFarFromAnyAreaPolygonPoint -> Area is null");
//            return 999;
//        }
//        if (area.getPolygon() == null) {
//            System.err.println("isPositionFarFromAnyAreaPolygonPoint -> area.getPolygon() is null");
//            return 999;
//        }
//
//        // === Define polygon points for given area ==============
//
//        Positions polygonPoints = new Positions();
//        if (areasToPolygonPoints.containsKey(area.toString())) {
//            polygonPoints = areasToPolygonPoints.get(area.toString());
//        }
//        else {
//            polygonPoints = new Positions();
//            polygonPoints.addPositions(area.getPolygon().getPoints());
//            areasToPolygonPoints.put(area.toString(), polygonPoints);
//        }
//
////        for (Positions positions : areasToPolygonPoints.values()) {
////            for (Iterator it = positions.arrayList().iterator(); it.hasNext();) {
////                Position position = (Position) it.next();
////                APainter.paintCircle(position, 13, Color.YELLOW);
////                APainter.paintCircle(position, 16, Color.YELLOW);
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
//        }
//        else {
//            return 99;
//        }
    }

    // =========================================================

    /**
     * Returns list of places that have geyser and mineral fields so they are the places where you could build
     * a base. Starting locations are also included here.
     */
    public static List<Base> getBases() {
        return getMap().getBases();
    }

    /**
     * Returns list of all places where players can start a game. Note, that if you play map for two players
     * and you know location of your own base. So you also know the location of enemy base (enemy *must* be
     * there), but still obviously you don't see him.
     */
    public static List<Base> getStartingLocations(boolean excludeOurStartLocation) {
        ArrayList<Base> startingLocations = new ArrayList<>();
        for (Base base : AMap.getBases()) {
            if (base.isStartingLocation()) {

                // Exclude our base location if needed.
                if (excludeOurStartLocation) {
                    AUnit mainBase = Select.mainBase();
                    if (mainBase != null && PositionUtil.distanceTo(mainBase, base.getCenter()) <= 10) {
                        continue;
                    }
                }

                startingLocations.add(base);
            }
        }
        return startingLocations;
    }

    /**
     * Returns list of all choke points i.e. places where suddenly it gets extra tight and fighting there
     * usually prefers ranged units. They are perfect places for terran bunkers.
     */
    public static List<ChokePoint> getChokePoints() {
        if (cached_chokePoints == null) {
            cached_chokePoints = new ArrayList<>();
            for (ChokePoint choke : getMap().getChokePoints()) {
                if (!disabledChokePoints.contains(choke)) { // choke.isDisabled()
                    cached_chokePoints.add(choke);
                }
            }
        }
        return cached_chokePoints;
    }

    /**
     * Returns area object for given <b>position</b>. This object provides some very helpful informations
     * like you can access list of choke points that belong to it etc.
     *
     * @see Area
     */
    public static Area getArea(Object positionOrAreaOrBase) {
        Position position = null;

        if (positionOrAreaOrBase instanceof Position) {
            position = (Position) positionOrAreaOrBase;
        } else if (positionOrAreaOrBase instanceof Area) {
            return (Area) positionOrAreaOrBase;
        } else if (positionOrAreaOrBase instanceof Base) {
            position = ((Base) positionOrAreaOrBase).getCenter();
        } else {
            System.err.println("getArea failed for " + positionOrAreaOrBase);
            return null;
        }

        return getMap().getArea(position.toTilePosition());
    }

    /**
     * Returns true if given position is explored i.e. if it's not black screen (but could be fog of war).
     */
    public static boolean isExplored(Position position) {
        return AMap.getBWMap().isExplored(position.toTilePosition());
    }

    /**
     * Returns true if given position visible.
     */
    public static boolean isVisible(Position position) {
        return AMap.getBWMap().isVisible(position.toTilePosition());
    }

    /**
     * Returns true if given position can be traversed by land units.
     */
    public static boolean isWalkable(APosition position) {
        return AMap.getBWMap().isWalkable(position.toWalkPosition());
    }

    // =========================================================
    // Special methods
    /**
     * Analyzing map and terrain is far from perfect. For many maps it happens that there are some choke
     * points near the main base which are completely invalid e.g. they lead to a dead-end or in the best case
     * are pointing to a place where the enemy won't come from. This method "disables" those points so they're
     * never returned, but they don't actually get removed. It only sets disabled=true flag for them.
     */
//    public static void disableSomeOfTheChokePoints() {
//        AUnit mainBase = Select.mainBase();
//        if (mainBase == null) {
//            return;
//        }
//
//        Area baseArea = getArea(mainBase.getPosition());
//        if (baseArea == null) {
//            System.err.println("Error #821493b");
//            System.err.println("Main base = " + mainBase);
//            System.err.println("Base area = " + baseArea);
//            return;
//        }
//
//        Collection<ChokePoint> chokes = baseArea.getChokePoints();
//        for (ChokePoint choke : chokes) {
//            if (baseArea.getChokePoints().contains(choke)) {
//                System.out.println("Disabling choke point: " + APosition.create(choke.getCenter()));
//                disabledChokePoints.add(choke);	//choke.setDisabled(true);
//            }
//        }
//    }

    /**
     * Returns true if given base location is free from units, meaning it's a good place for expansion.
     * <p>
     * Trying to avoid:
     * - existing buildings
     * - any enemy units
     * - planned constructions
     */
    public static boolean isBaseFreeOfBuildingsAndEnemyUnits(Base base) {

        // If we have any base, FALSE.
        if (Select.ourBases().inRadius(7, base.getCenter()).count() > 0) {
            return false;
        }

        // If any enemy unit is nearby
        if (Select.enemy().inRadius(11, base.getCenter()).count() > 0) {
            return false;
        }

        // Check for planned constructions
        for (ConstructionOrder constructionOrder : AConstructionManager.getAllConstructionOrders()) {
            APosition constructionPlace = constructionOrder.getPositionToBuildCenter();
            if (constructionPlace != null && constructionPlace.distanceTo(base.getCenter()) < 8) {
                return false;
            }
        }

        // All conditions have been fulfilled.
        return true;
    }

    /**
     * Warning - takes very long time.
     *
     * Returns land distance (in tiles) between unit and given position.
     */
//    public static double getGroundDistance(AUnit from, APosition to) {
////        return AMap.getMap().getPath()getGroundDistance(unit.getPosition().toTilePosition(), runTo.toTilePosition()) / 32;
////        Atlantis.getBW().
//        return Atlantis.getInteraction().
//        return AMap.getMap().getPath(from.getPosition(), to).size();
//    }


    /**
     * Warning - takes very long time.
     *
     * Returns shortest land distance (in tiles) between unit and given position.
     */
//    public static double getShortestPath(AUnit unit, APosition runTo) {
//        return BWTA.get(unit.getPosition().toTilePosition(), runTo.toTilePosition()) / 32;
//    }

    /**
     * An attempt to estimate choke point width.
     */
    public static double getChokePointWidth(ChokePoint chokePoint) {
        List<WalkPosition> geometry = chokePoint.getGeometry();
        return PositionUtil.distanceTo(geometry.get(1), geometry.get(2));
    }

    /**
     * @param from from
     * @param to   to
     * @return true if given two positions are connected and can walk from `from` to `to`.
     */
    public static boolean hasPath(Position from, Position to) {
        return AMap.getBWMap().hasPath(from, to);
    }

    public static double getGroundDistance(AUnit fromUnit, Position to) {
        return getGroundDistance(fromUnit.getPosition(), to);
    }

    public static double getGroundDistance(Position from, Position to) {
        double totalDistance = 0;
        CPPath path = AMap.getMap().getPath(from, to);
        Position lastPosition = from;

        for (Iterator<ChokePoint> it = path.iterator(); it.hasNext(); ) {
            Position chokePoint = it.next().getCenter().toPosition();
            totalDistance += PositionUtil.distanceTo(lastPosition, chokePoint);
            lastPosition = chokePoint;
        }
        totalDistance += PositionUtil.distanceTo(lastPosition, to);

        return totalDistance;
    }
}
