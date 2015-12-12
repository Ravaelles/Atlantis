package atlantis.information;

import atlantis.Atlantis;
import atlantis.util.RUtilities;
import atlantis.wrappers.Positions;
import atlantis.wrappers.SelectUnits;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jnibwapi.BaseLocation;
import jnibwapi.ChokePoint;
import jnibwapi.Map;
import jnibwapi.Position;
import jnibwapi.Region;
import jnibwapi.Unit;

/**
 * This class provides information about high-abstraction level map operations like returning place for the
 * next base or returning important choke point near the main base.
 */
public class AtlantisMap {

    private static List<ChokePoint> cached_chokePoints = null;
    private static ChokePoint cached_mainBaseChokepoint = null;

    // =========================================================
    /**
     * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This
     * method returns this choke point. It's perfect position to defend (because it's *choke* point).
     */
    public static ChokePoint getMainBaseChokepoint() {
        if (cached_mainBaseChokepoint == null) {
            Unit mainBase = SelectUnits.mainBase();
            // System.out.println("mainBase = " + mainBase);
            if (mainBase != null) {

                // Define region where our main base is
                Region mainRegion = getRegion(mainBase);
                // System.out.println("mainRegion = " + mainRegion);
                if (mainRegion != null) {

                    // Define localization of the second base to expand
                    BaseLocation secondBase = getSecondNearestBaseLocation(Atlantis.getBwapi().getSelf()
                            .getStartLocation());
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
                    for (ChokePoint mainRegionChoke : mainRegion.getChokePoints()) {
                        // System.out.println("mainRegionChoke = " + mainRegionChoke + " / "
                        // + (mainRegionChoke.getFirstRegion()) + " / " + (mainRegionChoke.getSecondRegion()));
                        if (secondRegion.equals(mainRegionChoke.getFirstRegion())
                                || secondRegion.equals(mainRegionChoke.getSecondRegion())) {
                            cached_mainBaseChokepoint = mainRegionChoke;
                            // System.out.println("MAIN CHOKE FOUND! " + cached_mainBaseChokepoint);
                            break;
                        }
                    }

                    if (cached_mainBaseChokepoint == null) {
                        cached_mainBaseChokepoint = mainRegion.getChokePoints().iterator().next();
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

        // Get list of all starting locations
        Positions<BaseLocation> startingLocations = new Positions<BaseLocation>();
        startingLocations.addPositions(getStartingLocations());

        // Sort them all by closest to given nearestTo position
        startingLocations.sortByDistanceTo(nearestTo, true);

        // For every location...
        for (BaseLocation baseLocation : startingLocations.list()) {
            if (!isExplored(baseLocation)) {
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
            int maxRadius = 30;
            int dx = -maxRadius + RUtilities.rand(0, 2 * maxRadius);
            int dy = -maxRadius + RUtilities.rand(0, 2 * maxRadius);
            position = startPoint.translated(dx, dy).makeValid();
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
    public static Map getMap() {
        return Atlantis.getBwapi().getMap();
    }

    /**
     * Returns list of places that have geyser and mineral fields so they are the places where you could build
     * a base. Starting locations are also included here.
     */
    public static List<BaseLocation> getBaseLocations() {
        return Atlantis.getBwapi().getMap().getBaseLocations();
    }

    /**
     * Returns list of all places where players can start a game. Note, that if you play map for two players
     * and you know location of your own base. So you also know the location of enemy base (enemy *must* be
     * there), but still obviously you don't see him.
     */
    public static List<BaseLocation> getStartingLocations() {
        ArrayList<BaseLocation> startingLocations = new ArrayList<>();
        for (BaseLocation baseLocation : AtlantisMap.getBaseLocations()) {
            if (baseLocation.isStartLocation()) {
                startingLocations.add(baseLocation);
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
            for (ChokePoint choke : Atlantis.getBwapi().getMap().getChokePoints()) {
                if (!choke.isDisabled()) {
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
        return Atlantis.getBwapi().getMap().getRegion(position);
    }

    /**
     * Returns true if given position is explored i.e. if it's not black screen (but could be fog of war).
     */
    public static boolean isExplored(Position position) {
        return Atlantis.getBwapi().isExplored(position);
    }

    /**
     * Returns true if given position visible.
     */
    public static boolean isVisible(Position position) {
        return Atlantis.getBwapi().isVisible(position);
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
        Unit mainBase = SelectUnits.mainBase();
        if (mainBase == null) {
            System.out.println("Error #821493a");
            return;
        }

        Region baseRegion = getRegion(mainBase);
        if (baseRegion == null) {
            System.out.println("Error #821493b");
            return;
        }

        Collection<ChokePoint> chokes = baseRegion.getChokePoints();
        for (ChokePoint choke : chokes) {
            if (baseRegion.getChokePoints().contains(choke)) {
                System.out.println("Disabling choke point: " + choke);
                choke.setDisabled(true);
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

}
