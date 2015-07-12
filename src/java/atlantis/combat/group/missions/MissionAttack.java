package atlantis.combat.group.missions;

import jnibwapi.Position;
import jnibwapi.Unit;
import atlantis.information.AtlantisEnemyInformationManager;

public class MissionAttack extends Mission {

	@Override
	public boolean update(Unit unit) {
		return false;
	}

	// =========================================================

	// =========================================================

	/**
	 * Do not interrupt unit if it is engaged in combat.
	 */
	@Override
	protected boolean canIssueOrderToUnit(Unit unit) {
		if (unit.isAttacking() || unit.isStartingAttack() || unit.isRunning()) {
			return false;
		}

		return true;
	}

	public static Position getFocusPoint() {
		return AtlantisEnemyInformationManager.getEnemyBase();
	}
}
