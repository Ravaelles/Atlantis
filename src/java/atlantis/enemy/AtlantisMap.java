package atlantis.enemy;

import atlantis.Atlantis;
import atlantis.util.PositionUtil;
import atlantis.util.AtlantisUtilities;
import atlantis.wrappers.Positions;
import atlantis.wrappers.Select;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import bwapi.Position;
import bwapi.TilePosition;
import bwta.Region;
import bwta.Chokepoint;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;

/**
 * This class provides information about high-abstraction level map operations like returning place for the
 * next base or returning important choke point near the main base.
 */
public class AtlantisMap {

    private static List<Chokepoint> cached_chokePoints = null;
    private static Chokepoint cached_mainBaseChokepoint = null;
    private static Set<Chokepoint> disabledChokepoints = new HashSet<>();
    private static BWTA bwta = new BWTA();	//all methods in BWTA are static, but I keep a class instance to return it in getMap()

    // =========================================================
    /**
     * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This
     * method returns this choke point. It's perfect position to defend (because it's *choke* point).
     */
    public static Chokepoint getMainBaseChokepoint() {
        if (cached_mainBaseChokepoint == null) {
            Unit mainBase = Select.mainBase();
            if (mainBase != null) {

                // Define region where our main base is
                Region mainRegion = getRegion(mainBase.getPosition());
                // System.out.println("mainRegion = " + mainRegion);
                if (mainRegion != null) {

                    // Define localization of the second base to expand
                    BaseLocation secondBase = getSecondNearestBaseLocation(Atlantis.getBwapi().self()
                            .getStartLocation().toPosition());
                    // System.out.println("secondBase = " + secondBase);
                    if (secondBase == null) {
                        return null;
                    }

                    // Define region of the second base
                    Region secondRegion = secondBase.getRegion();
                    // System.out.println("secondRegion = " + secondRegion);
                    if (secondRegion == null) {
                        return null;
                    }

                    // Try to match choke points between the two regions
                    for (Chokepoint mainRegionChoke : mainRegion.getChokepoints()) {
                        // System.out.println("mainRegionChoke = " + mainRegionChoke + " / "
                        // + (mainRegionChoke.getFirstRegion()) + " / " + (mainRegionChoke.getSecondRegion()));
                        if (secondRegion.equals(mainRegionChoke.getRegions().first)	// getFirstRegion()
                                || secondRegion.equals(mainRegionChoke.getRegions().second)) {	// getSecondRegion()
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
     * Returns starting location that's nearest to given position and is not yet explored (black space, not
     * fog of war).
     */
    public static BaseLocation getNearestUnexploredStartingLocation(Position nearestTo) {
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
    public static BaseLocation getNearestBaseLocationToExpand(Position nearestTo) {

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
     * Returns nearest base location (by the actual ground distance) to the given base location.
     */
    private static BaseLocation getSecondNearestBaseLocation(Position nearestTo) {

        // Get list of all base locations
        Positions<BaseLocation> baseLocations = new Positions<BaseLocation>();
        baseLocations.addPositions(getBaseLocations());

        // Sort them all by closest to given nearestTo position
        baseLocations.sortByDistanceTo(nearestTo, true);

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
    public static Position getRandomInvisiblePosition(Position startPoint) {
        Position position = null;
        for (int attempts = 0; attempts < 10; attempts++) {
            int maxRadius = 30 * TilePosition.SIZE_IN_PIXELS;	//TODO: check whether this scaling to TilePosition is oK
            int dx = -maxRadius + AtlantisUtilities.rand(0, 2 * maxRadius);
            int dy = -maxRadius + AtlantisUtilities.rand(0, 2 * maxRadius);
            position = PositionUtil.translate(startPoint, dx, dy).makeValid();
            if (!isVisible(position)) {
                return position;
            }
        }
        return position;
    }

    // =========================================================
    // Generic methods - wrappers for JNIBWAPI methods
    /**
     * Returns map object.
     */
    public static BWTA getMap() {
        return bwta;
    }

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
        for (BaseLocation baseLocation : AtlantisMap.getBaseLocations()) {
            if (baseLocation.isStartLocation()) {
                
                // Exclude our base location if needed.
                if (excludeOurStartLocation) {
                    Unit mainBase = Select.mainBase();
                    if (mainBase != null && PositionUtil.distanceTo(mainBase.getPosition(), baseLocation.getPosition()) <= 10) {
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
    public static Region getRegion(Position position) {
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

    // =========================================================
    // Special methods
    /**
     * Analyzing map and terrain is far from perfect. For many maps it happens that there are some choke
     * points near the main base which are completely invalid e.g. they lead to a dead-end or in the best case
     * are pointing to a place where the enemy won't come from. This method "disables" those points so they're
     * never returned, but they don't actually get removed. It only sets disabled=true flag for them.
     */
    public static void disableSomeOfTheChokePoints() {
        Unit mainBase = Select.mainBase();
        if (mainBase == null) {
            System.out.println("Error #821493a");
            return;
        }

        Region baseRegion = getRegion(mainBase.getPosition());
        if (baseRegion == null) {
            System.out.println("Error #821493b");
            return;
        }

        Collection<Chokepoint> chokes = baseRegion.getChokepoints();
        for (Chokepoint choke : chokes) {
            if (baseRegion.getChokepoints().contains(choke)) {
                System.out.println("Disabling choke point: " + choke);
                disabledChokepoints.add(choke);	//choke.setDisabled(true);
            }
        }

        // MapPoint secondBaseLocation =
        // TerranCommandCenter.getSecondBaseLocation();
        // System.out.println("secondBaseLocation = " + secondBaseLocation);
        // Collection<ChokePoint> chokes =
        // MapExploration.getChokePointsNear(
        // secondBaseLocation, 20);
        // Region baseRegion =
        // xvr.getBwapi().getMap().getRegion(xvr.getFirstBase());
        // for (ChokePoint choke : chokes) {
        // if (baseRegion.getChokePoints().contains(choke)) {
        // // chokePointsProcessed.remove(choke);
        // System.out.println("Disabling choke point: " + choke);
        // choke.setDisabled(true);
        // }
        // }
    }

    private static boolean isBaseLocationFreeOfBuildingsAndEnemyUnits(BaseLocation baseLocation) {
        
        // If we have any base, FALSE.
        if (Select.ourBases().inRadius(7, baseLocation.getPosition()).count() > 0) {
            return false;
        }
        
        // If any enemy unit is nearby
        if (Select.enemy().inRadius(11, baseLocation.getPosition()).count() > 0) {
            return false;
        }
        
        // All conditions have been fulfilled.
        return true;
    }

}
