package atlantis.combat.micro;

import jnibwapi.Unit;
import jnibwapi.types.UnitType.UnitTypes;
import atlantis.wrappers.SelectUnits;

public class DefaultRangedManager extends MicroRangedManager {

	@Override
	public void update(Unit unit) {
		if (canIssueOrderToUnit(unit)) {

			// SPECIAL UNIT TYPE action
			if (unit.isType(UnitTypes.Terran_Medic)) {
				if (MicroMedic.update(unit)) {
					return;
				}
			}

			// =========================================================
			// STANDARD actions

			Unit nearestEnemy = SelectUnits.enemy().nearestTo(unit);
			if (nearestEnemy != null) {
				// double distToEnemy = nearestEnemy.distanceTo(unit);
				// double distToMainBase = unit.distanceTo(SelectUnits.mainBase());
				// double enemyDistToDefendedPosition = nearestEnemy.distanceTo(SelectUnits.mainBase());
				//
				// // If unit has mission defend, don't attack close targets if further than X
				// if (unit.getGroup().getMission().equals(Missions.DEFEND)) {
				// boolean enemyVeryFarFromBase = enemyDistToDefendedPosition > 20
				// && nearestEnemy.distanceGroundTo(SelectUnits.mainBase()) > 30;
				// if (enemyVeryFarFromBase) {
				// return;
				// }
				//
				// }

				// // Run from the enemy
				// if (distToEnemy < 0.8 && unit.getHitPoints() < 30) {
				// unit.runFrom(nearestEnemy);
				// }
				//
				// // Pursue and attack the enemy
				// else {
				// if (distToEnemy > unit.getShootRangeAgainst(nearestEnemy)) {
				unit.attackUnit(nearestEnemy, false);
				// }
				// }
			}
		}
	}

	// =========================================================

	private boolean canIssueOrderToUnit(Unit unit) {
		if (unit.isStartingAttack()) {
			return false;
		}

		return true;
	}

}
