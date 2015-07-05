package atlantis.combat.group;

import atlantis.combat.group.missions.Mission;
import atlantis.wrappers.Units;

/**
 * Represents battle group (unit squad) that contains multiple battle units (could be one unit as well).
 */
public class Group extends Units {

	/**
	 * Convenience name for the group e.g. "Alpha", "Bravo", "Delta".
	 */
	private String name;

	/**
	 * Current mission for this group.
	 */
	private Mission mission;

	// =========================================================

	private Group(String name, Mission mission) {
		super();
		this.name = name;
		this.mission = mission;
	}

	// =========================================================

	/**
	 * Creates new group, designated by the given name. If <b>name</b> is null, default numeration "Alpha", "Bravo",
	 * "Charlie", "Delta" will be used.
	 */
	public static Group createNewGroup(String name, Mission mission) {

		// Name is null, use autonaming
		if (name == null) {
			String[] names = new String[] { "Alpha", "Bravo", "Charlie", "Delta", "Echo" };
			name = names[AtlantisGroupCommander.groups.size()];
		}

		Group group = new Group(name, mission);
		return group;
	}

	// =========================================================

	/**
	 * Convenience name for the group e.g. "Alpha", "Bravo", "Charlie", "Delta".
	 */
	public String getName() {
		return name;
	}

	/**
	 * Convenience name for the group e.g. "Alpha", "Bravo", "Charlie", "Delta".
	 */
	public void setName(String name) {
		this.name = name;
	}

}
