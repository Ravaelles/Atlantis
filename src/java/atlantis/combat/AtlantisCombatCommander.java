package atlantis.combat;

import jnibwapi.Unit;
import atlantis.combat.group.AtlantisGroupManager;
import atlantis.combat.group.Group;
import atlantis.combat.group.missions.Mission;
import atlantis.combat.group.missions.Missions;

public class AtlantisCombatCommander {

	/**
	 * This is the mission for main battle group forces. E.g. initially it will be DEFEND, then it should be PREPARE (go
	 * near enemy) and then ATTACK.
	 */
	private static Mission currentGlobalMission;

	// =========================================================

	/**
	 * Acts with all battle units.
	 */
	public static void update() {
		handleGlobalMission();
		handleAllBattleGroups();
	}

	// =========================================================

	/**
	 * Takes care of current strategy.
	 */
	private static void handleGlobalMission() {
		if (currentGlobalMission == null) {
			currentGlobalMission = Missions.DEFEND;
		}
	}

	/**
	 * Acts with all (combat) units that are part of a unit group.
	 */
	private static void handleAllBattleGroups() {
		for (Group group : AtlantisGroupManager.getGroups()) {
			handleBattleGroup(group);
		}
	}

	/**
	 * Acts with all units that are part of given battle group, according to the GroupMission object and using proper
	 * micro managers.
	 */
	private static void handleBattleGroup(Group group) {

		// Make sure this battle group has up-to-date strategy
		if (!currentGlobalMission.equals(group.getMission())) {
			group.setMission(currentGlobalMission);
		}

		// Act with every unit
		for (Unit unit : group.arrayList()) {

			// Never interrupt shooting units
			if (shouldNotDisturbUnit(unit)) {
				return;
			}

			// Handle generic actions according to current mission (e.g. DEFEND, ATTACK)
			boolean microDisallowed = group.getMission().update(unit);

			if (!microDisallowed) {

				// Handle micro-managers for given unit according to its type
				if (unit.isMeleeUnit()) {
					group.getMicroMeleeManager().update(unit);
				} else {
					group.getMicroRangedManager().update(unit);
				}
			}
		}
	}

	// =========================================================

	private static boolean shouldNotDisturbUnit(Unit unit) {
		return unit.isStartingAttack();
	}

}
