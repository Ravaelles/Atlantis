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
import bwapi.Color;
import bwapi.Position;
import bwapi.TilePosition;
import bwta.BWTA;
import bwta.BaseLocation;
import bwta.Chokepoint;
import bwta.Region;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides information about high-abstraction level map operations like returning place for the
 * next base or returning important choke point near the main base.
 */
public class AMap {

    private static BWTA bwta = new BWTA(); // all methods in BWTA are static, but I keep a class instance to return it in getMap()
    private static Set<Chokepoint> disabledChokepoints = new HashSet<>();
    private static List<Chokepoint> cached_chokePoints = null;
    private static Chokepoint cached_mainBaseChokepoint = null;
    private static Chokepoint cached_naturalBaseChokepoint = null;
    private static Map<String, Positions> regionsToPolygonPoints = new HashMap<>();

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
        return Atlantis.getBwapi().mapWidth();
    }

    /**
     * Returns map height in tiles.
     */
    public static int getMapHeightInTiles() {
        return Atlantis.getBwapi().mapHeight();
    }

    // === Choke points ========================================    
    
    /**
     * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This
     * method returns this choke point. It's perfect position to defend (because it's *choke* point).
     */
    public static Chokepoint getChokepointForMainBase() {
        if (cached_mainBaseChokepoint == null) {
            AUnit mainBase = Select.mainBase();
            if (mainBase != null) {

                // Define region where our main base is
                Region mainRegion = getRegion(mainBase.getPosition());
                // System.out.println("mainRegion = " + mainRegion);
                if (mainRegion != null) {

                    // Define localization of the second base to expand
                    BaseLocation naturalBase = getNaturalBaseLocation(Atlantis.getBwapi().self()
                            .getStartLocation().toPosition());
                    // System.out.println("secondBase = " + secondBase);
                    if (naturalBase == null) {
                        return null;
                    }

                    // Define region of the second base
                    Region naturalBaseRegion = naturalBase.getRegion();
                    // System.out.println("secondRegion = " + secondRegion);
                    if (naturalBaseRegion == null) {
                        return null;
                    }

                    // Try to match choke points between the two regions
                    for (Chokepoint mainRegionChoke : mainRegion.getChokepoints()) {
                        // System.out.println("mainRegionChoke = " + mainRegionChoke + " / "
                        // + (mainRegionChoke.getFirstRegion()) + " / " + (mainRegionChoke.getSecondRegion()));
                        if (naturalBaseRegion.equals(mainRegionChoke.getRegions().first)	// getFirstRegion()
                                || naturalBaseRegion.equals(mainRegionChoke.getRegions().second)) {	// getSecondRegion()
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
    public static Chokepoint getChokepointForNaturalBase() {
        if (cached_naturalBaseChokepoint != null) {
            APainter.paintCircle(APosition.create(cached_naturalBaseChokepoint.getCenter()), 5, Color.White);
            return cached_naturalBaseChokepoint;
        }
        
        // =========================================================
        
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            System.err.println("Can't find natural base chokepoint");
            return null;
        }
        
        Region naturalRegion = getRegion(getNaturalBaseLocation(mainBase.getPoint()));
        
        for (Chokepoint chokepoint : naturalRegion.getChokepoints()) {
            APosition center = APosition.create(chokepoint.getCenter());
            if (center.distanceTo(getChokepointForMainBase().getCenter()) > 1) {
                cached_naturalBaseChokepoint = chokepoint;
                return cached_naturalBaseChokepoint;
            }
        }
        
        return null;
    }

    /**
     * Returns starting location that's nearest to given position and is not yet explored (black space, not
     * fog of war).
     */
    public static BaseLocation getNearestUnexploredStartingLocation(APosition nearestTo) {
        if (nearestTo == null) {
            return null;
        }

        // Get list of all starting locations
        Positions<BaseLocation> startingLocations = new Positions<BaseLocation>();
        startingLocations.addPositions(getStartingLocations(true));

        // Sort them all by closest to given nearestTo position
        startingLocations.sortByDistanceTo(nearestTo, true);

        // For every location...
        for (BaseLocation baseLocation : startingLocations.list()) {
            if (!isExplored(baseLocation.getPosition())) {
                return baseLocation;
            }
        }
        return null;
    }
    
    public static BaseLocation getStartingLocationBasedOnIndex(int index) {
        ArrayList<BaseLocation> baseLocations = new ArrayList<>();
        baseLocations.addAll(getStartingLocations(true));
        
        return baseLocations.get(index % baseLocations.size());
    }

    /**
     * Returns nearest free base location where we don't have base built yet.
     */
    public static BaseLocation getExpansionFreeBaseLocationNearestTo(APosition nearestTo) {

        // Get list of all base locations
        Positions<BaseLocation> baseLocations = new Positions<BaseLocation>();
        baseLocations.addPositions(getBaseLocations());

        // Sort them all by closest to given nearestTo position
        if (nearestTo != null) {
            baseLocations.sortByDistanceTo(nearestTo, true);
        }

        // For every location...
        for (BaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation)) {
                return baseLocation;
            }
        }
        return null;
    }

    /**
     * Returns free base location which is as far from enemy starting location as possible.
     */
    public static BaseLocation getExpansionBaseLocationMostDistantToEnemy() {
        APosition farthestTo = AEnemyUnits.getEnemyBase();
        if (farthestTo == null) {
            return getExpansionFreeBaseLocationNearestTo(Select.ourBases().first().getPosition());
        }
        
        // =========================================================

        // Get list of all base locations
        Positions<BaseLocation> baseLocations = new Positions<BaseLocation>();
        baseLocations.addPositions(getBaseLocations());

        // Sort them all by closest to given nearestTo position
        if (farthestTo != null) {
            baseLocations.sortByDistanceTo(farthestTo, false);
        }

        // For every location...
        for (BaseLocation baseLocation : baseLocations.list()) {
            if (isBaseLocationFreeOfBuildingsAndEnemyUnits(baseLocation)) {
                return baseLocation;
            }
        }
        return null;
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static BaseLocation getNaturalBaseLocation() {
        return getNaturalBaseLocation(Select.mainBase().getPosition());
    }

    /**
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    public static BaseLocation getNaturalBaseLocation(Object mainBasePosition) {
        Position nearestTo = mainBasePosition instanceof Position 
                ? (Position) mainBasePosition 
                : ((APosition) mainBasePosition).getPoint();
        
        // =========================================================

        // Get list of all base locations
        Positions<BaseLocation> baseLocations = new Positions<BaseLocation>();
        baseLocations.addPositions(getBaseLocations());

        // Sort them all by closest to given nearestTo position
        baseLocations.sortByGroundDistanceTo(nearestTo, true);

        // Return second nearest location.
        int counter = 0;
        for (BaseLocation baseLocation : baseLocations.list()) {
            if (counter > 0) {
                return baseLocation;
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
     * Returns nearest (preferably directly connected) region which has center of it still unexplored.
     */
    public static Region getNearestUnexploredRegion(APosition position) {
        Region region = AMap.getRegion(position);
        Region regionToVisit = null;
        
        for (Region reachableRegion : region.getReachableRegions()) {
            if (!AMap.isExplored(reachableRegion.getCenter())) {
                regionToVisit = reachableRegion;
//                return APosition.createFrom(regionToVisit.getCenter());
                return regionToVisit;
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
                    APosition potentialPosition = position.translateByTiles(dx, dy).makeValidFarFromBounds();
                    if (!isExplored(potentialPosition) && position.hasPathTo(potentialPosition)) {
//                        System.out.println(potentialPosition);
                        return potentialPosition;
                    }
                }
            }
            
            currentRadius += 3;
        }
        
        System.err.println("Can't find getNearestUnexploredAccessiblePosition");
        return null;
    }
    
    public static Chokepoint getNearestChokepoint(APosition position) {
        double bestDistance = 99999;
        Chokepoint bestChoke = null;
        
        for (Chokepoint chokePoint : getChokePoints()) {
            double dist = position.distanceTo(chokePoint.getCenter()) - chokePoint.getWidth() / 32 / 2;
            if (dist < bestDistance) {
                bestDistance = dist;
                bestChoke = chokePoint;
            }
        }
        
        return bestChoke;
    }
    
    /**
     * Can be used to avoid getting to close to the region edges, which may cause unit to get stuck.
     */
    public static boolean isPositionFarFromAnyRegionPolygonPoint(AUnit unit) {
        Region region = unit.getPosition().getRegion();
        
        if (region == null) {
            System.err.println("isPositionFarFromAnyRegionPolygonPoint -> Region is null");
            return false;
        }
        if (region.getPolygon() == null) {
            System.err.println("isPositionFarFromAnyRegionPolygonPoint -> region.getPolygon() is null");
            return false;
        }

        // === Define polygon points for given region ==============
        
        Positions polygonPoints = new Positions();
        if (regionsToPolygonPoints.containsKey(region.toString())) {
            polygonPoints = regionsToPolygonPoints.get(region.toString());
        }
        else {
            polygonPoints = new Positions();
            polygonPoints.addPositions(region.getPolygon().getPoints());
            regionsToPolygonPoints.put(region.toString(), polygonPoints);
        }
        APosition nearestPolygon = polygonPoints.nearestTo(unit.getPosition());
        
        // =========================================================
        
        if (nearestPolygon != null && nearestPolygon.distanceTo(unit) < 3) {
            return false;
        }
        else {
            return true;
        }
    }
    
    // =========================================================
    
    /**
     * Returns list of places that have geyser and mineral fields so they are the places where you could build
     * a base. Starting locations are also included here.
     */
    public static List<BaseLocation> getBaseLocations() {
        return BWTA.getBaseLocations();
    }

    /**
     * Returns list of all places where players can start a game. Note, that if you play map for two players
     * and you know location of your own base. So you also know the location of enemy base (enemy *must* be
     * there), but still obviously you don't see him.
     */
    public static List<BaseLocation> getStartingLocations(boolean excludeOurStartLocation) {
        ArrayList<BaseLocation> startingLocations = new ArrayList<>();
        for (BaseLocation baseLocation : AMap.getBaseLocations()) {
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
    public static List<Chokepoint> getChokePoints() {
        if (cached_chokePoints == null) {
            cached_chokePoints = new ArrayList<>();
            for (Chokepoint choke : BWTA.getChokepoints()) {
                if (!disabledChokepoints.contains(choke)) { // choke.isDisabled()
                    cached_chokePoints.add(choke);
                }
            }
        }
        return cached_chokePoints;
    }

    /**
     * Returns region object for given <b>position</b>. This object provides some very helpful informations
     * like you can access list of choke points that belong to it etc.
     *
     * @see Region
     */
    public static Region getRegion(Object positionOrRegionOrBaseLocation) {
        Position position = null;
        
        if (positionOrRegionOrBaseLocation instanceof Position) {
            position = (Position) positionOrRegionOrBaseLocation;
        }
        else if (positionOrRegionOrBaseLocation instanceof Region) {
            position = ((Region) positionOrRegionOrBaseLocation).getCenter();
        }
        else if (positionOrRegionOrBaseLocation instanceof BaseLocation) {
            position = ((BaseLocation) positionOrRegionOrBaseLocation).getPosition();
        }
        else {
            System.err.println("getRegion failed for " + positionOrRegionOrBaseLocation);
            return null;
        }
        
        return BWTA.getRegion(position);
    }

    /**
     * Returns true if given position is explored i.e. if it's not black screen (but could be fog of war).
     */
    public static boolean isExplored(Position position) {
        return Atlantis.getBwapi().isExplored(position.toTilePosition());
    }

    /**
     * Returns true if given position visible.
     */
    public static boolean isVisible(Position position) {
        return Atlantis.getBwapi().isVisible(position.toTilePosition());
    }

    /**
     * Returns true if given position can be traversed by land units.
     */
    public static boolean isWalkable(APosition position) {
        return Atlantis.getBwapi().isWalkable(position.getX() / 8, position.getY() / 8);
    }
    
    // =========================================================
    // Special methods
    /**
     * Analyzing map and terrain is far from perfect. For many maps it happens that there are some choke
     * points near the main base which are completely invalid e.g. they lead to a dead-end or in the best case
     * are pointing to a place where the enemy won't come from. This method "disables" those points so they're
     * never returned, but they don't actually get removed. It only sets disabled=true flag for them.
     */
    public static void disableSomeOfTheChokePoints() {
        AUnit mainBase = Select.mainBase();
        if (mainBase == null) {
            return;
        }

        Region baseRegion = getRegion(mainBase.getPosition());
        if (baseRegion == null) {
            System.err.println("Error #821493b");
            System.err.println("Main base = " + mainBase);
            System.err.println("Base region = " + baseRegion);
            return;
        }

        Collection<Chokepoint> chokes = baseRegion.getChokepoints();
        for (Chokepoint choke : chokes) {
            if (baseRegion.getChokepoints().contains(choke)) {
                System.out.println("Disabling choke point: " + APosition.create(choke.getCenter()));
                disabledChokepoints.add(choke);	//choke.setDisabled(true);
            }
        }
    }

    /**
     * Returns true if given base location is free from units, meaning it's a good place for expansion.
     * 
     * Trying to avoid:
     * - existing buildings
     * - any enemy units
     * - planned constructions
     */
    public static boolean isBaseLocationFreeOfBuildingsAndEnemyUnits(BaseLocation baseLocation) {
        
        // If we have any base, FALSE.
        if (Select.ourBases().inRadius(7, baseLocation.getPosition()).count() > 0) {
            return false;
        }
        
        // If any enemy unit is nearby
        if (Select.enemy().inRadius(11, baseLocation.getPosition()).count() > 0) {
            return false;
        }
        
        // Check for planned constructions
        for (ConstructionOrder constructionOrder : AConstructionManager.getAllConstructionOrders()) {
            APosition constructionPlace = constructionOrder.getPositionToBuildCenter();
            if (constructionPlace != null && constructionPlace.distanceTo(baseLocation.getPosition()) < 8) {
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
    public static double getGroundDistance(AUnit unit, APosition runTo) {
        return BWTA.getGroundDistance(unit.getPosition().toTilePosition(), runTo.toTilePosition()) / 32;
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
