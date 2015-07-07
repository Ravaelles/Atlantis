package atlantis.combat.micro;

import jnibwapi.Unit;
import atlantis.combat.group.missions.Missions;
import atlantis.wrappers.SelectUnits;

public class DefaultRangedManager extends MicroRangedManager {

	@Override
	public void update(Unit unit) {
		if (shouldNotInterrupt(unit)) {
			Unit nearestEnemy = SelectUnits.enemy().nearestTo(unit);
			if (nearestEnemy != null) {
				double distToEnemy = nearestEnemy.distanceTo(unit);
				double distToMainBase = unit.distanceTo(SelectUnits.mainBase());
				double enemyDistToMainBase = nearestEnemy.distanceTo(SelectUnits.mainBase());

				// If unit has mission defend, don't attack close targets if further than X
				if (unit.getGroup().getMission().equals(Missions.DEFEND)
						&& (distToEnemy >= 10 || distToMainBase <= enemyDistToMainBase + 7)) {
					return;
				}

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

	private boolean shouldNotInterrupt(Unit unit) {
		if (unit.isStartingAttack()) {
			return true;
		}

		return false;
	}

}
