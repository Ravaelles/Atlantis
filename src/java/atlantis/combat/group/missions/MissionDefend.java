package atlantis.combat.group.missions;

import jnibwapi.ChokePoint;
import jnibwapi.Position;
import jnibwapi.Unit;
import atlantis.information.AtlantisMapInformationManager;

public class MissionDefend extends Mission {

	@Override
	public void update(Unit unit) {
		if (shouldNotDisturbUnit(unit)) {
			return;
		}

		if (moveUnitIfNeededNearChokePoint(unit)) {
			return;
		}
	}

	private boolean shouldNotDisturbUnit(Unit unit) {
		return unit.isAttacking() || unit.isStartingAttack() || unit.isMoving();
	}

	// =========================================================

	/**
	 * Unit will go towards important choke point near main base.
	 */
	private boolean moveUnitIfNeededNearChokePoint(Unit unit) {

		ChokePoint chokepoint = AtlantisMapInformationManager.getMainBaseChokepoint();
		if (chokepoint != null) {
			double dDistance = -1.9; // Smaller this value is, closer the unit will come to the choke point
			double distToChoke = chokepoint.distanceTo(unit) + dDistance; // Distance to the center of choke point
			double unitShootRange = unit.getShootRangeGround();

			// If unit is close enough, make it stop
			if (unitShootRange >= distToChoke) {
				unit.attack((Position) unit, false);
			}

			// Unit should go nearer
			else {
				unit.attack(chokepoint, false);
			}
		}

		return false;
	}
}
