package atlantis.combat.group;

import java.util.ArrayList;

import jnibwapi.Unit;
import atlantis.combat.group.missions.Missions;

/**
 * Commands all existing battle groups.
 */
public class AtlantisGroupCommander {

	/**
	 * List of all unit groups.
	 */
	protected static ArrayList<Group> groups = new ArrayList<>();

	// =========================================================

	public static void battleUnitCreated(Unit unit) {
		Group group = getAlphaGroup();
		group.addUnit(unit);
		unit.setGroup(group);
	}

	public static void battleUnitDestroyed(Unit unit) {
		Group group = unit.getGroup();
		if (group != null) {
			group.removeUnit(unit);
			unit.setGroup(null);
		}
	}

	// =========================================================
	// Manage squads

	/**
	 * Get first, main group of units.
	 */
	private static Group getAlphaGroup() {

		// If no group exists, create main group
		if (groups.isEmpty()) {
			Group unitGroup = Group.createNewGroup(null, Missions.DEFEND);
			groups.add(unitGroup);
		}

		return groups.get(0);
	}

}
