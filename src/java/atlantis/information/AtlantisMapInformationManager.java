package atlantis.information;

import java.util.Set;

import jnibwapi.ChokePoint;
import jnibwapi.Unit;
import atlantis.Atlantis;
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
				Set<ChokePoint> chokePoints = Atlantis.getBwapi().getMap().getRegion(mainBase).getChokePoints();
				if (!chokePoints.isEmpty()) {
					cached_mainBaseChokepoint = chokePoints.iterator().next();
				}
			}
		}

		return cached_mainBaseChokepoint;
	}

}
