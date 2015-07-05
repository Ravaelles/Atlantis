package atlantis.information;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jnibwapi.BaseLocation;
import jnibwapi.ChokePoint;
import jnibwapi.Position;
import jnibwapi.Region;
import jnibwapi.Unit;
import atlantis.Atlantis;
import atlantis.wrappers.Positions;
import atlantis.wrappers.SelectUnits;

/**
 * This class provides information about high-abstraction level map operations like returning place for the next base or
 * returning important choke point near the main base.
 */
public class AtlantisMapInformationManager {

	private static ChokePoint cached_mainBaseChokepoint = null;

	// =========================================================

	/**
	 * Every starting location in BroodWar AI tournament has exactly one critical choke point to defend. This method
	 * returns this choke point. It's perfect position to defend (because it's *choke* point).
	 */
	public static ChokePoint getMainBaseChokepoint() {
		if (cached_mainBaseChokepoint == null) {
			Unit mainBase = SelectUnits.mainBase();
			if (mainBase != null) {
				Region region = getRegion(mainBase);
				if (region != null) {
					Set<ChokePoint> chokePoints = region.getChokePoints();
					if (!chokePoints.isEmpty()) {
						cached_mainBaseChokepoint = chokePoints.iterator().next();
					}
				}
			}
		}

		return cached_mainBaseChokepoint;
	}

	/**
	 * Returns starting location that's nearest to given position and is not yet explored (black space, not fog of war).
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

	// =========================================================
	// Generic methods - wrappers for JNIBWAPI methods

	/**
	 * Returns list of places that have geyser and mineral fields so they are the places where you could build a base.
	 * Starting locations are also included here.
	 */
	public static List<BaseLocation> getBaseLocations() {
		return Atlantis.getBwapi().getMap().getBaseLocations();
	}

	/**
	 * Returns list of all places where players can start a game. Note, that if you play map for two players and you
	 * know location of your own base. So you also know the location of enemy base (enemy *must* be there), but still
	 * obviously you don't see him.
	 */
	public static List<BaseLocation> getStartingLocations() {
		ArrayList<BaseLocation> startingLocations = new ArrayList<>();
		for (BaseLocation baseLocation : AtlantisMapInformationManager.getBaseLocations()) {
			if (baseLocation.isStartLocation()) {
				startingLocations.add(baseLocation);
			}
		}
		return startingLocations;
	}

	/**
	 * Returns list of all choke points i.e. places where suddenly it gets extra tight and fighting there usually
	 * prefers ranged units. They are perfect places for terran bunkers.
	 */
	public static List<ChokePoint> getChokePoints() {
		return Atlantis.getBwapi().getMap().getChokePoints();
	}

	/**
	 * Returns region object for given <b>position</b>. This object provides some very helpful informations like you can
	 * access list of choke points that belong to it etc.
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

}
