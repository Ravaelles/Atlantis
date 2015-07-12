package atlantis.combat.group.missions;

import jnibwapi.ChokePoint;
import jnibwapi.Unit;
import atlantis.information.AtlantisMapInformationManager;
import atlantis.wrappers.SelectUnits;

public class MissionDefend extends Mission {

	@Override
	public boolean update(Unit unit) {
		if (canIssueOrderToUnit(unit)) {
			if (moveUnitIfNeededNearChokePoint(unit)) {
				return true;
			}
		}

		return false;
	}

	// =========================================================

	/**
	 * Unit will go towards important choke point near main base.
	 */
	private boolean moveUnitIfNeededNearChokePoint(Unit unit) {
		ChokePoint chokepoint = getFocusPoint();
		if (chokepoint == null) {
			System.err.println("Couldn't define choke point.");
			return false;
		}

		// =========================================================
		// Normal orders

		// Check if shouldn't disturb unit
		if (canIssueOrderToUnit(unit)) {

			// Too close to
			if (isCriticallyCloseToChokePoint(unit, chokepoint)) {
				unit.moveAwayFrom(chokepoint, 1.0);
				unit.setTooltip("Get back");
				return true;
			}

			// Unit is quite close to the choke point
			if (isCloseEnoughToChokePoint(unit, chokepoint)) {

				// Too many stacked units
				if (isTooManyUnitsAround(unit, chokepoint)) {
					unit.moveAwayFrom(chokepoint, 1.0);
					unit.setTooltip("Stacked");
				}

				// Units aren't stacked too much
				else {
				}
			}

			// Unit is far from choke point
			else {
				unit.move(chokepoint, false);
			}
		}

		return false;
	}

	private boolean isTooManyUnitsAround(Unit unit, ChokePoint chokepoint) {
		return SelectUnits.ourCombatUnits().inRadius(1.0, unit).count() >= 3;
	}

	private boolean isCloseEnoughToChokePoint(Unit unit, ChokePoint chokepoint) {
		if (unit == null || chokepoint == null) {
			return false;
		}

		// Bigger this value is, further from choke will units stand
		double standFurther = 1.6;

		// Distance to the center of choke point
		double distToChoke = chokepoint.distanceTo(unit) - chokepoint.getRadiusInTiles();

		// How far can the unit shoot
		double unitShootRange = unit.getShootRangeGround();

		// Define max allowed distance from choke point to consider "still close"
		double maxDistanceAllowed = unitShootRange + standFurther;

		return distToChoke <= maxDistanceAllowed;
	}

	private boolean isCriticallyCloseToChokePoint(Unit unit, ChokePoint chokepoint) {
		if (unit == null || chokepoint == null) {
			return false;
		}

		// Distance to the center of choke point
		double distToChoke = chokepoint.distanceTo(unit) - chokepoint.getRadiusInTiles();

		// Can't be closer than X from choke point
		if (distToChoke <= 4.8) {
			return true;
		}

		// Bigger this value is, further from choke will units stand
		double standFurther = 1;

		// How far can the unit shoot
		double unitShootRange = unit.getShootRangeGround();

		// Define max distance
		double maxDistance = unitShootRange + standFurther;

		return distToChoke <= maxDistance;
	}

	// =========================================================

	public static ChokePoint getFocusPoint() {
		return AtlantisMapInformationManager.getMainBaseChokepoint();
	}

	/**
	 * Do not interrupt unit if it is engaged in combat.
	 */
	@Override
	protected boolean canIssueOrderToUnit(Unit unit) {

		// If unit has far more important actions than fucking positioning, disallow any actions here.
		if (unit.isAttacking() || unit.isStartingAttack() || unit.isRunning()) {
			return false;
		}

		// If enemy is close, also it's dumb to do proper positioning. Let the MicroManager decide.
		Unit nearestEnemy = SelectUnits.enemy().nearestTo(unit);
		if (nearestEnemy != null && nearestEnemy.distanceTo(unit) < 13) {
			return false;
		}

		return true;
	}
}
