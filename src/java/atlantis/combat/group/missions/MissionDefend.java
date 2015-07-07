package atlantis.combat.group.missions;

import jnibwapi.ChokePoint;
import jnibwapi.Unit;
import atlantis.AtlantisGame;
import atlantis.information.AtlantisMapInformationManager;

public class MissionDefend extends Mission {

	@Override
	public void update(Unit unit) {
		if (moveUnitIfNeededNearChokePoint(unit)) {
			return;
		}
	}

	// =========================================================

	/**
	 * Unit will go towards important choke point near main base.
	 */
	private boolean moveUnitIfNeededNearChokePoint(Unit unit) {
		ChokePoint chokepoint = AtlantisMapInformationManager.getMainBaseChokepoint();
		if (chokepoint != null) {

			// Smaller this value is, closer the units will come to the choke point
			double dDistance = -5 + (-AtlantisGame.rand(1, 23) / 10.0) / (chokepoint.getRadius() / 32.0);

			// Distance to the center of choke point
			double distToChoke = chokepoint.distanceTo(unit) + dDistance;

			// How far can the unit shoot
			double unitShootRange = unit.getShootRangeGround();

			// If unit is close enough to the choke point, make it hold its position
			if (unitShootRange >= distToChoke) {
				unit.holdPosition(true);
			}

			// Unit should go nearer
			else {
				if (!unit.isHoldingPosition() && AtlantisGame.rand(1, 100) <= 1) {
					unit.move(chokepoint, false);
				}
			}
		}

		return false;
	}
}
