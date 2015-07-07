package atlantis.combat;

import jnibwapi.Unit;
import atlantis.combat.group.AtlantisGroupManager;
import atlantis.combat.group.Group;

public class AtlantisCombatCommander {

	/**
	 * Acts with all battle units.
	 */
	public static void update() {
		handleAllBattleGroups();
	}

	// =========================================================

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
		for (Unit unit : group.arrayList()) {

			// Never interrupt shooting units
			if (shouldNotDisturbUnit(unit)) {
				return;
			}

			// Handle generic actions according to current mission (e.g. DEFEND, ATTACK)
			group.getMission().update(unit);

			// Handle micro-managers for given unit according to its type
			if (unit.isRangedUnit()) {
				group.getMicroRangedManager().update(unit);
			} else if (unit.isMeleeUnit()) {
				group.getMicroMeleeManager().update(unit);
			}
		}
	}

	// =========================================================

	private static boolean shouldNotDisturbUnit(Unit unit) {
		return unit.isStartingAttack();
	}

}
